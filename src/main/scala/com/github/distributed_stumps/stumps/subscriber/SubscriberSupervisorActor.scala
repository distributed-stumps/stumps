package com.github.scalable_tech.stumps.subscriber

import akka.actor.{Props, Actor, SupervisorStrategy, OneForOneStrategy}

/** A supervisor actor to take care of the "subscriber" heirarchy
  *
  * @author John Murray "me at johnmurray dot io"
  */
class SubscriberSupervisorActor extends Actor {

   val registrationActor = context.actorOf(Props[RegistrationActor], "registration")

   // stop for nothing!
   override def supervisorStrategy = OneForOneStrategy() {
      case _ => SupervisorStrategy.resume
   }

   def receive = {
      case unknown => unhandled(unknown)
   }

}
