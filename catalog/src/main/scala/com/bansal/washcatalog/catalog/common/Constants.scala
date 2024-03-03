package com.bansal.washcatalog.catalog.common

import com.bansal.washcatalog.catalog.domain.{Account, City, Language, RestEntity, RestEntityElement}

object Constants {

  val entityMap: Map[String, RestEntity[_ <: RestEntityElement]]= Map(
    "languages" -> Language,
    "cities" -> City,
    "accounts" -> Account
  )

  val SUCCESS= "Success"

  val ID_NOT_FOUND= "Id not found"
  val DATA_NOT_PRESENT=  "Data not present"

}
