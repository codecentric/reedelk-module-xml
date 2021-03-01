package de.codecentric.reedelk.xml.internal.xpath;

import de.codecentric.reedelk.xml.component.XPathConfiguration;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XPathCompiler;

import java.util.Optional;

abstract class XPathAbstractEvaluator implements XPathExpressionEvaluator {

    protected final DocumentBuilder documentBuilder;
    protected final XPathCompiler xPathCompiler;

    public XPathAbstractEvaluator(XPathConfiguration configuration) {
        Processor processor = new Processor(false);
        xPathCompiler = processor.newXPathCompiler();
        configureXPath(configuration, xPathCompiler);
        documentBuilder = processor.newDocumentBuilder();
    }

    private void configureXPath(XPathConfiguration configuration, XPathCompiler xPathCompiler) {
        Optional.ofNullable(configuration)
                .flatMap(config -> Optional.ofNullable(config.getPrefixNamespaceMap()))
                .ifPresent(prefixNamespaceMap -> prefixNamespaceMap.forEach(xPathCompiler::declareNamespace));
    }
}
