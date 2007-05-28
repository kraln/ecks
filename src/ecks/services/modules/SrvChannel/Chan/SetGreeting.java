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

import ecks.protocols.Generic;
import ecks.services.Service;
import ecks.services.SrvChannel;
import ecks.services.SrvChannel_channel;
import ecks.services.modules.CommandDesc;
import ecks.services.modules.bCommand;

public class SetGreeting extends bCommand {
    public final CommandDesc Desc = new CommandDesc("setgreeting", 99, true, CommandDesc.access_levels.A_AUTHED, "Sets a short message that all users will be greeted with when entering a channel", "[channel] [message]");

    public CommandDesc getDesc() {
        return Desc;
    }

    public void handle_command(Service who, String user, String replyto, String arguments) {
        /*
        SrvChan: setinfo *
        Srvchan: setinfo #chan *
        SrvChan: setinfo something
        SrvChan: setinfo #chan something
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
                what = arguments.substring(whatchan.length() + 1);
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
                                if (!what.trim().equals("*")) {
                                    ((SrvChannel) who).getChannels().get(whatchan).setMeta("greeting", what);
                                    Generic.curProtocol.outPRVMSG(who, replyto, "Greeting Set.");
                                } else {
                                    ((SrvChannel) who).getChannels().get(whatchan).rmMeta("greeting");
                                    Generic.curProtocol.outPRVMSG(who, replyto, "Greeting Cleared.");
                                }
                            } else
                                Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 Must be master or greater to set greeting!");
                        } else
                            Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 User has no access to channel!");
                    } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 User is not authed!");
                } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 User does not exist!");
            } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 Not a registered channel!");
        } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 Not a channel!");
    }
}
