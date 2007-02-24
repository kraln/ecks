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
import org.w3c.dom.Element;

import java.util.Map;
import java.util.HashMap;

import ecks.util;
import ecks.Logging;
import ecks.Configuration;
import ecks.Threads.SrvChannel_ExpiryThread;
import ecks.Hooks.Hooks;
import ecks.protocols.Generic;

public class SrvChannel extends bService {
    public String name = "SrvChan";
    public Map<String, SrvChannel_channel> Channels;

    public void introduce() {
        Configuration.chanservice = name.toLowerCase();  // I lay claim to channels
        Generic.srvIntroduce(this);

        // register hooks that we care about
        Hooks.regHook(this, Hooks.Events.E_PRIVMSG);
        Hooks.regHook(this, Hooks.Events.E_JOINCHAN);
        Hooks.regHook(this, Hooks.Events.E_KICK);
        Hooks.regHook(this, Hooks.Events.E_MODE);
        Hooks.regHook(this, Hooks.Events.E_TOPIC);

        // start a thread to clear expired channels
        util.startThread(new Thread(new SrvChannel_ExpiryThread())).start();
        Logging.info("SRVCHAN", "Expiry thread started...");

        if (!(Configuration.Config.get("debugchan").equals("OFF"))) {
            Generic.curProtocol.srvJoin(this, Configuration.Config.get("debugchan"), "+stn");
            Generic.curProtocol.outSETMODE(this, Configuration.Config.get("debugchan"), "+o", name);
        }
        if (Configuration.Config.get("joinchannels").equals("YES")) // if we are set to...
            for (String chan : Channels.keySet()) // ... join registered channels
            {
                Generic.curProtocol.srvJoin(this, chan, "+srtn");
                Generic.curProtocol.outSETMODE(this, chan, "+ntro", name);
                if (Channels.get(chan).getAllMeta().containsKey("enftopic")) {
                    Generic.curProtocol.outTOPIC(this, chan, Channels.get(chan).getMeta("enftopic"));
                }
            }
    }

    public String getname() {
        return name;
    }

    public void setname(String nname) {
        name = nname;
    }

    public Map<String, SrvChannel_channel> getChannels() {
        return Channels;
    }

    public SrvChannel() {
        Channels = new HashMap<String, SrvChannel_channel>();
    }

    public String getSRVDB() {
        String tOut = "";
        tOut = tOut + "<service class=\"" + this.getClass().getName() + "\" name=\"" + name + "\">\r\n";
        for (Map.Entry<String, SrvChannel_channel> usar : Channels.entrySet()) {
            tOut = tOut + "\t" + "<channel>\r\n";
            tOut = tOut + "\t\t" + "<name value=\"" + util.encodeUTF(usar.getValue().channel) + "\"/>\r\n";
            tOut = tOut + "\t\t" + "<users>\r\n";
            for (Map.Entry<String, SrvChannel_channel.ChanAccess> md : usar.getValue().getUsers().entrySet()) {
                tOut = tOut + "\t\t\t" + "<" + util.encodeUTF(md.getKey()) + " value=\"" + md.getValue() + "\"/>\r\n";
            }
            tOut = tOut + "\t\t" + "</users>\r\n";
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
            Map<String, String> sTemp = new HashMap<String, String>();
            Map<String, String> mTemp = new HashMap<String, String>();
            Map<String, SrvChannel_channel.ChanAccess> uTemp = new HashMap<String, SrvChannel_channel.ChanAccess>();

            NodeList t;

            if (XMLin.item(i).getNodeType() != 1) continue;

            t = ((Element) XMLin.item(i)).getElementsByTagName("name");
            nTemp = util.decodeUTF(t.item(0).getAttributes().getNamedItem("value").getNodeValue());

            t = ((Element) XMLin.item(i)).getElementsByTagName("users").item(0).getChildNodes();
            for (int j = 0; j < t.getLength(); j++) {
                if (t.item(j).getNodeType() != 1) continue;
                String handle = (util.decodeUTF((t.item(j)).getNodeName()));
                String access = (t.item(j)).getAttributes().getNamedItem("value").getNodeValue();

                if (((SrvAuth) Configuration.getSvc().get(Configuration.authservice)).getUsers().containsKey(handle.toLowerCase()))
                    ((SrvAuth) Configuration.getSvc().get(Configuration.authservice)).getUsers().get(handle.toLowerCase()).WhereAccess.put(nTemp, access);

                uTemp.put(handle, SrvChannel_channel.ChanAccess.valueOf(access));

            }

            t = ((Element) XMLin.item(i)).getElementsByTagName("metadata").item(0).getChildNodes();
            for (int j = 0; j < t.getLength(); j++) {
                if (t.item(j).getNodeType() != 1) continue;
                mTemp.put(util.decodeUTF((t.item(j)).getNodeName()), util.decodeUTF((t.item(j)).getAttributes().getNamedItem("value").getNodeValue()));
            }

            Channels.put(nTemp.toLowerCase().trim(), new SrvChannel_channel(nTemp, uTemp, sTemp, mTemp));
        }
        Logging.info("SRVCHAN", "Loaded " + Channels.size() + " registered channels from database.");
    }

    public int getcount() {
        return Channels.size();
    }

    public void hookDispatch(Hooks.Events what, String source, String target, String args) {
        super.hookDispatch(this, what, source, target, args);
        switch (what) {
            case E_JOINCHAN:
                this.handle(target, source, "sync silent");
                break;
            case E_MODE:
                if (Channels.containsKey(target)) {
                    if (Channels.get(target).getAllMeta().containsKey("enfmodes")) {
                        Generic.curProtocol.outSETMODE(this, target, Channels.get(target).getMeta("enfmodes"), this.getname());
                    }
                }
                break;
            case E_TOPIC:
                if (Channels.containsKey(source))
                    if (Channels.get(source).getAllMeta().containsKey("enftopic")) {
                        if (!Generic.Channels.get(source).topic.equals(Channels.get(source).getMeta("enftopic")))
                            Generic.curProtocol.outTOPIC(this, source, Channels.get(source).getMeta("enftopic"));
                    }
                break;
            default:
        }
    }

}