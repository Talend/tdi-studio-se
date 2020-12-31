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
package org.talend.repository.ui.wizards.routines;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.exception.RuntimeExceptionHandler;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.general.ILibrariesService;
import org.talend.core.model.properties.ByteArray;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.codegen.ITalendSynchronizer;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * Wizard for the creation of a new project. <br/>
 *
 * $Id: NewProcessWizard.java 914 2006-12-08 08:28:53 +0000 (ven., 08 déc. 2006) bqian $
 *
 */
public class NewRoutineWizard extends Wizard {

    /** Main page. */
    private NewRoutineWizardPage mainPage;

    /** Created project. */
    protected RoutineItem routineItem;

    private Property property;

    private IPath path;

    /**
     * Constructs a new NewProjectWizard.
     *
     * @param author Project author.
     * @param server
     * @param password
     */
    public NewRoutineWizard(IPath path) {
        super();
        this.path = path;
        property = PropertiesFactory.eINSTANCE.createProperty();
        property.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
        property.setVersion(VersionUtils.DEFAULT_VERSION);
        property.setStatusCode(""); //$NON-NLS-1$
        addAdditionalProperties(property);

        routineItem = PropertiesFactory.eINSTANCE.createRoutineItem();

        routineItem.setProperty(property);

        ILibrariesService service = CorePlugin.getDefault().getLibrariesService();
        URL url = service.getRoutineTemplate();
        ByteArray byteArray = PropertiesFactory.eINSTANCE.createByteArray();
        InputStream stream = null;
        try {
            stream = url.openStream();
            byte[] innerContent = new byte[stream.available()];
            stream.read(innerContent);
            stream.close();
            byteArray.setInnerContent(innerContent);
        } catch (IOException e) {
            RuntimeExceptionHandler.process(e);
        }

        routineItem.setContent(byteArray);
    }

    protected void addAdditionalProperties(Property property) {
    }

    /**
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        mainPage = new NewRoutineWizardPage(property, path);
        addPage(mainPage);
        setWindowTitle(Messages.getString("NewRoutineWizard.title")); //$NON-NLS-1$
        setDefaultPageImageDescriptor(ImageProvider.getImageDesc(ECoreImage.ROUTINE_WIZ));
    }

    /**
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        IProxyRepositoryFactory repositoryFactory = ProxyRepositoryFactory.getInstance();
        try {
            updateRoutineContent();
            property.setId(repositoryFactory.getNextId());
            //changed by hqzhang for TDI-19527, label=displayName
            property.setLabel(property.getDisplayName());
            repositoryFactory.create(routineItem, mainPage.getDestinationPath());
        } catch (PersistenceException e) {
            MessageDialog.openError(getShell(), Messages.getString("NewProcessWizard.failureTitle"), ""); //$NON-NLS-1$ //$NON-NLS-2$
            ExceptionHandler.process(e);
        }

        return routineItem != null;
    }

    /**
     * Getter for project.
     *
     * @return the project
     */
    public RoutineItem getRoutine() {
        return this.routineItem;
    }

    private void updateRoutineContent() {
        if (routineItem == null) {
            return;
        }
        ByteArray content = routineItem.getContent();
        if (content != null) {
            String routineContent = new String(content.getInnerContent());
            String label = routineItem.getProperty().getLabel();
            if (routineContent != null && !label.equals(ITalendSynchronizer.TEMPLATE)) {
                routineContent = routineContent.replaceAll(ITalendSynchronizer.TEMPLATE, label);
                content.setInnerContent(routineContent.getBytes());
            }
        }
    }
}
