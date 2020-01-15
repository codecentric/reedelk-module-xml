package com.reedelk.xml.component;

import com.reedelk.runtime.api.exception.ESBException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

abstract class XSLTAbstractComponent {

    protected DocumentBuilder builder;
    protected Transformer transformer;

    protected void initializeWith(StreamSource styleSource) {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        try {
            builder = builderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new ESBException(e);
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            transformer = transformerFactory.newTransformer(styleSource);
        } catch (TransformerConfigurationException e) {
            throw new ESBException(e);
        }
    }
}
