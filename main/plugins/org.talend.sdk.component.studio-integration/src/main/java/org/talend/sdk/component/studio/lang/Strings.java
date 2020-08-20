/**
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.talend.sdk.component.studio.lang;

import java.util.regex.Pattern;

/**
 * Utility class which provides operations with String objects
 */
public final class Strings {

    private static final Pattern QUOTES_PATTERN = Pattern.compile("^\"|\"$");

    private static final Pattern QUOTED_STRING_PATTERN = Pattern.compile("^\".*\"$");

    // Suppress default constructor for noninstantiability
    private Strings() {
        throw new AssertionError();
    }

    public static String requireNonEmpty(final String str) {
        if (str.isEmpty()) {
            throw new IllegalArgumentException("String arg should not be empty");
        }
        return str;
    }

    public static String removeQuotes(final String str) {
        if (QUOTED_STRING_PATTERN.matcher(str).matches()) {
            return QUOTES_PATTERN.matcher(str).replaceAll("");
        } else {
            return str;
        }

    }

}
