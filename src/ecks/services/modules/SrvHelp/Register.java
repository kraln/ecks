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
package ecks.services.modules.SrvHelp;

import ecks.Logging;
import ecks.protocols.Generic;
import ecks.services.Service;
import ecks.services.SrvHelp;
import ecks.services.SrvHelp_channel;
import ecks.services.modules.CommandDesc;
import ecks.services.modules.bCommand;

public class Register extends bCommand {
    public final CommandDesc Desc = new CommandDesc("register", 2, true, CommandDesc.access_levels.A_OPER, "Registers a channel.", "<channel>");

    public CommandDesc getDesc() {
        return Desc;
    }

    public void handle_command(Service who, String user, String replyto, String arguments) {
        SrvHelp temp = ((SrvHelp) who);
        String args[] = arguments.split(" ");
        if (args.length == 1) {
            String ch = args[0].toLowerCase();
            if (!temp.getChannels().containsKey(ch)) {
                temp.getChannels().put(ch, new SrvHelp_channel(ch));
                Generic.curProtocol.outPRVMSG(who, replyto, "\u0002" + Generic.Users.get(user).uid + ":\u0002 Registration Succeeded!");
                Logging.info("SRVHELP", "Channel " + ch + " registered by " + user + ".");
                Generic.curProtocol.srvJoin(who, ch, "+stn");
                Generic.curProtocol.outSETMODE(who, ch, "+o", who.getname());
            } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 Channel is already registered.");
        } else
            Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 Invalid Arguments. Usage: register [channel]");
    }
}
