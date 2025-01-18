package aq.metallists.freundschaft;

//import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;

import aq.metallists.freundschaft.overridable.FSSoundInterface;
import aq.metallists.freundschaft.overridable.FSUnauthorizedError;
import aq.metallists.freundschaft.overridable.FSUser;
import aq.metallists.freundschaft.overridable.SoundLevelReceiver;
import aq.metallists.freundschaft.vocoder.GSMNativeVocoder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

public class FSClient {

    private FSUser usr;
    private FSServer svr;
    private Thread thl = null;
    private boolean wannaQuit = false;
    private FSSoundInterface soundif = null;
    byte[] nullSound = new byte[3200];
    byte[] nullSound2 = new byte[1600];
    private String _room;
    private boolean effectCompressor;
    private boolean effectLPF;
    private boolean isConnected = false;
    private long pttTimeout = 0;
    private boolean pttLatch = false;

    private FSClient(FSUser _usr, FSServer _svr, String room) {
        this.usr = _usr;
        this.svr = _svr;

        if (room != null) {
            this._room = room;
        } else {
            this._room = "Test";
        }

        Arrays.fill(this.nullSound, (byte) 0);
    }

    public static FSClient createForUser(FSUser _user, String _host, int port, String room) {
        FSClient cli = new FSClient(_user, new FSServer(_host, port), room);
        return cli;
    }

    public void setPttTimeout(long ptot) {
        this.pttTimeout = ptot;
    }

    public void connectLogin() throws IOException, ParserConfigurationException, FSUnauthorizedError {
        this.setConnected(false);
        this.svr.connect();

        // CT:<VX>[Version]</VX><EA>[EMailAddress]</EA><PW>[DynPassWord]</PW>
        //<ON>[CallsignAndUser]</ON><CL>[ClientType]</CL><BC>[BandAndChannel]</BC>
        //<DS>[Description]</DS><NN>[Country]</NN><CT>[CityCityPart]</CT><NT>[Net]</NT>
        FSPacket pak = new FSPacket("CT:");
        pak.addElement("VX", "2014000");
        pak.addElement("EA", this.usr.getEmail());
        pak.addElement("PW", this.usr.getPassword());
        pak.addElement("ON", this.usr.getCallsign());
        pak.addElement("CL", "2");
        pak.addElement("BC", "PC Only");
        pak.addElement("DS", this.usr.getDecaription());
        pak.addElement("NN", this.usr.getCountry());
        pak.addElement("CT", this.usr.getCity());
        pak.addElement("NT", this._room);

        this.svr.sendBytePacket(pak.getPacked().getBytes("WINDOWS-1251"));

        System.err.println("RESP:");
        System.err.println(this.svr.getLinePacket());

        String handshakeRequest = this.svr.getLinePacket();

        if (!handshakeRequest.contains("<AL>OK</AL>")) {
            this.wannaQuit = true;
            throw new FSUnauthorizedError("Authorization Error");
        }

        //System.err.println(handshakeRequest);
        String handshakeResponce = FSServer.getKPResponce(handshakeRequest);
        if (handshakeResponce != null) {
            //System.out.println(handshakeResponce);
            this.svr.sendStringPacket(handshakeResponce.concat("\r\n"));
        }


        //System.err.println(this.svr.getLinePacket());
        this.svr.sendStringPacket("RX0\r\n");
        this.svr.sendStringPacket("ST:0\r\n");
        //this.svr.sendStringPacket("TXR:1\r\n");
    }

    public void connectRegister() throws Exception {
        this.svr.connect();

        //IG:<ON>[CallsignAndUser]</ON><EA>[EMailAddress]</EA
        //><BC>[BandAndChannel]</BC><DS>[Description]</DS><NN>[Country]</NN><CT>[CityCityPart]</CT>
        FSPacket pak = new FSPacket("IG:");
        pak.addElement("ON", "T4ST");
        pak.addElement("EA", this.usr.getEmail());
        pak.addElement("BC", "PC Only");
        pak.addElement("DS", "Demo dummy!");
        pak.addElement("NN", "DE");
        pak.addElement("CT", "Denmark");

        this.svr.sendStringPacket(pak.getPacked());

        System.err.println(this.svr.getLinePacket());

    }

