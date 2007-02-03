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

import ecks.Logging;
import ecks.main;
import ecks.util;
import ecks.Configuration;
import ecks.Utility.Client;
import ecks.services.Service;
import ecks.Hooks.Hooks;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

public class unreal32 implements Protocol {
    /*

    Please note that I haven't tested this protocol module very thoroughly, and that it's
    based off of the fqircd/bahamut module as of 12/22/2006 - it may be old.

    Try not to judge ecks based on how well it deals with unreal.

    Services link, join channels, report debug information, and seem to work alright.
    There is no support for the extended channel modes supported by unreal(yet).
    There is no support (but it'd be quick) for unreal vhosts yet.

    Please let me know your experiences with this module.

    */
    BufferedWriter out; // where we send our irc commands to
    States myState; // what the current state of connectivity is
    boolean wasOnline; // helps us determine if we were online (split or first time connect)
    String myUplink; // what our uplink thinks it is
    long connected; // what time we connected
    final String modeargs = "abefhIjovqlLkE"; // what chanel modes are allowed to have arguments in this protocol

    public long getWhenStarted() {
        return connected;
    }

    public unreal32() {
        myState = States.S_DISCONNECTED; // we start out disconnected
        wasOnline = false;
    }

     public Map<Character, Character> getPrefixMap()
    {
        Map <Character, Character> z = new HashMap<Character, Character>();
        z.put('~', 'q');
        z.put('%', 'h');
        z.put('&', 'a');
        z.put('@', 'o');
        z.put('+', 'v');
        return z;
    }

    public String getModeArgs() {
        return modeargs;
    }

    public States getState() {
        return myState;
    }

    public void setState(States newstate) {
        myState = newstate;
    }

    public void setBuffers(BufferedWriter o) {
        out = o; // set our outbuffer to the one we're given
        myState = States.S_HASBUFFERS; // now we have somewhere to send our commands
        Logging.info("PROTOCOL", "Waiting for server...");
    }

    public void Incoming(String line) {

        Logging.raw(line, true); // raw lines get logged here

        if (line == null) { // this should never, ever happen.
            Logging.error("PROTOCOL", "Got NULL incoming line!");
            main.goGracefullyIntoTheNight();
            return; // will never get here...
        }

        // deal with all our tokenization and so forth *here*
        boolean hasSource = line.startsWith(":");
        String halves[] = line.split(" :", 2);
        boolean hasExtArg = (halves.length > 1);
        String tokens[] = halves[0].split(" ");
        String command = tokens[(hasSource ? 1 : 0)];
        String source = (hasSource ? tokens[0].substring(1) : null);

        Delegate(command, hasSource, source, tokens, hasExtArg, (hasExtArg ? halves[1] : null));

    }

    public void Outgoing(String what) throws IOException {
        out.write(what + "\r\n");
        Logging.raw(what, false);
        out.flush();
    }


