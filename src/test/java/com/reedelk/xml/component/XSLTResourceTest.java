package com.reedelk.xml.component;

import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.resource.ResourceText;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

class XSLTResourceTest extends AbstractTest {

    private ResourceText resourceText;

    private XSLTResource component;

    @BeforeEach
    void setUp() {
        resourceText = spy(ResourceText.from("/xslt/sheet_sample.xml"));
        component = new XSLTResource();
        component.setStyleSheetFile(resourceText);
        setUpMockConverterService(component);
    }

    @Test
    void shouldTransformCorrectlyInputDocument() {
        // Given
        String styleSheet = TestUtils.resourceAsString("/fixture/stylesheet_sample.xsl");
        doReturn(Flux.just(styleSheet)).when(resourceText).data();
        component.initialize();

        String xmlDocument = TestUtils.resourceAsString("/fixture/xslt_input_document.xml");
        Message message = MessageBuilder.get().withText(xmlDocument).build();

        // When
        Message result = component.apply(message, context);

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