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

public class Client {
    public String uid;
    public String uplink;
    public String hostmask;
    public String authname;
    public String ident;
    public String gecos;
    public String modes;

    public Client(String ud, String upl, String id, String host, String gec) // in on a burst or otherwise introduced
    {
        uid = ud; uplink = upl; hostmask = host; ident = id; gecos=gec; authname = null;
        modes="+o"; // todo: implement modes
    }
    public Client(){}
    public String getName()
    {
        return uid;
    }
    public String toString()
    {
        return uid + ": " + hostmask + " on " + uplink;
    }
}

