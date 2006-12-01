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

import ecks.protocols.Protocol;
import ecks.Logging;
import ecks.Storage;
import ecks.util;

import java.io.*;
import java.net.*;

public class DbThread implements Runnable {

    // my entire existence is to flush the DB to disk every five minutes

    public void run()
    {
      for(;;) // the semicolons, they do nothing!
      {
          try {
              Thread.sleep(1000 * 60 * 5);
          } catch (InterruptedException e) {
              Logging.warn("DBTHREAD", "Thread interrupted. Initiating DB Write...");
              Storage.flushDB();
              Logging.info("DBTHREAD", "DB Write completed...");
              break;
          }
          Storage.flushDB();
          Logging.info("DBTHREAD", "Wrote database...");
      }
      util.getThreads().remove(Thread.currentThread()); // if we're out of this loop, then this thread is over.
    }
}
