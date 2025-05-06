package aq.metallists.freundschaft;


import aq.metallists.freundschaft.overridable.FSUser;
import aq.metallists.freundschaft.overridable.FSSoundInterface;

public class FSTestRide {
    public static void main(String[] args) throws Exception {
        System.out.println("TESTING...");
        FSUser usr = new FSUser("test2@mail.com", "tezting1234");
        FSClient fsc = FSClient.createForUser(usr, "127.0.0.1", 10024,null);
        
        FSSoundInterface fsi = new FSSoundInterface(null);
        fsc.setSoundInterface(fsi);
        
        fsc.connectLogin();
        fsc.enterProtocolLoop();
        fsc.join();
    }
}
