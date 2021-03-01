package de.codecentric.reedelk.xml.internal.xpath;

import de.codecentric.reedelk.runtime.api.exception.PlatformException;
import de.codecentric.reedelk.runtime.api.flow.FlowContext;
import de.codecentric.reedelk.runtime.api.message.Message;
import de.codecentric.reedelk.xml.component.XPathConfiguration;
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
            throw new PlatformException(exception);
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
            throw new PlatformException(exception);
        }
    }
}
