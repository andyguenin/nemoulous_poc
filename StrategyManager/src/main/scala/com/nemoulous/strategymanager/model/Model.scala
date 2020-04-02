package com.nemoulous.strategymanager.model

import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.{Behavior, SupervisorStrategy}
import akka.actor.typed.scaladsl.Behaviors
import com.nemoulous.strategymanager.signal.Signal


/**
 * The model trait is inherited by all models. Think of models as finite state machines: incoming signals cause the
 * internal state to change and emit an action. You can see this in the handleSignal method: the existing state, plus a
 * signal, returns a new state and an action to be taken.
 * @tparam S The base signal type that the model consumes.
 * @tparam State The state of the given model.
 */
trait Model[S <: Signal, State] {

  val modelName: String

  /**
   * This will run before the model is launched
   */
  protected def initialize(): Unit

  /**
   * The initial state of the model
   * @return the initial state of the model
   */
  protected def initialState(): State

  /**
   * The model implementation occurs in this method.
   * @return a partial function taking the state and a signal and returning the new state and an action
   */
  protected def handleSignal: PartialFunction[ModelInput[State, S], ModelOutput[State, Action]]

  /**
   * After the model is run, the shutdown hook will run. This is not wired up right now
   */
  protected def shutdown(): Unit

  /**
   * Creates an actor so that the guardian can place it in an akka stream
   * @return The behavior of the actor
   */
  final def actor(): Behavior[Asking[S]] =
    Behaviors.setup[Asking[S]] {
      context =>
        context.system.receptionist ! Receptionist.Register(Model.getServiceKey[S](), context.self)
        initialize()
        Behaviors.supervise(handler(initialState())).onFailure[Exception](SupervisorStrategy.restart)
    }

  /**
   * This is the boundary between the actor system and calling the model logic. They are separated like this for ease
   * of unit testing.
   * @param state The state of the model
   * @return the behavior of the model
   */
  private def handler(state: State): Behavior[Asking[S]] = Behaviors.receiveMessage {
    message =>
      val response = handleSignal(ModelInput(state, message.payload))
      message.replyTo ! response.action
      handler(response.state)
  }
}

/**
 * The model companion object contains information helpful for model service discovery
 */
object Model {
  val genericServiceKey = getServiceKey[Signal]()

  def getServiceKey[T](): ServiceKey[Asking[T]] = ServiceKey[Asking[T]]("model")
}


