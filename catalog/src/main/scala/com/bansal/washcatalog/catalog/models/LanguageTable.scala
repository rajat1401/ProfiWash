package com.bansal.washcatalog.catalog.models

import com.bansal.washcatalog.catalog.common.CommonFields
import com.bansal.washcatalog.catalog.domain.Language
import shapeless.{Generic, HNil}
import slickless._
import slick.lifted.{ProvenShape, Tag}
import slick.jdbc.MySQLProfile.api._

class LanguageTable(tag: Tag) extends Table[Language](tag, "language") with CommonFields {

  override def * : ProvenShape[Language] = (id :: name_eng :: name_native :: created_on :: modified_on :: HNil).mappedWith(Generic[Language])

  def id: Rep[String] = column[String]("lang_code", O.PrimaryKey)

  def name_eng: Rep[String] = column[String]("name_eng")

  def name_native: Rep[String] = column[String]("name_native")

}
