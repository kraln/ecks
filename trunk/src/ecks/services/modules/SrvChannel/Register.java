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
package ecks.services.modules.SrvChannel;

import ecks.services.modules.bCommand;
import ecks.services.modules.CommandDesc;
import ecks.services.Service;
import ecks.services.SrvChannel;
import ecks.services.SrvChannel_channel;
import ecks.protocols.Protocol;
import ecks.Configuration;
import ecks.util;
import ecks.Logging;

public class Register extends bCommand {
    public final CommandDesc Desc = new CommandDesc("register", 2, true, CommandDesc.access_levels.A_HELPER, "Registers a channel.", "<channel> <user>");

    public CommandDesc getDesc() {
        return Desc;
    }

    public void handle_command(Service who, String user, String replyto, String arguments, Protocol p, Configuration c) {
        SrvChannel temp = ((SrvChannel) who);
        String args[] = arguments.split(" ");
        if (args.length == 2) {
            if (c.getDB().Users.containsKey(args[1].toLowerCase())) {
                if (c.getDB().Users.get(args[1].toLowerCase()).authname != null) {
                    String u = c.getDB().Users.get(args[1].toLowerCase()).authname;
                    String ch = args[0].toLowerCase();
                    if (!temp.getChannels().containsKey(ch)) {
                        temp.getChannels().put(ch, new SrvChannel_channel(ch, u));
                        temp.getChannels().get(ch).getUsers().put(u, SrvChannel_channel.ChanAccess.C_OWNER);
                        p.PrivMessage(who, replyto, "\u0002" + c.getDB().Users.get(user).uid + ":\u0002 Registration Succeeded!");
                        Logging.info("SRVCHAN", "Channel " + ch + " registered by " + user + " to " +  u +  ".");
                        p.SJoin(who.getname(), ch, "+stn");
                        p.forcemode(who, ch, "+o", who.getname());
                        
                    } else p.PrivMessage(who, replyto, "\u0002Error:\u0002 Channel is already registered.");
                } else p.PrivMessage(who, replyto, "\u0002Error:\u0002 Owner to-be is not logged in!");
            } else p.PrivMessage(who, replyto, "\u0002Error:\u0002 No such user is online!");
        } else p.PrivMessage(who, replyto, "\u0002Error:\u0002 Invalid Arguments. Usage: register [channel] [user]");
    }
}
