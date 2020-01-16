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

@ESBComponent("XPath Evaluate")
@Component(service = XPathEvaluate.class, scope = ServiceScope.PROTOTYPE)
public class XPathEvaluate implements ProcessorSync {

    @Property("XPath Expression")
    @PropertyInfo("Sets the XPath expression to be evaluated. It can be a dynamic expression. <br>" +
            "Examples: " +
            "<ul>" +
            "<li><i>Static</i>: //book[@year>2001]/title/text()</li>" +
            "<li><i>Static</i>: count(//book/title)</li>" +
            "<li><i>Static</i>: boolean(/inventory/book/price[text() > 14])</li>" +
            "<li><i>Static</i>: //ns2:bookStore/ns2:book/ns2:name/text()</li>" +
            "<li><i>Dynamic</i>: 'boolean(/inventory/book/price[text() > ' + message.attributes().queryParams.price + '])'</li>" +
            "</ul>")
    @Default("")
    @Hint("//book[@year>2001]/title/text()")
    private DynamicString expression;

    @Property("XPath Context")
    @PropertyInfo("The context configuration can be used when the XPath expression contains node" +
            " names with namespace prefixes. The configuration allows to define the prefixes > namespaces mapping.")
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
        XPathAttribute.XPATH_EXPRESSION.set(attributes, evaluationResult.getExpression());

        DefaultMessageAttributes responseAttributes
                = new DefaultMessageAttributes(XPathEvaluate.class, attributes);

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