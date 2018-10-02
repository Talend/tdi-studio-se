/**
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.talend.sdk.component.studio.service;

import static java.util.concurrent.TimeUnit.MINUTES;

import java.util.concurrent.CountDownLatch;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;

public class AsciidoctorService {

    private final Creator creator = new Creator();

    private final Options options = OptionsBuilder.options().backend("html").headerFooter(false)
            .attributes(AttributesBuilder.attributes().attribute("showtitle")).get();

    public AsciidoctorService() {
        creator.setContextClassLoader(AsciidoctorService.class.getClassLoader());
        creator.start();
    }

    public String convert(final String adoc) {
        try {
            creator.latch.await(5, MINUTES);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return creator.instance.convert(adoc, options);
    }

    private static class Creator extends Thread {

        private Asciidoctor instance;

        private CountDownLatch latch = new CountDownLatch(1);

        @Override
        public void run() {
            final ClassLoader old = Thread.currentThread().getContextClassLoader();
            final Thread thread = Thread.currentThread();
            thread.setContextClassLoader(AsciidoctorService.class.getClassLoader());
            try {
                instance = Asciidoctor.Factory.create();
            } finally {
                thread.setContextClassLoader(old);
                latch.countDown();
            }
        }
    }
}
