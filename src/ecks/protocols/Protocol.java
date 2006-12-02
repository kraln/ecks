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

import ecks.services.Service;
import ecks.Utility.Client;

import java.io.BufferedWriter;
import java.io.IOException;

public interface Protocol {
    int getState();

    void setBuffers(BufferedWriter o);

    void Incoming(String line);

    void Outgoing(String what) throws IOException;

    void srvIntroduce(Service whom)
// Introduce a service to the network
            ;

    void srvJoin (Service who, String where, String modes)
// Services joins a channel
            ;

    void srvPart (Service who, String where, String why)
// Services leaves a channel
            ;

    void outPRVMSG(Service me, String coru, String msg)
// Send a privmsg to a channel or user
            ;

    void outNOTICE(Service me, String coru, String msg)
// Send a notice to a channel or a user
            ;

    void outQUIT(Service me, String msg)
// Quit a service
            ;

    void outSETMODE(Service me, String channel, String mode, String who)
// Set a mode on a channel. If we're not ulined, we'll have to force it...
            ;

    void outKILL(Service me, String who, String why)
// Kill someone.
            ;

    void outPART(Service me, String chan, String reason)
// Have services leave a channel
            ;

    void outGLINE(Service me, Client who, String why)
// Add an AKILL
            ;

    void outUNGLINE(Service me, String mask)
// Remove an AKILL
            ;

    void srvSetAuthed(Service me, String who)
// Let other servers know that this user is authed
            ;

    void outKICK(Service me, String who, String where, String why)
// Kick someone from a channel
            ;

    void outINVITE(Service me, String who, String where)
// Invite someone somewhere
            ;

    void outMODE(Service me, String who, String where, String what);

    void outTOPIC(Service me, String where, String what);

    public static enum States {
        S_DISCONNECTED, S_HASBUFFERS, S_BURSTING, S_SERVICES, S_ONLINE
    }

    String getModeArgs()
    // Get what channel modes have arguments for this protocol
        ;

}
