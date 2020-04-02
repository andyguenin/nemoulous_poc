package com.nemoulous.strategymanager.model

import spray.json._
import DefaultJsonProtocol._

/**
 * An action is the output of a model. All actions must be present in this file
 */
sealed trait Action


/**
 * Sets the desired weight of a contract
 * @param contract
 * @param weight
 */
case class DesiredWeight(contract: String, weight: Double) extends Action


/**
 * This is a no op action, emitted if the model does not want to take an action. Akka streams require that there be an
 * output for each input, so after this is emitted we will use the filter operation to remove NoActions
 */
object NoAction extends Action


/**
 * The Action companion object contains json converters. Once moved to a real system, we will likely use protobuf, avro,
 * thrift, or another serialization method. For now, json works
 */
object Action {
  implicit val desiredWeight = jsonFormat2(DesiredWeight)

  def toJson(e: Action): JsValue = {
    e match {
      case e: DesiredWeight => e.toJson
      case NoAction => JsNull
    }
  }
}