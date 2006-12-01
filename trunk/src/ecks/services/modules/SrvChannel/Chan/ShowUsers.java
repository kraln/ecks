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
import ecks.services.SrvChannel_channel;
import ecks.services.SrvChannel;
import ecks.protocols.Protocol;
import ecks.protocols.Generic;
import ecks.Configuration;
import ecks.util;

import java.util.Map;

public class ShowUsers extends bCommand {
    public final CommandDesc Desc = new CommandDesc("showusers", 1, true, CommandDesc.access_levels.A_PENDING, "Shows users in channel", "[channel]");

    public CommandDesc getDesc() {
        return Desc;
    }

    public void handle_command(Service who, String user, String replyto, String arguments) {
        String whatchan = "";
        if (arguments.length() > 1)
            whatchan = arguments;
        else
            whatchan = replyto;

        whatchan = whatchan.toLowerCase();

        if (whatchan.startsWith("#")) {
            if (((SrvChannel) who).getChannels().containsKey(whatchan)) {
                Generic.curProtocol.outPRVMSG(who, user, "\u0002" + util.pad("USER",12) + "\u0002 " + "ACCESS");
                Generic.curProtocol.outPRVMSG(who, user, "------------------------------");
                for (Map.Entry<String, SrvChannel_channel.ChanAccess> t : ((SrvChannel) who).getChannels().get(whatchan).getUsers().entrySet()) {
                    Generic.curProtocol.outPRVMSG(who, user, "\u0002" + util.pad(t.getKey(),12) + "\u0002 " + t.getValue().toString().substring(2));
                }
            } else Generic.curProtocol.outPRVMSG(who, user, "\u0002Error:\u0002 Not a registered channel!");
        } else Generic.curProtocol.outPRVMSG(who, user, "\u0002Error:\u0002 Not a channel!");
    }
}
