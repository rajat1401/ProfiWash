package com.bansal.washcatalog.catalog.common

import com.bansal.washcatalog.catalog.common.CommonEnums.AccountType
import com.fasterxml.jackson.core.`type`.TypeReference
import slick.jdbc.MySQLProfile.api._

object SlickEnumMappers {

  implicit val enumAccountTypeMapper: BaseColumnType[AccountType.Value] = MappedColumnType.base[AccountType.Value, String] (
    e => e.toString,
    s => AccountType.values.find(_.toString == s).getOrElse(AccountType.asset)
  )

  class AccountTypeType extends TypeReference[AccountType.type]
}
