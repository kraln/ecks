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
import java.util.List;

public class SrvHelp extends bService {
    String name = "SrvHelp";
    public Map<String, SrvHelp_channel> Channels;

    public void introduce() {
        proto.Introduce(name, this);
        if(!(config.Config.get("debugchan").equals("OFF")))
        {
            proto.SJoin(name, config.Config.get("debugchan"), "+stn");
            proto.forcemode(this, config.Config.get("debugchan"), "+o", name);
        }
        if (config.Config.get("joinchannels").equals("YES")) // if we are set to...
        for(String chan : Channels.keySet()) // ... join registered help channels
        {
            proto.SJoin(name, chan, "+stn");
            proto.forcemode(this, chan, "+o", name);
        }
    }
    public void setname(String nname) { name = nname; }
    public Map<String,SrvHelp_channel> getChannels()
    {
        return Channels;
    }

    public SrvHelp()
    {
        Channels = new HashMap<String, SrvHelp_channel>();
    }

   public String getSRVDB() {
        String tOut="";
        tOut = tOut+ "<service class=\"" + this.getClass().getName() + "\" name=\"" + name + "\">\r\n";
        for (Map.Entry<String, SrvHelp_channel> usar : Channels.entrySet()) {
            tOut = tOut + "\t" + "<channel>\r\n";
            tOut = tOut + "\t\t" +"<name value=\"" + usar.getValue().channel + "\"/>\r\n";
            tOut = tOut + "\t" + "</channel>\r\n";
        }
        tOut = tOut+ "</service>\r\n";
        return tOut;
    }
    public void loadSRVDB(NodeList XMLin)
    {
        for (int i = 0; i < XMLin.getLength(); i++) {  // channel tags
            String nTemp;

            NodeList t;

            if (XMLin.item(i).getNodeType() != 1 ) continue;

            t = ((Element)XMLin.item(i)).getElementsByTagName("name");
            nTemp = t.item(0).getAttributes().getNamedItem("value").getNodeValue();
            Channels.put(nTemp.toLowerCase().trim(), new SrvHelp_channel(nTemp));
        }
    }
}