    void Delegate(String cmd, Boolean hasSource, String source, String[] tokens, Boolean hasargs, String args) {
        try {
            if (cmd.equals("PING")) {                                                                            // PING
                outPong();

            } else if (cmd.equals("EOS")) {


                if (myState == States.S_BURSTING) {
                    if (!wasOnline) { // first time connecting
                        Logging.info("PROTOCOL", "Burst completed. Bringing services online...");
                        myState = States.S_SERVICES;
                        Generic.BringServicesOnline();
                        wasOnline = true;
                        myState = States.S_ONLINE;
                        Logging.info("PROTOCOL", "Ecks Services " + util.getVersion() + " operational. " + util.getTS());
                        connected = Long.parseLong(util.getTS());

                    } else { // netsplit recovery
                        Logging.warn("PROTOCOL", "Network recovered from netsplit...");
                        Logging.info("PROTOCOL", "Burst completed.");
                        myState = States.S_SERVICES;
                    }
                }

            } else if (cmd.equals("NOTICE")) {                                                                 // NOTICE

                if (args.startsWith("*** Found"))
                if (myState == States.S_HASBUFFERS) { // We're probably being told about hostnames and so forth
                        outHandshake(); // send our half of the server information handshake
                        Logging.info("PROTOCOL", "Sending Handshake...");
                }

            } else if (cmd.equals("SMO")) {                                                                   // GNOTICE

                if (myState == States.S_HASBUFFERS)
                {
                    Logging.info("PROTOCOL", "Connection established. Beginning burst...");
                    myState = States.S_BURSTING;
                }

            } else if (cmd.equals("NICK")) {                                                                     // NICK

                if (hasSource) { // It's a rename
                    Generic.nickRename(source, tokens[2], 0);
                } else { // It's a new client, in a burst or otherwise
                    nickSignOn(tokens, args);
                }

            } else if (cmd.equals("KICK")) {                                                                     // KICK

                Generic.nickGotKicked(tokens[3], tokens[2]);

            } else if (cmd.equals("SERVER")) {                                                                 // SERVER

                myUplink = tokens[1];

            } else if (cmd.equals("KILL")) {                                                                     // KILL

                Generic.nickGotKilled(tokens[2]);

            } else if (cmd.equals("AWAY")) {                                                                     // AWAY

                // goggles. suppresses 'unsupported command'

            } else if (cmd.equals("PART")) {                                                                     // PART

                // :SOURCE PART #CHANNEL
                Generic.chanPart(tokens[2], source);


            } else if (cmd.equals("QUIT")) {                                                                     // QUIT

                Generic.nickSignOff(source);

            } else if (cmd.equals("MODE")) {                                                                     // MODE

                String modestring;
                if (tokens[2].startsWith("#")) { // is a channel mode
                    modestring = tokens[3];
                    if (tokens.length > 3)
                        for (int i = 4; i < tokens.length; i++)
                            modestring += " " + tokens[i];
                    Generic.modeChan(tokens[2], modestring);
                } else {                         // user mode has changed
                    modestring = args;
                    Generic.modeUser(tokens[2], modestring);
                }

            } else if (cmd.equals("PRIVMSG")) {                                                               // PRIVMSG

                //:SOURCE PRIVMSG TARGET :MESSAGE
                Hooks.hook(Hooks.Events.E_PRIVMSG, source, tokens[2], args);

            } else if (cmd.equals("SETHOST")) {                                                               // SETHOST
                // :SOURCE SETHOST NEWHOST

                Generic.vHost(source, tokens[2]);


            } else if (cmd.equals("JOIN")) {                                                                    // SJOIN

                // :SOURCE JOIN :#channel, #channel
                String[] t = tokens[2].split(",");
                if (!Generic.Users.containsKey(source.toLowerCase())) { // server is introducing channel
                    for (String c : t)
                        Generic.chanBurst(0, c, "", null);

                } else { // just a user joining
                    for (String c : t)
                        Generic.chanJoin(0, c, source.toLowerCase());
                }
            } else if (cmd.equals("TOPIC")) {                                                                   // TOPIC
                // :SOURCE TOPIC #CHANNAME SETTER TS :NEWTOPIC
                Generic.chanTopic(Integer.parseInt(tokens[4]), tokens[2], args);


            } else if (cmd.equals("ERROR")) {                                                                   // ERROR

                Logging.error("PROTOCOL", "Recieved Error. Disconnecting.");
                Logging.warn("PROTOCOL", "Error was: " + (hasargs ? args : ""));
                main.goGracefullyIntoTheNight();

            } else {                                                                                          // UNKNOWN

                Logging.warn("PROTOCOL", "Unsupported command: " + cmd);

            }
        } catch (IOException ioe) {
            Logging.error("PROTOCOL", "Got IOException while delegating command: " + cmd);
            Logging.error("PROTOCOL", "IOE Claims: " + ioe.getMessage());
        }
    }


    void nickSignOn(String[] tokens, String args) {
        // unreal specific...
        // nick NICKNAME HOPS SIGMON USER HOST SERVER SERVICE MODE ALTHOST :REALNAME
        // 0    1        2    3      4    5    6      7       8    9

        // generic...
        //      uid hops signon modes ident host althost uplink svsid numericip realname nickid

        if (tokens.length == 8) {
            String[] newargs = {null,
                    tokens[1],
                    tokens[2],
                    tokens[3],
                    "",
                    tokens[4],
                    tokens[5],
                    "",
                    tokens[6],
                    tokens[7],
                    "0",
                    args,
                    null};
            Generic.nickSignOn(newargs);
        } else {

            String[] newargs = {null,
                    tokens[1],
                    tokens[2],
                    tokens[3],
                    tokens[8],
                    tokens[4],
                    tokens[5],
                    tokens[9],
                    tokens[6],
                    tokens[7],
                    "0",
                    args,
                    null};
            Generic.nickSignOn(newargs);
        }

    }

