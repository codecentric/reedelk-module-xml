package com.reedelk.xml.component;

import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.converter.ConverterService;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.resource.DynamicResource;
import com.reedelk.runtime.api.resource.ResourceService;
import com.reedelk.xml.xslt.XSLTDynamicResourceTransformerStrategy;
import com.reedelk.xml.xslt.XSLTTransformerStrategy;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static com.reedelk.runtime.api.commons.ConfigurationPreconditions.requireNotNull;

@ESBComponent("XSLT From Resource Dynamic")
@Component(service = XSLTDynamicResource.class, scope = ServiceScope.PROTOTYPE)
public class XSLTDynamicResource implements ProcessorSync {

    @Property("XSLT stylesheet")
    @Default("#[]")
    @PropertyInfo("The path starting from the project 'resources' folder of the XSLT stylesheet file. " +
            "The file must be present in the project's resources folder. " +
            "A dynamic value might be used to define the XSLT stylesheet path.")
    private DynamicResource styleSheetFile;

    // TODO [0.7 Release]: replace with constant and add to Mime Types when added to the API.
    @Property("Output Mime type")
    @MimeTypeCombo
    @Default("text/xml")
    @PropertyInfo("Sets mime type of the transformed payload.")
    private String mimeType;

    @Reference
    private ResourceService resourceService;
    @Reference
    private ConverterService converterService;

    private XSLTTransformerStrategy strategy;

    @Override
    public void initialize() {
        requireNotNull(styleSheetFile,
                "Property 'styleSheetFile' must not be empty");
        strategy = new XSLTDynamicResourceTransformerStrategy(resourceService, styleSheetFile);
    }

    @Override
    public Message apply(Message message, FlowContext flowContext) {
        Object payload = message.payload();

        byte[] payloadBytes = converterService.convert(payload, byte[].class);

        InputStream document = new ByteArrayInputStream(payloadBytes);

        String transformResult = strategy.transform(document, message, flowContext);

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

    public void setStyleSheetFile(DynamicResource styleSheetFile) {
        this.styleSheetFile = styleSheetFile;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}