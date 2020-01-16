package com.reedelk.xml.xslt;

import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;

import java.io.InputStream;

public interface XSLTTransformerStrategy {

    String transform(InputStream inputDocument, Message message, FlowContext context);

    default void dispose() {
        // nothing to do by default.
    }
}
