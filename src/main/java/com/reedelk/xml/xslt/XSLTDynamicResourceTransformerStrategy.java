package com.reedelk.xml.xslt;

import com.reedelk.runtime.api.commons.StreamUtils;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.resource.DynamicResource;
import com.reedelk.runtime.api.resource.ResourceFile;
import com.reedelk.runtime.api.resource.ResourceService;
import net.sf.saxon.s9api.XsltTransformer;
import org.reactivestreams.Publisher;

import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class XSLTDynamicResourceTransformerStrategy extends XSLTAbstractTransformer implements XSLTTransformerStrategy {

    public static final int DEFAULT_READ_BUFFER_SIZE = 65536;

    private final ResourceService resourceService;
    private final DynamicResource styleSheetFile;

    public XSLTDynamicResourceTransformerStrategy(ResourceService resourceService, DynamicResource styleSheetFile) {
        this.resourceService = resourceService;
        this.styleSheetFile = styleSheetFile;
    }

    // TODO [0.7 release]: Add method to resource service
    //  where don't have to specify read buffer size.
    @Override
    public String transform(InputStream inputDocument, Message message, FlowContext context) {

        ResourceFile<byte[]> styleSheetContent =
                resourceService.find(styleSheetFile, DEFAULT_READ_BUFFER_SIZE, context, message);

        Publisher<byte[]> data = styleSheetContent.data();

        byte[] styleSheetData = StreamUtils.FromByteArray.consume(data);

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(styleSheetData);

        StreamSource styleSheetSource = new StreamSource(byteArrayInputStream);

        XsltTransformer transformer = null;
        try {
            transformer = createTransformerWith(styleSheetSource);
            return transform(inputDocument, transformer);
        } finally {
            if (transformer != null) {
                transformer.close();
            }
        }
    }
}
