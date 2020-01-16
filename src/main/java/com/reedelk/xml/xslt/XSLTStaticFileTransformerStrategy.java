package com.reedelk.xml.xslt;

import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
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
        } catch (IOException e) {
            throw new ESBException(e);
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
