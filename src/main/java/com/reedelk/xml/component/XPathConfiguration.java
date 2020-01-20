package com.reedelk.xml.component;

import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.annotation.Shared;
import com.reedelk.runtime.api.annotation.TabPlacementTop;
import com.reedelk.runtime.api.component.Implementor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

import java.util.Map;

@Shared
@Component(service = XPathConfiguration.class, scope = ServiceScope.PROTOTYPE)
public class XPathConfiguration implements Implementor {

    @TabPlacementTop
    @Property("Prefix > Namespace mappings")
    private Map<String,String> prefixNamespaceMap;

    public void setPrefixNamespaceMap(Map<String, String> prefixNamespaceMap) {
        this.prefixNamespaceMap = prefixNamespaceMap;
    }

    public Map<String, String> getPrefixNamespaceMap() {
        return prefixNamespaceMap;
    }
}
