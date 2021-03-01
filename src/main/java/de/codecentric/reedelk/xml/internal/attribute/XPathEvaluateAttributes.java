package de.codecentric.reedelk.xml.internal.attribute;

import de.codecentric.reedelk.runtime.api.annotation.Type;
import de.codecentric.reedelk.runtime.api.annotation.TypeProperty;
import de.codecentric.reedelk.runtime.api.message.MessageAttributes;

import static de.codecentric.reedelk.xml.internal.attribute.XPathEvaluateAttributes.XPATH_EXPRESSION;

@Type
@TypeProperty(name = XPATH_EXPRESSION, type = String.class)
public class XPathEvaluateAttributes extends MessageAttributes {

    static final String XPATH_EXPRESSION = "xPathExpression";

    public XPathEvaluateAttributes(String expression) {
        put(XPATH_EXPRESSION, expression);
    }
}
