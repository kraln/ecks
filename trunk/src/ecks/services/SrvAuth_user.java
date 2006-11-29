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

import ecks.services.modules.CommandDesc;
import ecks.util;

import java.util.Map;
import java.util.HashMap;
import java.util.NoSuchElementException;

public class SrvAuth_user { // direct correlation to a database entry
    String username;
    String password;
    String email;
    CommandDesc.access_levels services_access;
    Map<String, String> Metadata;

    public SrvAuth_user() {
    }

    public SrvAuth_user(String u, String p, String e, CommandDesc.access_levels a) {
        username = u;
        password = p;
        email = e;
        services_access = a;
        Metadata = new HashMap<String, String>();
    }

    public SrvAuth_user(String u, String p, String e, CommandDesc.access_levels a, Map<String, String> m) {
        username = u;
        password = p;
        email = e;
        services_access = a;
        Metadata = m;
    }

    public Map<String, String> getAllMeta() {
        return new HashMap<String, String>(Metadata); // read only copy...
    }

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

    public CommandDesc.access_levels getAccess() {
        return services_access;
    }

    public void cngpass(String p) {
        password = util.hash(p);
    }

    public void update(String e) {
        email = e;
    }

    public void update(CommandDesc.access_levels a) {
        services_access = a;
    }

    public String toString() {
        return username + " " + password.hashCode() + " " + email + " " + services_access.toString();
    }

}
