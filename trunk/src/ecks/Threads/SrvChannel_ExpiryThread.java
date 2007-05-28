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
package ecks.Threads;

import ecks.Configuration;
import ecks.Logging;
import ecks.protocols.Generic;
import ecks.services.SrvChannel;
import ecks.services.SrvChannel_channel;
import ecks.util;

import java.util.ArrayList;

public class SrvChannel_ExpiryThread implements Runnable {

    public void run() {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        for (; ;) {
            try {
                Thread.sleep(1000 * 60 * 5); // five minutes
            } catch (InterruptedException e) {
                Logging.warn("EXPIRY", "Thread interrupted.");
                break;
            }

            Logging.warn("EXPIRY", "Checking for expired channels...");
            ArrayList<SrvChannel_channel> deletelist = new ArrayList<SrvChannel_channel>();


            for (SrvChannel_channel chan : ((SrvChannel) Configuration.getSvc().get(Configuration.chanservice)).getChannels().values())
            // iterate through channels
            {
                if (chan.getAllMeta().containsKey("_ts_last")) {
                    if (Long.parseLong(util.getTS()) - Long.parseLong(chan.getMeta("_ts_last")) > (60 * 60 * 24 * 7 * 5))  // 5 weeks
                    {
                        // should expire
                        deletelist.add(chan);
                    }
                } else {
                    chan.setMeta("_ts_last", util.getTS()); // set ts to now if there is no ts
                }
            }

            // remove channels
            // remove channels
            for (SrvChannel_channel chan : deletelist) {
                ((SrvChannel) Configuration.getSvc().get(Configuration.chanservice)).getChannels().remove(chan.channel);
                Generic.srvPart(Configuration.getSvc().get(Configuration.chanservice), chan.channel, "Channel Expired.");
            }

            Logging.info("EXPIRY", deletelist.size() + " of " + (((SrvChannel) Configuration.getSvc().get(Configuration.chanservice)).getChannels().size()) + " channels expired due to inactivity...");
        }
        util.getThreads().remove(Thread.currentThread()); // if we're out of this loop, then this thread is over.
    }
}
