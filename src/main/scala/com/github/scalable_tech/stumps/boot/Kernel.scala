package com.github.scalable_tech.stumps.boot

import akka.actor._
import akka.kernel._

import com.github.scalable_tech.stumps.provider.ProviderSupervisorActor
import com.github.scalable_tech.stumps.registry.ServiceRegistryActor
import com.github.scalable_tech.stumps.subscriber.SubscriberSupervisorActor


class Kernel extends Bootable {

   val system = ActorSystem("stumps");

   def startup = {
      // val providerSupervisor   = system.actorOf(Props[ProviderSupervisorActor],   "provider")
      // val subscriberSupervisor = system.actorOf(Props[SubscriberSupervisorActor], "subscriber")
      // val registry             = system.actorOf(Props[ServiceRegistryActor],      "registry")
   }

   def shutdown = {
      system.shutdown()
   }
   
}
