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
package ecks.services.modules.SrvAuth;

import ecks.Logging;
import ecks.protocols.Generic;
import ecks.services.Service;
import ecks.services.SrvAuth;
import ecks.services.modules.CommandDesc;
import ecks.services.modules.bCommand;
import ecks.util;

public class Promote extends bCommand {
    public final CommandDesc Desc = new CommandDesc("promote", 2, true, CommandDesc.access_levels.A_HELPER, "Gives user services access.");

    public CommandDesc getDesc() {
        return Desc;
    }

    public void handle_command(Service who, String user, String replyto, String arguments) {
        SrvAuth temp = ((SrvAuth) who);
        String args[] = arguments.split(" ");
        String tU = args[0].toLowerCase();
        CommandDesc.access_levels tA;
        try {
            tA = CommandDesc.access_levels.valueOf(args[1]);
        } catch (IllegalArgumentException e) {
            Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 Invalid Access Level");
            return;
        }
        if (args.length == 2) {
            if (util.sanitize(tU)) {
                if (temp.getUsers().containsKey(tU)) {
                    if ((temp.getUsers().get(Generic.Users.get(user).authhandle)).getAccess().ordinal() > (temp.getUsers().get(tU).getAccess().ordinal() + 1)) { // we can only promote who are below us by two
                        if ((temp.getUsers().get(Generic.Users.get(user).authhandle)).getAccess().ordinal() > (tA.ordinal())) {
                            temp.getUsers().get(tU).update(tA); // update the account
                            Generic.curProtocol.outPRVMSG(who, replyto, "User account promoted to " + tA + ".");
                        } else
                        if ((temp.getUsers().get(Generic.Users.get(user).authhandle)).getAccess().equals(CommandDesc.access_levels.A_SRA)) { // if we're an SRA, we can do whatever we damn well please.
                            temp.getUsers().get(tU).update(tA); // update the account
                            Generic.curProtocol.outPRVMSG(who, replyto, "User account promoted to " + tA + ".");
                            Logging.info("SRVAUTH", "Account " + tU + " promoted by " + user + " to " + tA + ".");
                        } else
                            Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 Cannot promote users to your access level.");
                    } else
                        Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 User has access that is unpromotable from you!");
                } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 No such username is registered");
            } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 Invalid username.");
        } else
            Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 Invalid Arguments. Usage: promote [username] [access]");
    }
}
