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

    protected Processor saxonProc;
    protected XsltCompiler xsltCompiler;
    protected DocumentBuilder documentBuilder;

    protected void initializeDocumentBuilder() {
        saxonProc = new Processor(false);
        xsltCompiler = saxonProc.newXsltCompiler();
        documentBuilder = saxonProc.newDocumentBuilder();
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

    protected Message transform(InputStream documentInputStream, StreamSource styleSource, String outputMimeType) {
        XsltTransformer xsltTransformer = createTransformerWith(styleSource);
        try {
            return transform(documentInputStream, xsltTransformer, outputMimeType);
        } catch (SaxonApiException exception) {
            throw new ESBException(exception);
        } finally {
            if (xsltTransformer != null) {
                xsltTransformer.close();
            }
        }
    }

    protected Message transform(InputStream documentInputStream, XsltTransformer transformer, String outputMimeType) throws SaxonApiException {

        XdmNode inputDocument = documentBuilder.build(new StreamSource(documentInputStream));

        StringWriter stringWriter = new StringWriter();
        Serializer serializer = saxonProc.newSerializer(stringWriter);

        transformer.setDestination(serializer);
        transformer.setInitialContextNode(inputDocument);
        transformer.transform();
        transformer.close();

        MimeType mimeType = MimeType.parse(outputMimeType);

        return MessageBuilder.get().withText(stringWriter.toString()).mimeType(mimeType).build();
    }
}