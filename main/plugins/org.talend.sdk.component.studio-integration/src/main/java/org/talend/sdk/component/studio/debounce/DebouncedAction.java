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

import static java.util.Optional.ofNullable;

import java.util.concurrent.ScheduledFuture;

public class DebouncedAction implements Runnable {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DebouncedAction.class);

    private final DebounceManager manager;

    private volatile Runnable delegate;

    private volatile ScheduledFuture<?> scheduled;

    public DebouncedAction(final DebounceManager manager) {
        this.manager = manager;
    }

    public synchronized void debounce(final Runnable task, final int timeoutMillis) {
        if (scheduled != null && !scheduled.isDone()) {
            scheduled.cancel(true);
        }
        delegate = task;
        scheduled = manager.schedule(this, timeoutMillis);
    }

    @Override
    public void run() {
        try {
            ofNullable(delegate).ifPresent(Runnable::run);
        } catch (final RuntimeException re) {
            log.error(re.getMessage(), re);
            throw re;
        }
    }
}
