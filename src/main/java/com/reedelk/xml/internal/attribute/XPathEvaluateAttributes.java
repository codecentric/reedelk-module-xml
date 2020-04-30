package com.reedelk.xml.internal.attribute;

import com.reedelk.runtime.api.annotation.Type;
import com.reedelk.runtime.api.annotation.TypeProperty;
import com.reedelk.runtime.api.message.MessageAttributes;

import static com.reedelk.xml.internal.attribute.XPathEvaluateAttributes.XPATH_EXPRESSION;

@Type
@TypeProperty(name = XPATH_EXPRESSION, type = String.class)
public class XPathEvaluateAttributes extends MessageAttributes {

    static final String XPATH_EXPRESSION = "xPathExpression";

    public XPathEvaluateAttributes(String expression) {
        put(XPATH_EXPRESSION, expression);
    }
}
