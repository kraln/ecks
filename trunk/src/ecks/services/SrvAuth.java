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

import ecks.services.modules.CommandDesc;
import ecks.util;
import ecks.Logging;

import java.util.Map;
import java.util.HashMap;

import org.w3c.dom.*;

public class  SrvAuth extends bService {
    String name = "SrvAuth";
    public Map<String, SrvAuth_user> Users;

    public void introduce() {
        config.authservice = name.toLowerCase(); // I lay claim to users
        proto.Introduce(name, this);
        if(!(config.Config.get("debugchan").equals("OFF")))
            proto.SJoin(name, config.Config.get("debugchan"), "+stn");
    }

    public boolean chkpass(String pwd, String user) { return Users.get(user).password.equals(util.hash(pwd));  }
    public void setname(String nname) { name = nname; }
    public CommandDesc.access_levels checkAccess(String user)
    {
        if (Users.containsKey(user))
            return Users.get(user).services_access;
        else
            return CommandDesc.access_levels.A_NONE;
    }

    public void handle(String user, String replyto, String command) {
        // don't let people auth or register, or change password without fqdn
        String cmd = command.split(" ")[0];
        if (cmd.startsWith("FQDN"))
            super.handle(user.toLowerCase(), replyto.toLowerCase(), command);
        else {
            if (cmd.toLowerCase().equals("auth") || cmd.toLowerCase().equals("register") || cmd.toLowerCase().equals("changepass"))
                proto.PrivMessage(this, replyto, "\u0002Error:\u0002 You \u0002*MUST*\u0002 /msg " + this.getname() + "@" + this.config.Config.get("hostname") + " " + cmd + "!");
            else
                super.handle(user.toLowerCase(), replyto.toLowerCase(), command);
        }
    }

    public SrvAuth() {
        Users = new HashMap<String, SrvAuth_user>();
    }

    public Map<String, SrvAuth_user> getUsers()
    {
        return Users;
    }

    public String getSRVDB() {
        String tOut="";
        tOut = tOut+ "<service class=\"" + this.getClass().getName() + "\" name=\"" + name + "\">\r\n";
        for (Map.Entry<String, SrvAuth_user> usar : Users.entrySet()) {
            tOut = tOut + "\t" + "<user>\r\n";
            tOut = tOut + "\t\t" +"<username value=\"" + util.encodeUTF(usar.getValue().username) + "\"/>\r\n";
            tOut = tOut + "\t\t" +"<password value=\"" +util.encodeUTF(usar.getValue().password) + "\"/>\r\n";
            tOut = tOut + "\t\t" +"<email value=\"" + util.encodeUTF(usar.getValue().email) + "\"/>\r\n";
            tOut = tOut + "\t\t" +"<access value=\"" + usar.getValue().services_access + "\"/>\r\n";
            tOut = tOut + "\t\t" +"<metadata>\r\n";
            for (Map.Entry<String, String> md : usar.getValue().getAllMeta().entrySet()) {
                tOut = tOut + "\t\t\t" +"<" + util.encodeUTF(md.getKey()) + " value=\"" + util.encodeUTF(md.getValue()) + "\"/>\r\n";
            }
            tOut = tOut + "\t\t" +"</metadata>\r\n";
            tOut = tOut + "\t" + "</user>\r\n";
        }
        tOut = tOut+ "</service>\r\n";
        return tOut;
    }
    public void loadSRVDB(NodeList XMLin)
    {
        for (int i = 0; i < XMLin.getLength(); i++) {  // user tags
            String uTemp, pTemp,eTemp;
            CommandDesc.access_levels aTemp;
            Map <String,String> mTemp = new HashMap<String, String>();

            NodeList t;

            if (XMLin.item(i).getNodeType() != 1 ) continue;

            t = ((Element)XMLin.item(i)).getElementsByTagName("username");
            uTemp = util.decodeUTF(t.item(0).getAttributes().getNamedItem("value").getNodeValue());
            t = ((Element)XMLin.item(i)).getElementsByTagName("password");
            pTemp = util.decodeUTF(t.item(0).getAttributes().getNamedItem("value").getNodeValue());
            t = ((Element)XMLin.item(i)).getElementsByTagName("email");
            eTemp = util.decodeUTF(t.item(0).getAttributes().getNamedItem("value").getNodeValue());
            t = ((Element)XMLin.item(i)).getElementsByTagName("access");
            aTemp = CommandDesc.access_levels.valueOf(t.item(0).getAttributes().getNamedItem("value").getNodeValue());

            t = ((Element)XMLin.item(i)).getElementsByTagName("metadata").item(0).getChildNodes();
            for (int j =0; j< t.getLength();j++) {
                if (t.item(j).getNodeType() != 1 ) continue;
                mTemp.put(util.decodeUTF((t.item(j)).getNodeName()), util.decodeUTF((t.item(j)).getAttributes().getNamedItem("value").getNodeValue()));
            }

            Users.put(uTemp.toLowerCase().trim(), new SrvAuth_user(uTemp,pTemp,eTemp,aTemp,mTemp));
        }
        Logging.info("SRVAUTH", "Loaded " + Users.size() + " registered users from database.");
    }
    public int getcount()
    {
        return Users.size();
    }
}