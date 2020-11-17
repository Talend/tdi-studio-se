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
package org.talend.repository.ui.wizards.ConfigExternalLib;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.swt.dialogs.IConfigModuleDialog;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ILibraryManagerUIService;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.designer.core.model.utils.emf.component.ComponentFactory;
import org.talend.designer.core.model.utils.emf.component.IMPORTType;
import org.talend.repository.i18n.Messages;

/**
 * Page of the Job Scripts Export Wizard. <br/>
 *
 * @referto WizardArchiveFileResourceExportPage1 $Id: JobScriptsExportWizardPage.java 1 2006-12-13 ä¸‹å�ˆ03:09:07 bqian
 *
 */
public class ConfigExternalJarPage extends ConfigExternalLibPage {

    private Map<IMPORTType, File> newJarFiles = new HashMap<IMPORTType, File>();

    private LibraryField libField;

    /**
     * ConfigExternalJarPage.
     *
     * @param pageName
     * @param title
     * @param titleImage
     */
    public ConfigExternalJarPage(IStructuredSelection selection) {
        super(Messages.getString("ImportExternalJarPage.pageTitle"), selection); //$NON-NLS-1$
        String message = null;
        if (isReadOnly()) {
            message = Messages.getString("ImportExternalJarPage.pageMessagelock");//$NON-NLS-1$
        } else {
            message = Messages.getString("ImportExternalJarPage.pageMessage");//$NON-NLS-1$
        }
        this.setMessage(message);

        /**
         * Call this to make sure librariesListener is registed.<br>
         * Please refer to MavenPomSynchronizer#addChangeLibrariesListener
         */
        // FIXME
        CorePlugin.getDefault().getRunProcessService().getTalendCodeJavaProject(ERepositoryObjectType.ROUTINES);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        initializeDialogUnits(parent);

        Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
        composite.setLayout(new GridLayout(3, false));
        composite.setFont(parent.getFont());

        libField = new EditJavaRoutineExternalJarField(Messages.getString("ImportExternalJarPage.fileField.label"), //$NON-NLS-1$
                composite, isReadOnly());

        libField.setInput(getImports());

        setErrorMessage(null); // should not initially have error message

        setControl(composite);

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.wizards.importExternalLib.ImportExternalLibPage#finish()
     */
    @Override
    public boolean finish() {
        BusyIndicator.showWhile(Display.getDefault(), new Runnable() {

            @Override
            public void run() {
                try {
                    CorePlugin.getDefault().getProxyRepositoryFactory().save(getSelectedItem());
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
                CorePlugin.getDefault().getRunProcessService().updateLibraries(getSelectedItem());
                // TODO also update routinesjar project
            }
        });

        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.wizards.ConfigExternalLib.ConfigExternalLibPage#cancel()
     */
    @Override
    public void cancel() {
        libField.revert();
    }

    /**
     * Subclass of the LibraryField. <br/>
     *
     * $Id: ImportExternalJarPage.java Mar 20, 20074:15:28 PM bqian $
     *
     */
    class EditJavaRoutineExternalJarField extends LibraryField {

        /**
         *
         * @param name
         * @param parent
         */
        public EditJavaRoutineExternalJarField(String name, Composite parent, boolean isReadOnly) {
            super(name, parent, isReadOnly);
        }

        /*
         * (non-Javadoc)
         *
         * @see org.talend.repository.ui.wizards.importExternalLib.TableField#afterDeleteSelection(java.util.List)
         */
        @Override
        protected void afterDeleteSelection(List list) {
            for (int i = 0; i < list.size(); i++) {
                newJarFiles.remove(list.get(i));
            }
        }

        /*
         * (non-Javadoc)
         *
         * @see org.talend.repository.ui.wizards.importExternalLib.LibraryField#getNewInputObject()
         */
        @Override
        protected List<IMPORTType> getNewInputObject() {
            List<IMPORTType> importTypes = new ArrayList<IMPORTType>();
            if (GlobalServiceRegister.getDefault().isServiceRegistered(ILibraryManagerUIService.class)) {
                ILibraryManagerUIService libUiService = GlobalServiceRegister.getDefault()
                        .getService(ILibraryManagerUIService.class);
                IConfigModuleDialog dialog = libUiService.getConfigModuleDialog(getShell(), null);
                if (dialog.open() == IDialogConstants.OK_ID) {
                    IMPORTType type = ComponentFactory.eINSTANCE.createIMPORTType();
                    type.setNAME(getSelectedItem().getProperty().getLabel());
                    type.setMODULE(dialog.getModuleName());
                    type.setMVN(dialog.getMavenURI());
                    type.setREQUIRED(true);
                    importTypes.add(type);
                }
            }
            ConfigExternalJarPage.this.setPageComplete(true);
            return importTypes;
        }
    }

}
