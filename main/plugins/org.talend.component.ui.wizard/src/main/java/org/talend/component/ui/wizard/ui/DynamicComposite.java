// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.component.ui.wizard.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.talend.component.core.constants.IContextEventProperties;
import org.talend.component.core.constants.IElementParameterEventProperties;
import org.talend.component.core.model.GenericElementParameter;
import org.talend.component.core.utils.ComponentsUtils;
import org.talend.component.ui.model.genericMetadata.GenericConnection;
import org.talend.component.ui.wizard.i18n.Messages;
import org.talend.component.ui.wizard.internal.IGenericWizardInternalService;
import org.talend.component.ui.wizard.internal.service.GenericComponentServiceImpl;
import org.talend.component.ui.wizard.internal.service.GenericWizardInternalService;
import org.talend.component.ui.wizard.model.FakeElement;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.ValidationResult;
import org.talend.components.api.properties.ValidationResult.Result;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.service.ComponentService;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Property;
import org.talend.core.ui.check.Checker;
import org.talend.core.ui.check.IChecker;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.ui.views.properties.MultipleThreadDynamicComposite;

/**
 * 
 * created by ycbai on 2015年9月24日 Detailled comment
 *
 */
public class DynamicComposite extends MultipleThreadDynamicComposite implements PropertyChangeListener {

    private Element element;

    private Form form;

    private IChecker checker;

    private ConnectionItem connectionItem;

    private List<String> excludeParameterNames = new ArrayList<>();

    private IGenericWizardInternalService internalService;

    public DynamicComposite(Composite parentComposite, int styles, EComponentCategory section, Element element,
            boolean isCompactView, Color backgroundColor, Form form) {
        this(parentComposite, styles, section, element, isCompactView, backgroundColor, form, null);
    }

    public DynamicComposite(Composite parentComposite, int styles, EComponentCategory section, Element element,
            boolean isCompactView, Color backgroundColor, Form form, IChecker checker) {
        super(parentComposite, styles, section, element, isCompactView, backgroundColor);
        this.element = element;
        this.form = form;
        if (checker == null) {
            this.checker = new Checker();
        } else {
            this.checker = checker;
        }
        internalService = new GenericWizardInternalService();
    }

    private void resetComponentProperties() {
        if (connectionItem != null) {
            Connection connection = connectionItem.getConnection();
            if (connection instanceof GenericConnection) {
                GenericConnection genericConnection = (GenericConnection) connection;
                genericConnection.setCompProperties(form.getComponentProperties().toSerialized());
            }
        }
    }

    public List<ElementParameter> resetParameters() {
        ComponentService genericComponentService = new GenericComponentServiceImpl(internalService.getComponentService(),
                connectionItem);
        List<ElementParameter> parameters = ComponentsUtils.getParametersFromForm(element, null, form, null, null);
        Iterator<ElementParameter> parametersIterator = parameters.iterator();
        while (parametersIterator.hasNext()) {
            ElementParameter parameter = parametersIterator.next();
            if (parameter instanceof GenericElementParameter) {
                GenericElementParameter genericElementParameter = (GenericElementParameter) parameter;
                if (excludeParameterNames != null && excludeParameterNames.contains(genericElementParameter.getName())) {
                    parametersIterator.remove();
                    continue;
                }
                genericElementParameter.setComponentService(genericComponentService);
                genericElementParameter.callBeforePresent();
                genericElementParameter.addPropertyChangeListener(this);
            }
        }
        parameters.add(getUpdateParameter());
        element.setElementParameters(parameters);
        return parameters;
    }

    public List<ElementParameter> resetElementParameters() {
        List<ElementParameter> oldParameters = (List<ElementParameter>) element.getElementParameters();
        final List<ElementParameter> newParameters = new ArrayList<ElementParameter>();
        ComponentProperties props = null;
        if (element instanceof INode) {
            INode node = (INode) element;
            if (node.getComponentProperties() != null) {
                props = node.getComponentProperties();
            }
        }
        List<ElementParameter> parameters = ComponentsUtils
                .getParametersFromForm(element, section, props, null, form, null, null);
        for (ElementParameter parameter : parameters) {
            if (parameter instanceof GenericElementParameter) {
                ((GenericElementParameter) parameter).callBeforePresent();
                ((GenericElementParameter) parameter).addPropertyChangeListener(this);
            }
        }
        for (ElementParameter oldParameter : oldParameters) {
            if (EParameterName.UPDATE_COMPONENTS.getName().equals(oldParameter.getName())) {
                oldParameter.setValue(true);
            }
            boolean added = false;
            for (ElementParameter parameter : parameters) {
                if (oldParameter.getCategory() != null && oldParameter.getCategory().equals(parameter.getCategory())
                        && oldParameter.getName() != null && oldParameter.getName().equals(parameter.getName())) {
                    if (EParameterFieldType.SCHEMA_TYPE.equals(parameter.getFieldType())) {
                        if (oldParameter instanceof GenericElementParameter && parameter instanceof GenericElementParameter) {
                            ComponentProperties oldProperties = ((GenericElementParameter) oldParameter).getComponentProperties();
                            ComponentProperties newProperties = ((GenericElementParameter) parameter).getComponentProperties();
                            if (oldProperties != null && oldProperties.getName() != null && newProperties != null
                                    && newProperties.getName() != null && oldProperties.getName().equals(newProperties.getName())) {
                                if (parameter.getChildParameters().size() == 0) {
                                    parameter.getChildParameters().putAll(oldParameter.getChildParameters());
                                }
                            }
                        }
                    }
                    // Repository
                    IElementParameter property = element.getElementParameterFromField(EParameterFieldType.PROPERTY_TYPE, section);
                    if (property != null) {
                        Map<String, IElementParameter> childParameters = property.getChildParameters();
                        if (childParameters != null) {
                            IElementParameter elementParameter = childParameters.get(EParameterName.PROPERTY_TYPE.getName());
                            if (elementParameter != null && EmfComponent.REPOSITORY.equals(elementParameter.getValue())) {
                                String repositoryValue = parameter.getRepositoryValue();
                                if (oldParameter.isShow(oldParameters) && (repositoryValue != null)
                                        && (!parameter.getName().equals(EParameterName.PROPERTY_TYPE.getName()))
                                        && parameter.getCategory() == section) {
                                    parameter.setRepositoryValueUsed(true);
                                    parameter.setReadOnly(true);
                                }
                            }
                        }
                    }
                    newParameters.add(parameter);
                    added = true;
                }
            }
            if (!added) {
                newParameters.add(oldParameter);
            }
        }
        element.setElementParameters(newParameters);
        return newParameters;
    }

