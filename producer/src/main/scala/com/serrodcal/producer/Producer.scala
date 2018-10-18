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
import akka.stream.scaladsl.{Sink, Source}
import org.apache.kafka.common.serialization.StringSerializer
import akka.actor.ActorSystem
import org.apache.kafka.clients.producer.ProducerRecord

import scala.concurrent.Future
import scala.concurrent.duration._
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.unmarshalling.Unmarshal

import scala.concurrent.Await
import scala.util.{Failure, Success}

import akka.actor.{Actor, ActorRef}

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

  implicit val materializer: ActorMaterializer = ActorMaterializer()

  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher

  implicit val logger = Logging(system, getClass)

  implicit val timeout = Timeout(1 seconds)

  val host = config.getString("server.host")
  val port = config.getInt("server.port")

  val topic = config.getString("akka.kafka.producer.kafka-clients.topic")

  val bufferSize = config.getInt("bufferSize")

  //if the buffer fills up then this strategy drops the oldest elements
  //upon the arrival of a new element.
  val overflowStrategy = akka.stream.OverflowStrategy.dropHead

  val queue = Source.queue[String](bufferSize, overflowStrategy)
    .map(message => new ProducerRecord[String, String](topic, message))
    .to(akka.kafka.scaladsl.Producer.plainSink(producerSettings))
    .run() // in order to "keep" the queue Materialized value instead of the Sink's

  val requestHandler: HttpRequest => HttpResponse = {
    case HttpRequest(POST, Uri.Path("/publish"), _, entity, _) => {
      val bodyMessage: Future[String] = Unmarshal(entity).to[String]
      bodyMessage.onComplete{
        case Success(message) => queue offer message
        case Failure(_) => println("Some error getting message from body.")
      }
      HttpResponse(202, entity = "Message sent to Kafka!")
    }
    case r: HttpRequest =>
      r.discardEntityBytes() // important to drain incoming HTTP Entity stream
      HttpResponse(404, entity = "Unknown resource!")
  }


  val serverSource: Source[Http.IncomingConnection, Future[Http.ServerBinding]] =
    Http().bind(interface = host, port = port)
  val bindingFuture: Future[Http.ServerBinding] =
    serverSource.to(Sink.foreach { connection =>
        println("Accepted new connection from " + connection.remoteAddress)

        connection handleWithSyncHandler requestHandler
        // this is equivalent to
        // connection handleWith { Flow[HttpRequest] map requestHandler }
      }).run()

  logger.info(s"Server online at http://$host:$port/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  logger.info(s"Server stopped :(")
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

}
