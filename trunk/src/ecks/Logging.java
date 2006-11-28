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

import ecks.protocols.Protocol;

import java.io.*;

public class Logging {

    public static enum loglevels { D_NONE, D_ERRORS, D_WARN, D_SUMMARY, D_INFO, D_VERBOSE }

    static loglevels myLogLevel;
    static boolean inchan;
    static Writer out;
    static Protocol myP;
    static Configuration myC;
    /*

    This class should handle all logging. Duh.

    */
    public Logging()
    {
           myLogLevel = loglevels.D_NONE;
    }

    public static void setup(Configuration c, Protocol p)
    {
        myLogLevel = loglevels.valueOf(c.Config.get("debuglevel"));
        boolean stdio = c.Config.get("debugdevice").equals("stdio");
        try {
            if (stdio) {
                out = new OutputStreamWriter(System.out);
            } else {
                out = new BufferedWriter(new FileWriter(c.Config.get("debugdevice")));
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to create logging device. Giving up.");
            System.exit(-1); // no logging - give up
        }
        inchan = !c.Config.get("debugchan").equals("OFF");
        myP = p;
        myC = c;
    }

    public void finalize() throws Throwable {
        out.close();
        super.finalize();
    }

    static void log(String what, loglevels ll, String ai) throws IOException {
        // TIME \t EVENTLVL \t EVENT \t ADDT'L INFO
        // or
        // \t \t RAW \t -> RAW IRC HERE
        // \t \t RAW \t <- RAW IRC HERE
        if (myLogLevel.equals(loglevels.D_NONE)){
            // goggles do nothing...
            return;
        } else if (myLogLevel.equals(loglevels.D_VERBOSE)) {
            if (what.equals("RAW"))
            {
                out.write("         \t          \t" + util.pad(what, 10) + '\t' + ai + "\r\n");
                out.flush();
                return;
            }
        }

        if (inchan)
        {
            if (myP.getState() == 4) // only do this if we're connected
            myP.PrivMessage(myC.getSvc().get(myC.logservice), myC.Config.get("debugchan"), String.valueOf(System.currentTimeMillis() / 1000) + '\t' + util.pad(ll.toString(), 10) + '\t' + util.pad(what, 10) + '\t' + ai);
        }
        out.write(String.valueOf(System.currentTimeMillis() / 1000) + '\t' + util.pad(ll.toString(),10) + '\t' + util.pad(what,10) + '\t' + ai+ "\r\n");
        out.flush();

    }

    public static void info(String what, String more)
    {
        if (myLogLevel.ordinal() < loglevels.D_INFO.ordinal()) return;
        try {
            log(what,loglevels.D_INFO,more);
        } catch (IOException e) {
            System.err.println("Failed to log. Giving up.");
            System.exit(-1); // we can't output anything to the log file, so just kind of give up.
        }
    }
    public static void warn(String what, String more)
    {
        if (myLogLevel.ordinal() < loglevels.D_WARN.ordinal()) return;
        try {
            log(what,loglevels.D_WARN,more);
        } catch (IOException e) {
            System.err.println("Failed to log. Giving up.");
            System.exit(-1); // we can't output anything to the log file, so just kind of give up.
        }
    }
    public static void error(String what, String more)
    {
        if (myLogLevel.ordinal() < loglevels.D_ERRORS.ordinal()) return;
        try {
            log(what,loglevels.D_ERRORS,more);
        } catch (IOException e) {
            System.err.println("Failed to log. Giving up.");
            System.exit(-1); // we can't output anything to the log file, so just kind of give up.
        }
    }
    public static void raw(String what, Boolean in)
    {
        if (myLogLevel.ordinal() < loglevels.D_VERBOSE.ordinal()) return;
        String direction = "-> ";
        if (in) direction = "<- ";
        try {
            log("RAW",loglevels.D_VERBOSE, direction + what);
        } catch (IOException e) {
            System.err.println("Failed to log. Giving up.");
            System.exit(-1); // we can't output anything to the log file, so just kind of give up.
        }
    }
    public static void verbose(String what, String more)
    {
        if (myLogLevel.ordinal() < loglevels.D_VERBOSE.ordinal()) return;
        try {
            log(what,loglevels.D_VERBOSE,more);
        } catch (IOException e) {
            System.err.println("Failed to log. Giving up.");
            System.exit(-1); // we can't output anything to the log file, so just kind of give up.
        }
    }
    public static void summary(String what, String more)
    {
        if (myLogLevel.ordinal() < loglevels.D_SUMMARY.ordinal()) return;
        try {
            log(what,loglevels.D_SUMMARY,more);
        } catch (IOException e) {
            System.err.println("Failed to log. Giving up.");
            System.exit(-1); // we can't output anything to the log file, so just kind of give up.
        }
    }

}
