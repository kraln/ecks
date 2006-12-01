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
import ecks.services.SrvChannel_channel;
import ecks.protocols.Protocol;
import ecks.protocols.Generic;
import ecks.Configuration;
import ecks.Logging;
import ecks.util;
import ecks.Storage;

public class Kick extends bCommand {
    public final CommandDesc Desc = new CommandDesc("kick", 99, true, CommandDesc.access_levels.A_AUTHED, "Kicks a user from a channel", "[channel] <user> [reason]");

    public CommandDesc getDesc() {
        return Desc;
    }

    public void handle_command(Service who, String user, String replyto, String arguments) {
        /*
        Srvchan: kick john [why]
        SrvChan: kick #somechan john [why]
        SrvChan: kick john #somechan [why]
        */
        String whatchan = "";
        String whom = "";
        String reason = "Kicked.";
        String args[] = arguments.split(" ");

        whatchan = replyto;
        whom = user;

        try {
            if (args.length > 0 && (!(args[0].equals("")))) { // if we have arguments
                if (args[0].startsWith("#")) { // assume channel
                    whatchan = args[0];
                    if (args.length > 1)   // if there's another argument, assume it's a user
                        whom = args[1];
                    if (args.length > 2)
                        reason = arguments.substring(args[0].length() + args[1].length() + 1);
                } else if ((args.length > 1) && args[1].startsWith("#")) { // assume channel
                    whatchan = args[1];
                    whom = args[0];
                    if (args.length > 2)
                        reason = arguments.substring(args[0].length() + args[1].length() + 1);
                } else {
                    whom = args[0];
                    reason = arguments.substring(args[0].length());
                }

            }
        } catch (NullPointerException NPE) {
            NPE.printStackTrace();
            Logging.warn("SRVCHAN_KICK", "Got NPE: " + arguments);
        }

        whom = whom.toLowerCase();
        whatchan = whatchan.toLowerCase();

        if (whatchan.startsWith("#")) {
            if (((SrvChannel) who).getChannels().containsKey(whatchan)) {
                if (Generic.Users.containsKey(whom)) {
                    if (Configuration.getSvc().containsKey(whom))
                    {
                        Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 Users should not play with fire. (You cannot kick network services)");
                        return;
                    }
                    if (Generic.Users.get(user).authhandle != null) {
                        String mname = Generic.Users.get(user).authhandle;
                        if (((SrvChannel) who).getChannels().get(whatchan).getUsers().containsKey(mname)) {
                            SrvChannel_channel.ChanAccess mlevel = ((SrvChannel) who).getChannels().get(whatchan).getUsers().get(mname);
                            if (mlevel.ordinal() >= SrvChannel_channel.ChanAccess.C_CHANOP.ordinal()) {
                                if (Generic.Users.get(whom).authhandle != null) {
                                    String aname = Generic.Users.get(whom).authhandle;
                                    if (((SrvChannel) who).getChannels().get(whatchan).getUsers().containsKey(aname)) {
                                        SrvChannel_channel.ChanAccess alevel = ((SrvChannel) who).getChannels().get(whatchan).getUsers().get(aname);
                                        if (mlevel.ordinal() > alevel.ordinal()) {
                                            Generic.curProtocol.outKICK(who, whom, whatchan, reason);
                                        } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 User has access equal to or more than yours!");
                                    } else Generic.curProtocol.outKICK(who, whom, whatchan, reason);
                                } else Generic.curProtocol.outKICK(who, whom, whatchan, reason);
                            } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 You do not have sufficient access to perform that command");
                        } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 You have no access to this channel");
                    } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 You are not authed!");
                } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 User does not exist!");
            } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 Not a registered channel!");
        } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 Not a channel!");
    }
}