    public void enterProtocolLoop() throws ParserConfigurationException, IOException, FSUnauthorizedError {
        if (this.thl != null) {
            return;
        }

        if (this.soundif != null) {
            this.soundif.setConnecting(true);
        }

        connectLogin();

        this.thl = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!wannaQuit) {
                    try {
                        protocolLoop();
                    } catch (Exception x) {
                        setConnected(false);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {

                        }
                        if (soundif != null) {
                            soundif.setConnecting(true);
                        }
                        x.printStackTrace();
                        System.err.println("RECONNECT, RECONNECT, RECONNECT!");
                        try {
                            connectLogin();
                        } catch (Exception xc) {
                            xc.printStackTrace();
                            System.err.println("UNABLE TO RECONNECT!!!");
                            //wannaQuit = true;
                        }
                    }
                }
                try {
                    setConnected(false);
                    svr.disconnect();
                } catch (Exception x) {
                }
            }
        });

        this.thl.start();
    }

    public void join() {
        if (this.thl == null) {
            return;
        }

        try {
            this.thl.join();
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    public void setSoundInterface(FSSoundInterface _sif) {
        this.soundif = _sif;
    }

    private GSMNativeVocoder downlinkVocoder = null;

    private boolean isPttEnabled = false;
    private boolean isPttEnabledLatch = false;

    public void setPtt(boolean ptt) {
        if (ptt && this.pttTimeout > 0) {
            if (!pttLatch) {
                this.pttEndTime = System.currentTimeMillis() + (this.pttTimeout * 1000);
            } else {
                return;
            }
        } else {
            pttLatch = false;
        }
        this.isPttEnabled = ptt;
        if (this.soundif != null) {
            this.soundif.setPtt(this.isPttEnabled);
        }
    }

    public void allowCompressor(boolean state) {
        this.effectCompressor = state;
    }

    public void allowLPF(boolean state) {
        this.effectLPF = state;
    }

    private void onIncomingVoicePacket() throws Exception {
        byte[] voiceDL = new byte[327];
        int voiceDLLen = this.svr.getBytePacket(voiceDL);

        if ((voiceDLLen - 2) % 65 != 0) {
            System.out.print(voiceDLLen);
            System.out.println(": Voice DL error!");
            return;
        }

        this.setActiveSpeaker(voiceDL);

        byte[] voiceData = new byte[325];
        System.arraycopy(voiceDL, 2, voiceData, 0, voiceData.length);

        if (this.downlinkVocoder == null) {
            this.downlinkVocoder = new GSMNativeVocoder(this.effectCompressor, this.effectLPF);
            this.downlinkVocoder.connectXLRJack((SoundLevelReceiver) this.soundif);
            System.err.println("downlinkVocoder CREATED");
            if (this.soundif != null) {
                this.soundif.setVox(true);
            }
        }

        if (this.soundif != null) {
            byte[] decoded = this.downlinkVocoder.decode(voiceData);
            //System.out.println(decoded.length);
            this.soundif.playVoicePacket(decoded);
        }
    }

    private short activeSpeakerOffsetCache = -1;

    private void silenceAllSpeakers() {
        for (int ou = 0; ou < this.onlineUsers.length; ou++) {
            this.onlineUsers[ou].setXmitting(false);
        }
        this.activeSpeakerOffsetCache = -1;
    }

    private void setActiveSpeaker(byte[] speakerId) {
        ByteBuffer bufa = ByteBuffer.wrap(speakerId).order(ByteOrder.BIG_ENDIAN);
        short speakerOffset = bufa.getShort();

        if (this.activeSpeakerOffsetCache != speakerOffset) {
            this.activeSpeakerOffsetCache = speakerOffset;

            if (speakerOffset <= this.onlineUsers.length) {
                this.onlineUsers[speakerOffset - 1].setXmitting(true);
                if (this.soundif != null) {
                    this.soundif.setClientList(this.onlineUsers);
                }
            }
        }
    }

    long pttEndTime = 0;

    private void onOutgoingVoicePacket() throws Exception {
        if (this.pttTimeout > 0) {
            if (System.currentTimeMillis() > pttEndTime) {
                this.isPttEnabled = false;
                if (!this.pttLatch) {
                    this.soundif.pttTimedOut();
                }
                this.pttLatch = true;
            }
        }

        if (this.isPttEnabled) {
            this.svr.sendStringPacket("TX1\r\n");

            byte[] soundPacket = new byte[3200];
            long time_a1 = System.currentTimeMillis();
            int recordCnt = this.soundif.recordVoicePacket(soundPacket);
            System.out.print("RECORDING: ");
            System.out.println(System.currentTimeMillis() - time_a1);
            if (recordCnt != 3200) {
                throw new Exception("TX MISALLIGNED!");
            }

            if (this.downlinkVocoder == null) {
                this.downlinkVocoder = new GSMNativeVocoder(this.effectCompressor, this.effectLPF);
                this.downlinkVocoder.connectXLRJack((SoundLevelReceiver) this.soundif);
                System.err.println("downlinkVocoder CREATED");
                //this.downlinkVocoder.doCompressor = this.effectCompressor;
            }

            //byte[] bin = new byte[325];
            time_a1 = System.currentTimeMillis();
            byte[] bin = this.downlinkVocoder.encode(soundPacket);
            System.out.print("VOCODER: ");
            System.out.println(System.currentTimeMillis() - time_a1);
            if (bin.length != 325) {
                System.out.print(bin.length);
                System.out.println("TX INEQ!!!");
                System.exit(22);
            }

            soundPacket = null;

            time_a1 = System.currentTimeMillis();
            this.svr.sendBytePacket(bin);
            System.out.print("SENDER: ");
            System.out.println(System.currentTimeMillis() - time_a1);
            bin = null;
        } else {
            // someone wants to snitch on us!
            this.svr.sendStringPacket("RX0\r\n");
        }
    }

    private FSRoomMember[] onlineUsers = new FSRoomMember[]{};
    private String[] roomList = new String[]{};

    private void sendPacketPreamble() throws Exception {
        if (this.isPttEnabled) {
            // initialize transmission packet
            this.svr.sendStringPacket("TX0\r\n");
            if (!this.isPttEnabledLatch) {
                this.isPttEnabledLatch = true;
            }

        } else if (this.isPttEnabledLatch) {
            // ptt released - switch modes on the server
            this.svr.sendStringPacket("RX0\r\nP\r\n");
            this.isPttEnabledLatch = false;

            this.silenceAllSpeakers();

            if (this.soundif != null) {
                this.soundif.setClientList(this.onlineUsers);
            }

        } else {
            this.svr.sendStringPacket("P\r\n");
        }
    }

    private void onIDLEPacket() {
        setConnected(true);
        if (this.downlinkVocoder != null) {
            this.downlinkVocoder.close();
            System.out.println("downlinkVocoder destroyed");
            this.downlinkVocoder = null;
            if (this.soundif != null) {
                this.soundif.setVox(false);

                this.silenceAllSpeakers();
                this.soundif.setClientList(this.onlineUsers);
            }
        }

        if (this.soundif != null) {
            this.soundif.playVoicePacket(this.nullSound);

            if (!this.isPttEnabled) {
                this.soundif.recordVoicePacket(nullSound2);
            }
        }
    }

    private void onUserListPacket() throws Exception {
        System.out.println("DT_CLIENT_LIST: ");

        byte aci[] = new byte[2];
        this.svr.getBytePacket(aci);

        String numpak2 = this.svr.getLinePacket().trim();
        System.out.println(numpak2);
        int npak2 = Integer.parseInt(numpak2);

        List<FSRoomMember> users = new ArrayList<FSRoomMember>();
        for (int i = 0; i < npak2; i++) {
            users.add(FSRoomMember.FromString(this.svr.getLinePacket().trim()));
        }

        this.onlineUsers = new FSRoomMember[users.size()];
        onlineUsers = users.toArray(onlineUsers);
        if (this.soundif != null) {
            this.soundif.setClientList(this.onlineUsers);
        }
    }

    private boolean isTxApproved = false;

    private void receivePackets() throws Exception {
        if (this.isPttEnabled && this.isTxApproved) {
            this.onOutgoingVoicePacket();
            return;
        }
        this.sendPacketPreamble();

        byte[] pktype = new byte[1];
        long time_a1 = System.currentTimeMillis();
        this.svr.getBytePacket(pktype);

        int pktyp = (int) pktype[0];

        switch (pktyp) {
            case FSServer.DT_IDLE:
                if (this.soundif != null) {
                    this.soundif.setConnecting(false);
                }
                this.onIDLEPacket();
                this.isTxApproved = false;
                break;
            case FSServer.DT_DO_TX:
                this.isTxApproved = true;
                byte clientXmittingBuf[] = new byte[2];
                this.svr.getBytePacket(clientXmittingBuf);
                this.setActiveSpeaker(clientXmittingBuf);

                break;
            case FSServer.DT_CLIENT_LIST:
                this.onUserListPacket();
                break;
            case FSServer.DT_NET_NAMES:
                System.err.println("DT_NET_NAMES: ");

                try {
                    String numba = this.svr.getLinePacket().trim();
                    System.err.print("[NETWORKnumba:] ");
                    System.err.println(numba);

                    int npak = Integer.parseInt(numba);
                    List<String> rooms = new ArrayList<>();
                    for (int i = 0; i < npak; i++) {
                        String netzwerk = this.svr.getLinePacket().trim();
                        rooms.add(netzwerk);
                        System.err.print("[NETWORK:] ");
                        System.err.println(netzwerk);
                    }

                    this.roomList = rooms.toArray(new String[]{});
                    if (this.soundif != null) {
                        this.soundif.setNetworksList(this.roomList);
                    }

                } catch (Exception x) {
                }
                break;
            case FSServer.DT_VOICE_BUFFER:
                if (this.soundif != null) {
                    this.soundif.setConnecting(false);
                }
                this.onIncomingVoicePacket();
                break;
            default:
                byte[] data1 = new byte[1024];
                int datacnt1 = this.svr.getBytePacket(data1);

                System.out.println(String.format("PKT T: %d, LEN: %d", pktyp, datacnt1));
        }

    }

    private void protocolLoop() throws Exception {
        this.receivePackets();

        //Thread.sleep(10);
    }

    public void abort() {
        try {
            this.wannaQuit = true;
            //this.svr.disconnect();

            // klar the UI from active users
            this.soundif.setClientList(new FSRoomMember[]{});
            this.join();
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    private void setConnected(boolean newConnected) {
        if (newConnected != this.isConnected && this.soundif != null) {
            this.soundif.setConnected(newConnected);
        }
        this.isConnected = newConnected;
    }

    /**
     * Re-emits all status objects (netlist, client list, etc.)
     */
    public void requestDataUpdate() {
        if (this.soundif != null) {
            this.soundif.setClientList(this.onlineUsers);
            this.soundif.setNetworksList(this.roomList);
            this.soundif.setConnected(this.isConnected);
        }
    }

}
