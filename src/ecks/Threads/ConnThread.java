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
import ecks.protocols.Generic;
import ecks.Logging;
import ecks.util;
import ecks.main;

import java.io.*;
import java.net.*;
import java.nio.channels.ClosedByInterruptException;

public class ConnThread implements Runnable {

    // Send incoming lines, one at a time, to the protocol handler.

    BufferedReader stream;
    Protocol handoff;
    public ConnThread(BufferedReader in, Protocol p){ stream = in; handoff = p; }

    public void run()
    {
      for(;;) // the semicolons, they do nothing!
      {
          try {
              handoff.Incoming(  stream.readLine().trim() ); // give the incoming line to the protocol handler
          } catch (ClosedByInterruptException e) { // occurs when we get interrupted
              e.printStackTrace();
              Logging.warn("CONNTHREAD", "Thread is being interrupted.");
              break;
//          } catch (SocketException e) {
//              Generic.curProtocol.setState(Protocol.States.S_DISCONNECTED);
//              Logging.error("CONNTHREAD", "Connection terminated unexpectedly, quitting");
//              break;
            } catch (NullPointerException npe) {
              npe.printStackTrace();
              Logging.error("CONNTHREAD", "Null pointer exception!");
                  Generic.curProtocol.setState(Protocol.States.S_DISCONNECTING);
                  Logging.error("CONNTHREAD", "Upstream is null; Server closed connection.");                   
                    break;
          } catch (IOException e) {
              e.printStackTrace();
              break;
          } catch (Exception e) {
              e.printStackTrace();
              Logging.error("CONNTHREAD", "Thread got exception!");
              try {
                  if (!stream.ready())
                  {
                        Generic.curProtocol.setState(Protocol.States.S_DISCONNECTING);
                        Logging.error("CONNTHREAD", "Upstream is null; Server closed connection.");
                        break;
                  }
              } catch (IOException e1) {
                  break;
              }
              Logging.info("CONNTHREAD", e.getMessage());
          }

          if(Thread.interrupted())
              break; // we've been interrupted. likely going for a shutdown
      }

      Logging.warn("CONNTHREAD", "Thread has broken free of loop.");
      main.goGracefullyIntoTheNight();
      util.getThreads().remove(Thread.currentThread()); // if we're out of this loop, then this thread is over.

    }
}
