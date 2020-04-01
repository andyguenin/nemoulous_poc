package com.nemoulous.strategymanager.model

import akka.NotUsed
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.{ActorSystem, Behavior, SupervisorStrategy}
import akka.actor.typed.scaladsl.Behaviors
import com.nemoulous.strategymanager.signal.Signal
import akka.stream.scaladsl._
import akka.stream.typed.scaladsl.ActorFlow


trait Model[S <: Signal, State] {

  val modelName: String

  protected def initialize(): Unit

  protected def initialState(): State

  protected def handleSignal: PartialFunction[ModelInput[State, S], ModelOutput[State, Action]]

  protected def shutdown(): Unit

  def actor(): Behavior[Asking[S]] =
    Behaviors.setup[Asking[S]] {
      context =>
        context.system.receptionist ! Receptionist.Register(Model.getServiceKey[S](), context.self)
        initialize()
        Behaviors.supervise(handler(initialState())).onFailure[Exception](SupervisorStrategy.restart)
    }

  private def handler(state: State): Behavior[Asking[S]] = Behaviors.receiveMessage {
    message =>
      val response = handleSignal(ModelInput(state, message.payload))
      message.replyTo ! response.action
      handler(response.state)
  }
}

object Model {
  val genericServiceKey = getServiceKey[Signal]()

  def getServiceKey[T](): ServiceKey[Asking[T]] = ServiceKey[Asking[T]]("model")
}


