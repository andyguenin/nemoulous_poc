package com.nemoulous.strategymanager.model

import java.util.concurrent.TimeUnit


import akka.{actor => classic}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.typed.scaladsl.ActorFlow
import akka.util.Timeout
import com.nemoulous.strategymanager.model.implementations.spread.ConvergingSpreads
import com.nemoulous.strategymanager.provider.ActorSystemProvider
import com.nemoulous.strategymanager.signal.{FutureSpread, Signal}
import com.nemoulous.util.{KafkaConsumer, KafkaProducer}

import scala.concurrent.duration._

class ModelGuardian(override implicit val system: classic.ActorSystem) extends KafkaConsumer with KafkaProducer with ActorSystemProvider {

  implicit val t = Timeout(FiniteDuration(10, TimeUnit.SECONDS))

  val models = List(
    ConvergingSpreads
//    , ConvergingSpreads, ConvergingSpreads, ConvergingSpreads, ConvergingSpreads,
//    ConvergingSpreads, ConvergingSpreads, ConvergingSpreads, ConvergingSpreads, ConvergingSpreads
  )

  def getBehavior(): Behavior[Receptionist.Listing] = {
    Behaviors
      .setup[Receptionist.Listing] {
        context =>
          context.system.receptionist ! Receptionist.Subscribe(Model.genericServiceKey, context.self)

          models.foreach {
            m => context.spawnAnonymous(m.actor())
          }

          modelBehavior(Set())
      }
  }

  def modelBehavior(existingModels: Set[ActorRef[Asking[Signal]]]): Behavior[Receptionist.Listing] = {
    Behaviors.receiveMessagePartial[Receptionist.Listing] {
      case Model.genericServiceKey.Listing(listings) if listings.nonEmpty =>
        val newModels = listings.diff(existingModels)
        

        newModels.foreach {
          model => getJsonConsumer("nemoulous-signal")
            .map(Signal.getObject)
            .via(ActorFlow.ask(model)((el: Signal, replyTo: ActorRef[Action]) => Asking(el, replyTo)))
            .map(r => Action.toJson(r))
            .runWith(getJsonProducer("nemoulous-action"))

        }

        modelBehavior(listings)
      case _ => Behaviors.same
    }
  }
}
