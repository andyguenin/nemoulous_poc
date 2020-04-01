package com.nemoulous.strategymanager.provider

import akka.actor.ActorSystem
import akka.stream.Materializer

trait ActorSystemProvider {

  implicit val system: ActorSystem
  lazy implicit val mat = Materializer

}
