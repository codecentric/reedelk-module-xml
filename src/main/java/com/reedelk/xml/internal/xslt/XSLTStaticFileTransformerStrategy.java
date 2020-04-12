package com.reedelk.xml.internal.xslt;

import com.reedelk.runtime.api.exception.PlatformException;
import com.reedelk.runtime.api.flow.FlowContext;
import com.reedelk.runtime.api.message.Message;
import net.sf.saxon.s9api.XsltTransformer;

import javax.xml.transform.stream.StreamSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class XSLTStaticFileTransformerStrategy extends XSLTAbstractTransformer implements XSLTTransformerStrategy {

    private XsltTransformer transformer;

    public XSLTStaticFileTransformerStrategy(String stileSheetFile) {
        super();
        try(FileInputStream styleSheetFileInputStream = new FileInputStream(stileSheetFile)) {
            StreamSource styleSheetSource = new StreamSource(styleSheetFileInputStream);
            transformer = createTransformerWith(styleSheetSource);
        } catch (IOException exception) {
            throw new PlatformException(exception);
        }
    }

    @Override
    public String transform(InputStream inputDocument, Message message, FlowContext context) {
        return transform(inputDocument, transformer);
    }

    @Override
    public void dispose() {
        if (transformer != null)  {
            transformer.close();
        }
    }
}
