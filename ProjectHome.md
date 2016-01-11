Ecks services are a collection of bots and agents designed to make an IRC network easy to manage, maintain, and use. They are being developed specifically for GamesNET, which is running InspIRCd and the latest stable version of Ecks.

Currently:

  * SrvChan, SrvAuth, SrvOper, SrvSentinel, SrvHelp are implemented, and there is the ability to support many more...

  * Support for Unreal, InspIRCD, Bahamut, and Scarlet [IRCd protocols](EcksGuideIRCDGuide.md). Additional support by request...

  * Run on Windows, Linux, FreeBSD, OSX - any platform that has a java vm.

  * The services load their configuration from an XML file, and load/save their databases to another XML file, though the database storage is modular and could easily be extended to any variety of storage method (flatfile, sql).

  * Version 0.6 has been tested on a network with over 82,000 clients. If your network is larger than 82,000 clients (that's you QuakeNET, IRCnet, and UnderNET), we'll figure something out ;)

Grab a package and give em a try! Discussion is in #ecks on irc.gamesnet.net. If you have use of Ecks, please, please drop me a line!

> -Jeff 'Kuja' Katz


```
Total Physical Source Lines of Code (SLOC)                = 6,539
Development Effort Estimate, Person-Years (Person-Months) = 1.44 (17.24)
 (Basic COCOMO model, Person-Months = 2.4 * (KSLOC**1.05))
Schedule Estimate, Years (Months)                         = 0.61 (7.38)
 (Basic COCOMO model, Months = 2.5 * (person-months**0.38))
Estimated Average Number of Developers (Effort/Schedule)  = 2.34
Total Estimated Cost to Develop                           = $ 194,057
 (average salary = $56,286/year, overhead = 2.40).

generated using David A. Wheeler's 'SLOCCount'.
```