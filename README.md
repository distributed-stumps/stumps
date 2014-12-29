stumps
======

A service for resource registration and discovery build on Akka and made for Akka
and other JVM applications.


Motivation
----------

In large scala systems, you need a way to locate resources that your application needs
to consume. This can be done withing configurations up to a point. There is a limit where
the number of dependent services and the number of servers that those servies run on
makes configuration management a bit of a nightmare. There are other solutions such as
load-balancers and DNS based solutions like [GSLB][2], but those have their limits as well
and can be difficult at time to work with and/or around. 

Stumps is a service aimed at solving this problem and providng a solution that is flexible
to needs at this scale. Stumps runs as a resource registration service which client
applications can talk to. They can then implement their own load-balancing solutions within
the application, removing the need for both DNS and load balancers. This can be made
simpler with the use of [stumps-client][1] (not yet existent).


Current State
-------------

This application was born out of a hackathon and hasn't had much work invested into
it since then. The plan is to rework things to not be so messy and then write a
bunch of tests. Once this part is done, if you'd like to contirubte let me know
or just send me a pull request. Until this initial re-work is done however I will
not be taking pull requests.






  [1]: https://github.com/distributed-stumps/stumps-client
  [2]: http://www.eukhost.com/kb/global-server-load-balancing/
