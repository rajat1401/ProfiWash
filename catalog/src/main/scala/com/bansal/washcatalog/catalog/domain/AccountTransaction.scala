package com.bansal.washcatalog.catalog.domain

import akka.actor.ActorSystem
import com.bansal.washcatalog.catalog.SlickDbUtil.runDbAction
import com.bansal.washcatalog.catalog.common.CommonEnums.TransactionStatus
import com.bansal.washcatalog.catalog.common.CommonEnums.TransactionStatus.TransactionStatus
import com.bansal.washcatalog.catalog.common.Constants.SUCCESS
import com.bansal.washcatalog.catalog.common.Entities
import com.bansal.washcatalog.catalog.common.SlickEnumMappers.TransactionStatusType
import com.bansal.washcatalog.catalog.models.AccountTransactionTable
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import play.api.libs.json.JsValue
import play.api.libs.ws.WSClient
import slick.dbio.Effect
import slick.lifted.{Query, TableQuery}
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}
import slick.jdbc.MySQLProfile.api._
import utils.ObjectMapperUtil.objectMapper

import java.sql.Timestamp
import scala.concurrent.{ExecutionContext, Future}

case class AccountTransactionInput(
                                  account_dr_id: String,
                                  account_cr_id: String,
                                  amount: Double,
                                  @JsonScalaEnumeration(classOf[TransactionStatusType]) state: TransactionStatus = TransactionStatus.planned
                                  ) extends Input
case class AccountTransaction(
  id: String = java.util.UUID.randomUUID().toString,
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss.SSSZ", timezone= "GMT+0200")timestamp: Timestamp = new Timestamp(System.currentTimeMillis()),
  account_dr_id: String,
  account_cr_id: String,
  amount: Double,
  @JsonScalaEnumeration(classOf[TransactionStatusType]) state: TransactionStatus = TransactionStatus.planned
) extends RestEntityElement

object AccountTransaction extends RestEntity[AccountTransaction] {
  val table = TableQuery[AccountTransactionTable]
  override protected def getByIdQuery(id: String): FixedSqlStreamingAction[Seq[AccountTransaction], AccountTransaction, Effect.Read] = table.filter(_.id === id).result

  override protected def getAllQuery: FixedSqlStreamingAction[Seq[AccountTransaction], AccountTransaction, Effect.Read] = table.result

  override protected def getAllWithQuery(queryMap: Map[String, Seq[String]]): FixedSqlStreamingAction[Seq[AccountTransaction], RestEntityElement, Effect.Read] = {
    Entities.predicatePush(
        queryMap,
        table,
        Entities.caseclassToMap[AccountTransaction]).asInstanceOf[Query[AccountTransactionTable, AccountTransaction, Seq]]
      .sortBy(_.timestamp.desc.nullsLast).result
  }

  def inputToTable(input: AccountTransactionInput): AccountTransaction = {
    AccountTransaction(
      account_dr_id = input.account_dr_id,
      account_cr_id = input.account_cr_id,
      state = input.state,
      amount = input.amount
    )
  }

  def addQuery(input: AccountTransactionInput): (FixedSqlAction[Int, NoStream, Effect.Write], String) = {
    val accounttransaction = inputToTable(input)
    (table += accounttransaction, accounttransaction.id)
  }

  override def add(input: JsValue)(implicit ex: ExecutionContext, ws: WSClient, actorSystem: ActorSystem, headers: Map[String, Seq[String]]): Future[PWResp] = {
    val query = addQuery(objectMapper.readValue(input.toString(), classOf[AccountTransactionInput]))
    runDbAction(query._1).flatMap{
      case SUCCESS => getById(query._2)
      case err => Future(PWResp(success= false, error = Some(err)))
    }
  }
}
