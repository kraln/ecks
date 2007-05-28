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

import ecks.protocols.Generic;
import ecks.services.Service;
import ecks.services.modules.CommandDesc;
import ecks.services.modules.bCommand;

public class Oper extends bCommand {
    public final CommandDesc Desc = new CommandDesc("oper", 0, true, CommandDesc.access_levels.A_OPER, "Gives you +o", "");

    public CommandDesc getDesc() {
        return Desc;
    }

    public void handle_command(Service who, String user, String replyto, String arguments) {
        Generic.curProtocol.outMODE(who, Generic.Users.get(user.toLowerCase()), "+o", "");
    }
}
