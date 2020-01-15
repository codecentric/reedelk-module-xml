package com.reedelk.xml.commons;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class XPathNamespaceContext implements NamespaceContext {

    private Map<String, String> prefixesNamespaceMap;

    public XPathNamespaceContext(Map<String, String> prefixesNamespaceMap) {
        this.prefixesNamespaceMap = prefixesNamespaceMap;
    }

    @Override
    public String getNamespaceURI(String prefix) {
        return prefixesNamespaceMap.get(prefix);
    }

    @Override
    public String getPrefix(String namespaceURI) {
        for (Map.Entry<String,String> item : prefixesNamespaceMap.entrySet()) {
            if (item.getValue().equals(namespaceURI)) {
                return item.getKey();
            }
        }
        return null;
    }

    @Override
    public Iterator<String> getPrefixes(String targetNamespaceURI) {
        List<String> prefixes = new ArrayList<>();
        prefixesNamespaceMap.forEach((prefix, namespaceURI) -> {
            if (targetNamespaceURI.equals(namespaceURI)) {
                prefixes.add(prefix);
            }
        });
        return prefixes.iterator();
    }
}
