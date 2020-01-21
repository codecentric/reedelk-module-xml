package com.reedelk.xml.component;

import com.reedelk.runtime.api.commons.ModuleContext;
import com.reedelk.runtime.api.exception.ConfigurationException;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.resource.DynamicResource;
import com.reedelk.runtime.api.resource.ResourceFile;
import com.reedelk.runtime.api.resource.ResourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class XSLTDynamicResourceTest extends AbstractTest {

    private XSLTDynamicResource component;

    @Mock
    private ResourceService resourceService;

    @BeforeEach
    void setUp() {
        component = new XSLTDynamicResource();
        setUpMockConverterService(component);
        setComponentFieldWithObject(component, "resourceService", resourceService);
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
        DynamicResource dynamicStyleSheetResource = spy(DynamicResource.from("/fixture/stylesheet_sample.xsl", new ModuleContext(10L)));
        component.setStyleSheetFile(dynamicStyleSheetResource);

        String xmlDocument = TestUtils.resourceAsString("/fixture/xslt_input_document.xml");
        Message message = MessageBuilder.get().withText(xmlDocument).build();

        String styleSheet = TestUtils.resourceAsString("/fixture/stylesheet_sample.xsl");
        ResourceFile<byte[]> styleSheetResourceFile = resourceFileWithData(styleSheet);
        doReturn(styleSheetResourceFile).when(resourceService)
                .find(eq(dynamicStyleSheetResource), eq(context), eq(message));

        component.initialize();

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

    private ResourceFile<byte[]> resourceFileWithData(String data) {
        return new ResourceFile<byte[]>() {
            @Override
            public String path() {
                return "/sample/path";
            }

            @Override
            public Publisher<byte[]> data() {
                return Flux.just(data.getBytes());
            }
        };
    }
}
