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

import ecks.services.Service;
import ecks.protocols.Generic;

public class cmdSay extends bCommand{
    public final CommandDesc Desc = new CommandDesc("say", 99, true, CommandDesc.access_levels.A_OPER, "Sends a message as a services agent");
    public CommandDesc getDesc() { return Desc; }
    public void handle_command(Service who, String user, String replyto, String arguments) {
        String whatchan = replyto;
        String what = arguments;

        String args[] = what.split(" ");

        if (args.length > 0 && (!(args[0].equals("")))) { // if we have arguments
            if (args[0].startsWith("#")) { // assume channel
                whatchan = args[0];
                what = what.substring(args[0].length()).trim();
            }
        }

        Generic.curProtocol.outPRVMSG(who, whatchan, what);
    }
}
