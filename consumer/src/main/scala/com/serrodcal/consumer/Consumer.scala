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
object Consumer extends App {

  val config = ConfigFactory.load()

  println("Hi, world!")

}
