package com.nemoulous.strategymanager.model.implementations.spread

import com.nemoulous.strategymanager.model.{DesiredWeight, Model, ModelInput, ModelOutput}
import com.nemoulous.strategymanager.signal.{ConvergingSpreadsSignals, FutureSpread}

case class ConvergingSpreadsState(
                                 state: Int
                                 )

object ConvergingSpreads extends Model[ConvergingSpreadsSignals, ConvergingSpreadsState] {
  override protected def initialize(): Unit = ()

  override protected def initialState(): ConvergingSpreadsState = ConvergingSpreadsState(0)

  override protected def handleSignal:
  PartialFunction[
    ModelInput[ConvergingSpreadsState, ConvergingSpreadsSignals],
    ModelOutput[ConvergingSpreadsState, DesiredWeight]
  ] = {
    case ModelInput(state, signal) =>
      signal match {
        case FutureSpread(futureType, _, _, spread) =>
          ModelOutput(ConvergingSpreadsState(state.state + 1), DesiredWeight(futureType, state.state.toDouble))
      }
  }



  override protected def shutdown(): Unit = ()

  override val modelName: String = "Converging Spreads"

}
