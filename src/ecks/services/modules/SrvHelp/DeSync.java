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

import ecks.services.modules.bCommand;
import ecks.services.modules.CommandDesc;
import ecks.services.Service;
import ecks.services.SrvHelp;
import ecks.protocols.Protocol;
import ecks.protocols.Generic;
import ecks.Configuration;
import ecks.util;

public class DeSync extends bCommand {
    public final CommandDesc Desc = new CommandDesc("desync", 1, true, CommandDesc.access_levels.A_NONE, "Abandons a help request", "[channel]");

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
        whatchan = whatchan.toLowerCase();

        if (whatchan.startsWith("#")) {
            if (((SrvHelp) who).getChannels().containsKey(whatchan)) {
                if (((SrvHelp) who).getChannels().get(whatchan).queue.contains(Generic.Users.get(whom)))
                {
                    Generic.curProtocol.outPRVMSG(who, whom, "Your help request has been abandoned.");
                    ((SrvHelp) who).getChannels().get(whatchan).queue.remove(Generic.Users.get(whom));
                }
            } else if(!silent) Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 Not a registered channel!");
        } else if(!silent) Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 Not a channel!");
    }
}
