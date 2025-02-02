package aq.metallists.freundschaft;


import aq.metallists.freundschaft.overridable.FSUser;
import aq.metallists.freundschaft.overridable.FSVedroidSoundInterface;

public class FSTestRide {
    public static void main(String[] args) throws Exception {
        System.out.println("TESTING...");
        FSUser usr = new FSUser("test2@mail.com", "tezting1234");
        FSClient fsc = FSClient.createForUser(usr, "127.0.0.1", 10024,null);
        
        FSVedroidSoundInterface fsi = new FSVedroidSoundInterface(null);
        fsc.setSoundInterface(fsi);
        
        fsc.connectLogin();
        fsc.enterProtocolLoop();
        fsc.join();
    }
}
