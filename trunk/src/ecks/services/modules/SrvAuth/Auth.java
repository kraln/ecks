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

import ecks.services.modules.bCommand;
import ecks.services.modules.CommandDesc;
import ecks.services.Service;
import ecks.services.SrvAuth;
import ecks.protocols.Protocol;
import ecks.Configuration;

public class Auth extends bCommand {
    public final CommandDesc Desc = new CommandDesc("auth", 2, false, CommandDesc.access_levels.A_NONE, "Logs you into services.", "<username> <password>");

    public CommandDesc getDesc() {
        return Desc;
    }

    public void handle_command(Service who, String user, String replyto, String arguments, Protocol p, Configuration c) {
        SrvAuth temp = ((SrvAuth) who);
        if (arguments.split(" ").length == 2) {
            if (c.getDB().Users.get(user).authname == null) {
                if (temp.getUsers().containsKey(arguments.split(" ")[0].toLowerCase())) { // if the username exists
                    if (temp.chkpass(arguments.split(" ")[1], arguments.split(" ")[0].toLowerCase())) { // password matches
                        c.getDB().Users.get(user).authname = arguments.split(" ")[0].toLowerCase();
                        p.setauthed(who,c,c.getDB().Users.get(user).uid);
                        p.Notice(who, replyto, "\u0002" + c.getDB().Users.get(user).uid + ":\u0002 Welcome back!");
                        
                    } else p.PrivMessage(who, replyto, "\u0002Error:\u0002 Invalid Password!");
                } else p.PrivMessage(who, replyto, "\u0002Error:\u0002 Username not found!");
            } else p.PrivMessage(who, replyto, "\u0002Error:\u0002 You are already logged in!");
        } else p.PrivMessage(who, replyto, "\u0002Usage:\u0002 auth [username] [password]!");

    }
}
