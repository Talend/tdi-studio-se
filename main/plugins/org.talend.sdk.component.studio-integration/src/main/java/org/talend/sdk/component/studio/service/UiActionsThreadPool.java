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
package org.talend.sdk.component.studio.service;

import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

public class UiActionsThreadPool implements AutoCloseable {

    private final Executor executor;

    public UiActionsThreadPool(final Executor executor) {
        this.executor = executor;
    }

    @Override
    public void close() throws Exception {
        if (ExecutorService.class.isInstance(executor)) {
            final ExecutorService es = ExecutorService.class.cast(executor);
            es.shutdown();
            es.awaitTermination(10, SECONDS);
        }
    }

    public Executor getExecutor() {
        return this.executor;
    }
}
