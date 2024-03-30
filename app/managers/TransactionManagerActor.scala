package managers

import akka.actor.SupervisorStrategy.Escalate
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props, Terminated}
import com.bansal.washcatalog.catalog.SlickDbUtil.{db, runDbAction}
import com.bansal.washcatalog.catalog.common.CommonEnums.{AccountType, TransactionStatus}
import com.bansal.washcatalog.catalog.common.Constants.SUCCESS
import com.bansal.washcatalog.catalog.common.SlickEnumMappers._
import com.bansal.washcatalog.catalog.domain.{Account, AccountTransaction}
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

/**
 * Manager for handling transaction lifecycles.
 * Transaction is in planned -> check for accounts both exist and timestamp < (T - 60seconds) -> trying
 * Transaction is in trying ->
 *    check for account_dr type asset with balance>= amount
 *        deduct amount and credit amount
 *        committed
 *    else -> rejected.
 */
class TransactionManagerActor extends Actor with ActorLogging {
  import TransactionManagerActor._
  import DBExecutionManager._
  implicit val executionContext = context.dispatcher

  override def preStart(): Unit = {
    log.info(s"Starting supervisor")
    context.system.scheduler.scheduleAtFixedRate(5.seconds, 10.seconds, self, CheckNow)
  }

  override def receive: Receive = checkPlanned

  def checkPlanned: Receive = {
    case CheckNow =>
      log.info(s"Checking now for planned..")
      val query = for {
        (atplanned, accounts) <- AccountTransaction.table.filter(_.state === TransactionStatus.planned) joinLeft
          Account.table on ((at, a) => at.account_dr_id === a.id || at.account_dr_id === a.id)
      } yield (atplanned, accounts)
      val transactionsWAccounts = Await.result(db.run(query.result), 10.seconds)
      val currentTime = System.currentTimeMillis()
      val (revoked, trying) = transactionsWAccounts.groupBy(_._1).view.mapValues(_.map(_._2)).partition {
        case (at, accounts) => accounts.count(_.isDefined) != 2 || at.timestamp.getTime < (currentTime - 60000)
      }
      val updatequery = DBIO.seq(AccountTransaction.table.filter(_.id inSet revoked.keys.map(_.id)).map(_.state).update(TransactionStatus.revoked),
        AccountTransaction.table.filter(_.id inSet trying.keys.map(_.id)).map(_.state).update(TransactionStatus.trying)
      )
      val updateresult = Await.result(runDbAction(updatequery), 10.seconds)
      updateresult match {
        case SUCCESS =>
          log.info("Updated planned transactions successfully!!")
          context.become(checkTrying(trying.keys.toSeq))
        case err =>
          log.warning("Failed to update planned transactions..")
          sender() ! Escalate
      }
  }

  def checkTrying(transactions: Seq[AccountTransaction]): Receive = {
    case CheckNow =>
      log.info(s"[${self.path}] Checking now for trying..")
      val query = for {
        transaction <- AccountTransaction.table.filter(_.state === TransactionStatus.trying)
        sender <- Account.table.filter(_.id === transaction.account_dr_id)
        receiver <- Account.table.filter(_.id === transaction.account_cr_id)
      } yield (transaction, sender, receiver)
      val transactionsWAccounts = Await.result(db.run(query.result), 10.seconds)
      val (committed, rejected) = transactionsWAccounts.partition {
        case (at, sender, _) => (sender.balance >= at.amount && sender.account_type == AccountType.asset) ||
          sender.account_type == AccountType.revenue
      }
      val updateQuery = DBIO.seq(AccountTransaction.table.filter(_.id inSet committed.map(_._1.id)).map(_.state).update(TransactionStatus.committed),
        AccountTransaction.table.filter(_.id inSet rejected.map(_._1.id)).map(_.state).update(TransactionStatus.rejected))
      val updateResult = Await.result(runDbAction(updateQuery), 10.seconds)
      updateResult match {
        case SUCCESS =>
          val child = context.actorOf(Props[DBExecutionManager].withDispatcher("db-dispatcher"))
          context.watch(child)
          child ! ExecuteUpdateAccountBalance(committed)
        case err =>
          log.warning("Failed to update trying transactions..")
          sender() ! Escalate //restart transaction manager and bring to checkPlanned
      }
    case ChildSuccess(ref) =>
      log.info(s"Updated related account balances, killing ${ref.path.name}")
      ref ! PoisonPill
    case ChildFailure(err) =>
      log.info(s"Failed to udpate account balances due to ${err}, reverting..")
      Await.result(runDbAction(AccountTransaction.table.filter(_.id inSet transactions.map(_.id)).map(_.state).update(TransactionStatus.planned)), 10.seconds)
      context.become(checkPlanned)
    case Terminated(ref) =>
      log.info(s"Terminated child ${ref.path.name}")
      context.become(checkPlanned)
  }
}

object TransactionManagerActor {
  case object CheckNow

  def props()(implicit actorSystem: ActorSystem): Props = Props(new TransactionManagerActor())
}
