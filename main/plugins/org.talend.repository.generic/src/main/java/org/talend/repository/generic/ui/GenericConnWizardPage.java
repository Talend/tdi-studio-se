// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.components.api.service.ComponentService;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.daikon.properties.presentation.Form;
import org.talend.designer.core.generic.constants.IElementParameterEventProperties;
import org.talend.designer.core.generic.constants.IGenericConstants;
import org.talend.designer.core.generic.model.GenericElementParameter;
import org.talend.designer.core.model.FakeElement;
import org.talend.repository.generic.ui.common.GenericWizardPage;
import org.talend.repository.generic.ui.context.ContextComposite;
import org.talend.repository.generic.ui.httpsProxy.HttpsProxyComposite;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IProxyRepositoryService;

/**
 *
 * created by ycbai on 2015年9月21日 Detailled comment
 *
 */
public class GenericConnWizardPage extends GenericWizardPage implements PropertyChangeListener {

    private DynamicComposite dynamicComposite;

    public GenericConnWizardPage(ConnectionItem connectionItem, boolean isRepositoryObjectEditable, String[] existingNames,
            boolean creation, Form form, ComponentService compService, boolean addContextFields) {
        super(connectionItem, isRepositoryObjectEditable, existingNames, creation, form, compService, addContextFields);
    }

    /**
     * DOC nrousseau Comment method "getNameParameter".
     */
    private GenericElementParameter getNameParameter() {
        for (IElementParameter parameter : baseElement.getElementParameters()) {
            if (parameter instanceof GenericElementParameter) {
                if (IGenericConstants.NAME_PROPERTY.equals(parameter.getName())) {
                    return (GenericElementParameter) parameter;
                }
            }
        }
        return null;
    }

    @Override
    public void createControl(final Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));
        container.setLayout(new FormLayout());
        setControl(container);

        baseElement = new FakeElement(form.getName());
        baseElement.setReadOnly(!isRepositoryObjectEditable);
        dynamicComposite = new DynamicComposite(container, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS, EComponentCategory.BASIC,
                baseElement, connectionItem, true, container.getBackground(), form, false);
        dynamicComposite.setLayoutData(createMainFormData(addContextFields));
        dynamicComposite.setWizardPropertyChangeListener(this);
        addCheckListener(dynamicComposite.getChecker());
        if (addContextFields) {
            Composite contextParentComp = new Composite(container, SWT.NONE);
            contextParentComp.setLayoutData(createFooterFormData(dynamicComposite));
            contextParentComp.setLayout(new GridLayout());
            HttpsProxyComposite httpsProxyComposite = new HttpsProxyComposite(contextParentComp, SWT.NONE);
            httpsProxyComposite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
            ContextComposite contextComp = addContextFields(contextParentComp);
            contextComp.addPropertyChangeListener(dynamicComposite);
            contextComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        }
    }

    private FormData createMainFormData(boolean addContextSupport) {
        FormData data = new FormData();
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

    private FormData createFooterFormData(Composite topComposite) {
        FormData data = new FormData();
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(100, 0);
        data.top = new FormAttachment(topComposite, 0);
        data.bottom = new FormAttachment(100, 0);
        return data;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            dynamicComposite.resetParameters(false);
            dynamicComposite.refresh();
            dynamicComposite.setMinHeight(dynamicComposite.getMinHeight());
            updateContextFields();
            if (getNameParameter() != null) {
                Job job = new Job("") { //$NON-NLS-1$

                    @Override
                    protected IStatus run(IProgressMonitor monitor) {
                        try {
                            listExistingObjects = loadRepositoryViewObjectList();
                        } catch (PersistenceException e) {
                            return new org.eclipse.core.runtime.Status(IStatus.ERROR, "org.talend.metadata.management.ui", 1, "", //$NON-NLS-1$ //$NON-NLS-2$
                                    e);
                        }
                        retrieveNameFinished = true;
                        // force the refresh of the text field, no matter successfull retrieve of not.
                        Display d = DisplayUtils.getDisplay();
                        if (d != null) {
                            d.syncExec(new Runnable() {

                                @Override
                                public void run() {
                                    evaluateTextField();
                                }

                            });
                        }
                        return Status.OK_STATUS;
                    }
                };
                job.setUser(false);
                job.setPriority(Job.BUILD);
                job.schedule(); // start as soon as possible
            }
        }
    }

    /**
     * Loads the repository view objects that are used to check if the name of job(Opened in the current properties
     * wizard dialog) can be found. Added by Marvin Wang on Feb 22, 2013.
     *
     * @return a list includes the instance of <code>IRepositoryViewObject</code>, which are used to check if a given
     * job name is present in the list.
     * @throws PersistenceException
     */
    protected List<IRepositoryViewObject> loadRepositoryViewObjectList() throws PersistenceException {
        List<IRepositoryViewObject> list = new ArrayList<IRepositoryViewObject>();

        List<IRepositoryViewObject> repViewObjectWithSameType = loadRepViewObjectWithSameType();

        if (repViewObjectWithSameType != null && repViewObjectWithSameType.size() > 0) {
            list.addAll(repViewObjectWithSameType);
        }
        return list;
    }

    /**
     * Loads the repository view objects, which have the same repository type as the current job that are opened in
     * properties wizard dialog. Added by Marvin Wang on Feb 22, 2013.
     *
     * @return
     * @throws PersistenceException
     */
    private List<IRepositoryViewObject> loadRepViewObjectWithSameType() throws PersistenceException {
        List<IRepositoryViewObject> list = new ArrayList<IRepositoryViewObject>();
        ERepositoryObjectType type = ERepositoryObjectType.getType(connectionItem.getTypeName());
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IProxyRepositoryService.class)) {
            IProxyRepositoryService service = (IProxyRepositoryService) GlobalServiceRegister.getDefault()
                    .getService(IProxyRepositoryService.class);

            list = service.getProxyRepositoryFactory().getAll(type, true, false);
        }
        return list;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean isValid(String itemName) {

        IProxyRepositoryFactory repositoryFactory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();
        try {
            return repositoryFactory.isNameAvailable(connectionItem, itemName, listExistingObjects);
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
            return false;
        }
    }

    protected void evaluateTextField() {
        String name = (String) getNameParameter().getValue();
        evaluateName(name);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        String propertyName = event.getPropertyName();
        if (IElementParameterEventProperties.EVENT_PROPERTY_NAME_CHANGED.equals(propertyName)) {
            String newPropertyName = String.valueOf(event.getNewValue());
            updateProperty(newPropertyName);
        }
    }

    private void updateProperty(String newPropertyName) {
        if (connectionItem == null) {
            return;
        }
        this.nameModifiedByUser = true;
        evaluateTextField();
        if (isValid(newPropertyName)) {
            Connection connection = connectionItem.getConnection();
            Property connectionProperty = connectionItem.getProperty();
            String propertyName = StringUtils.trimToNull(newPropertyName);
            connectionProperty.setDisplayName(propertyName);
            connectionProperty.setLabel(propertyName);
            connectionProperty.setLabel(propertyName);
            connection.setName(propertyName);
            connection.setLabel(propertyName);
        }
    }

}
