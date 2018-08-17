package org.talend.repository.resource.ui.wizards;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.metadata.managment.ui.wizard.PropertiesWizard;
import org.talend.repository.resource.i18n.Messages;

public class EditRouteResourcePropertiesWizard extends PropertiesWizard{

	public EditRouteResourcePropertiesWizard(
			IRepositoryViewObject repositoryViewObject, IPath path,
			boolean useLastVersion) {
		super(repositoryViewObject, path, useLastVersion);
	}
	
    @Override
    public void addPages() {
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

}
