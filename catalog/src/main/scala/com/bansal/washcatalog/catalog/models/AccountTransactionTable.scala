package com.bansal.washcatalog.catalog.models

import com.bansal.washcatalog.catalog.common.CommonEnums.TransactionStatus
import com.bansal.washcatalog.catalog.common.CommonEnums.TransactionStatus.TransactionStatus
import com.bansal.washcatalog.catalog.domain.AccountTransaction
import slick.jdbc.MySQLProfile.api._
import com.bansal.washcatalog.catalog.common.SlickEnumMappers._
import shapeless.{Generic, HNil}
import slickless._

import java.sql.Timestamp

class AccountTransactionTable(tag: Tag) extends Table[AccountTransaction](tag, "account_transaction") {

  override def * = (id :: timestamp :: account_dr_id :: account_cr_id :: amount :: state :: HNil).mappedWith(Generic[AccountTransaction])

  def id: Rep[String] = column[String]("transaction_id", O.PrimaryKey)

  def timestamp: Rep[Timestamp] = column[Timestamp]("timestamp")

  def account_dr_id: Rep[String] = column[String]("account_dr_id")

  def account_cr_id: Rep[String] = column[String]("account_cr_id")

  def amount: Rep[Double] = column[Double]("amount")

  def state: Rep[TransactionStatus.Value] = column[TransactionStatus.Value]("state")

  def idx1 = index("idx_account_dr_timestamp", (timestamp, account_dr_id), unique = false)
  def idx2 = index("idx_account_cr_timestamp", (timestamp, account_cr_id), unique = false)
  def foreignKeyAccountDr = foreignKey("account_dr_fk", account_dr_id, TableQuery[AccountTable])(_.id, onDelete = ForeignKeyAction.Cascade,
    onUpdate = ForeignKeyAction.Cascade)

  def foreignKeyAccountCr = foreignKey("account_cr_fk", account_cr_id, TableQuery[AccountTable])(_.id, onDelete = ForeignKeyAction.Cascade,
    onUpdate = ForeignKeyAction.Cascade)

}
