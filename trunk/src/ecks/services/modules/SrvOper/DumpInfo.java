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
package ecks.services.modules.SrvOper;

import ecks.Utility.Channel;
import ecks.Utility.Client;
import ecks.protocols.Generic;
import ecks.services.Service;
import ecks.services.modules.CommandDesc;
import ecks.services.modules.bCommand;

import java.util.Map;

public class DumpInfo extends bCommand {
    public final CommandDesc Desc = new CommandDesc("dumpinfo", 0, true, CommandDesc.access_levels.A_SRA, "Dumps the services's view of the network (users and channels). FLOOD WARNING!");

    public CommandDesc getDesc() {
        return Desc;
    }

    public void handle_command(Service who, String user, String replyto, String arguments) {
        Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Users:\u0002");
        for (Map.Entry<String, Client> t : Generic.Users.entrySet()) {
            Generic.curProtocol.outPRVMSG(who, replyto, "  " + t.toString());
        }
        Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Channels:\u0002");
        for (Map.Entry<String, Channel> t : Generic.Channels.entrySet()) {
            Generic.curProtocol.outPRVMSG(who, replyto, "  " + t.toString());
        }
    }
}
