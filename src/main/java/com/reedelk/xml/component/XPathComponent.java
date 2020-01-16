package com.reedelk.xml.component;

import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.converter.ConverterService;
import com.reedelk.runtime.api.message.DefaultMessageAttributes;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.script.ScriptEngineService;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import com.reedelk.xml.xpath.*;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static com.reedelk.runtime.api.commons.ConfigurationPreconditions.requireNotBlank;

@ESBComponent("XPath Extract")
@Component(service = XPathComponent.class, scope = ServiceScope.PROTOTYPE)
public class XPathComponent implements ProcessorSync {

    @Property("XPath Expression")
    @PropertyInfo("The XPath expression to be evaluated e.g //book[@year>2001]/title/text()." +
            " The expression could be a dynamic value.")
    @Default("")
    @Hint("//book[@year>2001]/title/text()")
    private DynamicString expression;

    @Property("XPath Context")
    @PropertyInfo("The context configuration to be used during XPath evaluation. " +
            "The context might specify prefixes > namespaces mappings used within the XPath expression.")
    private XPathConfiguration configuration;

    @Reference
    private ScriptEngineService scriptEngine;
    @Reference
    private ConverterService converterService;

    private XPathExpressionEvaluator strategy;

    @Override
    public void initialize() {
        requireNotBlank(expression.value(), "XPath expression cannot be null.");
        if (expression.isScript()) {
            strategy = new XPathDynamicExpressionEvaluator(scriptEngine, configuration, expression);
        } else {
            String staticXPathValue = expression.value();
            strategy = new XPathStaticExpressionEvaluator(configuration, staticXPathValue);
        }
    }

    @Override
    public Message apply(Message message, FlowContext flowContext) {
        Object payload = message.payload();

        byte[] payloadAsBytes = converterService.convert(payload, byte[].class);

        EvaluationResult evaluationResult = strategy.evaluate(payloadAsBytes, message, flowContext);

        Object xPathResult = evaluationResult.getResult();

        Map<String, Serializable> attributes = new HashMap<>();
        attributes.put(XPathAttribute.XPATH_EXPRESSION, evaluationResult.getExpression());
        DefaultMessageAttributes responseAttributes
                = new DefaultMessageAttributes(XPathComponent.class, attributes);

        return MessageBuilder.get()
                .attributes(responseAttributes)
                .withJavaObject(xPathResult)
                .build();
    }

    @Override
    public void dispose() {
        strategy = null;
    }

    public void setExpression(DynamicString expression) {
        this.expression = expression;
    }

    public void setConfiguration(XPathConfiguration configuration) {
        this.configuration = configuration;
    }
}