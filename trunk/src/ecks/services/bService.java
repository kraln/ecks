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

import ecks.Configuration;
import ecks.Hooks.Hooks;
import ecks.Logging;
import ecks.protocols.Generic;
import ecks.services.modules.CommandDesc;
import ecks.services.modules.CommandModule;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;

public abstract class bService implements Service {
    public Map<String, CommandModule> Commands;

    public bService() {
        Commands = new HashMap<String, CommandModule>();
    }

    public void introduce() {
        Generic.srvIntroduce(this);
    }

    public abstract void setname(String nname);

    public void handle(String user, String replyto, String command) {
        boolean inchan = (!user.equals(replyto));
        command = command.trim();
        String cmd = command.split(" ")[0];

        if (cmd.startsWith("FQDN")) {
            cmd = cmd.substring(4); // remove the fqdn
            command = command.substring(4);
            Logging.verbose("SERVICE", "Command \"" + cmd + "\" is fully qualified.");
        }
        try {

            // Do we have this command?
            if (!Commands.containsKey(cmd.toLowerCase())) {
                Generic.curProtocol.outPRVMSG(this, replyto, "\u0002Error:\u0002 Unknown Command!");
                return;
            }

            CommandDesc c = Commands.get(cmd.toLowerCase()).getDesc();
            // Is this command available in-channel?
            if ((!inchan || !c.InChannel) && inchan) {
                Generic.curProtocol.outPRVMSG(this, replyto, "\u0002Error:\u0002 That command is unavailable in-channel!");
                return;
            }

            // Do we have too many arguments?
            if (command.split(" ").length > (c.ArgCount + 1)) {
                // todo: accept commands with no limit (add to desc?)
                Generic.curProtocol.outPRVMSG(this, replyto, "\u0002Error:\u0002 Too many arguments!");
                return;
            }

            // Is there a service that provides access levels?
            if (Configuration.getSvc().containsKey(Configuration.authservice)) {

                CommandDesc.access_levels req = Commands.get(cmd.toLowerCase()).getDesc().Required_Access;
                String handle = Generic.Users.get(user.toLowerCase()).authhandle;

/*  // I think at this point if there is a nonexistant user, we have bigger issues
                if (Generic.Users.containsKey(user.toLowerCase()))
*/

                CommandDesc.access_levels cur = ((SrvAuth) Configuration.getSvc().get(Configuration.authservice)).checkAccess(handle);
                // Do we need any access to use this command?
                if ((handle == null) && (req.ordinal() != 0)) {
                    Generic.curProtocol.outPRVMSG(this, replyto, "\u0002Error:\u0002 You're not authed!");
                    return;
                }

                // Do we have enough access?
                if (cur.ordinal() < req.ordinal()) {
                    Generic.curProtocol.outPRVMSG(this, replyto, "\u0002Error:\u0002 Not enough access!");
                    return;
                }

                // All checks passed, you are clear for takeoff
                Logging.verbose("SERVICE", "Handling command: " + cmd + ", for user: " + user + ".");
                Commands.get(cmd.toLowerCase()).handle_command(this, user.toLowerCase(), replyto, command.substring(cmd.length()).trim()); // run command


            } else {
                // there isn't, just pass the command
                Commands.get(cmd.toLowerCase()).handle_command(this, user, replyto, command.substring(cmd.length()).trim());
            }

        } catch (NullPointerException NPE) {
            // God damnit, what broke?
            Logging.error("SERVICE", "Caught NPE in command.");
            Logging.info("SERVICE", "NPE handling command:" + user + " \\ " + replyto + " \\ " + command);
            NPE.printStackTrace();
        }
    }

    public void die(String message) {
        Generic.srvDie(message);
    }

    public void addCommand(String cmdName, CommandModule newCmd) {
        Commands.put(cmdName, newCmd);
    }

    public Map<String, CommandModule> getCommands() {
        return Commands;
    }

    public abstract String getSRVDB();

    public abstract void loadSRVDB(NodeList XMLin);

    public void hookDispatch(Service which, Hooks.Events what, String source, String target, String args) {
        switch (what) {
            case E_PRIVMSG:
                // todo: fix this up so it's not such a steaming pile of crap
                String target2 = "", arguments = "";

                if (target.startsWith("#")) { // is a channel message
                    if (args.split(" ")[0].endsWith(":")) { // someone is addressing something. ie blah:
                        target2 = args.split(" ")[0];
                        target2 = target2.substring(0, target2.length() - 1);
                        arguments = args.substring(target2.length() + 1).trim();
                    }
                } else { // is a private message
                    target2 = target;
                    arguments = args;
                }

                // hack hack hack
                String temp[] = target2.split("@");
                target2 = temp[0];

                if (temp.length > 1)
                    arguments = "FQDN" + arguments;
                if (target2.toLowerCase().equals(which.getname().toLowerCase()))
                    which.handle(
                            source,
                            (target.startsWith("#") ? target : source),
                            arguments
                    );

                break;
            default:
        }
    }

}
