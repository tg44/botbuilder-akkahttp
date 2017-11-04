package com.microsoft.bot.bot

import java.net.URLEncoder

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.microsoft.bot.dialogs.{Dialog, SessionContext}
import com.microsoft.bot.models.Activity
import com.microsoft.bot.storage.{BotStorage, MicrosoftCloudBotStorage, NoneStorerStorage}
import com.microsoft.bot.util.StringUrl
import com.typesafe.scalalogging.Logger
import pdi.jwt.{JwtJson4s, JwtOptions}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

trait ChatConnectorSettings {
  val appId: String
  val appPassword: String
  val gzipData: Boolean
  val endpoint: ChatConnectorEndpoint
  val stateEndpoint: String
  val openIdMetadata: String
}

trait ChatConnectorEndpoint {
  val refreshEndpoint: String
  val refreshScope: String
  val botConnectorOpenIdMetadata: String
  val botConnectorIssuer: String
  val botConnectorAudience: String
  val msaOpenIdMetadata: String
  val msaIssuer: String
  val msaAudience: String
  val emulatorOpenIdMetadata: String
  val emulatorIssuerV1: String
  val emulatorIssuerV2: String
  val emulatorAudience: String
  val stateEndpoint: String
}

case class DefaultChatConnectorSettings(
    appId: String,
    appPassword: String,
    gzipData: Boolean = false,
    stateEndpoint: String = "https://state.botframework.com",
    openIdMetadata: String = "https://login.botframework.com/v1/.well-known/openidconfiguration"
) extends ChatConnectorSettings {
  val endpoint: ChatConnectorEndpoint = ChatConnectorEndpoint(appId, openIdMetadata, stateEndpoint)
}

object ChatConnectorEndpoint {
  def apply(
      appId: String,
      openIdMetadata: String = "https://login.botframework.com/v1/.well-known/openidconfiguration",
      stateEndpoint: String = "https://state.botframework.com"
  ) = DefaultChatConnectorEndpoint(appId, openIdMetadata, stateEndpoint)

  case class DefaultChatConnectorEndpoint(
      appId: String,
      openIdMetadata: String,
      stateEndpoint: String
  ) extends ChatConnectorEndpoint {
    val botConnectorAudience: String = appId
    val emulatorAudience: String = appId
    val botConnectorOpenIdMetadata = openIdMetadata
    val refreshEndpoint: String = "https://login.microsoftonline.com/botframework.com/oauth2/v2.0/token"
    val refreshScope: String = "https://api.botframework.com/.default"
    val botConnectorIssuer: String = "https://api.botframework.com"
    val msaOpenIdMetadata: String = "https://login.microsoftonline.com/common/v2.0/.well-known/openid-configuration"
    val msaIssuer: String = "https://sts.windows.net/72f988bf-86f1-41af-91ab-2d7cd011db47/"
    val msaAudience: String = "https://graph.microsoft.com"
    val emulatorOpenIdMetadata: String = "https://login.microsoftonline.com/botframework.com/v2.0/.well-known/openid-configuration"
    val emulatorIssuerV1: String = "https://sts.windows.net/d6d49420-f39b-4df7-a1dc-d59a935871db/"
    val emulatorIssuerV2: String = "https://login.microsoftonline.com/d6d49420-f39b-4df7-a1dc-d59a935871db/v2.0"
  }
}

object ChatConnector {
  def emptyHandler(implicit executionContext: ExecutionContext): PartialFunction[Activity, Future[Boolean]] = { case _ => Future(false) }

  def withInMemoryStorage(
      settings: ChatConnectorSettings,
      coreDialog: Class[_ <: Dialog],
      defaultHandler: Option[PartialFunction[Activity, Future[Boolean]]] = None
  )(implicit actorSystem: ActorSystem, materializer: Materializer, executionContext: ExecutionContext) = {
    val tokenHelper =
      new SendTokenHelper(ChatConnector.USER_AGENT, settings.appId, settings.appPassword, settings.endpoint.refreshEndpoint, settings.endpoint.refreshScope)
    new ChatConnector(
      settings,
      tokenHelper,
      coreDialog,
      NoneStorerStorage(),
      defaultHandler
    )
  }

  def withMicrosoftStorage(
      settings: ChatConnectorSettings,
      coreDialog: Class[_ <: Dialog],
      defaultHandler: Option[PartialFunction[Activity, Future[Boolean]]] = None
  )(implicit actorSystem: ActorSystem, materializer: Materializer, executionContext: ExecutionContext) = {
    val tokenHelper =
      new SendTokenHelper(ChatConnector.USER_AGENT, settings.appId, settings.appPassword, settings.endpoint.refreshEndpoint, settings.endpoint.refreshScope)
    val storage = MicrosoftCloudBotStorage(settings, tokenHelper)
    new ChatConnector(
      settings,
      tokenHelper,
      coreDialog,
      storage,
      None
    )
  }

  lazy val version = getClass.getPackage.getImplementationVersion
  val MAX_DATA_LENGTH = 65000
  lazy val USER_AGENT = "Microsoft-BotFramework/3.1 (BotBuilder AkkaHttp/" + version + ")"
}

