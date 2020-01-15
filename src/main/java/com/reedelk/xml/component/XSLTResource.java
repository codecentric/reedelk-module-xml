package com.reedelk.xml.component;

import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.commons.StreamUtils;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.converter.ConverterService;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.resource.ResourceText;
import net.sf.saxon.s9api.XsltTransformer;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;

@ESBComponent("XSLT From Resource")
@Component(service = XSLTResource.class, scope = ServiceScope.PROTOTYPE)
public class XSLTResource extends XSLTAbstractComponent implements ProcessorSync {

    @Property("XSL style sheet")
    @PropertyInfo("The local project's XSL style sheet.")
    private ResourceText resourceFile;

    @Property("Output Mime type")
    @MimeTypeCombo
    @Default("text/xml") // TODO: 0.7 Release: replace with constant and add to Mime Types when added to the API.
    @PropertyInfo("Sets mime type of the transformed payload.")
    private String mimeType;

    @Reference
    private ConverterService converterService;

    private XsltTransformer transformer;

    @Override
    public void initialize() {
        initializeDocumentBuilder();
        String xslt = StreamUtils.FromString.consume(resourceFile.data());
        StreamSource style = new StreamSource(new StringReader(xslt));
        transformer = createTransformerWith(style);
    }

    @Override
    public Message apply(Message message, FlowContext flowContext) {
        Object payload = message.payload();

        byte[] payloadBytes = converterService.convert(payload, byte[].class);

        InputStream fileInputStream = new ByteArrayInputStream(payloadBytes);

        return transform(fileInputStream, transformer, mimeType);
    }

    public void setResourceFile(ResourceText resourceFile) {
        this.resourceFile = resourceFile;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}