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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.talend.sdk.component.junit.base.junit5.TemporaryFolder;
import org.talend.sdk.component.junit.base.junit5.WithTemporaryFolder;

@WithTemporaryFolder
class LocalLockTest {

    @Test
    void run(final TestInfo info, final TemporaryFolder folder) throws InterruptedException, IOException {
        final File lockFile = folder.newFile(info.getTestMethod().get().getName());
        { // ensure it works "empty"
            final Lock lock1 = new LocalLock(lockFile, null);
            lock1.lock();
            lock1.unlock();
        }
        { // now ensure it works concurrently
            final Lock lock1 = new LocalLock(lockFile, null);
            final Lock lock2 = new LocalLock(lockFile, null);
            lock1.lock();

            final CountDownLatch latch = new CountDownLatch(1);
            new Thread(() -> {
                lock2.lock();
                latch.countDown();
            }).start();
            assertFalse(latch.await(3, SECONDS));
            lock1.unlock();
            assertTrue(latch.await(1, SECONDS));
            lock2.unlock();
            // ensure we can call unlock N times
            IntStream.range(0, 5).forEach(i -> lock2.unlock());
        }
    }
}
