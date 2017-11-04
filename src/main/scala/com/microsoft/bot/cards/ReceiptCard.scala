package com.microsoft.bot.cards

import com.microsoft.bot.models.ActivityAttachments
import spray.json.JsonWriter

case class ReceiptCard(
    title: String,
    items: Seq[ReceiptItem],
    facts: Seq[Fact],
    total: String,
    tax: String,
    vat: Option[String] = None,
    buttons: Seq[CardAction],
    tap: Option[CardAction] = None
) {

  def toAttachment()(implicit writer: JsonWriter[ReceiptCard]): ActivityAttachments = {
    import spray.json._
    ActivityAttachments(
      contentType = Option("application/vnd.microsoft.card.receipt"),
      content = Option(this.toJson)
    )
  }
}
case class Fact(key: String, value: String)

case class ReceiptItem(
    title: String,
    subtitle: Option[String] = None,
    text: Option[String] = None,
    image: CardImage,
    price: String,
    quantity: String,
    tap: Option[CardAction] = None
)
