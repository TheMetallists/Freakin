package aq.metallists.freundschaft;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import aq.metallists.freundschaft.tools.Logger;

public class FSRoomMember {

    private String uid = "";
    private String callsign = "";
    private String clientTypeStr = "";
    private String callsignSupplement = "";
    private String comment = "";

    public static FSRoomMember FromString(String input) {
        FSRoomMember obj = new FSRoomMember();

        obj.uid = input;
        try {
            obj.parseStringPacket();
        } catch (Exception x) {
            x.printStackTrace();
        }
        return obj;
    }

    private void parseStringPacket() throws Exception {
        // TODO: exception-free parsing
        Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();

        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<USER>" + this.uid + "</USER>"));
        doc = db.parse(is);

        FSRoomDocument frd = new FSRoomDocument(doc);

        String preCallsign = frd.getON();
        if (preCallsign.split(", ").length > 0) {
            preCallsign = preCallsign.split(", ")[0];
        }

        String city = frd.getNN();
        String cityPart = frd.getCT();
        if (cityPart.split(" - ").length > 1) {
            city = cityPart.split(" - ")[0];
            cityPart = cityPart.split(" - ")[1];
        }

        this.callsign = preCallsign;
        this.callsignSupplement = preCallsign + ", " + frd.getBC() + ", " + city;

        this.comment = frd.getDS() + ", " + cityPart + ", " + frd.getNN();

        clientTypeStr = frd.getBC();

        if (clientTypeStr.equals("PC Only")) {
            this.clientType = TYPE_PC;
        } else if (clientTypeStr.equals("Parrot")) {
            this.clientType = TYPE_PARROT;
        } else {
            this.clientType = TYPE_RADIO;
        }
    }

    private boolean isXmittin;
    private int clientType = 0;

    public static final int TYPE_PC = 0;
    public static final int TYPE_RADIO = 1;
    public static final int TYPE_PARROT = 2;

    public int getClientType() {
        return this.clientType;
    }

    public String getCientDescription() {
        return this.clientTypeStr;
    }

    public String getCallsignSupplement() {
        return this.callsignSupplement;
    }

    public String getComment() {
        return this.comment;
    }

    public void setXmitting(boolean isXmit) {
        this.isXmittin = isXmit;
        if (isXmit) {
            Logger.getInstance().i("[USER TX: ] ".concat(this.getCallsign()));
        }

    }

    public boolean getXmitting() {
        return isXmittin;
    }

    public String getCallsign() {
        return this.callsign;
    }

    private class FSRoomDocument {
        private Document doc;

        private FSRoomDocument(Document _doc) {
            doc = _doc;
        }

        @NotNull
        private String getON() {
            String r = doc.getElementsByTagName("ON").item(0).getTextContent();
            if (r != null)
                return r;
            else
                return "NULL";
        }

        @NotNull
        private String getBC() {
            String r = doc.getElementsByTagName("BC").item(0).getTextContent();
            if (r != null)
                return r;
            else
                return "NULL";
        }

        @NotNull
        private String getNN() {
            String r = doc.getElementsByTagName("NN").item(0).getTextContent();
            if (r != null)
                return r;
            else
                return "NULL";
        }

        @NotNull
        private String getCT() {
            String r = doc.getElementsByTagName("CT").item(0).getTextContent();
            if (r != null)
                return r;
            else
                return "NULL";
        }

        @NotNull
        private String getDS() {
            String r = doc.getElementsByTagName("DS").item(0).getTextContent();
            if (r != null)
                return r;
            else
                return "NULL";
        }
    }

}
