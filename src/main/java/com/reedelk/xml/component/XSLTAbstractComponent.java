package com.reedelk.xml.component;

import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.content.MimeType;
import net.sf.saxon.s9api.*;

import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

abstract class XSLTAbstractComponent {

    protected DocumentBuilder documentBuilder;
    protected XsltCompiler xsltCompiler;
    protected Processor saxonProc;

    protected void initializeDocumentBuilder() {
        saxonProc = new Processor(false);
        documentBuilder = saxonProc.newDocumentBuilder();
        xsltCompiler = saxonProc.newXsltCompiler();
    }

    protected XsltTransformer createTransformerWith(StreamSource styleSource) {
        try {
            XdmNode stylesheet = documentBuilder.build(styleSource);
            XsltExecutable foStyle = xsltCompiler.compile(stylesheet.asSource());
            return foStyle.load();
        } catch (SaxonApiException e) {
            throw new ESBException(e);
        }
    }

    protected Message transform(InputStream documentInputStream, String xsltDocument, String outputMimeType) {
        try {
            StreamSource style = new StreamSource(new StringReader(xsltDocument));
            XdmNode stylesheet = documentBuilder.build(style);
            XsltExecutable foStyle = xsltCompiler.compile(stylesheet.asSource());
            XsltTransformer foTransformer = foStyle.load();
            return transform(documentInputStream, foTransformer, outputMimeType);
        } catch (SaxonApiException e) {
            throw new ESBException(e);
        }
    }

    protected Message transform(InputStream documentInputStream, XsltTransformer foTransformer, String outputMimeType) {
        try {
            XdmNode inputDocument = documentBuilder.build(new StreamSource(documentInputStream));

            StringWriter stringWriter = new StringWriter();
            Serializer serializer = saxonProc.newSerializer(stringWriter);
            foTransformer.setDestination(serializer);
            foTransformer.setInitialContextNode(inputDocument);

            foTransformer.transform();

            foTransformer.close();

            MimeType mimeType = MimeType.parse(outputMimeType);

            return MessageBuilder.get().withText(stringWriter.toString()).mimeType(mimeType).build();
        } catch (SaxonApiException e) {
            throw new ESBException(e);
        }
    }
}