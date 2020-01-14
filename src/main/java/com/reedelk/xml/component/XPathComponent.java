package com.reedelk.xml.component;

import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

@ESBComponent("XPath Extract")
@Component(service = XPathComponent.class, scope = ServiceScope.PROTOTYPE)
public class XPathComponent implements ProcessorSync {

    @Property("XPath Expression")
    private String expression;


    private DocumentBuilder builder;
    private XPathExpression xPathExpression;
    private Transformer xform;

    @Override
    public void initialize() {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        try {
            builder = builderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new ESBException(e);
        }

        XPath xPath = XPathFactory.newInstance().newXPath();
        try {
            xPathExpression = xPath.compile(expression);
        } catch (XPathExpressionException e) {
            throw new ESBException(e);
        }

        try {
            xform = TransformerFactory.newInstance().newTransformer();
            xform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes"); // optional
            xform.setOutputProperty(OutputKeys.INDENT, "yes"); // optional
        } catch (TransformerConfigurationException e) {
            throw new ESBException(e);
        }
    }

    @Override
    public Message apply(Message message, FlowContext flowContext) {

        String payload = message.payload();

        InputStream fileInputStream = new ByteArrayInputStream(payload.getBytes());
        try {
            Document xmlDocument = builder.parse(fileInputStream);
            NodeList nodeList = (NodeList) xPathExpression.evaluate(xmlDocument, XPathConstants.NODESET);

            List<String> results = new ArrayList<>();
            for (int i = 0; i < nodeList.getLength(); i++) {
                String token = convertToString(nodeList.item(i));
                results.add(token);
            }
            return MessageBuilder.get().withJavaObject(results).build();
        } catch (XPathExpressionException | SAXException | IOException e) {
            throw new ESBException(e);
        }
    }

    @Override
    public void dispose() {
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    private String convertToString(Node element) {
        StringWriter buf = new StringWriter();
        try {
            xform.transform(new DOMSource(element), new StreamResult(buf));
            return buf.toString();
        } catch (TransformerException e) {
            throw new ESBException(e);
        }
    }
}