package com.bansal.washcatalog.catalog.domain

import akka.actor.ActorSystem
import com.bansal.washcatalog.catalog.SlickDbUtil.{db, runDbAction}
import com.bansal.washcatalog.catalog.common.Constants.SUCCESS
import com.bansal.washcatalog.catalog.common.{Entities, TableMapping}
import com.bansal.washcatalog.catalog.models.LanguageTable
import com.fasterxml.jackson.annotation.JsonFormat
import play.api.Logging
import play.api.libs.json.JsValue
import play.api.libs.ws.WSClient
import slick.jdbc.MySQLProfile.api._
import slick.lifted.TableQuery
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}
import slick.jdbc.MySQLProfile
import utils.ObjectMapperUtil.objectMapper

import java.sql.Timestamp
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}


case class LanguageInput(
                        lang_code: String,
                        name_eng: String,
                        name_native: String
                        ) extends Input
case class Language(
                   id: String,
                   name_eng: String,
                   name_native: String,
                  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss.SSSZ", timezone= "EET") created_on: Timestamp = new Timestamp(System.currentTimeMillis()),
                   @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss.SSSZ", timezone= "EET") modified_on: Timestamp = new Timestamp(System.currentTimeMillis())
                   ) extends RestEntityElement

object Language extends RestEntity[Language] with Logging {
  val table: TableQuery[LanguageTable] = TableQuery[LanguageTable]
  lazy val fieldMapping: Map[String, TableMapping]= Map(
    "id" -> TableMapping("lang_code", "Language"),
    "name_eng" -> TableMapping("name_eng", "Language"),
    "name_native" -> TableMapping("name_native", "Language"),
  )

  def inputToTable(input: LanguageInput): Language = {
    Language(id = input.lang_code, name_eng = input.name_eng, name_native = input.name_native)
  }

  override def getByIdQuery(id: String): FixedSqlStreamingAction[Seq[Language], Language, Effect.Read] = {
    table.filter(_.id === id).result
  }

  override protected def getAllQuery: FixedSqlStreamingAction[Seq[Language], Language, Effect.Read] = table.result

  override protected def getAllWithQuery(queryMap: Map[String, Seq[String]]): FixedSqlStreamingAction[Seq[Language], Language, Effect.Read] = {
    Entities.predicatePush(queryMap, Language.table, Entities.caseclassToMap[Language]).asInstanceOf[Query[LanguageTable, Language, Seq]]
      .sortBy(_.modified_on.desc.nullsLast).result
  }

  def addQuery(input: Input)(implicit ex: ExecutionContext): (FixedSqlAction[Int, NoStream, Effect.Write], String) = {
    val languageInput = input.asInstanceOf[LanguageInput]
    (table += inputToTable(languageInput), languageInput.lang_code)
  }

  override def add(input: JsValue)(implicit ex: ExecutionContext, ws: WSClient, actorSystem: ActorSystem, headers: Map[String, Seq[String]]): Future[PWResp] = {
    val query = addQuery(objectMapper.readValue(input.toString(), classOf[LanguageInput]))
    runDbAction(DBIO.seq(query._1))(logger).flatMap {
      case SUCCESS => getById(query._2, isSimple = false)
      case exception => Future(PWResp(success = false, error = Some(s"Error ${exception} while adding language")))
    }
  }

  override def getById(id: String, isSimple: Boolean)(implicit ex: ExecutionContext, ws: WSClient, actorSystem: ActorSystem, headers: Map[String, Seq[String]]): Future[PWResp] = super.getById(id)


}
