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
import ecks.*;
import ecks.Utility.*;

import java.util.Map;
import java.util.HashMap;


public class Generic {
    public static Map<String, Client> Users = new HashMap<String, Client>();
    public static Map<String, Channel> Channels = new HashMap<String, Channel>();
    public static Protocol curProtocol;

    public static void SetProtocol(Protocol p) {
        curProtocol = p;
    }

    public static void BringServicesOnline()
    // tell the services we're connected, and that they should attempt to introduce themselves
    {
        try {
            for (Map.Entry<String, Service> Serve : Configuration.getSvc().entrySet()) {
                Serve.getValue().introduce(); // tell all the ladies about our services
            }
        } catch (NullPointerException e) {
            Logging.warn("PROTOCOL", "Got NPE whilst bringing services agents online!");
        }
    }

    public static void nickRename(String oldnick, String newnick)
    // client has renamed
    {
        // todo: hook rename
        String oldid = oldnick.toLowerCase();
        String newid = newnick.toLowerCase();

        Client client = Users.get(oldid);
        client.uid = newnick; // keep their uid in step with their nickname...

        Users.put(newid, client);
        Users.remove(oldid);
    }

    public static void modeUser(String target, String modes)
    // a user changed his/her modes
    {
        // todo hook user modes
        Users.get(target.toLowerCase()).modes.applyChanges(modes);

    }
    public static void modeChan(String target, String modes)
    // a client changed a channel's modes
    {
        // todo: hook channel modes
        Channels.get(target.toLowerCase()).modes.applyChanges(modes);

    }


    public static void nickSignOn(String[] tokens)
    // a new client has arrived
    {
        Users.put(
                tokens[1].toLowerCase(),
                // client constructor takes the following:
                // uid hops signon modes ident host althost uplink svsid numericip realname nickid
                new Client(
                        tokens[1],
                        Integer.parseInt(tokens[2]),
                        Long.parseLong(tokens[3]),
                        tokens[4],
                        tokens[5],
                        tokens[6],
                        tokens[7],
                        tokens[8],
                        Long.parseLong(tokens[9]),
                        Long.parseLong(tokens[10]),
                        tokens[11],
                        tokens[12]
                )
        );
    }

    public static void nickSignOff(String who)
    // a client has exited
    {
        // todo: hook quit
        if (Users.containsKey(who.toLowerCase())) {
            Users.remove(who);
        } else {
            Logging.warn("PROTOCOL", "Tried to sign off a user that didn't exist");
        }
    }

    public static void nickGotKicked(String user, String channel)
    {
        // todo: hook kicks
        chanPart(user, channel); // track parts properly
        if (Configuration.getSvc().containsKey(user.toLowerCase())) {
        // they've kicked one of us. bad idea.
            Logging.info("PROTOCOL", "Service was kicked! Attempting rejoin.");
            curProtocol.srvJoin(Configuration.getSvc().get(user.toLowerCase()), channel, "+nt"); // rejoin
        }
    }

    public static void nickGotKilled(String user)
    {
        // todo: hook kills
        nickSignOff(user); // track quits properly
        if (Configuration.getSvc().containsKey(user.toLowerCase())) {
        // they've killed one of us. possibly wrong protocol?
            Logging.error("PROTOCOL", "Service was killed! Attempting 'reconnect'.");
            Configuration.getSvc().get(user.toLowerCase()).introduce(); // reintroduce
        }
    }

    public static void chanBurst(int ts, String channel, String modes, String[] users) {
        // todo: hook join
        ChanModes m = new ChanModes();
        Map<Client, UserModes> cm = new HashMap<Client, UserModes>();

        m.applyChanges(modes);

        for (String user : users) {
            UserModes t = new UserModes();
            Client z;
            if (user.startsWith("@")) { // we should really pull these character->mode mappings from the PROTOCOL or whatever
                z = Users.get(user.substring(1).toLowerCase());
                t.applyChanges("+o");
            } else if (user.startsWith("+")) {
                z = Users.get(user.substring(1).toLowerCase());
                t.applyChanges("+v");
            } else {
                z = Users.get(user.toLowerCase());
            }
            cm.put(z, t); // add this client -> mode mapping to channel
        }

        if (Channels.containsKey(channel.toLowerCase())) {
            Logging.error("PROTOCOL", "Attempted to add a channel that already exists");
        } else {
            Channels.put(channel.toLowerCase(), new Channel(ts, channel, m, cm));
            Logging.info("PROTOCOL", "Channel " + channel + " is now being tracked.");
        }

    }

    public static void chanJoin(int ts, String channel, String user) {
        // todo: hook join
        if (Channels.containsKey(channel.toLowerCase())) {
            Channels.get(channel.toLowerCase()).clientmodes.put(Users.get(user.toLowerCase()), new UserModes());
            Users.get(user.toLowerCase()).chans.add(channel);
        } else { // services joining an empty channel
            chanBurst(ts,channel,"+nt", new String[] {user});
        }

    }

    public static void chanTopic(int ts, String channel, String what) {
        // todo: hook join

        if (Channels.containsKey(channel.toLowerCase())) {
            Channels.get(channel.toLowerCase()).topic = what;
            Channels.get(channel.toLowerCase()).tts = ts;
        } else {
            Logging.error("PROTOCOL", "Attempted to set a topic on a channel that does not exist");
        }

    }

    public static void chanPart(String channel, String user) {
        // todo: hook part

        if (!Channels.containsKey(channel.toLowerCase())) // we had better...
        {
            Logging.warn("PROTOCOL", "Tried to part a user from a channel that didn't exist");
            return;
        }

        Client who;

        if (Users.containsKey(user.toLowerCase())) // we had better
        {
            who = Users.get(user.toLowerCase());
        } else {
            Logging.warn("PROTOCOL", "Tried to part a user that didn't exist");
            return;
        }

        if (Channels.get(channel.toLowerCase()).clientmodes.containsKey(who)) { // we had better...
            Channels.get(channel.toLowerCase()).clientmodes.remove(who);
            who.chans.remove(channel);
        } else {
            Logging.warn("PROTOCOL", "Tried to part a user from a channel that they weren't on");
            return;
        }

        if(Channels.get(channel.toLowerCase()).clientmodes.size() == 0) // channel is empty, remove
        {
            Channels.remove(channel.toLowerCase());
            Logging.info("PROTOCOL", "Channel " + channel + " is no longer being tracked.");
        }

    }

    public static void srvIntroduce(Service whatservice)
    // just pass this one straight down to the protocol
    {
        curProtocol.srvIntroduce(whatservice);
    }

    public static void srvJoin(Service whatservice, String where, String modes)
    // just pass this one straight down to the protocol
    {
        curProtocol.srvJoin(whatservice,where,modes);
    }

    public static void srvPart(Service whatservice, String where, String why)
    // just pass this one straight down to the protocol
    {
        curProtocol.srvPart(whatservice,where,why);
    }

    public static void srvDie() {
        srvDie("Quitting Gracefully.");
    }

    public static void srvDie(String message) {
        if (message.trim().equals("")) srvDie(); // don't quit with no message
        for (Service Serve : Configuration.getSvc().values()) {
            curProtocol.outQUIT(Serve,message);
        }
        main.goGracefullyIntoTheNight();
    }


}
