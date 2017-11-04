package com.microsoft.bot.models

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.json4s.DefaultFormats
import spray.json.{DefaultJsonProtocol, JsArray, JsObject, RootJsonFormat}

case class BotData(eTag: String, data: Option[JsArray])

trait BotDataJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val botDataJsonFormatter: RootJsonFormat[BotData] = jsonFormat2(BotData)
}
