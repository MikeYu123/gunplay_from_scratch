package com.mikeyu123.gunplay.server

import java.util.UUID

import akka.actor.{Actor, ActorRef, Terminated}
import akka.http.scaladsl.model.ws.TextMessage
import com.mikeyu123.gunplay.server.messaging.{JsonProtocol, ObjectsMarshaller}
import com.mikeyu123.gunplay.utils.{ControlsParser, SpawnPool, Vector2}
import com.mikeyu123.gunplay_physics.structs.{Point, Vector}
import spray.json._

import scala.util.Try

/**
  * Created by mihailurcenkov on 25.07.17.
  */
case class RegisteredMessage(id: UUID, registered: Boolean = true)
case class Register(name: Option[String] = None)
class ClientConnectionActor(worldActor: ActorRef) extends Actor with JsonProtocol {
  //TODO possibly move connection to constructor to avoid Option handling
  var connection: Option[ActorRef] = None

  val receive: Receive = {
//    Connection initialized
    case RegisterConnection(a: ActorRef) =>
      connection = Some(a)
      context.watch(a)

//      Connection terminated
    case Terminated(a) if connection.contains(a) =>
      connection = None
      context.stop(self)

//      Sink close callback
    case ConnectionClose =>
      context.stop(self)

//    Here we take incoming messages
//      TODO: check whether there might be binary messages
//      TODO: move deserializers to separate layer
    case TextMessage.Strict(t) =>
      val json: ClientMessage = t.parseJson.convertTo[ClientMessage]
      json.`type` match {
        case "controls" =>
          json.message.foreach { message =>
//            TODO this fails if message is broken
            val controls: Controls = message.convertTo[Controls]
            val (velocity: Vector2, angle: Double, click: Boolean) = ControlsParser.parseControls(controls)
            val messageToSend: UpdateControls = UpdateControls(velocity, angle, click)
            worldActor ! messageToSend
          }
        case "register" =>
//          TODO REWORK THIS WHOLE
          val name: String =
            json.message.flatMap(_.convertTo[Register].name).getOrElse("huy")
//            TODO: Exception check
          val messageToSend = AddPlayer(name)
          worldActor ! messageToSend
      }

    case message: PublishUpdates =>
      connection foreach { conn =>
        val messageToSend = message.updates.toJson.toString()
        conn ! TextMessage.Strict(messageToSend)
      }

    case Registered(uuid) =>
//      println(s"registered $uuid")
      connection foreach {
        conn =>
          val messageToSend = RegisteredMessage(uuid).toJson.toString
          conn ! TextMessage.Strict(messageToSend)
      }

    case _ => // ingore
  }

  override def postStop(): Unit = connection.foreach(context.stop)
}