class ChatConnector(
    settings: ChatConnectorSettings,
    tokenHelper: SendTokenHelper,
    coreDialog: Class[_ <: Dialog],
    store: BotStorage,
    defaultHandler: Option[PartialFunction[Activity, Future[Boolean]]]
)(implicit actorSystem: ActorSystem, materializer: Materializer, executionContext: ExecutionContext)
    extends JsonSupport {
  import spray.json._

  val logger = Logger(classOf[ChatConnector])
  val requestWaitTimeout = 8.seconds

  val botConnectorOpenIdMetadata = new OpenIdHelper(settings.endpoint.botConnectorOpenIdMetadata)
  val msaOpenIdMetadata = new OpenIdHelper(settings.endpoint.msaOpenIdMetadata)
  val emulatorOpenIdMetadata = new OpenIdHelper(settings.endpoint.emulatorOpenIdMetadata)

  val sessionStore = new SessionStore(SessionContext(this, store, coreDialog, defaultHandler.getOrElse(ChatConnector.emptyHandler)))

  //verify bot fw
  //need an akkahttp route check the header and deserialize the body to action call the dispatch
  val requestHandler: HttpRequest => Future[HttpResponse] = { request =>
    val authHeader = request.headers.find(_.name().toLowerCase == "authorization")
    val tokenOpt = authHeader.flatMap { header =>
      if (header.value().startsWith("Bearer ")) {
        Option(header.value().split(" ")(1))
      } else {
        None
      }
    }

    Unmarshal(request.entity).to[Activity].flatMap { message =>
      tokenOpt match {
        case Some(token) if message.isEmulator =>
          val jwtPayload = getPayloadFromToken(token)
          if (isEmulatorTokenInvalid(jwtPayload)) {
            println(settings.appId)
            println(jwtPayload.fields("ver").toString())
            println(jwtPayload.fields("appid").toString())
            logger.error("ChatConnector: receive - invalid token. Requested by unexpected app ID.")
            Future.successful(HttpResponse(StatusCodes.Forbidden))
          } else {
            validateTheJwt(token, jwtPayload, true).flatMap {
              case true =>
                sessionStore.routeActivity(message).map(_ => HttpResponse(StatusCodes.OK))
              case false =>
                logger.error("ChatConnector: receive - invalid token. Check bot's app ID & Password.");
                Future.successful(HttpResponse(StatusCodes.Forbidden))
            }
            //???

          }
        case Some(token) =>
          val jwtPayload = getPayloadFromToken(token)
          Await.result(validateTheJwt(token, jwtPayload, false), requestWaitTimeout)
          ???

        case None =>
          logger.error("ChatConnector: receive - no security token sent.");
          Future.successful(HttpResponse(StatusCodes.Unauthorized))
      }
    }

  }

  def send(activity: Activity): Future[HttpResponse] = {
    tokenHelper.getHeaders.flatMap { headers =>
      var path = "/v3/conversations/" + URLEncoder.encode(activity.header.conversation.get.id.get, "UTF-8") + "/activities"
      if (activity.header.id.isDefined && activity.header.channelId.get != "skype") {
        path += '/' + URLEncoder.encode(activity.header.id.get, "UTF-8")
      }

      Http()
        .singleRequest(
          HttpRequest(
            method = HttpMethods.POST,
            uri = StringUrl.urlJoin(activity.header.serviceUrl.get, path),
            headers = headers,
            entity = HttpEntity(ContentTypes.`application/json`, activity.toJson.compactPrint)
          )
        )
        .map { x =>
          println(x); x
        }
    }
  }

  def send(activity: Future[Activity]): Future[HttpResponse] = {
    activity.flatMap(send(_))
  }

  private def getPayloadFromToken(token: String) = {
    JwtJson4s.decodeRaw(token = token, JwtOptions(signature = false)).get.parseJson.asJsObject
  }

  private def validateTheJwt(token: String, tokenPayload: JsObject, isEmulator: Boolean): Future[Boolean] = {
    val openIdMetadata = if (isEmulator && tokenPayload.fields("iss").convertTo[String] == settings.endpoint.msaIssuer) {
      // This token came from MSA, so check it via the emulator path
      msaOpenIdMetadata
    } else if (isEmulator && tokenPayload.fields("ver").convertTo[String] == "1.0" && tokenPayload
                 .fields("iss")
                 .convertTo[String] == settings.endpoint.emulatorIssuerV1) {
      // This token came from the emulator, so check it via the emulator path
      emulatorOpenIdMetadata
    } else if (isEmulator && tokenPayload.fields("ver").convertTo[String] == "2.0" && tokenPayload
                 .fields("iss")
                 .convertTo[String] == settings.endpoint.emulatorIssuerV2) {
      // This token came from the emulator, so check it via the emulator path
      emulatorOpenIdMetadata
    } else {
      // This is a normal token, so use our Bot Connector verification
      botConnectorOpenIdMetadata
    }
    val kid = JwtJson4s.decodeRawAll(token = token, JwtOptions(signature = false)).get._1.parseJson.asJsObject.fields("kid").convertTo[String]
    openIdMetadata.getKey(kid).map { signature =>
      //todo error handling and logging
      val k = JwtJson4s.decode(token = token, signature.get.key).get
      true
    }
  }

  private def isEmulatorTokenInvalid(jwtPayload: JsObject) = {
    (jwtPayload.fields("ver").convertTo[String] == "2.0" && jwtPayload.fields("azp").convertTo[String] != settings.appId) ||
    (jwtPayload.fields("ver").convertTo[String] != "2.0" && jwtPayload.fields("appid").convertTo[String] != settings.appId)
  }
}
