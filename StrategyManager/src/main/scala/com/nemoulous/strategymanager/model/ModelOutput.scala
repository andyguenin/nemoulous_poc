package com.nemoulous.strategymanager.model

case class ModelOutput[State, +Action](state: State, action: Action)
