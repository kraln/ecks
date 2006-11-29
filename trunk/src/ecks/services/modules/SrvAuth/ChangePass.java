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
 * The Initial Developers of the Original Code are Copyright (C)Jeff Katz
 * <jeff@katzonline.net>.
 *
 */
package ecks.services.modules.SrvAuth;

import ecks.Configuration;
import ecks.Logging;
import ecks.Storage;
import ecks.protocols.Protocol;
import ecks.services.Service;
import ecks.services.SrvAuth;
import ecks.services.modules.CommandDesc;
import ecks.services.modules.bCommand;


public class ChangePass extends bCommand {
    public final CommandDesc Desc = new CommandDesc("changepass", 2, false, CommandDesc.access_levels.A_PENDING, "Changes your password.", "<old> <new>");

    public CommandDesc getDesc() {
        return Desc;
    }

    public void handle_command(Service who, String user, String replyto, String arguments, Protocol p, Configuration c) {
        SrvAuth temp = ((SrvAuth) who);
        String uTemp = Storage.Users.get(user).authname;
        String args[] = arguments.split(" ");

        if (args.length != 2) {
            p.PrivMessage(who, replyto, "\u0002Usage:\u0002 changepass <old> <new>");
            return;
        }

        if (temp.checkAccess(uTemp).ordinal() >= CommandDesc.access_levels.A_HELPER.ordinal())  // we're a helper
        {
            // changepass username newpassword
            if (temp.getUsers().containsKey(args[0])) {
                temp.getUsers().get(args[0]).cngpass(args[1]);
                p.PrivMessage(who, replyto, "Password Changed!");
                Logging.warn("SRVAUTH", user + " changed password for username: " + args[0]);
            } else p.PrivMessage(who, replyto, "\u0002Error:\u0002 No such username exists!");
        } else { // we're not a helper. vette old password
            // changepass oldpass newpass
            if (temp.chkpass(args[0], user)) {
                temp.getUsers().get(uTemp).cngpass(args[1]);
                p.PrivMessage(who, replyto, "Password Changed!");
            } else p.PrivMessage(who, replyto, "\u0002Error:\u0002 Old password incorrect!");
        }
    }
}
