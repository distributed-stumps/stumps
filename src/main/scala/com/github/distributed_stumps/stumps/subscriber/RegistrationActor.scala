package com.github.distributed_stumps.stumps.subscriber

import akka.actor._

import com.github.distributed_stumps.stumps.message.common._
import com.github.distributed_stumps.stumps.message.subscriber._
import com.github.distributed_stumps.stumps.registry.ServiceRegistryActor

import scala.concurrent.duration._

/** Register to listen for changes / availability in resources
  *
  * @author John Murray "me at johnmurray dot io"
  */
class RegistrationActor extends Actor {
   
   import RegistrationActor._

   implicit val ec = context.system.dispatcher
   val log = context.system.log
   val serviceRegistryActor = context.actorSelection("/user/registry")

   override def preStart(): Unit = {
      self ! Connect
   }

   /**
    * Connects to the registry, switches to [[active]] once connected
    */
   def receive = {
      case Connect =>
         serviceRegistryActor ! Identify(self)
      case ActorIdentity(`self`, Some(ref)) =>
         context.become(active(ref))
         context.watch(ref)
      case ActorIdentity(`self`, None) =>
         log.error("Could not find registry. Retrying in 2 seconds")
         context.system.scheduler.scheduleOnce(2.seconds, self, Connect)
      case unknown =>
         log.warning(s"Received unknown message: $unknown")
         unhandled(unknown)
   }


   /**
    * Maps:
    *    ResourceName--DC -> List[ActorRef]
    */
   var subscriptions : Map[String, List[ActorRef]] = Map.empty


   /**
    * handles registering subscribers to the registry to watch for updates
    */
   def active(registry: ActorRef): Actor.Receive = {
      // register resource-listener with registry
      case Register(resourceName, datacenter) =>
         log.info(s"Registering $resourceName for $datacenter with $sender")
         updateSubscription(resourceName, datacenter, sender)
         registry ! ServiceRegistryActor.Subscribe(resourceName)
         sender ! Registered

      // push resource addition to client
      case ServiceRegistryActor.Add(resource, host) =>
         log.info(s"Adding resource $resource for $host")
         val dcSpecificKey = subscriptionKey(resource.name, Some(host.datacenter))
         val allDcKey = subscriptionKey(resource.name, None)
         subscriptions.getOrElse(dcSpecificKey, Nil).foreach(_! Add(ResourceIdentifier(host.datacenter, resource)))
         subscriptions.getOrElse(allDcKey, Nil).foreach(_ ! Add(ResourceIdentifier(host.datacenter, resource)))

      // push resource subtraction to client
      case ServiceRegistryActor.Remove(resource, host) =>
         log.info(s"Removing resource $resource for $host")
         val dcSpecificKey = subscriptionKey(resource.name, Some(host.datacenter))
         val allDcKey = subscriptionKey(resource.name, None)
         subscriptions.getOrElse(dcSpecificKey, Nil).foreach(_! Remove(ResourceIdentifier(host.datacenter, resource)))
         subscriptions.getOrElse(allDcKey, Nil).foreach(_ ! Remove(ResourceIdentifier(host.datacenter, resource)))

      // re-connect to registry
      case Terminated(`registry`) =>
         log.error("Registry actor terminated. Attempting to re-connect")
         context.become(receive)
         self ! Connect

      case unknown =>
         log.warning(s"Received unknown message: $unknown")
         unhandled(unknown)
   }
   
   
   def updateSubscription(resourceName: String, datacenter: Option[String], ref: ActorRef) : Unit = {
      val key = subscriptionKey(resourceName, datacenter)
      subscriptions += key -> (ref :: subscriptions.getOrElse(key, Nil))
   }

   def subscriptionKey(resourceName: String, datacenter: Option[String]): String = {
      (resourceName + datacenter.map("--" + _).getOrElse("")).toLowerCase
   }

}

object RegistrationActor {
   case object Connect
}
