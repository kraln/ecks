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
import ecks.protocols.Protocol;
import ecks.services.Service;
import ecks.services.SrvAuth;
import ecks.services.modules.CommandDesc;
import ecks.services.modules.bCommand;

public class Whois extends bCommand {
    public final CommandDesc Desc = new CommandDesc("whois", 1, true, CommandDesc.access_levels.A_HELPER, "Gives you information about a user");
    public CommandDesc getDesc() { return Desc; }
    public void handle_command(Service who, String user, String replyto, String arguments, Protocol p, Configuration c) {
        if (c.Database.Users.containsKey(arguments.toLowerCase())) {
            if (c.Database.Users.get(arguments.toLowerCase()).authname != null) {
                p.Notice(who, user, "\u0002User Info:\u0002 " + c.Database.Users.get(arguments.toLowerCase()));
                p.Notice(who, user, "\u0002Services Info:\u0002 " + ((SrvAuth) who).getUsers().get(c.Database.Users.get(arguments.toLowerCase()).authname));
            } else {
                p.Notice(who, user, "\u0002User Info:\u0002 " + c.Database.Users.get(arguments.toLowerCase()));
                p.Notice(who, user, "\u0002Services Info:\u0002 Not logged in.");
            }
        } else p.Notice(who, user, "\u0002Error:\u0002 No such user...");
    }
}
