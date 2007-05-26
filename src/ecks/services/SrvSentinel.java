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
import ecks.util;
import ecks.Logging;
import ecks.Utility.Client;
import ecks.Utility.Modes;
import ecks.Utility.UserModes;
import ecks.Hooks.Hooks;
import ecks.protocols.Generic;

import java.util.Map;
import java.util.HashMap;

import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

public class SrvSentinel extends bService {
    public String name = "SrvSentinel";

    public void introduce() {
        Generic.srvIntroduce(this);
        Hooks.regHook(this, Hooks.Events.E_SIGNON);
        Hooks.regHook(this, Hooks.Events.E_JOINCHAN);
        if(!(Configuration.Config.get("debugchan").equals("OFF")))
        {
            Generic.curProtocol.srvJoin(this, Configuration.Config.get("debugchan"), "+stn");
            Generic.curProtocol.outSETMODE(this, Configuration.Config.get("debugchan"), "+o", name);
        }
    }

    public String getname() { return name; }
    public void setname(String nname) { name = nname; }

    public String getSRVDB()
    { return "";}

    public void loadSRVDB(NodeList XMLin)
    { }
    public int getcount()
    {
        return 0;
    }
    public void hookDispatch(Hooks.Events what, String source, String target, String args) {
        super.hookDispatch(this, what, source, target, args);
        switch (what) {
            case E_JOINCHAN:
            if (Configuration.chanservice!= null)
            if (((SrvChannel) Configuration.getSvc().get(Configuration.chanservice)).getChannels().containsKey(source.toLowerCase()))
            {
                // channel is registered. don't do anything
            } else {
                // channel is unregistered. ensure that users do not have ops, only registered users can speak, etc
                if(Generic.Channels.containsKey(source.toLowerCase()))
                {
                    try {
                for (Map.Entry<Client, UserModes> e : Generic.Channels.get(source.toLowerCase()).clientmodes.entrySet())
                {

                    Generic.curProtocol.outSETMODE(this,source,"-oaqh",e.getKey().uid);
                    if (e.getKey().modes.contains("r"))
                        Generic.curProtocol.outSETMODE(this,source,"+v",e.getKey().uid);
                }
                Generic.curProtocol.outTOPIC(this,source,"This is an unregistered channel. Only registered users may chat. Please see the network help channel for more information.");
                Generic.curProtocol.outSETMODE(this, source, "+Mstl", "10");
                Generic.curProtocol.outPRVMSG(this,source,"This is an unregistered channel. Only registered users may chat. Please see the network help channel for more information.");
                    } catch (NullPointerException NPE)
                    {
                        NPE.printStackTrace();
                    }
                    } else {

                // ERROR!!111
                }

            }
            break;
            case E_SIGNON:
                // don't do it for me.
                if (source != null)
                Generic.curProtocol.outMODE(this,Generic.Users.get(source.toLowerCase()),"+R",""); // enforce +R
            default:
        }
    }

}
