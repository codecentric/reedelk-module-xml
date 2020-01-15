package com.reedelk.xml.component;

import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.converter.ConverterService;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.script.ScriptEngineService;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

@ESBComponent("XSLT File")
@Component(service = XSLTFile.class, scope = ServiceScope.PROTOTYPE)
public class XSLTFile extends XSLTAbstractComponent implements ProcessorSync {

    @Property("XSLT File")
    @PropertyInfo("The path and name of the file to be read from the file system.")
    private DynamicString fileName;

    @Property("Output Mime type")
    @MimeTypeCombo
    @Default("text/xml") // TODO: 0.7 Release: replace with constant and add to Mime Types when added to the API.
    @PropertyInfo("Sets mime type of the transformed payload.")
    private String mimeType;

    @Reference
    private ScriptEngineService scriptEngine;
    @Reference
    private ConverterService converterService;

    @Override
    public void initialize() {
        initializeDocumentBuilder();
    }

    @Override
    public Message apply(Message message, FlowContext flowContext) {

        return scriptEngine.evaluate(fileName, flowContext, message).map(evaluatedFilePath -> {

            File initialFile = new File(evaluatedFilePath);

            try {
                Object payload = message.payload();

                byte[] payloadBytes = converterService.convert(payload, byte[].class);

                InputStream document = new ByteArrayInputStream(payloadBytes);

                Transformer transformerWith = createTransformerWith(new StreamSource(new FileReader(initialFile)));

                return transform(document, transformerWith, mimeType);

            } catch (FileNotFoundException e) {
                throw new ESBException(e);
            }

        }).orElse(MessageBuilder.get().empty().build());
    }

    public void setFileName(DynamicString fileName) {
        this.fileName = fileName;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}