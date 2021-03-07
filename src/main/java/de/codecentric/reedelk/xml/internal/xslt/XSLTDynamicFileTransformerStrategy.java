package de.codecentric.reedelk.xml.internal.xslt;

import de.codecentric.reedelk.runtime.api.exception.PlatformException;
import de.codecentric.reedelk.runtime.api.flow.FlowContext;
import de.codecentric.reedelk.runtime.api.message.Message;
import de.codecentric.reedelk.runtime.api.script.ScriptEngineService;
import de.codecentric.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import net.sf.saxon.s9api.XsltTransformer;

import javax.xml.transform.stream.StreamSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class XSLTDynamicFileTransformerStrategy extends XSLTAbstractTransformer implements XSLTTransformerStrategy {

    private final ScriptEngineService scriptEngine;
    private final DynamicString styleSheetFile;

    public XSLTDynamicFileTransformerStrategy(ScriptEngineService scriptEngine, DynamicString styleSheetFile) {
        super();
        this.scriptEngine = scriptEngine;
        this.styleSheetFile = styleSheetFile;
    }
    
    @Override
    public String transform(InputStream inputDocument, Message message, FlowContext context) {
        return scriptEngine.evaluate(styleSheetFile, context, message).map(evaluatedStyleSheetPath -> {

            XsltTransformer transformer = null;

            try (FileInputStream styleSheetFileInputStream = new FileInputStream(evaluatedStyleSheetPath)) {

                StreamSource styleSheetSource = new StreamSource(styleSheetFileInputStream);

                transformer = createTransformerWith(styleSheetSource);

                return transform(inputDocument, transformer);

            } catch (IOException exception) {
                throw new PlatformException(exception);

            } finally {
                if (transformer != null) {
                    transformer.close();
                }
            }

        }).orElse(null);
    }
}
