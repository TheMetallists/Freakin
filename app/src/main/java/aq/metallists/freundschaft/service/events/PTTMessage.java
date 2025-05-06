package aq.metallists.freundschaft.service.events;

public class PTTMessage {
    public PTTMessage(boolean isKD) {
        isKeyedDown = isKD;
    }

    public boolean isKeyedDown;
}
