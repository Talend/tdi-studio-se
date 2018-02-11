// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.swt.dialogs.IConfigModuleDialog;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ILibraryManagerUIService;
import org.talend.core.model.properties.RoutineItem;
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

    private RoutineItem routineItem;

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
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    EList routines = null;

    @Override
    public void createControl(Composite parent) {
        initializeDialogUnits(parent);

        Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
        composite.setLayout(new GridLayout(3, false));
        composite.setFont(parent.getFont());

        libField = new EditJavaRoutineExternalJarField(Messages.getString("ImportExternalJarPage.fileField.label"), //$NON-NLS-1$
                composite, isReadOnly());

        routineItem = getSelectedRoutine();
        routines = routineItem.getImports();
        libField.setInput(routines);
        Button button = new Button(composite, getMessageType());
        button.setText(Messages.getString("ConfigExternalJarPage.reloadLibrary")); //$NON-NLS-1$
        button.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {

            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                for (int i = 0; i < routines.size(); i++) {
                    String value = ((IMPORTType) routines.get(i)).getUrlPath();
                    File file = new File(value);
                    if (file.exists()) {
                        try {
                            CorePlugin.getDefault().getLibrariesService().deployLibrary(file.toURL());
                        } catch (MalformedURLException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        } catch (IOException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                    }

                }

            }

        });
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
                    CorePlugin.getDefault().getProxyRepositoryFactory().save(getSelectedRoutine());
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
                CorePlugin.getDefault().getRunProcessService().updateLibraries(routineItem);
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
                ILibraryManagerUIService libUiService = (ILibraryManagerUIService) GlobalServiceRegister.getDefault().getService(
                        ILibraryManagerUIService.class);
                IConfigModuleDialog dialog = libUiService.getConfigModuleDialog(getShell(), null);
                if (dialog.open() == IDialogConstants.OK_ID) {
                    IMPORTType type = ComponentFactory.eINSTANCE.createIMPORTType();
                    RoutineItem routine = getSelectedRoutine();
                    type.setNAME(routine.getProperty().getLabel());
                    type.setMODULE(dialog.getModuleName());
                    type.setMVN(dialog.getMavenURI());
                    type.setREQUIRED(true);
                    importTypes.add(type);
                }
                ConfigExternalJarPage.this.setPageComplete(true);
            }
            ConfigExternalJarPage.this.setPageComplete(true);
            return importTypes;
        }
    }

}
