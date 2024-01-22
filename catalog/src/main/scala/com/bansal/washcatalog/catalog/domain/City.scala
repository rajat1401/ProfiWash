package com.bansal.washcatalog.catalog.domain

import akka.actor.ActorSystem
import com.bansal.washcatalog.catalog.SlickDbUtil.db
import com.bansal.washcatalog.catalog.common.Entities
import com.bansal.washcatalog.catalog.models.{CityTable, LanguageTable}
import com.fasterxml.jackson.annotation.JsonFormat
import play.api.Logging
import play.api.libs.ws.WSClient
import slick.dbio.Effect
import slick.lifted.TableQuery
import slick.jdbc.MySQLProfile.api._
import slick.sql.FixedSqlStreamingAction

import java.sql.Timestamp
import scala.concurrent.{ExecutionContext, Future}

case class CityInput(name: String, lang_code: String)

case class City(
   id: String,
   name: String,
   lang_code: String,
   @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss.SSSZ", timezone= "EET") created_on: Timestamp = new Timestamp(System.currentTimeMillis()),
   @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss.SSSZ", timezone= "EET") modified_on: Timestamp = new Timestamp(System.currentTimeMillis())) extends RestEntityElement

case class CityOutput(id: String, name: String, language: Language, created_on: String, modified_on: String) extends RestEntityElement

object City extends RestEntity[City] with Logging {
  val table: TableQuery[CityTable]= TableQuery[CityTable]

  def inputToTable(inputRec: CityInput): City = {
    City(
      id = table.size.asInstanceOf[String],
      name= inputRec.name,
      lang_code = inputRec.lang_code,
    )
  }

  override protected def getByIdQuery(id: String): FixedSqlStreamingAction[Seq[City], City, Effect.Read] = table.filter(_.id === id.toInt).result

  override protected def getAllQuery: FixedSqlStreamingAction[Seq[City], City, Effect.Read] = table.result

//  override protected def getAllWithQuery(queryMap: Map[String, Seq[String]]): FixedSqlStreamingAction[Seq[City], City, Effect.Read] = {
//    Entities.predicatePush(queryMap, table, Entities.caseclassToMap[CityTable]).asInstanceOf[Query[CityTable, City, Seq]]
//      .sortBy(_.id.asc.nullsLast).result
//  }

  override def getById(id: String, isSimple: Boolean = true)(implicit ex: ExecutionContext, ws: WSClient, actorSystem: ActorSystem, headers: Map[String, Seq[String]]): Future[PWResp] = {
    if(isSimple) {
      super.getById(id)
    } else {
     val query= (for {
       city <- table.filter(_.id === id.toInt)
       language <- Language.table.filter(_.id === city.lang_code)
     } yield  (city, language)).result
      logger.info(s"RUNNING QUERY: ${query.statements.mkString("\n")}")
     db.run(query).flatMap{ resp =>
       resp.isEmpty match {
         case true => Future.successful(PWResp(false, error = Some("City not found")))
         case false => Future.successful(PWResp(true, Some(resp.map(cl => CityOutput(cl._1.id, cl._1.name, cl._2, cl._1.created_on.toString, cl._1.modified_on.toString)))))
       }
     }
    }
  }

}
