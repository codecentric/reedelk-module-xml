package com.reedelk.xml.internal.xpath;

import com.reedelk.runtime.api.flow.FlowContext;
import com.reedelk.runtime.api.message.Message;

public interface XPathExpressionEvaluator {

    EvaluationResult evaluate(byte[] payload, Message message, FlowContext flowContext);

}
