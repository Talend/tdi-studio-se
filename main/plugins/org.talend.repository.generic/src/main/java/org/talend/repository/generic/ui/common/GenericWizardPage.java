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
package org.talend.repository.generic.ui.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Composite;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.ComponentService;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.ui.check.ICheckListener;
import org.talend.core.ui.check.IChecker;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.designer.core.generic.model.GenericElementParameter;
import org.talend.designer.core.generic.utils.ComponentsUtils;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.metadata.managment.ui.wizard.AbstractNamedWizardPage;
import org.talend.metadata.managment.ui.wizard.context.MetadataContextPropertyValueEvaluator;
import org.talend.repository.generic.handler.IContextHandler;
import org.talend.repository.generic.ui.context.ContextComposite;
import org.talend.repository.generic.ui.context.handler.GenericContextHandler;

/**
 *
 * created by ycbai on 2015年9月29日 Detailled comment
 *
 */
public abstract class GenericWizardPage extends AbstractNamedWizardPage {

    protected Element baseElement;

    protected ConnectionItem connectionItem;

    protected String[] existingNames;

    protected boolean creation;

    protected boolean isRepositoryObjectEditable;

    protected Form form;

    protected ComponentService compService;

    protected boolean isSupportContext = true; // FIXME: will get the value from component contract later.

    protected boolean addContextFields;

    private IContextHandler contextHandler;

    protected IStatus genericStatus;


    public GenericWizardPage(ConnectionItem connectionItem, boolean isRepositoryObjectEditable, String[] existingNames,
            boolean creation, Form form, ComponentService compService, boolean addContextFields) {
        this(connectionItem, isRepositoryObjectEditable);
        this.existingNames = existingNames;
        this.creation = creation;
        this.form = form;
        this.compService = compService;
        this.addContextFields = addContextFields;
        genericStatus = createOkStatus();
        setupPropertyValueEvaluator();
    }

    public GenericWizardPage(ConnectionItem connectionItem, boolean isRepositoryObjectEditable) {
        super("GenericWizardPage"); //$NON-NLS-1$
        this.connectionItem = connectionItem;
        this.isRepositoryObjectEditable = isRepositoryObjectEditable;
    }

    protected void setupPropertyValueEvaluator() {
        if (form != null && getConnection() != null) {
            form.getProperties().setValueEvaluator(new MetadataContextPropertyValueEvaluator(getConnection()));
        }
    }

    private boolean callBefore() {
        if (form.isCallBeforeFormPresent()) {
            try {
                compService.beforeFormPresent(form.getName(), (ComponentProperties) form.getProperties());
                return true;
            } catch (Throwable e) {
                ExceptionHandler.process(e);
            }
        }
        return false;
    }

    private boolean hasValidateWidget() {
        Collection<Widget> widgets = form.getWidgets();
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
        ContextComposite contextComp = new ContextComposite(parentComposite, connectionItem, !isRepositoryObjectEditable,
                contextHandler);
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

    public Form getForm() {
        return this.form;
    }

    @Override
    protected IStatus[] getStatuses() {
        return new IStatus[] { nameStatus, genericStatus };
    }


    protected void addCheckListener(IChecker checker) {
        ICheckListener checkListener = new ICheckListener() {

            @Override
            public void checkPerformed(IChecker source) {
                String status = source.getStatus();
                if (source.isStatusOnError()) {
                    genericStatus = createStatus(IStatus.ERROR, status);
                } else {
                    genericStatus = createOkStatus();
                    if (status != null) {
                        genericStatus = createStatus(source.getStatusLevel(), status);
                    }
                }
                updatePageStatus();
            }
        };
        checker.setListener(checkListener);
    }

    public List<? extends IElementParameter> getParameters() {
        return baseElement.getElementParameters();
    }

    public List<IElementParameter> getContextParameters() {
        List<IElementParameter> contextParameters = new ArrayList<>();
        for (IElementParameter parameter : baseElement.getElementParameters()) {
            if (parameter instanceof GenericElementParameter) {
                GenericElementParameter genericElementParameter = (GenericElementParameter) parameter;
                if (genericElementParameter.isSupportContext()) {
                    contextParameters.add(parameter);
                }
                List<ElementParameter> relatedParameters = ComponentsUtils.getRelatedParameters(genericElementParameter);
                for (ElementParameter relatedParameter : relatedParameters) {
                    if (relatedParameter instanceof GenericElementParameter
                            && ((GenericElementParameter) relatedParameter).isSupportContext()) {
                        contextParameters.add(relatedParameter);
                    }
                }
            }
        }
        return contextParameters;
    }

    public Connection getConnection() {
        return connectionItem.getConnection();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.talend.metadata.managment.ui.wizard.AbstractNamedWizardPage#getRepositoryObjectType()
     */
    @Override
    public ERepositoryObjectType getRepositoryObjectType() {
        return ERepositoryObjectType.getType(connectionItem.getTypeName());
    }

    /* (non-Javadoc)
     * @see org.talend.metadata.managment.ui.wizard.AbstractNamedWizardPage#getProperty()
     */
    @Override
    public Property getProperty() {
        return connectionItem.getProperty();
    }

    public boolean nameModifiedByUser() {
        return this.nameModifiedByUser;
    }
}
