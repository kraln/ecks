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
package ecks.Utility;
import ecks.Utility.Modes;
import ecks.Utility.Client;

import java.util.Map;
import java.util.HashMap;

public class Channel {
    public String name;
    public int ts;
    public String topic;
    public int tts;
    public ChanModes modes;
    public Map<Client, UserModes> clientmodes;

    public Channel(){}
    public Channel(int t, String n, ChanModes m)
    {
        ts = t; name = n; modes = m;
        clientmodes=new HashMap<Client,UserModes>();
    }
    public Channel(int t, String n, ChanModes m, Map<Client, UserModes> cm)
    {
        ts = t;
        name = n;
        modes = m;
        clientmodes = cm;
        topic = "";
    }
    public String toString()
    {
        String c = "";
        for (Client uname : clientmodes.keySet())
        c += " " + uname.uid;                                                                                                                  
        return name + ": " + ts + " " + modes.getModes() + " occupied by " + clientmodes.size() + " users. Topic is: " + topic + ". Users:" + c;
    }
}
