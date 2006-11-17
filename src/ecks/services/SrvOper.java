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

public class SrvOper extends bService {
    String name = "SrvOper";

    public void introduce() {        
        proto.Introduce(name, this);
        if(!(config.Config.get("debugchan").equals("OFF")))
        {
            proto.SJoin(name, config.Config.get("debugchan"), "+stn");
            proto.forcemode(this, config.Config.get("debugchan"), "+o", name);
        }
    }
    public void setname(String nname) { name = nname; }
    public String getSRVDB() {
        return "";
    }
    
    public void loadSRVDB(NodeList XMLin)
    {

    }
}