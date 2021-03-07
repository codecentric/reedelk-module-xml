package de.codecentric.reedelk.xml.internal.xpath;

import de.codecentric.reedelk.runtime.api.flow.FlowContext;
import de.codecentric.reedelk.runtime.api.message.Message;

public interface XPathExpressionEvaluator {

    EvaluationResult evaluate(byte[] payload, Message message, FlowContext flowContext);

}
