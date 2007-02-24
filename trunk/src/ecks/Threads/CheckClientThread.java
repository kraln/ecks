package ecks.Threads;

import com.crackj2ee.mail.MailEngine;
import com.crackj2ee.mail.MxConfig;
import com.crackj2ee.mail.Mail;
import com.crackj2ee.mail.MailListenerAdapter;
import ecks.util;
import ecks.Logging;
import ecks.services.Service;
import ecks.protocols.Generic;
import ecks.Utility.Client;
import ecks.Utility.RBLChecker;

import java.io.IOException;

public class CheckClientThread implements Runnable {
    Client who;
    Service parent;

    public CheckClientThread(Client which, Service daddy)
    {
        who = which;
        parent = daddy;
    }
    public void run()
    {
        Logging.info("CLIENTCHK", "Checking incoming client...");
        if(RBLChecker.checkRelay(who.host))
        {
            // if client is an open relay
            Logging.warn("CLIENTCHK", who.uid + " is listed in DNSBL!");
            Generic.curProtocol.outGLINE(parent,who,"Your host is listed in an open spam relay.");  // this should not be permanant.
        }

        Logging.info("CLIENTCHK", "Client check thread completed.");
        util.getThreads().remove(Thread.currentThread()); // this thread is over.
    }
}
