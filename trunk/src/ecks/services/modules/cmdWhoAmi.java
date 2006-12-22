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
package ecks.services.modules;

import ecks.protocols.Protocol;
import ecks.protocols.Generic;
import ecks.Configuration;
import ecks.services.Service;

public class cmdWhoAmi extends bCommand{
    public final CommandDesc Desc = new CommandDesc("whoami", 0, true, CommandDesc.access_levels.A_NONE, "Gives you information about yourself");
    public CommandDesc getDesc() { return Desc; }
    public void handle_command(Service who, String user, String replyto, String arguments) {
        if (Generic.Users.containsKey(user.toLowerCase()))
                    Generic.curProtocol.outNOTICE(who, user, "\u0002Info:\u0002 " + Generic.Users.get(user.toLowerCase()));
                else
                    Generic.curProtocol.outNOTICE(who, user, "\u0002Error:\u0002 You don't exist! (this is a bug)");
    }
}