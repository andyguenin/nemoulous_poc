package com.nemoulous.strategymanager.model

case class ModelOutput[State, +A <: Action](state: State, action: A)
