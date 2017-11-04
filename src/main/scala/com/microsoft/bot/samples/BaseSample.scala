package com.microsoft.bot.samples

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.RouteResult.Complete
import akka.stream.ActorMaterializer
import com.microsoft.bot.bot.{ChatConnector, DefaultChatConnectorSettings}
import com.microsoft.bot.samples.AttachementReceiveSample.ct

import scala.concurrent.Future

trait BaseSample {

  import akka.http.scaladsl.server.Directives._

  val ct: ChatConnector

  implicit val actorSystem = ActorSystem("server")
  implicit val mat = ActorMaterializer()
  implicit val ex = actorSystem.dispatcher

  val settings = DefaultChatConnectorSettings(
    appId = "xxxx",
    appPassword = "xxxx",
    gzipData = false,
    stateEndpoint = "stateEndpoint",
    openIdMetadata = "openidMeta"
  )

  val routes: Route = pathPrefix("api") {
    pathPrefix("messages") { requestCtx =>
      ct.requestHandler(requestCtx.request).map(Complete)
    }
  }

  val adminApiBindingFuture: Future[ServerBinding] = Http()
    .bindAndHandle(routes, "localhost", 8080)
    .map(binding => {
      println("Server started")
      binding
    })

}
