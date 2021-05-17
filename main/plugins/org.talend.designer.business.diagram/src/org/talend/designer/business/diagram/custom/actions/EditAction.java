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

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.talend.designer.business.diagram.custom.util.EmfPropertyHelper;
import org.talend.designer.business.diagram.custom.util.ShellUtil;
import org.talend.designer.business.diagram.i18n.Messages;

public class EditAction extends Action {

    private EObject eObject;

    private AdapterFactory adapterFactory;

    private EStructuralFeature structuralFeature;

    /**
     * DOC mhelleboid EditAction constructor comment.
     *
     * @param object2
     * @param adapterFactory
     * @param structuralFeature
     */
    public EditAction(EObject eObject, AdapterFactory adapterFactory, EStructuralFeature structuralFeature) {
        this.eObject = eObject;
        this.adapterFactory = adapterFactory;
        this.structuralFeature = structuralFeature;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        Shell shell = ShellUtil.getShell();
        if (shell != null) {
            IItemPropertySource itemPropertySource = EmfPropertyHelper.getItemPropertySource(adapterFactory, eObject);
            IItemPropertyDescriptor itemPropertyDescriptor = EmfPropertyHelper.getItemPropertyDescriptor(
                    itemPropertySource, eObject, structuralFeature);

            if (itemPropertySource != null) {
                EditAction.EditDialog editDialog = new EditDialog(shell, adapterFactory, itemPropertyDescriptor,
                        eObject);
                if (editDialog.open() == Dialog.OK) {
                    itemPropertyDescriptor.setPropertyValue(eObject, editDialog.getValue());
                }
            }
        }
    }

    private static class EditDialog extends Dialog {

        private AdapterFactory adapterFactory;

        private IItemPropertyDescriptor itemPropertyDescriptor;

        private EObject eObject;

        private Label eObjectText;

        private Label eObjectImage;

        private Label propertyLabel;

        private Text propertyText;

        private String value;

        /**
         * DOC mhelleboid EditDialog constructor comment.
         *
         * @param parentShell
         * @param adapterFactory
         * @param eObject
         * @param propertyDescriptor
         */
        protected EditDialog(Shell parentShell, AdapterFactory adapterFactory,
                IItemPropertyDescriptor itemPropertyDescriptor, EObject eObject) {
            super(parentShell);
            this.adapterFactory = adapterFactory;
            this.itemPropertyDescriptor = itemPropertyDescriptor;
            this.eObject = eObject;

        }

        /*
         * (non-Javadoc) Method declared in Window.
         */
        protected void configureShell(Shell shell) {
            super.configureShell(shell);
            shell.setText(Messages.getString("EditAction.Edit") + getPropertyLabel()); //$NON-NLS-1$
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
         */
        @Override
        protected Control createDialogArea(Composite parent) {
            Composite composite = (Composite) super.createDialogArea(parent);

            composite.setLayout(new GridLayout(1, false));

            AdapterFactoryLabelProvider adapterFactoryLabelProvider = new AdapterFactoryLabelProvider(adapterFactory);

            Composite eObjectComposite = new Composite(composite, SWT.NONE);
            eObjectComposite.setLayout(new GridLayout(2, false));
            eObjectComposite.setBackground(composite.getBackground());
            eObjectComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

            eObjectImage = new Label(eObjectComposite, SWT.LEFT);
            eObjectImage.setBackground(eObjectComposite.getBackground());
            eObjectImage.setImage(adapterFactoryLabelProvider.getImage(eObject));

            eObjectText = new Label(eObjectComposite, SWT.LEFT);
            eObjectText.setBackground(eObjectComposite.getBackground());
            eObjectText.setText(adapterFactoryLabelProvider.getText(eObject));

            Composite propertyComposite = new Composite(composite, SWT.NONE);
            propertyComposite.setLayout(new GridLayout(2, false));
            propertyComposite.setBackground(composite.getBackground());
            propertyComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

            propertyLabel = new Label(propertyComposite, SWT.LEFT);
            propertyLabel.setBackground(propertyComposite.getBackground());
            propertyLabel.setText(getPropertyLabel() + " : "); //$NON-NLS-1$

            propertyText = new Text(propertyComposite, SWT.SINGLE | SWT.BORDER);
            propertyText.addModifyListener(new ModifyListener() {

                public void modifyText(ModifyEvent e) {
                    value = propertyText.getText();
                }
            });
            propertyText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

            propertyText.setText(getPropertyValue());
            propertyText.selectAll();

            return composite;
        }

        /**
         * DOC mhelleboid Comment method "getPropertyValue".
         *
         * @return
         */
        private String getPropertyValue() {
            return EmfPropertyHelper.getValue(itemPropertyDescriptor, eObject);
        }

        /**
         * DOC mhelleboid Comment method "getPropertyLabel".
         *
         * @return
         */
        private String getPropertyLabel() {
            return itemPropertyDescriptor.getDisplayName(eObject);
        }

        /**
         * Getter for value.
         *
         * @return the value
         */
        public String getValue() {
            return this.value;
        }

    }
}
