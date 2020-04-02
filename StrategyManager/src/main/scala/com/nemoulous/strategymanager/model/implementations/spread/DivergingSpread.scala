package com.nemoulous.strategymanager.model.implementations.spread

import com.nemoulous.strategymanager.model.{Action, DesiredWeight, Model, ModelInput, ModelOutput, NoAction}
import com.nemoulous.strategymanager.signal.{ConvergingSpreadsSignals, FutureSpread}

case class DivergingSpreadState(
                                 lastSpreads: List[Double],
                                 tradeOn: Boolean,
                                 ticksSinceTradeOn: Int
                               )

class DivergingSpread(val lookback: Int) extends Model[ConvergingSpreadsSignals, DivergingSpreadState] {
  override protected def initialize(): Unit = ()

  override protected def initialState(): DivergingSpreadState = DivergingSpreadState(List(), false, 0)

  override protected def handleSignal:
  PartialFunction[
    ModelInput[DivergingSpreadState, ConvergingSpreadsSignals],
    ModelOutput[DivergingSpreadState, Action]
  ] = {
    case ModelInput(state, signal) =>
      signal match {
        case FutureSpread(futureType, _, _, spread) =>

          val newSpread = (spread :: state.lastSpreads).take(lookback)

          val modelOutput: ModelOutput[DivergingSpreadState, Action] =
            if (state.lastSpreads.size == lookback) {
              if (state.tradeOn) {
                if (state.ticksSinceTradeOn == 10) {
                  ModelOutput(DivergingSpreadState(newSpread, false, 0), DesiredWeight(futureType, 0))
                } else {
                  ModelOutput(state.copy(lastSpreads = newSpread, ticksSinceTradeOn = state.ticksSinceTradeOn + 1), NoAction)
                }
              } else {
                val allIncreasing = state.lastSpreads.zip(state.lastSpreads.tail).forall { case (front, back) => front > back }
                if(allIncreasing) {
                  ModelOutput(DivergingSpreadState(newSpread, true, 0), DesiredWeight(futureType, 1))
                } else {
                  ModelOutput(state.copy(lastSpreads = newSpread), NoAction)
                }
              }
            } else {
              ModelOutput(state.copy(lastSpreads = newSpread), NoAction)
            }

          modelOutput
      }
  }


  override protected def shutdown(): Unit = ()

  override val modelName: String = "Diverging Spreads"

}
