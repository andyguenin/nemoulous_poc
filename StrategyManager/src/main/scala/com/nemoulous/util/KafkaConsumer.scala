package com.nemoulous.util

import spray.json._

import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.scaladsl.{Sink, Source}
import com.nemoulous.strategymanager.provider.ActorSystemProvider
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization._

trait KafkaConsumer {
  this: ActorSystemProvider =>

  lazy private val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)


  def getConsumer(topic: String): Source[ConsumerRecord[String, String], Consumer.Control] =
    Consumer.plainSource(consumerSettings, Subscriptions.topics(topic))


  def getJsonConsumer(topic: String): Source[JsObject, Consumer.Control] =
    getConsumer(topic).map(r => r.value().parseJson.asJsObject())
}
