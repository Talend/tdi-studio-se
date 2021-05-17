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
package org.talend.sdk.component.studio.debounce;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

public class DebounceManager implements AutoCloseable {
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(
            new BasicThreadFactory.Builder().namingPattern(getClass().getName() + "-%d").build());

    private final Collection<ScheduledFuture<?>> futures = new ArrayList<>();

    public DebouncedAction createAction() {
        return new DebouncedAction(this);
    }

    @Override
    public void close() {
        scheduledExecutorService.shutdownNow(); // ok to skip last tasks
    }

    public ScheduledFuture<?> schedule(final DebouncedAction debouncedAction, final int timeoutMillis) {
        final ScheduledFuture<?> schedule = scheduledExecutorService.schedule(() -> {
            try {
                debouncedAction.run();
            } finally {
                synchronized (futures) {
                    futures.removeIf(ScheduledFuture::isDone);
                }
            }
        }, timeoutMillis, MILLISECONDS);
        synchronized (futures) {
            futures.add(schedule);
        }
        return schedule;
    }
}
