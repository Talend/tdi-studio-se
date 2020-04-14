/*
 * Copyright (C) 2006-2020 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 */
package org.talend.sdk.component.studio.provider;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.emf.common.util.EList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.core.runtime.repository.build.BuildExportManager;
import org.talend.core.runtime.repository.build.BuildExportManager.EXPORT_TYPE;
import org.talend.core.runtime.repository.build.IBuildExportDependenciesProvider;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.utils.UnifiedComponentUtil;
import org.talend.repository.documentation.ExportFileResource;
import org.talend.sdk.component.studio.ComponentModel;

//TODO: refactorings and code cleanup... this is WIP! ;-)
public class TacokitExportDependenciesProvider implements IBuildExportDependenciesProvider {

    private final static Logger LOG = LoggerFactory.getLogger(TacokitExportDependenciesProvider.class.getName());

    /**
     * Called when exporting a job, this is up to the implementor to check that the item should export the dependencies.
     *
     * We use this interface as a hook to feed MAVEN-INF repository and do not respect the contract, but this will do the job.
     *
     * Side-note: this method is called every time we have an ESB job export as Microservice export also calls it.
     *
     * @param exportFileResource unused see above for explanations.
     * @param item
     *
     * @see org.talend.core.runtime.repository.build.IBuildExportDependenciesProvider
     */
    @Override
    public void exportDependencies(final ExportFileResource exportFileResource, final Item item) {
        if (!BuildExportManager.getInstance().getCurrentExportType().equals(EXPORT_TYPE.OSGI)) {
            return;
        }
        LOG.debug("[exportDependencies] Searching for TaCoKit components...");
        final Map<String, String> plugins = new HashMap<String, String>();
        final EList<?> nodes = ProcessItem.class.cast(item).getProcess().getNode();
        final String DI = ComponentCategory.CATEGORY_4_DI.getName();
        nodes.stream()
                .map(node -> {
                    final String componentName = ((NodeType) node).getComponentName();
                    IComponent component = ComponentsFactoryProvider.getInstance().get(componentName, DI);
                    if (component == null) {
                        component = UnifiedComponentUtil.getDelegateComponent(componentName, DI);
                    }
                    return component;
                })
                .filter(Objects::nonNull)
                .filter(ComponentModel.class::isInstance)
                .map(ComponentModel.class::cast)
                .map(ComponentModel::getId)
                .forEach(id -> {
                    plugins.put(id.getPlugin(), gavToJar(id.getPluginLocation()));
                });
        LOG.info("[exportDependencies] Found {} TaCoKit components.", plugins.size());
        if (plugins.isEmpty()) {
            return;
        }
        try {
            ITalendProcessJavaProject project = CorePlugin.getDefault().getRunProcessService()
                    .getTalendJobJavaProject(item.getProperty());
            final String output = project.getResourcesFolder().getLocationURI().getPath();
            final Path m2 = findM2Path();
            final Path resM2 = Paths.get(output, "MAVEN-INF", "repository");
            final Path coordinates = Paths.get(output, "TALEND-INF", "plugins.properties");
            exportFileResource.addResource("TALEND-INF/", coordinates.toUri().toURL());
            Files.createDirectories(resM2);
            if (Files.exists(coordinates)) {
                Files.readAllLines(coordinates).stream()
                        .filter(line -> !line.matches("^\\s?#"))
                        .filter(line -> line.matches(".*=.*"))
                        .map(line -> line.split("="))
                        // we assume gav already translated
                        .forEach((line) -> plugins.putIfAbsent(line[0].trim(), line[1].trim()));
            } else {
                Files.createDirectories(coordinates.getParent());
            }
            // feeding MAVEN-INF repository
            plugins.forEach((plugin, location) -> {
                LOG.info("[exportDependencies] Adding {} to MAVEN-INF.", plugin);
                final Path src = m2.resolve(location);
                final Path dst = resM2.resolve(location);
                try {
                    // first cp component jar
                    copyJar(src, dst, exportFileResource);
                    // then find deps for current plugin : This is definitely needed for the microservice case and may
                    // help in future to avoid classes collisions as it may happen with azure-dls-gen2 for instance!
                    // TODO: remove components' only deps from container's classpath and keep 'em in MAVEN-INF only.
                    // This may be not so easy but it would be required really soon.
                    JarFile jar = new JarFile(src.toFile());
                    final JarEntry entry = jar.getJarEntry("TALEND-INF/dependencies.txt");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(jar.getInputStream(entry)));
                    reader.lines()
                            .filter(l -> !l.endsWith(":test"))
                            .map(this::gavToJar)
                            .peek(dep -> LOG.debug("[exportDependencies] Copying dependency {} to MAVEN-INF.", dep))
                            .forEach(dep -> copyJar(m2.resolve(dep), resM2.resolve(dep), exportFileResource));
                    reader.close();
                    jar.close();
                } catch (IOException | IllegalStateException e) {
                    LOG.error("[exportDependencies] Error occurred during artifact copy:", e);
                    ExceptionHandler.process(e);
                }
            });
            // Finalize by writing our plugins.properties
            final StringBuffer coord = new StringBuffer("# component-runtime components coordinates:\n");
            plugins.forEach((k, v) -> coord.append(String.format("%s = %s\n", k, v)));
            Files.copy(new BufferedInputStream(new ByteArrayInputStream(coord.toString()
                    .getBytes())), coordinates, REPLACE_EXISTING);
            // For microservice m2 extraction, not necessary for real OSGi bundle
            System.setProperty("talend.component.manager.components.present", "true");
            LOG.info("[exportDependencies] Finished MAVEN-INF feeding.");
        } catch (Exception e) {
            LOG.error("[exportDependencies] Error occurred:", e);
            ExceptionHandler.process(e);
        }
    }

    /**
     * Copy a jar and update export resources.
     *
     * @param source             m2 jar
     * @param destination        MAVEN-INF destination jar
     * @param exportFileResource file resources for job export build
     */
    private void copyJar(Path source, Path destination, ExportFileResource exportFileResource) {
        try {
            if (!Files.exists(destination.getParent())) {
                Files.createDirectories(destination.getParent());
            }
            if (!Files.exists(destination)) {
                Files.copy(source, destination);
            }
            exportFileResource.addResource(destination.getParent().toString()
                    .substring(destination.getParent().toString().indexOf("MAVEN-INF/")), destination.toUri().toURL());
        } catch (IOException e) {
            LOG.error("[copyJar] Something went wrong during jar copying...", e);
            throw new IllegalStateException(e);
        }
    }

    /**
     * Translates a GAV (ie com.tutorial:tutorial-component:0.0.1) to a maven repository path (ie com/tutorial/tutorial-component/0.0.1/tutorial-component-0.0.1.jar).
     *
     * @param gav GroupId ArtifactId Version. The GAV may have the following forms:
     *            com.tutorial:tutorial-component:0.0.1
     *            or
     *            com.tutorial:tutorial-component:jar:0.0.1:compile
     *
     * @return a translated maven path
     */
    public String gavToJar(String gav) {
        final String jarPathFmt = "%s/%s/%s/%s-%s.jar";
        final String[] segments = gav.split(":");
        if (segments.length < 3) {
            throw new IllegalArgumentException("Bad GAV given!"); // TODO improve message
        }
        String group = segments[0].replaceAll("\\.", "/");
        String artifact = segments[1];
        String version = "";
        if (segments.length == 3) {
            version = segments[2];
        } else {
            version = segments[3];
        }
        return String.format(jarPathFmt, group, artifact, version, artifact, version);
    }

    /**
     * Find the maven repository path.
     *
     * @return the configured m2 repository path
     */
    public static Path findM2Path() {
        return Optional.ofNullable(System.getProperty("talend.component.manager.m2.repository"))
                .map(Paths::get)
                .orElseGet(() -> {
                    // check if we are in the studio process if so just grab the the studio config
                    final String m2Repo = System.getProperty("maven.repository");
                    if (!"global".equals(m2Repo)) {
                        final String m2StudioRepo = System.getProperty("osgi.configuration.area", "")
                                .replaceAll("^file:", "");
                        final Path localM2 = Paths.get(m2StudioRepo, ".m2/repository");
                        if (Files.exists(localM2)) {
                            return localM2;
                        }
                    }
                    // defaults to user m2
                    return Paths.get(System.getProperty("user.home", "")).resolve(".m2/repository");
                });
    }

}
