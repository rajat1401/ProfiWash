package managers

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.bansal.washcatalog.catalog.SlickDbUtil.{db, runDbAction}
import com.bansal.washcatalog.catalog.common.Constants.SUCCESS
import com.bansal.washcatalog.catalog.domain.{Account, AccountTransaction}
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class DBExecutionManager extends Actor with ActorLogging {
  import DBExecutionManager._
  override def receive: Receive = {
    case ExecuteUpdateAccountBalance(committed) =>
      val balanceUpdateMap = committed.flatMap { t =>
        Seq((t._2.id, -t._1.amount), (t._3.id, t._1.amount))
      }.groupBy(_._1).view.mapValues(_.map(_._2).sum).toMap
      val updateAction = DBIO.seq(balanceUpdateMap.map { case (accountId, amount) =>
        sqlu"UPDATE account SET balance = balance + $amount WHERE id = $accountId"
      }.toSeq: _*)
      val updateresult = Await.result(runDbAction(updateAction), 10.seconds)
      updateresult match {
        case SUCCESS => sender() ! ChildSuccess(self)
        case err => sender() ! ChildFailure(err.toString)
      }
  }
}

object DBExecutionManager {
  case class ChildSuccess(ref: ActorRef)
  case class ChildFailure(error: String)
  case class ExecuteUpdateDBIOAction(action: DBIOAction[Unit, NoStream, Effect.Write])
  case class ExecuteUpdateAccountBalance(committedTransactions: Seq[(AccountTransaction, Account, Account)])
}
