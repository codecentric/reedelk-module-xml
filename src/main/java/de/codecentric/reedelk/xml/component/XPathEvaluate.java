package de.codecentric.reedelk.xml.component;

import de.codecentric.reedelk.runtime.api.annotation.*;
import de.codecentric.reedelk.runtime.api.component.ProcessorSync;
import de.codecentric.reedelk.runtime.api.converter.ConverterService;
import de.codecentric.reedelk.runtime.api.flow.FlowContext;
import de.codecentric.reedelk.runtime.api.message.Message;
import de.codecentric.reedelk.runtime.api.message.MessageAttributes;
import de.codecentric.reedelk.runtime.api.message.MessageBuilder;
import de.codecentric.reedelk.runtime.api.script.ScriptEngineService;
import de.codecentric.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import de.codecentric.reedelk.xml.internal.attribute.XPathEvaluateAttributes;
import de.codecentric.reedelk.xml.internal.xpath.EvaluationResult;
import de.codecentric.reedelk.xml.internal.xpath.XPathDynamicExpressionEvaluator;
import de.codecentric.reedelk.xml.internal.xpath.XPathExpressionEvaluator;
import de.codecentric.reedelk.xml.internal.xpath.XPathStaticExpressionEvaluator;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import static de.codecentric.reedelk.runtime.api.commons.ComponentPrecondition.Configuration.requireNotBlank;

@ModuleComponent("XPath Evaluate")
@ComponentOutput(
        attributes = XPathEvaluateAttributes.class,
        payload = Object.class,
        description = "The result of the xpath evaluate operation.")
@ComponentInput(
        payload = { String.class, byte[].class},
        description = "The XML as string or byte array on which the XPath expression should be evaluated on.")
@Description("The XPath Evaluate component evaluates XPath expressions. " +
                "The output of an XPath expression might be a list of strings " +
                "(since it might match any number of elements in the given XML document) a " +
                "number (e.g when the expression uses count() function) or a boolean " +
                "(e.g when the expression uses not() function).")
@Component(service = XPathEvaluate.class, scope = ServiceScope.PROTOTYPE)
public class XPathEvaluate implements ProcessorSync {

    @Property("XPath expression")
    @InitValue("")
    @Hint("//book[@year>2001]/title/text()")
    @Example("<ul>" +
            "<li><i>Static</i>: //book[@year>2001]/title/text()</li>" +
            "<li><i>Static</i>: count(//book/title)</li>" +
            "<li><i>Static</i>: boolean(/inventory/book/price[text() > 14])</li>" +
            "<li><i>Static</i>: //ns2:bookStore/ns2:book/ns2:name/text()</li>" +
            "<li><i>Dynamic</i>: 'boolean(/inventory/book/price[text() > ' + message.attributes().queryParams.price + '])'</li>" +
            "</ul>")
    @Description("Sets the XPath expression to be evaluated. It can be a dynamic expression.")
    private DynamicString expression;

    @DialogTitle("XPath Configuration")
    @Property("Configuration")
    @Description("The context configuration can be used when the XPath expression uses prefixes in the definition." +
            " The configuration allows to define the prefixes > namespaces mapping.")
    private XPathConfiguration configuration;

    @Reference
    private ScriptEngineService scriptEngine;
    @Reference
    private ConverterService converterService;

    private XPathExpressionEvaluator strategy;

    @Override
    public void initialize() {
        requireNotBlank(XPathEvaluate.class, expression.value(), "XPath expression cannot be null.");
        if (expression.isScript()) {
            strategy = new XPathDynamicExpressionEvaluator(scriptEngine, configuration, expression);
        } else {
            String staticXPathValue = expression.value();
            strategy = new XPathStaticExpressionEvaluator(configuration, staticXPathValue);
        }
    }

    @Override
    public Message apply(FlowContext flowContext, Message message) {
        Object payload = message.payload();

        byte[] payloadAsBytes = converterService.convert(payload, byte[].class);

        EvaluationResult evaluationResult = strategy.evaluate(payloadAsBytes, message, flowContext);

        Object xPathResult = evaluationResult.getResult();

        MessageAttributes attributes = new XPathEvaluateAttributes(evaluationResult.getExpression());

        return MessageBuilder.get(XPathEvaluate.class)
                .withJavaObject(xPathResult)
                .attributes(attributes)
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
