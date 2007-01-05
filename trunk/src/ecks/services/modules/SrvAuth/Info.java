package ecks.services.modules.SrvAuth;

import ecks.services.modules.bCommand;
import ecks.services.modules.CommandDesc;
import ecks.services.Service;
import ecks.services.SrvAuth;
import ecks.services.SrvAuth_user;
import ecks.protocols.Generic;
import ecks.Configuration;
import ecks.util;

import java.util.Map;
import java.util.Date;
import java.text.DateFormat;

public class Info extends bCommand {
    public final CommandDesc Desc = new CommandDesc("info", 1, true, CommandDesc.access_levels.A_AUTHED, "Returns information about a handle");
    public CommandDesc getDesc() { return Desc; }
    public void handle_command(Service who, String user, String replyto, String arguments) {
        if (((SrvAuth) who).getUsers().containsKey(arguments.toLowerCase())) {

            SrvAuth_user t = ((SrvAuth) who).getUsers().get(arguments.toLowerCase());
            Generic.curProtocol.outNOTICE(who, user, "\u0002Username:\u0002 " + arguments);
            Generic.curProtocol.outNOTICE(who, user, "\u0002---------\u0002 ");
            Generic.curProtocol.outNOTICE(who, user, "Access Level: " + t.getAccess());

            if (t.getAllMeta().containsKey("_ts_registered"))
                Generic.curProtocol.outNOTICE(who, user, "Registered On: " + DateFormat.getDateTimeInstance(DateFormat.LONG,DateFormat.LONG).format(new Date(t.getMeta("_ts_registered"))));
            else
                Generic.curProtocol.outNOTICE(who, user, "Unknown Registration Date.");

            if (t.getAllMeta().containsKey("_ts_last"))
                Generic.curProtocol.outNOTICE(who, user, "Last Seen On: " + DateFormat.getDateTimeInstance(DateFormat.LONG,DateFormat.LONG).format(new Date(t.getMeta("_ts_last"))));
            else
                Generic.curProtocol.outNOTICE(who, user, "Has never logged in.");

            if (t.getAllMeta().containsKey("staffnote"))
                Generic.curProtocol.outNOTICE(who, user, "Staff Note: " + t.getMeta("staffnote"));
            else
                Generic.curProtocol.outNOTICE(who, user, "Has no staff note.");

            if (t.getAllMeta().containsKey("cookie"))
            {
                Generic.curProtocol.outNOTICE(who, user, "Has a cookie");
                if (util.checkaccess(user, CommandDesc.access_levels.A_HELPER.ordinal()))
                Generic.curProtocol.outNOTICE(who, user, "Cookie: " + t.getMeta("cookie"));
            }

            if (t.WhereAccess.size() == 0)
                Generic.curProtocol.outNOTICE(who, user, "Has no access to any channel");
            else
                Generic.curProtocol.outNOTICE(who, user, "Channel Access:");
            for (Map.Entry<String, String> e : t.WhereAccess.entrySet())
                Generic.curProtocol.outNOTICE(who, user, "    " + util.pad(e.getKey(), 14) + ": " + e.getValue());

        } else Generic.curProtocol.outNOTICE(who, user, "\u0002Error:\u0002 No such registered username...");
    }
}
