package com.reedelk.xml.component;

import com.reedelk.runtime.api.exception.ConfigurationException;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.resource.ResourceText;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

class XSLTResourceTest extends AbstractTest {

    private XSLTResource component;

    @BeforeEach
    void setUp() {
        component = new XSLTResource();
        setUpMockConverterService(component);
    }

    @Test
    void shouldThrowExceptionWhenInitializedAndStyleSheetFileNotDefined() {
        // When
        ConfigurationException thrown =
                assertThrows(ConfigurationException.class, () -> component.initialize());

        // Expect
        assertThat(thrown).isNotNull();
    }

    @Test
    void shouldTransformCorrectlyInputDocument() {
        // Given
        ResourceText resourceText = spy(ResourceText.from("/xslt/stylesheet_sample.xsl"));
        component.setStyleSheetFile(resourceText);

        String styleSheet = TestUtils.resourceAsString("/fixture/stylesheet_sample.xsl");
        doReturn(Flux.just(styleSheet)).when(resourceText).data();
        component.initialize();

        String xmlDocument = TestUtils.resourceAsString("/fixture/xslt_input_document.xml");
        Message message = MessageBuilder.get().withText(xmlDocument).build();

        // When
        Message result = component.apply(context, message);

        // Then
        String transformedDocument = result.payload();
        assertThat(transformedDocument).isEqualTo("<html>\n" +
                "   <body>\n" +
                "      <h2>XSLT transformation example</h2>\n" +
                "      <table border=\"1\">\n" +
                "         <tr bgcolor=\"grey\">\n" +
                "            <th>First Name</th>\n" +
                "            <th>Surname</th>\n" +
                "            <th>First line of Address</th>\n" +
                "            <th>Second line of Address</th>\n" +
                "            <th>City</th>\n" +
                "            <th>Age</th>\n" +
                "         </tr>\n" +
                "         <tr>\n" +
                "            <td>Steve</td>\n" +
                "            <td>Jones</td>\n" +
                "            <td>33 Churchill Road</td>\n" +
                "            <td>Washington</td>\n" +
                "            <td>Washington DC</td>\n" +
                "            <td>45</td>\n" +
                "         </tr>\n" +
                "      </table>\n" +
                "   </body>\n" +
                "</html>");
    }
}
