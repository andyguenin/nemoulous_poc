package com.nemoulous.strategymanager.signal

import spray.json._
import DefaultJsonProtocol._


// All 'signals' must be present in this file to take advantage of the sealed scala keyword.


/**
 * Base trait
 */
sealed trait Signal {
  val name = this.getClass.getSimpleName
}

sealed trait SignalName {
  val name: String
}

/**
 * Models list the signals they require here
 */
sealed abstract class ConvergingSpreadsSignals(override val name: String) extends Signal


/**
 * All of the individual signals are present down here and extend the traits of the models they are required in
 */
case class FutureSpread(
                               futureType: String,
                               closeContract: String,
                               farContract: String,
                               spread: Double
                             ) extends ConvergingSpreadsSignals(FutureSpreadName.name)
object FutureSpreadName extends SignalName {
  val name = "futurespread"
}




/**
 * Json converters
 */

object Signal {
  def formatter[S <: Signal, SN <: SignalName](s: SN, rootJsonFormat: RootJsonFormat[S]): RootJsonFormat[S] = {
    new RootJsonFormat[S] {
      override def write(obj: S): JsValue = {
        JsObject(
          Map("name" -> JsString(obj.name)) ++ rootJsonFormat.write(obj).asJsObject.fields
        )
      }

      override def read(json: JsValue): S = {
        rootJsonFormat.read(json)
      }
    }
  }



  implicit val futureSpreadConverter = formatter(FutureSpreadName, jsonFormat4(FutureSpread))

  def getObject[E <: Signal](json: JsObject): Signal = {

    val name = json.fields("name").convertTo[String]

    val jsObj = name match {
      case FutureSpreadName.name => futureSpreadConverter.read(json)
    }

    jsObj

  }

}

