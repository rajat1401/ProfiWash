package com.bansal.washcatalog.catalog.common

object CommonEnums {

  object Occupation extends Enumeration {
    type Occupation = Value

    val engineer,doctor,lawyer,artist = Value
  }

  object QueryType extends Enumeration {
    type QueryType = Value

    val getById,getAll,getWithQuery= Value
  }

  object AccountType extends Enumeration {
    type AccountType = Value

    val asset, liability, revenue = Value
  }


  object TransactionStatus extends Enumeration {
    type TransactionStatus = Value

    val planned, trying, revoked, committed, rejected = Value
  }

}
