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
        Behaviors.supervise(handler(initialState())).onFailure[Exception](SupervisorStrategy.restart)

    }

  private def handler(state: State): Behavior[Asking[S]] = Behaviors.receiveMessage {
    message =>
      val response = handleSignal(ModelInput(state, message.payload))
      message.replyTo ! DesiredWeight("NG", 1.0)
      handler(response.state)
  }

  //
  //  def getFlow(): Flow[S, Action, NotUsed] = Flow[S]
  //    .via(ActorFlow.ask(actor(initialState())))

}

object Model {
  val genericServiceKey = getServiceKey[Signal]()

  def getServiceKey[T](): ServiceKey[Asking[T]] = ServiceKey[Asking[T]]("model")
}


