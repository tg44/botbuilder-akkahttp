package com.microsoft.bot.dialogs

import com.microsoft.bot.models.FacebookSpecific.Place
import com.microsoft.bot.models.{Activity, FacebookSpecific}

import scala.concurrent.Future

// todo not tested, based on the C# echo bot implementation
class FacebookLocationDialog(val session: Session) extends Dialog with FacebookSpecific.FacebookJsonSupport {
  import spray.json._

  override def receive = {
    case Dialog.AddedToStack =>
      val channelSpecData = FacebookSpecific.FacebookMessage("Please share your location.",
                                                             Seq(FacebookSpecific.FacebookQuickReply(FacebookSpecific.FacebookContentTypes.location, "", "")))
      session.sendChannelSpecificMessage(channelSpecData.toJson)
    case msg: Activity =>
      val location: Option[Place] = msg.content.entities.flatMap(_.find(t => t.`type`.getOrElse("") == "Place").flatMap(_.properties.map(_.convertTo[Place])));
      Future(session.endDialog(location))
    case _ => Future()
  }
}
