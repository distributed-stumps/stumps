package com.github.distributed_stumps.stumps.registry

import akka.actor.{ActorRef, Actor}

import com.github.distributed_stumps.stumps.message.common._
import com.github.distributed_stumps.stumps.message.provider._

/** A simple actor that acts as a central registry for registered services
  *
  * @author John Murray "me at johnmurray dot io"
  */
class ServiceRegistryActor extends Actor {

   import ServiceRegistryActor._

   val log = context.system.log

   /*
    * Map a resource to a map that maps datacenters to resources and where they are
    * located.
    *    Resource ->
    *       DC-1 -> List(Host)
    *       DC-2 -> List(Host)
    *       ...
    */
   var serviceRegistry : Map[Resource, Map[String, List[Host]]] = Map.empty

   /*
    * A set of actors that have the lookout for a set of resources
    */
   var resourceWatchers : Map[String, List[ActorRef]] = Map.empty

   def receive = {
      case Register(host, resource, _) =>
         if (serviceRegistry.get(resource).isDefined) {
            val resourceRegistry = serviceRegistry(resource)
            if (resourceRegistry.get(host.datacenter).isDefined) {
               serviceRegistry += resource -> {
                  resourceRegistry + (host.datacenter -> {
                     resourceRegistry(host.datacenter) ++ List(host)
                  })
               }
            } else {
               serviceRegistry += resource -> {
                  serviceRegistry(resource) + (host.datacenter -> List(host))
               }
            }
         } else {
            serviceRegistry += resource -> Map(host.datacenter -> List(host))
         }

         log.info(s"$serviceRegistry")

         if (resourceWatchers.get(resource.name).isDefined) {
            resourceWatchers.getOrElse(resource.name, Nil).foreach(_ ! Add(resource, host))
         }

      case Unregister(host, resource, _) =>
         log.info(s"BEFORE: $serviceRegistry")
         if (serviceRegistry.get(resource).isDefined
             && serviceRegistry(resource).get(host.datacenter).isDefined
             && serviceRegistry(resource)(host.datacenter).map(_.hostname).contains(host.hostname)) {

            val locationList = serviceRegistry(resource)(host.datacenter).filter(p => p.hostname != host.hostname)
            if (locationList.isEmpty) {
               serviceRegistry += resource -> {
                  serviceRegistry(resource) - host.datacenter
               }
            } else {
               serviceRegistry += resource -> {
                  serviceRegistry(resource) + (host.datacenter -> locationList)
               }
            }
         }
         clearEmptyResources()
         log.info(s"AFTER: $serviceRegistry")

         if (resourceWatchers.get(resource.name).isDefined) {
            resourceWatchers.getOrElse(resource.name, Nil).foreach(_ ! Remove(resource, host))
         }

      case Subscribe(resourceName) =>
         val watchers = resourceWatchers.getOrElse(resourceName, Nil)
         if (! watchers.contains(sender)) {
            resourceWatchers += resourceName -> (sender :: watchers)
         }

         // go ahead and send notifications for resources that already exist
         serviceRegistry.keys.filter(k => k.name == resourceName).foreach { resource =>
            serviceRegistry(resource).foreach { case (dc: String, hosts: List[Host]) =>
                  hosts.foreach( sender ! Add(resource, _) )
            }
         }

      case unknown =>
         unhandled(unknown)
   }


   def clearEmptyResources(): Unit = {
      val emptyResources = serviceRegistry.filter(_._2.isEmpty).keys
      emptyResources.foreach(serviceRegistry -= _)
   }

}


object ServiceRegistryActor {
   case class Subscribe(resourceName: String)
   case class Add(resource: Resource, host: Host)
   case class Remove(resource: Resource, host: Host)
}
