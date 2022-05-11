package com.goyeau.socialnetwork.model

import java.net.URI
import java.time.Instant

import com.goyeau.socialnetwork.Record

case class Account(id: Id[Account], updatedOn: Instant, owner: Id[User], deleted: Boolean)

object Account {
  implicit val record: Record[Id[Account], Account] = new Record[Id[Account], Account] {
    val topic = "account"
    def key(post: Account): Id[Account] = post.id
    def timestamp(post: Account): Long = post.updatedOn.toEpochMilli
  }
}
