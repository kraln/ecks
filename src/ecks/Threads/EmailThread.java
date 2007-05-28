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
package ecks.Threads;

import com.crackj2ee.mail.Mail;
import com.crackj2ee.mail.MailEngine;
import com.crackj2ee.mail.MailListenerAdapter;
import com.crackj2ee.mail.MxConfig;
import ecks.Logging;
import ecks.util;

import java.io.IOException;

public class EmailThread implements Runnable {
    String to;
    String code;

    public EmailThread(String t, String c) {
        to = t;
        code = c;
    }

    public void run() {
        MailEngine sender = new MailEngine();
        sender.setMxConfig(new MxConfig());

        // Create a new Mail:
        Mail mail = null;
        try {
            mail = Mail.buildTextMail(
                    "services-no-reply@gamesnet.net",
                    to,
                    "Welcome to GamesNET",
                    util.readFileAsString("register.txt").replace("%%CODE%%", code));


            sender.send(mail, new MailListenerAdapter()); // send it!                    
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Logging.info("EMAIL", "Email thread completed.");
        Logging.verbose("EMAIL", "Email sent to " + to + ", code was " + code);
        util.getThreads().remove(Thread.currentThread()); // this thread is over.
    }
}
