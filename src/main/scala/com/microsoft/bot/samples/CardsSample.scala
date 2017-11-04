package com.microsoft.bot.samples

import com.microsoft.bot.bot.ChatConnector
import com.microsoft.bot.cards._
import com.microsoft.bot.dialogs.Dialog.Receive
import com.microsoft.bot.dialogs.{Dialog, Session}
import com.microsoft.bot.models.{Activity, ActivityAttachments, Content}
import spray.json.JsString

import scala.concurrent.Future

object CardsSample extends App with BaseSample {

  val ct: ChatConnector = ChatConnector.withMicrosoftStorage(settings, classOf[CardSampleRootDialog])

}

class CardSampleRootDialog(val session: Session) extends Dialog with CardsJsonSupport {

  override def receive: Receive = {
    case x: Activity => sendAttachement(x)
    case _ => Future()
  }

  val help =
    """
      |1 hero card
      |2 thumbnail card
      |3 receipt card
      |4 sign in
      |5 animation card
      |6 video card
      |7 audio card
      |8 CarouselCards
      |9 position
      |10 multipleButton
    """.stripMargin

  def sendAttachement(message: Activity): Future[Unit] = {
    if (message.header.`type` == Option("message")) {
      val msgText = message.content.text.get

      val reply: Activity = msgText match {
        case "1" => createReplyFromAttachment(message, getHeroCard())
        case "2" => createReplyFromAttachment(message, getThumbnailCard())
        case "3" => createReplyFromAttachment(message, getReceiptCard())
        case "4" => createReplyFromAttachment(message, getSignInCard())
        case "5" => createReplyFromAttachment(message, getAnimationCard())
        case "6" => createReplyFromAttachment(message, getVideoCard())
        case "7" => createReplyFromAttachment(message, getAudioCard())
        case "8" => createCarouserReply(message)
        case "9" => message.createReply(Option("not yet")) // todo works only with fb?
        case "10" => createReplyFromAttachment(message, multibuttonHero())
        case _ => message.createReply(Option(help))

      }

      session.send(reply).map(_ => ())
    } else Future.successful()
  }

  def createReplyFromAttachment(message: Activity, attachment: ActivityAttachments) = {
    message.createReply().copy(content = Content(attachments = Option(Seq(attachment))))
  }

  def createCarouserReply(message: Activity) = {
    message
      .createReply()
      .copy(
        channelSettings = message.channelSettings.copy(attachmentLayout = Option(AttachmentLayoutTypes.Carousel)),
        content = Content(
          attachments = Option(Seq(getHeroCard(), getThumbnailCard(), getSignInCard(), getVideoCard(), getAudioCard()))
        )
      )
  }

  def getHeroCard() = {
    val heroCard = HeroCard(
      title = "BotFramework Hero Card",
      subtitle = "Your bots — wherever your users are talking",
      text =
        "Build and connect intelligent bots to interact with your users naturally wherever they are, from text/sms to Skype, Slack, Office 365 mail and other popular services.",
      images = Seq(CardImage(url = Option("https://sec.ch9.ms/ch9/7ff5/e07cfef0-aa3b-40bb-9baa-7c9ef8ff7ff5/buildreactionbotframework_960.jpg"))),
      buttons = Seq(CardAction(Option(ActionTypes.OpenUrl), Option("Get Started"), None, Option(JsString("https://docs.microsoft.com/bot-framework"))))
    )
    heroCard.toAttachment()
  }

  def getThumbnailCard() = {
    val thumbnailCard = ThumbnailCard(
      title = "BotFramework Thumbnail Card",
      subtitle = "Your bots — wherever your users are talking",
      text =
        "Build and connect intelligent bots to interact with your users naturally wherever they are, from text/sms to Skype, Slack, Office 365 mail and other popular services.",
      images = Seq(CardImage(url = Option("https://sec.ch9.ms/ch9/7ff5/e07cfef0-aa3b-40bb-9baa-7c9ef8ff7ff5/buildreactionbotframework_960.jpg"))),
      buttons = Seq(CardAction(Option(ActionTypes.OpenUrl), Option("Get Started"), None, Option(JsString("https://docs.microsoft.com/bot-framework"))))
    )
    thumbnailCard.toAttachment()
  }

  def getReceiptCard() = {
    val receiptCard = ReceiptCard(
      title = "John Doe",
      facts = Seq(Fact("Order Number", "1234"), Fact("Payment Method", "VISA 5555-****")),
      items = Seq(
        ReceiptItem(
          "Data Transfer",
          price = "$ 38.45",
          quantity = "368",
          image = CardImage(url = Option("https://github.com/amido/azure-vector-icons/raw/master/renders/traffic-manager.png"))
        ),
        ReceiptItem(
          "App Service",
          price = "$ 45.00",
          quantity = "720",
          image = CardImage(url = Option("https://github.com/amido/azure-vector-icons/raw/master/renders/cloud-service.png"))
        )
      ),
      tax = "$ 7.50",
      total = "$ 90.95",
      buttons = Seq(
        CardAction(
          Option(ActionTypes.OpenUrl),
          Option("More information"),
          Option("https://account.windowsazure.com/content/6.10.1.38-.8225.160809-1618/aux-pre/images/offer-icon-freetrial.png"),
          Option(JsString("https://azure.microsoft.com/en-us/pricing/"))
        )
      )
    )
    receiptCard.toAttachment()
  }

