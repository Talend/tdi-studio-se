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
package org.talend.designer.core.ui.wizards;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.part.EditorPart;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.CorePlugin;
import org.talend.core.model.properties.ByteArray;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.SQLPatternItem;
import org.talend.core.repository.ui.editor.RepositoryEditorInput;
import org.talend.designer.core.DesignerPlugin;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryService;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.wizards.sqlpattern.NewSqlpatternWizardPage;

/**
 * Wizard for the creation of a new project. <br/>
 *
 * $Id: SaveAsSQLPatternWizard.java 46332 2010-08-05 06:48:56Z cli $
 *
 */
public class SaveAsSQLPatternWizard extends Wizard {

    /** Main page. */
    private NewSqlpatternWizardPage mainPage;

    /** Created project. */
    private SQLPatternItem sqlpatternItem;

    private Property property;

    private IPath path;

    private IProxyRepositoryFactory repositoryFactory;

    private RepositoryEditorInput repositoryEditorInput;

    private SQLPatternItem oldSqlpatternItem;

    private Property oldProperty;

    private boolean isUpdate;

    public SaveAsSQLPatternWizard(EditorPart editorPart) {

        this.repositoryEditorInput = (RepositoryEditorInput) editorPart.getEditorInput();

        RepositoryNode repositoryNode = repositoryEditorInput.getRepositoryNode();
        // see: RepositoryEditorInput.setRepositoryNode(IRepositoryNode repositoryNode)
        if (repositoryNode == null) {
            repositoryNode = (RepositoryNode) CorePlugin.getDefault().getRepositoryService().getRepositoryNode(
                    repositoryEditorInput.getItem().getProperty().getId(), false);
        }

        IRepositoryService service = DesignerPlugin.getDefault().getRepositoryService();
        this.path = service.getRepositoryPath((RepositoryNode) repositoryNode);

        oldSqlpatternItem = (SQLPatternItem) repositoryEditorInput.getItem();
        oldProperty = oldSqlpatternItem.getProperty();

        this.property = PropertiesFactory.eINSTANCE.createProperty();

        assginVlaues(this.property, oldProperty);

        sqlpatternItem = PropertiesFactory.eINSTANCE.createSQLPatternItem();

        sqlpatternItem.setProperty(property);

        // set ELT Name
        sqlpatternItem.setEltName(((SQLPatternItem) repositoryEditorInput.getItem()).getEltName());

        repositoryFactory = service.getProxyRepositoryFactory();

        setDefaultPageImageDescriptor(ImageProvider.getImageDesc(ECoreImage.METADATA_SQLPATTERN_WIZ));
    }

    public void addPages() {
        mainPage = new NewSqlpatternWizardPage(property, path);
        mainPage.initializeSaveAs(oldProperty.getLabel(), oldProperty.getVersion(), true);

        // overwrite it.
        mainPage.setTitle("Save As");
        mainPage.setDescription("Save as another new SQLTemplate.");

        addPage(mainPage);
        setWindowTitle("Save As");
    }

    public boolean performFinish() {

        boolean ok = false;
        try {

            isUpdate = isUpdate();

            if (isUpdate) {
                assginVlaues(oldProperty, property);

                repositoryFactory.save(oldSqlpatternItem);

                // assign value
                sqlpatternItem = oldSqlpatternItem;

            } else {
                property.setId(repositoryFactory.getNextId());

                // copy the byte[] content, the new routineItem get the old saved content, it is not the newest.
                SQLPatternItem oldItem = (SQLPatternItem) repositoryEditorInput.getItem();
                ByteArray byteArray = PropertiesFactory.eINSTANCE.createByteArray();
                byteArray.setInnerContent(oldItem.getContent().getInnerContent());
                sqlpatternItem.setContent(byteArray);

                // don't need to add depended routines.
                repositoryFactory.create(sqlpatternItem, mainPage.getDestinationPath());
            }

            ok = true;

        } catch (Exception e) {
            MessageDialog.openError(getShell(), "Error", "SQLTemplate could not be saved" + " : " + e.getMessage());
            ExceptionHandler.process(e);
        }

        return ok;
    }

    public SQLPatternItem getSQLPatternItem() {
        return this.sqlpatternItem;
    }

    // left = right
    private void assginVlaues(Property leftProperty, Property rightProperty) {
        // 6 fields, don't contains the "locker" and "path". and author , they are the same.
        leftProperty.setLabel(rightProperty.getLabel());
        leftProperty.setPurpose(rightProperty.getPurpose());
        leftProperty.setDescription(rightProperty.getDescription());
        // same author as old one.
        leftProperty.setAuthor(rightProperty.getAuthor());
        leftProperty.setVersion(rightProperty.getVersion());
        leftProperty.setStatusCode(rightProperty.getStatusCode());
    }

    // if name is different, it will create a new job, if name is the same, means to update the job(version or
    // description...)
    private boolean isUpdate() {
        if (oldProperty.getLabel().trim().equalsIgnoreCase(property.getLabel().trim())) {
            return true;
        } else {
            return false;
        }
    }
}
