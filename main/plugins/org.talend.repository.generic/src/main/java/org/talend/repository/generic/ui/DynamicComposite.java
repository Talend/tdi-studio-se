// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.generic.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.components.api.service.ComponentService;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.ui.check.Checker;
import org.talend.core.ui.check.IChecker;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyValueEvaluator;
import org.talend.designer.core.generic.constants.IContextEventProperties;
import org.talend.designer.core.generic.constants.IElementParameterEventProperties;
import org.talend.designer.core.generic.constants.IGenericConstants;
import org.talend.designer.core.generic.context.ComponentContextPropertyValueEvaluator;
import org.talend.designer.core.generic.model.GenericElementParameter;
import org.talend.designer.core.generic.utils.ComponentsUtils;
import org.talend.designer.core.generic.utils.SchemaUtils;
import org.talend.designer.core.model.FakeElement;
import org.talend.designer.core.model.components.AbstractBasicComponent;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.ui.views.properties.composites.MissingSettingsMultiThreadDynamicComposite;
import org.talend.metadata.managment.ui.wizard.context.MetadataContextPropertyValueEvaluator;
import org.talend.repository.generic.i18n.Messages;
import org.talend.repository.generic.internal.IGenericWizardInternalService;
import org.talend.repository.generic.internal.service.GenericWizardInternalService;
import org.talend.repository.generic.model.genericMetadata.SubContainer;
import org.talend.repository.generic.util.GenericConnectionUtil;

/**
 *
 * created by ycbai on 2015年9月24日 Detailled comment
 *
 */
public class DynamicComposite extends MissingSettingsMultiThreadDynamicComposite implements PropertyChangeListener {

    protected Element element;

    protected Form form;

    protected IChecker checker;

    protected ConnectionItem connectionItem;

    protected IGenericWizardInternalService internalService;

    protected boolean drivedByForm;

    protected PropertyChangeListener wizardPropertyChangeListener;

    public DynamicComposite(Composite parentComposite, int styles, EComponentCategory section, Element element,
            boolean isCompactView, Color backgroundColor, Form form) {
        this(parentComposite, styles, section, element, isCompactView, backgroundColor, form, false);
    }

    public DynamicComposite(Composite parentComposite, int styles, EComponentCategory section, Element element,
            boolean isCompactView, Color backgroundColor, Form form, boolean drivedByForm) {
        super(parentComposite, styles, section, element, isCompactView, backgroundColor);
        this.element = element;
        this.form = form;
        this.drivedByForm = drivedByForm;
        checker = new Checker();
        internalService = new GenericWizardInternalService();
        if (drivedByForm) {
            internalService.getComponentService().makeFormCancelable(form.getProperties(), form.getName());
        }
        resetParameters(true);
    }

    public DynamicComposite(Composite parentComposite, int styles, EComponentCategory section, Element element,
            ConnectionItem connectionItem, boolean isCompactView, Color backgroundColor, Form form, boolean drivedByForm) {
        super(parentComposite, styles, section, element, isCompactView, backgroundColor);
        this.element = element;
        this.form = form;
        this.drivedByForm = drivedByForm;
        this.connectionItem = connectionItem;
        checker = new Checker();
        internalService = new GenericWizardInternalService();
        if (drivedByForm) {
            internalService.getComponentService().makeFormCancelable(form.getProperties(), form.getName());
        }
        resetParameters(true);
    }

    private void resetComponentProperties() {
        if (connectionItem != null) {
            Connection connection = connectionItem.getConnection();
            connection.setCompProperties(form.getProperties().toSerialized());
        }
    }

    protected void editJDBCParameter(boolean isforEditor, Connection dbConnection,
            GenericElementParameter genericElementParameter) {
    }

