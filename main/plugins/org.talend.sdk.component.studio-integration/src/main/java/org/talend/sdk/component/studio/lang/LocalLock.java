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

import static java.lang.Thread.sleep;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class LocalLock implements Lock {

    private final File marker;

    private volatile Runnable releaser;

    public LocalLock(final File marker, final Runnable releaser) {
        this.marker = marker;
        this.releaser = releaser;
    }

    @Override
    public synchronized void lock() {
        if (!marker.exists()) {
            return;
        }
        try {
            final FileChannel channel = new RandomAccessFile(marker, "rw").getChannel();
            final int iterationPause = 250;
            final int iterations = Integer.getInteger("component.lock.timeout", 60000) / iterationPause;
            for (int i = 0; i < iterations; i++) {
                try {
                    final FileLock lock = channel.lock();
                    releaser = () -> {
                        try {
                            lock.release();
                        } catch (final IOException e) {
                            throw new IllegalStateException(e);
                        } finally {
                            try {
                                channel.close();
                            } catch (final IOException e) {
                            }
                        }
                    };
                    // no-op
                    break;
                } catch (final OverlappingFileLockException ifle) {
                    try {
                        sleep(iterationPause);
                    } catch (final InterruptedException e) {
                        Thread.interrupted();
                        throw new IllegalStateException(e);
                    }
                }
            }
        } catch (final IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public synchronized void unlock() {
        if (releaser == null) {
            return;
        }
        releaser.run();
        releaser = null;
    }

    @Override
    public void lockInterruptibly() {
        throw new UnsupportedOperationException("lockInterruptibly()");
    }

    @Override
    public boolean tryLock() {
        throw new UnsupportedOperationException("tryLock()");
    }

    @Override
    public boolean tryLock(final long time, final TimeUnit unit) {
        throw new UnsupportedOperationException("tryLock(time, unit)");
    }

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException("newCondition()");
    }

}
