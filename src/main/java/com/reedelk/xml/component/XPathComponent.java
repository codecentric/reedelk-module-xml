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
import net.sf.saxon.s9api.*;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.w3c.dom.Node;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.io.ByteArrayInputStream;
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

    private DocumentBuilder documentBuilder;
    private XPathExpression xPathExpression;
    private Transformer xform;
    private XPathExecutable xPathExecutable;
    private XPathCompiler xPathCompiler;
    private Processor processor;

    @Override
    public void initialize() {
        processor = new Processor(false);
        documentBuilder = processor.newDocumentBuilder();

        xPathCompiler = processor.newXPathCompiler();
        configureNamespaceContext();


        // If it is not a script we can pre-compile it, if it is a script
        // we cannot do it and we must re-evaluate it every time.
        if (!expression.isScript()) {
            try {
                // TODO: Check if not null and throw suitable exception.
                String xPathExpressionValue = expression.value();
                xPathExecutable = xPathCompiler.compile(xPathExpressionValue);
            } catch (SaxonApiException e) {
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
            XdmNode node = documentBuilder.build(new StreamSource(fileInputStream));

            XdmValue nodeList = evaluate(node, flowContext, message);

            List<String> results = new ArrayList<>();
            nodeList.stream().forEach((Consumer<XdmItem>) xdmItem ->
                    results.add(xdmItem.toString()));
            return MessageBuilder.get().withJavaObject(results).build();
        } catch (XPathExpressionException | SaxonApiException e) {
            throw new ESBException(e);
        }
    }

    private XdmValue evaluate(XdmNode xmlDocument, FlowContext context, Message message) throws XPathExpressionException {
        if (expression.isScript()) {
            //String evaluated = scriptEngine.evaluate(expression, context, message).orElse(null);
            //XPathExpression expression = x.compile(evaluated);
            //return (NodeList) expression.evaluate(xmlDocument, XPathConstants.NODESET);
            return null;

        } else {
            XPathSelector load = xPathExecutable.load();
            try {
                load.setContextItem(xmlDocument);
                return load.evaluate();
            } catch (SaxonApiException e) {
                throw new ESBException(e);
            }
        }
    }

    public void setExpression(DynamicString expression) {
        this.expression = expression;
    }

    public void setConfiguration(XPathConfiguration configuration) {
        this.configuration = configuration;
    }

    private void configureNamespaceContext() {
        Optional.ofNullable(configuration).ifPresent(config -> {
            Map<String, String> prefixNamespaceMap = configuration.getPrefixNamespaceMap();
            if (prefixNamespaceMap != null) {
                prefixNamespaceMap.forEach((prefix, namespace) ->
                        xPathCompiler.declareNamespace(prefix, namespace));
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