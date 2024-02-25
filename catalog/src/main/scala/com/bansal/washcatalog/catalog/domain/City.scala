package com.bansal.washcatalog.catalog.domain

import akka.actor.ActorSystem
import com.bansal.washcatalog.catalog.SlickDbUtil.{db, runDbAction}
import com.bansal.washcatalog.catalog.common.Constants.SUCCESS
import com.bansal.washcatalog.catalog.common.Entities
import com.bansal.washcatalog.catalog.domain.Language.{addQuery, getById, logger}
import com.bansal.washcatalog.catalog.models.{CityTable, LanguageTable}
import com.fasterxml.jackson.annotation.JsonFormat
import play.api.Logging
import play.api.libs.json.JsValue
import play.api.libs.ws.WSClient
import slick.dbio.Effect
import slick.lifted.TableQuery
import slick.jdbc.MySQLProfile.api._
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}
import utils.ObjectMapperUtil.objectMapper

import java.sql.Timestamp
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Try

case class CityInput(name: String, lang_code: String) extends Input

case class City(
   id: String,
   city_id: Option[Int]= None,
   name: String,
   lang_code: String,
   @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss.SSSZ", timezone= "EET") created_on: Timestamp = new Timestamp(System.currentTimeMillis()),
   @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss.SSSZ", timezone= "EET") modified_on: Timestamp = new Timestamp(System.currentTimeMillis())) extends RestEntityElement

case class CityOutput(id: String, name: String, language: Language, created_on: String, modified_on: String) extends RestEntityElement

object City extends RestEntity[City] with Logging {
  val table: TableQuery[CityTable]= TableQuery[CityTable]

  def tupleToCity(id: Option[Int], name: String, lang_code: String, created_on: Timestamp, modified_on: Timestamp): City = {
    City(id.map(_.toString).getOrElse("0"), id, name, lang_code, created_on, modified_on)
  }

  def cityToTuple(city: City): Option[(Option[Int], String, String, Timestamp, Timestamp)] = {
    Some(city.city_id, city.name, city.lang_code, city.created_on, city.modified_on)
  }

  def inputToTable(input: CityInput): City= {
    City(id= "0", None, name= input.name, lang_code= input.lang_code)
  }

  def processQuery(id: Option[String]= None)(implicit ex: ExecutionContext): Future[PWResp]  = {
    val query= (for {
      cty <- table.filterOpt(id){
        case (tbl, idd) => tbl.city_id === idd.toInt
      }
      lng <- Language.table.filter(_.id === cty.lang_code)
    } yield (cty, lng)).result
    logger.info(s"RUNNING QUERY: ${query.statements.mkString("\n")}")
    db.run(query).map { resp =>
      resp.isEmpty match {
        case true => PWResp(success = false, error = Some("City not found"))
        case false => PWResp(success = true, Some(resp.map(cl => CityOutput(cl._1.id, cl._1.name, cl._2, cl._1.created_on.toString, cl._1.modified_on.toString))))
      }
    }
  }

  override protected def getByIdQuery(id: String): FixedSqlStreamingAction[Seq[City], City, Effect.Read] = table.filter(_.city_id === id.toInt).result

  override protected def getAllQuery: FixedSqlStreamingAction[Seq[City], City, Effect.Read] = table.result

  override protected def getAllWithQuery(queryMap: Map[String, Seq[String]]): FixedSqlStreamingAction[Seq[City], City, Effect.Read] = {
    Entities.predicatePush(
        queryMap,
        table,
        Entities.caseclassToMap[City]).asInstanceOf[Query[CityTable, City, Seq]]
      .sortBy(_.city_id.asc.nullsLast).result
  }

  def addQuery(input: Input)(implicit ex: ExecutionContext): FixedSqlAction[Any, NoStream, Effect.Write] = {
    val ctyInput = input.asInstanceOf[CityInput]
    val insertQuery = table returning table.map(_.city_id) into((obj, id) => obj.copy(id = id.toString))
    insertQuery += inputToTable(ctyInput)
  }

  def deleteQuery(id: String)(implicit ex: ExecutionContext): Future[String] = {
    db.run(table.filter(_.city_id === id.toInt).delete).flatMap(resp => Future.successful(resp.toString))
  }

  override def add(input: JsValue)(implicit ex: ExecutionContext, ws: WSClient, actorSystem: ActorSystem, headers: Map[String, Seq[String]]): Future[PWResp] = {
    val query = addQuery(objectMapper.readValue(input.toString(), classOf[CityInput]))
    db.run(query).flatMap {
      case cty: City => getById(cty.id, isSimple = false)
      case _ => Future(PWResp(success = false, error = Some("Error while adding city")))
    }
  }

  override def getById(id: String, isSimple: Boolean)(implicit ex: ExecutionContext, ws: WSClient, actorSystem: ActorSystem, headers: Map[String, Seq[String]]): Future[PWResp] = {
    if(isSimple) {
      super.getById(id)
    } else {
      processQuery(Some(id))
    }
  }

  override def getAll(isSimple: Boolean = false)(implicit ex: ExecutionContext, ws: WSClient, actorSystem: ActorSystem, headers: Map[String, Seq[String]]): Future[PWResp] = {
    if(isSimple) {
      super.getAll()
    } else {
      processQuery()
    }
  }

  override def getWithQuery(queryMap: Map[String, Seq[String]], isSimple: Boolean)(implicit ex: ExecutionContext, ws: WSClient, actorSystem: ActorSystem, headers: Map[String, Seq[String]]): Future[PWResp] = {
    if (isSimple){
      super.getWithQuery(queryMap, isSimple)
    } else {
      val query = for {
        cty <- getAllWithQuery(queryMap)
        lng <- Language.table.filter(_.id inSet cty.map(_.lang_code).toSet).result
      } yield (cty, lng)
      db.run(query).map { resp =>
        resp._1.isEmpty match {
          case true => PWResp(success = false, error = Some("City not found"))
          case false => PWResp(success = true, Some(resp._1.map(ct => CityOutput(ct.id, ct.name, resp._2.find(_.id == ct.lang_code).get, ct.created_on.toString, ct.modified_on.toString))))
        }
      }
    }
  }
}
