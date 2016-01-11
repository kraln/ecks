# Section 3: Choosing an IRCd #

IRCds that are supported by Ecks:

| IRCd name | Ecks Version | Ecks Module Name | Type of Support |
|:----------|:-------------|:-----------------|:----------------|
| InspIRCD  | 0.6          | ecks.protocols.inspircd | Full            |
| Unreal    | 0.4          | ecks.protocols.unreal32 | Full / Experimental <sup>1</sup>|
| Bahamut <sup>2</sup> | 0.3          | ecks.protocols.ngfqircd | Full            |
| Scarlet   | 0.4          | ecks.protocols.cots | Full            |

<sup>1:</sup> Unreal support has not been tested very much, and assumes that the ircd hasn't been mucked around with much. The version of the protocol that is implemented is U2303.

<sup>2:</sup> Bahamut is defaultly set up to remove umode +r from users when they change nicknames. We have this patched out of our version for obvious reasons. Everything should still work okay, except that users won't keep their +r through nickchanges. You may wish to patch this behavior.

Your IRCd not listed here? Drop us a line, and we'll more than likely add support in the next version.

[<- Previous](EcksGuideGettingStarted.md) | [Next ->](EcksGuideInstall.md)





















