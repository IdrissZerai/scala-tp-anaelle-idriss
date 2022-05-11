package com.scalatp.banking.model

import com.scalatp.banking.Record

case class DenormalisedAccount(account: Account, owner: User, transactions: DenormalisedAccount.Transactions)

object DenormalisedAccount {
case class Transactions(operations: Set[Operation])
  implicit val record = new Record[Id[Account], DenormalisedAccount] {
    val topic = "denormalised-accounts"
    def key(denormalisedPost: DenormalisedAccount): Id[Account] = denormalisedPost.account.id
    def timestamp(denormalisedPost: DenormalisedAccount): Long = denormalisedPost.account.updatedOn.toEpochMilli
  }
}
