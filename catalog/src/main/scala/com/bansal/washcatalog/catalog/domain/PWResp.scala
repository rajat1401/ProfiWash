package com.bansal.washcatalog.catalog.domain

case class PWResp(
  success: Boolean,
  data: Option[Seq[RestEntityElement]]= None,
  error: Option[String]= None)
