package com.microsoft.bot.bot

import java.security.PublicKey

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.microsoft.bot.bot.OpenIdHelper._
import com.microsoft.bot.util.CryptoHelper
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.concurrent.{ExecutionContext, Future}

class OpenIdHelper(url: String)(implicit actorSystem: ActorSystem, materializer: Materializer, executionContext: ExecutionContext) extends OpenIdJsonSupport {

  var keys = Seq.empty[Key]
  var lastUpdated: Long = 0

  def now(): Long = System.currentTimeMillis() / 1000l

  def getKey(keyId: String): Future[Option[OpenIdMetadataKey]] = {
    val now = System.currentTimeMillis() / 1000l
    if (lastUpdated < (now - 1000 * 60 * 60 * 24 * 5)) {
      refreshCache().map(_ => findKey(keyId))
    } else {
      Future.successful(findKey(keyId))
    }
  }

  def findKey(keyId: String) = {
    keys
      .find(key => key.kid == keyId && key.n.nonEmpty && key.e.nonEmpty)
      .map(key => OpenIdMetadataKey(CryptoHelper.getPem(key.n, key.e), key.endorsements))
  }

  def refreshCache(): Future[Unit] = {
    loadKeys().map { keyList =>
      keys = keyList
      lastUpdated = now()
      ()
    }
  }

  def loadKeys(): Future[Seq[Key]] = {
    Http()
      .singleRequest(HttpRequest(method = HttpMethods.GET, uri = url))
      .flatMap(
        response => Unmarshal(response).to[OpenIdConfig]
      )
      .flatMap(config => Http().singleRequest(HttpRequest(method = HttpMethods.GET, uri = config.jwks_uri)))
      .flatMap { response =>
        Unmarshal(response).to[KeyListDto]
      }
      .map(_.keys)
  }
}

object OpenIdHelper {

  case class Key(
      kty: String,
      use: String,
      kid: String,
      x5t: String,
      n: String,
      e: String,
      x5c: Seq[String],
      endorsements: Option[Seq[String]]
  )

  case class OpenIdMetadataKey(
      key: PublicKey,
      endorsements: Option[Seq[String]]
  )

  case class OpenIdConfig(
      issuer: String,
      authorization_endpoint: String,
      jwks_uri: String,
      id_token_signing_alg_values_supported: Seq[String],
      token_endpoint_auth_methods_supported: Seq[String]
  )

  case class KeyListDto(
      keys: Seq[Key]
  )

  trait OpenIdJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

    implicit val keyJsonFormatter: RootJsonFormat[Key] = jsonFormat8(Key.apply)
    implicit val keyListJsonFormatter: RootJsonFormat[KeyListDto] = jsonFormat1(KeyListDto)
    implicit val openIdConfigJsonFormatter: RootJsonFormat[OpenIdConfig] = jsonFormat5(OpenIdConfig)
  }

}
