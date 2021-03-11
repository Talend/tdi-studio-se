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
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.PropertyChangeEvent;
import java.util.HashMap;

import org.junit.Test;
import org.talend.sdk.component.studio.model.parameter.PropertyDefinitionDecorator.Condition;
import org.talend.sdk.component.studio.model.parameter.TaCoKitElementParameter;
import org.talend.sdk.component.studio.model.parameter.TextElementParameter;
import org.talend.sdk.component.studio.model.parameter.condition.ConditionGroup;

// todo: check why these tests are correct, seems it happens on the property change
//       and not the relational conditions changes
public class ActiveIfListenerTest {
    @Test
    public void containsNegative() {
        final TacokitTestParameter param = new TacokitTestParameter("-");
        new ActiveIfListener(singletonList(
                new ConditionGroup(
                    singletonList(new Condition(new String[] { "A" }, "a", false, "contains")), true)),
                param, singletonMap("a", new TacokitTestParameter("foo")))
                .propertyChange(new PropertyChangeEvent(param, "value", "bar", "foo"));
        assertFalse(param.setShowValue);
    }

    @Test
    public void contains() {
        final TacokitTestParameter param = new TacokitTestParameter("-");
        new ActiveIfListener(singletonList(
                new ConditionGroup(
                    singletonList(new Condition(new String[] { "A" }, "a", false, "contains")), true)),
                param, singletonMap("a", new TacokitTestParameter("bAr")))
                .propertyChange(new PropertyChangeEvent(param, "value", "foo", "bAr"));
        assertTrue(param.setShowValue);
    }

    @Test
    public void containsLowercase() {
        final TacokitTestParameter param = new TacokitTestParameter("-");
        new ActiveIfListener(singletonList(
                new ConditionGroup(
                    singletonList(new Condition(
                            new String[] { "a" }, "a", false, "contains(lowercase=true)")), true)),
                param, singletonMap("a", new TacokitTestParameter("bAR")))
                .propertyChange(new PropertyChangeEvent(param, "value", "foo", "bAr"));
        assertTrue(param.setShowValue);
    }

    @Test
    public void notActivatedCondition() {
        final TacokitTestParameter param = new TacokitTestParameter("-");
        new ActiveIfListener(singletonList(
                new ConditionGroup(
                    singletonList(new Condition(new String[] { "A" }, "a", false, "DEFAULT")), true)),
                param, singletonMap("a", new TacokitTestParameter("C")))
                .propertyChange(new PropertyChangeEvent(param, "value", "foo", "bar"));
        assertFalse(param.setShowValue);
    }

    @Test
    public void singleCondition() {
        final TacokitTestParameter param = new TacokitTestParameter("-");
        new ActiveIfListener(singletonList(
                new ConditionGroup(
                    singletonList(new Condition(new String[] { "A" }, "a", false, "DEFAULT")), true)),
                param, singletonMap("a", new TacokitTestParameter("A")))
                .propertyChange(new PropertyChangeEvent(param, "value", "foo", "bar"));
        assertTrue(param.setShowValue);
    }

    @Test
    public void andCondition() {
        final TacokitTestParameter param = new TacokitTestParameter("-");
        new ActiveIfListener(singletonList(
                new ConditionGroup(
                    asList(
                            new Condition(new String[] { "A" }, "a", false, "DEFAULT"),
                            new Condition(new String[] { "B" }, "b", false, "DEFAULT")),
                        true)),
                param, new HashMap<String, TaCoKitElementParameter>() {{
                    put("a", new TacokitTestParameter("A"));
                    put("b", new TacokitTestParameter("B"));
        }}).propertyChange(new PropertyChangeEvent(param, "value", "foo", "bar"));
        assertTrue(param.setShowValue);
    }

    @Test
    public void orCondition() {
        final TacokitTestParameter param = new TacokitTestParameter("-");
        new ActiveIfListener(singletonList(
                new ConditionGroup(
                    asList(
                            new Condition(new String[] { "A" }, "a", false, "DEFAULT"),
                            new Condition(new String[] { "B" }, "b", false, "DEFAULT")),
                        false)),
                param, new HashMap<String, TaCoKitElementParameter>() {{
                    put("a", new TacokitTestParameter("A"));
                    put("b", new TacokitTestParameter("C"));
        }}).propertyChange(new PropertyChangeEvent(param, "value", "foo", "bar"));
        assertTrue(param.setShowValue);
    }

    private static class TacokitTestParameter extends TextElementParameter {
        private final String value;
        private boolean setShowValue;

        private TacokitTestParameter(final String value) {
            super(null);
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public void setShow(boolean show) {
            this.setShowValue = show;
        }

        @Override
        public void redraw() {
            // no-op
        }

        @Override
        public void firePropertyChange(final String name, final Object oldValue, final Object newValue) {
            // no-op
        }
    }
}
