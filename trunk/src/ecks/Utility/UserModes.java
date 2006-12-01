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

public class UserModes implements Modes {
    // I hate this class. I really do.


    List modes;

    public UserModes() {
        modes = new ArrayList<Character>();
    }

    // this class represents modes
    public void applyChanges(String newmodes) {
        int plusstart;
        int plusend = 0;
        int minusstart;

        String[] toks = newmodes.split(" ");
        String rawm = toks[0];
        plusstart = rawm.indexOf('+');
        minusstart = rawm.indexOf('-');
        if (plusstart >= 0) {
            if (minusstart > 0)
                plusend = minusstart;
            else
                plusend = rawm.length();
        }

        String plusses = "";
        String minuses = "";

        if (plusstart > -1)
            plusses = newmodes.substring(plusstart+1, plusend);

        if (minusstart > -1)
            minuses = newmodes.substring(minusstart+1);

        int numofargssofar = 0;

        for (int i = 0; i < plusses.length(); i++) {
            CharSequence w = plusses.subSequence(i, i + 1);
            if (modes.contains(w.charAt(0))) {
            } else {
                modes.add(w.charAt(0)); // add mode
            }
        }

        for (int i = 0; i < minuses.length(); i++) {
            CharSequence w = minuses.subSequence(i, i + 1);
            if (modes.contains(w.charAt(0))) {
                modes.remove(w.charAt(0)); // remove mode
            }
        }
    }

    public boolean contains(String what) {
        return modes.contains(what.charAt(0));
    }

    public String getModes() {
        //todo: me
        String t = "";
        for (Object e : modes.toArray()) {
            t += e.toString();
        }

        return "+" + t;
    }

}
