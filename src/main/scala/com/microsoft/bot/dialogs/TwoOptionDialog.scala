package com.microsoft.bot.dialogs

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.microsoft.bot.cards.{ActionTypes, CardAction, CardsJsonSupport, HeroCard}
import com.microsoft.bot.dialogs.Dialog.Receive
import com.microsoft.bot.models.{Activity, ActivityAttachments}
import spray.json._

import scala.concurrent.Future

class TwoOptionDialog(val session: Session) extends Dialog with CardsJsonSupport with DefaultDialogsJsonSupport {
  override def receive: Receive = {
    case Dialog.AddedToStack =>
      session.send(asAttachement)
    case msg: Activity if msg.header.`type` == Option("message") =>
      if (msg.getText.toLowerCase == data.positiveOption.toLowerCase) {
        Future(session.endDialog(Option(true)))
      } else if (msg.getText.toLowerCase == data.negativeOption.toLowerCase) {
        Future(session.endDialog(Option(false)))
      } else if (data.retries == 0) {
        Future(session.endDialog(None))
      } else {
        data = data.copy(retries = data.retries - 1)
        session.send(asAttachement)
      }
    case _ =>
      Future()
  }

  var data: TwoOptionData = _

  override def serialize(): JsObject = data.toJson.asJsObject

  override def deserialize(obj: JsValue): Unit = {
    data = obj.convertTo[TwoOptionData]
  }

  lazy val heroCard = HeroCard(
    title = data.title,
    subtitle = "",
    text = "",
    images = Seq(),
    buttons = Seq(
      CardAction(Option(ActionTypes.ImBack), Option(data.positiveOption), None, Option(JsString(data.positiveOption))),
      CardAction(Option(ActionTypes.ImBack), Option(data.negativeOption), None, Option(JsString(data.negativeOption)))
    )
  )
  lazy val asAttachement: ActivityAttachments = heroCard.toAttachment()
}

case class TwoOptionData(title: String, positiveOption: String, negativeOption: String, retries: Int = 3)
