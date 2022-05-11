package com.scalatp.banking

import com.goyeau.kafka.streams.circe.CirceSerdes._
import com.goyeau.socialnetwork.model._
import com.scalatp.banking.model.{Account, Id, Operation, User}
import io.circe.generic.auto._
import io.circe.java8.time._

import java.net.URI
import java.time.Instant
import java.util.Properties
import scala.concurrent.Await
import scala.concurrent.duration._
import org.apache.kafka.clients.producer.ProducerConfig

object API extends App {
  println("Starting producer")

  val config = new Properties()
  config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Config.BootstrapServers)

  // Send a user
  val userProducer = Producer[User](config)

  val user = User(
    Id[User]("user1"),
    Instant.now(),
    "Test",
    deleted = false
  )
  Await.result(userProducer.send(user), 1.minute)

  userProducer.close()

  // Send a post
  val postProducer = Producer[Account](config)

  val account = Account(
    Id[Account]("account1"),
    Instant.now(),
    user.id,
    deleted = false
  )
  Await.result(postProducer.send(account), 1.minute)

  val operationProducer = Producer[Operation](config)
  val operation = Operation(
    Id[Operation]("operation0"),
    user.id,
    account.id,
    Instant.now(),
    200,
    failed = false
  )
  Await.result(postProducer.send(account), 1.minute)

  postProducer.close()
}
