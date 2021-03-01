package de.codecentric.reedelk.xml.component;

import de.codecentric.reedelk.runtime.api.commons.FileUtils;

import java.net.URL;

public class TestUtils {

    public static String resourceAsString(String resourceFile) {
        URL url = TestUtils.class.getResource(resourceFile);
        return FileUtils.ReadFromURL.asString(url);
    }
}