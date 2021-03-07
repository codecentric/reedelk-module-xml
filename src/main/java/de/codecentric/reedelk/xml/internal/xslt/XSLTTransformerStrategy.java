package de.codecentric.reedelk.xml.internal.xslt;

import de.codecentric.reedelk.runtime.api.flow.FlowContext;
import de.codecentric.reedelk.runtime.api.message.Message;

import java.io.InputStream;

public interface XSLTTransformerStrategy {

    String transform(InputStream inputDocument, Message message, FlowContext context);

    default void dispose() {
        // nothing to do by default.
    }
}
