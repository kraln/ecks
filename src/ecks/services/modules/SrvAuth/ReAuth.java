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
import ecks.services.SrvAuth_user;
import ecks.services.modules.CommandDesc;
import ecks.services.modules.bCommand;

public class ReAuth extends bCommand {
    public final CommandDesc Desc = new CommandDesc("reauth", 2, true, CommandDesc.access_levels.A_NONE, "Re-logs you into services after split", "<dbid>, <ts>");

    public CommandDesc getDesc() {
        return Desc;
    }

    public void handle_command(Service who, String user, String replyto, String arguments) {
        SrvAuth temp = ((SrvAuth) who);
        String[] args = arguments.split(" ");

        Logging.verbose("SRVREAUTH", "Attempting reauth!");
        if (!replyto.equals("service")) {
            Generic.curProtocol.outPRVMSG(who, user, "\u0002Error:\u0002 This command is for internal use by services only!");
            return;
        }
        if (args.length == 2) {
            Logging.verbose("SRVREAUTH", "Args Correct!");
            if (Generic.Users.get(user).authhandle == null) { // not logged in (shouldn't be)
                Logging.verbose("SRVREAUTH", "Not logged in!");
                if (Math.abs(Generic.Users.get(user).svsid) == Long.parseLong(args[0])) { // if there is a svsid
                    Logging.verbose("SRVREAUTH", "Has correct svsid! " + args[0]);
                    String uname = temp.dbMap.get(Long.parseLong(args[0]));
                    if (temp.Users.containsKey(uname)) { // if uname associated with svsid is valid
                        Logging.verbose("SRVREAUTH", "Uname associated with svsid! " + uname);
                        SrvAuth_user t = temp.Users.get(uname);
                        Logging.verbose("SRVREAUTH", "Uname has svsid! " + t.getMeta("svsid"));
                        if (t.getMeta("svsid").equals(args[0])) // if the username thinks it has the same svsid as the svsid thinks it has username
                        {
                            Logging.verbose("SRVREAUTH", "Should auth!");
                            Generic.Users.get(user).authhandle = uname;

//todo: fill a queue
//                            Generic.curProtocol.outNOTICE(who, user, "\u0002" + Generic.Users.get(user).uid + ":\u0002 Welcome back! (auto logged-in as " + uname + ")");
//                            if (Configuration.getSvc().containsKey(Configuration.chanservice))
//                                Configuration.getSvc().get(Configuration.chanservice).handle(user, user, "syncall"); // sync them in all of their channels
                        } else {
                            // mismatch for whatever reason. Unset authed
                            Generic.curProtocol.srvUnSetAuthed(who, Generic.Users.get(user).uid);
                        }
                    }
                }
            }
        }
    }
}
