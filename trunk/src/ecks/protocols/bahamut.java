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

import ecks.*;
import ecks.services.Service;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

public class bahamut extends bProtocol {
    public void Incoming(String line) {
        if (line == null) {
            Logging.error("PROTOCOL", "Got NULL incoming line!");
            main.goGracefullyIntoTheNight();
        }

        Logging.raw(line,true); // if we're logging raw, this would be the place to deal wif it

        if (line.startsWith("ERROR")) // oh no. bail bail bail
        {
            Logging.error("PROTOCOL", "Recieved Error.");
            Logging.warn("PROTOCOL RAW", line);
            main.goGracefullyIntoTheNight();
        }

        if (line.startsWith("PING")) // we pong no matter what state we're in
        {
            try {
                Outgoing("PONG :" + (System.currentTimeMillis() / 1000)); // reply
                Logging.info("PROTOCOL", "Server ping-ponged...");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (line.startsWith("NICK")) // this is a bursting nick, or just a new client
        // NICK phil 2 1163265829 +i ~u3435591 cpc3-nthc11-0-0-cust625.nrth.cable.ntl.com King.GamesNET.net svsid numip :phil
        {
            try {
            String nick = "", host = "", ident = "", gecos = "", uplink = "";
            nick = line.split(" ")[1];
            host = line.split(" ")[6];
            ident = line.split(" ")[5];
            gecos = line.split(" :")[1];
            uplink = line.split(" ")[7];
            config.Database.Users.put(nick.toLowerCase(), new Client(nick, uplink, ident, host, gecos));
            Logging.info("PROTOCOL", "Added new client: " +  nick);
            } catch (Exception e)
            {
                e.printStackTrace();
                Logging.error("PROTOCOL", "Got exception adding new client: " + e.getMessage());
                Logging.warn("PROTOCOL RAW", line);
            }

        }
        if (line.split(" ")[1].equals("PART"))  // someone is leaving a channel, probably causing the channel to close.
        // :cathy PART #Cheers
        {
            Client c;
            Channel chan;
            String cname, channame;

            cname = line.split(" ")[0].substring(1).toLowerCase();
            channame = line.split(" ")[2];

            c = config.Database.Users.get(cname.toLowerCase()); // this should never, ever fail if we're sync'd

            if (config.Database.Channels.containsKey(channame.toLowerCase())) // it had better...
            {
                chan = config.Database.Channels.get(channame.toLowerCase());

                try {
                    chan.clientmodes.remove(c); // remove user
                } catch (NullPointerException npe) {
                    npe.printStackTrace();
                    Logging.error("PROTOCOL", "Caused NPE - tried to delete " + c.toString() + " from the following:");
                    for (Client ch : chan.clientmodes.keySet()) {
                        Logging.error("PROTOCOL", "    " + ch.toString());
                    }
                }

                if (chan.clientmodes.size() == 0) // no more users in this channel, it goes boom
                    config.Database.Channels.remove(channame.toLowerCase());

                for (String s : config.Services.keySet()) {
                    if (config.Services.get(s).getCommands().containsKey("desync")) // if we care about userparts
                    {
                        config.Services.get(s).handle
                                (line.split(" ")[0].substring(1).toLowerCase(), channame, "desync silent");
                    }
                }
            }
        }

        if (line.split(" ")[1].equals("SJOIN")) // this is a bursting channel, or a user joining a channel
        // :US.Hub.GamesNET.net SJOIN 1 #gamesnet +st :+Sneddy Zeet art Kuja @robp Gary Heru`ur nocebo-king @aGGraVaTOr[NeT] @ChanServ +nocebo-vulcan
        {
            String name, modes;
            int time;
            Map<Client, String> cmodes = new HashMap<Client, String>();

            modes = "";
            time = Integer.parseInt(line.split(" ")[2]);
            name = line.split(" ")[3];
            String tusers[];

            if (line.split(" ")[0].contains(".")) // if we're an uplink
            {
                modes = line.split(" ")[4];
                tusers = line.split(" :")[1].split(" ");
                for (int i = 0; i < tusers.length; i++) {
                    String tmode = "";
                    if (tusers[i].startsWith("@")) {
                        tmode += "o";
                        tusers[i] = tusers[i].substring(1);
                    }
                    if (tusers[i].startsWith("+")) {
                        tmode += "v";
                        tusers[i] = tusers[i].substring(1);
                    }
                    cmodes.put(config.Database.Users.get(tusers[i].toLowerCase()), tmode);
                }
                config.Database.Channels.put(name.toLowerCase(), new Channel(time, name, modes, cmodes));
                Logging.info("PROTOCOL", "Added new channel: " +  name);
            } else { // just a user joining
                if (config.Database.Channels.containsKey(name.toLowerCase())) // if this check fails we fail somewhere
                {
                    cmodes = config.Database.Channels.get(name.toLowerCase()).clientmodes;
                    int t = config.Database.Channels.get(name.toLowerCase()).ts;
                    String m = config.Database.Channels.get(name.toLowerCase()).modes;
                    cmodes.put(config.Database.Users.get(line.split(" ")[0].substring(1).toLowerCase()), "");
                    config.Database.Channels.remove(name.toLowerCase());
                    config.Database.Channels.put(name.toLowerCase(), new Channel(t, name, m, cmodes));

                    for (String s : config.Services.keySet()) {
                        if (config.Services.get(s).getCommands().containsKey("sync")) // if we care about userjoins
                        {
                            config.Services.get(s).handle
                                    (line.split(" ")[0].substring(1).toLowerCase(), name, "sync silent");
                        }
                    }
                }
            }

        }
        if (line.split(" ")[1].equals("NICK")) // just a rename
        {
            try {
            config.Database.Users.put(line.split(" ")[2].toLowerCase(), config.Database.Users.get(line.split(" ")[0].substring(1).toLowerCase()));
            config.Database.Users.get(line.split(" ")[2].toLowerCase()).uid = line.split(" ")[2];
            config.Database.Users.remove(line.split(" ")[0].substring(1).toLowerCase());
            } catch (NullPointerException NPE)
            {
                NPE.printStackTrace();
                Logging.warn("PROTOCOL", "Caused NPE during rename");
                Logging.info("PROTOCOL RAW", line);
            }
        }

        if (line.split(" ")[1].equals("QUIT")) // farvel fair user
        //:O35012812 QUIT :Client Quit
        {
            config.Database.Users.remove(line.split(" ")[0].substring(1).toLowerCase());
        }

        if (line.split(" ")[1].equals("KILL")) // farvel asshat
        //:nocebo KILL SrvChan :US.Hub.GamesNET.net!nocebo.!nocebo!nocebo (bye)
        {
            if (config.Services.containsKey(line.split(" ")[2].toLowerCase())) {
                // they've killed one of us. bad idea.
                Logging.info("PROTOCOL", "Service was killed!");
                config.Services.get(line.split(" ")[2].toLowerCase()).introduce();
            } else config.Database.Users.remove(line.split(" ")[2].toLowerCase());
        }
        // :Kuja KICK #debug ChanServ :feck off // someone's getting kicked - do we care?
        if (line.split(" ")[1].equals("KICK")) // we only care if this is us, on non-ulined servers
        {
            if (config.Services.containsKey(line.split(" ")[3].toLowerCase()))
            {    // they've kicked one of us. bad idea.
                SJoin(line.split(" ")[3].toLowerCase(), line.split(" ")[2].toLowerCase(), "+nt");
                Logging.info("PROTOCOL", "Service was kicked!");
                forcemode(config.Services.get(line.split(" ")[3].toLowerCase()), line.split(" ")[2].toLowerCase(), "+o", line.split(" ")[3].toLowerCase());
            }
        }

        // state machine goes here
        if (state == 1) // we have a buffer, but we're not yet connected
        {
            if (line.contains("NOTICE AUTH :*** Couldn't look up your hostname") || line.contains("NOTICE AUTH :*** Found your hostname") || line.contains("NOTICE AUTH :*** No Ident response"))
            {
                // server is done with it's first half. we're handshaking now
                try {
                    Outgoing("CAPAB :QS TSORA SERVICES"); // We support QS and TSORA
                    Outgoing("PASS " + config.Config.get("password") + " :TS"); // send our connection password
                    Outgoing("SERVER " + config.Config.get("hostname") + " 1 " + config.Config.get("numeric") +
                            " :Services ecks"); // send our server info
                    Outgoing("SVINFO 6 3 1 :" + (System.currentTimeMillis() / 1000)); // send our ts version and offset
                } catch (IOException e) {
                    e.printStackTrace();
                }
                state = 2;
                Logging.info("PROTOCOL", "Started Burst.");
            }
        }

        if (state == 2) // this is burst. we're going to get some NICKS, channels, etc. handle this stuff
        {

            if (line.startsWith("PING")) // after the burst, we get pignal. or just a ping, really
            {
                state = 3;
                Logging.info("PROTOCOL", "End of Burst. Bringing agents online...");
            }
        }
        if (state == 3) // time to bring services online
        {

            // fall down from above
            try {
                for (Map.Entry<String, Service> Serve : config.Services.entrySet()) {
                    Serve.getValue().Initialize(config, this, Serve.getKey());
                    Serve.getValue().introduce(); // tell all the ladies about our services
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
                // System.exit(1);
            }
            state = 4;
            Logging.warn("PROTOCOL", "Services Online.");
        }

        if (state == 4) // this is our main operational state.
        {

            // the brackets, they do nothing -- wtf was I thinking when I wrote this comment - Jeff?

            // Example of an in-channel message :Kuja PRIVMSG #debug :SrvChan: hi
            // Example of a private pm          :Kuja PRIVMSG SrvChan :hi

            int z = line.indexOf(" :");
            if (z == -1) return;
            String split1[] = {line.substring(0,z),line.substring(z+2)};
            String split2[] = split1[0].split(" ");

            try {
            if (split2.length > 1) // OH NO NPE
            {
                if (split2[1].equals("PRIVMSG")) // I'm interested
                {
                    if (split2[2].startsWith("#")) // is a channel message
                    {
                        if ((split1.length > 0) && split1[1].indexOf(':') > 0) // is possibly talking to me
                        {
                            String who = split1[1];
                            who = who.substring(0, who.indexOf(':')).toLowerCase();
                            if (config.Services.containsKey(who)) // you're talking to me
                            {
                                config.Services.get(who).handle(split2[0].substring(1).toLowerCase(), split2[2].toLowerCase(),
                                        split1[1].substring(who.length() + 1).trim());
                            }
                        }
                    } else if (split1[1].startsWith("\u0001")) {  // is a ctcp, you're talking to me
                        config.Services.get(split2[2].toLowerCase()).handle
                                (split2[0].substring(1).toLowerCase(), split2[0].substring(1).toLowerCase(), "\u0001" + split1[1].trim());
                    } else { // is a private message
                        if (config.Services.containsKey(split2[2].toLowerCase())) // you're talking to me
                        {
                            config.Services.get(split2[2].toLowerCase()).handle(split2[0].substring(1).toLowerCase(), split2[0].substring(1).toLowerCase(), split1[1].trim());
                        }
                        if (split2[2].contains("@")) // fully qualified
                            if (config.Services.containsKey(split2[2].toLowerCase().subSequence(0, split2[2].indexOf("@")))) // you're talking to me
                            {
                                config.Services.get(split2[2].toLowerCase().subSequence(0, split2[2].indexOf("@"))).handle(split2[0].substring(1).toLowerCase(), split2[0].substring(1).toLowerCase(), "FQDN" + split1[1].trim());
                            }
                    }
                }
            }
            } catch (NullPointerException NPE)
            {
                NPE.printStackTrace();
                Logging.error("PROTOCOL", "Raised Generic NPE...");
                Logging.info("PROTOCOL RAW", line);
            }

            // we're going to be getting lots of nothing, and occasionally some commands
            //    if the commands are for services, dispatch them.
            //    if the commands are for daemon, deal with them.
        }
    }

    public void Outgoing(String what) throws IOException {
        out.write(what + "\r\n");
        Logging.raw(what,false); // if we're logging raw, this would be the place to deal wif it
        out.flush();
    }

    public void Introduce(String servicename, Service who) {
        try {
            Outgoing("NICK " + servicename + " 1 " + (System.currentTimeMillis() / 1000) + " +ior " + servicename +
                    " services " + config.Config.get("hostname") + " 0 1066435662 :Network Services"); // bring service online
            //NICK <nick> <hops> <TS> <umode> <user> <host> <server> <services#> <nickip>:<ircname>
            //NICK Jeff 1 1163432928 +i ~Jeff 63.144.132.78 Vulcan.GamesNET.net 0 1066435662 :Jeff Katz
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!config.Database.Users.containsKey(servicename.toLowerCase())) // we had better not already exist. grumble
        {
            config.Database.Users.put(servicename.toLowerCase(), new Client("", "", servicename, config.Config.get("hostname"), ""));
        }
        setauthed(who, config, servicename);
    }

    public void SJoin(String servicename, String where, String modes) {
        try {
            Outgoing("SJOIN " + (System.currentTimeMillis() / 1000) + " " + where + " " + modes + " :@" + servicename);
            // join service to a channel
            //SJOIN <TS> <CHAN> +MODES :user
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!config.Database.Channels.containsKey(where.toLowerCase())) // we're making this channel by joining it
        {
            Map<Client, String> blah = new HashMap<Client, String>();
            blah.put(config.Database.Users.get(servicename.toLowerCase()), "+o");
            config.Database.Channels.put(where.toLowerCase(), new Channel((int) (System.currentTimeMillis() / 1000), where, modes, blah));
        } else { // gotta update the channel...
            Map<Client, String> blah = config.Database.Channels.get(where.toLowerCase()).clientmodes;
            int t = config.Database.Channels.get(where.toLowerCase()).ts;
            String m = config.Database.Channels.get(where.toLowerCase()).modes;
            blah.put(config.Database.Users.get(servicename.toLowerCase()), "+o");
            config.Database.Channels.remove(where.toLowerCase());
            config.Database.Channels.put(where.toLowerCase(), new Channel(t, where, m, blah));
        }
    }

    public void PrivMessage(Service me, String chan, String msg) {
        try {
            Outgoing(":" + me.getname() + " PRIVMSG " + chan + " :" + msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Notice(Service me, String them, String msg) {
        try {
            Outgoing(":" + me.getname() + " NOTICE " + them + " :" + msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void die(Service me, String msg) {
        try {
            Outgoing(":" + me.getname() + " QUIT :" + msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void diegraceful() {
        diegraceful("Quitting Gracefully.");
    }

    public void diegraceful(String message) {
        if (message.trim().equals("")) diegraceful(); // no empty messages

        for (Map.Entry<String, Service> Serve : config.Services.entrySet()) {
            Serve.getValue().diegraceful(message); // quit each
        }
        try {
            Outgoing("QUIT");
        } catch (IOException e) {
            e.printStackTrace();
        }
        main.goGracefullyIntoTheNight();
    }

    public void forcemode(Service me, String channel, String mode, String who) {
        try {
            Outgoing(":" + me.getname() + " MODE " + channel + " " + mode + " " + who);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void kill(Service me, Configuration conf, String who, String why) {
        try {
            Outgoing(":" + me.getname() + " KILL " + who + " :" + conf.Config.get("hostname") + "!services!" + me.getname() + "! (" + why + ")");
            conf.Database.Users.remove(who.toLowerCase());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void part(Service me, String chan, String reason) {
        try {
            Outgoing(":" + me.getname() + " PART " + chan + " :" + reason);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<Client, String> blah = config.Database.Channels.get(chan.toLowerCase()).clientmodes;
        int t = config.Database.Channels.get(chan.toLowerCase()).ts;
        String m = config.Database.Channels.get(chan.toLowerCase()).modes;
        blah.remove(config.Database.Users.get(me.getname().toLowerCase()));
        config.Database.Channels.remove(chan.toLowerCase());
        if (blah.size() > 0) // only put it back if it still has users
            config.Database.Channels.put(chan.toLowerCase(), new Channel(t, chan, m, blah));
    }

    public void gline(Service me, Configuration conf, Client who, String why) {
        try {
            Outgoing(":" + conf.Config.get("hostname") + " AKILL *@" + who.hostmask + " " + who.uid + " 0 " + me.getname() + " " + (int) (System.currentTimeMillis() / 1000) + " :" + why);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setauthed(Service me, Configuration conf, String who) {
        try {

            Outgoing("SVSMODE " + who + " :+r");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

//:Kuja KICK #debug NuclearFriend :rejoin