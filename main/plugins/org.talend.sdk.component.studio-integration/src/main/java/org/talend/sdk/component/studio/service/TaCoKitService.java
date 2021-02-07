/**
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.talend.sdk.component.studio.service;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.IProgressMonitor;
import org.talend.commons.runtime.service.ITaCoKitService;
import org.talend.sdk.component.server.front.model.ComponentDetail;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.metadata.TaCoKitCache;
import org.talend.sdk.component.studio.model.parameter.PropertyDefinitionDecorator;
import org.talend.sdk.component.studio.model.parameter.VersionParameter;
import org.talend.sdk.component.studio.toolbar.ReloadAction;
import org.talend.sdk.component.studio.util.TaCoKitUtil;
import org.talend.updates.runtime.service.ITaCoKitUpdateService;


public class TaCoKitService implements ITaCoKitService {

    @Override
    public String reload(IProgressMonitor monitor) throws Exception {
        return new ReloadAction().reload(monitor);
    }

    @Override
    public boolean isTaCoKitCar(File file, IProgressMonitor monitor) throws Exception {
        return ITaCoKitUpdateService.getInstance().isCar(file, monitor);
    }

    @Override
    public boolean isNeedMigration(String componentName, Map<String, String> persistedProperties) {
        TaCoKitCache currentCach = Lookups.taCoKitCache();
        Optional<ComponentDetail> detail = Lookups.service().getDetail(componentName);
        final Collection<PropertyDefinitionDecorator> properties = PropertyDefinitionDecorator
                .wrap(detail.get().getProperties());
        for (String key : persistedProperties.keySet()) {
            for (PropertyDefinitionDecorator p : properties) {
                if (p.getConfigurationType() != null && p.getConfigurationTypeName() != null
                        && (p.getPath() + VersionParameter.VERSION_SUFFIX).equals(key)) {
                    int currentVersion = TaCoKitUtil.getConfigTypeVersion(p, currentCach.getConfigTypeNodes(),
                            detail.get().getId().getFamilyId());
                    int persistedVersion = Integer.parseInt(persistedProperties.get(key));
                    if (currentVersion > persistedVersion) {
                        return true;
                    }
                    break;
                }
            }
        }
        return false;
    }
}
