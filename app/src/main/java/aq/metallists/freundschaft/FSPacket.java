package aq.metallists.freundschaft;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import java.io.ByteArrayOutputStream;

public class FSPacket {

    private String prefix;

    public FSPacket(String _prefix) throws ParserConfigurationException {
        this.prefix = _prefix;
        this.populateXmlBuilder();
    }

    public FSPacket() throws Exception {
        this.prefix = "";
        this.populateXmlBuilder();
    }

    private Document doc;
    private Element root;

    private void populateXmlBuilder() throws ParserConfigurationException {
        this.doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        this.root = this.doc.createElement("ROOT");
        this.doc.appendChild(this.root);
    }

    public void addElement(String name, String value) {
        Element ne = this.doc.createElement(name);
        ne.setTextContent(value);
        this.root.appendChild(ne);
    }

    public String getPacked() {
        String out = this.prefix.concat(this.innerXml(this.root));
        return out.concat("\r\n");
    }

    private String innerXml(Node node) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            Source source = new DOMSource(node);
            Result target = new StreamResult(out);
            transformer.transform(source, target);
            //System.err.println(out.toString().replace("<ROOT>", "").replace("</ROOT>", ""));
            return out.toString().replace("<ROOT>", "").replace("</ROOT>", "");
        } catch (Exception x) {
            x.printStackTrace();
            return "";
        }



        /*DOMImplementationLS lsImpl = (DOMImplementationLS) node.getOwnerDocument().getImplementation();
        LSSerializer lsSerializer = lsImpl.createLSSerializer();
        lsSerializer.getDomConfig().setParameter("xml-declaration", false);
        NodeList childNodes = node.getChildNodes();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < childNodes.getLength(); i++) {
            sb.append(lsSerializer.writeToString(childNodes.item(i)));
        }
        return sb.toString();*/
    }

}
