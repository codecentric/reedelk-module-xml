package com.reedelk.xml.xpath;

import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.xml.component.XPathConfiguration;
import net.sf.saxon.s9api.*;

import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class XPathStaticExpressionEvaluator extends XPathAbstractEvaluator {

    private final XPathExecutable xPathExecutable;

    public XPathStaticExpressionEvaluator(XPathConfiguration configuration, String xPathExpression) {
        super(configuration);
        try {
            xPathExecutable = xPathCompiler.compile(xPathExpression);
        } catch (SaxonApiException exception) {
            throw new ESBException(exception);
        }
    }

    @Override
    public Object evaluate(byte[] payload, Message message, FlowContext flowContext) {

        try (InputStream fileInputStream = new ByteArrayInputStream(payload)) {

            StreamSource streamSource = new StreamSource(fileInputStream);

            XdmNode xmlDocumentNode = documentBuilder.build(streamSource);

            XPathSelector load = xPathExecutable.load();

            load.setContextItem(xmlDocumentNode);

            XdmValue result = load.evaluate();

            return XPathResultMapper.map(result);

        } catch (SaxonApiException | IOException exception) {
            throw new ESBException(exception);
        }
    }
}
