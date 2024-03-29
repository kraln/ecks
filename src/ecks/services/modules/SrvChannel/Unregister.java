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
package ecks.services.modules.SrvChannel;

import ecks.Logging;
import ecks.protocols.Generic;
import ecks.services.Service;
import ecks.services.SrvChannel;
import ecks.services.modules.CommandDesc;
import ecks.services.modules.bCommand;

public class Unregister extends bCommand {
    public final CommandDesc Desc = new CommandDesc("unregister", 1, true, CommandDesc.access_levels.A_HELPER, "Unregisters a channel", "<channel>");

    public CommandDesc getDesc() {
        return Desc;
    }

    public void handle_command(Service who, String user, String replyto, String arguments) {
        // todo: make this check channel for any 'protected' metadata
        SrvChannel temp = ((SrvChannel) who);
        String args[] = arguments.split(" ");
        String tU = args[0].toLowerCase();
        if (args.length == 1) {
            if (temp.getChannels().containsKey(tU)) {
                temp.getChannels().remove(tU); // drop the channel
                Generic.srvPart(who, tU, "Channel Unregistered.");
                Logging.info("SRVCHAN", "Channel " + tU + " unregistered by " + user + ".");
                Generic.curProtocol.outPRVMSG(who, replyto, "Channel removed.");
            } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 No such channel is registered");
        } else
            Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 Invalid Arguments. Usage: unregister [username]");

    }
}
