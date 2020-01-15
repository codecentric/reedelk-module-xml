package com.reedelk.xml.component;

import com.reedelk.runtime.api.commons.FileUtils;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class XPathComponentTest {

    private XPathComponent component = new XPathComponent();

    @Mock
    private FlowContext context;

    @Test
    void shouldCorrectlyMatchXPathUsingNamespace() {
        // Given
        String xmlDocument = resourceAsString("/fixture/book_store.xml");

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
        List<String> xPathResult = result.payload();
        assertThat(xPathResult).containsExactlyInAnyOrder("Data Structure", "Java Core");
    }

    @Test
    void shouldGetBookTitles() {
        // Given
        String xml = resourceAsString("/fixture/book_inventory.xml");
        DynamicString xPathExpression = DynamicString.from("//book[@year>2001]/title/text()");

        component.setExpression(xPathExpression);
        component.initialize();

        Message message = MessageBuilder.get().withText(xml).build();

        // When
        Message result = component.apply(message, context);

        // Then
        List<String> xPathResult = result.payload();
        assertThat(xPathResult).containsExactlyInAnyOrder("Burning Tower");
    }

    @Test
    void shouldCountAllBookTitles() {
        // Given
        String xml = resourceAsString("/fixture/book_inventory.xml");
        DynamicString xPathExpression = DynamicString.from("count(//book/title)");

        component.setExpression(xPathExpression);
        component.initialize();

        Message message = MessageBuilder.get().withText(xml).build();

        // When
        Message result = component.apply(message, context);

        // Then
        List<String> xPathResult = result.payload();
        assertThat(xPathResult).containsExactlyInAnyOrder("3");
    }

    private String resourceAsString(String resourceFile) {
        URL url = XPathComponentTest.class.getResource(resourceFile);
        return FileUtils.ReadFromURL.asString(url);
    }
}