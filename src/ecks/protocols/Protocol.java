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

import ecks.Utility.Client;
import ecks.services.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;

/**
 * Interface Protocol
 * <p/>
 * Defines the interface that all protocol modules must work through.
 *
 * @author Jeff Katz
 */
public interface Protocol {
    /**
     * Method getState returns the connection state of this Protocol object.
     *
     * @return the state (type States) of this Protocol object.
     */
    States getState();

    /**
     * Method setState sets the connection state of this Protocol object.
     *
     * @param newstate the state of this Protocol object.
     */
    void setState(States newstate);

    /**
     * Method setBuffers sets the output buffer of this Protocol object.
     *
     * @param o the buffer to set for this Protocol object.
     */
    void setBuffers(BufferedWriter o);

    /**
     * Method Incoming recieves a single actionable line from the connection thread
     *
     * @param line of type String
     */
    void Incoming(String line);

    /**
     * Method Outgoing sends a single line to the connection
     *
     * @param what of type String
     * @throws IOException when the buffer is null
     */
    void Outgoing(String what) throws IOException;

    /**
     * Method srvIntroduce introduces a services agent to the network
     *
     * @param whom of type Service
     */
    void srvIntroduce(Service whom);

    /**
     * Method srvJoin joins a services agent to a channel
     *
     * @param who   of type Service
     * @param where of type String (what channel)
     * @param modes of type String (what modes are set)
     */
    void srvJoin(Service who, String where, String modes);

    /**
     * Method srvPart has the services agent leave a channel
     *
     * @param who   of type Service
     * @param where of type String (what channel)
     * @param why   of type String (part reason)
     */
    void srvPart(Service who, String where, String why);

    /**
     * Method outPRVMSG sends a private message to a channel or user
     *
     * @param me   of type Service
     * @param coru of type String (channel, or user target)
     * @param msg  of type String  (what message)
     */
    void outPRVMSG(Service me, String coru, String msg);

    /**
     * Method outNOTICE sends a notice message to a channel or user
     *
     * @param me   of type Service
     * @param coru of type String (channel, or user target)
     * @param msg  of type String  (what message)
     */
    void outNOTICE(Service me, String coru, String msg);

    /**
     * Method outQUIT have a services agent quit
     *
     * @param me  of type Service
     * @param msg of type String (why we're quitting)
     */
    void outQUIT(Service me, String msg);

    /**
     * Method outSETMODE sets a mode on a channel. Forces if not u:lined or similar
     *
     * @param me      of type Service
     * @param channel of type String
     * @param mode    of type String
     * @param who     of type String
     */
    void outSETMODE(Service me, String channel, String mode, String who);

    /**
     * Method outMODE sets a mode on a user. Forces if not u:lined or similar
     *
     * @param me   of type Service
     * @param who  of type Client
     * @param what of type String (what modes)
     * @param more of type String (mode args)
     */
    void outMODE(Service me, Client who, String what, String more);

    /**
     * Method outKILL sends a kill message for a user
     *
     * @param me  of type Service
     * @param who of type String
     * @param why of type String
     */
    void outKILL(Service me, String who, String why);

    /**
     * Method outPART has a services agent leave a channel
     *
     * @param me     of type Service
     * @param chan   of type String (what channel)
     * @param reason of type String (part reason)
     */
    void outPART(Service me, String chan, String reason);

    /**
     * Method outGLINE adds a gline for a Client object
     *
     * @param me  of type Service
     * @param who of type Client (client to kill)
     * @param why of type String (reason)
     */
    void outGLINE(Service me, Client who, String why);

    /**
     * Method outGLINE adds a gline for a mask and duration
     *
     * @param me       of type Service
     * @param mask     of type String
     * @param duration of type long
     * @param why      of type String
     */
    void outGLINE(Service me, String mask, long duration, String why);

    /**
     * Method outUNGLINE unglines a mask
     *
     * @param me   of type Service
     * @param mask of type String
     */
    void outUNGLINE(Service me, String mask);

    /**
     * Method srvSetAuthed does whatever is neccessary in this protocol to mark a user as authenticated (+r, etc)
     *
     * @param me    of type Service
     * @param who   of type String
     * @param svsid of type Long (for irc protocols that store the logged in user info in their user objects)
     */
    void srvSetAuthed(Service me, String who, Long svsid);

    /**
     * Method srvUnSetAuthed does whatever is neccessary to log a user out
     *
     * @param me  of type Service
     * @param who of type String
     */
    void srvUnSetAuthed(Service me, String who);


    /**
     * Method outKICK kicks a user from a channel
     *
     * @param me    of type Service
     * @param who   of type String
     * @param where of type String
     * @param why   of type String
     */
    void outKICK(Service me, String who, String where, String why);

    /**
     * Method outINVITE invites someone somewhere
     *
     * @param me    of type Service
     * @param who   of type String
     * @param where of type String
     */
    void outINVITE(Service me, String who, String where);


    /**
     * Method outTOPIC set the topic in a channel
     *
     * @param me    of type Service
     * @param where of type String
     * @param what  of type String
     */
    void outTOPIC(Service me, String where, String what);

    public static enum States {
        S_DISCONNECTED, S_HASBUFFERS, S_BURSTING, S_SERVICES, S_ONLINE, S_DISCONNECTING
    }

    /**
     * Method getWhenStarted returns the unix timestamp of when the services were started.
     *
     * @return the whenStarted (type long) of this Protocol object.
     */
    long getWhenStarted();

    /**
     * Method getModeArgs returns the modeArgs of this Protocol object.
     *
     * @return the modeArgs (type String) of what channel modes have arguments for this protocol.
     */
    String getModeArgs();

    /**
     * Method getPrefixMap returns the pmapping of channel prefix to symbol for this protocol
     *
     * @return the prefixMap (type Map<Character, Character>) of this Protocol object.
     */
    Map<Character, Character> getPrefixMap();

}
