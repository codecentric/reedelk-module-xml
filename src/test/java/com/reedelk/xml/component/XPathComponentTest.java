package com.reedelk.xml.component;

import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class XPathComponentTest {

    private XPathComponent component = new XPathComponent();

    @Mock
    private FlowContext context;

    @Test
    void shouldDoSomething() {
        // Given
        String xmlDocument = "<ns2:bookStore xmlns:ns2=\"http://bookstore.com/schemes\">\n" +
                "    <ns2:book id=\"1\">\n" +
                "        <ns2:name>Data Structure</ns2:name>\n" +
                "    </ns2:book>\n" +
                "    <ns2:book id=\"2\">\n" +
                "        <ns2:name>Java Core</ns2:name>\n" +
                "    </ns2:book>\n" +
                "</ns2:bookStore>";

        Map<String,String> prefixNamespaces = new HashMap<>();
        prefixNamespaces.put("ns2", "http://bookstore.com/schemes");
        XPathConfiguration configuration = new XPathConfiguration();
        configuration.setPrefixNamespaceMap(prefixNamespaces);
        DynamicString xPathExpression =
                DynamicString.from("//ns2:bookStore/ns2:book/ns2:name/text()");

        component.setExpression(xPathExpression);
        component.setConfiguration(configuration);
        component.initialize();

        Message message = MessageBuilder.get().withText(xmlDocument).build();

        // When
        Message result = component.apply(message, context);

        // Then
        System.out.println((Object)result.payload());
        assertThat(result).isNotNull();
    }
}
