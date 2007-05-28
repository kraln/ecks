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

import ecks.Hooks.Hooks;
import ecks.services.modules.CommandModule;
import org.w3c.dom.NodeList;

import java.util.Map;

/**
 * Interface Service - the interface which all services agents must provide
 *
 * @author Jeff Katz
 */
public abstract interface Service {
    Map<String, CommandModule> Commands = null;

    /**
     * Method introduce instructs the service what to do when it is called into existence
     */
    public void introduce();

    /**
     * Method getname returns the configured name of the services agent
     *
     * @return String (the name of the agent)
     */
    public String getname();

    /**
     * Method getcount returns a number (usually the number of objects this agent tracks)
     * This is usually used for RPC purposes
     *
     * @return int (whatever the agent wants)
     */
    public int getcount();

    /**
     * Method setname sets the name of the services agent to something else
     *
     * @param nname of type String
     */
    public void setname(String nname);

    /**
     * Method handle asks the services agent to deal with a command
     *
     * @param user    of type String
     * @param replyto of type String
     * @param command of type String
     */
    public void handle(String user, String replyto, String command);

    /**
     * Method die instructs the agent to cleanup whatever it's doing and prepare to die
     *
     * @param message of type String
     */
    public void die(String message);

    /**
     * Method addCommand adds a command module to the list of commands this agent can process
     *
     * @param cmdName of type String
     * @param newCmd  of type CommandModule
     */
    public void addCommand(String cmdName, CommandModule newCmd);

    /**
     * Method getCommands returns the commands of this Service object.
     *
     * @return the commands (type Map<String, CommandModule>) of this Service object.
     */
    public Map<String, CommandModule> getCommands();

    /**
     * Method hookDispatch accepts system hooks and handles them
     *
     * @param what   of type Events
     * @param source of type String
     * @param target of type String
     * @param args   of type String
     */
    public void hookDispatch(Hooks.Events what, String source, String target, String args);

    /**
     * Method getSRVDB returns the SRVDB of this Service object.
     *
     * @return the SRVDB (type String) of this Service object.
     */
    public String getSRVDB();

    /**
     * Method loadSRVDB ...
     *
     * @param XMLin of type NodeList
     */
    public void loadSRVDB(NodeList XMLin);
}
