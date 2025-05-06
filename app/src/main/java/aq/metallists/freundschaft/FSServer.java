package aq.metallists.freundschaft;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import aq.metallists.freundschaft.tools.Logger;

class FSServer {

    private String host;
    private int port;
    private Socket sck;
    private InputStream sis;
    //private BufferedReader brd;
    private OutputStream sos;
    //DataOutputStream dos;
    private String connCharset = "WINDOWS-1251";

    public FSServer(String _host, int _port) {
        this.host = _host;
        this.port = _port;
    }

    public void connect() throws IOException {
        sck = new Socket(host, port);
        this.sis = sck.getInputStream();

        this.sos = sck.getOutputStream();
    }

    public void disconnect() throws Exception {
        this.sis.close();
        this.sos.close();
        this.sck.close();
        this.sck = null;
    }

    public void sendStringPacket(String packet) throws IOException {
        sos.write(packet.getBytes(connCharset));
        sos.flush();
    }

    public String getLinePacket() throws IOException {
        StringWriter sw = new StringWriter();
        byte c;
        while (true) {
            c = (byte) (this.sis.read() & 0xFF);

            if (c == 0x00 || c == '\n') {
                if (c == 0x00) {
                    Logger.getInstance().w("ZERO-TERMINATED STRING RXD!");
                }
                sw.append('\n');

                if (sw.toString().equals("\n") || sw.toString().equals("\r\n")) {
                    // is this right?
                    Logger.getInstance().w("READ-STRING SLIP-TROUGHT!");
                    return getLinePacket();
                }

                return new String(sw.toString().getBytes(this.connCharset), this.connCharset);
            }
            sw.write(new String(new byte[]{c}, this.connCharset));
        }
    }
    
    /*public String getStringPacket() throws Exception {
        //return this.brd.readLine();
        
        //this.sis.read(new byte[1024]);
        return "OK";
        /*String line;
        while (true) {
            line = this.brd.readLine();
            if (line != null) {
                return line;
            }
        }* /
    }*/

    public int getByteLeftovers(byte[] inb) throws Exception {
        return this.sis.read(inb, 0, this.sis.available());
    }

    public int getBytePacket(byte[] inb) throws Exception {
        return this.sis.read(inb);
    }

    public void sendBytePacket(byte[] packet) throws IOException {
        this.sos.write(packet);
        this.sos.flush();
    }


    //checksum functions

    public static String getKPResponce(String inpak) {
        final Pattern pattern = Pattern.compile("<KP>(\\d{6})<\\/KP>", Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(inpak);

        if (matcher.find() && matcher.groupCount() > 0) {
            try {
                return calculateHandshakeSum(matcher.group(1));
            } catch (Exception x) {
                x.printStackTrace();
                return null;
            }
        }

        return null;
    }

    public static String calculateHandshakeSum(String inm) throws Exception {
        if (inm == null || inm.length() != 6) {
            throw new Exception("Invalid input");
        }

        int in_a = Integer.parseInt(new String(new char[]{
                inm.charAt(0),
                inm.charAt(1)
        }));
        int in_b = Integer.parseInt(new String(new char[]{
                inm.charAt(2),
                inm.charAt(3)
        }));
        int in_c = Integer.parseInt(new String(new char[]{
                inm.charAt(4),
                inm.charAt(5)
        }));

        int out_int = (in_a + 2) * (in_b + 1) + (in_c + 4) * (in_c + 7);
        char[] outc = new char[5];
        String outs = Integer.toString(out_int);
        outs.getChars(0, outs.length(), outc, 5 - outs.length());

        //System.out.println(outc);
        for (int i = 0; i < outc.length; i++) {
            if ((int) outc[i] == 0)
                outc[i] = '0';
        }
        return new String(new char[]{outc[3], outc[0], outc[2], outc[4], outc[1]});
    }

    // protocol constants
    public static final int DT_IDLE = 0;
    public static final int DT_DO_TX = 1;
    public static final int DT_VOICE_BUFFER = 2;
    public static final int DT_CLIENT_LIST = 3;
    public static final int DT_TEXT_MESSAGE = 4;
    public static final int DT_NET_NAMES = 5;
    public static final int DT_ADMIN_LIST = 6;
    public static final int DT_ACCESS_LIST = 7;
    public static final int DT_BLOCK_LIST = 8;
    public static final int DT_MUTE_LIST = 9;
    public static final int DT_ACCESS_MODE = 10;

}
