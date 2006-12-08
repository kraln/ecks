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
import ecks.Hooks.Hooks;
import ecks.Utility.*;

import java.util.Map;
import java.util.HashMap;
import java.util.List;


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
            e.printStackTrace();
            Logging.warn("PROTOCOL", "Got NPE whilst bringing services agents online!");
        }
    }

    public static void nickRename(String oldnick, String newnick, long ts)
    // client has renamed
    {
        String oldid = oldnick.toLowerCase();
        String newid = newnick.toLowerCase();

        Client client = Users.get(oldid);
        client.uid = newnick; // keep their uid in step with their nickname...
        client.signon = ts;

        if (oldid.equals(newid)) return;
        // this was a major issue. ircd sends nick changes for case changing.
        // we are case insensitive. we'd end up removing the user and causing all sorts of crap
        // so the above should fix it.

        Users.put(newid, client);
        Users.remove(oldid);
    }

    public static void modeUser(String target, String modes)
    // a user changed his/her modes
    {
        Users.get(target.toLowerCase()).modes.applyChanges(modes);
        Hooks.hook(Hooks.Events.E_UMODE, null, target, modes);

    }

    public static void modeChan(String target, String modes)
    // a client changed a channel's modes
    {
        Channels.get(target.toLowerCase()).modes.applyChanges(modes);
        Hooks.hook(Hooks.Events.E_MODE, null, target, modes);
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
        Logging.verbose("PROTOCOL", "User " + tokens[1] + " is now being tracked.");
        if (Long.parseLong(tokens[9]) > 0) // if user was authed before
        {
            Logging.info("PROTOCOL", "User " + tokens[1] + " was previously authed.");
            if (Configuration.getSvc().containsKey(Configuration.authservice)) // if we have an auth service
            {
                Logging.info("PROTOCOL", "Attempting re-entry...");
                Configuration.getSvc().get(Configuration.authservice).handle(tokens[1].toLowerCase(),"service","reauth " + tokens[9] + " " + tokens[3]);
            }
        }
    }

    public synchronized static void nickSignOff(String who)
    // a client has exited
    {
        if (Users.containsKey(who.toLowerCase())) {
            List<String> blah = Users.get(who.toLowerCase()).getChans(); // avoid concurrency issues
            for (String chan : blah)
                chanPart(chan, who);
            Users.remove(who);
            Logging.verbose("PROTOCOL", "User " + who + " is no longer being tracked.");
        } else {
            Logging.warn("PROTOCOL", "Tried to sign off a user that didn't exist");
        }
    }

    public static void nickGotKicked(String user, String channel) {

        chanPart(channel, user); // track parts properly
        Hooks.hook(Hooks.Events.E_KICK, channel, user, null);
        if (Configuration.getSvc().containsKey(user.toLowerCase())) {
            // they've kicked one of us. bad idea.
            Logging.info("PROTOCOL", "Service was kicked! Attempting rejoin.");
            curProtocol.srvJoin(Configuration.getSvc().get(user.toLowerCase()), channel, "+nt"); // rejoin
        }
    }

    public static void nickGotKilled(String user) {
        nickSignOff(user); // track quits properly
        if (Configuration.getSvc().containsKey(user.toLowerCase())) {
            // they've killed one of us. possibly wrong protocol?
            Logging.error("PROTOCOL", "Service was killed! Attempting 'reconnect'.");
            Configuration.getSvc().get(user.toLowerCase()).introduce(); // reintroduce
        }
    }

    public static void chanBurst(int ts, String channel, String modes, String[] users) {
        ChanModes m = new ChanModes();
        Map<Client, UserModes> cm = new HashMap<Client, UserModes>();

        m.applyChanges(modes);

        for (String user : users) {
            UserModes t = new UserModes();
            Client z;
            if (user.startsWith("@"))
            { // we should really pull these character->mode mappings from the PROTOCOL or whatever
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
            Logging.error("PROTOCOL", "Attempted to add a channel " + channel + " that already exists. " + cm.size() + " user(s) joined.");
            Logging.info("PROTOCOL", "Chan is: " + Channels.get(channel.toLowerCase()).toString());
            Channels.get(channel.toLowerCase()).clientmodes.putAll(cm);
        } else {
            Channels.put(channel.toLowerCase(), new Channel(ts, channel, m, cm));
            Logging.verbose("PROTOCOL", "Channel " + channel + " is now being tracked.");
        }

        for (String user : users) {
            if (user.startsWith("@")) user = user.substring(1);
            if (user.startsWith("+")) user = user.substring(1);
            user = user.toLowerCase();
            Users.get(user).chans.add(channel.toLowerCase());
            Hooks.hook(Hooks.Events.E_JOINCHAN, channel, user, null);
        }

    }

    public static void chanJoin(int ts, String channel, String user) {
        if (Channels.containsKey(channel.toLowerCase())) {
            Channels.get(channel.toLowerCase()).clientmodes.put(Users.get(user.toLowerCase()), new UserModes());
            Users.get(user.toLowerCase()).chans.add(channel.toLowerCase());
            Hooks.hook(Hooks.Events.E_JOINCHAN, channel, user, "");
        } else { // services joining an empty channel
            chanBurst(ts, channel, "+nt", new String[]{user});
        }

    }

    public static void chanTopic(int ts, String channel, String what) {
        if (Channels.containsKey(channel.toLowerCase())) {
            Channels.get(channel.toLowerCase()).topic = what;
            Channels.get(channel.toLowerCase()).tts = ts;
            Hooks.hook(Hooks.Events.E_TOPIC, channel, what, null);
        } else {
            Logging.error("PROTOCOL", "Attempted to set a topic on a channel that does not exist");
        }

    }

    public static void chanPart(String channel, String user) {
        if (!Channels.containsKey(channel.toLowerCase())) // we had better...
        {
            Logging.warn("PROTOCOL", "Tried to part user " + user + "  from a channel " + channel + " that didn't exist");
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
            who.chans.remove(channel.toLowerCase());
            Channels.get(channel.toLowerCase()).clientmodes.remove(who);
            Hooks.hook(Hooks.Events.E_PARTCHAN, channel, user, null);
        } else {
            Logging.warn("PROTOCOL", "Tried to part user " + user + " from channel " + channel + " that they weren't on");
            Logging.info("PROTOCOL", "User is: " + who.toString());
            Logging.info("PROTOCOL", "Chan is: " + Channels.get(channel.toLowerCase()).toString());
        }

        if (Channels.get(channel.toLowerCase()).clientmodes.size() == 0) // channel is empty, remove
        {
            Channels.remove(channel.toLowerCase());
            Logging.verbose("PROTOCOL", "Channel " + channel + " is no longer being tracked.");
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
        curProtocol.srvJoin(whatservice, where, modes);
    }

    public static void srvPart(Service whatservice, String where, String why)
    // just pass this one straight down to the protocol
    {
        curProtocol.srvPart(whatservice, where, why);
    }

    public static void srvDie() {
        srvDie("Quitting Gracefully.");
    }

    public static void srvDie(String message) {
        if (message.trim().equals("")) srvDie(); // don't quit with no message
        for (Service Serve : Configuration.getSvc().values()) {
            curProtocol.outQUIT(Serve, message);
        }
        main.goGracefullyIntoTheNight();
    }


}
