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

import ecks.Configuration;
import ecks.Hooks.Hooks;
import ecks.Logging;
import ecks.Utility.Client;
import ecks.main;
import ecks.services.Service;
import ecks.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class inspircd implements Protocol {
    BufferedWriter out;
    States myState;
    boolean wasOnline;
    String myUplink;
    long connected;
    final String modeargs = "ovqahblkIE";

    public long getWhenStarted() {
        return connected;
    }

    public Map<Character, Character> getPrefixMap() {
        Map<Character, Character> z = new HashMap<Character, Character>();
        z.put('@', 'o');
        z.put('+', 'v');
        z.put('~', 'q');
        z.put('&', 'a');
        z.put('%', 'h');
        return z;
    }

    public inspircd() {
        myState = States.S_DISCONNECTED; // we start out disconnected
        wasOnline = false;
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
        out = o;
        myState = States.S_HASBUFFERS;
        Logging.info("PROTOCOL", "Waiting for server...");
    }

    public void Incoming(String line) {

        Logging.raw(line, true); // raw lines get logged here

        if (line == null) { // this should never, ever happen.
            Logging.error("PROTOCOL", "Got NULL incoming line!");
            main.goGracefullyIntoTheNight();
            return;
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
            } else if (cmd.equals("ENDBURST")) {                                                             // ENDBURST
                Logging.info("PROTOCOL", "Burst completed.");
                myState = States.S_ONLINE;
                Logging.info("PROTOCOL", "Ecks Services " + util.getVersion() + " operational. " + util.getTS());
                connected = Long.parseLong(util.getTS());

            } else if (cmd.equals("CAPAB")) {                                                                   // CAPAB
                // nothing interesting gets sent in capab, yet
                if (tokens[1].equals("END")) {
                    Logging.info("PROTOCOL", "Sending Handshake...");
                    outHandshake();
                    myState = States.S_SERVICES;
                    Generic.BringServicesOnline();
                    myState = States.S_BURSTING;
                }
            } else if (cmd.equals("NOTICE")) {                                                                 // NOTICE
            } else if (cmd.equals("GNOTICE")) {                                                               // GNOTICE
            } else if (cmd.equals("METADATA")) {                                                             // METADATA
                // :nethack.kraln.com METADATA Kuja accountname :Kuja
                if (tokens[3].equals("accountname")) {
                    Logging.info("PROTOCOL", "User " + tokens[2] + " was previously authed.");
                    if (Configuration.getSvc().containsKey(Configuration.authservice)) // if we have an auth service
                    {
                        Logging.info("PROTOCOL", "Attempting re-entry...");
                        Configuration.getSvc().get(Configuration.authservice).handle(tokens[2].toLowerCase(), "service", "reauth " + args + " " + util.getTS());
                    }
                }
            } else if (cmd.equals("NICK")) {                                                                     // NICK

                if (tokens.length > 7) {
                    nickSignOn(tokens, args);
                } else { // just a rename
                    Generic.nickRename(source, tokens[2], 0);
                }
            } else if (cmd.equals("KICK")) {                                                                     // KICK
                Generic.nickGotKicked(tokens[3], tokens[2]);
            } else if (cmd.equals("SERVER")) {                                                                 // SERVER
                myUplink = tokens[1];
            } else if (cmd.equals("KILL")) {                                                                     // KILL
                Generic.nickGotKilled(tokens[2]);
            } else if (cmd.equals("AWAY")) {                                                                     // AWAY
            } else if (cmd.equals("PART")) {                                                                     // PART
                Generic.chanPart(tokens[2], source);
            } else if (cmd.equals("QUIT")) {                                                                     // QUIT
                Generic.nickSignOff(source);
            } else if (cmd.equals("OPERTYPE")) {                                                             // OPERTYPE
                Generic.modeUser(source, "+o"); // lame...
            } else if (cmd.equals("FMODE")) {                                                                   // FMODE

                String modestring;
                if (tokens[2].startsWith("#")) { // is a channel mode
                    modestring = tokens[4];
                    if (tokens.length > 4)
                        for (int i = 5; i < tokens.length; i++)
                            modestring += " " + tokens[i];
                    Generic.modeChan(tokens[2], modestring);
                } else {                         // user mode has changed
                    Generic.modeUser(tokens[2], tokens[5]);
                }

            } else if (cmd.equals("PRIVMSG")) {                                                               // PRIVMSG
                //:SOURCE PRIVMSG TARGET :MESSAGE
                Hooks.hook(Hooks.Events.E_PRIVMSG, source, tokens[2], args);
            } else if (cmd.equals("JOIN")) {                                                                     // JOIN
                Generic.chanJoin(
                        Integer.parseInt(tokens[3]),
                        tokens[2],
                        source
                );
            } else if (cmd.equals("FJOIN")) {                                                                   // FJOIN
                // :SOURCE FJOIN #CHANNEL TS [MODE,USER]
                if (!Generic.Users.containsKey(source.toLowerCase())) { // server is introducing channel
                    String m = "";
                    String n[];
                    if (hasargs) {
                        n = args.split(" ");
                        String z[] = n;

                        int i = -1;
                        for (String y : z) {
                            String c[] = y.split(",");
                            m = m + c[0];
                            n[++i] = c[1];
                        }
                    } else {
                        n = tokens;
                        String z[] = n;

                        String c[] = z[4].split(",");
                        m = m + c[0];
                        n = new String[]{c[1]};
                    }

                    try {
                        Generic.chanBurst(
                                Integer.parseInt(tokens[3]),
                                tokens[2],
                                m,
                                n
                        );

                    } catch (NullPointerException NPE) {
                        System.out.println(tokens[3] + " " + tokens[2]);
                        NPE.printStackTrace();

                    }
                    // todo: fix null pointer here.

                } else { // just a user joining
                    Generic.chanJoin(
                            Integer.parseInt(tokens[2]),
                            tokens[3],
                            source
                    );
                }

            } else if (cmd.equals("TOPIC")) {                                                                   // TOPIC
                Generic.chanTopic(0, tokens[2], args);
            } else {                                                                                          // UNKNOWN
                Logging.warn("PROTOCOL", "Unsupported command: " + cmd);
            }
        } catch (IOException ioe) {
            Logging.error("PROTOCOL", "Got IOException while delegating command: " + cmd);
            Logging.error("PROTOCOL", "IOE Claims: " + ioe.getMessage());
        }
    }


    void nickSignOn(String[] tokens, String args) {
        // inspircd specific...
        // :SOURCE NICK <timestamp> <nick> <hostname> <displayed-hostname> <ident> +<modes> <ip> :<gecos>
        // 0       1    2           3       4         5                    6       7        8

        // generic...
        // uid hops signon modes ident host althost uplink svsid numericip realname nickid
        String[] newargs = {null,
                tokens[3],
                "1",
                tokens[2],
                tokens[7],
                tokens[6],
                tokens[4],
                tokens[5],
                tokens[0],
                "0",
                String.valueOf(util.ip2long(tokens[8])),
                args,
                null};
        Generic.nickSignOn(newargs);
    }

    void outPong() throws IOException {
        Outgoing("PONG :" + util.getTS());
        Logging.info("PROTOCOL", "Server pinged...");
    }

    void outHandshake() throws IOException // very much bahamut specific
    {
        Outgoing("SERVER " + Configuration.Config.get("hostname") + " " + Configuration.Config.get("password") + " 0 :Ecks Services " + util.getVersion()); // send our server info
        Outgoing("BURST " + util.getTS()); // send our ts version and offset
        Outgoing(":" + Configuration.Config.get("hostname") + " VERSION :Ecks Services " + util.getVersion());
    }

    public void srvIntroduce(Service whom)
    // Introduce a service to the network
    {
        //server NICK <timestamp> <nick> <hostname> <displayed-hostname> <ident> +<modes> <ip> :<gecos>
        try {
            String o = "NICK "
                    + util.getTS() + " "
                    + whom.getname() + " "
                    + Configuration.Config.get("hostname") + " "
                    + Configuration.Config.get("hostname") + " "
                    + whom.getname()
                    + " +ir "
                    + "0.0.0.0 :Network Services";
            Outgoing(":" + Configuration.Config.get("hostname") + " " + o);
            String[] tokens = o.split(" ");

            String[] newargs = {null,
                    tokens[2],
                    "1",
                    tokens[1],
                    tokens[6],
                    tokens[5],
                    tokens[3],
                    tokens[4],
                    "", // should be source
                    "0",
                    "0",
                    "0",
                    null};
            Generic.nickSignOn(newargs);
            Outgoing(":" + whom.getname() + " OPERTYPE :network service");

        } catch (IOException e) {
            Logging.error("PROTOCOL", "Got IOException while introducing service: " + whom.getname());
            Logging.error("PROTOCOL", "IOE Claims: " + e.getMessage());
        }
    }

    public void srvJoin(Service who, String where, String modes)
    // Have service join a channel
    {
        try {
            Outgoing(":" + Configuration.Config.get("hostname") + " FJOIN " + where + " " + util.getTS() + " :@," + who.getname());
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
            this.setState(States.S_DISCONNECTING);
            Logging.error("PROTOCOL", "Got IOException while sending a command.");
            Logging.error("PROTOCOL", "IOE: " + e.getMessage() + "... " + e.toString());
            main.goGracefullyIntoTheNight();
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

    public void outSETMODE(Service me, String channel, String mode, String who) {
        try {
            Outgoing(":" + me.getname() + " MODE " + channel + " " + mode + " " + who);
        } catch (IOException e) {
            Logging.error("PROTOCOL", "Got IOException while sending a command.");
            Logging.error("PROTOCOL", "IOE: " + e.getMessage() + "... " + e.toString());
        }

    }

    public void outKILL(Service me, String who, String why) {
        try {
            Outgoing(":" + me.getname() + " KILL " + who + " :" + Configuration.Config.get("hostname") + "!services!" + me.getname() + "! (" + why + ")");
            Generic.nickSignOff(who.toLowerCase());
        } catch (IOException e) {
            Logging.error("PROTOCOL", "Got IOException while sending a command.");
            Logging.error("PROTOCOL", "IOE: " + e.getMessage() + "... " + e.toString());
        }
    }

    public void outPART(Service me, String chan, String reason) {
        try {
            Outgoing(":" + me.getname() + " PART " + chan + " :" + reason);
            Generic.chanPart(chan, me.getname());
        } catch (IOException e) {
            Logging.error("PROTOCOL", "Got IOException while sending a command.");
            Logging.error("PROTOCOL", "IOE: " + e.getMessage() + "... " + e.toString());
        }
    }

    public void outGLINE(Service me, Client who, String why) {
        try {
            Outgoing(":" + Configuration.Config.get("hostname") + " AKILL " + who.host + " " + who.ident + " 3600 " + me.getname() + " " + util.getTS() + " :" + why);
        } catch (IOException e) {
            Logging.error("PROTOCOL", "Got IOException while sending a command.");
            Logging.error("PROTOCOL", "IOE: " + e.getMessage() + "... " + e.toString());
        }
    }

    public void outGLINE(Service me, String mask, long duration, String why) {
        try {
            String id;
            String host;
            String[] t = mask.split("@");
            id = t[0];
            host = t[1];
            Outgoing(":" + Configuration.Config.get("hostname") + " AKILL " + host + " " + id + " " + duration + " " + me.getname() + " " + util.getTS() + " :" + why);
        } catch (IOException e) {
            Logging.error("PROTOCOL", "Got IOException while sending a command.");
            Logging.error("PROTOCOL", "IOE: " + e.getMessage() + "... " + e.toString());
        }
    }

    public void outUNGLINE(Service me, String mask) {
        try {
            String id;
            String host;
            String[] t = mask.split("@");
            id = t[0];
            host = t[1];
            Outgoing("RAKILL " + host + " " + id + " " + me.getname());
        } catch (IOException e) {
            Logging.error("PROTOCOL", "Got IOException while sending a command.");
            Logging.error("PROTOCOL", "IOE: " + e.getMessage() + "... " + e.toString());
        }
    }

    public void srvSetAuthed(Service me, String who, Long svsid) {
        outMODE(me, Generic.Users.get(who.toLowerCase()), "+r", "");
        try {
            Outgoing(":" + Configuration.Config.get("hostname") + " METADATA " + who.toLowerCase() + " accountname :" + Generic.Users.get(who.toLowerCase()).svsid);
        } catch (IOException e) {
            Logging.error("PROTOCOL", "Got IOException while sending a command.");
            Logging.error("PROTOCOL", "IOE: " + e.getMessage() + "... " + e.toString());
        }
    }

    public void srvUnSetAuthed(Service me, String who) {
        outMODE(me, Generic.Users.get(who.toLowerCase()), "-r", "");
        try {
            Outgoing(":" + Configuration.Config.get("hostname") + " METADATA " + who.toLowerCase() + " accountname :\0");
        } catch (IOException e) {
            Logging.error("PROTOCOL", "Got IOException while sending a command.");
            Logging.error("PROTOCOL", "IOE: " + e.getMessage() + "... " + e.toString());
        }
    }


    public void outKICK(Service me, String who, String where, String why) {
        try {
            Outgoing(":" + me.getname() + " KICK " + where + " " + who + " :" + why);
        } catch (IOException e) {
            Logging.error("PROTOCOL", "Got IOException while sending a command.");
            Logging.error("PROTOCOL", "IOE: " + e.getMessage() + "... " + e.toString());
        }
    }

    public void outINVITE(Service me, String who, String where) {
        try {
            Outgoing(":" + me.getname() + " INVITE " + who + " " + where);
        } catch (IOException e) {
            Logging.error("PROTOCOL", "Got IOException while sending a command.");
            Logging.error("PROTOCOL", "IOE: " + e.getMessage() + "... " + e.toString());
        }
    }

    public void outMODE(Service me, Client who, String what, String more) {
        try {
            // :<source server or nickname> MODE <target> <modes and parameters>
            Outgoing(":" + me.getname() + " MODE " + who.uid + " " + what + " " + more);
            who.modes.applyChanges(what + " " + more);
        } catch (IOException e) {
            Logging.error("PROTOCOL", "Got IOException while sending a command.");
            Logging.error("PROTOCOL", "IOE: " + e.getMessage() + "... " + e.toString());
        }
    }

    public void outTOPIC(Service me, String where, String what) {
        try {
            Outgoing(":" + me.getname() + " TOPIC " + where + " :" + what);
        } catch (IOException e) {
            Logging.error("PROTOCOL", "Got IOException while sending a command.");
            Logging.error("PROTOCOL", "IOE: " + e.getMessage() + "... " + e.toString());
        }
    }

}
