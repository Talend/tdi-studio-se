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
package org.talend.sdk.component.studio;

import static java.util.stream.Collectors.joining;
import static org.apache.xbean.asm9.ClassWriter.COMPUTE_FRAMES;
import static org.apache.xbean.asm9.Opcodes.ACC_PUBLIC;
import static org.apache.xbean.asm9.Opcodes.ACC_SUPER;
import static org.apache.xbean.asm9.Opcodes.ALOAD;
import static org.apache.xbean.asm9.Opcodes.ARETURN;
import static org.apache.xbean.asm9.Opcodes.DUP;
import static org.apache.xbean.asm9.Opcodes.INVOKESPECIAL;
import static org.apache.xbean.asm9.Opcodes.NEW;
import static org.apache.xbean.asm9.Opcodes.RETURN;
import static org.apache.xbean.asm9.Opcodes.V1_8;
import static org.apache.ziplock.JarLocation.jarLocation;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.talend.sdk.component.server.front.model.ErrorDictionary.ICON_MISSING;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.jar.JarOutputStream;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

import org.apache.commons.io.IOUtils;
import org.apache.xbean.asm9.AnnotationVisitor;
import org.apache.xbean.asm9.ClassWriter;
import org.apache.xbean.asm9.MethodVisitor;
import org.apache.xbean.asm9.Type;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.talend.sdk.component.api.processor.ElementListener;
import org.talend.sdk.component.api.processor.Processor;
import org.talend.sdk.component.api.service.Action;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.server.front.model.ComponentDetailList;
import org.talend.sdk.component.server.front.model.ComponentIndices;
import org.talend.sdk.component.server.front.model.ConfigTypeNodes;
import org.talend.sdk.component.studio.mvn.Mvn;
import org.talend.sdk.component.studio.util.TaCoKitConst;
import org.talend.sdk.component.studio.websocket.WebSocketClient;

class ServerManagerTest {

    @BeforeEach
    void before(@TempDir final File folder) {
        createM2(folder);
        System.setProperty("component.java.m2", folder.getAbsolutePath());
        System.setProperty("talend.component.server.component.coordinates", "test:test-component:1.0");
    }

    @AfterEach
    void reset() {
        System.clearProperty("component.java.m2");
        System.clearProperty("talend.component.server.component.coordinates");
    }

    @Disabled
    @Test
    void startServer() throws Exception {
        int port;
        final Thread thread = Thread.currentThread();
        final ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        try (URLClassLoader buildLoader = new URLClassLoader(new URL[0], oldLoader) {

            @Override
            public InputStream getResourceAsStream(final String name) {
                if (("META-INF/maven/" + GAV.INSTANCE.getGroupId() + "/" + GAV.INSTANCE.getArtifactId() + "/pom.properties").equals(name)) {
                    return new ByteArrayInputStream(
                            ("version = " + System.getProperty("test.version")).getBytes(StandardCharsets.UTF_8));
                }
                return super.getResourceAsStream(name);
            }
        }; ProcessManager mgr = new ProcessManager(GAV.INSTANCE.getGroupId(), gav -> {
            final String normalizedGav = Mvn.locationToMvn(gav);
            final String[] segments = normalizedGav.substring(normalizedGav.lastIndexOf('!') + 1).split("/");
            if (segments[1].startsWith("component-")) {
                // try in the project
                final File[] root = jarLocation(ServerManagerTest.class).getParentFile().getParentFile().getParentFile()
                        .listFiles((dir, name) -> name.equals(segments[1]));
                if (root != null && root.length == 1) {
                    final File[] jar = new File(root[0], "target").listFiles(
                            (dir, name) -> name.startsWith(segments[1]) && name.endsWith(".jar") && !name.contains("-source")
                                    && !name.contains("-model") && !name.contains("-fat") && !name.contains("-javadoc"));
                    if (jar != null && jar.length == 1) {
                        return jar[0];
                    }
                }
            }
            final File file = new File(
                    System.getProperty("test.m2.repository", System.getProperty("user.home") + "/.m2/repository"),
                    segments[0].replace('.', '/') + '/' + segments[1] + '/' + segments[2] + '/' + segments[1] + '-' + segments[2]
                            + ".jar");
            assertTrue(file.exists(), file.getAbsolutePath());
            return file;
        })) {
            thread.setContextClassLoader(buildLoader);
            mgr.start();
            mgr.waitForServer(() -> {
            });
            port = mgr.getPort();
            assertTrue(isStarted(port));
            assertClient(port);
        } finally {
            thread.setContextClassLoader(oldLoader);
        }
        assertFalse(isStarted(port));
    }

