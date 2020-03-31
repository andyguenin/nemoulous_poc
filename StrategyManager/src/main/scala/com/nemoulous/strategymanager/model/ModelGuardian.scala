package com.nemoulous.strategymanager.model

import java.util.concurrent.TimeUnit

import akka.{actor => classic}
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.Source
import akka.stream.typed.scaladsl.ActorFlow
import akka.util.Timeout
import com.nemoulous.strategymanager.model.implementations.spread.ConvergingSpreads
import com.nemoulous.strategymanager.signal.{FutureSpread, Signal}

import scala.concurrent.duration._

object ModelGuardian {

  implicit val t = Timeout(FiniteDuration(10, TimeUnit.SECONDS))

  implicit val oldSystem = classic.ActorSystem("sys")


  val models = List(
    ConvergingSpreads
  )

  def apply(): Behavior[Receptionist.Listing] = {
    Behaviors
      .setup[Receptionist.Listing] {
        context =>
          models.foreach {
            m => context.spawnAnonymous(m.actor())
          }
          context.system.receptionist ! Receptionist.Subscribe(Model.genericServiceKey, context.self)


          Behaviors.receiveMessagePartial[Receptionist.Listing] {
            case Model.genericServiceKey.Listing(listings) if listings.size > 0 =>


              val spreads = List(
                FutureSpread("NG", "NGF18", "NGH18", 0.01),
                FutureSpread("NG", "NGF18", "NGH18", 0.02),
                FutureSpread("NG", "NGF18", "NGH18", 0.03),
                FutureSpread("NG", "NGF18", "NGH18", 0.04),
                FutureSpread("NG", "NGF18", "NGH18", 0.05),
                FutureSpread("NG", "NGF18", "NGH18", 0.06),
                FutureSpread("NG", "NGF18", "NGH18", 0.07)
              )



              Source
                .tick(0.seconds, 1.milli, spreads.head)
                .via(ActorFlow.ask(listings.head)((el: Signal, replyTo: ActorRef[Action]) => Asking(el, replyTo)))
                .runForeach(println)
              Behaviors.same

            case _ => Behaviors.same
          }
      }
  }
}
