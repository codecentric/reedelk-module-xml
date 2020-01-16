package com.reedelk.xml.xpath;

import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.ScriptEngineService;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import com.reedelk.xml.component.XPathConfiguration;
import net.sf.saxon.s9api.*;

import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class XPathDynamicExpressionEvaluator extends XPathAbstractEvaluator {

    private final DynamicString dynamicExpression;
    private final ScriptEngineService engineService;

    public XPathDynamicExpressionEvaluator(ScriptEngineService engineService,
                                           XPathConfiguration configuration,
                                           DynamicString dynamicExpression) {
        super(configuration);
        this.engineService = engineService;
        this.dynamicExpression = dynamicExpression;
    }

    @Override
    public EvaluationResult evaluate(byte[] payload, Message message, FlowContext flowContext) {
        return engineService.evaluate(dynamicExpression, flowContext, message).map(evaluatedXPathExpression -> {

            try (InputStream fileInputStream = new ByteArrayInputStream(payload)) {

                XPathExecutable xPathExecutable = xPathCompiler.compile(evaluatedXPathExpression);

                StreamSource streamSource = new StreamSource(fileInputStream);

                XdmNode xmlDocumentNode = documentBuilder.build(streamSource);

                XPathSelector load = xPathExecutable.load();

                load.setContextItem(xmlDocumentNode);

                XdmValue result = load.evaluate();

                Object mappedResult = XPathResultMapper.map(result);

                return new EvaluationResult(evaluatedXPathExpression, mappedResult);

            } catch (SaxonApiException | IOException exception) {
                throw new ESBException(exception);
            }

        }).orElse(new EvaluationResult(null, null));
    }
}