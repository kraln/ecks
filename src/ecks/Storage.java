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

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Map;
import java.util.HashMap;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

import org.xml.sax.SAXException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;

public class Storage {
    public Map<String, Client> Users = new HashMap<String, Client>();
    public Map<String, Channel> Channels = new HashMap<String, Channel>();

    public void loadDB(Configuration conf) {
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
                if (conf.Services.containsKey(el.getAttribute("name").toLowerCase())) {
                    conf.getSvc().get(el.getAttribute("name").toLowerCase()).loadSRVDB(el.getChildNodes());
                }
            }
        } else {
            System.exit(3);
        }

    }

    public synchronized void flushDB(Configuration conf) {
        String out = "";
        for (Map.Entry<String, Service> Serve : conf.Services.entrySet()) {
            out = out + Serve.getValue().getSRVDB(); // get well-formed xml from each
        }
        out = out.trim();
        try {
            BufferedWriter o = new BufferedWriter(new FileWriter("srvdb.xml"));
            o.write("<db>\r\n" + out + "\r\n</db>");
            o.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


