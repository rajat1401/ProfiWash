package com.bansal.washcatalog.catalog.common

import com.bansal.washcatalog.catalog.domain.{Account, AccountTransaction, City, Language, RestEntity, RestEntityElement}

object Constants {

  val entityMap: Map[String, RestEntity[_ <: RestEntityElement]]= Map(
    "languages" -> Language,
    "cities" -> City,
    "accounts" -> Account,
    "account_transactions" -> AccountTransaction
  )

  val SUCCESS= "Success"
  val X_ERROR_HEADER = "X-ERROR-HEADER"
  val ID_NOT_FOUND= "Id not found"
  val DATA_NOT_PRESENT=  "Data not present"

}
