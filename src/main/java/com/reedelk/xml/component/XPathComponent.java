package com.reedelk.xml.component;

import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.converter.ConverterService;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.script.ScriptEngineService;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import com.reedelk.xml.xpath.XPathDynamicExpressionEvaluator;
import com.reedelk.xml.xpath.XPathExpressionEvaluator;
import com.reedelk.xml.xpath.XPathStaticExpressionEvaluator;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

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

        Object evaluationResult = strategy.evaluate(payloadAsBytes, message, flowContext);

        return MessageBuilder.get().withJavaObject(evaluationResult).build();
    }

    public void setExpression(DynamicString expression) {
        this.expression = expression;
    }

    public void setConfiguration(XPathConfiguration configuration) {
        this.configuration = configuration;
    }
}