package com.bansal.washcatalog.catalog.models

import com.bansal.washcatalog.catalog.common.CommonFields
import com.bansal.washcatalog.catalog.domain.{City, Language}
import shapeless.{Generic, HNil}
import slickless._
import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape

class CityTable(tag: Tag) extends Table[City](tag, "city_i18n") with CommonFields {
  override def * : ProvenShape[City] = (id.toString :: name :: lang_code :: created_on :: modified_on :: HNil).mappedWith(Generic[City])

  def id = column[Int]("city_id", O.AutoInc)
  def name = column[String]("name")
  def lang_code = column[String]("lang_code")

  def cityPrimaryKey = primaryKey("pk_city", (id, lang_code))
  def langCodeFK = foreignKey("lang_code_fk", lang_code, TableQuery[LanguageTable])(_.id,
    onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)

}
