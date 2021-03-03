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
package org.talend.designer.core.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.services.ISVNProviderService;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.AbstractTalendEditor;
import org.talend.designer.core.ui.editor.ProcessEditorInput;
import org.talend.designer.core.ui.editor.TalendEditor;

/**
 * This class is the main editor, the differents pages in it are: <br/>
 * <b>1)</b> {@link TalendEditor} <br/>
 * <b>2)</b> {@link Text Editor on the generated code} <br/>
 * <br/>
 * This class uses the interface ISelectionListener, it allows to propage the Delete evenement to the designer. <br/>
 *
 * $Id$
 *
 */
public class MultiPageTalendEditor extends AbstractMultiPageTalendEditor {

    public static final String ID = "org.talend.designer.core.ui.MultiPageTalendEditor"; //$NON-NLS-1$

    public MultiPageTalendEditor() {
        this(true);
    }

    protected MultiPageTalendEditor(boolean withDefaultEditor) {
        super();
        if (withDefaultEditor) {
            designerEditor = new TalendEditor();
        }
    }

    /**
     * Getter for designerEditor.
     *
     * @return the designerEditor
     */
    @Override
    public AbstractTalendEditor getDesignerEditor() {
        return this.designerEditor;
    }

    /**
     * Creates the pages of the multi-page editor.
     */
    @Override
    protected void createPages() {
        super.createPages();
    }

    /**
     * The <code>MultiPageEditorExample</code> implementation of this method checks that the input is an instance of
     * <code>IFileEditorInput</code>.
     */
    @Override
    public void init(final IEditorSite site, final IEditorInput editorInput) throws PartInitException {
        if (!(editorInput instanceof IFileEditorInput) && !(editorInput instanceof ProcessEditorInput)) {
            throw new PartInitException(Messages.getString("MultiPageTalendEditor.InvalidInput")); //$NON-NLS-1$
        }
        super.init(site, editorInput);
    }

    /**
     * DOC smallet Comment method "setName".
     *
     * @param label
     */
    @Override
    public void setName() {
        if (getEditorInput() == null) {
            return;
        }
        super.setName();

        IProcess2 process2 = this.getProcess();
        if (process2 == null) {
            return;
        }
        Property property = process2.getProperty();
        if (property == null) {
            return;
        }
        String label = property.getDisplayName();
        String jobVersion = "0.1"; //$NON-NLS-1$
        if (process2 != null) {
            jobVersion = process2.getVersion();
        }
        // if (getActivePage() == 1) {
        ISVNProviderService service = null;
        if (PluginChecker.isSVNProviderPluginLoaded()) {
            service = (ISVNProviderService) GlobalServiceRegister.getDefault().getService(ISVNProviderService.class);
            if (revisionChanged && service.isProjectInSvnMode()) {
                revisionNumStr = service.getCurrentSVNRevision(process2);
                revisionChanged = false;
                if (revisionNumStr != null) {
                    revisionNumStr = ".r" + revisionNumStr; //$NON-NLS-1$
                }
            }
        }
        String title = "MultiPageTalendEditor.Job";//$NON-NLS-1$
        if (process2 != null) {
            Item item = process2.getProperty().getItem();
            if (item instanceof JobletProcessItem) {
                title = "MultiPageTalendEditor.Joblet";//$NON-NLS-1$
            }
        }
        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        boolean allowVerchange = brandingService.getBrandingConfiguration().isAllowChengeVersion();
        if (allowVerchange) {
            if (revisionNumStr != null) {
                setPartName(Messages.getString(title, label, jobVersion) + revisionNumStr);
            } else {
                setPartName(Messages.getString(title, label, jobVersion));
            }
        } else {
            if (revisionNumStr != null) {
                setPartName(Messages.getString(title, label, "") + revisionNumStr);//$NON-NLS-1$
            } else {
                setPartName(Messages.getString(title, label, "")); //$NON-NLS-1$
            }
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.AbstractMultiPageTalendEditor#getEditorId()
     */
    @Override
    public String getEditorId() {
        return ID;
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        if (haveDirtyJoblet()) {
            return;
        }
        // TODO Auto-generated method stub
        super.doSave(monitor);
        this.setName();
    }

}
