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

import ecks.protocols.Generic;
import ecks.services.Service;
import ecks.services.SrvChannel;
import ecks.services.SrvChannel_channel;
import ecks.services.modules.CommandDesc;
import ecks.services.modules.bCommand;
import ecks.util;

import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

public class Info extends bCommand {
    public final CommandDesc Desc = new CommandDesc("info", 1, true, CommandDesc.access_levels.A_AUTHED, "Returns information about a channel");

    public CommandDesc getDesc() {
        return Desc;
    }

    public void handle_command(Service who, String user, String replyto, String arguments) {
        if (((SrvChannel) who).getChannels().containsKey(arguments.toLowerCase())) {

            SrvChannel_channel t = ((SrvChannel) who).getChannels().get(arguments.toLowerCase());
            Generic.curProtocol.outNOTICE(who, user, "\u0002Channel:\u0002 " + arguments);
            Generic.curProtocol.outNOTICE(who, user, "\u0002---------\u0002 ");

            if (t.getAllMeta().containsKey("_ts_registered"))
                Generic.curProtocol.outNOTICE(who, user, "Registered On: " + DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(new Date(Integer.parseInt(t.getMeta("_ts_registered")) * 1000L)));
            else
                Generic.curProtocol.outNOTICE(who, user, "Unknown Registration Date.");

            if (t.getAllMeta().containsKey("_ts_last"))
                Generic.curProtocol.outNOTICE(who, user, "Last had a user: " + DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(new Date(Integer.parseInt(t.getMeta("_ts_last")) * 1000L)));
            else
                Generic.curProtocol.outNOTICE(who, user, "Has never had a user.");

            if (t.getAllMeta().containsKey("staffnote"))
                Generic.curProtocol.outNOTICE(who, user, "Staff Note: " + t.getMeta("staffnote"));
            else
                Generic.curProtocol.outNOTICE(who, user, "Has no staff note.");

            if (t.getAllMeta().containsKey("_isbad"))
                Generic.curProtocol.outNOTICE(who, user, "Is a naughty, naughty channel!");

            if (t.getAllMeta().size() == 0)
                Generic.curProtocol.outNOTICE(who, user, "Has no Metadata.");
            else
                Generic.curProtocol.outNOTICE(who, user, "Channel Metadata:");
            for (Map.Entry<String, String> e : t.getAllMeta().entrySet())
                Generic.curProtocol.outNOTICE(who, user, "    " + util.pad(e.getKey(), 14) + ": " + e.getValue());


            if (t.getUsers().size() == 0)
                Generic.curProtocol.outNOTICE(who, user, "Has no users.");
            else
                Generic.curProtocol.outNOTICE(who, user, "Channel Users:");
            for (Map.Entry<String, SrvChannel_channel.ChanAccess> e : t.getUsers().entrySet())
                Generic.curProtocol.outNOTICE(who, user, "    " + util.pad(e.getKey(), 14) + ": " + e.getValue());

        } else Generic.curProtocol.outNOTICE(who, user, "\u0002Error:\u0002 No such registered channel...");
    }
}
