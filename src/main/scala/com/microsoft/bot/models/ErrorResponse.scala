package com.microsoft.bot.models

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.json4s.DefaultFormats
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

case class ErrorResponse(error: MicrosoftError)

case class MicrosoftError(code: String, message: String)

trait ErrorResponseJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val jsonFormats = DefaultFormats
  implicit val microsoftErrorJsonFormatter: RootJsonFormat[MicrosoftError] = jsonFormat2(MicrosoftError)
  implicit val errorResponseJsonFormatter: RootJsonFormat[ErrorResponse] = jsonFormat1(ErrorResponse)
}
