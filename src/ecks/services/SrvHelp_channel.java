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
package ecks.services;

import ecks.Utility.Client;

import java.util.*;

public class SrvHelp_channel { // direct correlation to a database entry
    String channel;
    public List<Client> queue;
    Map<String, String> Metadata;

    public String getMeta(String what) throws NoSuchElementException {
        if (Metadata.containsKey(what)) {
            return Metadata.get(what);
        } else
            throw new java.util.NoSuchElementException();
    }

    public void setMeta(String what, String val) {
        if (Metadata.containsKey(what))
            Metadata.remove(what);
        Metadata.put(what, val);
    }

    public void rmMeta(String what) throws NoSuchElementException {
        if (Metadata.containsKey(what)) {
            Metadata.remove(what);
        } else
            throw new java.util.NoSuchElementException();
    }

    public Map<String, String> getAllMeta() {
        return new HashMap<String, String>(Metadata); // read only copy...
    }

    public SrvHelp_channel() {
    }

    public SrvHelp_channel(String chan) {
        channel = chan;
        queue = new ArrayList<Client>();
        Metadata = new HashMap<String, String>();
    }

    public SrvHelp_channel(String chan, Map<String, String> m) {
        channel = chan;
        queue = new ArrayList<Client>();
        Metadata = m;
    }

    public String toString() {
        return channel + ": queue size " + queue.size();
    }

}
