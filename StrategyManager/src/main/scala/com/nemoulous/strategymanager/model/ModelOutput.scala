package com.nemoulous.strategymanager.model

/**
 * This represents the output of the model
 * @param state the new state of the model
 * @param action the action that the model emits
 * @tparam State the type of the state of the model
 * @tparam A the type of the action to be emitted.
 */
case class ModelOutput[State, +A <: Action](state: State, action: A)
