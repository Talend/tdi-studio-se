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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class StringPropertiesTokenizer {

    private String input;

    private char[] delim;

    private int pos = 0;

    private char lastDelimiter = 0;

    public StringPropertiesTokenizer(final String str) {
        input = str;
        delim = ", \t\r\n\f".toCharArray();
    }

    public List<String> tokens() {
        final List<String> out = new ArrayList<>();
        while (hasMoreTokens()) {
            out.add(nextToken());
        }
        return out;
    }

    private boolean hasMoreTokens() {
        final int oldpos = pos;
        final char olddelim = lastDelimiter;
        try {
            nextToken();
            return true;
        } catch (final NoSuchElementException nsee) {
            return false;
        } finally {
            pos = oldpos;
            lastDelimiter = olddelim;
        }
    }

    private String nextToken() {
        if (pos >= input.length()) {
            throw new NoSuchElementException();
        }

        final StringBuilder sb = new StringBuilder();
        char prevch = 0;
        char ch;
        while (pos < input.length()) {
            lastDelimiter = ch = input.charAt(pos);
            if (isDelimiter(ch, prevch)) {
                break;
            }
            if (isDelimiter(ch, (char) 0) && prevch == '\\') {
                sb.setLength(sb.length() - 1);
            }

            sb.append(ch);
            prevch = ch;
            pos++;
        }
        if (sb.length() == 0) {
            pos++;
            return nextToken();
        }

        return sb.toString();
    }

    private boolean isDelimiter(final char ch, final char prevch) {
        for (char currentChar : delim) {
            if (ch == currentChar && prevch != '\\') {
                return true;
            }
        }
        return false;
    }
}
