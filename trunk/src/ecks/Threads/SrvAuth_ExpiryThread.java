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
import ecks.services.SrvAuth;
import ecks.services.SrvAuth_user;
import ecks.util;

import java.util.ArrayList;

public class SrvAuth_ExpiryThread implements Runnable {

    public void run() {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        for (; ;) {
            try {
                Thread.sleep(1000 * 60 * 5); // five minutes
            } catch (InterruptedException e) {
                Logging.warn("EXPIRY", "Thread interrupted.");
                break;
            }

            Logging.warn("EXPIRY", "Checking for expired user accounts...");
            ArrayList<SrvAuth_user> deletelist = new ArrayList<SrvAuth_user>();


            for (SrvAuth_user user : ((SrvAuth) Configuration.getSvc().get(Configuration.authservice)).getUsers().values())
            // iterate through users
            {
                if (user.getAllMeta().containsKey("_ts_last")) {
                    if (Long.parseLong(util.getTS()) - Long.parseLong(user.getMeta("_ts_last")) > (60 * 60 * 24 * 7 * 5))  // 5 weeks
                    {
                        // should expire
                        deletelist.add(user);
                    }
                } else {
                    user.setMeta("_ts_last", util.getTS()); // set ts to now if there is no ts
                }
            }

            // remove users
            //            for (SrvAuth_user user : deletelist)
            //         ((SrvAuth) Configuration.getSvc().get(Configuration.authservice)).getUsers().remove(user);
        }
        util.getThreads().remove(Thread.currentThread()); // if we're out of this loop, then this thread is over.
    }
}
