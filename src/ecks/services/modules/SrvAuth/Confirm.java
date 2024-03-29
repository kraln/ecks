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
package ecks.services.modules.SrvAuth;

import ecks.Logging;
import ecks.protocols.Generic;
import ecks.services.Service;
import ecks.services.SrvAuth;
import ecks.services.modules.CommandDesc;
import ecks.services.modules.bCommand;
import ecks.util;

public class Confirm extends bCommand {
    public final CommandDesc Desc = new CommandDesc("confirm", 1, false, CommandDesc.access_levels.A_PENDING, "Verifies your email.", "<cookie>");

    public CommandDesc getDesc() {
        return Desc;
    }

    public void handle_command(Service who, String user, String replyto, String arguments) {
        SrvAuth temp = ((SrvAuth) who);
        String uTemp = Generic.Users.get(user).authhandle;
        if (arguments.length() > 1) {
            if (uTemp != null) {
                if (((temp.getUsers().get(Generic.Users.get(user).authhandle)).getAccess() == Desc.Required_Access) || (temp.getUsers().get(Generic.Users.get(user).authhandle)).getAccess().ordinal() >= CommandDesc.access_levels.A_HELPER.ordinal()) {
                    if (temp.getUsers().containsKey(uTemp)) {
                        if (util.sanitize(arguments)) {
                            if (temp.getUsers().get(uTemp).getAllMeta().containsKey("cookie") || (temp.getUsers().get(Generic.Users.get(user).authhandle)).getAccess().ordinal() >= CommandDesc.access_levels.A_HELPER.ordinal()) {
                                if (temp.getUsers().get(uTemp).getMeta("cookie").equals(arguments)) {
                                    temp.getUsers().get(uTemp).update(CommandDesc.access_levels.A_AUTHED);
                                    temp.getUsers().get(uTemp).rmMeta("cookie");
                                    Generic.curProtocol.outPRVMSG(who, replyto, "\u0002" + Generic.Users.get(user).uid + ":\u0002 Your account has been confirmed!");
                                } else
                                if (temp.getUsers().get(uTemp).getAccess().ordinal() >= CommandDesc.access_levels.A_HELPER.ordinal()) {
                                    // is a helper and is confirming account for user
                                    if (temp.getUsers().containsKey(arguments.toLowerCase())) {
                                        temp.getUsers().get(arguments.toLowerCase()).update(CommandDesc.access_levels.A_AUTHED);
                                        temp.getUsers().get(arguments.toLowerCase()).rmMeta("cookie");
                                        Generic.curProtocol.outPRVMSG(who, arguments.toLowerCase(), "\u0002" + Generic.Users.get(arguments.toLowerCase()).uid + ":\u0002 The account has been confirmed!");
                                        Logging.warn("SRVAUTH", "Username " + arguments + " had email confirmed by " + user + ".");
                                    } else
                                        Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 That user doesn't exist!");
                                } else
                                    Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 Your cookie is wrong!");
                            } else
                                Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 You don't have a cookie!");
                        } else
                            Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 Your cookie contains invalid characters!");
                    } else
                        Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 Your account doesn't exist! (This is a bug)");
                } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 Your account isn't pending!");
            } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 You're not logged in!");
        } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Usage:\u0002 confirm [yourcookie]");
    }
}
