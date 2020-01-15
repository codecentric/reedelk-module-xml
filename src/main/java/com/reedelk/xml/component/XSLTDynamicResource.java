package com.reedelk.xml.component;

import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.resource.DynamicResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

@ESBComponent("XSLT Dynamic Resource")
@Component(service = XSLTDynamicResource.class, scope = ServiceScope.PROTOTYPE)
public class XSLTDynamicResource implements ProcessorSync {

    @Property("XSL style sheet")
    @PropertyInfo("The local project's XSL style sheet.")
    private DynamicResource resourceFile;

    @Property("Mime type")
    @MimeTypeCombo
    @Default("text/xml") // TODO: 0.7 Release: replace with constant and add to Mime Types when added to the API.
    @PropertyInfo("Sets mime type of the transformed payload.")
    private String mimeType;

    @Override
    public Message apply(Message message, FlowContext flowContext) {
        return null;
    }

    public void setResourceFile(DynamicResource resourceFile) {
        this.resourceFile = resourceFile;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
