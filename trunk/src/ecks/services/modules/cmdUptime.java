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

    long started = Long.valueOf(util.getTS()) - Generic.curProtocol.getWhenStarted();
    long days =  started / 86400;
    long hours = (started - (days * 86400)) / 3600;
    long minutes = (started - (days * 86400) - (hours * 3600)) / 60 ;

    String uptime = ((days>0)?days + " day" + (days>1?"s":""):"") + ((hours>0)?", " + hours + " hour"+(hours>1?"s ":""):"") + ((minutes>0)?((days>0 || hours>0)?"and ":"") + minutes + " minute" + (minutes>1?"s":""):"");
        if (uptime.equals(""))
        {
            Generic.curProtocol.outPRVMSG(who, replyto, "\u0002" + Generic.Users.get(user).uid + ":\u0002 Ecks " + util.getVersion() + " has been up for " + started + " second"+(started!=1?"s":"")+", with " + util.getThreads().size() + " threads using " + ((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1024) + "kb of memory");
        } else {
            Generic.curProtocol.outPRVMSG(who, replyto, "\u0002" + Generic.Users.get(user).uid + ":\u0002 Ecks " + util.getVersion() + " has been up for " + uptime + ", with " + util.getThreads().size() + " threads using " + ((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1024) + "kb of memory");
        }
    }
}
