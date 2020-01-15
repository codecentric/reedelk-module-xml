package com.reedelk.xml.component;

import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.commons.StreamUtils;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.converter.ConverterService;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.resource.DynamicResource;
import com.reedelk.runtime.api.resource.ResourceFile;
import com.reedelk.runtime.api.resource.ResourceService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.reactivestreams.Publisher;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@ESBComponent("XSLT From Resource Dynamic")
@Component(service = XSLTDynamicResource.class, scope = ServiceScope.PROTOTYPE)
public class XSLTDynamicResource extends XSLTAbstractComponent implements ProcessorSync {

    public static final int DEFAULT_READ_BUFFER_SIZE = 65536;

    @Property("XSL style sheet")
    @PropertyInfo("The local project's XSL style sheet.")
    private DynamicResource resourceFile;

    @Property("Output Mime type")
    @MimeTypeCombo
    @Default("text/xml") // TODO: 0.7 Release: replace with constant and add to Mime Types when added to the API.
    @PropertyInfo("Sets mime type of the transformed payload.")
    private String mimeType;

    @Reference
    private ResourceService resourceService;
    @Reference
    private ConverterService converterService;

    @Override
    public void initialize() {
        initializeDocumentBuilder();
    }

    @Override
    public Message apply(Message message, FlowContext flowContext) {
        Object payload = message.payload();

        byte[] payloadBytes = converterService.convert(payload, byte[].class);

        InputStream document = new ByteArrayInputStream(payloadBytes);

        // TODO: 0.7 Add method where don't have to specify read buffer size
        ResourceFile<byte[]> resourceFile =
                resourceService.find(this.resourceFile, DEFAULT_READ_BUFFER_SIZE, flowContext, message);

        Publisher<byte[]> data = resourceFile.data();

        Publisher<String> xsltStream = StreamUtils.FromByteArray.asStringStream(data);

        String xslt = StreamUtils.FromString.consume(xsltStream);

        return transform(document, xslt, mimeType);
    }

    public void setResourceFile(DynamicResource resourceFile) {
        this.resourceFile = resourceFile;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}