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

import java.util.Map;
import java.util.HashMap;

public class SrvChannel_channel { // direct correlation to a database entry
    public static enum ChanAccess { C_NONE, C_PEON, C_CHANOP, C_MASTER, C_COOWNER, C_OWNER }
    String channel;
    String owner;
    Map<String, ChanAccess> Users;
    Map<String, String> Settings;

    public SrvChannel_channel(){}
    public SrvChannel_channel(String chan, String o)
    {
        channel = chan;
        Users = new HashMap<String,ChanAccess>();
        owner = o;
        Users.put(o,ChanAccess.C_OWNER);
        Settings = new HashMap<String,String>();
    }
    public SrvChannel_channel(String chan, String o, Map<String, ChanAccess> u, Map<String, String> s)
    {
        channel = chan;
        Users = u;
        owner = o;
        Settings = s;
    }
    public Map<String,ChanAccess> getUsers()
    {
        return Users;
    }
    public Map<String,String> getSettings()
    {
        return Settings;
    }
    public String toString()
    {
        return channel + " " + Users.size() + " users, " + Settings.size() + " settings. Owned by: " + owner;
    }

}
