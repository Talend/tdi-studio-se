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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IElementParameter;
import org.talend.designer.core.model.FakeElement;
import org.talend.designer.core.model.components.DummyComponent;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.model.process.DataNode;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.i18n.Messages;
import org.talend.sdk.component.studio.metadata.model.TaCoKitConfigurationModel;
import org.talend.sdk.component.studio.model.parameter.Layout;
import org.talend.sdk.component.studio.model.parameter.LayoutParameter;
import org.talend.sdk.component.studio.model.parameter.Metadatas;
import org.talend.sdk.component.studio.model.parameter.PropertyNode;
import org.talend.sdk.component.studio.model.parameter.PropertyTreeCreator;
import org.talend.sdk.component.studio.model.parameter.VersionParameter;
import org.talend.sdk.component.studio.ui.composite.TaCoKitWizardComposite;
import org.talend.sdk.component.studio.ui.composite.problemmanager.WizardProblemManager;
import org.talend.sdk.component.studio.ui.composite.problemmanager.WizardProblemManager.IWizardHandler;
import org.talend.sdk.component.studio.ui.wizard.TaCoKitConfigurationRuntimeData;

/**
 * DOC cmeng class global comment. Detailled comment
 */
public class TaCoKitConfigurationWizardPage extends AbsTaCoKitWizardPage {

    private final boolean isNew;

    private final Element element;

    private TaCoKitConfigurationModel configurationModel;

    private IStatus tacokitConfigStatus;

    private final String form;

    private final EComponentCategory category;

    private WizardProblemManager problemManager;

    public TaCoKitConfigurationWizardPage(final TaCoKitConfigurationRuntimeData runtimeData,
            final String form,
            final boolean isNew,
            List<IElementParameter> parameters) {
        super(Messages.getString("WizardPage.TaCoKitConfiguration"), runtimeData); //$NON-NLS-1$
        this.isNew = isNew;
        final ConfigTypeNode configTypeNode = runtimeData.getConfigTypeNode();
        setTitle(Messages.getString("TaCoKitConfiguration.wizard.title.str", configTypeNode.getDisplayName(), //$NON-NLS-1$
                configTypeNode.getConfigurationType()));
        setDescription(Messages.getString("TaCoKitConfiguration.wizard.description.edit.str", //$NON-NLS-1$
                configTypeNode.getDisplayName(), configTypeNode.getConfigurationType()));
        this.form = form;
        this.category = Metadatas.MAIN_FORM.equals(form) ? EComponentCategory.BASIC : EComponentCategory.ADVANCED;
        if (!runtimeData.isReadonly()) {
            try {
                // do not remove. It initializes connection instance
                TaCoKitConfigurationModel configurationItemModel = new TaCoKitConfigurationModel(
                        runtimeData.getConnectionItem().getConnection(), runtimeData.getConfigTypeNode());
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }
        problemManager = new WizardProblemManager();
        runtimeData.registProblemManager(problemManager);
        problemManager.setWizardHandler(new WizardHandler());

        element = new FakeElement(runtimeData.getTaCoKitRepositoryNode().getConfigTypeNode().getDisplayName());
        element.setReadOnly(runtimeData.isReadonly());

        final PropertyNode root = new PropertyTreeCreator(new WizardTypeMapper()).createPropertyTree(configTypeNode);

        final ElementParameter layoutParameter = createLayoutParameter(root, form, category, element);
        parameters.add(layoutParameter);
        element.setElementParameters(parameters);
    }

    @Override
    public void createControl(final Composite parent) {
        try {
            final Composite container = new Composite(parent, SWT.NONE);
            container.setLayoutData(new GridData(GridData.FILL_BOTH));
            container.setLayout(new FormLayout());
            setControl(container);

            final TaCoKitConfigurationRuntimeData runtimeData = getTaCoKitConfigurationRuntimeData();
            configurationModel = getConfigurationItemModel();
            final ConfigTypeNode configTypeNode = runtimeData.getConfigTypeNode();
            final DummyComponent component = new DummyComponent(configTypeNode.getDisplayName());
            final DataNode node = new DataNode(component, component.getName());

            //add version params
            Map<String, ConfigTypeNode> nodes = Lookups.taCoKitCache().getConfigTypeNodeMap();
            configTypeNode.getProperties()
                    .stream()
                    .filter(p -> p.getMetadata().containsKey("configurationtype::type") && p.getMetadata().containsKey("configurationtype::name"))
                    .map(p -> new VersionParameter(node, p.getPath(),
                            nodes.values().stream()
                                    .filter(n -> n.getConfigurationType() != null)
                                    .filter(n -> p.getMetadata().get("configurationtype::type").equals(n.getConfigurationType()))
                                    .filter(n -> n.getName().equals(p.getMetadata().get("configurationtype::name")))
                                    .findFirst()
                                    .map(n -> String.valueOf(n.getVersion())).orElse("-1")))
                    .forEach(p -> configurationModel.setValue(p));

            final TaCoKitWizardComposite tacokitComposite = new TaCoKitWizardComposite(container, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS, category,
                    element, configurationModel, true, container.getBackground(), isNew, problemManager);
            tacokitComposite.setLayoutData(createMainFormData(runtimeData.isAddContextFields()));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private TaCoKitConfigurationModel getConfigurationItemModel() {
        if (this.configurationModel == null) {
            try {
                final TaCoKitConfigurationRuntimeData runtimeData = getTaCoKitConfigurationRuntimeData();
                configurationModel = new TaCoKitConfigurationModel(runtimeData.getConnectionItem().getConnection());
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }
        return this.configurationModel;
    }

    private FormData createMainFormData(final boolean addContextSupport) {
        final FormData data = new FormData();
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(100, 0);
        data.top = new FormAttachment(0, 0);
        if (addContextSupport) {
            data.bottom = new FormAttachment(85, 0);
        } else {
            data.bottom = new FormAttachment(100, 0);
        }
        return data;
    }

    // TODO it is duplicated in ElementParameterCreator. Refactor to avoid duplication
    private ElementParameter createLayoutParameter(final PropertyNode root, final String form,
            final EComponentCategory category, final Element element) {
        final Layout layout = root.getLayout(form);
        final LayoutParameter layoutParameter = new LayoutParameter(element, layout, category);
        return layoutParameter;
    }

    @Override
    protected IStatus[] getStatuses() {
        List<IStatus> status = Arrays.asList(super.getStatuses());
        status.add(tacokitConfigStatus);
        return status.toArray(new IStatus[0]);
    }

    @Override
    public boolean isPageComplete() {
        if (problemManager != null) {
            return !problemManager.hasError() && !problemManager.hasUnresolvedRequiredElem();
        }
        return super.isPageComplete();
    }

    private class WizardHandler implements IWizardHandler {

        @Override
        public void showError(String error) {
            getCurrentPage().ifPresent(c -> c.setMessage(error, WizardPage.ERROR));
        }

        @Override
        public void showWarn(String warn) {
            getCurrentPage().ifPresent(c -> c.setMessage(warn, WizardPage.WARNING));
        }

        @Override
        public void showInfo(String info) {
            getCurrentPage().ifPresent(c -> c.setMessage(info, WizardPage.INFORMATION));
        }

        @Override
        public void updateStatus() {
            getContainer().updateButtons();
        }

        private Optional<WizardPage> getCurrentPage() {
            // IWizardContainer container = getContainer();
            // if (container != null) {
            // return Optional.ofNullable((WizardPage) container.getCurrentPage());
            // }
            return Optional.ofNullable(TaCoKitConfigurationWizardPage.this);
        }

    }
}
