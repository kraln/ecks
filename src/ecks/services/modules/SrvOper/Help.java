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

import ecks.Configuration;
import ecks.protocols.Generic;
import ecks.services.Service;
import ecks.services.SrvAuth;
import ecks.services.modules.CommandDesc;
import ecks.services.modules.CommandModule;
import ecks.services.modules.bCommand;
import ecks.util;

import java.util.Map;

public class Help extends bCommand {
    public final CommandDesc Desc = new CommandDesc("help", 0, true, CommandDesc.access_levels.A_NONE, "Shows you help for this service.");

    public CommandDesc getDesc() {
        return Desc;
    }

    public void handle_command(Service who, String user, String replyto, String arguments) {

        Generic.curProtocol.outPRVMSG(who, user, "\u0002COMMAND     \u0002<required argument> [optional argument]");
        Generic.curProtocol.outPRVMSG(who, user, "\u0002            Command Description\u0002");
        Generic.curProtocol.outPRVMSG(who, user, "\u0002------------------------------------------------\u0002");

        for (Map.Entry<String, CommandModule> z : who.getCommands().entrySet()) {
            CommandModule cm = z.getValue();
            if (Configuration.getSvc().containsKey(Configuration.authservice)) {
                if (cm.getDesc().Required_Access.ordinal() <= ((SrvAuth) Configuration.getSvc().get(Configuration.authservice)).checkAccess(user.toLowerCase()).ordinal()) {
                    if (!cm.getName().startsWith("\u0001")) {
                        Generic.curProtocol.outPRVMSG(who, user, "\u0002" + util.pad(z.getKey(), 12) + "\u0002" + cm.getDesc().arguments);
                        Generic.curProtocol.outPRVMSG(who, user, "\u0002            " + cm.getDesc().help + "\u0002");
                    }
                }
            } else {
                if (!cm.getName().startsWith("\u0001")) {
                    Generic.curProtocol.outPRVMSG(who, user, "\u0002" + util.pad(z.getKey(), 12) + "\u0002" + cm.getDesc().arguments);
                    Generic.curProtocol.outPRVMSG(who, user, "\u0002            " + cm.getDesc().help + "\u0002");
                }
            }

        }
    }
}
