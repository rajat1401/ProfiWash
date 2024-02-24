package common


import com.bansal.washcatalog.catalog.common.CommonEnums.Occupation
import com.bansal.washcatalog.catalog.common.CommonEnums.Occupation.Occupation
import com.bansal.washcatalog.catalog.common.Entities
import com.bansal.washcatalog.catalog.domain.City.table

import scala.concurrent.ExecutionContext.Implicits.global
import com.bansal.washcatalog.catalog.domain.{City, CityInput, Language, LanguageInput, RestEntityElement}
import com.bansal.washcatalog.catalog.models.{CityTable, LanguageTable}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import slick.jdbc.MySQLProfile.api._
import play.api.Logging
import play.api.libs.json.{JsValue, Json}
import utils.ObjectMapperUtil.objectMapper

import javax.management.openmbean.OpenMBeanConstructorInfo
import scala.Console.println
import scala.concurrent.Await
import scala.concurrent.duration.{Duration, DurationInt}
import scala.reflect.runtime.universe.typeOf


case class TestClass(id: String, name: String, age: Int, occupation: Occupation) extends RestEntityElement

class EntitiesTest extends AnyFlatSpec with Logging with BeforeAndAfterAll {
  var idToDelete: String= "0"

//  override def beforeAll(): Unit = {
//    val cityInput= CityInput(
//      "NewYork",
//      "en"
//    )
//    idToDelete= Await.result(City.addQuery(cityInput), Duration.Inf)
//  }

  "caseclassToMap" should "return map of case class field types" in {
    val caseclassMap = Entities.caseclassToMap[TestClass]
    logger.info(caseclassMap.toString)
    assert(caseclassMap.size== 4 && caseclassMap.values.exists(_.contains("com.bansal.washcatalog.catalog.common.CommonEnums.Occupation.Occupation")))
  }

  "toInput" should "convert input json to case class" in {
    val inputJson= Map("lang_code"-> "es", "name_eng"-> "Spanish", "name_native"-> "español")
    val jsvalue= Json.parse(objectMapper.writeValueAsString(inputJson))
    val languageInput= objectMapper.readValue(jsvalue.toString(), classOf[LanguageInput])
    assert(languageInput.lang_code.equals("es"))

  }

//  override def afterAll(): Unit = {
//    val op= Await.result(City.deleteQuery(idToDelete), Duration.Inf)
//    assert(op.equals("1"))
//    logger.info(op)
//  }

}
