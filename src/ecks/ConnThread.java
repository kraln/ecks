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
package ecks;

import ecks.protocols.Protocol;

import java.io.*;
import java.net.*;

public class ConnThread implements Runnable {

    // my entire existence is to provide non-blocking (input) reads

    BufferedReader stream;
    Protocol handoff;
    ConnThread(BufferedReader in, Protocol p){ stream = in; handoff = p; }

    public void run()
    {
      for(;;) // the semicolons, they do nothing!
      {
          try {
              handoff.Incoming(  stream.readLine().trim() ); // give the incoming line to the protocol handler
          } catch (IOException e) {
              e.printStackTrace();
          } catch (NullPointerException npe)
          {
              npe.printStackTrace();
              System.exit(666); // connection terminated. likely, our uplink cored.
          }
      }
    }
}