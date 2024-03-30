package com.bansal.washcatalog.catalog

import com.bansal.washcatalog.catalog.common.Constants.SUCCESS
import play.api.Logger
import slick.dbio.{DBIOAction, Effect, NoStream}
import slick.jdbc.JdbcBackend.Database
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object SlickDbUtil {

  /**
   * initialise a slick database instance
   */
  val db= Database.forConfig("db.wash_catalog")

  def runDbAction(action: DBIOAction[Any, NoStream, Effect.All]): Future[String] = {
    db.run(action.asTry).map{
      case Failure(exception) =>
        exception.getMessage
      case Success(_) =>
        SUCCESS
    }
  }
}
