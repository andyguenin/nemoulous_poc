package com.nemoulous.strategymanager



import akka.actor.{ActorSystem => OldActorSystem}
import com.nemoulous.util.Settings
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.stream.Materializer
import akka.stream.scaladsl.{Keep, Sink, Source}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{IntegerDeserializer, StringDeserializer, StringSerializer}
import org.flywaydb.core.Flyway

import scala.concurrent.duration._


object StrategyManager {

  def main(args: Array[String]): Unit = {
    implicit val settings = Settings(args)

    //    migrate(settings)

    val system = OldActorSystem("kafka")
    implicit val mat = Materializer(system)


    val topic = "nemoulous-topic"

    val kafkaConsumerSettings = ConsumerSettings(system, new IntegerDeserializer, new StringDeserializer)
      .withBootstrapServers(settings.kafkaBootstrapServers)
      .withGroupId("nemoulous")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      .withStopTimeout(0.seconds)
    val producerSettings = ProducerSettings(system, new StringSerializer, new StringSerializer).withBootstrapServers(settings.kafkaBootstrapServers)

    val control = Consumer.committableSource(kafkaConsumerSettings, Subscriptions.topics(topic)).toMat(Sink.foreach(println))(Keep.both).mapMaterializedValue(DrainingControl.apply).run()

    Thread.sleep(5000)
    Source(1 to 100).map(_.toString).map(value => new ProducerRecord[String, String]("nemoulous-topic", value)).runWith(Producer.plainSink(producerSettings))
    //

    //
    //
    //    val control: Future[Done] = Consumer.sourceWithOffsetContext(kafkaConsumerSettings, Subscriptions.topics("nemoulous-topic"))
    //      .runWith(Sink.foreach(println))
    //
    //
    //
    //
    //

    //    val kafkaProducer = Producer.plainSink(producerSettings)
    //
    //    Thread.sleep(10000)
    ////    val a = Source(1 to 10).map(r => new ProducerRecord[String, String]("nemoulous-topic", "", r.toString)).runForeach(println)
    //
    //

    ////    val system: ActorSystem[Receptionist.Listing] = ActorSystem(ModelGuardian(), "models")
    //


  }

  private[this] def migrate(settings: Settings): Unit = {
    val flyway = Flyway.configure.dataSource(settings.ds).schemas(settings.schema).load()
    flyway.migrate()
  }

}
