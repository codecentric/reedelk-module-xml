package com.reedelk.xml.internal.xpath;

import java.io.Serializable;
import java.util.Map;

public enum XPathAttribute {

    XPATH_EXPRESSION("xPathExpression");

    private final String attributeName;

    XPathAttribute(String attributeName) {
        this.attributeName = attributeName;
    }

    public void set(Map<String, Serializable> attributesMap, Serializable attributeValue) {
        attributesMap.put(attributeName, attributeValue);
    }
}
