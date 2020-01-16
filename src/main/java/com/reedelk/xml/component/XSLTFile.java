package com.reedelk.xml.component;

import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.converter.ConverterService;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.script.ScriptEngineService;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import com.reedelk.xml.xslt.XSLTDynamicFileTransformerStrategy;
import com.reedelk.xml.xslt.XSLTStaticFileTransformerStrategy;
import com.reedelk.xml.xslt.XSLTTransformerStrategy;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static com.reedelk.runtime.api.commons.ConfigurationPreconditions.requireNotNull;

@ESBComponent("XSLT From File")
@Component(service = XSLTFile.class, scope = ServiceScope.PROTOTYPE)
public class XSLTFile implements ProcessorSync {

    @Property("XSLT File")
    @PropertyInfo("The path and name of the file to be read from the file system.")
    private DynamicString styleSheetFile;

    // TODO [0.7 Release]: replace with constant and add to Mime Types when added to the API.
    @Property("Output Mime type")
    @MimeTypeCombo
    @Default("text/xml")
    @PropertyInfo("Sets mime type of the transformed payload.")
    private String mimeType;

    @Reference
    private ScriptEngineService scriptEngine;
    @Reference
    private ConverterService converterService;

    private XSLTTransformerStrategy strategy;

    @Override
    public void initialize() {
        requireNotNull(styleSheetFile,
                "Property 'styleSheetFile' must not be empty");
        if (styleSheetFile.isScript()) {
            strategy = new XSLTDynamicFileTransformerStrategy(scriptEngine, styleSheetFile);
        } else {
            String styleSheetFilePath = styleSheetFile.value();
            strategy = new XSLTStaticFileTransformerStrategy(styleSheetFilePath);
        }
    }

    @Override
    public Message apply(Message message, FlowContext flowContext) {

        Object payload = message.payload();

        byte[] payloadAsBytes = converterService.convert(payload, byte[].class);

        InputStream inputDocument = new ByteArrayInputStream(payloadAsBytes);

        String transformResult = strategy.transform(inputDocument, message, flowContext);

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

    public void setStyleSheetFile(DynamicString styleSheetFile) {
        this.styleSheetFile = styleSheetFile;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}