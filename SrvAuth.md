# Introduction #

SrvAuth provides user authentication for the other services.

# Usage Example #

> SomeGui registers a username
```
/msg srvauth register someusername sekreit lame@email.tld
-SrvAuth- SomeGui: Registration Succeeded!
```
> email is sent to lame@email.tld
```
/msg srvauth confirm A3Co89IXoz
-SrvAuth- SomeGui: Your account has been confirmed!
```
> someusername is now a confirmed user
```
/msg srvauth whoami
-SrvAuth- User Info: SomeGui: svsid: 0, +Rr ~SumGui pool-80-191-131-45.washdc.fios.verizon.net 
(pool-80-191-131-45.washdc.fios.verizon.net) on :nethack.GamesNET.net. 
In 3 channels: #ecks #debug #staff
-SrvAuth- Services Info: someusername lame@email.tld A_AUTHED
```

# Modules #

| **MODULE** | **ACCESS LEVEL** | **DESCRIPTION**|
|:-----------|:-----------------|:---------------|
| SrvAuth\DumpUsers | SRA              | Dumps registered users to privmsg |
| SrvAuth\Confirm | PENDING          | Verifies account email. |
| SrvAuth\Unregister | HELPER           | Unregisters an account |
| SrvAuth\Info | AUTHED           | Returns information about a handle |
| SrvAuth\Promote | HELPER           | Sets user services access. |
| SrvAuth\ReAuth | NONE             | Re-logs user into services after split (INTERNAL COMMAND) |
| SrvAuth\WhoAmi | NONE             | Gives user information about themself, including services info |
| SrvAuth\Auth | NONE             | Logs user into services. |
| SrvAuth\Whois | HELPER           | Gives user information about another user |
| SrvAuth\ChangePass | PENDING          | Changes user password. |
| SrvAuth\Register | NONE             | Registers account with services|





