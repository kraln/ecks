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

import ecks.protocols.Generic;
import ecks.Utility.Modes;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

public class ChanModes implements Modes {
    // I hate this class. I really do.


    Map <Character, List> modes;

    public ChanModes()
    {
        modes = new HashMap<Character, List>();
    }

    // this class represents modes
    public void applyChanges(String newmodes)
    {
        int plusstart;
        int plusend = 0;
        int minusstart;

        String[] toks = newmodes.split(" ");

        plusstart = toks[0].indexOf('+');
        minusstart = toks[0].indexOf('-');
        if (plusstart>=0) {
            if (minusstart > 0)
                plusend = minusstart;
            else
                plusend = toks[0].length();
        }

        String plusses = "";
        String minuses = "";

        if (plusstart > -1)
        plusses = newmodes.substring(plusstart+1,plusend);

        if (minusstart > -1)
        minuses = newmodes.substring(minusstart+1);

        int numofargssofar = 0;

        for (int i = 0; i < plusses.length(); i++)
        {
            CharSequence w = plusses.subSequence(i, i+1);
            if (modes.containsKey(w.charAt(0)))
            {
                if (Generic.curProtocol.getModeArgs().contains(w))
                {
                    if (modes.get(w.charAt(0)).contains(toks[numofargssofar+1]))
                    {
                      // do nothing - this argument is already on this list.
                    } else {
                      modes.get(w.charAt(0)).add(toks[numofargssofar+1]); // add this argument to the list for this mode
                    }
                    numofargssofar++;
                } else {
                    // do nothing because this mode is alerady set
                }
            } else {
                if (Generic.curProtocol.getModeArgs().contains(w))
                {
                    List t = new ArrayList<String>();
                    t.add(toks[numofargssofar+1]); 
                    modes.put(w.charAt(0), t);
                    numofargssofar++;
                } else {
                    modes.put(w.charAt(0),null); // add mode
                }
            }
        }
        for (int i = 0; i < minuses.length(); i++)
        {
            CharSequence w = minuses.subSequence(i, i+1);
            if (modes.containsKey(w.charAt(0)))
            {
                if (Generic.curProtocol.getModeArgs().contains(w))
                {
                    if (modes.get(w.charAt(0)).contains(toks[numofargssofar+1]))
                    {
                        modes.get(w.charAt(0)).remove(toks[numofargssofar+1]); // remove it
                        if (modes.get(w.charAt(0)).size() == 0)
                            modes.remove(w.charAt(0)); // mode has no more complex arguments, remove mode
                    } else {
                        // do nothing because this complex argument wasn't there
                    }
                    numofargssofar++;
                } else {
                    modes.remove(w.charAt(0)); // mode has no more complex arguments, remove mode
                }
            } else; // do nothing because the mode is alredy not set
        }
    }

    public boolean contains(String what)
    {
        return modes.containsKey(what.charAt(0));
    }
    public String getModes()
    {

        String t ="", ca =" ";
        for (Map.Entry<Character, List> e : modes.entrySet())
        {
            if (e.getValue() != null)
            {
                List j = e.getValue();
                for (int k = 0; k < j.size(); k++)
                {
                    t += e.getKey();
                    ca += j.get(k) + " ";
                }
            } else {
                t += e.getKey();
            }
        }

        return "+" + t + ca.trim();
    }

}
