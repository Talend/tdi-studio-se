// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ILibraryManagerService;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.properties.RoutineItem;
import org.talend.designer.core.model.utils.emf.component.ComponentFactory;
import org.talend.designer.core.model.utils.emf.component.IMPORTType;
import org.talend.librariesmanager.prefs.LibrariesManagerUtils;
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

        RoutineItem routine = getSelectedRoutine();
        routines = routine.getImports();
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
                for (File file : newJarFiles.values()) {
                    try {
                        CorePlugin.getDefault().getLibrariesService().deployLibrary(file.toURL());
                    } catch (Exception e) {
                        ExceptionHandler.process(e);
                    }
                }
                try {
                    CorePlugin.getDefault().getProxyRepositoryFactory().save(getSelectedRoutine());
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
                CorePlugin.getDefault().getLibrariesService().resetModulesNeeded();
                // TDI-18870
                CorePlugin.getDefault().getRunProcessService().updateLibraries(new HashSet<ModuleNeeded>(), null);
                for (File file : newJarFiles.values()) {
                    try {
                        CorePlugin.getDefault().getLibrariesService().deployProjectLibrary(file);
                    } catch (Exception e) {
                        ExceptionHandler.process(e);
                    }
                }
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
            ModulePropertyDialog dialog = new ModulePropertyDialog(this.getShell());
            ConfigExternalJarPage.this.setPageComplete(false);
            if (dialog.open() == IDialogConstants.OK_ID) {
                IMPORTType type = dialog.getImportType();
                RoutineItem routine = getSelectedRoutine();
                type.setNAME(routine.getProperty().getLabel());
                File jarFile = dialog.getFile();
                if (jarFile != null) {
                    newJarFiles.put(type, jarFile);

                }
                importTypes.add(type);
            }
            ConfigExternalJarPage.this.setPageComplete(true);
            return importTypes;
        }
    }

    /**
     * ConfigExternalPerlModulePage class global comment. Detailled comment <br/>
     * 
     * $Id: ConfigExternalPerlModulePage.java Mar 21, 20074:07:54 PM bqian $
     * 
     */
    class ModulePropertyDialog extends Dialog {

        /**
         * DOC acer ModulePropertyDialog constructor comment.
         * 
         * @param parentShell
         */
        public ModulePropertyDialog(Shell parentShell) {
            super(parentShell);
            setShellStyle(SWT.CLOSE | SWT.MIN | SWT.MAX | SWT.RESIZE | SWT.APPLICATION_MODAL);
        }

        private IMPORTType importType = null;

        private Button requiredButton;

        private Text desText;

        private FileFieldEditor fileField;

        private File file;

        private Button typeNameRadioButton;

        private Text nameText;

        private Button fileRadioButton;

        /**
         * Getter for file.
         * 
         * @return the file
         */
        public File getFile() {
            return this.file;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.dialogs.Dialog#okPressed()
         */
        @Override
        protected void okPressed() {
            IMPORTType type = ComponentFactory.eINSTANCE.createIMPORTType();
            type.setMESSAGE(desText.getText());
            String modelName = "modelName"; //$NON-NLS-1$
            boolean libExists = true; // hywang add
            ILibraryManagerService libManager = (ILibraryManagerService) GlobalServiceRegister.getDefault().getService(
                    ILibraryManagerService.class);

            if (typeNameRadioButton.getSelection()) {
                modelName = nameText.getText();
                if (!libManager.contains(modelName)) {
                    final String name = modelName;
                    libExists = false;
                    Display.getDefault().asyncExec(new Runnable() {

                        @Override
                        public void run() {
                            MessageDialog.openError(getParentShell(),
                                    Messages.getString("ConfigExternalJarPage.error"), Messages.getString( //$NON-NLS-1$
                                            "ConfigExternalJarPage.fileNotFound", name));

                        }
                    });
                }
            } else {
                file = new File(fileField.getStringValue());
                modelName = file.getName();
            }
            if (libExists) {
                type.setMODULE(modelName);
                type.setREQUIRED(requiredButton.getSelection());
                type.setUrlPath(fileField.getStringValue());
                this.importType = type;
                super.okPressed();
            }

        }

        /**
         * Validate the ok button.
         */
        private void checkEnable() {
            Button okButton = getButton(IDialogConstants.OK_ID);

            boolean ok = false;
            if (typeNameRadioButton.getSelection()) {
                ok = (nameText.getText().length() != 0);
            } else {
                String filePath = fileField.getStringValue();
                File f = new File(filePath);
                ok = f.exists();
            }
            okButton.setEnabled(ok);
        }

        /**
         * Getter for importType.
         * 
         * @return the importType
         */
        public IMPORTType getImportType() {
            return this.importType;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
         */
        @Override
        protected void configureShell(Shell shell) {
            super.configureShell(shell);
            // shell.setSize(400, 300);
            shell.setText(Messages.getString("ConfigExternalJarPage.title")); //$NON-NLS-1$
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
         */
        @Override
        protected Control createDialogArea(Composite parent) {
            // create composite
            final Composite composite = (Composite) super.createDialogArea(parent);
            GridLayout layout = (GridLayout) composite.getLayout();
            layout.numColumns = 3;

            GridLayout copyLayout = GridLayoutFactory.copyLayout(layout);

            typeNameRadioButton = new Button(composite, SWT.RADIO);
            typeNameRadioButton.setText(Messages.getString("ConfigExternalJarPage.inputJARName")); //$NON-NLS-1$
            GridDataFactory.fillDefaults().span(3, 1).applyTo(typeNameRadioButton);
            typeNameRadioButton.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    nameText.setEnabled(true);
                    fileField.setEnabled(false, composite);
                    checkEnable();

                }
            });

            ModifyListener modifyListener = new ModifyListener() {

                @Override
                public void modifyText(ModifyEvent e) {
                    checkEnable();
                }
            };

            nameText = new Text(composite, SWT.BORDER);
            nameText.addModifyListener(modifyListener);
            GridDataFactory.fillDefaults().span(3, 1).applyTo(nameText);

            fileRadioButton = new Button(composite, SWT.RADIO);
            fileRadioButton.setText(Messages.getString("ConfigExternalJarPage.browseJARFile")); //$NON-NLS-1$
            GridDataFactory.fillDefaults().span(3, 1).applyTo(fileRadioButton);

            fileRadioButton.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    nameText.setEnabled(false);
                    fileField.setEnabled(true, composite);
                    checkEnable();

                }
            });

            fileField = new FileFieldEditor(
                    "JarFileFileEdior", Messages.getString("ConfigExternalJarPage.jarFile.label"), composite); //$NON-NLS-1$ //$NON-NLS-2$
            Text filePathText = fileField.getTextControl(composite);
            filePathText.addModifyListener(modifyListener);

            fileField.setFileExtensions(FilesUtils.getAcceptJARFilesSuffix());
            composite.setLayout(copyLayout);

            GridData data = new GridData();

            requiredButton = new Button(composite, SWT.CHECK);
            String labelText = Messages.getString("ConfigExternalJarPage.required.label"); //$NON-NLS-1$
            requiredButton.setText(labelText);
            requiredButton.setToolTipText(labelText);
            requiredButton.setSelection(true);
            data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 3;
            requiredButton.setLayoutData(data);

            Label desLabel = new Label(composite, SWT.NONE);
            desLabel.setText(Messages.getString("ConfigExternalJarPage.description.label")); //$NON-NLS-1$
            data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
            desLabel.setLayoutData(data);
            desLabel.setFont(parent.getFont());

            desText = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
            data = new GridData(GridData.FILL_BOTH);
            data.heightHint = 80;
            data.widthHint = 300;
            data.horizontalSpan = 2;
            desText.setLayoutData(data);

            applyDialogFont(composite);
            return composite;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
         */
        @Override
        protected void createButtonsForButtonBar(Composite parent) {
            super.createButtonsForButtonBar(parent);
            checkEnable();
        }

    }

}
