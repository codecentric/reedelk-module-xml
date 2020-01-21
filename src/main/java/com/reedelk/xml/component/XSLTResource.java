package com.reedelk.xml.component;

import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.converter.ConverterService;
import com.reedelk.runtime.api.flow.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.resource.ResourceText;
import com.reedelk.xml.xslt.XSLTStaticResourceTransformerStrategy;
import com.reedelk.xml.xslt.XSLTTransformerStrategy;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static com.reedelk.runtime.api.commons.ConfigurationPreconditions.requireNotNull;

@ESBComponent("XSLT From Resource")
@Component(service = XSLTResource.class, scope = ServiceScope.PROTOTYPE)
public class XSLTResource implements ProcessorSync {

    @Property("XSLT stylesheet")
    @PropertyInfo("The path starting from the project 'resources' folder of the XSLT stylesheet file. " +
            "The file must be present in the project's resources folder.")
    private ResourceText styleSheetFile;

    @Property("Output Mime type")
    @MimeTypeCombo
    @Default(MimeType.MIME_TYPE_TEXT_XML)
    @PropertyInfo("Sets mime type of the transformed payload.")
    private String mimeType;

    @Reference
    private ConverterService converterService;

    private XSLTTransformerStrategy strategy;

    @Override
    public void initialize() {
        requireNotNull(XSLTResource.class, styleSheetFile,
                "Property 'styleSheetFile' must not be empty");
        strategy = new XSLTStaticResourceTransformerStrategy(styleSheetFile);
    }

    @Override
    public Message apply(Message message, FlowContext flowContext) {
        Object payload = message.payload();

        byte[] payloadBytes = converterService.convert(payload, byte[].class);

        InputStream fileInputStream = new ByteArrayInputStream(payloadBytes);

        String transformResult = strategy.transform(fileInputStream, message, flowContext);

        MimeType parsedMimeType = MimeType.parse(mimeType);

        return MessageBuilder.get()
                .withText(transformResult)
                .mimeType(parsedMimeType)
                .build();
    }

    @Override
    public void dispose() {
        if (strategy != null) {
            strategy.dispose();
            strategy = null;
        }
    }

    public void setStyleSheetFile(ResourceText styleSheetFile) {
        this.styleSheetFile = styleSheetFile;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}