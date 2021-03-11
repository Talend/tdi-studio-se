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
package org.talend.repository.resource.ui.wizards;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.service.IResourcesDependenciesService;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.metadata.managment.ui.wizard.PropertiesWizard;
import org.talend.repository.resource.i18n.Messages;

public class EditRouteResourcePropertiesWizard extends PropertiesWizard{

    private String originalLabel;

    private String originalVersion;

	public EditRouteResourcePropertiesWizard(
			IRepositoryViewObject repositoryViewObject, IPath path,
			boolean useLastVersion) {
		super(repositoryViewObject, path, useLastVersion);
	}

    @Override
    public void addPages() {
        originalLabel = object.getProperty().getLabel();
        originalVersion = object.getProperty().getVersion();
        mainPage = new NewRouteResourceWizardPage("WizardPage", object.getProperty(), path, isReadOnly(), false, lastVersionFound) {

            @Override
            protected void evaluateTextField() {
            	if(alreadyEditedByUser){
            		nameStatus = createStatus(IStatus.ERROR, Messages.getString("EditRouteResourcePropertiesWizard_itemLocked"));  //$NON-NLS-1$
            		updatePageStatus();
            		return;
            	}
            	super.evaluateTextField();
            }
        };
        addPage(mainPage);
        setWindowTitle(Messages.getString("EditRouteResourcePropertiesWizard_title"));
    }

    @Override
    public boolean performFinish() {
        boolean performFinish = super.performFinish();
        // to refresh context view
        IDesignerCoreService designerCoreService = CoreRuntimePlugin.getInstance().getDesignerCoreService();
        if (designerCoreService != null) {
            designerCoreService.switchToCurContextsView();
        }
        if (!object.getProperty().getLabel().equals(originalLabel)
                || !object.getProperty().getVersion().equals(originalVersion)) {
            if (GlobalServiceRegister.getDefault().isServiceRegistered(IResourcesDependenciesService.class)) {
                IResourcesDependenciesService service = (IResourcesDependenciesService) GlobalServiceRegister.getDefault()
                        .getService(IResourcesDependenciesService.class);
                if (service != null) {
                    service.removeBuildJobCacheForResource(object.getProperty().getId());
                }
            }
        }

        return performFinish;
    }

}
