package ecks.services;

import ecks.Configuration;
import ecks.Hooks.Hooks;
import ecks.protocols.Generic;
import org.w3c.dom.NodeList;

public class SrvStress extends bService {
    public String name = "SrvStress";
    public long number = 0;

    public void introduce() {
        Hooks.regHook(this, Hooks.Events.E_PRIVMSG);
        for (number = 0; number < 2000; number++) {
            SrvStress Stress = new SrvStress();
            Stress.name = "Stress" + number;
            Generic.srvIntroduce(Stress);
            if (!(Configuration.Config.get("debugchan").equals("OFF"))) {
                Generic.curProtocol.srvJoin(Stress, "#", "+stn");
                StressThread st = new StressThread(Stress);
                st.start();
            }
        }
    }

    public String getname() {
        return name;
    }

    public void setname(String nname) {
        name = nname;
    }

    public String getSRVDB() {
        return "";
    }

    public void loadSRVDB(NodeList XMLin) {
    }

    public int getcount() {
        return 0;
    }

    public void hookDispatch(Hooks.Events what, String source, String target, String args) {
        super.hookDispatch(this, what, source, target, args);
    }

}

class StressThread extends Thread {

    SrvStress s;

    StressThread(SrvStress ss) {
        s = ss;
    }

    public void run() {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        for (; ;) {
            Generic.curProtocol.outPRVMSG(s, "#", "This is a madness. THIS IS INSPIRCDDAAAAAAAAAAAAAAAA!!!");
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Generic.curProtocol.srvPart(s, "#", "JOINPARTING");
            Generic.curProtocol.srvJoin(s, "#", "");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}