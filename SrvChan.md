# SrvChan #

Channel Services Agent. Provides channel registration and user access list management.

# Modules #

| **MODULE** | **ACCESS LEVEL** | **DESCRIPTION**|
|:-----------|:-----------------|:---------------|
| SrvChannel\Chan\Access.java    | PENDING          | Shows the channel access level of a user |
| SrvChannel\Chan\ShowUsers.java     |  PENDING         | Shows all the users who have access to the channel   |
| SrvChannel\Chan\ChUser.java    |  AUTHED          | Changes a user's channel access level    |
| SrvChannel\Chan\Help.java    |  NONE            | Shows all the commands you have access to |
| SrvChannel\Chan\SetInfo.java    | AUTHED           | Sets a short message that you will be greeted with when entering the channel   |
| SrvChannel\Chan\SyncChan.java    | AUTHED           | Syncronizes channel users with access levels   |
| SrvChannel\Chan\AddUser.java    |  AUTHED          | Adds channel access to a user   |
| SrvChannel\Chan\DelUser.java    |  AUTHED          | Removes channel access from a user   |
| SrvChannel\Chan\SyncAll.java    |  NONE            | Synchronizes user in all channels with corresponding access levels   |
| SrvChannel\Chan\Kick.java    |  AUTHED          | Kicks a user from a channel   |
| SrvChannel\Chan\Sync.java    |   NONE           | Synchronizes user with corresponding access level   |
| SrvChannel\Chan\Invite.java    |  AUTHED          | Invites a user to a channel    |
| SrvChannel\Chan\SetGreeting.java    |  AUTHED          | Sets a short message that all users will be greeted with upon joining channel   |
| SrvChannel\Chan\Info.java    |  AUTHED          | Displays services information about a channel   |
| SrvChannel\Chan\DeSync.java    | NONE             | Drops all modes from user in channel   |
| SrvChannel\Chan\SetModes.java    |  AUTHED          |  Sets the current channel modes as the modes to be enforced   |
| SrvChannel\Chan\SetTopic.java    | AUTHED           | Sets the current topic as the topic to be enforced   |
| SrvChannel\Unregister.java    |  HELPER          |  Unregister a channel   |
| SrvChannel\Register.java    | HELPER           | Register a channel    |
| SrvChannel\DumpChans.java    |   SRA            |  Lists all registered channels  |
