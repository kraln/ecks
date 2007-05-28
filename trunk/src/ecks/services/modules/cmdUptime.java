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
package ecks.services.modules;

import ecks.protocols.Generic;
import ecks.services.Service;
import ecks.util;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class cmdUptime extends bCommand {
    public final CommandDesc Desc = new CommandDesc("uptime", 0, true, CommandDesc.access_levels.A_NONE, "Shows services uptime");

    public CommandDesc getDesc() {
        return Desc;
    }

    public void handle_command(Service who, String user, String replyto, String arguments) {
    DateFormat mySimpleDateFormat = new SimpleDateFormat( "HH:MM SS" );
        Generic.curProtocol.outPRVMSG(who, replyto, "\u0002" + Generic.Users.get(user).uid + ":\u0002 Ecks " + util.getVersion() + " has been up for " + mySimpleDateFormat.format( new Date((Long.valueOf(util.getTS()) - Generic.curProtocol.getWhenStarted()) * 1000 )) + ", with " + util.getThreads().size() + " threads");
    }
}