  def getSignInCard() = {
    val signInCard = SignInCard(
      text = "BotFramework Sign-in Card",
      buttons = Seq(CardAction(Option(ActionTypes.Signin), Option("Sign-in"), None, Option(JsString("https://login.microsoftonline.com/"))))
    )

    signInCard.toAttachment()
  }

  def getAnimationCard() = {
    val aimationCard = AnimationCard(
      title = "Microsoft Bot Framework",
      subtitle = "Animation Card",
      text = "",
      buttons = Seq(),
      image = ThumbnailUrl(
        url = Option("https://docs.microsoft.com/en-us/bot-framework/media/how-it-works/architecture-resize.png")
      ),
      media = Seq(
        MediaUrl(
          url = Option("http://i.giphy.com/Ki55RUbOV5njy.gif")
        )
      )
    )
    aimationCard.toAttachment()
  }

  def getVideoCard() = {
    val videoCard = VideoCard(
      title = "Big Buck Bunny",
      subtitle = "by the Blender Institute",
      text =
        "Big Buck Bunny (code-named Peach) is a short computer-animated comedy film by the Blender Institute, part of the Blender Foundation. Like the foundation's previous film Elephants Dream, the film was made using Blender, a free software application for animation made by the same foundation. It was released as an open-source film under Creative Commons License Attribution 3.0.",
      image = ThumbnailUrl(
        url = Option("https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/Big_buck_bunny_poster_big.jpg/220px-Big_buck_bunny_poster_big.jpg")
      ),
      media = Seq(
        MediaUrl(
          url = Option("http://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_320x180.mp4")
        )
      ),
      buttons = Seq(
        CardAction(
          title = Option("Learn More"),
          `type` = Option(ActionTypes.OpenUrl),
          image = None,
          value = Option(JsString("https://peach.blender.org/"))
        )
      )
    )
    videoCard.toAttachment()
  }

  def getAudioCard() = {
    val audioCard = AudioCard(
      title = "I am your father",
      subtitle = "Star Wars: Episode V - The Empire Strikes Back",
      text =
        "The Empire Strikes Back (also known as Star Wars: Episode V – The Empire Strikes Back) is a 1980 American epic space opera film directed by Irvin Kershner. Leigh Brackett and Lawrence Kasdan wrote the screenplay, with George Lucas writing the film's story and serving as executive producer. The second installment in the original Star Wars trilogy, it was produced by Gary Kurtz for Lucasfilm Ltd. and stars Mark Hamill, Harrison Ford, Carrie Fisher, Billy Dee Williams, Anthony Daniels, David Prowse, Kenny Baker, Peter Mayhew and Frank Oz.",
      image = ThumbnailUrl(
        url = Option("https://upload.wikimedia.org/wikipedia/en/3/3c/SW_-_Empire_Strikes_Back.jpg")
      ),
      media = Seq(
        MediaUrl(
          url = Option("http://www.wavlist.com/movies/004/father.wav")
        )
      ),
      buttons = Seq(
        CardAction(
          title = Option("Read More"),
          `type` = Option(ActionTypes.OpenUrl),
          image = None,
          value = Option(JsString("https://en.wikipedia.org/wiki/The_Empire_Strikes_Back"))
        )
      )
    )
    audioCard.toAttachment()
  }

  def multibuttonHero() = {
    val heroCard = HeroCard(
      title = "Multibutton Card",
      subtitle = "",
      text = "",
      images = Seq(),
      buttons = Seq(
        CardAction(Option(ActionTypes.OpenUrl), Option("OpenUrl"), None, Option(JsString("https://docs.microsoft.com/bot-framework"))),
        CardAction(Option(ActionTypes.MessageBack), Option("messageBack"), None, Option(JsString("https://docs.microsoft.com/bot-framework"))),
        CardAction(Option(ActionTypes.Signin), Option("signIn"), None, Option(JsString("https://docs.microsoft.com/bot-framework"))),
        CardAction(Option(ActionTypes.DownloadFile), Option("download"), None, Option(JsString("https://docs.microsoft.com/bot-framework"))),
        CardAction(Option(ActionTypes.PostBack), Option("postBack"), None, Option(JsString("https://docs.microsoft.com/bot-framework"))),
        CardAction(Option(ActionTypes.ShowImage), Option("showImage"), None, Option(JsString("https://docs.microsoft.com/bot-framework"))),
        CardAction(Option(ActionTypes.ImBack), Option("imBack"), None, Option(JsString("https://docs.microsoft.com/bot-framework")))
      )
    )
    heroCard.toAttachment()
  }
}
