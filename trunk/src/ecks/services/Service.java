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

import ecks.*;
import ecks.Hooks.Hooks;
import ecks.services.modules.CommandModule;
import ecks.protocols.Protocol;
import org.w3c.dom.NodeList;

import java.util.Map;

public abstract interface Service {
    Map <String, CommandModule> Commands = null;

    public void introduce();
    public String getname();
    public int getcount();
    public void setname(String nname);
    public void handle(String user, String replyto, String command);
    public void die(String message);
    public void addCommand (String cmdName, CommandModule newCmd);
    public Map<String, CommandModule> getCommands();
    public void hookDispatch(Hooks.Events what, String source, String target, String args);

    public String getSRVDB();
    public void loadSRVDB(NodeList XMLin);
}
