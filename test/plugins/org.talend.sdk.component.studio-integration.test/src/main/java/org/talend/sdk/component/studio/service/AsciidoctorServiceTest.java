package org.talend.sdk.component.studio.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;

public class AsciidoctorServiceTest {

    @Test 
    public void init() {
        final AsciidoctorService instance = new AsciidoctorService();
        String result = instance.convert("= Header");
        assertEquals("<h1>Header</h1>",result.trim());
    }

}
