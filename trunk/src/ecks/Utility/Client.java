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
package ecks.Utility;

import ecks.Utility.Modes;

import java.util.List;
import java.util.ArrayList;

public class Client {
    // this class contains every possible bit of information we could ever have about a client
    // in practice, most ircds only send a small portion of the following


    public String uid;
    public int hops;
    public long signon;
    public UserModes modes;
    public String ident;
    public String host;
    public String althost;
    public String uplink;
    public long svsid;
    public long numericip;
    public String realname;
    public String nickid;
    public List<String> chans;

    public String authhandle; // for use with auth services

    public Client(String a, int b, long c, String d, String e, String f, String g, String h, long i, long j, String k, String l)
    {
        uid = a;
        hops = b;
        signon = c;
        modes = new UserModes(); modes.applyChanges(d);
        ident = e;
        host = f;
        althost = g;
        uplink = h;
        svsid = i;
        numericip = j;
        realname = k;
        nickid = l;

        chans = new ArrayList<String>();
        authhandle = null;
    }


    //public Client(){  modes = new UserModes(); chans = new ArrayList<String>(); }
    public String toString()
    {
        return uid + ": " + modes.getModes() + " " + ident + " " + host + " (" + althost + ") on " + uplink;
    }
}

