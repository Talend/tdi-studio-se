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
package org.talend.designer.business.diagram.custom.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.IWorkbench;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.properties.BusinessProcessItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.business.diagram.i18n.Messages;
import org.talend.metadata.managment.ui.wizard.RepositoryWizard;
import org.talend.repository.model.IProxyRepositoryFactory;

public class SimpleBusinessCreationWizard extends RepositoryWizard {

    private Property property;

    private IPath destinationPath;

    private BusinessProcessItem businessProcessItem;

    private SimpleBusinessCreationWizardPage mainPage;

    public SimpleBusinessCreationWizard(IWorkbench workbench, IPath destinationPath) {

        super(workbench, true);
        this.destinationPath = destinationPath;

        property = PropertiesFactory.eINSTANCE.createProperty();
        property.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
        property.setVersion(VersionUtils.DEFAULT_VERSION);
        property.setStatusCode(""); //$NON-NLS-1$

        businessProcessItem = PropertiesFactory.eINSTANCE.createBusinessProcessItem();
        businessProcessItem.setProperty(property);

        setWindowTitle(Messages.getString("SimpleBusinessCreationWizard.NewBusinessModel")); //$NON-NLS-1$
        setDefaultPageImageDescriptor(ImageProvider.getImageDesc(ECoreImage.BUSINESS_PROCESS_WIZ));
    }

    public void addPages() {
        mainPage = new SimpleBusinessCreationWizardPage(property, destinationPath);
        addPage(mainPage);
    }

    public boolean performFinish() {
        IProxyRepositoryFactory repositoryFactory = ProxyRepositoryFactory.getInstance();
        String nextId = repositoryFactory.getNextId();

        property.setId(nextId);
      //changed by hqzhang for TDI-19527, label=displayName
        property.setLabel(property.getDisplayName());

        DiagramResourceManager diagramResourceManager = new DiagramResourceManager(getWorkbench().getActiveWorkbenchWindow()
                .getActivePage(), new NullProgressMonitor());
        IFile file = diagramResourceManager.createDiagramFile();
        diagramResourceManager.updateFromResource(businessProcessItem, file);

        try {
            repositoryFactory.create(businessProcessItem, mainPage.getDestinationPath());
        } catch (PersistenceException e1) {
            throw new IllegalStateException(e1);
        }

        diagramResourceManager.openEditor(businessProcessItem, file, false);

        return true;
    }
}
