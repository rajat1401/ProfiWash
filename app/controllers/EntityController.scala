package controllers

import akka.actor.ActorSystem
import com.bansal.washcatalog.catalog.common.CommonEnums.QueryType
import com.bansal.washcatalog.catalog.common.Constants.entityMap
import com.bansal.washcatalog.catalog.domain.{City, Input, Language, PWResp}
import controllers.RespWrapper.RespWrapper
import play.api.libs.ws.WSClient
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, Result}
import utils.ObjectMapperUtil._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EntityController @Inject() (cc: ControllerComponents)(implicit ex: ExecutionContext, ws: WSClient, actorSystem: ActorSystem)
  extends AbstractController(cc) {

  def getSingle(entityId: String, id: String): Action[AnyContent] = Action.async {
    implicit request =>
      val headers= request.headers.toMap
      entityMap.get(entityId) match {
        case Some(entity) =>
          entity.runQuery(QueryType.getById, Some(id), Map())(ex,ws, actorSystem, headers)
            .map(resp => RespWrapper(resp).withHeaders)
        case None =>
          Future(NotFound)
      }
  }

  def getAll(entityId: String): Action[AnyContent] = Action.async {
    implicit request =>
      val headers= request.headers.toMap
      val queryString= request.queryString
      listing(
        entityId, queryString, false
      )(headers)
  }

  def getAllSimple(entityId: String): Action[AnyContent] = Action.async {
    implicit request =>
      val headers = request.headers.toMap
      val queryString = request.queryString
      listing(
        entityId, queryString, true
      )(headers)
  }

  def addSingle(entityId: String): Action[AnyContent] = Action.async {
    implicit request =>
      val headers= request.headers.toMap
      val input = request.body.asJson
      if(input.nonEmpty) {
        entityMap.get(entityId) match {
          case Some(entity) =>
            entity.add(input.get)(ex, ws, actorSystem, headers)
              .map(resp => RespWrapper(resp).withHeaders)
        }
      } else {
        Future(BadRequest)
      }
  }

  def listing(entityId: String, queryString: Map[String, Seq[String]], isSimple: Boolean)(headers: Map[String,Seq[String]]): Future[Result] = {
    entityMap.get(entityId) match {
      case Some(entity) =>
        val resp = if (queryString.nonEmpty) {
          entity.runQuery(QueryType.getWithQuery, None, queryString, isSimple)(ex, ws, actorSystem, headers)
        } else {
          entity.runQuery(QueryType.getAll, None, Map(), isSimple)(ex, ws, actorSystem, headers)
        }
        resp.map(resp => RespWrapper(resp).withHeaders)
      case None =>
        Future(NotFound)
    }
  }
}
