package com.github.distributed_stumps.stumps.boot

import akka.actor._
import akka.kernel._

import com.github.distributed_stumps.stumps.provider.ProviderSupervisorActor
import com.github.distributed_stumps.stumps.registry.ServiceRegistryActor
import com.github.distributed_stumps.stumps.subscriber.SubscriberSupervisorActor


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
