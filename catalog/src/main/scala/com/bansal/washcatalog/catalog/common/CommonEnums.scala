package com.bansal.washcatalog.catalog.common

object CommonEnums {

  object Occupation extends Enumeration {
    type Occupation = Value

    val engineer = Value("Engineer")
    val doctor = Value("Doctor")
    val lawyer = Value("Lawyer")
    val artist = Value("Artist")
  }

}
