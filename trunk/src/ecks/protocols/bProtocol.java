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
package ecks.protocols;

import java.io.BufferedWriter;
import java.io.IOException;
import ecks.*;
import ecks.services.Service;

public abstract class bProtocol implements Protocol {
    Configuration config;

    public bProtocol()
    {
        state = 0;
    }

    public int getState()
    {
        return state;
    }

    public void setConfig(Configuration conf)
    {
        config = conf;
    }

    int state;
    BufferedWriter out;

    public void setBuffers(BufferedWriter o) {
        out = o;
        state = 1;
        Logging.info("PROTOCOL", "Awaiting Identd Check");
    }
}
