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

package ecks.RPC;

import ecks.Configuration;
import ecks.Logging;
import ecks.protocols.Generic;

/**
 * Class RPCHandler allows for functions that can be called remotely
 *
 * @author Jeff
 */
public class RPCHandler {
    /**
     * Method getUserCount returns the network wide usercount
     *
     * @return the userCount (type int).
     */
    public int getUserCount() {
        Logging.info("RPC", "Got UserCount Request!");
        return Generic.Users.size();
    }

    /**
     * Method getChanCount returns the network wide channel count.
     *
     * @return the chanCount (type int).
     */
    public int getChanCount() {
        Logging.info("RPC", "Got ChanCount Request!");
        return Generic.Channels.size();
    }

    /**
     * Method getRegUserCount returns the global registered users count (if auth service is used).
     *
     * @return the regUserCount (type int).
     */
    public int getRegUserCount() {
        Logging.info("RPC", "Got RegUserCount Request!");
        if (Configuration.getSvc().containsKey(Configuration.authservice))
            return Configuration.getSvc().get(Configuration.authservice).getcount();
        return -1; // no auth service
    }

    /**
     * Method getRegChanCount returns the global registered channels count (if chan service is used).
     *
     * @return the regChanCount (type int).
     */
    public int getRegChanCount() {
        Logging.info("RPC", "Got RegChanCount Request!");
        if (Configuration.getSvc().containsKey(Configuration.chanservice))
            return Configuration.getSvc().get(Configuration.chanservice).getcount();
        return -1; // no chan service
    }

    /**
     * Method getChanUserCount returns the number of users currently active in a channel
     *
     * @param whatchan of type String (channel
     * @return int (users in the channel)
     */
    public int getChanUserCount(String whatchan) {
        Logging.info("RPC", "Got ChanUserCount Request!");
        Logging.verbose("RPC", "Request was for channel: " + whatchan);
        if (Generic.Channels.containsKey(whatchan.toLowerCase()))
            return Generic.Channels.get(whatchan.toLowerCase()).clientmodes.size();
        return -1;
    }

}
