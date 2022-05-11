package com.goyeau.socialnetwork.model

import java.time.Instant

import com.goyeau.socialnetwork.Record

case class Operation(id: Id[Operation],userId: Id[User], accountId: Id[Account], updatedOn: Instant, amount: Int, failed: Boolean)

object Operation {
  implicit val record: Record[Id[Account], Operation] = new Record[Id[Account], Operation] {
    val topic = "operations"
    def key(like: Operation): Id[Account] = like.accountId
    def timestamp(like: Operation): Long = like.updatedOn.toEpochMilli
  }
}
