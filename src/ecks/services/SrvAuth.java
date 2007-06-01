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
import ecks.Threads.SrvAuth_ExpiryThread;
import ecks.protocols.Generic;
import ecks.services.modules.CommandDesc;
import ecks.util;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;

public class SrvAuth extends bService {
    public String name = "SrvAuth";
    public Map<String, SrvAuth_user> Users;
    public Map<Long, String> dbMap; // lets us lookup usernames by dbid

    public void introduce() {
        Hooks.regHook(this, Hooks.Events.E_PRIVMSG);
        Generic.srvIntroduce(this);

        // start a thread to clear expired users
        util.startThread(new Thread(new SrvAuth_ExpiryThread())).start();
        Logging.info("SRVAUTH", "Expiry thread started...");

        if (!(Configuration.Config.get("debugchan").equals("OFF")))
            Generic.srvJoin(this, Configuration.Config.get("debugchan"), "+stn");
    }

    public boolean chkpass(String pwd, String user) {
        return Users.get(user).password.equals(util.hash(pwd));
    }

    public String getname() {
        return name;
    }

    public void setname(String nname) {
        name = nname;
    }

    public CommandDesc.access_levels checkAccess(String user) {
        if (Users.containsKey(user))
            return Users.get(user).services_access;
        else
            return CommandDesc.access_levels.A_NONE;
    }

    public void handle(String user, String replyto, String command) {
        // don't let people auth or register, or change password without fqdn
        String cmd = command.split(" ")[0];

        if (Configuration.Config.get("secure").equals("NO")) {
            super.handle(user.toLowerCase(), replyto.toLowerCase(), command);
            return;
        }

        if (cmd.startsWith("FQDN"))
            super.handle(user.toLowerCase(), replyto.toLowerCase(), command);
        else {
            if (cmd.toLowerCase().equals("auth") || cmd.toLowerCase().equals("register") || cmd.toLowerCase().equals("changepass"))
                Generic.curProtocol.outPRVMSG(this, replyto, "\u0002Error:\u0002 You \u0002*MUST*\u0002 /msg " + this.getname() + "@" + Configuration.Config.get("hostname") + " " + cmd + "!");
            else
                super.handle(user.toLowerCase(), replyto.toLowerCase(), command);
        }
    }

    public SrvAuth() {
        Users = new HashMap<String, SrvAuth_user>();
        dbMap = new HashMap<Long, String>();
        Configuration.authservice = name.toLowerCase(); // I lay claim to users early, so they can reauth.
    }

    public Map<String, SrvAuth_user> getUsers() {
        return Users;
    }

    public String getSRVDB() {
        Logging.verbose("SRVAUTH", "Started gathering SrvAuth Database...");
        StringBuilder tOut = new StringBuilder("<service class=\"" + this.getClass().getName() + "\" name=\"" + name + "\">\r\n");
        HashMap<String, SrvAuth_user> X = new HashMap<String, SrvAuth_user>(Users);
        for (Map.Entry<String, SrvAuth_user> usar : X.entrySet()) {
            tOut.append("\t" + "<user>\r\n");
            tOut.append("\t\t" + "<username value=\"").append(util.encodeUTF(usar.getValue().username)).append("\"/>\r\n");
            tOut.append("\t\t" + "<password value=\"").append(util.encodeUTF(usar.getValue().password)).append("\"/>\r\n");
            tOut.append("\t\t" + "<email value=\"").append(util.encodeUTF(usar.getValue().email)).append("\"/>\r\n");
            tOut.append("\t\t" + "<access value=\"").append(usar.getValue().services_access).append("\"/>\r\n");
            tOut.append("\t\t" + "<metadata>\r\n");
            for (Map.Entry<String, String> md : usar.getValue().getAllMeta().entrySet()) {
                tOut.append("\t\t\t" + "<").append(util.encodeUTF(md.getKey())).append(" value=\"").append(util.encodeUTF(md.getValue())).append("\"/>\r\n");
            }
            tOut.append("\t\t" + "</metadata>\r\n");
            tOut.append("\t" + "</user>\r\n");
        }
        tOut.append("</service>\r\n");
        Logging.verbose("SRVAUTH", "Done gathering...");
        return tOut.toString();
    }

    public void loadSRVDB(NodeList XMLin) {
        for (int i = 0; i < XMLin.getLength(); i++) {  // user tags
            String uTemp, pTemp, eTemp;
            CommandDesc.access_levels aTemp;
            Map<String, String> mTemp = new HashMap<String, String>();

            NodeList t;

            if (XMLin.item(i).getNodeType() != 1) continue;

            t = ((Element) XMLin.item(i)).getElementsByTagName("username");
            uTemp = util.decodeUTF(t.item(0).getAttributes().getNamedItem("value").getNodeValue());
            t = ((Element) XMLin.item(i)).getElementsByTagName("password");
            pTemp = util.decodeUTF(t.item(0).getAttributes().getNamedItem("value").getNodeValue());
            t = ((Element) XMLin.item(i)).getElementsByTagName("email");
            eTemp = util.decodeUTF(t.item(0).getAttributes().getNamedItem("value").getNodeValue());
            t = ((Element) XMLin.item(i)).getElementsByTagName("access");
            aTemp = CommandDesc.access_levels.valueOf(t.item(0).getAttributes().getNamedItem("value").getNodeValue());

            t = ((Element) XMLin.item(i)).getElementsByTagName("metadata").item(0).getChildNodes();
            for (int j = 0; j < t.getLength(); j++) {
                if (t.item(j).getNodeType() != 1) continue;
                mTemp.put(util.decodeUTF((t.item(j)).getNodeName()), util.decodeUTF((t.item(j)).getAttributes().getNamedItem("value").getNodeValue()));
            }

            Users.put(uTemp.toLowerCase().trim(), new SrvAuth_user(uTemp, pTemp, eTemp, aTemp, mTemp));
            if (!Users.get(uTemp.toLowerCase().trim()).getAllMeta().containsKey("svsid")) {
                if (!dbMap.containsKey((long) i))
                    Users.get(uTemp.toLowerCase().trim()).setMeta("svsid", String.valueOf(i));
                else
                    Users.get(uTemp.toLowerCase().trim()).setMeta("svsid", String.valueOf(dbMap.size() - 1));
            }
            dbMap.put(Long.parseLong(Users.get(uTemp.toLowerCase().trim()).getMeta("svsid")), uTemp.toLowerCase().trim());
        }
        Logging.info("SRVAUTH", "Loaded " + Users.size() + " registered users from database.");
    }

    public int getcount() {
        return Users.size();
    }

    public void hookDispatch(Hooks.Events what, String source, String target, String args) {
        super.hookDispatch(this, what, source, target, args);
        switch (what) {
            default:
        }
    }


}