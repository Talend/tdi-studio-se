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
package org.talend.repository.ui.wizards.ConfigExternalLib;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.designer.core.model.utils.emf.component.ComponentFactory;
import org.talend.designer.core.model.utils.emf.component.IMPORTType;
import org.talend.repository.i18n.Messages;

/**
 * Page of the Job Scripts Export Wizard. <br/>
 *
 * @referto WizardArchiveFileResourceExportPage1 $Id: JobScriptsExportWizardPage.java 1 2006-12-13 下午03:09:07 bqian
 *
 */
public class ConfigExternalPerlModulePage extends ConfigExternalLibPage {

    private LibraryField libField;

    /**
     * ConfigExternalPerlModulePage.
     *
     * @param pageName
     * @param title
     * @param titleImage
     */
    public ConfigExternalPerlModulePage(IStructuredSelection selection) {
        super(Messages.getString("ConfigExternalPerlModulePage.title"), selection); //$NON-NLS-1$
        this.setMessage(Messages.getString("ConfigExternalPerlModulePage.message")); //$NON-NLS-1$
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

        libField = new EditPerlRoutineExternalModuleField(
                Messages.getString("ConfigExternalPerlModulePage.moduleFieldLabel"), composite); //$NON-NLS-1$

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
     * subclass of the LibraryField. <br/>
     *
     * $Id: ImportExternalJarPage.java Mar 20, 20074:15:28 PM bqian $
     *
     */
    class EditPerlRoutineExternalModuleField extends LibraryField {

        /**
         *
         * @param name
         * @param parent
         */
        public EditPerlRoutineExternalModuleField(String name, Composite parent) {
            super(name, parent);
        }

        /*
         * (non-Javadoc)
         *
         * @see org.talend.repository.ui.wizards.importExternalLib.TableField#afterDeleteSelection(java.util.List)
         */
        @Override
        protected void afterDeleteSelection(List list) {

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
            if (dialog.open() == IDialogConstants.OK_ID) {
                IMPORTType type = dialog.getImportType();
                type.setNAME(getSelectedItem().getProperty().getLabel());
                importTypes.add(type);
            }
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
            setShellStyle(SWT.CLOSE | SWT.MIN | SWT.MAX | SWT.RESIZE);
        }

        private IMPORTType importType = null;

        private Button requiredButton;

        private Text desText;

        private Text moduletext;

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.dialogs.Dialog#okPressed()
         */
        @Override
        protected void okPressed() {
            IMPORTType type = ComponentFactory.eINSTANCE.createIMPORTType();
            type.setMESSAGE(desText.getText());

            type.setMODULE(moduletext.getText());
            type.setREQUIRED(requiredButton.getSelection());
            this.importType = type;
            super.okPressed();
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

            shell.setText(Messages.getString("ConfigExternalPerlModulePage.title.newModule")); //$NON-NLS-1$

        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
         */
        @Override
        protected Control createDialogArea(Composite parent) {

            // create composite
            Composite composite = (Composite) super.createDialogArea(parent);
            GridLayout layout = (GridLayout) composite.getLayout();
            layout.numColumns = 2;

            // create message
            Label label = new Label(composite, SWT.NONE);
            label.setText(Messages.getString("ConfigExternalPerlModulePage.moduleLabel")); //$NON-NLS-1$
            GridData data = new GridData();
            // data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
            label.setLayoutData(data);
            label.setFont(parent.getFont());

            moduletext = new Text(composite, SWT.SINGLE | SWT.BORDER);
            GridData gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.widthHint = 300;
            moduletext.setLayoutData(gd);

            requiredButton = new Button(composite, SWT.CHECK);
            String labelText = Messages.getString("ConfigExternalPerlModulePage.required.label"); //$NON-NLS-1$
            requiredButton.setText(labelText);
            requiredButton.setToolTipText(labelText);
            requiredButton.setSelection(true);
            data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = 2;
            requiredButton.setLayoutData(data);

            Label desLabel = new Label(composite, SWT.NONE);
            desLabel.setText(Messages.getString("ConfigExternalPerlModulePage.description.label")); //$NON-NLS-1$
            data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
            // data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
            desLabel.setLayoutData(data);
            desLabel.setFont(parent.getFont());

            desText = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
            gd = new GridData(GridData.FILL_BOTH);
            gd.heightHint = 80;
            desText.setLayoutData(gd);

            applyDialogFont(composite);
            return composite;

        }
    }

}