    void outPong() throws IOException {
        Outgoing("PONG :" + util.getTS());
        Logging.info("PROTOCOL", "Server pinged...");
    }

    void outHandshake() throws IOException // very much bahamut specific
    {
        Outgoing("CAPAB :QS NICKv2 SJOIN UMODE2 SJ3 VHP"); // Quitstorm, Timestamp, Services, @+notices, klines and glines, knocking, and SJOIN for joins
        Outgoing("PASS " + Configuration.Config.get("password")); // send our connection password
        Outgoing("SERVER " + Configuration.Config.get("hostname") + " 1 :U2303-CFhiIXe-" + Configuration.Config.get("numeric") + " Ecks Services " + util.getVersion()); // send our server info
    }

    public void srvIntroduce(Service whom)
    // Introduce a service to the network
    {
        //nick NICKNAME HOPS SIGMON USER HOST SERVER SERVICE MODE ALTHOST :REALNAME
        try {
            String o = "NICK "
                    + whom.getname()
                    + " 1 "
                    + util.getTS()
                    + " "
                    + whom.getname()
                    + " services "
                    + Configuration.Config.get("hostname")
                    + " -1"
                    + " +iorS "
                    + Configuration.Config.get("hostname")
                    + " :Network Services";
            Outgoing(o);
            String[] tokens = o.split(" ");
            String[] newargs = {null,
                    tokens[1],
                    tokens[2],
                    tokens[3],
                    tokens[8],
                    tokens[4],
                    tokens[5],
                    tokens[9],
                    tokens[6],
                    tokens[7],
                    "0",
                    "Network Services",
                    null};
            Generic.nickSignOn(newargs);
        } catch (IOException e) {
            Logging.error("PROTOCOL", "Got IOException while introducing service: " + whom.getname());
            Logging.error("PROTOCOL", "IOE Claims: " + e.getMessage());
        }
    }

    public void srvJoin(Service who, String where, String modes)
    // Have service join a channel
    {
        try {
            Outgoing("SJOIN " + util.getTS() + " " + where + " " + modes + " :@" + who.getname());
            Generic.chanJoin(Integer.parseInt(util.getTS()), where, who.getname());
        } catch (IOException e) {
            Logging.error("PROTOCOL", "Got IOException while sending a command.");
            Logging.error("PROTOCOL", "IOE: " + e.getMessage() + "... " + e.toString());
        }
    }

    public void srvPart(Service who, String where, String why)
    // Have service leave a channel
    {
        try {
            Outgoing(":" + who.getname() + " PART " + where + " :" + why);
            Generic.chanPart(where, who.getname());
        } catch (IOException e) {
            Logging.error("PROTOCOL", "Got IOException while sending a command.");
            Logging.error("PROTOCOL", "IOE: " + e.getMessage() + "... " + e.toString());
        }
    }

    public void outPRVMSG(Service me, String coru, String msg)
    // Send a privmsg to a channel or user
    {
        try {
            Outgoing(":" + me.getname() + " PRIVMSG " + coru + " :" + msg);
        } catch (IOException e) {
            Logging.error("PROTOCOL", "Got IOException while sending a command.");
            Logging.error("PROTOCOL", "IOE: " + e.getMessage() + "... " + e.toString());
        }
    }

    public void outNOTICE(Service me, String coru, String msg)
    // Send a notice to a channel or a user
    {
        try {
            Outgoing(":" + me.getname() + " NOTICE " + coru + " :" + msg);
        } catch (IOException e) {
            Logging.error("PROTOCOL", "Got IOException while sending a command.");
            Logging.error("PROTOCOL", "IOE: " + e.getMessage() + "... " + e.toString());
        }
    }

    public void outQUIT(Service me, String msg)
    // Quit a service
    {
        try {
            Outgoing(":" + me.getname() + " QUIT :" + msg);
        } catch (IOException e) {
            Logging.error("PROTOCOL", "Got IOException while sending a command.");
            Logging.error("PROTOCOL", "IOE: " + e.getMessage() + "... " + e.toString());
        }
    }

    public void outSETMODE(Service me, String channel, String mode, String who)
    // Set a mode on a channel. If we're not ulined, we'll have to force it...
    {
        try {
            Outgoing(":" + me.getname() + " MODE " + channel + " " + mode + " " + who);
        } catch (IOException e) {
            Logging.error("PROTOCOL", "Got IOException while sending a command.");
            Logging.error("PROTOCOL", "IOE: " + e.getMessage() + "... " + e.toString());
        }
    }

