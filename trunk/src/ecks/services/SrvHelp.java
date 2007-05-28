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
import ecks.Utility.Client;
import ecks.protocols.Generic;
import ecks.util;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;

public class SrvHelp extends bService {
    public String name = "SrvHelp";
    public Map<String, SrvHelp_channel> Channels;
    public Map<Client, SrvHelp_channel> qMap;

    public void introduce() {
        Generic.srvIntroduce(this);
        Hooks.regHook(this, Hooks.Events.E_PRIVMSG);
        Hooks.regHook(this, Hooks.Events.E_JOINCHAN);
        Hooks.regHook(this, Hooks.Events.E_PARTCHAN);
        if (!(Configuration.Config.get("debugchan").equals("OFF"))) {
            Generic.curProtocol.srvJoin(this, Configuration.Config.get("debugchan"), "+stn");
            Generic.curProtocol.outSETMODE(this, Configuration.Config.get("debugchan"), "+o", name);
        }
        if (Configuration.Config.get("joinchannels").equals("YES")) // if we are set to...
            for (String chan : Channels.keySet()) // ... join registered channels
            {
                Generic.curProtocol.srvJoin(this, chan, "+stn");
                Generic.curProtocol.outSETMODE(this, chan, "+o", name);
            }
    }

    public String getname() {
        return name;
    }

    public void setname(String nname) {
        name = nname;
    }

    public Map<String, SrvHelp_channel> getChannels() {
        return Channels;
    }

    public SrvHelp() {
        Channels = new HashMap<String, SrvHelp_channel>();
        qMap = new HashMap<Client, SrvHelp_channel>();
    }

    public String getSRVDB() {
        String tOut = "";
        tOut = tOut + "<service class=\"" + this.getClass().getName() + "\" name=\"" + name + "\">\r\n";
        for (Map.Entry<String, SrvHelp_channel> usar : Channels.entrySet()) {
            tOut = tOut + "\t" + "<channel>\r\n";
            tOut = tOut + "\t\t" + "<name value=\"" + util.encodeUTF(usar.getValue().channel) + "\"/>\r\n";
            tOut = tOut + "\t\t" + "<metadata>\r\n";
            for (Map.Entry<String, String> md : usar.getValue().getAllMeta().entrySet()) {
                tOut = tOut + "\t\t\t" + "<" + util.encodeUTF(md.getKey()) + " value=\"" + util.encodeUTF(md.getValue()) + "\"/>\r\n";
            }
            tOut = tOut + "\t\t" + "</metadata>\r\n";
            tOut = tOut + "\t" + "</channel>\r\n";
        }
        tOut = tOut + "</service>\r\n";
        return tOut;
    }

    public void loadSRVDB(NodeList XMLin) {
        for (int i = 0; i < XMLin.getLength(); i++) {  // channel tags
            String nTemp;
            Map<String, String> mTemp = new HashMap<String, String>();
            NodeList t;

            if (XMLin.item(i).getNodeType() != 1) continue;

            t = ((Element) XMLin.item(i)).getElementsByTagName("name");
            nTemp = util.decodeUTF(t.item(0).getAttributes().getNamedItem("value").getNodeValue());

            t = ((Element) XMLin.item(i)).getElementsByTagName("metadata").item(0).getChildNodes();

            for (int j = 0; j < t.getLength(); j++) {
                if (t.item(j).getNodeType() != 1) continue;
                mTemp.put(util.decodeUTF((t.item(j)).getNodeName()), util.decodeUTF((t.item(j)).getAttributes().getNamedItem("value").getNodeValue()));
            }
            Channels.put(nTemp.toLowerCase().trim(), new SrvHelp_channel(nTemp, mTemp));
        }
        Logging.info("SRVHELP", "Loaded " + Channels.size() + " help channels from database.");
    }

    public int getcount() {
        return Channels.size();
    }

    public void hookDispatch(Hooks.Events what, String source, String target, String args) {
        switch (what) {
            case E_PRIVMSG:
                if (qMap.containsKey(Generic.Users.get(source.toLowerCase()))) {
                    // user is being helped. instead of parsing their text as commands,
                    // add it to a buffer or redirect it to the person assigned to them
                    break;
                }
            default:
                super.hookDispatch(this, what, source, target, args); // this comes after we handle things, so if we're a privmsg
                // we can intercept without causing an error
        }
    }

}
