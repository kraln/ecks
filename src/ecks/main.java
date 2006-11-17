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

public class main {

    public static void main(String[] args) throws Exception {

        // get our configuration
        Configuration myConf = new Configuration();

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

        //load services
        myConf.LoadServices(myProto);

        // at this point, we load up the services database.
        myConf.Database = new Storage();

        // load up the database
        myConf.Database.loadDB(myConf);

        // start a thread to save the database every five minutes.
        new Thread(new DbThread(myConf)).start();

        // initialize the connection
        InetAddress inetT;
        if (myConf.Config.get("localhost").toLowerCase().equals("any"))
            inetT = InetAddress.getLocalHost();
        else
            inetT = InetAddress.getByName(myConf.Config.get("localhost"));

        Connection myConnection = new Connection(myConf.Config.get("remote"), Integer.parseInt(myConf.Config.get("port")),myConf.Config.get("localport"), inetT, myProto);

        // cross our fingers and connect
        myConnection.Connect();
    }
}
