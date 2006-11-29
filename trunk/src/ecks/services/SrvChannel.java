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

public class SrvChannel extends bService {
    String name = "SrvChan";
    public Map<String, SrvChannel_channel> Channels;

    public void introduce() {
        config.chanservice = name.toLowerCase();  // I lay claim to channels
        proto.Introduce(name, this);
        if(!(config.Config.get("debugchan").equals("OFF")))
        {
            proto.SJoin(name, config.Config.get("debugchan"), "+stn");
            proto.forcemode(this, config.Config.get("debugchan"), "+o", name);
        }
        if (config.Config.get("joinchannels").equals("YES")) // if we are set to...
        for(String chan : Channels.keySet()) // ... join registered channels
        {
            proto.SJoin(name, chan, "+stn");
            proto.forcemode(this, chan, "+o", name);
        }
    }
    public void setname(String nname) { name = nname; }
    public Map<String,SrvChannel_channel> getChannels()
    {
        return Channels;
    }

    public SrvChannel()
    {
        Channels = new HashMap<String, SrvChannel_channel>();
    }

   public String getSRVDB() {
        String tOut="";
        tOut = tOut+ "<service class=\"" + this.getClass().getName() + "\" name=\"" + name + "\">\r\n";
        for (Map.Entry<String, SrvChannel_channel> usar : Channels.entrySet()) {
            tOut = tOut + "\t" + "<channel>\r\n";
            tOut = tOut + "\t\t" +"<name value=\"" + util.encodeUTF(usar.getValue().channel) + "\"/>\r\n";
            tOut = tOut + "\t\t" +"<owner value=\"" + util.encodeUTF(usar.getValue().owner) + "\"/>\r\n";
            tOut = tOut + "\t\t" +"<users>\r\n";
            for (Map.Entry<String, SrvChannel_channel.ChanAccess> md : usar.getValue().getUsers().entrySet()) {
                tOut = tOut + "\t\t\t" +"<" + util.encodeUTF(md.getKey()) + " value=\"" + md.getValue() + "\"/>\r\n";
            }
            tOut = tOut + "\t\t" +"</users>\r\n";
            tOut = tOut + "\t\t" +"<settings>\r\n";
            for (Map.Entry<String, String> md : usar.getValue().getSettings().entrySet()) {
                tOut = tOut + "\t\t\t" +"<" + util.encodeUTF(md.getKey()) + " value=\"" + util.encodeUTF(md.getValue()) + "\"/>\r\n";
            }
            tOut = tOut + "\t\t" +"</settings>\r\n";
            tOut = tOut + "\t\t" +"<metadata>\r\n";
            for (Map.Entry<String, String> md : usar.getValue().getAllMeta().entrySet()) {
                tOut = tOut + "\t\t\t" +"<" + util.encodeUTF(md.getKey()) + " value=\"" + util.encodeUTF(md.getValue()) + "\"/>\r\n";
            }
            tOut = tOut + "\t\t" +"</metadata>\r\n";
            tOut = tOut + "\t" + "</channel>\r\n";
        }
        tOut = tOut+ "</service>\r\n";
        return tOut;
    }
    public void loadSRVDB(NodeList XMLin)
    {
        for (int i = 0; i < XMLin.getLength(); i++) {  // channel tags
            String nTemp, oTemp;
            Map <String,String> sTemp = new HashMap<String, String>();
            Map <String,String> mTemp = new HashMap<String, String>();
            Map <String,SrvChannel_channel.ChanAccess> uTemp = new HashMap<String, SrvChannel_channel.ChanAccess>();

            NodeList t;

            if (XMLin.item(i).getNodeType() != 1 ) continue;

            t = ((Element)XMLin.item(i)).getElementsByTagName("name");
            nTemp = util.decodeUTF(t.item(0).getAttributes().getNamedItem("value").getNodeValue());
            t = ((Element)XMLin.item(i)).getElementsByTagName("owner");
            oTemp = util.decodeUTF(t.item(0).getAttributes().getNamedItem("value").getNodeValue());

            t = ((Element)XMLin.item(i)).getElementsByTagName("settings").item(0).getChildNodes();
            for (int j =0; j< t.getLength();j++) {
                if (t.item(j).getNodeType() != 1 ) continue;
                sTemp.put(util.decodeUTF((t.item(j)).getNodeName()), util.decodeUTF((t.item(j)).getAttributes().getNamedItem("value").getNodeValue()));
            }

            t = ((Element)XMLin.item(i)).getElementsByTagName("users").item(0).getChildNodes();
            for (int j =0; j< t.getLength();j++) {
                if (t.item(j).getNodeType() != 1 ) continue;
                uTemp.put(util.decodeUTF((t.item(j)).getNodeName()), SrvChannel_channel.ChanAccess.valueOf((t.item(j)).getAttributes().getNamedItem("value").getNodeValue()));
            }

            t = ((Element)XMLin.item(i)).getElementsByTagName("metadata").item(0).getChildNodes();
            for (int j =0; j< t.getLength();j++) {
                if (t.item(j).getNodeType() != 1 ) continue;
                mTemp.put(util.decodeUTF((t.item(j)).getNodeName()), util.decodeUTF((t.item(j)).getAttributes().getNamedItem("value").getNodeValue()));
            }

            Channels.put(nTemp.toLowerCase().trim(), new SrvChannel_channel(nTemp,oTemp,uTemp, sTemp,mTemp));
        }
        Logging.info("SRVCHAN", "Loaded " + Channels.size() + " registered channels from database.");
    }
    public int getcount()
    {
        return Channels.size();
    }
}