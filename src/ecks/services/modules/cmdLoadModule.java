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

import ecks.protocols.Protocol;
import ecks.Configuration;
import ecks.services.Service;

public class cmdLoadModule extends bCommand{
    public final CommandDesc Desc = new CommandDesc("loadmod", 1, true, CommandDesc.access_levels.A_SRA, "Load a module.");
    public CommandDesc getDesc() { return Desc; }
    public void handle_command(Service who, String user, String replyto, String arguments, Protocol p, Configuration c) {
        try {
            who.addCommand(((CommandModule) Class.forName(arguments).newInstance()).getName().toLowerCase(), (CommandModule) Class.forName(arguments).newInstance());
            p.PrivMessage(who, replyto, "\u0002Loading:\u0002 Success!");
        } catch (ClassCastException e) {
            e.printStackTrace();
            p.PrivMessage(who, replyto, "\u0002Error:\u0002 That's not one of my modules!");            
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            p.PrivMessage(who, replyto, "\u0002Error:\u0002 Module not found.");
        }
    }
}
