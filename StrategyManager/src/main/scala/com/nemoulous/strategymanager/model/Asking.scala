package com.nemoulous.strategymanager.model

import akka.actor.typed.ActorRef

case class Asking[+T](payload: T, replyTo: ActorRef[Action])

