package com.bansal.washcatalog.catalog.domain

import akka.actor.ActorSystem
import com.bansal.washcatalog.catalog.SlickDbUtil.{db, runDbAction}
import com.bansal.washcatalog.catalog.common.CommonEnums.QueryType
import com.bansal.washcatalog.catalog.common.CommonEnums.QueryType.QueryType
import com.bansal.washcatalog.catalog.common.Constants.{DATA_NOT_PRESENT, ID_NOT_FOUND}
import play.api.Logging
import play.api.libs.json.JsValue
import play.api.libs.ws.WSClient
import slick.jdbc.MySQLProfile.api._
import slick.lifted.TableQuery
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.runtime.universe.{MethodSymbol, TypeTag, typeOf}

trait RestEntity[A <: RestEntityElement] extends Logging {
  protected def getByIdQuery(id: String): FixedSqlStreamingAction[Seq[A], RestEntityElement, Effect.Read]
  protected def getAllQuery: FixedSqlStreamingAction[Seq[A], RestEntityElement, Effect.Read]
  protected def getAllWithQuery(queryMap: Map[String,Seq[String]]): FixedSqlStreamingAction[Seq[A], RestEntityElement, Effect.Read]

  protected def getById(id: String, isSimple: Boolean= false)(implicit ex: ExecutionContext, ws: WSClient, actorSystem: ActorSystem, headers:Map[String,Seq[String]]): Future[PWResp]= {
    val query= getByIdQuery(id)
    db.run(query).map { resp =>
      resp.headOption match {
        case Some(_) => PWResp(success = true, Some(resp))
        case None => PWResp(success = false, None, error = Some(ID_NOT_FOUND))
      }
    }
  }

  protected def getAll(isSimple: Boolean= false)(implicit ex: ExecutionContext, ws: WSClient, actorSystem: ActorSystem, headers:Map[String,Seq[String]]): Future[PWResp]= {
    val query= getAllQuery
    db.run(query).map { resp =>
      resp.headOption match {
        case Some(_) => PWResp(success = true, data = Some(resp))
        case None => PWResp(success = false, None, error = Some(DATA_NOT_PRESENT))
      }
    }
  }

  protected def getWithQuery(queryMap: Map[String,Seq[String]], isSimple: Boolean= false)(implicit ex: ExecutionContext, ws: WSClient, actorSystem: ActorSystem, headers:Map[String,Seq[String]]): Future[PWResp]= {
    val query = getAllWithQuery(queryMap)
    db.run(query).map { resp =>
      resp.headOption match {
        case Some(_) => PWResp(success = true, data = Some(resp))
        case None => PWResp(success = false, None, error = Some(DATA_NOT_PRESENT))
      }
    }
  }

  def add(input: JsValue)(implicit ex: ExecutionContext, ws: WSClient, actorSystem: ActorSystem, headers:Map[String,Seq[String]]): Future[PWResp]

  def runQuery(queryType: QueryType = QueryType.getAll, id: Option[String]= None, queryMap: Map[String,Seq[String]], isSimple: Boolean= false)(implicit ex: ExecutionContext, ws: WSClient, actorSystem: ActorSystem, headers:Map[String,Seq[String]]): Future[PWResp]= {
    queryType match {
      case QueryType.getAll => getAll(isSimple)
      case QueryType.getById => getById(id.get, isSimple)
      case QueryType.getWithQuery => getWithQuery(queryMap, isSimple)
    }
  }
}
