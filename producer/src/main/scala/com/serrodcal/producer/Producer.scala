package com.serrodcal.producer

import akka.NotUsed
import akka.event.Logging
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import akka.http.scaladsl.server.Route

import scala.io.StdIn
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.Http
import akka.kafka.ProducerSettings
import akka.stream.scaladsl.Source
import org.apache.kafka.common.serialization.StringSerializer
import akka.actor.ActorSystem
import org.apache.kafka.clients.producer.ProducerRecord

import scala.concurrent.duration._

/**
 * @author serrodcal
 */
object Producer extends App {

  val config = ConfigFactory.load()

  val bootstrapServer = config.getString("akka.kafka.producer.kafka-clients.bootstrap.server")

  val producerConfig = config.getConfig("akka.kafka.producer")

  val producerSettings =
    ProducerSettings(producerConfig, new StringSerializer, new StringSerializer)
      .withBootstrapServers(bootstrapServer)

  implicit val system = ActorSystem(config.getString("server.actor-system"))

  implicit val materializer = ActorMaterializer()

  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher

  implicit val logger = Logging(system, getClass)

  implicit val timeout = Timeout(1 seconds)

  val route : Route = post {
    path("publish") {
      entity(as[String]) { message =>
        logger.info(s"Message recived: {$message}")
        Source.single(message)
          .map(message => new ProducerRecord[String, String]("myTopic", message))
          .runWith(akka.kafka.scaladsl.Producer.plainSink(producerSettings))
        complete(StatusCodes.Accepted, "Message recived and sent to Kafka!")
      }
    }
  }

  // Pipe several routes: val routes = route1 ~ route2 ...
  val routes = route

  val host = config.getString("server.host")
  val port = config.getInt("server.port")
  val bindingFuture = Http().bindAndHandle(routes, host, port)

  logger.info(s"Server online at http://$host:$port/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  logger.info(s"Server stopped :(")
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done*/

}
