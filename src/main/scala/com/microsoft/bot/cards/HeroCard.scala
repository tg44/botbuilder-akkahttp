package com.microsoft.bot.cards

import com.microsoft.bot.models.ActivityAttachments
import spray.json.{JsObject, JsValue, JsonWriter}

case class HeroCard(
    title: String,
    subtitle: String,
    text: String,
    images: Seq[CardImage],
    buttons: Seq[CardAction],
    tap: Option[CardAction] = None
) {

  def toAttachment()(implicit writer: JsonWriter[HeroCard]): ActivityAttachments = {
    import spray.json._
    ActivityAttachments(
      contentType = Option("application/vnd.microsoft.card.hero"),
      content = Option(this.toJson)
    )
  }

}
