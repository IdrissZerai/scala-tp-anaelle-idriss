package com.scalatp.banking.model

import java.time.Instant
import com.scalatp.banking
import com.scalatp.banking.Record

case class Operation(id: Id[Operation],userId: Id[User], accountId: Id[Account], updatedOn: Instant, amount: Int, failed: Boolean)

object Operation {
  implicit val record: banking.Record[Id[Account], Operation] = new banking.Record[Id[Account], Operation] {
    val topic = "operations"
    def key(like: Operation): Id[Account] = like.accountId
    def timestamp(like: Operation): Long = like.updatedOn.toEpochMilli
  }
}
