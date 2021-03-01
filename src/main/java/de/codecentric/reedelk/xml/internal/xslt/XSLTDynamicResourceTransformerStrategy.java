package de.codecentric.reedelk.xml.internal.xslt;

import de.codecentric.reedelk.runtime.api.commons.StreamUtils;
import de.codecentric.reedelk.runtime.api.flow.FlowContext;
import de.codecentric.reedelk.runtime.api.message.Message;
import de.codecentric.reedelk.runtime.api.resource.DynamicResource;
import de.codecentric.reedelk.runtime.api.resource.ResourceFile;
import de.codecentric.reedelk.runtime.api.resource.ResourceService;
import net.sf.saxon.s9api.XsltTransformer;
import org.reactivestreams.Publisher;

import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class XSLTDynamicResourceTransformerStrategy extends XSLTAbstractTransformer implements XSLTTransformerStrategy {

    private final ResourceService resourceService;
    private final DynamicResource styleSheetFile;

    public XSLTDynamicResourceTransformerStrategy(ResourceService resourceService, DynamicResource styleSheetFile) {
        this.resourceService = resourceService;
        this.styleSheetFile = styleSheetFile;
    }

    @Override
    public String transform(InputStream inputDocument, Message message, FlowContext context) {

        ResourceFile<byte[]> styleSheetContent = resourceService.find(styleSheetFile, context, message);

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
