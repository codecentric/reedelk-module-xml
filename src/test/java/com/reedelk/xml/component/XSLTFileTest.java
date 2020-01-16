package com.reedelk.xml.component;

import com.reedelk.runtime.api.exception.ConfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class XSLTFileTest extends AbstractTest {

    private XSLTFile component;

    @BeforeEach
    void setUp() {
        component = new XSLTFile();
        setUpMockConverterService(component);
    }

    @Test
    void shouldThrowExceptionWhenInitializedAndStyleSheetFileNotDefined() {
        // When
        ConfigurationException thrown = assertThrows(ConfigurationException.class,
                () -> component.initialize());

        // Expect
        assertThat(thrown).isNotNull();
    }


}
