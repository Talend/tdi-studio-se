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
package org.talend.sdk.component.studio.mvn;

import static java.util.stream.Collectors.toSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.function.Function;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

import org.talend.core.runtime.maven.MavenConstants;

// todo: move to a service
public final class Mvn {

    private Mvn() {
        throw new AssertionError();
    }

    public static String locationToMvn(final String location) {
        if (location.startsWith("mvn:")) {
            return location;
        }
        String[] segments = location.split(":");
        if (segments.length < 3) {
            throw new IllegalArgumentException("Invalid coordinate: " + location);
        }
        switch (segments.length) {
        // support some optional values 3: g:a:v, 4: g:a:t:v
        case 3:
            segments = new String[] { segments[0], segments[1], "jar", segments[2], "compile" };
            break;

        case 4:
            segments = (location + ":compile").split(":");
            break;

        default:
        }
        // mvn:group/artifact/version/type[/classifier]
        final int classifierOffset = segments.length == 5 ? 0 : 1;
        return "mvn:" + MavenConstants.LOCAL_RESOLUTION_URL + '!' + segments[0] + "/" + segments[1] + "/"
                + segments[3 + classifierOffset] + "/" + segments[2] + ((classifierOffset == 0) ? "" : "/" + segments[3]);
    }

    public static <T> T withDependencies(final File module, final String resource, final boolean acceptProvided,
            final Function<Stream<String>, T> fn) throws IOException {
        return withResource(module, resource, s -> {
            if (s == null) {
                return fn.apply(Stream.empty());
            }
            try {
                return fn.apply(toDependencies(s, acceptProvided).stream());
            } catch (final IOException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    private static Set<String> toDependencies(final InputStream deps, final boolean acceptProvided) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(deps))) {
            // this logic excludes  (optional)  intentionally
            return reader.lines().map(String::trim).filter(s -> !s.isEmpty() || s.split(":").length < 4)
                    .filter(s -> !s.endsWith(":test"))
                    .filter(s -> (acceptProvided && s.endsWith(":provided"))
                            || (!acceptProvided && (s.endsWith("compile") || s.endsWith("runtime"))))
                    .filter(s -> !s.contains("log4j")).map(Mvn::locationToMvn).collect(toSet());
        }
    }

    private static <T> T withResource(final File module, final String resource, final Function<InputStream, T> fn)
            throws IOException {
        if (module.isDirectory()) {
            try (InputStream stream = new FileInputStream(new File(module, resource))) {
                return fn.apply(stream);
            }
        } else {
            try (JarFile jar = new JarFile(module)) {
                final ZipEntry entry = jar.getEntry(resource);
                try (InputStream stream = jar.getInputStream(entry)) {
                    return fn.apply(stream);
                }
            }
        }
    }

}
