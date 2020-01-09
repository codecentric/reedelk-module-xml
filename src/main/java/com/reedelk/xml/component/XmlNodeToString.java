package com.reedelk.xml.component;

import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

@ESBComponent("XML Node To String")
@Component(service = XmlNodeToString.class, scope = ServiceScope.PROTOTYPE)
public class XmlNodeToString implements ProcessorSync {

    @Override
    public Message apply(Message message, FlowContext flowContext) {
        NodeList nodeList = message.payload();
        Node elem = nodeList.item(0);//Your Node
        StringWriter buf = new StringWriter();
        try {
            Transformer xform = TransformerFactory.newInstance().newTransformer();
            xform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes"); // optional
            xform.setOutputProperty(OutputKeys.INDENT, "yes"); // optional
            xform.transform(new DOMSource(elem), new StreamResult(buf));
            String result = buf.toString();
            return MessageBuilder.get().withText(result).build();
        } catch (TransformerException e) {
            throw new ESBException(e);
        }
    }
}