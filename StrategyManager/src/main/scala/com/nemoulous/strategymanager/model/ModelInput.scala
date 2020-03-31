package com.nemoulous.strategymanager.model

case class ModelInput[State, Signal](state: State, signal: Signal)