package com.bansal.washcatalog.catalog.domain


trait MyResponse {
  val success: Boolean
  val error: Option[String]
  val data: Option[Seq[_]]
}

case class PWResp(
  success: Boolean,
  data: Option[Seq[RestEntityElement]]= None,
  error: Option[String]= None) extends MyResponse
