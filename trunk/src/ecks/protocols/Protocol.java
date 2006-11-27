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
package ecks.protocols;

import java.io.BufferedWriter;
import java.io.IOException;
import ecks.*;
import ecks.services.Service;

public interface Protocol {
    Configuration config = null;
    int state = 0;
    BufferedWriter out = null;
    public void setConfig(Configuration conf);
    public int getState();
    void setBuffers(BufferedWriter o);
    void Incoming(String line);
    void Outgoing(String what) throws IOException;
    void Introduce(String servicename, Service who);
    void SJoin(String servicename, String where, String modes);
    void PrivMessage(Service me, String chan, String msg);
    void Notice(Service me, String them, String msg);
    void die(Service me, String msg);
    void kill(Service me, Configuration conf, String user, String reason);
    void gline(Service me, Configuration conf, Client user, String reason);
    void part(Service me, String chan, String reason);
    void diegraceful();
    void diegraceful(String message);
    void forcemode(Service me, String channel, String mode, String who);
    void setauthed(Service me, Configuration conf, String who);
}
