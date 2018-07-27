/**
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

import static java.util.Collections.singletonMap;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyChangeEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;
import org.talend.designer.core.model.FakeElement;
import org.talend.sdk.component.studio.model.action.ActionParameter;
import org.talend.sdk.component.studio.model.parameter.TaCoKitElementParameter;
import org.talend.sdk.component.studio.model.parameter.ValidationLabel;

class ValidationListenerTest {

    @Test
    void simple() throws InterruptedException {

        final CountDownLatch latch = new CountDownLatch(1);
        final ActionParameter param = new ActionParameter("test", "the.test.param.url", null);
        final ValidationLabel validationLabel = new ValidationLabel((new FakeElement("validation"))) {

            @Override
            public void showValidation(final String message) {
                super.showValidation(message);
                latch.countDown();
            }

            @Override
            public void hideValidation() {
                super.hideValidation();
                latch.countDown();
            }
        };
        final ValidationListener listener = new ValidationListener(validationLabel, "test", "validation") {

            @Override
            public Map<String, String> callback() {

                final String url = payload().get("the.test.param.url");
                try {
                    new URL(url);
                    return singletonMap("status", "OK");
                } catch (final MalformedURLException e) {
                    return new HashMap<String, String>() {

                        {
                            put("status", "KO");
                            put("comment", "invalid url");
                        }
                    };
                }
            }
        };
        listener.addParameter(param);

        final TaCoKitElementParameter testParam = new TaCoKitElementParameter(new FakeElement("test"));
        testParam.setName("test");
        listener.propertyChange(new PropertyChangeEvent(testParam, "value", null, "htt://gateway/api"));
        latch.await(1, MINUTES);
        assertEquals("invalid url", String.valueOf(validationLabel.getValue()));
    }
}
