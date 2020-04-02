package com.nemoulous.strategymanager.model

import akka.actor.typed.ActorRef

/**
 * This class allows us to use the akka ask pattern while using the new typed actors
 * @param payload The payload to send to the actor
 * @param replyTo The actorref to reply to
 * @tparam T the type of the payload
 */
case class Asking[+T](payload: T, replyTo: ActorRef[Action])

