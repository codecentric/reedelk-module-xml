package com.reedelk.xml.component;

import com.reedelk.runtime.api.commons.ModuleContext;
import com.reedelk.runtime.api.commons.ScriptUtils;
import com.reedelk.runtime.api.exception.ConfigurationException;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

public class XSLTFileTest extends AbstractTest {

    private XSLTFile component;

    @BeforeEach
    void setUp() {
        component = new XSLTFile();
        setUpMockConverterService(component);
        setUpScriptEngineService(component);
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
    void shouldTransformCorrectlyInputDocumentWhenStaticValue() throws IOException {
        // Given
        // Copy the stylesheet into a tmp directory
        // (so that the component can find it on the filesystem).
        String tmpDirPath = System.getProperty("java.io.tmpdir");
        Path tmpDirectory = Paths.get(tmpDirPath, UUID.randomUUID().toString(), "stylesheet_sample.xml");

        String styleSheet = TestUtils.resourceAsString("/fixture/stylesheet_sample.xsl");
        Files.createDirectories(tmpDirectory.getParent());
        Files.write(tmpDirectory, styleSheet.getBytes());

        DynamicString dynamicStyleSheetFilePath = DynamicString.from(tmpDirectory.toString());
        component.setStyleSheetFile(dynamicStyleSheetFilePath);
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

    @Test
    void shouldTransformCorrectlyInputDocumentWhenDynamicValue() throws IOException {
        // Given
        // Copy the stylesheet into a tmp directory
        // (so that the component can find it on the filesystem).
        String tmpDirPath = System.getProperty("java.io.tmpdir");
        Path tmpDirectory = Paths.get(tmpDirPath, UUID.randomUUID().toString(), "stylesheet_sample.xml");

        String styleSheet = TestUtils.resourceAsString("/fixture/stylesheet_sample.xsl");
        Files.createDirectories(tmpDirectory.getParent());
        Files.write(tmpDirectory, styleSheet.getBytes());

        DynamicString dynamicStyleSheetFilePath =
                DynamicString.from(ScriptUtils.asScript("'" + tmpDirectory.toString() + "'"),
                        new ModuleContext(10L));
        component.setStyleSheetFile(dynamicStyleSheetFilePath);
        component.initialize();

        String xmlDocument = TestUtils.resourceAsString("/fixture/xslt_input_document.xml");
        Message message = MessageBuilder.get().withText(xmlDocument).build();

        doReturn(Optional.of(tmpDirectory.toString()))
                .when(scriptEngineService)
                .evaluate(dynamicStyleSheetFilePath, context, message);

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