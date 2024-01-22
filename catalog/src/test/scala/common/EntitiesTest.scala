package common

import com.bansal.washcatalog.catalog.SlickDbUtil.db
import com.bansal.washcatalog.catalog.common.CommonEnums.Occupation.Occupation
import com.bansal.washcatalog.catalog.common.Entities
import com.bansal.washcatalog.catalog.domain.{City, Language, RestEntityElement}
import com.bansal.washcatalog.catalog.models.{CityTable, LanguageTable}
import org.scalatest.flatspec.AnyFlatSpec
import slick.jdbc.MySQLProfile.api._
import play.api.Logging

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.reflect.runtime.universe.typeOf


case class TestClass(id: String, name: String, age: Int, occupation: Occupation) extends RestEntityElement

class EntitiesTest extends AnyFlatSpec with Logging {

  "caseclassToMap" should "return map of case class field types" in {
    val caseclassMap = Entities.caseclassToMap[TestClass]
    logger.info(caseclassMap.toString)
    assert(caseclassMap.size== 3 && caseclassMap.values.exists(_.contains("com.bansal.washcatalog.catalog.common.CommonEnums.Occupation.Occupation")))
  }

//  "getTableMappingType" should "get type of table to at runtime" in {
//    def typeTest[A](table: Query[A, _, Seq]): Class[_ <: A] = {
//      Await.result(db.run(table.result), 2.seconds).head.asInstanceOf[A].getClass
//    }
//    val table: Query[LanguageTable, _, Seq] = Language.table.take(1)
//    assert(typeTest(table) == classOf[CityTable])
//  }

}
