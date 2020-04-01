package com.nemoulous.strategymanager



import akka.actor.typed.ActorSystem
import akka.actor.{ActorSystem => OldActorSystem}
import com.nemoulous.util.{KafkaConsumer, KafkaProducer, Settings}
import akka.stream.Materializer
import akka.stream.scaladsl.{Keep, Source}
import com.nemoulous.strategymanager.model.{Model, ModelGuardian}
import com.nemoulous.strategymanager.provider.ActorSystemProvider
import com.nemoulous.strategymanager.signal.{FutureSpread, Signal}
import org.flywaydb.core.Flyway
import spray.json._
import DefaultJsonProtocol._

import scala.util.{Failure, Success}


object StrategyManager {

  def main(args: Array[String]): Unit = {
    implicit val settings = Settings(args)
//    migrate(settings)
    implicit val system = OldActorSystem("kafka", settings.config)
    implicit val mat = Materializer(system)
    implicit val ec = system.dispatcher


    val actionTopic = "nemoulous-action"

    val s = system
    object A extends KafkaConsumer with KafkaProducer with ActorSystemProvider {
      override implicit val system: OldActorSystem = s
    }

    val models = ActorSystem((new ModelGuardian).getBehavior(), "model-guardian")


//    A.getJsonConsumer("nemoulous-signal").runForeach(println)

    models.whenTerminated.onComplete{
      case Success(s) => println("complete")
      case Failure(f) => println("completed, but with a failure")
    }
  }

  private[this] def migrate(settings: Settings): Unit = {
    val flyway = Flyway.configure.dataSource(settings.ds).schemas(settings.schema).load()
    flyway.migrate()
  }

}
