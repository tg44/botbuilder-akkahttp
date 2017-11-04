package com.microsoft.bot.models

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, JsonWriter, RootJsonFormat}

object FacebookSpecific {

  case class FacebookMessage(text: String, quick_replies: Seq[FacebookQuickReply])

  case class FacebookQuickReply(
      content_type: String,
      title: String,
      payload: String,
      image_url: String = ""
  )

  object FacebookContentTypes {
    val text = "text"
    val location = "location"
  }

  trait FacebookJsonSupport { self: SprayJsonSupport with DefaultJsonProtocol =>

    implicit val FacebookQuickReplyJsonFormatter: RootJsonFormat[FacebookQuickReply] = jsonFormat4(FacebookQuickReply)
    implicit val FacebookMessageJsonFormatter: RootJsonFormat[FacebookMessage] = jsonFormat2(FacebookMessage)
    implicit val GeoCoordinatesJsonFormatter: RootJsonFormat[GeoCoordinates] = jsonFormat3(GeoCoordinates)
    implicit val PlaceJsonFormatter: RootJsonFormat[Place] = jsonFormat1(Place)
  }

  case class Place(geo: GeoCoordinates)

  case class GeoCoordinates(elevation: Double, latitude: Double, longitude: Double)
}
