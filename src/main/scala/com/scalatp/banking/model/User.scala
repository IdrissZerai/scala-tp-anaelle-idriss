package com.scalatp.banking.model

import java.net.URI
import java.time.Instant
import com.scalatp.banking
import com.scalatp.banking.Record

case class User(id: Id[User], updatedOn: Instant, nickname: String, deleted: Boolean)

object User {
  implicit val record: banking.Record[Id[User], User] = new banking.Record[Id[User], User] {
    val topic = "owners"
    def key(user: User): Id[User] = user.id
    def timestamp(user: User): Long = user.updatedOn.toEpochMilli
  }
}
