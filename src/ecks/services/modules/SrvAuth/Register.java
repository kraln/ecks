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

import ecks.protocols.Protocol;
import ecks.Configuration;
import ecks.util;
import ecks.services.Service;
import ecks.services.SrvAuth;
import ecks.services.SrvAuth_user;
import ecks.services.modules.CommandDesc;
import ecks.services.modules.bCommand;

public class Register extends bCommand {
    public final CommandDesc Desc = new CommandDesc("register", 3, false, CommandDesc.access_levels.A_NONE, "Registers your account with services", "<username> <password> <email address>");

    public CommandDesc getDesc() {
        return Desc;
    }

    public void handle_command(Service who, String user, String replyto, String arguments, Protocol p, Configuration c) {
        SrvAuth temp = ((SrvAuth) who);
        String args[] = arguments.split(" ");
        if (args.length == 3) {
            if (c.getDB().Users.get(user).authname == null) {
                String u = args[0].toLowerCase();
                if (util.sanitize(u)) {
                    if (!temp.getUsers().containsKey(u)) {
                        String pw = args[1];
                        if (util.sanitize(pw)) {
                            pw = util.hash(pw);
                            String e = args[2];
                            if (util.checkemail(e)) {
                                if (!(temp.getUsers().size()==0))
                                {
                                    temp.getUsers().put(u, new SrvAuth_user(u, pw, e, CommandDesc.access_levels.A_PENDING));
                                    c.getDB().Users.get(user).authname = u.trim().toLowerCase(); // user is now authed
                                    String tCookie = util.makeCookie();
                                    temp.getUsers().get(u).setMeta("cookie", tCookie);
                                    util.SendRegMail(e, tCookie);
                                    p.PrivMessage(who, replyto, "\u0002" + c.getDB().Users.get(user).uid + ":\u0002 Registration Succeeded!");
                                } else { // first registration is an SRA
                                    temp.getUsers().put(u, new SrvAuth_user(u, pw, e, CommandDesc.access_levels.A_SRA));
                                    c.getDB().Users.get(user).authname = u.trim().toLowerCase(); // user is now authed
                                    p.PrivMessage(who, replyto, "\u0002" + c.getDB().Users.get(user).uid + ":\u0002 Registration Succeeded! You are now an SRA!");
                                }
                            } else p.PrivMessage(who, replyto, "\u0002Error:\u0002 Invalid Email Address");
                        } else p.PrivMessage(who, replyto, "\u0002Error:\u0002 Password contains invalid characters");
                    } else p.PrivMessage(who, replyto, "\u0002Error:\u0002 Username is already registered.");
                } else p.PrivMessage(who, replyto, "\u0002Error:\u0002 Username contains invalid characters");
            } else p.PrivMessage(who, replyto, "\u0002Error:\u0002 You are already logged in!");
        } else p.PrivMessage(who, replyto, "\u0002Error:\u0002 Invalid Arguments. Usage: register [username] [password] [email]");
    }
}
