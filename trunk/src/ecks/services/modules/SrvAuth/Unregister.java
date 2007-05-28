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
package ecks.services.modules.SrvAuth;

import ecks.Configuration;
import ecks.Logging;
import ecks.protocols.Generic;
import ecks.services.Service;
import ecks.services.SrvAuth;
import ecks.services.SrvChannel;
import ecks.services.SrvChannel_channel;
import ecks.services.modules.CommandDesc;
import ecks.services.modules.bCommand;
import ecks.util;

import java.util.Map;

public class Unregister extends bCommand {
    public final CommandDesc Desc = new CommandDesc("unregister", 1, true, CommandDesc.access_levels.A_HELPER, "Unregisters an account");

    public CommandDesc getDesc() {
        return Desc;
    }

    public void handle_command(Service who, String user, String replyto, String arguments) {
        SrvAuth temp = ((SrvAuth) who);
        String args[] = arguments.split(" ");
        String tU = args[0].toLowerCase();
        if (args.length == 1) {
            if (util.sanitize(tU)) {
                if (temp.getUsers().containsKey(tU)) {
                    if ((temp.getUsers().get(Generic.Users.get(user).authhandle)).getAccess().ordinal() > (temp.getUsers().get(tU).getAccess().ordinal())) // we can only delete people lower than us
                    {
                        for (String e : temp.getUsers().get(tU).WhereAccess.keySet()) {
                            if (((SrvChannel) Configuration.getSvc().get(Configuration.chanservice)).getChannels().get(e).getUsers().get(tU) == SrvChannel_channel.ChanAccess.C_OWNER) {   // we have a problem, this person owns the channel
                                boolean promoted = false;
                                int threshold = SrvChannel_channel.ChanAccess.C_OWNER.ordinal();
                                while (!promoted) {
                                    threshold--;
                                    if (threshold < SrvChannel_channel.ChanAccess.C_PEON.ordinal()) {   // no suitable replacement found.
                                        // remove channel
                                        ((SrvChannel) Configuration.getSvc().get(Configuration.chanservice)).getChannels().remove(e); // drop the channel
                                        Generic.srvPart(who, e, "Channel Unregistered (owner unregistered, no other users).");
                                        Logging.info("SRVCHAN", "Channel " + e + " unregistered by virtue of having no users left.");
                                        promoted = true;
                                        break;
                                    }
                                    for (Map.Entry<String, SrvChannel_channel.ChanAccess> z : (((SrvChannel) Configuration.getSvc().get(Configuration.chanservice)).getChannels().get(e).getUsers().entrySet())) {
                                        // iterate looking for replacement
                                        if (z.getValue().ordinal() >= threshold) {
                                            ((SrvChannel) Configuration.getSvc().get(Configuration.chanservice)).getChannels().get(e).getUsers().remove(Generic.Users.get(user).authhandle);
                                            ((SrvChannel) Configuration.getSvc().get(Configuration.chanservice)).getChannels().get(e).getUsers().put(Generic.Users.get(user).authhandle, SrvChannel_channel.ChanAccess.C_OWNER);
                                            promoted = true;
                                            break;
                                        }
                                    }
                                }
                            }
                            ((SrvChannel) Configuration.getSvc().get(Configuration.chanservice)).getChannels().get(e).getUsers().remove(tU); // remove access from each channel
                        }

                        temp.getUsers().remove(tU); // drop the account
                        Generic.curProtocol.outMODE(who, Generic.Users.get(tU), "-r", "");
                        // Generic.Users.get(tU).authhandle = null; // user is no longer authed
                        Generic.curProtocol.outPRVMSG(who, replyto, "User account removed.");
                    } else
                        Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 User has equal/higher access than you!");
                } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 No such username is registered");
            } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 Invalid username.");
        } else
            Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 Invalid Arguments. Usage: unregister [username]");
    }
}