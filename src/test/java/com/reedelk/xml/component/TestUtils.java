package com.reedelk.xml.component;

import com.reedelk.runtime.api.commons.FileUtils;

import java.net.URL;

public class TestUtils {

    public static String resourceAsString(String resourceFile) {
        URL url = TestUtils.class.getResource(resourceFile);
        return FileUtils.ReadFromURL.asString(url);
    }
}