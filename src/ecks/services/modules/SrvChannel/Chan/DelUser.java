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
package ecks.services.modules.SrvChannel.Chan;

import ecks.services.modules.bCommand;
import ecks.services.modules.CommandDesc;
import ecks.services.Service;
import ecks.services.SrvChannel_channel;
import ecks.services.SrvChannel;
import ecks.protocols.Protocol;
import ecks.Configuration;

public class DelUser extends bCommand {
    public final CommandDesc Desc = new CommandDesc("deluser", 2, true, CommandDesc.access_levels.A_AUTHED, "Removes a user from a channel", "<user> [channel]");

    public CommandDesc getDesc() {
        return Desc;
    }

    public void handle_command(Service who, String user, String replyto, String arguments, Protocol p, Configuration c) {
        /*
            deluser jeff
            deluser #chan jeff
            deluser jeff #chan
        */

        String whatchan = "";
        String whom;
        String args[] = arguments.split(" ");

        if (replyto.startsWith("#")) whatchan = replyto;
        whom = "";

        if (args.length > 0 && (!(args[0].equals("")))) { // if we have arguments

            if (args.length == 1) {
                whom = args[0];
            } else if (args.length == 2) {
                if (args[0].startsWith("#")) {
                    whom = args[1];
                    whatchan = args[0];
                } else if (args[1].startsWith("#")) {
                    whom = args[0];
                    whatchan = args[1];
                } else {
                    p.PrivMessage(who, replyto, "\u0002Error:\u0002 Must specify a channel!");
                    return;
                }
            }
        }

        whom = whom.toLowerCase();
        whatchan = whatchan.toLowerCase();

        if (whatchan.startsWith("#")) {
            if (((SrvChannel) who).getChannels().containsKey(whatchan)) {
                if (c.getDB().Users.containsKey(whom)) {
                    if (c.getDB().Users.get(whom).authname != null) {
                        if (c.getDB().Users.get(user).authname != null) {
                            String aname = c.getDB().Users.get(user).authname;
                            if (((SrvChannel) who).getChannels().get(whatchan).getUsers().containsKey(aname)) {
                                if (((SrvChannel) who).getChannels().get(whatchan).getUsers().containsKey(c.getDB().Users.get(whom).authname)) {
                                    SrvChannel_channel.ChanAccess alevel = ((SrvChannel) who).getChannels().get(whatchan).getUsers().get(aname);
                                    SrvChannel_channel.ChanAccess blevel = ((SrvChannel) who).getChannels().get(whatchan).getUsers().get(c.getDB().Users.get(whom).authname);
                                    if (blevel.ordinal() < alevel.ordinal()) {
                                        ((SrvChannel) who).getChannels().get(whatchan).getUsers().remove(c.getDB().Users.get(whom).authname);
                                        p.PrivMessage(who, replyto, "User Removed!");
                                    } else p.PrivMessage(who, replyto, "\u0002Error:\u0002 You cannot remove a user of higher access than yourself!");
                                } else p.PrivMessage(who, replyto, "\u0002Error:\u0002 User already has no access to channel!");
                            } else p.PrivMessage(who, replyto, "\u0002Error:\u0002 You have no access to channel!");
                        } else p.PrivMessage(who, replyto, "\u0002Error:\u0002 You are not authed!");
                    } else p.PrivMessage(who, replyto, "\u0002Error:\u0002 User is not authed!");
                } else p.PrivMessage(who, replyto, "\u0002Error:\u0002 User does not exist!");
            } else p.PrivMessage(who, replyto, "\u0002Error:\u0002 Not a registered channel!");
        } else p.PrivMessage(who, replyto, "\u0002Error:\u0002 Not a channel!");
    }
}
