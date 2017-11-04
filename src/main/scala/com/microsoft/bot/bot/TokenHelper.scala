package com.microsoft.bot.bot

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.microsoft.bot.bot.SendTokenHelper.{SendToken, TokenRefreshJsonSupport, TokenRefreshResponseDto}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

class SendTokenHelper(userAgent: String, appId: String, appPwd: String, refreshEndpoint: String, refreshScope: String)(
    implicit executionContext: ExecutionContext,
    actorSystem: ActorSystem,
    materializer: Materializer
) extends TokenRefreshJsonSupport {

  var token = SendToken("", 0)

  def getToken = {
    if (token.isExpired()) {
      refreshAccessToken.map(_ => token.token)
    } else {
      Future.successful(token.token)
    }
  }

  def getHeaders = {
    getToken.map { token =>
      immutable.Seq(
        RawHeader("User-Agent", userAgent),
        RawHeader("Authorization", s"Bearer $token")
      )
    }
  }

  private def refreshAccessToken: Future[Unit] = {

    val headers: immutable.Seq[HttpHeader] = immutable.Seq(
      RawHeader("User-Agent", userAgent)
    )

    val data = FormData(Map("grant_type" -> "client_credentials", "client_id" -> appId, "client_secret" -> appPwd, "scope" -> refreshScope))

    Http()
      .singleRequest(
        HttpRequest(
          method = HttpMethods.POST,
          uri = refreshEndpoint,
          headers = headers,
          entity = data.toEntity
        )
      )
      .flatMap { resp =>
        Unmarshal(resp).to[TokenRefreshResponseDto]
      }
      .map(resp => token = SendToken(resp.access_token, resp.expires_in - 300))
  }

}

object SendTokenHelper {

  case class SendToken(token: String, expire: Long) {
    def isExpired(): Boolean = {
      (System.currentTimeMillis() / 1000L) >= expire
    }
  }

  case class TokenRefreshRequestDto(
      grant_type: String,
      client_id: String,
      client_secret: String,
      scope: String
  )

  case class TokenRefreshResponseDto(access_token: String, expires_in: Long)

  trait TokenRefreshJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

    implicit val tokenRefreshReqDtoJsonFormatter: RootJsonFormat[TokenRefreshRequestDto] = jsonFormat4(TokenRefreshRequestDto)
    implicit val tokenRefreshRespDtoJsonFormatter: RootJsonFormat[TokenRefreshResponseDto] = jsonFormat2(TokenRefreshResponseDto)
  }

}
