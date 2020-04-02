package com.nemoulous.strategymanager.model.implementations.spread

import com.nemoulous.strategymanager.model.{Action, DesiredWeight, Model, ModelInput, ModelOutput, NoAction}
import com.nemoulous.strategymanager.signal.{ConvergingSpreadsSignals, FutureSpread}


/**
 * Diverging Spread model. See associated README for detailed explanation on how it works.
 *
 * @param lookback
 */
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

          // All possible ModelOutputs will need to update the state to include the newest datapoint
          val newSpread = (spread :: state.lastSpreads).take(lookback)

          val modelOutput: ModelOutput[DivergingSpreadState, Action] =

          // lookback:if Do we have enough history here to look back to the previous n- lookback values?
            if (state.lastSpreads.size == lookback) {
              // tradeon:if Do we have a trade on?
              if (state.tradeOn) {
                // timeelapse:if Have lookback * 2 periods happened?
                if (state.ticksSinceTradeOn == lookback * 2) {
                  // We undo the trade we previously put on
                  ModelOutput(DivergingSpreadState(newSpread, false, 0), DesiredWeight(futureType, 0))
                } else {
                  // We take no action, but update the state to indicate one more period has passed
                  ModelOutput(state.copy(lastSpreads = newSpread, ticksSinceTradeOn = state.ticksSinceTradeOn + 1), NoAction)
                }
              }
              //tradeon:else No trade is current on
              else {
                val allIncreasing = state.lastSpreads.zip(state.lastSpreads.tail).forall { case (front, back) => front > back }
                // allincreasing:if Are all spreads increasing?
                if (allIncreasing) {
                  // Put the trade on!
                  ModelOutput(DivergingSpreadState(newSpread, true, 0), DesiredWeight(futureType, 1))
                }
                // allincreasing:else Not all spreads are increasing
                else {
                  // Do nothing
                  ModelOutput(state.copy(lastSpreads = newSpread), NoAction)
                }
              }
            }
            // lookback:else
            else {
              // Do nothing
              ModelOutput(state.copy(lastSpreads = newSpread), NoAction)
            }

          modelOutput
      }
  }


  override protected def shutdown(): Unit = ()

  override val modelName: String = "Diverging Spreads"

}


/**
 * State of the Diverging Spread model
 *
 * @param lastSpreads       list of spreads, sorted from most recent to least recent
 * @param tradeOn           is there a trade currently on?
 * @param ticksSinceTradeOn How many candles have come in since tradeOn was toggled to be true?
 */
case class DivergingSpreadState(
                                 lastSpreads: List[Double],
                                 tradeOn: Boolean,
                                 ticksSinceTradeOn: Int
                               )