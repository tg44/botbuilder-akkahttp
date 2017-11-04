package com.microsoft.bot.cards

import com.microsoft.bot.models.ActivityAttachments
import spray.json.JsonWriter

case class SignInCard(
    text: String,
    buttons: Seq[CardAction]
) {

  def toAttachment()(implicit writer: JsonWriter[SignInCard]): ActivityAttachments = {
    import spray.json._
    ActivityAttachments(
      contentType = Option("application/vnd.microsoft.card.signin"),
      content = Option(this.toJson)
    )
  }

}
