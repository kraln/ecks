# Introduction #

You're probably reading this page because you were directed here by network staff for a quick overview of what you can do with your new services agent.

Great!

Read on for information on how to do the day to day channel management tasks with Ecks and SrvChannel.


# Details #

## Commands ##

SrvChannel takes commands in one of three ways:
  * /srvchan command (we refer to this one as out-of-band)
  * SrvChan: command (in-channel)
  * !command (in-channel)

Some people find that sr

&lt;tab&gt;

 is quicker than !, so which you use is entirely up to you. For the rest of this page, we're going to use the !command notation.

## Sync ##

The first thing you should probably do when a channel is registered to you is probably !sync, which as the channel owner will grant you ops (or +q on networks with this mode enabled).

```
Jun 02 01:41:01 <Qix>	!sync
Jun 02 01:41:01 ---	SrvChan gives channel operator status to Qix
```

This command is called automatically, silently, when you join a channel - only if something funky happens or you manage to deop yourself or whatever will you need to manually issue this command.

## Adding Users ##

To add additional users to your channel, you'd use the !adduser command
```
Jun 02 01:44:03 <Qix>	!adduser kuja
```

The level does not have to be specified right away (it defaults to none), but the user you're adding does need to be authenticated with the SrvAuth service.

Once you decide what access you want to grant your user, you can use the !chuser command to change their access level.

Your options for access level are: none, peon (auto-voice, +v), chanop (auto-op, +o), master, and coowner. On networks with half-ops and +qa, the breakdown is similar as would be expected.

Each channel can only have one owner (that's you)

## Remembering Topic ##

Once you set a topic in your channel, you can have SrvChan enforce the topic (so that no one can change it, and if the room becomes empty it will be re-set when someone joins)

To set up enforcement, once you have the topic you want, use the !settopic command (with no arguments). To turn off enforcement (say, if you want to change the topic), use ` !settopic * `

## Setting a Greeting ##

To set a greeting message that will be displayed to all users when they enter, use !setgreeting <The greeting you want here>. To disable this greeting, use ` !setgreeting * `

## Setting Personal Greeting ##

To set a greeting message that will be displayed in the channel when you join, use !setinfo 

&lt;greeting&gt;

. ` !setinfo * ` clears.


