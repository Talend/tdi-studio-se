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
package org.talend.sdk.component.studio.ui.wizard.page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IElementParameter;
import org.talend.designer.core.model.FakeElement;
import org.talend.designer.core.model.components.DummyComponent;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.model.process.DataNode;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.studio.i18n.Messages;
import org.talend.sdk.component.studio.metadata.model.TaCoKitConfigurationItemModel;
import org.talend.sdk.component.studio.metadata.model.TaCoKitConfigurationModel;
import org.talend.sdk.component.studio.model.parameter.Layout;
import org.talend.sdk.component.studio.model.parameter.LayoutParameter;
import org.talend.sdk.component.studio.model.parameter.Metadatas;
import org.talend.sdk.component.studio.model.parameter.PropertyNode;
import org.talend.sdk.component.studio.model.parameter.PropertyTreeCreator;
import org.talend.sdk.component.studio.model.parameter.SettingVisitor;
import org.talend.sdk.component.studio.ui.composite.TaCoKitWizardComposite;
import org.talend.sdk.component.studio.ui.wizard.TaCoKitConfigurationRuntimeData;

/**
 * DOC cmeng class global comment. Detailled comment
 */
public class TaCoKitConfigurationWizardPage extends AbsTaCoKitWizardPage {

    private Element element;

    private TaCoKitWizardComposite tacokitComposite;

    private TaCoKitConfigurationModel configurationModel;

    private IStatus tacokitConfigStatus;

    private final String form;

    private final EComponentCategory category;

    public TaCoKitConfigurationWizardPage(final TaCoKitConfigurationRuntimeData runtimeData, final String form) {
        super(Messages.getString("WizardPage.TaCoKitConfiguration"), runtimeData); //$NON-NLS-1$
        final ConfigTypeNode configTypeNode = runtimeData.getConfigTypeNode();
        setTitle(Messages.getString("TaCoKitConfiguration.wizard.title", configTypeNode.getConfigurationType(), // $NON-NLS-1$
                configTypeNode.getDisplayName()));
        setDescription(Messages.getString("TaCoKitConfiguration.wizard.description.edit",
                configTypeNode.getConfigurationType(), // $NON-NLS-1$
                configTypeNode.getDisplayName()));
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
            final boolean addContextFields = runtimeData.isAddContextFields();

            final ConfigTypeNode configTypeNode = runtimeData.getConfigTypeNode();
            final DummyComponent component = new DummyComponent(configTypeNode.getDisplayName());
            final DataNode node = new DataNode(component, component.getName());
            final PropertyNode root =
                    new PropertyTreeCreator(new WizardTypeMapper()).createPropertyTree(configTypeNode);
            element = new FakeElement(runtimeData.getTaCoKitRepositoryNode().getConfigTypeNode().getDisplayName());
            element.setReadOnly(runtimeData.isReadonly());
            final ElementParameter updateParameter = createUpdateComponentsParameter(element);
            final List<IElementParameter> parameters = new ArrayList<>();
            parameters.add(updateParameter);
            final SettingVisitor settingsCreator =
                    new SettingVisitor(node, updateParameter, configTypeNode).withCategory(category);
            root.accept(settingsCreator, form);
            parameters.addAll(settingsCreator.getSettings());
            final ElementParameter layoutParameter = createLayoutParameter(root, form, category, element);
            parameters.add(layoutParameter);
            element.setElementParameters(parameters);

            tacokitComposite =
                    new TaCoKitWizardComposite(container, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS, category,
                            element, configurationModel, true, container.getBackground());
            tacokitComposite.setLayoutData(createMainFormData(addContextFields));

            if (addContextFields) {
                // Composite contextParentComp = new Composite(container, SWT.NONE);
                // contextParentComp.setLayoutData(createFooterFormData(tacokitComposite));
                // contextParentComp.setLayout(new GridLayout());
                // ContextComposite contextComp = addContextFields(contextParentComp);
                // contextComp.addPropertyChangeListener(tacokitComposite);
                // contextComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            }
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

    /**
     * Creates and adds {@link EParameterName#UPDATE_COMPONENTS} parameter
     * This parameter is used to decide whether UI should be redrawn during Composite refresh
     */
    // TODO it is duplicated in ElementParameterCreator. Refactor to avoid duplication
    private ElementParameter createUpdateComponentsParameter(final Element element) {
        final ElementParameter parameter = new ElementParameter(element);
        parameter.setName(EParameterName.UPDATE_COMPONENTS.getName());
        parameter.setValue(false);
        parameter.setDisplayName(EParameterName.UPDATE_COMPONENTS.getDisplayName());
        parameter.setFieldType(EParameterFieldType.CHECK);
        parameter.setCategory(EComponentCategory.TECHNICAL);
        parameter.setReadOnly(true);
        parameter.setRequired(false);
        parameter.setShow(false);
        return parameter;
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
        return Arrays.asList(super.getStatuses(), tacokitConfigStatus).toArray(new IStatus[0]);
    }

}
