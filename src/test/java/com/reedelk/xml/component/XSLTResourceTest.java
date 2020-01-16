package com.reedelk.xml.component;

import com.reedelk.runtime.api.converter.ConverterService;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.resource.ResourceText;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class XSLTResourceTest {

    @Mock
    private FlowContext flowContext;
    @Mock
    private ConverterService converterService;

    private ResourceText resourceText;

    private XSLTResource component;

    @BeforeEach
    void setUp() {
        resourceText = spy(ResourceText.from("/xslt/sheet_sample.xml"));
        component = new XSLTResource();
        component.setStyleSheetFile(resourceText);
        setUpMockConverterService();
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
        Message result = component.apply(message, flowContext);

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

    private void setUpMockConverterService() {
        when(converterService.convert(any(Object.class), eq(byte[].class))).thenAnswer(invocation -> {
            String actualValue = invocation.getArgument(0);
            return actualValue.getBytes();
        });
        setComponentFieldWithObject("converterService", converterService);
    }

    private void setComponentFieldWithObject(String field, Object object) {
        try {
            Field converterServiceField = component.getClass().getDeclaredField(field);
            converterServiceField.setAccessible(true);
            converterServiceField.set(component, object);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            fail(e.getMessage(), e);
        }
    }
}