    public List<ElementParameter> resetParameters(boolean isforEditor) {
        final List<ElementParameter> newParameters = new ArrayList<>();
        List<ElementParameter> currentParameters = (List<ElementParameter>) element.getElementParameters();
        List<ElementParameter> parameters = new ArrayList<>();
        ComponentService componentService = internalService.getComponentService();
        Connection theConnection = null;
        if (connectionItem != null) {
            theConnection = connectionItem.getConnection();
        }

        Properties properties = null;
        PropertyValueEvaluator evaluator = null;
        if (element instanceof FakeElement) {
            properties = form.getProperties();
            evaluator = new MetadataContextPropertyValueEvaluator(theConnection);
            currentParameters.clear();
        } else {
            properties = ((INode) element).getComponentProperties();
            evaluator = new ComponentContextPropertyValueEvaluator((INode) element);
        }
        if (properties instanceof ComponentProperties) {
            if (form != null) {
                properties.setValueEvaluator(evaluator);
                properties.refreshLayout(form);
            }
            properties.setValueEvaluator(null); // For context display.
            boolean isInitializing = false;
            if (element instanceof INode) {
                INode node = (INode) element;
                IComponent component = node.getComponent();
                if (component instanceof AbstractBasicComponent) {
                    isInitializing = ((AbstractBasicComponent) component).isInitializing();
                }
                try {
                    GenericConnectionUtil.synRefProperties(properties, node.getProcess());
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }
            parameters = ComponentsUtils.getParametersFromForm(element, isInitializing, section, (ComponentProperties) properties,
                    form);
            addUpdateParameterIfNotExist(parameters);
            properties.setValueEvaluator(evaluator);
        }
        for (ElementParameter parameter : parameters) {
            if (parameter instanceof GenericElementParameter) {
                GenericElementParameter genericElementParameter = (GenericElementParameter) parameter;
                editJDBCParameter(isforEditor, theConnection, genericElementParameter);
                genericElementParameter.setComponentService(componentService);
                genericElementParameter.setDrivedByForm(drivedByForm);
                genericElementParameter.callBeforePresent();
                genericElementParameter.removePropertyChangeListener(this);
                genericElementParameter.addPropertyChangeListener(this);
                if (wizardPropertyChangeListener != null && IGenericConstants.NAME_PROPERTY.equals(parameter.getName())) {
                    genericElementParameter.addPropertyChangeListener(wizardPropertyChangeListener);
                }
                if (EParameterFieldType.SCHEMA_REFERENCE.equals(genericElementParameter.getFieldType())) {
                    if (genericElementParameter.getChildParameters().size() == 0) {
                        IElementParameter schemaParameter = element
                                .getElementParameterFromField(EParameterFieldType.SCHEMA_REFERENCE, section);
                        genericElementParameter.getChildParameters().putAll(schemaParameter.getChildParameters());
                    }
                } else if (EParameterFieldType.NAME_SELECTION_AREA.equals(genericElementParameter.getFieldType())
                        && theConnection != null) {
                    List<NamedThing> values = new ArrayList<>();
                    List<MetadataTable> metadataTables = null;
                    if (connectionItem.getTypeName() != null
                            && SchemaUtils.isExtraDBType(ERepositoryObjectType.valueOf(connectionItem.getTypeName()))) {
                        metadataTables = ConnectionHelper.getTablesWithOrders(theConnection);
                    } else {
                        metadataTables = SchemaUtils.getMetadataTables(theConnection, SubContainer.class);
                    }
                    List<String> tableLabels = new ArrayList<>();
                    for (MetadataTable metaTable : metadataTables) {
                        tableLabels.add(metaTable.getLabel());
                    }
                    List<NamedThing> possibleValues = ComponentsUtils.getFormalPossibleValues(genericElementParameter);
                    for (NamedThing possibleValue : possibleValues) {
                        if (tableLabels.contains(possibleValue.getName())) {
                            values.add(possibleValue);
                        }
                    }
                    genericElementParameter.setValue(values);
                }
                if (properties != null && isRepository(element)) {
                    String repositoryValue = genericElementParameter.getRepositoryValue();
                    if (genericElementParameter.isShow(currentParameters) && (repositoryValue != null)
                            && (!genericElementParameter.getName().equals(EParameterName.PROPERTY_TYPE.getName()))
                            && genericElementParameter.getCategory() == section) {
                        org.talend.daikon.properties.property.Property property = properties
                                .getValuedProperty(genericElementParameter.getName());
                        if (property != null && property.getTaggedValue(IGenericConstants.REPOSITORY_VALUE) != null) {
                            genericElementParameter.setRepositoryValueUsed(true);
                            genericElementParameter.setReadOnly(true);
                        }
                        Properties pros = properties.getProperties(genericElementParameter.getName());
                        if (pros != null) {
                            boolean isRepValueUsed = true;
                            for (NamedThing thing : pros.getProperties()) {
                                if (thing instanceof Property) {
                                    boolean result = ((Property) thing)
                                            .getTaggedValue(IGenericConstants.REPOSITORY_VALUE) != null;
                                    if (!result) {
                                        isRepValueUsed = false;
                                    }
                                }
                            }
                            genericElementParameter.setRepositoryValueUsed(isRepValueUsed);
                            genericElementParameter.setReadOnly(isRepValueUsed);
                        }
                    }
                }
            }
        }

        boolean added = false;
        for (ElementParameter currentParameter : currentParameters) {
            if (EParameterName.UPDATE_COMPONENTS.getName().equals(currentParameter.getName())) {
                currentParameter.setValue(true);
            }
            if (currentParameter.isSerialized() && currentParameter.getCategory().equals(section)) {
                if (!added) {
                    newParameters.addAll(parameters);
                    added = true;
                }
                continue;
            }
            newParameters.add(currentParameter);
        }
        if (element instanceof FakeElement) {
            newParameters.addAll(reverseParameters(parameters));
        }
        element.setElementParameters(newParameters);
        return newParameters;
    }

    protected List<ElementParameter> reverseParameters(List<ElementParameter> parameters) {
        List<ElementParameter> reversedParameters = new ArrayList<>();
        Map<Integer, List<ElementParameter>> paramMap = new LinkedHashMap<>();
        for (ElementParameter parameter : parameters) {
            int numRow = parameter.getNumRow();
            List<ElementParameter> params = paramMap.get(numRow);
            if (params == null) {
                params = new ArrayList<>();
                paramMap.put(numRow, params);
            }
            params.add(parameter);
        }
        Set<Entry<Integer, List<ElementParameter>>> paramEntrySet = paramMap.entrySet();
        Iterator<Entry<Integer, List<ElementParameter>>> paramIterator = paramEntrySet.iterator();
        while (paramIterator.hasNext()) {
            Entry<Integer, List<ElementParameter>> paramEntry = paramIterator.next();
            List<ElementParameter> params = paramEntry.getValue();
            if (params != null && params.size() > 1) {
                Collections.reverse(params);
            }
            reversedParameters.addAll(params);
        }
        return reversedParameters;
    }

    protected boolean isRepository(Element element) {
        IElementParameter property = element.getElementParameterFromField(EParameterFieldType.PROPERTY_TYPE, section);
        if (property != null) {
            Map<String, IElementParameter> childParameters = property.getChildParameters();
            if (childParameters != null) {
                IElementParameter elementParameter = childParameters.get(EParameterName.PROPERTY_TYPE.getName());
                return elementParameter != null && EmfComponent.REPOSITORY.equals(elementParameter.getValue());
            }
        }
        return false;
    }

    protected void addUpdateParameterIfNotExist(List<ElementParameter> parameters) {
        boolean isExist = false;
        for (ElementParameter elementParameter : parameters) {
            if (EParameterName.UPDATE_COMPONENTS.getName().equals(elementParameter.getName())) {
                isExist = true;
                break;
            }
        }
        if (!isExist) {
            parameters.add(getUpdateParameter());
        }
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
        if (validationMessage == null) {
            if (validationStatus == Result.OK) {
                checker.updateStatus(IStatus.OK, null);
                return;
            } else if (validationStatus == Result.ERROR) {
                validationMessage = Messages.getString("DynamicComposite.defaultErrorMessage"); //$NON-NLS-1$
            } else {
                // skip every empty messages
                return;
            }
        }
        String message = validationMessage;
        switch (validationStatus) {
        case WARNING:
            checker.updateStatus(IStatus.WARNING, null);
            DisplayUtils.getDisplay().syncExec(new Runnable() {

                @Override
                public void run() {
                    MessageDialog.openWarning(getShell(), elem.getElementName(), message);
                }
            });
            break;
        case ERROR:
            checker.updateStatus(IStatus.ERROR, null);
            DisplayUtils.getDisplay().syncExec(new Runnable() {

                @Override
                public void run() {
                    MessageDialog.openError(getShell(), elem.getElementName(), message);
                }
            });
            break;
        default:
            checker.updateStatus(IStatus.OK, null);
            DisplayUtils.getDisplay().syncExec(new Runnable() {

                @Override
                public void run() {
                    MessageDialog.openInformation(getShell(), "", message); //$NON-NLS-1$
                }
            });
            break;
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        String propertyName = event.getPropertyName();
        if (IElementParameterEventProperties.EVENT_PROPERTY_VALUE_CHANGED.equals(propertyName)) {
            reset(true);
        } else if (IElementParameterEventProperties.EVENT_VALIDATE_RESULT_UPDATE.equals(propertyName)) {
            Object newValue = event.getNewValue();
            if (newValue instanceof ValidationResult) {
                updateValidationStatus((ValidationResult) newValue);
            }
        } else if (IElementParameterEventProperties.EVENT_SHOW_DIALOG.equals(propertyName)) {
            Object newValue = event.getNewValue();
            if (newValue instanceof Form) {
                GenericDialog genericDialog = new GenericDialog(getShell(), (Form) newValue);
                genericDialog.setConnectionItem(connectionItem);
                genericDialog.open();
            }
        } else if (IContextEventProperties.EVENT_PROPERTY_EXPORT_CONTEXT.equals(propertyName)) {
            resetComponentProperties();
        } else if (IContextEventProperties.EVENT_PROPERTY_REFRESH_UI.equals(propertyName)) {
            Object newValue = event.getNewValue();
            if (newValue instanceof ComponentProperties) {
                ComponentProperties newComponentProperties = (ComponentProperties) newValue;
                form.getProperties().copyValuesFrom(newComponentProperties);
                reset(true);
            }
        }
    }

    public IChecker getChecker() {
        return this.checker;
    }

    private void reset(boolean refresh) {
        resetParameters(false);
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
    public void refresh() {
        if (element instanceof FakeElement) {
            DisplayUtils.getDisplay().syncExec(new Runnable() {

                @Override
                public void run() {
                    operationInThread();
                }
            });
        } else {
            super.refresh();
        }
    }

    @Override
    public int getMinHeight() {
        if (minHeight < 200) {
            return 200;
        } else if (minHeight > 700) {
            return 700;
        }
        return minHeight;
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

    @Override
    protected boolean isShouldDisParameter(IElementParameter curParam) {
        if (EParameterFieldType.PROPERTY_TYPE.equals(curParam.getFieldType())) {
            IElementParameter compRefParameter = elem.getElementParameterFromField(EParameterFieldType.COMPONENT_REFERENCE);
            if (compRefParameter != null) {
                GenericElementParameter gParam = (GenericElementParameter) compRefParameter;
                ComponentReferenceProperties props = (ComponentReferenceProperties) gParam.getWidget().getContent();
                return props.getReference() == null;
            }
        }
        return true;
    }

    /**
     * Sets the wizardPropertyChangeListener.
     *
     * @param wizardPropertyChangeListener the wizardPropertyChangeListener to set
     */
    public void setWizardPropertyChangeListener(PropertyChangeListener wizardPropertyChangeListener) {
        this.wizardPropertyChangeListener = wizardPropertyChangeListener;
    }

    public Form getForm() {
        return this.form;
    }

    @Override
    protected synchronized void operationInThread() {
        super.operationInThread();
    }


}
