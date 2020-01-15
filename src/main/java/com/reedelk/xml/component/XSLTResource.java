package com.reedelk.xml.component;

import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.commons.StreamUtils;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.resource.ResourceText;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

@ESBComponent("XSLT Resource")
@Component(service = XSLTResource.class, scope = ServiceScope.PROTOTYPE)
public class XSLTResource extends XSLTAbstractComponent implements ProcessorSync {

    @Property("XSL style sheet")
    @PropertyInfo("The local project's XSL style sheet.")
    private ResourceText resourceFile;

    @Property("Mime type")
    @MimeTypeCombo
    @Default("text/xml") // TODO: 0.7 Release: replace with constant and add to Mime Types when added to the API.
    @PropertyInfo("Sets mime type of the transformed payload.")
    private String mimeType;

    @Override
    public void initialize() {
        String xslt = StreamUtils.FromString.consume(resourceFile.data());
        StreamSource style = new StreamSource(new StringReader(xslt));
        initializeWith(style);
    }

    @Override
    public Message apply(Message message, FlowContext flowContext) {

        String payload = message.payload();

        InputStream fileInputStream = new ByteArrayInputStream(payload.getBytes());

        try {
            Document xmlDocument = builder.parse(fileInputStream);

            StringWriter buf = new StringWriter();

            transformer.transform(new DOMSource(xmlDocument), new StreamResult(buf));

            MimeType mimeType = MimeType.parse(this.mimeType);

            return MessageBuilder.get().withText(buf.toString()).mimeType(mimeType).build();

        } catch (SAXException | IOException | TransformerException e) {
            throw new ESBException(e);
        }
    }

    public void setResourceFile(ResourceText resourceFile) {
        this.resourceFile = resourceFile;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}