package com.reedelk.xml.xpath;

import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.flow.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.xml.component.XPathConfiguration;
import net.sf.saxon.s9api.*;

import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class XPathStaticExpressionEvaluator extends XPathAbstractEvaluator {

    private final String xPathExpression;
    private final XPathExecutable xPathExecutable;

    public XPathStaticExpressionEvaluator(XPathConfiguration configuration, String xPathExpression) {
        super(configuration);
        this.xPathExpression = xPathExpression;
        try {
            xPathExecutable = xPathCompiler.compile(xPathExpression);
        } catch (SaxonApiException exception) {
            throw new ESBException(exception);
        }
    }

    @Override
    public EvaluationResult evaluate(byte[] payload, Message message, FlowContext flowContext) {

        try (InputStream fileInputStream = new ByteArrayInputStream(payload)) {

            StreamSource streamSource = new StreamSource(fileInputStream);

            XdmNode xmlDocumentNode = documentBuilder.build(streamSource);

            XPathSelector load = xPathExecutable.load();

            load.setContextItem(xmlDocumentNode);

            XdmValue result = load.evaluate();

            Object mappedResult = XPathResultMapper.map(result);

            return new EvaluationResult(xPathExpression, mappedResult);

        } catch (SaxonApiException | IOException exception) {
            throw new ESBException(exception);
        }
    }
}
