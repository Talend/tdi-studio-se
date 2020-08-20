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
package org.talend.sdk.component.studio.debounce;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

class DebounceManagerTest {

    @Test
    void debounce() throws InterruptedException {
        try (final DebounceManager manager = new DebounceManager()) {
            final DebouncedAction action = manager.createAction();
            final Collection<Long> timestamps = new ArrayList<>();
            action.debounce(() -> timestamps.add(System.nanoTime()), 1000);
            sleep(1500);
            assertEquals(1, timestamps.size());

            // execute only once
            sleep(1500);
            assertEquals(1, timestamps.size());

            // can be reused
            timestamps.clear();
            action.debounce(() -> timestamps.add(System.nanoTime()), 1000);
            sleep(1500);
            assertEquals(1, timestamps.size());

            // can be updated
            timestamps.clear();
            final long start = System.nanoTime();
            action.debounce(() -> timestamps.add(0L), 1000);
            sleep(500);
            action.debounce(() -> timestamps.add(System.nanoTime()), 1000);
            sleep(1300);
            assertEquals(1, timestamps.size());
            final long waitDuration = timestamps.iterator().next() - start;
            // 1s after the last update which happens after 500ms
            assertEquals(TimeUnit.NANOSECONDS.toMillis(waitDuration), 1500, 100);

            // ensure we can start an action and close the manager without errors
            action.debounce(() -> timestamps.add(System.nanoTime()), 10000);
        }
    }
}
