package aq.metallists.freundschaft.overridable;

import aq.metallists.freundschaft.FSRoomMember;

public interface FSSoundInterface {

    public void playVoicePacket(byte[] packet);

    public int recordVoicePacket(byte[] packet);

    public void setVox(boolean newPtt);
    public void setPtt(boolean newPtt);

    public void setClientList(FSRoomMember[] clients);
    public void setNetworksList(String []networks);

    public void setConnected(boolean connected);

    public void setConnecting(boolean isConn);
    public void setAudioLevel(short tgtLevel);

    public void pttTimedOut();
    public void onDisconnected(String message);
}
