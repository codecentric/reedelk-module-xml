package com.reedelk.xml.component;

import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.annotation.PropertyInfo;
import com.reedelk.runtime.api.commons.StreamUtils;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.resource.ResourceText;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

@ESBComponent("XSLT Transform")
@Component(service = XSLTTransform.class, scope = ServiceScope.PROTOTYPE)
public class XSLTTransform implements ProcessorSync {

    @Property("XSL style sheet")
    @PropertyInfo("The local project's XSL style sheet.")
    private ResourceText resourceFile;

    @Property("Mime Type")

    private DocumentBuilder builder;
    private Transformer transformer;

    @Override
    public void initialize() {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        try {
            builder = builderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new ESBException(e);
        }

        // Use a Transformer for output
        TransformerFactory transformerFactory = TransformerFactory.newInstance();

        String xslt = StreamUtils.FromString.consume(resourceFile.data());

        StreamSource style = new StreamSource(new StringReader(xslt));

        try {
            transformer = transformerFactory.newTransformer(style);
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

            StringWriter buf = new StringWriter();

            transformer.transform(new DOMSource(xmlDocument), new StreamResult(buf));

            return MessageBuilder.get().withText(buf.toString()).build();

        } catch (SAXException | IOException | TransformerException e) {
            throw new ESBException(e);
        }
    }

    public void setResourceFile(ResourceText resourceFile) {
        this.resourceFile = resourceFile;
    }
}
