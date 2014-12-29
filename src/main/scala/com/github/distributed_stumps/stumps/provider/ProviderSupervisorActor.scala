package com.github.scalable_tech.stumps.provider

import akka.actor._

/** A simple supervisor to the "provider" heirarchy
  *
  * @author John Murray "me at johnmurray dot io"
  */
class ProviderSupervisorActor extends Actor {

   val heartbeatMonitorActor = context.actorOf(Props[HeartbeatMonitorActor], "heartbeat-monitor")
   val registrationActor = context.actorOf(Props(classOf[RegistrationActor], heartbeatMonitorActor), "registration")

   // stop for nothing!
   override def supervisorStrategy = OneForOneStrategy() {
      case _ => SupervisorStrategy.resume
   }

   def receive = {
      case unknown => unhandled(unknown)
   }

}
