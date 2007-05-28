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
import ecks.protocols.Generic;
import org.w3c.dom.NodeList;

public class SrvOper extends bService {
    public String name = "SrvOper";

    public void introduce() {
        Configuration.logservice = name.toLowerCase(); // I lay claim to logging
        Generic.srvIntroduce(this);
        Hooks.regHook(this, Hooks.Events.E_PRIVMSG);
        if (!(Configuration.Config.get("debugchan").equals("OFF"))) {
            Generic.curProtocol.srvJoin(this, Configuration.Config.get("debugchan"), "+stn");
            Generic.curProtocol.outSETMODE(this, Configuration.Config.get("debugchan"), "+o", name);
        }
    }

    public String getname() {
        return name;
    }

    public void setname(String nname) {
        name = nname;
    }

    public String getSRVDB() {
        return "";
    }

    public void loadSRVDB(NodeList XMLin) {
        // I don't have a database yet
    }

    public void handle(String user, String replyto, String command) {
        String cmd = command.split(" ")[0];
        if (Generic.Users.get(user.toLowerCase()).modes.contains("o")) // if we're an oper
            super.handle(user.toLowerCase(), replyto.toLowerCase(), command);
        else {
            if (!cmd.toLowerCase().equals("oper")) // let people oper using us
                Generic.curProtocol.outPRVMSG(this, replyto, "\u0002Error:\u0002 You \u0002*MUST*\u0002 be an IRCop to use this service!");
            else
                super.handle(user.toLowerCase(), replyto.toLowerCase(), command);
        }
    }

    public int getcount() {
        return -1; // return ideally the number of opers online...
    }

    public void hookDispatch(Hooks.Events what, String source, String target, String args) {
        super.hookDispatch(this, what, source, target, args);
        switch (what) {
            default:
        }
    }

}