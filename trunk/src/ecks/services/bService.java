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
package ecks.services;

import java.util.Map;
import java.util.HashMap;

import ecks.*;
import ecks.services.modules.CommandModule;
import ecks.services.modules.CommandDesc;
import ecks.services.modules.bCommand;
import ecks.protocols.Protocol;
import org.w3c.dom.NodeList;

public abstract class bService implements Service {
    Configuration config = null;
    Protocol proto = null;
    public Map<String, CommandModule> Commands;
    String name = "Srv";

    public bService() {
        Commands = new HashMap<String, CommandModule>();
    }

    public void Initialize(Configuration c, Protocol p, String n) {
        config = c;
        proto = p;
        name = n;
    }

    public void introduce() {
        proto.Introduce(name, this);
    }

    public String getname() {
        return name;
    }
    public abstract void setname(String nname);

    public void handle(String user, String replyto, String command) {
        boolean inchan = (!user.equals(replyto));
        command = command.trim();
        String cmd = command.split(" ")[0];

        if (cmd.startsWith("FQDN"))
        {
            cmd = cmd.substring(4); // remove the fqdn
            command = command.substring(4);
            Logging.verbose("SERVICE", "Command \"" + cmd + "\" is fully qualified.");
        }
        try {
        if (Commands.containsKey(cmd.toLowerCase())) {
            CommandDesc c = Commands.get(cmd.toLowerCase()).getDesc();
            if ((inchan && c.InChannel) || !inchan)               // inchannel check
            {
                if (command.split(" ").length <= (c.ArgCount + 1)) // too many arguments check
                {
                    if (Configuration.getSvc().containsKey(Configuration.authservice)) // if we have an authserv
                    {
                        CommandDesc.access_levels req = Commands.get(cmd.toLowerCase()).getDesc().Required_Access;
                        String handle = null;
                        if (Storage.Users.containsKey(user.toLowerCase()))
                            handle = Storage.Users.get(user.toLowerCase()).authname;
                        CommandDesc.access_levels cur = ((SrvAuth) Configuration.getSvc().get(Configuration.authservice)).checkAccess(handle);
                        if ((handle != null) || (req.ordinal() == 0)) { // we're authed, or the command doesn't care
                            if (cur.ordinal() >= req.ordinal()) { // if we have the access
                                Logging.verbose("SERVICE", "Handling command: " + cmd + ", for user: " + user + ".");
                                Commands.get(cmd.toLowerCase()).handle_command(this, user.toLowerCase(), replyto, command.substring(cmd.length()).trim(), proto, config); // run command
                            } else proto.PrivMessage(this, replyto, "\u0002Error:\u0002 Not enough access!");
                        } else proto.PrivMessage(this, replyto, "\u0002Error:\u0002 You're not authed!");
                    } else Commands.get(cmd.toLowerCase()).handle_command(this, user, replyto, command.substring(cmd.length()).trim(), proto, config); // just do it without checking levels
                } else proto.PrivMessage(this, replyto, "\u0002Error:\u0002 Too many arguments!");
            } else proto.PrivMessage(this, replyto, "\u0002Error:\u0002 That command is unavailable in-channel!");
        } else proto.PrivMessage(this, replyto, "\u0002Error:\u0002 Unknown Command!");
        } catch (NullPointerException NPE)
        {
            Logging.error("SERVICE", "Caught NPE in command.");
            Logging.info("SERVICE", "NPE handling command:" + user + " \\ " + replyto + " \\ " + command);
            NPE.printStackTrace();
        }
    }

    public void diegraceful(String message) {
        proto.die(this, message);
    }

    public void addCommand(String cmdName, CommandModule newCmd) {
        Commands.put(cmdName, newCmd);
    }

    public Map<String, CommandModule> getCommands() {
        return Commands;
    }

    public abstract String getSRVDB();

    public abstract void loadSRVDB(NodeList XMLin);
}
