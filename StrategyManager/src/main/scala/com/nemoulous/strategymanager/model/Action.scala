package com.nemoulous.strategymanager.model

sealed trait Action

case class DesiredWeight(contract: String, weight: Double) extends Action