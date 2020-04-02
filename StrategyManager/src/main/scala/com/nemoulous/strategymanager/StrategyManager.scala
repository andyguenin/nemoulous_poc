package com.nemoulous.strategymanager

import akka.actor.typed.ActorSystem
import akka.actor.{ActorSystem => OldActorSystem}
import com.nemoulous.util.Settings
import akka.stream.Materializer
import com.nemoulous.strategymanager.model.ModelGuardian
import org.flywaydb.core.Flyway

import scala.util.{Failure, Success}


/**
 * Application entry point. It is responsible for loading configuration and starting the model guardian
 */
object StrategyManager {

  def main(args: Array[String]): Unit = {
    implicit val settings = Settings(args)
    migrate(settings)
    implicit val system = OldActorSystem("kafka", settings.config)
    implicit val mat = Materializer(system)
    implicit val ec = system.dispatcher

    val models = ActorSystem((new ModelGuardian).getBehavior(), "model-guardian")

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
