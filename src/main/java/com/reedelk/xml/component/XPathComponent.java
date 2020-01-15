package com.reedelk.xml.component;

import com.reedelk.runtime.api.annotation.Default;
import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.Hint;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.script.ScriptEngineService;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import com.reedelk.xml.commons.XPathNamespaceContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
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
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@ESBComponent("XPath Extract")
@Component(service = XPathComponent.class, scope = ServiceScope.PROTOTYPE)
public class XPathComponent implements ProcessorSync {

    @Property("XPath Expression")
    @Default("")
    @Hint("//book[@year>2001]/title/text()")
    private DynamicString expression;

    @Property("Context")
    private XPathConfiguration configuration;

    @Reference
    private ScriptEngineService scriptEngine;

    private DocumentBuilder builder;
    private XPathExpression xPathExpression;
    private Transformer xform;
    private XPath xPath;

    @Override
    public void initialize() {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        try {
            builder = builderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new ESBException(e);
        }

        xPath = XPathFactory.newInstance().newXPath();
        configureNamespaceContext(xPath);
        if (!expression.isScript()) {
            try {
                String xPathExpressionValue = expression.value(); // TODO: Check if not null
                xPathExpression = xPath.compile(xPathExpressionValue);
            } catch (XPathExpressionException e) {
                throw new ESBException(e);
            }
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

            NodeList nodeList = evaluate(xmlDocument, flowContext, message);

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

    private NodeList evaluate(Document xmlDocument, FlowContext context, Message message) throws XPathExpressionException {
        if (expression.isScript()) {
            String evaluated = scriptEngine.evaluate(expression, context, message).orElse(null);
            XPathExpression expression = xPath.compile(evaluated);
            return (NodeList) expression.evaluate(xmlDocument, XPathConstants.NODESET);

        } else {
            return (NodeList) xPathExpression.evaluate(xmlDocument, XPathConstants.NODESET);
        }
    }

    public void setExpression(DynamicString expression) {
        this.expression = expression;
    }

    public void setConfiguration(XPathConfiguration configuration) {
        this.configuration = configuration;
    }

    private void configureNamespaceContext(XPath xPath) {
        Optional.ofNullable(configuration).ifPresent(config -> {
            Map<String, String> prefixNamespaceMap = configuration.getPrefixNamespaceMap();
            if (prefixNamespaceMap != null) {
                XPathNamespaceContext context = new XPathNamespaceContext(prefixNamespaceMap);
                xPath.setNamespaceContext(context);
            }
        });
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