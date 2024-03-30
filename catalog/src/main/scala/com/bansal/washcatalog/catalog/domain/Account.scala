package com.bansal.washcatalog.catalog.domain

import akka.actor.ActorSystem
import com.bansal.washcatalog.catalog.SlickDbUtil.runDbAction
import com.bansal.washcatalog.catalog.common.CommonEnums.AccountType
import com.bansal.washcatalog.catalog.common.CommonEnums.AccountType.AccountType
import com.bansal.washcatalog.catalog.common.Constants.SUCCESS
import com.bansal.washcatalog.catalog.common.Entities
import com.bansal.washcatalog.catalog.common.SlickEnumMappers.AccountTypeType
import com.bansal.washcatalog.catalog.models.{AccountTable, CityTable}
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import play.api.Logging
import play.api.libs.json.JsValue
import play.api.libs.ws.WSClient
import slick.dbio.Effect
import slick.lifted.{Query, TableQuery}
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}
import slick.jdbc.MySQLProfile.api._
import utils.ObjectMapperUtil.objectMapper

import java.sql.Timestamp
import scala.concurrent.{ExecutionContext, Future}

case class AccountInput(
                      @JsonScalaEnumeration(classOf[AccountTypeType]) account_type: AccountType,
                      balance: Double = 0.0
                       ) extends Input
case class Account(
                  id: String = java.util.UUID.randomUUID().toString,
                  @JsonScalaEnumeration(classOf[AccountTypeType]) account_type: AccountType,
                  balance: Double,
                  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss.SSSZ", timezone = "EET") created_on: Timestamp = new Timestamp(System.currentTimeMillis()),
                  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss.SSSZ", timezone = "EET") modified_on: Timestamp = new Timestamp(System.currentTimeMillis())
                  ) extends RestEntityElement

object Account extends RestEntity[Account] with Logging {
  val table = TableQuery[AccountTable]

  def inputToTable(input: AccountInput): Account = {
    Account(
      account_type = input.account_type,
      balance = input.balance
    )
  }

  def addQuery(input: Input): (FixedSqlAction[Int, NoStream, Effect.Write], String) = {
    val account = inputToTable(input.asInstanceOf[AccountInput])
    (table += account, account.id)
  }

  override protected def getByIdQuery(id: String): FixedSqlStreamingAction[Seq[Account], Account, Effect.Read] = table.filter(_.id === id).result

  override protected def getAllQuery: FixedSqlStreamingAction[Seq[Account], Account, Effect.Read] = table.result

  override protected def getAllWithQuery(queryMap: Map[String, Seq[String]]): FixedSqlStreamingAction[Seq[Account], Account, Effect.Read] = {
    Entities.predicatePush(
        queryMap,
        table,
        Entities.caseclassToMap[Account]).asInstanceOf[Query[AccountTable, Account, Seq]]
      .sortBy(_.balance.desc.nullsLast).result
  }

  override def getById(id: String, isSimple: Boolean)(implicit ex: ExecutionContext, ws: WSClient, actorSystem: ActorSystem, headers: Map[String, Seq[String]]): Future[PWResp] = {
    super.getById(id, isSimple)
  }

  override def add(input: JsValue)(implicit ex: ExecutionContext, ws: WSClient, actorSystem: ActorSystem, headers: Map[String, Seq[String]]): Future[PWResp] = {
    val query = addQuery(objectMapper.readValue(input.toString(), classOf[AccountInput]))
    runDbAction(query._1).flatMap {
      case SUCCESS => getById(query._2)
      case err => Future(PWResp(success= false, error = Some(err)))
    }

  }
}
