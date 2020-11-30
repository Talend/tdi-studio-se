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
package org.talend.sdk.component.studio.service;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.properties.Item;
import org.talend.core.service.ITaCoKitDependencyService;
import org.talend.sdk.component.studio.ComponentModel;
import org.talend.sdk.component.studio.util.TaCoKitUtil;

public class TaCoKitDependencyService implements ITaCoKitDependencyService {

    private final static Logger LOG = LoggerFactory.getLogger(TaCoKitDependencyService.class.getName());

    public final static List<String> TACOKIT_JARS_BLACKLIST = Arrays.asList("component-api-",
            "component-runtime-design-extension-", "component-runtime-di-", "component-runtime-impl-",
            "component-runtime-manager-", "component-spi-", "container-core-", "geronimo-annotation_1.3_spec-",
            "geronimo-json_1.1_spec-", "geronimo-jsonb_1.0_spec-", "johnzon-core-", "johnzon-jsonb-", "johnzon-mapper-",
            "slf4j-api-", "slf4j-log4j12-", "xbean-asm7-shaded-", "xbean-finder-shaded-", "xbean-reflect-");

    /**
     * @param components
     *
     * @return
     */
    @Override
    public Set<String> getTaCoKitOnlyDependencies(final Stream<IComponent> components) {
        LOG.debug("[tckOnlyDependencies] Searching for components dependencies...");
        final Set<String> diDeps = new HashSet<>();
        final Set<String> tckDeps = new HashSet<>();
        components.forEach(component -> {
            final Set<String> deps = component.getModulesNeeded()
                    .stream()
                    .map(d -> Optional.ofNullable(d.getModuleLocaion())
                            .map(loc -> loc.substring(loc.lastIndexOf("/") + 1))
                            .orElseGet(() -> d.getModuleName()))
                    .filter(jar -> TACOKIT_JARS_BLACKLIST.stream().noneMatch(tck -> jar.contains(tck)))
                    .collect(Collectors.toSet());
            if (ComponentModel.class.isInstance(component)) {
                tckDeps.addAll(deps);
            } else {
                diDeps.addAll(deps);
            }
        });
        tckDeps.removeAll(diDeps);
        LOG.info("[tckOnlyDependencies] Found {} TaCoKit components only dependencies.", tckDeps.size());
        return tckDeps;
    }

    @Override
    public Stream<IComponent> getJobComponents(final Item item) {
        return TaCoKitUtil.getJobComponents(item);
    }

    @Override
    public Path findM2Path() {
        return TaCoKitUtil.findM2Path();
    }

    @Override
    public boolean hasTaCoKitComponents(final Stream<IComponent> components) {
        return TaCoKitUtil.hasTaCoKitComponents(components);
    }

}
