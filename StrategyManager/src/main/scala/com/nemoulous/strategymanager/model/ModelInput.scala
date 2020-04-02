package com.nemoulous.strategymanager.model

/**
 * Specifies the input to the model
 * @param state the current model state
 * @param signal the curent signal to be parsed
 * @tparam State the type of the state
 * @tparam Signal the type of the signal to be parsed
 */
case class ModelInput[State, Signal](state: State, signal: Signal)