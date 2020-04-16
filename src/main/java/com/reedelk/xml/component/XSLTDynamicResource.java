package com.reedelk.xml.component;

import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.converter.ConverterService;
import com.reedelk.runtime.api.flow.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.resource.DynamicResource;
import com.reedelk.runtime.api.resource.ResourceService;
import com.reedelk.xml.internal.xslt.XSLTDynamicResourceTransformerStrategy;
import com.reedelk.xml.internal.xslt.XSLTTransformerStrategy;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static com.reedelk.runtime.api.commons.ConfigurationPreconditions.requireNotNull;

@ModuleComponent("XSLT From Resource Dynamic")
@Description("The XSLT component transforms XML documents into other XML documents, " +
                "or other formats such as HTML for web pages, plain text or XSL Formatting Objects. " +
                "The XSLT expects as input a stylesheet defining the transformation to be performed on " +
                "the XML given in input. This component can be used when the stylesheet to be used is a " +
                "file embedded in the project's resources directory and the exact name of the file depends " +
                "on a context variable or a message property.")
@Component(service = XSLTDynamicResource.class, scope = ServiceScope.PROTOTYPE)
public class XSLTDynamicResource implements ProcessorSync {

    @Property("XSLT stylesheet")
    @Hint("/assets/my-stylesheet.xsl")
    @InitValue("#[]")
    @Example("<code>'/assets/' + message.attributes().queryParams.file[0]</code>")
    @Description("The path starting from the project 'resources' folder of the XSLT stylesheet file. " +
            "The file must be present in the project's resources folder. " +
            "A dynamic value might be used to define the XSLT stylesheet path.")
    private DynamicResource styleSheetFile;

    @Property("Output Mime type")
    @MimeTypeCombo
    @Example(MimeType.AsString.TEXT_XML)
    @DefaultValue(MimeType.AsString.TEXT_XML)
    @Description("Sets mime type of the transformed payload.")
    private String mimeType;

    @Reference
    private ResourceService resourceService;
    @Reference
    private ConverterService converterService;

    private XSLTTransformerStrategy strategy;

    @Override
    public void initialize() {
        requireNotNull(XSLTDynamicResource.class, styleSheetFile,
                "Property 'styleSheetFile' must not be empty");
        strategy = new XSLTDynamicResourceTransformerStrategy(resourceService, styleSheetFile);
    }

    @Override
    public Message apply(FlowContext flowContext, Message message) {
        Object payload = message.payload();

        byte[] payloadBytes = converterService.convert(payload, byte[].class);

        InputStream document = new ByteArrayInputStream(payloadBytes);

        String transformResult = strategy.transform(document, message, flowContext);

        MimeType parsedMimeType = MimeType.parse(mimeType, MimeType.TEXT_XML);

        return MessageBuilder.get(XSLTDynamicResource.class)
                .withString(transformResult, parsedMimeType)
                .build();
    }

    @Override
    public void dispose() {
        if (strategy != null) {
            strategy.dispose();
            strategy = null;
        }
    }

    public void setStyleSheetFile(DynamicResource styleSheetFile) {
        this.styleSheetFile = styleSheetFile;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
