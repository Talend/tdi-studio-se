// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.sdk.component.studio.lang;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StringsTest {

    @Test
    public void testRequireNonEmpty() {
        final String expected = "some string";
        final String actual = Strings.requireNonEmpty(expected);
        Assertions.assertEquals(expected, actual);
        IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class, () -> Strings.requireNonEmpty(""));
        Assertions.assertEquals("String arg should not be empty", e.getMessage());
    }

    @Test
    public void testRemoveQuotes() {
        Assertions.assertEquals("some string", Strings.removeQuotes("\"some string\""));
        Assertions.assertEquals("some\"inner\" string", Strings.removeQuotes("\"some\"inner\" string\""));
        Assertions.assertEquals("\"some string", Strings.removeQuotes("\"some string"));
        Assertions.assertEquals("some string\"", Strings.removeQuotes("some string\""));
    }

}
