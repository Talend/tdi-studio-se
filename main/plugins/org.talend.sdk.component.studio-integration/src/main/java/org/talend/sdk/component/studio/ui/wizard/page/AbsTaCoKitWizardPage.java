/**
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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
package org.talend.sdk.component.studio.ui.wizard.page;

import org.eclipse.swt.widgets.Composite;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.metadata.managment.ui.wizard.AbstractNamedWizardPage;
import org.talend.sdk.component.studio.ui.wizard.TaCoKitConfigurationRuntimeData;
import org.talend.sdk.component.studio.util.TaCoKitConst;

public abstract class AbsTaCoKitWizardPage extends AbstractNamedWizardPage {

    private TaCoKitConfigurationRuntimeData runtimeData;

    protected AbsTaCoKitWizardPage(final String pageName, final TaCoKitConfigurationRuntimeData runtimeData) {
        super(pageName);
        this.runtimeData = runtimeData;
    }

    @Override
    public void createControl(final Composite arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public ERepositoryObjectType getRepositoryObjectType() {
        return TaCoKitConst.METADATA_TACOKIT;
    }

    @Override
    public Property getProperty() {
        // TODO Auto-generated method stub
        return null;
    }

    public TaCoKitConfigurationRuntimeData getTaCoKitConfigurationRuntimeData() {
        return this.runtimeData;
    }

    public void setTaCoKitConfigurationRuntimeData(final TaCoKitConfigurationRuntimeData runtimeData) {
        this.runtimeData = runtimeData;
    }

}
