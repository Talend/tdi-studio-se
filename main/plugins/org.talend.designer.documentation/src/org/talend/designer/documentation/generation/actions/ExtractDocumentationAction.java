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
package org.talend.designer.documentation.generation.actions;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.swt.dialogs.ProgressDialog;
import org.talend.core.model.properties.ByteArray;
import org.talend.core.model.properties.DocumentationItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.LinkDocumentationItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.metadata.managment.ui.wizard.documentation.LinkDocumentationHelper;
import org.talend.metadata.managment.ui.wizard.documentation.LinkUtils;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.BinRepositoryNode;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.ui.actions.AContextualAction;

/**
 * Saves the content of a document on the local file system. <br/>
 *
 * $Id: ExtractDocumentationAction.java 77219 2012-01-24 01:14:15Z mhirt $
 *
 */
public class ExtractDocumentationAction extends AContextualAction {

    /**
     * Constructs a new ExtractDocumentationAction.
     */
    public ExtractDocumentationAction() {
        super();

        setText(Messages.getString("ExtractDocumentationAction.text.saveAs")); //$NON-NLS-1$
        setToolTipText(Messages.getString("ExtractDocumentationAction.toolTipText.extractDoctoFileSys")); //$NON-NLS-1$
        setImageDescriptor(ImageProvider.getImageDesc(ECoreImage.DOCUMENTATION_ICON));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.actions.ITreeContextualAction#init(org.eclipse.jface.viewers.TreeViewer,
     * org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        boolean canWork = !selection.isEmpty() && selection.size() == 1;
        RepositoryNode node = (RepositoryNode) selection.getFirstElement();
        if (canWork) {
            canWork = node.getType() == ENodeType.REPOSITORY_ELEMENT
                    && node.getObject().getRepositoryObjectType() == ERepositoryObjectType.DOCUMENTATION;
        }
        RepositoryNode parent = null;
        if (node != null) {
            parent = node.getParent();
        }
        if (canWork && parent != null && parent instanceof BinRepositoryNode) {
            canWork = false;
        }
        setEnabled(canWork);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.Action#run()
     */
    protected void doRun() {
        RepositoryNode node = (RepositoryNode) ((IStructuredSelection) getSelection()).getFirstElement();

        final Item item = node.getObject().getProperty().getItem();
        if (item == null) {
            return;
        }
        String initialFileName = null;
        String initialExtension = null;
        if (item instanceof DocumentationItem) {
            DocumentationItem documentationItem = (DocumentationItem) item;
            initialFileName = documentationItem.getName();
            if (documentationItem.getExtension() != null) {
                initialExtension = documentationItem.getExtension();
            }
        } else if (item instanceof LinkDocumentationItem) { // link documenation
            LinkDocumentationItem linkDocItem = (LinkDocumentationItem) item;

            if (!LinkUtils.validateLink(linkDocItem.getLink())) {
                MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages
                        .getString("ExtractDocumentationAction.fileErrorTitle"), //$NON-NLS-1$
                        Messages.getString("ExtractDocumentationAction.fileErrorMessages")); //$NON-NLS-1$
                return;
            }

            initialFileName = linkDocItem.getName();
            if (linkDocItem.getExtension() != null) {
                initialExtension = linkDocItem.getExtension();
            }
        }
        if (initialFileName != null) {
            FileDialog fileDlg = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);

            if (initialExtension != null) {
                initialFileName = initialFileName + LinkUtils.DOT + initialExtension; //$NON-NLS-1$
                fileDlg.setFilterExtensions(new String[] { "*." + initialExtension, "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
            }
            fileDlg.setFileName(initialFileName);
            String filename = fileDlg.open();
            if (filename != null) {
                final File file = new File(filename);

                ProgressDialog progressDialog = new ProgressDialog(Display.getCurrent().getActiveShell()) {

                    @Override
                    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                        try {
                            if (item instanceof DocumentationItem) {
                                DocumentationItem documentationItem = (DocumentationItem) item;
                                documentationItem.getContent().setInnerContentToFile(file);
                            } else if (item instanceof LinkDocumentationItem) { // link documenation
                                LinkDocumentationItem linkDocItem = (LinkDocumentationItem) item;
                                ByteArray byteArray = LinkDocumentationHelper.getLinkItemContent(linkDocItem);
                                if (byteArray != null) {
                                    byteArray.setInnerContentToFile(file);
                                }
                            }

                        } catch (IOException ioe) {
                            MessageBoxExceptionHandler.process(ioe);
                        }
                    }
                };
                try {
                    progressDialog.executeProcess();
                } catch (InvocationTargetException e) {
                    ExceptionHandler.process(e);
                } catch (InterruptedException e) {
                    // Nothing to do
                }

            }
        }
    }

}
