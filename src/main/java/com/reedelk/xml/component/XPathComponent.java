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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@ESBComponent("XPath")
@Component(service = XPathComponent.class, scope = ServiceScope.PROTOTYPE)
public class XPathComponent implements ProcessorSync {

    @Property("XPath Expression")
    private String expression;


    private DocumentBuilder builder;
    private XPath xPath;

    @Override
    public void initialize() {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        try {
            builder = builderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new ESBException(e);
        }
        this.xPath = XPathFactory.newInstance().newXPath();
    }

    @Override
    public void dispose() {

    }

    @Override
    public Message apply(Message message, FlowContext flowContext) {

        String payload = message.payload();

        InputStream fileIS = new ByteArrayInputStream(payload.getBytes());
        try {
            Document xmlDocument = builder.parse(fileIS);
            String expression = "/Tutorials/Tutorial";
            NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);


            return MessageBuilder.get().withJavaObject(nodeList).build();
        } catch (SAXException | IOException | XPathExpressionException e) {
            throw new ESBException(e);
        }
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }
}
