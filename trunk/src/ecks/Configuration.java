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
import ecks.services.modules.CommandModule;
import org.apache.xmlrpc.WebServer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Configuration {
    public static Map<String, String> Config;
    static Map<String, Service> Services;
    public static Storage Database;
    public WebServer RPCServer;
    static Object cp;
    public static String authservice;
    public static String chanservice;
    public static String logservice; // what service ends up logging in-chan

    Configuration() {
        Config = new HashMap<String, String>();
        Services = new HashMap<String, Service>();
        Database = new Storage();
        cp = new ConfParse();
        authservice = null;
        chanservice = null;
        logservice = null;
    }

    public static Storage getDB() {
        return Database;
    }

    public static Map<String, Service> getSvc() {
        return Services;
    }

    static void LoadServices() {
        ((ConfParse) cp).parseDocument2();
    }
}

class ConfParse {

    Document dom;

    public ConfParse() {
        parseXmlFile();
        parseDocument();
    }

    // load and parse XML file into DOM
    private void parseXmlFile() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse("ecks.xml");
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void parseDocument() {
        Element docEle = dom.getDocumentElement();
        NodeList zl;
        Element el;

        zl = docEle.getElementsByTagName("debuglevel");
        el = (Element) zl.item(0);
        Configuration.Config.put("debuglevel", el.getAttribute("value"));

        zl = docEle.getElementsByTagName("debugdevice");
        el = (Element) zl.item(0);
        Configuration.Config.put("debugdevice", el.getAttribute("value"));

        zl = docEle.getElementsByTagName("debugchan");
        el = (Element) zl.item(0);
        Configuration.Config.put("debugchan", el.getAttribute("value"));

        zl = docEle.getElementsByTagName("joinchannels");
        el = (Element) zl.item(0);
        Configuration.Config.put("joinchannels", el.getAttribute("value"));

        zl = docEle.getElementsByTagName("secure");
        el = (Element) zl.item(0);
        Configuration.Config.put("secure", el.getAttribute("value"));

        NodeList nl = docEle.getElementsByTagName("uplink");
        Configuration.Config.put("protocol", ((Element) nl.item(0)).getAttribute("protocol"));

        NodeList cnod = nl.item(0).getChildNodes();
        for (int i = 0; i < cnod.getLength(); i++) {
            String mname = "";
            String mval = "";
            try {
                mname = (cnod.item(i)).getNodeName();
                mval = (cnod.item(i)).getAttributes().getNamedItem("value").getNodeValue();

            } catch (NullPointerException N) {
                //
            }
            if (!mname.equals("#text") && !mname.equals("#comment")) {
                Configuration.Config.put(mname, mval);
            }
        }
    }

    public void parseDocument2() {
        Element docEle = dom.getDocumentElement();

        NodeList nl = docEle.getElementsByTagName("service");

        for (int i = 0; i < nl.getLength(); i++) {
            String newSvc = ((Element) nl.item(i)).getAttribute("name");
            try {
                Configuration.Services.put(
                        newSvc.toLowerCase(),
                        (Service) Class.forName(
                                ((Element) nl.item(i)).getAttribute("class")
                        ).newInstance()
                );
                Configuration.Services.get(newSvc.toLowerCase()).setname(newSvc);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                Logging.error("CONFIGURATION", "Exception: Class not found! (check your config)");
                main.goGracefullyIntoTheNight();
            }

            NodeList z = ((Element) nl.item(i)).getElementsByTagName("modules");
            NodeList nle;
            if (z.item(0) != null) {
                nle = ((Element) z.item(0)).getElementsByTagName("command");
                Logging.info("CONFIGURATION", "Loading " + nle.getLength() + " command modules for service " + newSvc + ".");
                for (int iz = 0; iz < nle.getLength(); iz++) {
                    try {
                        // I dare you to understand what this line does in one go. I can't.
                        Configuration.Services.get(newSvc.toLowerCase()).addCommand(((CommandModule) Class.forName(((Element) nle.item(iz)).getAttribute("value")).newInstance()).getName().toLowerCase(), (CommandModule) Class.forName(((Element) nle.item(iz)).getAttribute("value")).newInstance());
                        Logging.verbose("CONFIGURATION", "Loading command module " + ((Element) nle.item(iz)).getAttribute("value") + " for service " + newSvc + ".");
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
