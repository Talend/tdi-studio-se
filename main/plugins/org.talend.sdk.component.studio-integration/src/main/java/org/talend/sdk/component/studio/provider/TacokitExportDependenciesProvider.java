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
import static org.talend.sdk.component.studio.util.TaCoKitUtil.findM2Path;
import static org.talend.sdk.component.studio.util.TaCoKitUtil.gavToMvnPath;
import static org.talend.sdk.component.studio.util.TaCoKitUtil.getJobComponents;
import static org.talend.sdk.component.studio.util.TaCoKitUtil.getTaCoKitComponents;
import static org.talend.sdk.component.studio.util.TaCoKitUtil.hasTaCoKitComponents;

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
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.system.EnvironmentUtils;
import org.talend.core.CorePlugin;
import org.talend.core.model.properties.Item;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.core.runtime.repository.build.BuildExportManager;
import org.talend.core.runtime.repository.build.BuildExportManager.EXPORT_TYPE;
import org.talend.core.runtime.repository.build.IBuildExportDependenciesProvider;
import org.talend.repository.documentation.ExportFileResource;
import org.talend.sdk.component.studio.ComponentModel;
import org.talend.sdk.component.studio.util.TaCoKitConst;

public class TacokitExportDependenciesProvider implements IBuildExportDependenciesProvider {

    private final static Logger LOG = LoggerFactory.getLogger(TacokitExportDependenciesProvider.class.getName());

    /**
     * Called when exporting a job, this is up to the implementor to check that the item should export the dependencies.
     *
     * We use this interface as a hook to feed MAVEN-INF repository.
     *
     * Side-note: this method is called every time we have an ESB job export as Microservice export also calls it.
     *
     * @param exportFileResource resources to export, mainly needed in OSGi.
     * @param item               <code>ProcessItem</code>
     *
     * @see org.talend.core.runtime.repository.build.IBuildExportDependenciesProvider
     */
    @Override
    public void exportDependencies(final ExportFileResource exportFileResource, final Item item) {
        if (!BuildExportManager.getInstance().getCurrentExportType()
                .equals(EXPORT_TYPE.OSGI) || !hasTaCoKitComponents(getJobComponents(item))) {
            return;
        }
        LOG.debug("[exportDependencies] Searching for TaCoKit components...");
        final Map<String, String> plugins = new HashMap<String, String>();
        getTaCoKitComponents(getJobComponents(item))
                .map(ComponentModel::getId)
                .forEach(id -> {
                    plugins.put(id.getPlugin(), gavToMvnPath(id.getPluginLocation()));
                });
        LOG.info("[exportDependencies] Found {} TaCoKit components.", plugins.size());
        try {
            ITalendProcessJavaProject project = CorePlugin.getDefault().getRunProcessService()
                    .getTalendJobJavaProject(item.getProperty());
            final String output = EnvironmentUtils.isWindowsSystem() ?
                    project.getResourcesFolder().getLocationURI().getPath().substring(1) :
                    project.getResourcesFolder().getLocationURI().getPath();
            final Path m2 = findM2Path();
            final Path resM2 = Paths.get(output, TaCoKitConst.MAVEN_INF, "repository");
            final Path coordinates = Paths.get(output, TaCoKitConst.TALEND_INF, "plugins.properties");
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
            // Feed MAVEN-INF repository
            plugins.forEach((plugin, location) -> {
                LOG.info("[exportDependencies] Adding {} to MAVEN-INF.", plugin);
                final Path src = m2.resolve(location);
                final Path dst = resM2.resolve(location);
                try {
                    // First, cp component jar
                    copyJar(src, dst, exportFileResource);
                    // then, find deps for current plugin : This is definitely needed for the microservice case and may
                    // help to avoid classes collisions as it may happen with azure-dls-gen2 for instance!
                    JarFile jar = new JarFile(src.toFile());
                    final JarEntry entry = jar.getJarEntry("TALEND-INF/dependencies.txt");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(jar.getInputStream(entry)));
                    reader.lines()
                            .filter(l -> !l.endsWith(":test"))
                            .map(this::translateGavToJar)
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
     * Class wrapper for {@link @TaCoKitDependencyUtil#gavToJar(String)}
     *
     * @param gav see {@link @TaCoKitDependencyUtil#gavToJar(String)}
     *
     * @return a translated maven path
     */
    public String translateGavToJar(String gav) {
        return gavToMvnPath(gav);
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
                    .substring(destination.getParent().toString().indexOf("MAVEN-INF")), destination.toUri().toURL());
        } catch (IOException e) {
            LOG.error("[copyJar] Something went wrong during jar copying...", e);
            throw new IllegalStateException(e);
        }
    }

}
