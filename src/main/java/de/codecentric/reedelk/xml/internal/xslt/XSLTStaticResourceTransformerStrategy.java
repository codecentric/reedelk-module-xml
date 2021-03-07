package de.codecentric.reedelk.xml.internal.xslt;

import de.codecentric.reedelk.runtime.api.commons.StreamUtils;
import de.codecentric.reedelk.runtime.api.flow.FlowContext;
import de.codecentric.reedelk.runtime.api.message.Message;
import de.codecentric.reedelk.runtime.api.resource.ResourceText;
import net.sf.saxon.s9api.XsltTransformer;

import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.StringReader;

public class XSLTStaticResourceTransformerStrategy extends XSLTAbstractTransformer implements XSLTTransformerStrategy {

    private XsltTransformer transformer;

    public XSLTStaticResourceTransformerStrategy(ResourceText styleSheetFile) {
        super();
        String xslt = StreamUtils.FromString.consume(styleSheetFile.data());
        StreamSource style = new StreamSource(new StringReader(xslt));
        transformer = createTransformerWith(style);
    }

    public XSLTStaticResourceTransformerStrategy(String styleSheet) {
        super();
        StreamSource style = new StreamSource(new StringReader(styleSheet));
        transformer = createTransformerWith(style);
    }

    @Override
    public String transform(InputStream inputDocument, Message message, FlowContext context) {
        return transform(inputDocument, transformer);
    }

    @Override
    public void dispose() {
        if (transformer != null) {
            transformer.close();
        }
    }
}
