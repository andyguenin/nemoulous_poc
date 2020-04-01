package com.nemoulous.util

import akka.Done
import akka.kafka.scaladsl.Producer
import akka.kafka.ProducerSettings
import akka.stream.scaladsl.{Flow, Keep, Sink}
import com.nemoulous.strategymanager.provider.ActorSystemProvider
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization._
import spray.json.JsValue

import scala.concurrent.Future

trait KafkaProducer {
  this: ActorSystemProvider =>

  private lazy val producerSettings = ProducerSettings(system, new StringSerializer, new StringSerializer)

  def getProducer(topic: String, key: String = ""): Sink[String, Future[Done]] =
    Flow[String].map(r => new ProducerRecord[String, String](topic, key, r))
        .toMat(Producer.plainSink(producerSettings))(Keep.right)

  def getJsonProducer(topic: String, key: String = ""): Sink[JsValue, Future[Done]] =
    Flow[JsValue].map(_.toString()).toMat(getProducer(topic, key))(Keep.right)
}