    public void outKILL(Service me, String who, String why)
    // Kill someone.
    {
        try {
            Outgoing(":" + me.getname() + " KILL " + who + " :" + Configuration.Config.get("hostname") + "!services!" + me.getname() + "! (" + why + ")");
            Generic.nickSignOff(who.toLowerCase());
        } catch (IOException e) {
            Logging.error("PROTOCOL", "Got IOException while sending a command.");
            Logging.error("PROTOCOL", "IOE: " + e.getMessage() + "... " + e.toString());
        }
    }

    public void outPART(Service me, String chan, String reason)
    // Have services leave a channel
    {
        try {
            Outgoing(":" + me.getname() + " PART " + chan + " :" + reason);
            Generic.chanPart(chan, me.getname());
        } catch (IOException e) {
            Logging.error("PROTOCOL", "Got IOException while sending a command.");
            Logging.error("PROTOCOL", "IOE: " + e.getMessage() + "... " + e.toString());
        }
    }

    public void outGLINE(Service me, Client who, String why)
    // Add an AKILL
    {
        try {
            Outgoing(":" + Configuration.Config.get("hostname") + " TKL + G " + who.ident + " " + who.host + me.getname() + " 0 " + util.getTS() + " :" + why);
        } catch (IOException e) {
            Logging.error("PROTOCOL", "Got IOException while sending a command.");
            Logging.error("PROTOCOL", "IOE: " + e.getMessage() + "... " + e.toString());
        }
    }

    public void outGLINE(Service me, String mask, long duration, String why)
    // Add an AKILL on an arbitrary mask
    {
        try {
            String id;
            String host;
            String [] t = mask.split("@");
            id = t[0];
            host = t[1];
            Outgoing(":" + Configuration.Config.get("hostname") + " TKL + G " + id + " " + host + " " + me.getname() + " " + (util.getTS() + duration) + " " + util.getTS() + " :" + why);
        } catch (IOException e) {
            Logging.error("PROTOCOL", "Got IOException while sending a command.");
            Logging.error("PROTOCOL", "IOE: " + e.getMessage() + "... " + e.toString());
        }
    }

    public void outUNGLINE(Service me, String mask)
    // Remove an AKILL
    {
        try {
            String id;
            String host;
            String [] t = mask.split("@");
            id = t[0];
            host = t[1];
            Outgoing("TKL - " + id + " " + host + " " + me.getname());
        } catch (IOException e) {
            Logging.error("PROTOCOL", "Got IOException while sending a command.");
            Logging.error("PROTOCOL", "IOE: " + e.getMessage() + "... " + e.toString());
        }
    }

    public void srvSetAuthed(Service me, String who, Long svsid)
    // Let other servers know that this user is authed
    {
        outMODE(me, Generic.Users.get(who.toLowerCase()), "+r", "");
    }

    public void srvUnSetAuthed(Service me, String who)
    // Un-auth a user
    {
        outMODE(me, Generic.Users.get(who.toLowerCase()), "-r", "");
    }


    public void outKICK(Service me, String who, String where, String why)
    // Kick someone from a channel
    {
        try {
            Outgoing(":" + me.getname() + " KICK " + where + " " + who + " :" + why);
        } catch (IOException e) {
            Logging.error("PROTOCOL", "Got IOException while sending a command.");
            Logging.error("PROTOCOL", "IOE: " + e.getMessage() + "... " + e.toString());
        }
    }

    public void outINVITE(Service me, String who, String where)
    // Invite someone somewhere
    {
        try {
            Outgoing(":" + me.getname() + " INVITE " + who + " " + where);
        } catch (IOException e) {
            Logging.error("PROTOCOL", "Got IOException while sending a command.");
            Logging.error("PROTOCOL", "IOE: " + e.getMessage() + "... " + e.toString());
        }
    }

    public void outMODE(Service me, Client who, String what, String more) {
        try {
            Outgoing("SVSMODE " + who.uid + " " + what + " " + more);
            who.modes.applyChanges(what + " " + more);
        } catch (IOException e) {
            Logging.error("PROTOCOL", "Got IOException while sending a command.");
            Logging.error("PROTOCOL", "IOE: " + e.getMessage() + "... " + e.toString());
        }
    }

    public void outTOPIC(Service me, String where, String what) {

    }

}
