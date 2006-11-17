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

import org.w3c.dom.NodeList;

public class SrvOper extends bService {
    String name = "SrvOper";

    public void introduce() {        
        proto.Introduce(name, this);
        if(!(config.Config.get("debugchan").equals("OFF")))
        {
            proto.SJoin(name, config.Config.get("debugchan"), "+stn");
            proto.forcemode(this, config.Config.get("debugchan"), "+o", name);
        }
    }
    public void setname(String nname) { name = nname; }
    public String getSRVDB() {
        return "";
    }
    
    public void loadSRVDB(NodeList XMLin)
    {

    }

    public void handle(String user, String replyto, String command) {
        // todo: actually handle user mode updates...
        String cmd = command.split(" ")[0];
        if (config.Database.Users.get(user.toLowerCase()).modes.contains("o")) // if we're an oper
            super.handle(user.toLowerCase(), replyto.toLowerCase(), command);
        else {
            if (!cmd.toLowerCase().equals("oper")) // let people oper using us
                proto.PrivMessage(this, replyto, "\u0002Error:\u0002 You \u0002*MUST*\u0002 be an IRCop to use this service!");
            else
                super.handle(user.toLowerCase(), replyto.toLowerCase(), command);
        }
    }
}