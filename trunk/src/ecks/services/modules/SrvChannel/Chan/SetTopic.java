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
import ecks.protocols.Generic;

public class SetTopic extends bCommand {
    public final CommandDesc Desc = new CommandDesc("settopic", 2, true, CommandDesc.access_levels.A_AUTHED, "Enforces the current channel topic.", "[channel]");

    public CommandDesc getDesc() {
        return Desc;
    }

    public void handle_command(Service who, String user, String replyto, String arguments) {
        /*
        SrvChan: settopic *
        Srvchan: settopic #chan *
        SrvChan: settopic
        SrvChan: settopic #chan 
        */
        String whatchan = "";
        String whom = "";
        String args[] = arguments.split(" ");
        String what = "";
        whatchan = replyto;
        whom = user;

        if (args.length > 0 && (!(args[0].equals(""))))  // if we have arguments
            if (args[0].startsWith("#")) { // assume channel
                whatchan = args[0];
                what = arguments.substring(whatchan.length()+1);
            } else {
                what = arguments;
            }



        whom = whom.toLowerCase();
        whatchan = whatchan.toLowerCase();

        if (whatchan.startsWith("#")) {
            if (((SrvChannel) who).getChannels().containsKey(whatchan)) {
                if (Generic.Users.containsKey(whom)) {
                    if (Generic.Users.get(whom).authhandle != null) {
                        String aname = Generic.Users.get(whom).authhandle;
                        if (((SrvChannel) who).getChannels().get(whatchan).getUsers().containsKey(aname)) {
                            if (((SrvChannel) who).getChannels().get(whatchan).getUsers().get(aname).ordinal() >= SrvChannel_channel.ChanAccess.C_MASTER.ordinal()) {
                                if (!what.trim().equals("*"))
                                {
                                    ((SrvChannel) who).getChannels().get(whatchan).setMeta("enftopic", Generic.Channels.get(whatchan).topic);
                                    Generic.curProtocol.outPRVMSG(who, replyto, "Enforced Topic Set.");
                                } else {
                                    ((SrvChannel) who).getChannels().get(whatchan).rmMeta("enftopic");
                                    Generic.curProtocol.outPRVMSG(who, replyto, "Enforced Topic Cleared.");
                                }
                            } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 Must be master or greater to set topic!");
                        } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 User has no access to channel!");
                    } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 User is not authed!");
                } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 User does not exist!");
            } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 Not a registered channel!");
        } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 Not a channel!");
    }
}
