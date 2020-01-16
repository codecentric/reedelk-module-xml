package com.reedelk.xml.xpath;

import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmValue;

import java.util.stream.Collectors;

public class XPathResultMapper {

    private XPathResultMapper() {
    }

    public static Object map(XdmValue result) {
        // Integer, Long, BigInteger, Boolean: primitive types are kept raw.
        if (result instanceof XdmAtomicValue) {
            XdmAtomicValue atomicResult = (XdmAtomicValue) result;
            return atomicResult.getValue();
        } else {
            // Everything else is added to a list of strings
            return result.stream().map(XdmValue::toString).collect(Collectors.toList());
        }
    }
}
