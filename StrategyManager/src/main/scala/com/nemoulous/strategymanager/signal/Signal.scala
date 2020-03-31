package com.nemoulous.strategymanager.signal

// All 'signals' must be present in this file to take advantage of the sealed scala keyword.


/**
 * Base trait
 */
sealed trait Signal


/**
 * Models list the signals they require here
 */
sealed trait ConvergingSpreadsSignals extends Signal


/**
 * All of the individual signals are present down here and extend the traits of the models they are required in
 */
final case class FutureSpread(
                               futureType: String,
                               closeContract: String,
                               farContract: String,
                               spread: Double
                             ) extends ConvergingSpreadsSignals


