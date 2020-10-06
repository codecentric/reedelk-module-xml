package com.reedelk.xml.component;

import com.reedelk.runtime.api.annotation.ComponentInput;
import com.reedelk.runtime.api.annotation.ComponentOutput;
import com.reedelk.runtime.api.annotation.Description;
import com.reedelk.runtime.api.annotation.ModuleComponent;
import com.reedelk.runtime.api.commons.FileUtils;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.converter.ConverterService;
import com.reedelk.runtime.api.flow.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageAttributes;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.xml.internal.xslt.XSLTStaticResourceTransformerStrategy;
import com.reedelk.xml.internal.xslt.XSLTTransformerStrategy;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ModuleComponent("XML To JSON")
@ComponentOutput(
        attributes = MessageAttributes.class,
        payload = String.class,
        description = "A JSON document mapped from the given XML document in the message payload.")
@ComponentInput(
        payload = { String.class, byte[].class},
        description = "The XML document as string or byte array to be converted into JSON.")
@Description("Converts an XML input document from the current message payload into a JSON document. XML attributes and nodes are mapped to JSON properties for the object being mapped to.")
@Component(service = XMLToJSON.class, scope = PROTOTYPE)
public class XMLToJSON implements ProcessorSync {

    private final String XML_TO_JSON_XSLT = "/xml_to_json.xslt";

    @Reference
    private ConverterService converterService;

    private XSLTTransformerStrategy strategy;

    @Override
    public void initialize() {
        URL resourceUrl = XMLToJSON.class.getResource(XML_TO_JSON_XSLT);
        String xslt = FileUtils.ReadFromURL.asString(resourceUrl);
        strategy = new XSLTStaticResourceTransformerStrategy(xslt);
    }

    @Override
    public Message apply(FlowContext flowContext, Message message) {
        Object payload = message.payload();

        byte[] payloadBytes = converterService.convert(payload, byte[].class);

        InputStream fileInputStream = new ByteArrayInputStream(payloadBytes);

        String transformResult = strategy.transform(fileInputStream, message, flowContext);

        return MessageBuilder.get(XMLToJSON.class)
                .withString(transformResult, MimeType.APPLICATION_JSON)
                .build();
    }

    @Override
    public void dispose() {
        if (strategy != null) {
            strategy.dispose();
        }
    }
}
