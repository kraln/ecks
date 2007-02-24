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

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.ArrayList;

import sun.misc.BASE64Encoder;
import ecks.Threads.EmailThread;
import ecks.services.SrvAuth;
import ecks.services.modules.CommandDesc;

public class util {

    public static List<Thread> threads;

    public static String getVersion() {
        return "0.5B";
    }

    public static Thread startThread(Thread whattostart) {
        if (threads == null) // not set
            threads = new ArrayList<Thread>();
        threads.add(whattostart);
        Logging.verbose("THREADING", "New thread created!");
        return whattostart;
    }

    public static List<Thread> getThreads() {
        return threads;
    }

    public static synchronized String pad(String s, int n) {
        return paddingString(s, n, ' ', false);
    }

    public static String getTS()
    {
        return String.valueOf(System.currentTimeMillis() / 1000);
    }

    public static synchronized String paddingString(String s, int n, char c, boolean paddingLeft) {
        StringBuffer str = new StringBuffer(s);
        int strLength = str.length();
        if (n > 0 && n > strLength) {
            for (int i = 0; i <= n; i++) {
                if (paddingLeft) {
                    if (i < n - strLength) str.insert(0, c);
                } else {
                    if (i > strLength) str.append(c);
                }
            }
        }
        return str.toString();
    }

    public static long ip2long(InetAddress ip) {
        long l = 0;
        byte[] addr = ip.getAddress();

        if (addr.length == 4) { //IPV4
            for (int i = 0; i < 4; ++i)
                l += (((long) addr[i] & 0xFF) << 8 * (3 - i));
        } else { //IPV6
            return 0;  //Have no idea how to deal with those
        }
        return l;
    }

    public static boolean checkemail(String input) {
        return true;
        // return input.matches("(.+)@(.+)*");
    }

    public static boolean sanitize(String input) { // verify string is good for the database
        return !input.contains("\"") && !input.contains("\'") && input.matches("(\\w+)*");
    }

    public static void SendRegMail(String to, String code) {
        startThread(new Thread(new EmailThread(to, code))).start();
    }

    public static String readFileAsString(String filePath)
            throws java.io.IOException {
        StringBuffer sb = new StringBuffer(1024);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        char[] chars = new char[1024];
        int numRead = 0;
        while (numRead > -1) {
            numRead = reader.read(chars);
            sb.append(String.valueOf(chars));
        }

        reader.close();

        return sb.toString();
    }

    public static String makeCookie() {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        String pass = "";
        for (int x = 0; x < 10; x++) {
            int i = (int) Math.floor(Math.random() * 62);
            pass += chars.charAt(i);
        }
        return pass;
    }

    public static String encodeUTF(String what) {
        String result = null;
        try {
            result = URLEncoder.encode(what, "UTF-8");
        }
        catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("UTF-8 not supported", ex);
        }
        return result;
    }
    
    public static boolean checkaccess(String user, int level)
    {
        return ((SrvAuth) Configuration.getSvc().get(Configuration.authservice)).checkAccess(user.toLowerCase()).ordinal() >= level;
    }

    public static String decodeUTF(String what) {
        String result = null;
        try {
            result = URLDecoder.decode(what, "UTF-8");
        }
        catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("UTF-8 not supported", ex);
        }
        return result;
    }

    public static synchronized String hash(String plaintext) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        try {
            md.update(plaintext.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            return null;
        }
        byte raw[] = md.digest();
        return (new BASE64Encoder()).encode(raw);
    }

     static class CustomException extends Exception {
        CustomException(String message) {
            super(message);
        }
    }

}

