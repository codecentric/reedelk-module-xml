package de.codecentric.reedelk.xml.component;

import de.codecentric.reedelk.runtime.api.commons.ModuleContext;
import de.codecentric.reedelk.runtime.api.message.Message;
import de.codecentric.reedelk.runtime.api.message.MessageAttributes;
import de.codecentric.reedelk.runtime.api.message.MessageBuilder;
import de.codecentric.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.codecentric.reedelk.xml.component.TestUtils.resourceAsString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;


public class XPathEvaluateTest extends AbstractTest {

    private XPathEvaluate component;

    @BeforeEach
    void setUp() {
        component = new XPathEvaluate();
        setUpMockConverterService(component);
        setUpScriptEngineService(component);
    }

    @Nested
    @DisplayName("Static XQuery Expression")
    class StaticXQueryExpression {

        @Test
        void shouldCorrectlyMatchXPathUsingNamespace() {
            // Given
            String xmlDocument = resourceAsString("/fixture/book_store.xml");

            Map<String, String> prefixNamespaces = new HashMap<>();
            prefixNamespaces.put("ns2", "http://bookstore.com/schemes");
            XPathConfiguration configuration = new XPathConfiguration();
            configuration.setPrefixNamespaceMap(prefixNamespaces);
            DynamicString xPathExpression =
                    DynamicString.from("//ns2:bookStore/ns2:book/ns2:name/text()");

            component.setExpression(xPathExpression);
            component.setConfiguration(configuration);
            component.initialize();

            Message message = MessageBuilder.get(TestComponent.class).withText(xmlDocument).build();

            // When
            Message result = component.apply(context, message);

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

            Message message = MessageBuilder.get(TestComponent.class).withText(xml).build();

            // When
            Message result = component.apply(context, message);

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

            Message message = MessageBuilder.get(TestComponent.class).withText(xml).build();

            // When
            Message result = component.apply(context, message);

            // Then
            BigInteger count = result.payload();
            assertThat(count).isEqualTo(3);
        }

        @Test
        void shouldReturnCorrectBooleanResult() {
            // Given
            String xml = resourceAsString("/fixture/book_inventory.xml");
            // Exists book with price greater than 14.
            DynamicString xPathExpression = DynamicString.from("boolean(/inventory/book/price[text() > 14])");

            component.setExpression(xPathExpression);
            component.initialize();

            Message message = MessageBuilder.get(TestComponent.class).withText(xml).build();

            // When
            Message result = component.apply(context, message);

            // Then
            boolean existsBookWithPriceGreaterThan14 = result.payload();
            assertThat(existsBookWithPriceGreaterThan14).isTrue();
        }

        @Test
        void shouldAddCorrectAttributesInOutputMessage() {
            // Given
            String xml = resourceAsString("/fixture/book_inventory.xml");
            DynamicString xPathExpression = DynamicString.from("//book[@year>2001]/title/text()");

            component.setExpression(xPathExpression);
            component.initialize();

            Message message = MessageBuilder.get(TestComponent.class).withText(xml).build();

            // When
            Message result = component.apply(context, message);

            // Then
            MessageAttributes attributes = result.attributes();
            assertThat(attributes).containsEntry("xPathExpression", "//book[@year>2001]/title/text()");
        }
    }

    @Nested
    @DisplayName("Dynamic XQuery Expression")
    class DynamicXQueryExpression {

        private ModuleContext moduleContext = new ModuleContext(10L);

        @Test
        void shouldCorrectlyEvaluateDynamicExpression() {
            // Given
            String xml = resourceAsString("/fixture/book_inventory.xml");

            DynamicString xPathExpression =
                    DynamicString.from("#['//book[@year>2001]/title/text()']", moduleContext);

            component.setExpression(xPathExpression);
            component.initialize();

            Message message = MessageBuilder.get(TestComponent.class).withText(xml).build();

            doReturn(Optional.of("//book[@year>2001]/title/text()"))
                    .when(scriptEngineService)
                    .evaluate(xPathExpression, context, message);

            // When
            Message result = component.apply(context, message);

            // Then
            List<String> xPathResult = result.payload();
            assertThat(xPathResult).containsExactly("Burning Tower");
        }

        @Test
        void shouldReturnEmptyMessageWhenEvaluatedExpressionIsEmpty() {
            // Given
            String xml = resourceAsString("/fixture/book_inventory.xml");
            DynamicString xPathExpression =
                    DynamicString.from("#['//book[@year>2001]/title/text()']", moduleContext);

            component.setExpression(xPathExpression);
            component.initialize();

            Message message = MessageBuilder.get(TestComponent.class).withText(xml).build();

            doReturn(Optional.empty())
                    .when(scriptEngineService)
                    .evaluate(xPathExpression, context, message);

            // When
            Message result = component.apply(context, message);

            // Then
            Object payload = result.payload();
            assertThat(payload).isNull();
        }

        @Test
        void shouldAddCorrectAttributesInOutputMessage() {
            // Given
            String xml = resourceAsString("/fixture/book_inventory.xml");
            DynamicString xPathExpression =
                    DynamicString.from("#['//book[@year>2001]/title/text()']", moduleContext);

            component.setExpression(xPathExpression);
            component.initialize();

            Message message = MessageBuilder.get(TestComponent.class).withText(xml).build();

            doReturn(Optional.of("//book[@year>2001]/title/text()"))
                    .when(scriptEngineService)
                    .evaluate(xPathExpression, context, message);

            // When
            Message result = component.apply(context, message);

            // Then
            MessageAttributes attributes = result.attributes();
            assertThat(attributes).containsEntry("xPathExpression", "//book[@year>2001]/title/text()");
        }
    }
}
