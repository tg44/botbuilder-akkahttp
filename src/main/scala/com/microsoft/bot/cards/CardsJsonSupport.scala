package com.microsoft.bot.cards

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait CardsJsonSupport { self: SprayJsonSupport with DefaultJsonProtocol =>

  implicit val thumbnailUrlJsonFormatter: RootJsonFormat[ThumbnailUrl] = jsonFormat2(ThumbnailUrl)
  implicit val mediaUrlJsonFormatter: RootJsonFormat[MediaUrl] = jsonFormat2(MediaUrl)
  implicit val cardActionJsonFormatter: RootJsonFormat[CardAction] = jsonFormat4(CardAction)
  implicit val cardImageJsonFormatter: RootJsonFormat[CardImage] = jsonFormat3(CardImage)
  implicit val factJsonFormatter: RootJsonFormat[Fact] = jsonFormat2(Fact)
  implicit val receiptItemJsonFormatter: RootJsonFormat[ReceiptItem] = jsonFormat7(ReceiptItem)

  implicit val heroCardJsonFormatter: RootJsonFormat[HeroCard] = jsonFormat6(HeroCard)
  implicit val thumbnailCardJsonFormatter: RootJsonFormat[ThumbnailCard] = jsonFormat6(ThumbnailCard)
  implicit val signInCardCardJsonFormatter: RootJsonFormat[SignInCard] = jsonFormat2(SignInCard)
  implicit val videoCardCardJsonFormatter: RootJsonFormat[VideoCard] = jsonFormat10(VideoCard)
  implicit val audioCardCardJsonFormatter: RootJsonFormat[AudioCard] = jsonFormat10(AudioCard)
  implicit val animationCardCardJsonFormatter: RootJsonFormat[AnimationCard] = jsonFormat9(AnimationCard)
  implicit val receiptCardCardJsonFormatter: RootJsonFormat[ReceiptCard] = jsonFormat8(ReceiptCard)

}