    private void createM2(final File root) {
        final File jar = new File(root, "test/test-component/1.0/test-component-1.0.jar");
        jar.getParentFile().mkdirs();
        try {
            new PluginGenerator().createPlugin(jar, "my-component");
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void assertClient(final int port) {
        try (WebSocketClient client = new WebSocketClient("ws://", String.valueOf(port), "/websocket/v1", 600000)) {
            // we loop since we reuse the same session so we must ensure this reuse works
            for (int i = 0; i < 2; i++) {
                // simple endpoint
                final ComponentIndices indices = client.v1().component().getIndex("en");
                assertEquals(2, indices.getComponents().size());
            }
            {
                // path params, ensure we can change it through the same session usage
                final ComponentDetailList proc1 = client.v1().component().getDetail("en",
                        new String[] { "dGVzdC1jb21wb25lbnQjY29tcCNwcm9jMQ" });
                assertEquals(1, proc1.getDetails().size());
                assertEquals("proc1", proc1.getDetails().get(0).getDisplayName());
                final ComponentDetailList proc2 = client.v1().component().getDetail("en",
                        new String[] { "dGVzdC1jb21wb25lbnQjY29tcCNwcm9jMg" });
                assertEquals(1, proc2.getDetails().size());
                assertEquals("proc2", proc2.getDetails().get(0).getDisplayName());
            }
            // post endpoint, todo: enrich with real returned/configured data to make the
            // test more relevant
            for (int i = 0; i < 2; i++) {
                final String result = client.v1().action().execute(String.class, "proc", "user", "my-componentAction",
                        new HashMap<>());
                assertEquals("{}", result);
            }
            {
                final ConfigTypeNodes repositoryModel = client.v1().configurationType().getRepositoryModel();
                assertTrue(repositoryModel.getNodes().isEmpty());
            }
            assertIcons(client, "dGVzdC1jb21wb25lbnQjY29tcCNwcm9jMQ", "Y29tcA");
        }
    }

    private void assertIcons(final WebSocketClient client, final String compId, final String familyId) {
        final WebSocketClient.V1Component component = client.v1().component();
        try {
            component.icon(compId);
        } catch (final WebSocketClient.ClientException ce) {
            assertNotNull(ce.getErrorPayload());
            assertEquals(ICON_MISSING, ce.getErrorPayload().getCode());
            assertEquals("No icon for identifier: " + compId, ce.getErrorPayload().getDescription());
        }
        try {
            component.familyIcon(familyId);
        } catch (final WebSocketClient.ClientException ce) {
            assertNotNull(ce.getErrorPayload());
            assertEquals(ICON_MISSING, ce.getErrorPayload().getCode());
            assertEquals("No icon for family identifier: " + familyId, ce.getErrorPayload().getDescription());
        }
    }

    private boolean isStarted(final int port) throws IOException {
        final URL url = new URL("http://" + TaCoKitConst.DEFAULT_LOCALHOST + ":" + port + "/api/v1/component/index");
        InputStream stream = null;
        try {
            stream = url.openStream();
            final String content = IOUtils.toString(stream, StandardCharsets.UTF_8.name());
            return content.contains("{\"components\":[");
        } catch (final ConnectException ioe) {
            return false;
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    private static class PluginGenerator {

        private String toPackage(final String container) {
            return "org.talend.test.generated." + container.replace(".jar", "");
        }

        private void createPlugin(final File jar, final String name, final String... deps) throws IOException {
            try (JarOutputStream outputStream = new JarOutputStream(new FileOutputStream(jar))) {
                addDependencies(outputStream, deps);
                // write the classes
                final String packageName = toPackage(name).replace(".", "/");
                outputStream.write(createProcessor("1", outputStream, packageName));
                outputStream.write(createProcessor("2", outputStream, packageName));
                outputStream.write(createModel(outputStream, packageName));
                outputStream.write(createService(outputStream, packageName, name));
            }
        }

        private byte[] createService(final JarOutputStream outputStream, final String packageName, final String name)
                throws IOException {
            final String className = packageName + "/AService.class";
            outputStream.putNextEntry(new ZipEntry(className));
            final ClassWriter writer = new ClassWriter(COMPUTE_FRAMES);
            writer.visitAnnotation(Type.getDescriptor(Service.class), true).visitEnd();
            writer.visit(V1_8, ACC_PUBLIC + ACC_SUPER, className.substring(0, className.length() - ".class".length()), null,
                    Type.getInternalName(Object.class), null);
            writer.visitSource(className.replace(".class", ".java"), null);
            addConstructor(writer);
            final MethodVisitor action = writer.visitMethod(ACC_PUBLIC, "doAction",
                    "(L" + packageName + "/AModel;)L" + packageName + "/AModel;", null, new String[0]);
            final AnnotationVisitor actionAnnotation = action.visitAnnotation(Type.getDescriptor(Action.class), true);
            actionAnnotation.visit("family", "proc");
            actionAnnotation.visit("value", name + "Action");
            actionAnnotation.visitEnd();
            action.visitCode();
            action.visitTypeInsn(NEW, packageName + "/AModel");
            action.visitInsn(DUP);
            action.visitMethodInsn(INVOKESPECIAL, packageName + "/AModel", "<init>", "()V", false);
            action.visitInsn(ARETURN);
            action.visitInsn(ARETURN);
            action.visitMaxs(1, 1);
            action.visitEnd();
            writer.visitEnd();
            return writer.toByteArray();
        }

        private byte[] createModel(final JarOutputStream outputStream, final String packageName) throws IOException {
            final String className = packageName + "/AModel.class";
            outputStream.putNextEntry(new ZipEntry(className));
            final ClassWriter writer = new ClassWriter(COMPUTE_FRAMES);
            writer.visit(V1_8, ACC_PUBLIC + ACC_SUPER, className.substring(0, className.length() - ".class".length()), null,
                    Type.getInternalName(Object.class), null);
            writer.visitSource(className.replace(".class", ".java"), null);
            addConstructor(writer);
            // no real content (fields/methods) for now
            writer.visitEnd();
            return writer.toByteArray();
        }

        private byte[] createProcessor(final String id, final JarOutputStream outputStream, final String packageName)
                throws IOException {
            final String className = packageName + "/AProcessor" + id + ".class";
            outputStream.putNextEntry(new ZipEntry(className));
            final ClassWriter writer = new ClassWriter(COMPUTE_FRAMES);
            final AnnotationVisitor processorAnnotation = writer.visitAnnotation(Type.getDescriptor(Processor.class), true);
            processorAnnotation.visit("family", "comp");
            processorAnnotation.visit("name", "proc" + id);
            processorAnnotation.visitEnd();
            writer.visit(V1_8, ACC_PUBLIC + ACC_SUPER, className.substring(0, className.length() - ".class".length()), null,
                    Type.getInternalName(Object.class), new String[] { Serializable.class.getName().replace(".", "/") });
            writer.visitSource(className.replace(".class", ".java"), null);
            addConstructor(writer);
            // generate a processor
            final MethodVisitor emitMethod = writer.visitMethod(ACC_PUBLIC, "emit",
                    "(L" + packageName + "/AModel;)L" + packageName + "/AModel;", null, new String[0]);
            emitMethod.visitAnnotation(Type.getDescriptor(ElementListener.class), true).visitEnd();
            emitMethod.visitCode();
            emitMethod.visitTypeInsn(NEW, packageName + "/AModel");
            emitMethod.visitInsn(DUP);
            emitMethod.visitMethodInsn(INVOKESPECIAL, packageName + "/AModel", "<init>", "()V", false);
            emitMethod.visitInsn(ARETURN);
            emitMethod.visitInsn(ARETURN);
            emitMethod.visitMaxs(1, 1);
            emitMethod.visitEnd();
            writer.visitEnd();
            return writer.toByteArray();
        }

        private void addDependencies(final JarOutputStream outputStream, final String[] deps) throws IOException {
            // start by writing the dependencies file
            outputStream.putNextEntry(new ZipEntry("TALEND-INF/dependencies.txt"));
            outputStream.write("The following files have been resolved:\n".getBytes(StandardCharsets.UTF_8));
            outputStream.write(Stream.of(deps).collect(joining("\n")).getBytes(StandardCharsets.UTF_8));
        }

        private void addConstructor(final ClassWriter writer) {
            final MethodVisitor constructor = writer.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            constructor.visitCode();
            constructor.visitVarInsn(ALOAD, 0);
            constructor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            constructor.visitInsn(RETURN);
            constructor.visitMaxs(1, 1);
            constructor.visitEnd();
        }
    }
}
