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

import ecks.Hooks.Hooks;
import ecks.RPC.RPCHandler;
import ecks.Threads.DbThread;
import ecks.protocols.Generic;
import ecks.protocols.Protocol;
import org.apache.xmlrpc.WebServer;

import java.io.IOException;
import java.net.InetAddress;

public class main {

    static Configuration myConf = new Configuration(); // hold our configuration

    public static void main(String[] args) throws Exception {

        // declare our protocol
        Protocol myProto = null;
        try {
            myProto = (Protocol) Class.forName(Configuration.Config.get("protocol")).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        assert myProto != null;
        Generic.SetProtocol(myProto);

        Logging.setup();
        Logging.summary("STARTUP", "Welcome to Ecks Services. Internal Version: " + util.getVersion());
        Logging.verbose("STARTUP", "Logging loaded...");

        //initialize hooks
        Hooks.initialize();
        Logging.verbose("STARTUP", "Hooks initialized...");

        //load services
        Configuration.LoadServices();
        Logging.info("STARTUP", "Services loaded...");

        // at this point, we load up the services database.
        Configuration.Database = new Storage();

        // load up the database
        Configuration.Database.loadDB();
        Logging.info("STARTUP", "Database loaded...");

        // start a thread to save the database every five minutes.
        util.startThread(new Thread(new DbThread())).start();
        Logging.info("STARTUP", "Database thread started...");

        // initialize the connection
        InetAddress inetT;
        if (Configuration.Config.get("localhost").toLowerCase().equals("any"))
            inetT = InetAddress.getLocalHost();
        else
            inetT = InetAddress.getByName(Configuration.Config.get("localhost"));

        Connection myConnection = new Connection(Configuration.Config.get("remote"), Integer.parseInt(Configuration.Config.get("port")), Configuration.Config.get("localport"), inetT, myProto);

        myConnection.Connect(); // cross our fingers and connect
        Logging.info("STARTUP", "Connection attempted...");

        if (Configuration.Config.get("rpcport").equals("any"))
            myConf.RPCServer = new WebServer(8081);
        else
            myConf.RPCServer = new WebServer(Integer.parseInt(Configuration.Config.get("rpcport")), inetT);

        myConf.RPCServer.addHandler("ecks", new RPCHandler());
        myConf.RPCServer.start();

        Logging.info("STARTUP", "XMLRPC Started...");
        Logging.verbose("STARTUP", "Good luck!");
    }

    public synchronized static void goGracefullyIntoTheNight() {
        if (Generic.curProtocol.getState().equals(Protocol.States.S_DISCONNECTING)) System.exit(0);
        Generic.curProtocol.setState(Protocol.States.S_DISCONNECTING);
        Logging.warn("SHUTDOWN", "Interrupting Threads...");
        for (Thread t : util.getThreads())
            t.interrupt();
        Logging.warn("SHUTDOWN", "Stopping RPC");
        myConf.RPCServer.shutdown();
        if (Generic.curProtocol.getWhenStarted() != 0)
            Logging.summary("SHUTDOWN", "Uptime was: " + (Long.parseLong(util.getTS()) - Generic.curProtocol.getWhenStarted()));
        else
            Logging.summary("SHUTDOWN", "Never established a connection!");
        Logging.warn("SHUTDOWN", "Goodbye."); // should put summary here.
        for (Thread t : util.getThreads())
            t.interrupt();
        try {
            Connection.sock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // if we're not done by this point, something is terribly amiss
    }

}