    private void updateProperty(String newPropertyName) {
        if (connectionItem == null) {
            return;
        }
        Connection connection = connectionItem.getConnection();
        Property connectionProperty = connectionItem.getProperty();
        String propertyName = StringUtils.trimToNull(newPropertyName);
        connectionProperty.setDisplayName(propertyName);
        connectionProperty.setLabel(propertyName);
        connectionProperty.setModificationDate(new Date());
        connectionProperty.setLabel(propertyName);
        connection.setName(propertyName);
        connection.setLabel(propertyName);
    }

    private ElementParameter getUpdateParameter() {
        ElementParameter param = new ElementParameter(element);
        param.setName(EParameterName.UPDATE_COMPONENTS.getName());
        param.setValue(true);
        param.setDisplayName(EParameterName.UPDATE_COMPONENTS.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.TECHNICAL);
        param.setNumRow(1000);
        param.setReadOnly(true);
        param.setRequired(false);
        param.setShow(false);
        return param;
    }

    private void updateValidationStatus(ValidationResult validationResult) {
        String validationMessage = validationResult.getMessage();
        Result validationStatus = validationResult.getStatus();
        switch (validationStatus) {
        case WARNING:
            checker.updateStatus(IStatus.WARNING, null);
            MessageDialog.openWarning(getShell(), Messages.getString("DynamicComposite.connectionTest.title"), validationMessage); //$NON-NLS-1$
            break;
        case ERROR:
            checker.updateStatus(IStatus.ERROR, null);
            MessageDialog.openError(getShell(), Messages.getString("DynamicComposite.connectionTest.title"), validationMessage); //$NON-NLS-1$
            break;
        default:
            checker.updateStatus(IStatus.OK, null);
            MessageDialog.openInformation(getShell(), Messages.getString("DynamicComposite.connectionTest.title"), //$NON-NLS-1$
                    Messages.getString("DynamicComposite.connectionTest.msg.success")); //$NON-NLS-1$
            break;
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        String propertyName = event.getPropertyName();
        if (IElementParameterEventProperties.EVENT_PROPERTY_VALUE_CHANGED.equals(propertyName)) {
            reset(true);
        } else if (IElementParameterEventProperties.EVENT_PROPERTY_NAME_CHANGED.equals(propertyName)) {
            String newPropertyName = String.valueOf(event.getNewValue());
            updateProperty(newPropertyName);
        } else if (IElementParameterEventProperties.EVENT_VALIDATE_RESULT_UPDATE.equals(propertyName)) {
            Object newValue = event.getNewValue();
            if (newValue instanceof ValidationResult) {
                updateValidationStatus((ValidationResult) newValue);
            }
        } else if (IElementParameterEventProperties.EVENT_SHOW_DIALOG.equals(propertyName)) {
            Object newValue = event.getNewValue();
            if (newValue instanceof Form) {
                new GenericDialog(getShell(), (Form) newValue).open();
            }
        } else if (IContextEventProperties.EVENT_PROPERTY_EXPORT_CONTEXT.equals(propertyName)) {
            resetComponentProperties();
        } else if (IContextEventProperties.EVENT_PROPERTY_REFRESH_UI.equals(propertyName)) {
            Object newValue = event.getNewValue();
            if (newValue instanceof ComponentProperties) {
                ComponentProperties newComponentProperties = (ComponentProperties) newValue;
                form.getComponentProperties().copyValuesFrom(newComponentProperties);
                reset(true);
            }
        }
    }

    public IChecker getChecker() {
        return this.checker;
    }

    private void reset(boolean refresh) {
        if (element instanceof FakeElement) {
            resetParameters();
        } else {
            resetElementParameters();
        }
        if (refresh) {
            Display.getDefault().asyncExec(new Runnable() {

                @Override
                public void run() {
                    refresh();
                }
            });
        }
    }

    @Override
    public synchronized void dispose() {
        List<? extends IElementParameter> elementParameters = element.getElementParameters();
        for (IElementParameter elementParameter : elementParameters) {
            if (elementParameter instanceof GenericElementParameter) {
                ((GenericElementParameter) elementParameter).removePropertyChangeListener(this);
            }
        }
        super.dispose();
    }

    public ConnectionItem getConnectionItem() {
        return this.connectionItem;
    }

    public void setConnectionItem(ConnectionItem connectionItem) {
        this.connectionItem = connectionItem;
    }

    public List<String> getExcludeParameterNames() {
        return this.excludeParameterNames;
    }

    public void setExcludeParameterNames(List<String> excludeParameterNames) {
        this.excludeParameterNames = excludeParameterNames;
    }

}
