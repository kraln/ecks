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

public class Sync extends bCommand {
    public final CommandDesc Desc = new CommandDesc("sync", 1, true, CommandDesc.access_levels.A_NONE, "Synchronizes a user's modes with their access.", "[channel]");

    public CommandDesc getDesc() {
        return Desc;
    }

    public void handle_command(Service who, String user, String replyto, String arguments) {
        String whatchan = "";
        String whom = "";
        String args[] = arguments.split(" ");

        boolean silent = false;

        whatchan = replyto;
        whom = user;

        if (args.length > 0 && (!(args[0].equals("")))) { // if we have arguments
            if (args[0].startsWith("#")) { // assume channel
                whatchan = args[0];
            }
            if (args[0].equals("silent"))
                silent = true;
        }

        whom = whom.toLowerCase();
        whatchan = whatchan.toLowerCase(); // srvchan only knows about lowercase versions of channels...

        if (whatchan.startsWith("#")) {
            if (((SrvChannel) who).getChannels().containsKey(whatchan)) {
                if (Generic.Users.get(whom).authhandle != null) {
                    String aname = Generic.Users.get(whom).authhandle;
                    if (((SrvChannel) who).getChannels().get(whatchan).getUsers().containsKey(aname)) {
                        SrvChannel_channel.ChanAccess alevel = ((SrvChannel) who).getChannels().get(whatchan).getUsers().get(aname);
                        String newmode = "+";
                        if (alevel.ordinal() >= SrvChannel_channel.ChanAccess.C_PEON.ordinal())
                            newmode = "+v";
                        if (alevel.ordinal() >= SrvChannel_channel.ChanAccess.C_CHANOP.ordinal())
                            newmode = "+o";
                        Generic.curProtocol.outSETMODE(who,whatchan,newmode, whom);
                        if (((SrvChannel) who).getChannels().get(whatchan).getAllMeta().containsKey("setinfo-" + aname)) // if they have a SetInfo
                            Generic.curProtocol.outPRVMSG(who, whatchan, "\u0002[" + whom + "]\u0002: " + ((SrvChannel) who).getChannels().get(whatchan).getMeta("setinfo-" + aname));
                    } else if(!silent) Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 User has no access to channel!");
                } else if(!silent) Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 User is not authed!");
            } else if(!silent) Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 Not a registered channel!");
        } else if(!silent) Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 Not a channel!");
    }
}
