package com.github.scalable_tech.stumps.provider

import akka.actor.{ActorRef, Actor}

import com.github.scalable_tech.stumps.message.provider.{Registered, Register}

/** Simple actor to register resources with. Once registered, a watch is setup with
  ( the heartbeat monitor actor.
  *
  * @author John Murray "me at johnmurray dot io"
  */
class RegistrationActor(heartbeatMonitor: ActorRef) extends Actor {

   val log = context.system.log
   val serviceRegistryActor = context.actorSelection("/user/registry")

   def receive = {
      case r: Register =>
         log.debug(s"Registering: $r")
         heartbeatMonitor ! r
         serviceRegistryActor ! r
         sender ! Registered
      case unknown =>
         log.warning(s"Unknown message received: $unknown")
         unhandled(unknown)
   }

}
