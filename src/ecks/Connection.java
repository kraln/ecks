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

public class Connection {
    // Hi. I handle all the specifics of connecting at a tcp/ip level.
    // I start a thread that handles all incoming stuff from the uplink.
    // That thread, in turn, hands all incoming stuff off to the protocol handler
    // which, by the way, I tell how to talk to the server. Neat, huh?


     String host, lp;
     InetAddress lhost;
     int p;
     Protocol prot;
     Socket sock;
     BufferedWriter out;
     BufferedReader in;

     Connection(String hostname, int port, String lport, InetAddress lh, Protocol grok) {host=hostname; p=port; prot=grok; lhost = lh; lp = lport; }
     public void Connect() {
         try {
             if (lp.equals("any")) // we don't care about binding to a specific address
                 sock = new Socket(host, p);
             else // we do care. sigh.
                 sock = new Socket(host, p, lhost, Integer.parseInt(lp));
             out = new BufferedWriter (new OutputStreamWriter(sock.getOutputStream()));
             in = new BufferedReader (new InputStreamReader(sock.getInputStream()));
         } catch (IOException e) {
             e.printStackTrace();
             return;
         }
         prot.setBuffers(out);

         util.startThread(new Thread(new ConnThread(in,prot))).start(); // start async input thread

     }
}
