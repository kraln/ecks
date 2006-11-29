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
package ecks.services.modules.SrvOper;

import ecks.services.modules.bCommand;
import ecks.services.modules.CommandDesc;
import ecks.services.Service;
import ecks.services.SrvOper;
import ecks.protocols.Protocol;
import ecks.*;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

public class Grep extends bCommand {
    public final CommandDesc Desc = new CommandDesc("grep", 50, true, CommandDesc.access_levels.A_OPER, "Sort (java regex) and act upon data.", "[users|channels] [(uid|ident|mask)(name|topic)] [print|(kill|gline)(close)] regexp");

    public CommandDesc getDesc() {
        return Desc;
    }

    public void handle_command(Service who, String user, String replyto, String arguments, Protocol p, Configuration c) {
        String args[] = arguments.split(" ");
        // grep [users|channels] [info|kill|gline] [wildcards|mask]
        if (args.length > 3) {

            String what = args[0];
            String what2 = args[1];
            String cmd = args[2];
            String towhat = args[3];
            Boolean confirm = false;
            String reason = "";
            if (args.length == 5) confirm = args[4].equals("CONFIRM");
            if (args.length > 5) for (int i = 5; i < args.length; i++) reason = reason + args[i] + " ";
            else reason = "Matched Grep.";
            try {
            if (what.equals("users")) {
                List matches = new ArrayList<Client>();
                for (Map.Entry<String, Client> Client : Storage.Users.entrySet()) {
                    if (what2.equals("uid")) {
                        if (Client.getValue().uid.matches(towhat.replace("*", "\\S+")))
                            matches.add(Client.getValue());
                    } else if (what2.equals("mask")) {
                        if (Client.getValue().hostmask.matches(towhat.replace("*", "\\S+")))
                            matches.add(Client.getValue());
                    } else if (what2.equals("ident")) {
                        if (Client.getValue().ident.matches(towhat.replace("*", "\\S+")))
                            matches.add(Client.getValue());
                    } else {
                        p.PrivMessage(who, replyto, "\u0002Error:\u0002 Invalid field for user. Usage: grep users [uid|mask|ident] [print|kill|gline] regexp");
                        return;
                    }
                }
                if (cmd.equals("print")) {
                    p.PrivMessage(who, replyto, "\u0002Grep Results:\u0002 " + matches.size() + " matches.");
                    for (int i = 0; i < matches.size(); i++)
                        p.PrivMessage(who, replyto, "   " + matches.get(i).toString());
                } else if (cmd.equals("kill")) {
                    if (matches.size() > (Storage.Users.size() / 10))  // we're not going to kill more than 10% without confirm
                    {
                        if (confirm) {
                            for (int i = 0; i < matches.size(); i++)
                            {
                                Logging.warn("SRVOPER", user + " killed more than 10% of the network.!");
                                p.kill(who, c, ((Client) matches.get(i)).getName(), reason);
                            }
                        } else
                            p.PrivMessage(who, replyto, "\u0002Error:\u0002 Affects more than 10% of network (" + matches.size() + "/" + Storage.Users.size() + ", " + ((matches.size()/Storage.Users.size()) * 100) + "%). Confirm by adding CONFIRM to end of command.");
                    } else {
                        for (int i = 0; i < matches.size(); i++)
                            p.kill(who, c, ((Client) matches.get(i)).getName(), reason);
                    }
                } else if (cmd.equals("gline")) {
                    if (matches.size() > (Storage.Users.size() / 10))  // we're not going to akill more than 10% without confirm
                    {
                        if (confirm) {
                            for (int i = 0; i < matches.size(); i++) {
                                p.gline(who, c, ((Client) matches.get(i)), reason);
                                Logging.warn("SRVOPER", user + " managed to gline more than 10% of the network!");
                            }
                        } else
                            p.PrivMessage(who, replyto, "\u0002Error:\u0002 Affects more than 10% of network (" + matches.size() + "/" + Storage.Users.size() + ", " + ((matches.size()/Storage.Users.size()) * 100) + "%). Confirm by adding CONFIRM to end of command.");
                    } else {
                        for (int i = 0; i < matches.size(); i++)
                            p.gline(who, c, ((Client) matches.get(i)), reason);
                    }
                } else
                    p.PrivMessage(who, replyto, "\u0002Error:\u0002 Invalid action for user. Usage: grep users [uid|mask|ident] [print|kill|gline] regexp");
            } else if (what.equals("channels")) {
                List matches = new ArrayList<Channel>();
                for (Map.Entry<String, Channel> Client : Storage.Channels.entrySet()) {
                    if (what2.equals("name")) {
                        if (Client.getValue().name.matches(towhat.replace("*", "\\S+")))
                            matches.add(Client.getValue());
                    } else if (what2.equals("topic")) {
                        //todo
                        p.PrivMessage(who, replyto, "\u0002Error:\u0002 Unimplimented");
                        return;
                    } else {
                        p.PrivMessage(who, replyto, "\u0002Error:\u0002 Invalid field for channel. Usage: grep channels [name|topic] [print|close] regexp");
                        return;
                    }
                }
                if (cmd.equals("print")) {
                    p.PrivMessage(who, replyto, "\u0002Grep Results:\u0002 " + matches.size() + " matches.");
                    for (int i = 0; i < matches.size(); i++)
                        p.PrivMessage(who, replyto, "   " + matches.get(i).toString());
                } else if (cmd.equals("close")) {
                    for (int i = 0; i < matches.size(); i++)
                    {
                       for (Map.Entry<Client, String> Cl : ((Channel) matches.get(i)).clientmodes.entrySet()) {
                            p.kill(who,c,Cl.getKey().uid,"Channel #" +  ((Channel)matches.get(i)).name + " has been closed by network staff."); // kill? maybe we should just kick...
                            Logging.warn("SRVOPER", user + " closed channel " +  ((Channel)matches.get(i)).name + " !");
                       }
                    }
                } else p.PrivMessage(who, replyto, "\u0002Error:\u0002 Invalid action for channels. Usage: grep channels [name|topic] [print|close] regexp");
            } else p.PrivMessage(who, replyto, "\u0002Error:\u0002 Invalid command. Usage: grep [users|channels] [(uid|ident|mask)(name|topic)] [print|(kill|gline)(close)] regexp");
            } catch (PatternSyntaxException pse)
            {
               p.PrivMessage(who, replyto, "\u0002Error:\u0002 Bad Regex."); 
            }
        } else p.PrivMessage(who, replyto, "\u0002Error:\u0002 Invalid arguments. Usage: grep [users|channels] [(uid|ident|mask)(name|topic)] [print|(kill|gline)(close)] regexp");
    }
}