/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is Ecks, also known as "SrvEcks" or Ecks Services.
 *
 * The Initial Developer of the Original Code is Copyright (C)Jeff Katz
 * <jeff@katzonline.net>. All Rights Reserved.
 *
 */
package ecks.services.modules.SrvChannel.Chan;

import ecks.Configuration;
import ecks.protocols.Generic;
import ecks.services.Service;
import ecks.services.SrvAuth;
import ecks.services.SrvChannel;
import ecks.services.SrvChannel_channel;
import ecks.services.modules.CommandDesc;
import ecks.services.modules.bCommand;

public class AddUser extends bCommand {
    public final CommandDesc Desc = new CommandDesc("adduser", 3, true, CommandDesc.access_levels.A_AUTHED, "Adds a user to a channel. Access is one of [none|peon|chanop|master|coowner].", "<user> [channel] [access]");

    public CommandDesc getDesc() {
        return Desc;
    }

    public void handle_command(Service who, String user, String replyto, String arguments) {
        /*
            adduser jeff
            adduser jeff access
            adduser #chan jeff
            adduser jeff #chan
            adduser #chan jeff access
            adduser jeff #chan access
        */

        String whatchan = "";
        String whom = "";
        String args[] = arguments.split(" ");
        SrvChannel_channel.ChanAccess newacc;
        boolean silent = false;

        if (replyto.startsWith("#")) whatchan = replyto;
        whom = "";
        newacc = SrvChannel_channel.ChanAccess.C_NONE;

        if (args.length > 0 && (!(args[0].equals("")))) { // if we have arguments

            if (args.length == 1) {
                whom = args[0];
            } else if (args.length == 2) {
                if (args[0].startsWith("#")) {
                    whom = args[1];
                    whatchan = args[0];
                } else if (args[1].startsWith("#")) {
                    whom = args[0];
                    whatchan = args[1];
                } else {
                    whom = args[0];
                    try {
                        newacc = SrvChannel_channel.ChanAccess.valueOf("C_" + args[1].toUpperCase());
                    } catch (IllegalArgumentException iae) {
                        Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 Invalid level. Valid levels are: NONE, PEON, CHANOP, MASTER, COOWNER, OWNER");
                        return;
                    }
                }
            } else if (args.length == 3) {
                if (args[0].startsWith("#")) {
                    whom = args[1];
                    whatchan = args[0];
                    try {
                        newacc = SrvChannel_channel.ChanAccess.valueOf("C_" + args[2].toUpperCase());
                    } catch (IllegalArgumentException iae) {
                        Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 Invalid level. Valid levels are: NONE, PEON, CHANOP, MASTER, COOWNER, OWNER");
                        return;
                    }
                } else if (args[1].startsWith("#")) {
                    whom = args[0];
                    whatchan = args[1];
                    try {
                        newacc = SrvChannel_channel.ChanAccess.valueOf("C_" + args[2].toUpperCase());
                    } catch (IllegalArgumentException iae) {
                        Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 Invalid level. Valid levels are: NONE, PEON, CHANOP, MASTER, COOWNER, OWNER");
                        return;
                    }
                } else {
                    Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 Invalid arguments.");
                    return;
                }
            }
        }

        whom = whom.toLowerCase();
        whatchan = whatchan.toLowerCase();


        if (whatchan.startsWith("#")) {
            if (((SrvChannel) who).getChannels().containsKey(whatchan)) {
                if (Generic.Users.containsKey(whom)) {
                    if (Generic.Users.get(whom).authhandle != null) {
                        if (Generic.Users.get(user).authhandle != null) {
                            if (!Generic.Users.get(user).authhandle.equals(Generic.Users.get(whom).authhandle)) {
                                String aname = Generic.Users.get(user).authhandle;
                                String bname = Generic.Users.get(whom).authhandle;
                                if (((SrvChannel) who).getChannels().get(whatchan).getUsers().containsKey(aname)) {
                                    if (!((SrvChannel) who).getChannels().get(whatchan).getUsers().containsKey(bname)) {
                                        SrvChannel_channel.ChanAccess alevel = ((SrvChannel) who).getChannels().get(whatchan).getUsers().get(aname);
                                        if (newacc.ordinal() < alevel.ordinal()) {
                                            ((SrvChannel) who).getChannels().get(whatchan).getUsers().put(Generic.Users.get(whom).authhandle, newacc);
                                            ((SrvAuth) Configuration.getSvc().get(Configuration.authservice)).getUsers().get(Generic.Users.get(whom).authhandle).WhereAccess.put(whatchan, newacc.toString());
                                            Generic.curProtocol.outPRVMSG(who, replyto, "User Added!");
                                        } else
                                            Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 You cannot grant a user higher access than yourself!");
                                    } else
                                        Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 You have no access to channel!");
                                } else
                                    Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 User already exists (use chuser)!");
                            } else
                                Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 You cannot add yourself!");
                        } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 You are not authed!");
                    } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 User is not authed!");
                } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 No such user!");
            } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 Not a registered channel!");
        } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 Not a channel!");
    }
}
