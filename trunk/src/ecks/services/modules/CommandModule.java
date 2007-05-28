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
package ecks.services.modules;

import ecks.services.Service;

/**
 * Interface CommandModule is the interface of which all commands must follow
 *
 * @author Jeff
 */
public interface CommandModule {
    public final CommandDesc Desc = null;

    /**
     * Method getName returns the name of this CommandModule object.
     *
     * @return the name (type String) of this CommandModule object.
     */
    public String getName();

    /**
     * Method getDesc returns the desc of this CommandModule object.
     *
     * @return the desc (type CommandDesc) of this CommandModule object.
     */
    public CommandDesc getDesc();

    /**
     * Method handle_command passes the user's command to the module for processing
     *
     * @param who       of type Service
     * @param user      of type String
     * @param replyto   of type String
     * @param arguments of type String
     */
    public void handle_command(Service who, String user, String replyto, String arguments);

}
