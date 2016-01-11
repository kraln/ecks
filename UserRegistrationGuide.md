# Introduction #

To make use of the services that your network has to offer, you must first register with the username service, generally named SrvAuth.

# Usage #

Most networks will have an aliased command that sends directly to SrvAuth. You should try to avoid sending SrvAuth privmsgs directly, so in the following examples we assume that your network has /SrvAuth and /Auth aliases setup properly.

## Registration ##

The first thing you should do in order to use the network resources would be to register a username with SrvAuth. The command is called register, and is formatted thusly:

` /SrvAuth Register [username you want] [password you can remember] [your email address] `

For instance,

` /SrvAuth Register TestUsername MyPassW3rD test@asdf.com `

You should recieve an email at your email address within five minutes. If you don't, you may need to see a network helper for assistance.

## Confirmation ##

You should have recieved your network welcome email. In it, there is a 'cookie' which you need to confirm your email address with SrvAuth. You **must** confirm your email before you can use any of the extended features of services. To confirm your email address,

` /SrvAuth confirm [Cookie] `

For instance,

` /SrvAuth confirm zmgwRkIUu0 `

After your account is successfully confirmed, you should be all set.

## Authentication ##

Every time you connect to the network, you'll need to "log in" to SrvAuth with your username and password. To do this, you can /SrvAuth Auth Username Password, or usually just /Auth Username Password. Our test user would use the following:

` /Auth TestUsername MyPassW3rD `

After you authenticate, you are automatically 'sync'ed in each channel you are currently in or about to join.
