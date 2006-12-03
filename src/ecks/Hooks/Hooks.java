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
package ecks.Hooks;

import ecks.services.Service;
import ecks.Logging;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class Hooks {
    public static enum Events {
        E_JOINCHAN, E_PARTCHAN, E_PRIVMSG, E_KICK, E_MODE, E_UMODE, E_TOPIC
    }

    public static Map<Service, List<Events>> regHooks;

    public static void initialize() {
        regHooks = new HashMap<Service, List<Events>>();
    }

    public static void regHook(Service who, Events what) {
        if (regHooks.containsKey(who)) {
            List<Events> e = regHooks.get(who);
            e.add(what);
            regHooks.put(who, e);
        } else {
            List<Events> e = new ArrayList<Events>();
            e.add(what);
            regHooks.put(who, e);
        }
    }

    public static void unregHook(Service who, Events what) {
        if (regHooks.containsKey(who)) {
            List<Events> e = regHooks.get(who);
            if (e.contains(what)) {
                e.remove(what);
                regHooks.remove(who);
                regHooks.put(who, e);
            } else Logging.warn("HOOKS", "Tried to remove a hook for that doesn't exist!");
        } else Logging.warn("HOOKS", "Tried to remove a hook for a service that doesn't exist!");
    }

    public static void hook (Events what, String source, String target, String args){
        for(Map.Entry<Service, List<Events>> e : regHooks.entrySet()) {
            if (e.getValue().contains(what)) { // if there's a hook here
                e.getKey().hookDispatch(what,source,target,args); // dispatch hook      
            }
        }
    }

}
