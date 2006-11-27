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

import java.util.Map;
import java.io.IOException;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;
import org.xml.sax.SAXException;
import ecks.protocols.Protocol;

import ecks.services.Service;
import ecks.services.modules.CommandModule;

public class Configuration {
    public Map<String, String> Config;
    public Map<String, Service> Services;
    public Storage Database;
    public Object cp;
    public String authservice;
    public String chanservice;
    public String logservice; // what service ends up logging in-chan

    Configuration() {
        Config = new HashMap<String, String>();
        Services = new HashMap<String, Service>();
        Database = new Storage();
        cp = new ConfParse(this);
        authservice = "";
        chanservice = "";
        logservice = "";
    }

    public Storage getDB()
    {
        return Database;
    }

    public Map<String,Service> getSvc()
    {
        return Services;
    }

    void LoadServices(Protocol talkto) {
        ((ConfParse) cp).parseDocument2(this, talkto);
    }
}

class ConfParse {

    Document dom;

    public ConfParse(Configuration towhere) {
        parseXmlFile();
        parseDocument(towhere);
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

    private void parseDocument(Configuration conf) {
        Element docEle = dom.getDocumentElement();
        NodeList zl;
        Element el;

        zl = docEle.getElementsByTagName("debuglevel");
        el = (Element) zl.item(0);
        conf.Config.put("debuglevel", el.getAttribute("value"));

        zl = docEle.getElementsByTagName("debugdevice");
        el = (Element) zl.item(0);
        conf.Config.put("debugdevice", el.getAttribute("value"));

        zl = docEle.getElementsByTagName("debugchan");
        el = (Element) zl.item(0);
        conf.Config.put("debugchan", el.getAttribute("value"));

        zl = docEle.getElementsByTagName("joinchannels");
        el = (Element) zl.item(0);
        conf.Config.put("joinchannels", el.getAttribute("value"));

        NodeList nl = docEle.getElementsByTagName("uplink");
        conf.Config.put("protocol", ((Element) nl.item(0)).getAttribute("protocol"));

        NodeList cnod = nl.item(0).getChildNodes();
        for (int i = 0; i < cnod.getLength(); i++) {
            String mname = "";
            String mval = "";
            try {
                mname = (cnod.item(i)).getNodeName();
                mval = (cnod.item(i)).getAttributes().getNamedItem("value").getNodeValue();

            } catch (NullPointerException N) {
                // myLog.log("Loading XML Config: EXCEPTION at NODE " + i);
            }
            if (!mname.equals("#text") && !mname.equals("#comment")) {
                conf.Config.put(mname, mval);
            }
        }
    }

    public void parseDocument2(Configuration conf, Protocol proto) {
        Element docEle = dom.getDocumentElement();

        NodeList nl = docEle.getElementsByTagName("service");

        for (int i = 0; i < nl.getLength(); i++) {
            String newSvc = ((Element) nl.item(i)).getAttribute("name");
            try {
                conf.Services.put(
                        newSvc.toLowerCase(),
                        (Service) Class.forName(
                                ((Element) nl.item(i)).getAttribute("class")
                        ).newInstance()
                );
                conf.Services.get(newSvc.toLowerCase()).setname(newSvc);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                System.out.println("Class not found... Bad configuration");
                System.exit(1);
            }

            NodeList z = ((Element) nl.item(i)).getElementsByTagName("modules");
            NodeList nle;
            if (z.item(0) != null) {
                nle = ((Element) z.item(0)).getElementsByTagName("command");
                // debug - loaded commands here
                for (int iz = 0; iz < nle.getLength(); iz++) {
                    try {
                        // I dare you to understand what this line does. I don't
                        conf.Services.get(newSvc.toLowerCase()).addCommand(((CommandModule) Class.forName(((Element) nle.item(iz)).getAttribute("value")).newInstance()).getName().toLowerCase(), (CommandModule) Class.forName(((Element) nle.item(iz)).getAttribute("value")).newInstance());
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
