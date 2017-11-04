package com.microsoft.bot.cards

import com.microsoft.bot.models.ActivityAttachments
import spray.json.JsonWriter

case class ThumbnailCard(
    title: String,
    subtitle: String,
    text: String,
    images: Seq[CardImage],
    buttons: Seq[CardAction],
    tap: Option[CardAction] = None
) {

  def toAttachment()(implicit writer: JsonWriter[ThumbnailCard]): ActivityAttachments = {
    import spray.json._
    ActivityAttachments(
      contentType = Option("application/vnd.microsoft.card.thumbnail"),
      content = Option(this.toJson)
    )
  }

}
