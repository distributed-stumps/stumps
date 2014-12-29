package com.github.distributed_stumps.stumps.provider

import akka.actor._

import com.github.distributed_stumps.stumps.message.provider.{Unregister, Register}
import com.github.distributed_stumps.stumps.registry.ServiceRegistryActor

import scala.concurrent.duration._

/** Makes sure things are alive. Notifies the [[ServiceRegistryActor]] when something is
  * dead (determined dead based on some function of time / errors).
  *
  * @author John Murray "me at johnmurray dot io"
  */
class HeartbeatMonitorActor extends Actor {

   import HeartbeatMonitorActor._

   implicit val ec = context.system.dispatcher
   val log = context.system.log
   val serviceRegistryActor = context.actorSelection("/user/registry")

   var watched : Map[ActorRef, Register] = Map.empty


   override def preStart(): Unit = {
      self ! Connect
   }

   def receive = {
      case Connect =>
         log.info(s"Attempting to connect to $serviceRegistryActor")
         serviceRegistryActor ! Identify(self)

      case ActorIdentity(`self`, Some(ref)) =>
         log.info(s"Connected to $ref")
         context.become(active(ref))

      case ActorIdentity(`self`, None) =>
         log.warning("Could not connect to resource listener actor. Reconnecting in 5 seconds")
         context.system.scheduler.scheduleOnce(5.seconds, self, Connect)

      case unknown => unhandled(unknown)
   }

   def active(serviceRegistry: ActorRef): Actor.Receive = {
      case r @ Register(host, resource, heartbeatListener) =>
         watched += heartbeatListener -> r

         // akka takes care of heartbeart monitoring. We're just connecting to an actor to
         // make sure the system doesn't go down. (there is probably a better way)
         log.debug(s"Logging death watch with $heartbeatListener")
         context.watch(heartbeatListener)

      case Terminated(ref) =>
         if (watched.get(ref).isDefined) {
            log.info(s"Received termination event for $ref")
            val reg = watched(ref)
            serviceRegistry ! Unregister(reg.host, reg.resource, reg.heartBeatListener)
            watched -= ref
         } else {
            log.error(s"Received termination event for $ref, but was not being watched")
         }
      case unknown =>
         unhandled(unknown)
   }

}


object HeartbeatMonitorActor {
   case object Connect
}
