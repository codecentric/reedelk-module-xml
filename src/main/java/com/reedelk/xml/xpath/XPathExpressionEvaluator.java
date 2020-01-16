package com.reedelk.xml.xpath;

import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;

public interface XPathExpressionEvaluator {

    Object evaluate(byte[] payload, Message message, FlowContext flowContext);

}
