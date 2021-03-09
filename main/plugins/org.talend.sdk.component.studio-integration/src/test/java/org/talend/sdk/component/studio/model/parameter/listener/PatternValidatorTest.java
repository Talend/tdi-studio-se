/**
 * Copyright (C) 2006-2021 Talend Inc. - www.talend.com
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
package org.talend.sdk.component.studio.model.parameter.listener;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collection;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.talend.sdk.component.studio.model.parameter.ValidationLabel;


class PatternValidatorTest {

    @Test
    void simpleRegex() {
        Stream.of(
                new RegexCases("\\S+@\\S+\\.\\S+", asList("foo@bar.com", "test.thing@dummy.provider.com"),
                        asList("wrong", "notgoodgmail.com")),
                new RegexCases("^[a-zA-Z ]+$", asList("John Doe", "Some other Name"),
                        asList("@bsolutely wrong", "Not a Name v3")))
                .flatMap(base -> Stream.of(base, new RegexCases("/^" + base.regex + "$/", base.validTexts, base.invalidTexts)))
                .forEach(Runnable::run);
    }

    @Test
    void withFlags() {
        new RegexCases("/[a-z]+/i", asList("foo", "FOO", "FoO"), asList("123", "@")).run();
    }

    private static class RegexCases implements Runnable {

        private final PatternValidator regex;

        private final Collection<String> validTexts;

        private final Collection<String> invalidTexts;

        private RegexCases(final String regex, final Collection<String> validTexts, final Collection<String> invalidTexts) {
            this.regex = new PatternValidator(new ValidationLabel(null), regex);
            this.validTexts = validTexts;
            this.invalidTexts = invalidTexts;
        }

        @Override
        public void run() {
            validTexts.forEach(v -> validate(v, true));
            invalidTexts.forEach(v -> validate(v, false));
        }

        private void validate(final String value, final boolean state) {
            assertEquals(state, regex.validate(value), "Error in validating " + regex + " > " + value);
        }
    }
}
