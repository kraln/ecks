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
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class cots implements Protocol {
    BufferedWriter out; // where we send our irc commands to
    States myState; // what the current state of connectivity is
    boolean wasOnline; // helps us determine if we were online (split or first time connect)
    String myUplink; // what our uplink thinks it is
    long connected; // what time we connected
    int nCount; // for keeping track of notices at the very beginning of the connection
    String modeargs = ""; // what chanel modes are allowed to have arguments in this protocol (dynamic in cots)
    Map<String, String> xlate;
    Map<Character, Character> mPrefix;

    public long getWhenStarted() {
        return connected;
    }

    public cots() {
        myState = States.S_DISCONNECTED; // we start out disconnected
        xlate = new HashMap<String, String>();
        mPrefix = new HashMap<Character, Character>();
        wasOnline = false;
        nCount = 0;
    }

    public String getModeArgs() {
        return modeargs;
    }

    public Map<Character, Character> getPrefixMap() {
        return mPrefix;
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

        if (hasSource)
            source = o2n(source);


        Delegate(command, hasSource, source, tokens, hasExtArg, (hasExtArg ? halves[1] : null));

    }

    public void Outgoing(String what) throws IOException {
        out.write(what);
        out.newLine();
        Logging.raw(what, false);
        out.flush();
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    void Delegate(String cmd, Boolean hasSource, String source, String[] tokens, Boolean hasargs, String args) {
        try {
            if (cmd.equals("PING")) {                                                                            // PING

                outPong();

            } else if (cmd.equals("NOTICE")) {                                                                 // NOTICE

                if (myState == States.S_HASBUFFERS) {
                    outHandshake(); // send our half of the server information handshake
                    Logging.info("PROTOCOL", "Sending Handshake...");
                }

            } else if (cmd.equals("CHANMODE")) {                                                             // CHANMODE
                if (!tokens[2].equals("D")) // if we're talking about modes that take arguments
                    if (!modeargs.contains(tokens[1]))
                        modeargs = modeargs + tokens[1];
            } else if (cmd.equals("BURST")) {                                                                   // BURST

                if (tokens[1].equals("R")) // asking for burst
                {
                    myState = States.S_SERVICES;
                    Generic.BringServicesOnline();
                    wasOnline = true;
                    Outgoing("BURST E");
                    myState = States.S_BURSTING;
                    Outgoing("BURST R");
                }

                if (tokens[1].equals("E")) // done with burst
                {
                    myState = States.S_ONLINE;
                    Logging.info("PROTOCOL", "Ecks Services " + util.getVersion() + " operational. " + util.getTS());
                    connected = Long.parseLong(util.getTS());
                }
            } else if (cmd.equals("NOTE")) {                                                                     // NOTE
                Logging.info("PROTOCOL", "NOTE: " + args);
            } else if (cmd.equals("PREFIX")) {                                                                 // PREFIX

                String[] blah = args.substring(1).split("\\)");
                for (int i = 0; i < blah[0].length(); i++)
                    mPrefix.put(blah[1].charAt(i), blah[0].charAt(i));

            } else if (cmd.equals("USER")) {                                                                     // USER

                if (hasargs)
                    nickSignOn(tokens, args);
                else
                    nickSignOn(tokens, tokens[16]); // voodoo for lazy :

            } else if (cmd.equals("NICK")) {                                                                     // NICK

                Generic.nickRename(source, tokens[2], Long.parseLong(util.getTS()));

            } else if (cmd.equals("KICK")) {                                                                     // KICK

                Generic.nickGotKicked(o2n(tokens[3]), o2n(tokens[2]));

            } else if (cmd.equals("SERVER")) {                                                                 // SERVER

                myUplink = tokens[1];
                xlate.put(tokens[2], tokens[1]);

            } else if (cmd.equals("KILL")) {                                                                     // KILL

                if (tokens[2].equals(Configuration.Config.get("hostname"))) // we're getting killed?
                {
                    Logging.error("PROTOCOL", "We've done something wrong. Bailing.");
                    main.goGracefullyIntoTheNight();
                }
                Generic.nickGotKilled(o2n(tokens[2]));

            } else if (cmd.equals("AWAY")) {                                                                     // AWAY

                // goggles. maybe we should track these at some point.

            } else if (cmd.equals("PART")) {                                                                     // PART

                // :SOURCE PART #CHANNEL
                Generic.chanPart(tokens[2], source);

            } else if (cmd.equals("QUIT")) {                                                                     // QUIT

                if (myUplink.equals(o2n(source))) {
                    main.goGracefullyIntoTheNight();
                    return;
                }
                Generic.nickSignOff(source);

            } else if (cmd.equals("MODE")) {                                                                     // MODE
                // :SOURCE MODE TARGET :CHANGE
                if (!hasargs)
                    args = tokens[3];
                if (o2n(tokens[2]).startsWith("#")) { // is a channel mode
                    Generic.modeChan(o2n(tokens[2]), args);
                } else {                         // user mode has changed
                    Generic.modeUser(o2n(tokens[2]), args);
                }
            } else if (cmd.equals("SMODE")) {                                                                   // SMODE
                // SMODE ts channel mode
                Generic.modeChan(tokens[3], args);

            } else if (cmd.equals("PRIVMSG")) {                                                               // PRIVMSG

                //:SOURCE PRIVMSG TARGET :MESSAGE
                Hooks.hook(Hooks.Events.E_PRIVMSG, source, o2n(tokens[2]), args);

            } else if (cmd.equals("JOIN")) {                                                                     // JOIN
                if (tokens[2].equals("0")) // partall
                {
                    Generic.Users.get(source).chans.clear();
                } else {

                    // :SOURCE JOIN #CHANNEL MODE
                    Generic.chanJoin(
                            0,
                            tokens[2],
                            source
                    );
                }
            } else if (cmd.equals("SJOIN")) {                                                                   // SJOIN

                // :SOURCE SJOIN TS #CHANNEL :PREFIXUSERS
                String[] temp = args.split(" ");
                for (int i = 0; i < temp.length; i++) {
                    String z = "";
                    for (Map.Entry<Character, Character> e : mPrefix.entrySet())  // remove prefix, xlate, put it back...
                        if (temp[i].startsWith(e.getKey().toString())) {
                            z = temp[i].substring(0, 1);
                            temp[i] = temp[i].substring(1);
                        }
                    temp[i] = o2n(temp[i]);
                    temp[i] = z + temp[i];
                }
                Generic.chanBurst(
                        Integer.parseInt(tokens[2]),
                        tokens[3],
                        "",
                        temp
                );

            } else if (cmd.equals("TOPIC")) {                                                                   // TOPIC
                // :SOURCE TOPIC #CHANNAME :NEWTOPIC
                Generic.chanTopic(0, tokens[2], args);


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
        // cots specific...
        // :SOURCE user oid ts name user host realhost ip rport lport mode machine info nickts idle :realname
        // 0       1    2   3  4    5    6    7        8  9     10    11   12      13   14     15   args

        // generic...
        // uid hops signon modes ident host althost uplink svsid numericip realname nickid
        // 2   x    3      11    5     7    6       0      x     8         args       x

        xlate.put(tokens[2], tokens[4]);
        try {
            String[] newargs = {null,
                    tokens[4],
                    "0",
                    tokens[3],
                    tokens[11],
                    tokens[5],
                    tokens[7],
                    tokens[6],
                    tokens[0],
                    "0",
                    String.valueOf(util.ip2long(InetAddress.getByName(tokens[8]))),
                    args,
                    null};

            Generic.nickSignOn(newargs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void outPong() throws IOException {
        Outgoing("PONG :" + util.getTS());
        Logging.info("PROTOCOL", "Server pinged...");
    }

    void outHandshake() throws IOException // very much cots specific
    {
        Outgoing("NOTICE & :\u0001VERSION  " + util.getVersion() + " \u0001"); // version reply
        Outgoing("AUTH S-A-D * I " + Configuration.Config.get("password")); // auth/password
        Outgoing("SERVER " + Configuration.Config.get("hostname") + " " + Configuration.Config.get("numeric") + " " + util.getTS() + " :Ecks Services " + util.getVersion()); // send our server info
    }

    public void srvIntroduce(Service whom)
    // Introduce a service to the network
    {
        //:SOURCE user oid ts name user host realhost ip rport lport mode machine info nickts idle :realname
        try {
            xlate.put("0" + util.paddingString(whom.getname(), 7, '0', true), whom.getname());
            String o = ":" + Configuration.Config.get("hostname")
                    + " USER 0"
                    + util.paddingString(whom.getname(), 7, '0', true) + " "
                    + util.getTS() + " "
                    + whom.getname()
                    + " services "
                    + Configuration.Config.get("hostname") + " "
                    + Configuration.Config.get("hostname")
                    + " 0 "
                    + " * * "
                    + " +ior "
                    + " * * "
                    + util.getTS() + " "
                    + util.getTS() + " "
                    + ":Network Services";
            Outgoing(o);
            String[] tokens = o.split(" ");
            String[] newargs = {null,
                    tokens[4],
                    "0",
                    tokens[3],
                    tokens[11],
                    tokens[5],
                    tokens[7],
                    tokens[6],
                    tokens[0],
                    "0",
                    tokens[8],
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
            Outgoing(":" + who.getname() + " JOIN " + where);
            Outgoing(":" + who.getname() + " MODE :" + modes);
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
            Outgoing(":" + Configuration.Config.get("hostname") + " AKILL " + who.host + " " + who.ident + " 0 " + me.getname() + " " + util.getTS() + " :" + why);
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
            String[] t = mask.split("@");
            id = t[0];
            host = t[1];
            Outgoing(":" + Configuration.Config.get("hostname") + " AKILL " + host + " " + id + " " + duration + " " + me.getname() + " " + util.getTS() + " :" + why);
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
            String[] t = mask.split("@");
            id = t[0];
            host = t[1];
            Outgoing("RAKILL " + host + " " + id + " " + me.getname());
        } catch (IOException e) {
            Logging.error("PROTOCOL", "Got IOException while sending a command.");
            Logging.error("PROTOCOL", "IOE: " + e.getMessage() + "... " + e.toString());
        }
    }

    public void srvSetAuthed(Service me, String who, Long svsid)
    // Let other servers know that this user is authed
    {
        outMODE(me, Generic.Users.get(who.toLowerCase()), "+rd", svsid.toString());
    }

    public void srvUnSetAuthed(Service me, String who)
    // Un-auth a user
    {
        outMODE(me, Generic.Users.get(who.toLowerCase()), "-rd", "");
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
            Outgoing("SVSMODE " + who.uid + " " + who.signon + " " + what + " " + more);
            who.modes.applyChanges(what + " " + more);
        } catch (IOException e) {
            Logging.error("PROTOCOL", "Got IOException while sending a command.");
            Logging.error("PROTOCOL", "IOE: " + e.getMessage() + "... " + e.toString());
        }
    }

    public void outTOPIC(Service me, String where, String what) {

    }

    String o2n(String what) {
        if (what.startsWith("0")) // is an oid
            return xlate.get(what);
        return what;
    }

}
