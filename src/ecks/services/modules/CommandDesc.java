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
package ecks.services.modules;

public class CommandDesc {
    public static enum access_levels {
        A_NONE, A_PENDING, A_AUTHED, A_HELPER, A_OPER, A_SRA
    }
    public String command;
    public int ArgCount;
    public boolean InChannel;
    public access_levels Required_Access;
    public String help;
    public String arguments;

    public CommandDesc(String c, int a, boolean i, access_levels r, String h)
    {
        command = c;
        ArgCount = a;
        InChannel = i;
        Required_Access = r;
        help = h;
        arguments = "";
    }

    public CommandDesc(String c, int a, boolean i, access_levels r, String h, String arg)
    {
        command = c;
        ArgCount = a;
        InChannel = i;
        Required_Access = r;
        help = h;
        arguments = arg;
    }

}
