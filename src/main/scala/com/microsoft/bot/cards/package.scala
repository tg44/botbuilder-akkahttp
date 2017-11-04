package com.microsoft.bot

import spray.json.JsValue

package object cards {
  case class CardAction(
      `type`: Option[String],
      title: Option[String],
      image: Option[String],
      value: Option[JsValue]
  )

  case class CardImage(
      url: Option[String] = None,
      alt: Option[String] = None,
      tap: Option[CardAction] = None
  )

  case class MediaUrl(
      url: Option[String] = None,
      profile: Option[String] = None
  )

  case class ThumbnailUrl(
      url: Option[String] = None,
      alt: Option[String] = None
  )

  object ActionTypes {
    val OpenUrl = "openUrl"
    val ImBack = "imBack"
    val PostBack = "postBack"
    val PlayAudio = "playAudio"
    val PlayVideo = "playVideo"
    val ShowImage = "showImage"
    val DownloadFile = "downloadFile"
    val Signin = "signin"
    val MessageBack = "messageBack"
  }
  object AttachmentLayoutTypes {
    val List = "list"
    val Carousel = "carousel"
  }
}
