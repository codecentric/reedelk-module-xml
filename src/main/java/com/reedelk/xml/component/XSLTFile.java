package com.reedelk.xml.component;

import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.converter.ConverterService;
import com.reedelk.runtime.api.flow.FlowContext;
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

@ModuleComponent("XSLT From File")
@Description("The XSLT component transforms XML documents into other XML documents, or other formats " +
                "such as HTML for web pages, plain text or XSL Formatting Objects. " +
                "The XSLT expects as input a stylesheet defining the transformation to be performed on " +
                "the XML given in input. This component can be used when the stylesheet to be used is on the filesystem.")
@Component(service = XSLTFile.class, scope = ServiceScope.PROTOTYPE)
public class XSLTFile implements ProcessorSync {

    @Property("XSLT stylesheet")
    @Example("/var/xml/my-stylesheet.xsl")
    @Description("The path on the file system of the XSLT stylesheet file. " +
            "The file must be present on the file system otherwise an error will be thrown. " +
            "A dynamic value might be used to define the XSLT stylesheet path.")
    private DynamicString styleSheetFile;

    @Property("Output mime type")
    @MimeTypeCombo
    @Example(MimeType.MIME_TYPE_TEXT_XML)
    @InitValue(MimeType.MIME_TYPE_TEXT_XML)
    @DefaultValue(MimeType.MIME_TYPE_TEXT_XML)
    @Description("Sets mime type of the transformed payload.")
    private String mimeType;

    @Reference
    private ScriptEngineService scriptEngine;
    @Reference
    private ConverterService converterService;

    private XSLTTransformerStrategy strategy;

    @Override
    public void initialize() {
        requireNotNull(XSLTFile.class, styleSheetFile,
                "Property 'styleSheetFile' must not be empty");
        if (styleSheetFile.isScript()) {
            strategy = new XSLTDynamicFileTransformerStrategy(scriptEngine, styleSheetFile);
        } else {
            String styleSheetFilePath = styleSheetFile.value();
            strategy = new XSLTStaticFileTransformerStrategy(styleSheetFilePath);
        }
    }

    @Override
    public Message apply(FlowContext flowContext, Message message) {

        Object payload = message.payload();

        byte[] payloadAsBytes = converterService.convert(payload, byte[].class);

        InputStream inputDocument = new ByteArrayInputStream(payloadAsBytes);

        String transformResult = strategy.transform(inputDocument, message, flowContext);

        MimeType parsedMimeType = MimeType.parse(mimeType, MimeType.TEXT_XML);

        return MessageBuilder.get()
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

    public void setStyleSheetFile(DynamicString styleSheetFile) {
        this.styleSheetFile = styleSheetFile;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
