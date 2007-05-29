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

import ecks.services.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class Storage {
    public void loadDB() {
        Document dom = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse("srvdb.xml");
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return;
        }

        assert dom != null;
        Element docEle = dom.getDocumentElement();
        NodeList zl;
        Element el;

        zl = docEle.getElementsByTagName("service");
        if ((zl != null) && zl.getLength() > 0) {
            for (int i = 0; i < zl.getLength(); i++) {
                el = (Element) zl.item(i);
                if (Configuration.Services.containsKey(el.getAttribute("name").toLowerCase())) {
                    Logging.verbose("DATABASE", el.getAttribute("name") + " Loading database...");
                    Configuration.getSvc().get(el.getAttribute("name").toLowerCase()).loadSRVDB(el.getChildNodes());
                    Logging.verbose("DATABASE", el.getAttribute("name") + " Loading complete...");
                }
            }
        } else {
            Logging.error("DATABASE", "No services defined in configuration file! Exiting");
            main.goGracefullyIntoTheNight();
        }

    }

    public static void flushDB() {
        Logging.verbose("DATABASE", "Started DB save thread...");
        util.startThread(new Thread(new flushThread())).start();
        Logging.verbose("DATABASE", "Thread started...");
    }

    static class flushThread implements Runnable {
        public void run() {
        String out = "";
        Logging.verbose("DATABASE", "Started gathering services info...");
        for (Map.Entry<String, Service> Serve : Configuration.Services.entrySet()) {
            out = out + Serve.getValue().getSRVDB(); // get well-formed xml from each
        }
        out = out.trim();
            Logging.info("DATABASE", "Writing " + (out.getBytes().length) + "bytes of services data to disk...");
            try {
                BufferedWriter o = new BufferedWriter(new FileWriter("srvdb.xml"));
                o.write("<db version=\"" + util.getVersion() + "\" stamp=\"" + util.getTS() + "\">\r\n" + out + "\r\n</db>");
                o.close();
                Logging.verbose("DATABASE", "Write completed.");
            } catch (IOException e) {
                e.printStackTrace();
            }
            util.getThreads().remove(Thread.currentThread());
        }
    }
}


