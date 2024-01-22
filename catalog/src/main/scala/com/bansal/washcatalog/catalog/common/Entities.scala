package com.bansal.washcatalog.catalog.common

import com.bansal.washcatalog.catalog.SlickDbUtil.db
import com.bansal.washcatalog.catalog.domain.RestEntityElement
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.reflect.runtime.universe.{MethodSymbol, typeOf}
import scala.reflect.runtime.universe.TypeTag
import scala.util.Try
case class TableMapping(field: String, table: String)

object Entities {

  def caseclassToMap[A: TypeTag]: Map[String, String] = {
    typeOf[A].members.collect {
      case m: MethodSymbol if m.isCaseAccessor => (m.name.toString, m.returnType.toString)
    }.toMap
  }

  def getTableFieldValue[A](tbl: A, field: String): AnyRef = {
    tbl.getClass.getMethod(field).invoke(tbl)
  }

  def predicatePush[A](queryMap: Map[String, Seq[String]], table: Query[A, _, Seq], caseclassMap: Map[String,String]): Query[A, _, Seq] = {
    val initQuery= table
//    val tableclass= Await.result(db.run(table.take(1).result), 2.seconds).head.asInstanceOf[A]
//    val tableFieldValuesMap= queryMap.map(tuple => (tuple._1, getTableFieldValue(tableclass, tuple._1)))
    queryMap.foldLeft(initQuery)((query, tuple) => {
      val newquery=  caseclassMap.get(tuple._1) match {
        case Some("String") =>
          val valuestoFilter= tuple._2
          query.filterOpt(Option(valuestoFilter)) {
            case (tbl, value) => getTableFieldValue(tbl, tuple._1).asInstanceOf[Rep[String]] inSet value
          }
        case Some("Int") =>
          val valuestoFilter= tuple._2.map(value => Try(value.toInt).getOrElse(0))
          query.filterOpt(Option(valuestoFilter)) {
            case (tbl, value) => getTableFieldValue(tbl, tuple._1).asInstanceOf[Rep[Int]] inSet value
          }
        case Some("Option[String]") =>
          val valuestoFilter = tuple._2
          query.filterOpt(Option(valuestoFilter)) {
            case (tbl, value) => getTableFieldValue(tbl, tuple._1).asInstanceOf[Rep[Option[String]]] inSet value
          }
        case Some("Double") =>
          val valuestoFilter = tuple._2.map(value => Try(value.toDouble).getOrElse(0.0))
          query.filterOpt(Option(valuestoFilter)) {
            case (tbl, value) => getTableFieldValue(tbl, tuple._1).asInstanceOf[Rep[Double]] inSet value
          }
        case _ => query
      }
      newquery
    })
  }

}
