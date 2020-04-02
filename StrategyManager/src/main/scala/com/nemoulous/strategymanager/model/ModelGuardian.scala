package com.nemoulous.strategymanager.model

import java.util.concurrent.TimeUnit


import akka.{actor => classic}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.typed.scaladsl.ActorFlow
import akka.util.Timeout
import com.nemoulous.strategymanager.model.implementations.spread.DivergingSpread
import com.nemoulous.strategymanager.provider.ActorSystemProvider
import com.nemoulous.strategymanager.signal.Signal
import com.nemoulous.util.{KafkaConsumer, KafkaProducer}

import scala.concurrent.duration._

/**
 * The model guardial is responsible for "booting" all of the individual models.
 * @param system
 */
class ModelGuardian(override implicit val system: classic.ActorSystem) extends KafkaConsumer with KafkaProducer with ActorSystemProvider {

  /**
   * Akka actor's ask function requires a timeout.
   */
  implicit val t = Timeout(FiniteDuration(10, TimeUnit.SECONDS))

  /**
   * A comprehensive list of all of the models should be placed here. This is not super scalable, but workable for now
   * with a poc
   */
  val models = List(
    new DivergingSpread(5)
  )

  /**
   * This is the guardian actor's behavior.
   * @return the guardian actor's behavior.
   */
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

  /**
   * This sets up the individual model actors to be used within akka streams. This way, we can directly connect the
   * kafka consumer to the model, and then handle the model output.
   * @param existingModels
   * @return
   */
  def modelBehavior(existingModels: Set[ActorRef[Asking[Signal]]]): Behavior[Receptionist.Listing] = {
    Behaviors.receiveMessagePartial[Receptionist.Listing] {
      case Model.genericServiceKey.Listing(listings) if listings.nonEmpty =>
        val newModels = listings.diff(existingModels)

        newModels.foreach {
          model =>
            getJsonConsumer("nemoulous-signal")
              .map(Signal.getObject)
              .via(ActorFlow.ask(model)((el: Signal, replyTo: ActorRef[Action]) => Asking(el, replyTo)))
              .filter(r => r != NoAction)
              .map(r => Action.toJson(r))
              // For now, this application will output all actions, but in the future, we will stream it back to kafka
//              .runWith(getJsonProducer("nemoulous-action"))
              .runForeach(println)

        }
        modelBehavior(listings)
      case _ => Behaviors.same
    }
  }
}
