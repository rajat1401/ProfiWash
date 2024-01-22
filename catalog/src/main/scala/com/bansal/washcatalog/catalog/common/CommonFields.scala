package com.bansal.washcatalog.catalog.common

import slick.lifted.Rep
import slick.jdbc.MySQLProfile.api._
import java.sql.Timestamp

trait CommonFields {
  //means this trait would always be extending a table (sort of reverse dependency injection?)
  this: Table[_] =>

  def created_on: Rep[Timestamp] = column[Timestamp]("created_on")

  def modified_on: Rep[Timestamp] = column[Timestamp]("modified_on")

}
