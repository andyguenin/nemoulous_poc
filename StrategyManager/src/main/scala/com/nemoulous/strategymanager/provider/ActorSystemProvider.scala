package com.nemoulous.strategymanager.provider

import akka.actor.ActorSystem
import akka.stream.Materializer

/**
 * This trait is to ensure that inheriting classes have an actor system in them
 */
trait ActorSystemProvider {

  implicit val system: ActorSystem
  lazy implicit val mat = Materializer

}
