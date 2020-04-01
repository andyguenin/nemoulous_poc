package com.nemoulous.strategymanager.model

import spray.json._
import DefaultJsonProtocol._

sealed trait Action

case class DesiredWeight(contract: String, weight: Double) extends Action

object Action {
  implicit val desiredWeight = jsonFormat2(DesiredWeight)

  def toJson(e: Action): JsValue = {
    e match {
      case e: DesiredWeight => e.toJson
    }
  }
}