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

import ecks.services.modules.bCommand;
import ecks.services.modules.CommandDesc;
import ecks.services.Service;
import ecks.protocols.Protocol;
import ecks.protocols.Generic;
import ecks.Configuration;
import ecks.Logging;

import java.io.IOException;

public class SetLogLevel extends bCommand {
    public final CommandDesc Desc = new CommandDesc("setloglevel", 1, true, CommandDesc.access_levels.A_SRA, "Changes the current logging level", "<new level>");

    public CommandDesc getDesc() {
        return Desc;
    }

    public void handle_command(Service who, String user, String replyto, String arguments) {
        try {
            Logging.myLogLevel = Logging.loglevels.valueOf(arguments);
            Logging.warn("SRVOPER", user + " changed logging level!");
            Logging.info("SRVOPER", "New level is :" + Logging.myLogLevel);
            Generic.curProtocol.outPRVMSG(who, replyto, "Log level changed.");
        } catch (Exception e) {
            Generic.curProtocol.outPRVMSG(who, replyto, "Log levelchange Failed! " + e.getMessage());
            Logging.error("SRVOPER", user + " tried to change the logging level, and it failed miserably.");
        }
    }
}
