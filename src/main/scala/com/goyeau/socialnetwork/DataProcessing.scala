package com.goyeau.socialnetwork

import com.goyeau.kafka.streams.circe.CirceSerdes
import com.goyeau.kafka.streams.circe.CirceSerdes._
import com.goyeau.socialnetwork.model._
import com.lightbend.kafka.scala.streams.ImplicitConversions._
import com.lightbend.kafka.scala.streams.StreamsBuilderS
import io.circe.generic.auto._
import io.circe.java8.time._
import io.circe.syntax._
import java.util.Properties
import monocle.macros.syntax.lens._
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.state.KeyValueStore

object DataProcessing extends App {
  println("Starting streams")

  val streamsBuilder = new StreamsBuilderS()
  val usersStream = streamsBuilder.streamFromRecord[User]()
  val accountStream = streamsBuilder.streamFromRecord[Account]()
  val operationsStream = streamsBuilder.streamFromRecord[Operation]()

  val usersByKey = usersStream
    .groupByKey
    .reduce((first, second) => if (first.updatedOn.isAfter(second.updatedOn)) first else second)

  val accountsByUser = accountStream
    .groupBy((_,account)=>account.owner)
    .aggregate(
      () => Map.empty[Id[Account], Account],
      (_, post: Account, account: Map[Id[Account], Account]) =>
        if (account.get(post.id).exists(_.updatedOn.isAfter(post.updatedOn))) account
        else account + (post.id -> post),
      Materialized.`with`[Id[User], Map[Id[Account], Account], KeyValueStore[Bytes, Array[Byte]]](
        CirceSerdes.serde[Id[User]],
        CirceSerdes.serde[Map[Id[Account], Account]]
      )
    )
    .mapValues(_.values.toSet)



  val operationsByKey = operationsStream
    .groupByKey
    .aggregate(
      () => Set.empty[Operation],
      (_, operation: Operation, operations: Set[Operation]) =>
        if (operation.failed) operations - operation else operations + operation,
        Materialized.`with`[Id[Account], Set[Operation], KeyValueStore[Bytes, Array[Byte]]](
        CirceSerdes.serde[Id[Account]],
        CirceSerdes.serde[Set[Operation]]
      )
    )

  accountsByUser
    .join(usersByKey,
      (posts: Set[Account], owner: User) =>
        posts.map(DenormalisedAccount(_, owner, DenormalisedAccount.Transactions(Set.empty))))
    .toStream
    .flatMapValues(identity)
    .groupBy((_, denormalisedAccount) => denormalisedAccount.account.id)
    .reduce((first, second) => if (first.account.updatedOn.isAfter(second.account.updatedOn)) first else second)
    .leftJoin(operationsByKey,
      (denormalisedPost: DenormalisedAccount, operation: Set[Operation]) =>
        Option(operation).fold(denormalisedPost)(denormalisedPost.lens(_.transactions.operations).set(_)))
    .toStream
    .toTopic

  val config = new Properties()
  config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, Config.BootstrapServers)
  config.put(StreamsConfig.APPLICATION_ID_CONFIG, "stream0")
  val streams = new KafkaStreams(streamsBuilder.build(), config)
  Runtime.getRuntime.addShutdownHook(new Thread(() => streams.close()))
  streams.start()
}
