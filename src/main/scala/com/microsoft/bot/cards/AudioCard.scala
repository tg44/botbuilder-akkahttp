package com.microsoft.bot.cards

import com.microsoft.bot.models.ActivityAttachments
import spray.json.JsonWriter

case class AudioCard(
    title: String,
    subtitle: String,
    text: String,
    image: ThumbnailUrl,
    media: Seq[MediaUrl],
    buttons: Seq[CardAction],
    aspect: Option[String] = None,
    shareable: Option[Boolean] = None,
    autoloop: Option[Boolean] = None,
    autostart: Option[Boolean] = None
) {

  def toAttachment()(implicit writer: JsonWriter[AudioCard]): ActivityAttachments = {
    import spray.json._
    ActivityAttachments(
      contentType = Option("application/vnd.microsoft.card.audio"),
      content = Option(this.toJson)
    )
  }

}
