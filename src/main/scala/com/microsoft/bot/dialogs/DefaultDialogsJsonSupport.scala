package com.microsoft.bot.dialogs

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait DefaultDialogsJsonSupport { self: SprayJsonSupport with DefaultJsonProtocol =>

  implicit val twoOptionDataJsonFormatter: RootJsonFormat[TwoOptionData] = jsonFormat4(TwoOptionData)
}
