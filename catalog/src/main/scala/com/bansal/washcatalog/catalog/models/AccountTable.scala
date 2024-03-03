package com.bansal.washcatalog.catalog.models

import com.bansal.washcatalog.catalog.common.CommonEnums.AccountType
import com.bansal.washcatalog.catalog.common.CommonEnums.AccountType.AccountType
import com.bansal.washcatalog.catalog.common.CommonFields
import com.bansal.washcatalog.catalog.domain.Account
import shapeless.{Generic, HNil}
import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape
import slickless._
import com.bansal.washcatalog.catalog.common.SlickEnumMappers._

class AccountTable(tag: Tag) extends Table[Account](tag, "account") with CommonFields {

  override def * : ProvenShape[Account] = (id :: account_type :: balance :: created_on :: modified_on :: HNil).mappedWith(Generic[Account])

  def id: Rep[String] = column[String]("account_id", O.PrimaryKey)

  def account_type: Rep[AccountType.Value] = column[AccountType.Value]("account_type")

  def balance: Rep[Double] = column[Double]("balance")

}
