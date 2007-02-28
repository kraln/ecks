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

import ecks.Configuration;
import ecks.Storage;
import ecks.util;
import ecks.protocols.Protocol;
import ecks.protocols.Generic;
import ecks.services.Service;
import ecks.services.SrvAuth;
import ecks.services.modules.CommandDesc;
import ecks.services.modules.bCommand;

import java.util.NoSuchElementException;

public class Auth extends bCommand {
    public final CommandDesc Desc = new CommandDesc("auth", 2, false, CommandDesc.access_levels.A_NONE, "Logs you into services.", "<username> <password>");

    public CommandDesc getDesc() {
        return Desc;
    }

    public void handle_command(Service who, String user, String replyto, String arguments) {
        SrvAuth temp = ((SrvAuth) who);
        if (arguments.split(" ").length == 2) {
            if (Generic.Users.get(user).authhandle == null) {
                String uname = arguments.split(" ")[0].toLowerCase();
                if (temp.getUsers().containsKey(uname)) { // if the username exists
                    if (temp.chkpass(arguments.split(" ")[1], uname)) { // password matches
                        Generic.Users.get(user).authhandle = uname;
                        ((SrvAuth) who).getUsers().get(uname).setMeta("_ts_last", util.getTS()); // update last seen metadata
                        try {
                            Generic.curProtocol.srvSetAuthed(who,Generic.Users.get(user).uid, Long.parseLong(temp.getUsers().get(uname).getMeta("svsid")));
                        } catch (NoSuchElementException nsee) {
                            temp.getUsers().get(uname).setMeta("svsid", "-1");
                            Generic.curProtocol.srvSetAuthed(who,Generic.Users.get(user).uid, (long)-1);
                        }
                        Generic.curProtocol.outNOTICE(who, replyto, "\u0002" + Generic.Users.get(user).uid + ":\u0002 Welcome back!");
                        if (Configuration.getSvc().containsKey(Configuration.chanservice))
                            Configuration.getSvc().get(Configuration.chanservice).handle(user,user,"syncall"); // up them in all of their channels
                    } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 Invalid Password!");
                } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 Username not found!");
            } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 You are already logged in!");
        } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Usage:\u0002 auth [username] [password]!");

    }
}
