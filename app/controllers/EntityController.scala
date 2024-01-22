package controllers

import akka.actor.ActorSystem
import com.bansal.washcatalog.catalog.domain.{City, Language, PWResp}
import play.api.libs.ws.WSClient
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, Result}
import utils.ObjectMapperUtil._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EntityController @Inject() (cc: ControllerComponents)(implicit ex: ExecutionContext, ws: WSClient, actorSystem: ActorSystem)
  extends AbstractController(cc) {

  def getEntityById(entityId: String, id: String): Action[AnyContent] = Action.async {
    implicit request =>
      val headers= request.headers.toMap
      val entityresp= entityId match {
        case "languages" => Language.getById(id, false)(ex, ws, actorSystem, headers)
        case "cities" => City.getById(id, false)(ex, ws, actorSystem, headers)
        case _ => Future(PWResp(false, error = Some("invalid entity")))
      }
      entityresp.map{ entity =>
        Ok(objectMapper.writeValueAsString(entity))
      }
  }

}
