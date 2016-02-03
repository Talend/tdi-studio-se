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
package org.talend.component.ui.wizard.ui.common;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.component.core.model.GenericElementParameter;
import org.talend.component.ui.model.genericMetadata.GenericConnection;
import org.talend.component.ui.wizard.handler.IContextHandler;
import org.talend.component.ui.wizard.ui.context.ContextComposite;
import org.talend.component.ui.wizard.ui.context.handler.GenericContextHandler;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.properties.presentation.Widget;
import org.talend.components.api.service.ComponentService;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.ui.check.ICheckListener;
import org.talend.core.ui.check.IChecker;
import org.talend.core.ui.metadata.generic.IGenericMetadataWizardPage;
import org.talend.designer.core.model.components.ElementParameter;

/**
 * 
 * created by ycbai on 2015年9月29日 Detailled comment
 *
 */
public abstract class GenericWizardPage extends WizardPage implements IGenericMetadataWizardPage {

    protected List<ElementParameter> parameters = new ArrayList<>();

    protected ConnectionItem connectionItem;

    protected String[] existingNames;

    protected boolean creation;

    protected boolean isRepositoryObjectEditable;

    protected Form form;

    protected ComponentService compService;

    protected boolean isSupportContext = true; // FIXME: will get the value from component contract later.

    protected boolean addContextFields;

    private IContextHandler contextHandler;

    public GenericWizardPage(ConnectionItem connectionItem, boolean isRepositoryObjectEditable, String[] existingNames,
            boolean creation, Form form, ComponentService compService, boolean addContextFields) {
        this(connectionItem, isRepositoryObjectEditable);
        this.existingNames = existingNames;
        this.creation = creation;
        this.form = form;
        this.compService = compService;
        this.addContextFields = addContextFields;
    }

    public GenericWizardPage(ConnectionItem connectionItem, boolean isRepositoryObjectEditable) {
        super("GenericWizardPage"); //$NON-NLS-1$
        this.connectionItem = connectionItem;
        this.isRepositoryObjectEditable = isRepositoryObjectEditable;
    }

    private boolean callBefore() {
        if (form.isCallBeforeFormPresent()) {
            try {
                compService.beforeFormPresent(form.getName(), form.getComponentProperties());
                return true;
            } catch (Throwable e) {
                ExceptionHandler.process(e);
            }
        }
        return false;
    }

    private boolean hasValidateWidget() {
        List<Widget> widgets = form.getWidgets();
        for (Widget widget : widgets) {
            if (widget.isCallValidate()) {
                return true;
            }
        }
        return false;
    }

    protected ContextComposite addContextFields(Composite parentComposite) {
        contextHandler = new GenericContextHandler();
        contextHandler.setParameters(getContextParameters());
        ContextComposite contextComp = new ContextComposite(parentComposite, connectionItem, contextHandler);
        return contextComp;
    }

    protected void updateContextFields() {
        if (contextHandler != null) {
            contextHandler.setParameters(getContextParameters());
        }
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            callBefore();
            setPageComplete(!creation || !hasValidateWidget());
        }
    }

    @Override
    public Form getForm() {
        return this.form;
    }

    protected void addCheckListener(IChecker checker) {
        ICheckListener<IChecker> checkListener = new ICheckListener<IChecker>() {

            @Override
            public void checkPerformed(IChecker source) {
                String status = source.getStatus();
                if (source.isStatusOnError()) {
                    setErrorMessage(status);
                } else {
                    setErrorMessage(null);
                    if (status != null) {
                        setMessage(status, source.getStatusLevel());
                    }
                }
                updatePageStatus();
            }
        };
        checker.setListener(checkListener);
    }

    protected void updatePageStatus() {
        setPageComplete(getErrorMessage() == null);
    }

    public List<ElementParameter> getParameters() {
        return this.parameters;
    }

    public List<ElementParameter> getContextParameters() {
        List<ElementParameter> contextParameters = new ArrayList<>();
        for (ElementParameter parameter : parameters) {
            if (parameter instanceof GenericElementParameter) {
                GenericElementParameter genericElementParameter = (GenericElementParameter) parameter;
                if (genericElementParameter.isSupportContext() && genericElementParameter.isShow(parameters)) {
                    contextParameters.add(parameter);
                }
            }
        }
        return contextParameters;
    }

    public GenericConnection getConnection() {
        return (GenericConnection) connectionItem.getConnection();
    }

}
