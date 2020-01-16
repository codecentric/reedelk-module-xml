package com.reedelk.xml.component;

import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class XSLTResourceTest {

    @Mock
    private FlowContext flowContext;

    private XSLTResource component;

    @BeforeEach
    void setUp() {
        component = new XSLTResource();
    }

    @Test
    void shouldDoSomething() {
        // Given
        String xmlDocument = TestUtils.resourceAsString("/fixture/book_store.xml");
        Message message = MessageBuilder.get().withText(xmlDocument).build();

        // When
        Message result = component.apply(message, flowContext);

        // Then
    }
}