package com.goyeau.socialnetwork.model

import java.net.URI
import java.time.Instant

import com.goyeau.socialnetwork.Record

case class User(id: Id[User], updatedOn: Instant, nickname: String, deleted: Boolean)

object User {
  implicit val record: Record[Id[User], User] = new Record[Id[User], User] {
    val topic = "owners"
    def key(user: User): Id[User] = user.id
    def timestamp(user: User): Long = user.updatedOn.toEpochMilli
  }
}
