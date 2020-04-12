package com.reedelk.xml.internal.xslt;

import com.reedelk.runtime.api.exception.PlatformException;
import net.sf.saxon.s9api.*;

import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.StringWriter;

abstract class XSLTAbstractTransformer {

    private final Processor processor;
    private final XsltCompiler compiler;
    private final DocumentBuilder documentBuilder;

    protected XSLTAbstractTransformer() {
        processor = new Processor(false);
        compiler = processor.newXsltCompiler();
        documentBuilder = processor.newDocumentBuilder();
    }

    protected XsltTransformer createTransformerWith(StreamSource styleSource) {
        try {
            XdmNode stylesheet = documentBuilder.build(styleSource);
            XsltExecutable foStyle = compiler.compile(stylesheet.asSource());
            return foStyle.load();
        } catch (SaxonApiException exception) {
            throw new PlatformException(exception);
        }
    }

    protected String transform(InputStream documentInputStream, XsltTransformer transformer) {
        try {
            XdmNode inputDocument = documentBuilder.build(new StreamSource(documentInputStream));

            StringWriter writer = new StringWriter();
            Serializer serializer = processor.newSerializer(writer);
            transformer.setDestination(serializer);
            transformer.setInitialContextNode(inputDocument);
            transformer.transform();
            transformer.close();

            return writer.toString();
        } catch (SaxonApiException e) {
            throw new PlatformException(e);
        }
    }
}
