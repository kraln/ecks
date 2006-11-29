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
import ecks.protocols.*;

import java.net.InetAddress;
import org.apache.xmlrpc.WebServer;

public class main {

    static Configuration myConf = new Configuration(); // hold our configuration

    public static void main(String[] args) throws Exception {

        // declare our protocol
        Protocol myProto = null;
        try {
           myProto = (Protocol) Class.forName(myConf.Config.get("protocol")).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        assert myProto != null;
        myProto.setConfig(myConf);

        Logging.setup(myConf,myProto);
        Logging.summary("STARTUP", "Welcome to Ecks Services. Internal Version: " + util.getVersion());
        Logging.verbose("STARTUP", "Logging loaded...");

        //load services
        myConf.LoadServices(myProto);
        Logging.info("STARTUP", "Services loaded...");

        // at this point, we load up the services database.
        myConf.Database = new Storage();

        // load up the database
        myConf.Database.loadDB(myConf);
        Logging.info("STARTUP", "Database loaded...");

        // start a thread to save the database every five minutes.
        util.startThread(new Thread(new DbThread(myConf))).start();
        Logging.info("STARTUP", "Database thread started...");

        // initialize the connection
        InetAddress inetT;
        if (myConf.Config.get("localhost").toLowerCase().equals("any"))
            inetT = InetAddress.getLocalHost();
        else
            inetT = InetAddress.getByName(myConf.Config.get("localhost"));

        Connection myConnection = new Connection(myConf.Config.get("remote"), Integer.parseInt(myConf.Config.get("port")),myConf.Config.get("localport"), inetT, myProto);

        myConnection.Connect(); // cross our fingers and connect
        Logging.info("STARTUP", "Connection attempted...");

        if (myConf.Config.get("localport").equals("any"))
            myConf.RPCServer = new WebServer(8081);
        else
            myConf.RPCServer = new WebServer(Integer.parseInt(myConf.Config.get("localport"))+1,inetT);

        myConf.RPCServer.addHandler("ecks", new RPCHandler());
        myConf.RPCServer.start();

        Logging.info("STARTUP", "XMLRPC Started...");
        Logging.verbose("STARTUP", "Good luck!");
    }

    public static void goGracefullyIntoTheNight()
    {
        Logging.warn("SHUTDOWN", "Interrupting Threads...");
        for (Thread t : util.getThreads())
            t.interrupt();
        Logging.warn("SHUTDOWN", "Stopping RPC");
        myConf.RPCServer.shutdown();
        Logging.warn("SHUTDOWN", "Goodbye."); // should put summary here.
        // if we're not done by this point, something is terribly amiss
    }

}
