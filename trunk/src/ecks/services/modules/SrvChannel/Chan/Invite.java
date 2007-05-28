package ecks.services.modules.SrvChannel.Chan;

import ecks.Configuration;
import ecks.Logging;
import ecks.protocols.Generic;
import ecks.services.Service;
import ecks.services.SrvChannel;
import ecks.services.SrvChannel_channel;
import ecks.services.modules.CommandDesc;
import ecks.services.modules.bCommand;

public class Invite extends bCommand {
    public final CommandDesc Desc = new CommandDesc("invite", 2, true, CommandDesc.access_levels.A_AUTHED, "Invites a user to a channel", "[channel] <user>");

    public CommandDesc getDesc() {
        return Desc;
    }

    public void handle_command(Service who, String user, String replyto, String arguments) {
        /*
        Srvchan: kick john [why]
        SrvChan: kick #somechan john [why]
        SrvChan: kick john #somechan [why]
        */
        String whatchan = "";
        String whom = "";
        String args[] = arguments.split(" ");

        whatchan = replyto;
        whom = user;

        try {
            if (args.length > 0 && (!(args[0].equals("")))) { // if we have arguments
                if (args[0].startsWith("#")) { // assume channel
                    whatchan = args[0];
                    if (args.length > 1)   // if there's another argument, assume it's a user
                        whom = args[1];
                } else if ((args.length > 1) && args[1].startsWith("#")) { // assume channel
                    whatchan = args[1];
                    whom = args[0];
                } else {
                    whom = args[0];
                }

            }
        } catch (NullPointerException NPE) {
            NPE.printStackTrace();
            Logging.warn("SRVCHAN_INVITE", "Got NPE: " + arguments);
        }

        whom = whom.toLowerCase();
        whatchan = whatchan.toLowerCase();

        if (whatchan.startsWith("#")) {
            if (((SrvChannel) who).getChannels().containsKey(whatchan)) {
                if (Generic.Users.containsKey(whom)) {
                    if (Configuration.getSvc().containsKey(whom)) {
                        Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 Users should not play with fire. (You cannot kick network services)");
                        return;
                    }
                    if (Generic.Users.get(user).authhandle != null) {
                        String mname = Generic.Users.get(user).authhandle;
                        if (((SrvChannel) who).getChannels().get(whatchan).getUsers().containsKey(mname)) {
                            SrvChannel_channel.ChanAccess mlevel = ((SrvChannel) who).getChannels().get(whatchan).getUsers().get(mname);
                            if (mlevel.ordinal() >= SrvChannel_channel.ChanAccess.C_CHANOP.ordinal()) {
                                Generic.curProtocol.outINVITE(who, whom, whatchan);
                            } else
                                Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 You do not have sufficient access to perform that command");
                        } else
                            Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 You have no access to this channel");
                    } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 You are not authed!");
                } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 User does not exist!");
            } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 Not a registered channel!");
        } else Generic.curProtocol.outPRVMSG(who, replyto, "\u0002Error:\u0002 Not a channel!");
    }
}
