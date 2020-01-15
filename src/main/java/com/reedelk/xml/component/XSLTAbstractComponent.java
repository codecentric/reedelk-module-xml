package com.reedelk.xml.component;

import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.content.MimeType;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

abstract class XSLTAbstractComponent {

    protected DocumentBuilder builder;

    protected void initializeDocumentBuilder() {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        try {
            builder = builderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new ESBException(e);
        }
    }

    protected Transformer createTransformerWith(StreamSource styleSource) {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            return transformerFactory.newTransformer(styleSource);
        } catch (TransformerConfigurationException e) {
            throw new ESBException(e);
        }
    }

    protected Message transform(InputStream documentInputStream, Transformer transformer, String outputMimeType) {
        try {
            Document xmlDocument = builder.parse(documentInputStream);
            StringWriter output = new StringWriter();
            transformer.transform(new DOMSource(xmlDocument), new StreamResult(output));
            MimeType mimeType = MimeType.parse(outputMimeType);
            return MessageBuilder.get()
                    .withText(output.toString())
                    .mimeType(mimeType)
                    .build();
        } catch (SAXException | IOException | TransformerException e) {
            throw new ESBException(e);
        }
    }
}