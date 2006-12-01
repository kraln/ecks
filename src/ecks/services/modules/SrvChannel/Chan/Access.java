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

import ecks.services.modules.bCommand;
import ecks.services.modules.CommandDesc;
import ecks.services.Service;
import ecks.services.SrvChannel;
import ecks.protocols.Protocol;
import ecks.protocols.Generic;
import ecks.Configuration;
import ecks.util;
import ecks.Logging;
import ecks.Storage;

public class Access extends bCommand {
    public final CommandDesc Desc = new CommandDesc("access", 2, true, CommandDesc.access_levels.A_PENDING, "Shows access of user in a channel", "[channel] [user]");

    public CommandDesc getDesc() {
        return Desc;
    }

    public void handle_command(Service who, String user, String replyto, String arguments) {
        /*
        SrvChan: access  - get my access in this channel
        Srvchan: access john - get john's access in this channel
        SrvChan: access #somechan - get my access in somechannel
        SrvChan: access #somechan john - get john's access in somechannel
        SrvChan: access john #somechan - get john's access in somechannel
        */
        String whatchan = "";
        String whom = "";
        String args[] = arguments.split(" ");

        whatchan = replyto;
        whom = user;

        try {
            if (args.length > 0 && (!(args[0].equals("")))) { // if we have arguments
                if (args[0].startsWith("#")) { // assume channel
                    whatchan = args[0];
                    if (args.length > 1)   // if there's another argument, assume it's a user
                        whom = args[1];
                } else if ((args.length > 1) && args[1].startsWith("#")) { // assume channel
                    whatchan = args[1];
                    whom = args[0];
                } else {
                    whom = args[0];
                }

            }
        } catch (NullPointerException NPE) {
            NPE.printStackTrace();
            Logging.warn("SRVCHAN_ACCESS", "Got NPE: " + arguments);
        }

        whom = whom.toLowerCase();
        whatchan = whatchan.toLowerCase();

        if (whatchan.startsWith("#")) {
            if (((SrvChannel) who).getChannels().containsKey(whatchan)) {
                if (Generic.Users.containsKey(whom)) {
                    if (Generic.Users.get(whom).authhandle != null) {
                        String aname = Generic.Users.get(whom).authhandle;
                        if (((SrvChannel) who).getChannels().get(whatchan).getUsers().containsKey(aname)) {

                            String alevel = ((SrvChannel) who).getChannels().get(whatchan).getUsers().get(aname).toString();

                            Generic.curProtocol.outPRVMSG(who, replyto, util.pad("\u0002" + aname + "\u0002 ", 20) + alevel.substring(2));
                        } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 User has no access to channel!");
                    } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 User is not authed!");
                } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 User does not exist!");
            } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 Not a registered channel!");
        } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 Not a channel!");
    }
}
