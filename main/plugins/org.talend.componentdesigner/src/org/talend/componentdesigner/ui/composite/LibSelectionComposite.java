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
package org.talend.componentdesigner.ui.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.talend.componentdesigner.model.IConfiguration;
import org.talend.componentdesigner.model.ILibEntry;
import org.talend.componentdesigner.model.componentpref.ComponentPref;
import org.talend.componentdesigner.ui.action.AddExternalResourceAction;
import org.talend.componentdesigner.ui.action.AddResourceAction;
import org.talend.componentdesigner.ui.action.RemoveResourceAction;
import org.talend.componentdesigner.ui.action.UseResourceAction;
import org.talend.componentdesigner.ui.composite.provider.LibListProvider;

/**
 * DOC rli class global comment. Detailled comment
 */
public class LibSelectionComposite extends Composite {

    public LibSelectionComposite(Composite parent, int style) {
        super(parent, style);
        this.createControl(parent);
    }

    protected LibListViewer libListViewer;

    protected static final String DIALOG_SETTINGS_PREFIX = "JavaClasspathTab"; //$NON-NLS-1$

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        Font font = parent.getFont();

        // Composite comp = new Composite(parent, SWT.NONE);
        GridLayout topLayout = new GridLayout();
        topLayout.numColumns = 2;
        this.setLayout(topLayout);
        GridData gd;

        // Label label = new Label(this, SWT.NONE);
        // label.setText("Libraries Selection:");
        // gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        // gd.horizontalSpan = 2;
        // label.setLayoutData(gd);

        libListViewer = new LibListViewer(this);
        libListViewer.getControl().setFont(font);
        gd = new GridData(GridData.FILL_BOTH);
        // gd.horizontalSpan = 7;
        gd.heightHint = 100;
        gd.widthHint = 240;
        libListViewer.getControl().setLayoutData(gd);

        LibListProvider provider = new LibListProvider();
        libListViewer.setLabelProvider(provider);
        libListViewer.setContentProvider(provider);
        Composite pathButtonComp = new Composite(this, SWT.NONE);
        GridLayout pathButtonLayout = new GridLayout();
        pathButtonLayout.marginHeight = 0;
        pathButtonLayout.marginWidth = 0;
        pathButtonComp.setLayout(pathButtonLayout);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        // gd.horizontalSpan = 2;
        pathButtonComp.setLayoutData(gd);
        pathButtonComp.setFont(font);

        createPathButtons(pathButtonComp);
    }

    /**
     * Creates the buttons to manipulate the classpath.
     * 
     * @param pathButtonComp composite buttons are contained in
     * @since 3.0
     */
    protected void createPathButtons(Composite pathButtonComp) {
        createButton(pathButtonComp, new AddResourceAction(libListViewer));
        createButton(pathButtonComp, new AddExternalResourceAction(libListViewer, DIALOG_SETTINGS_PREFIX));
        createButton(pathButtonComp, new RemoveResourceAction(libListViewer));
    }

    /**
     * Creates a button for the given action.
     * 
     * @param pathButtonComp parent composite for the button
     * @param action the action triggered by the button
     * @return the button that was created
     */
    protected Button createButton(Composite pathButtonComp, UseResourceAction action) {
        Button button = createPushButton(pathButtonComp, action.getText(), null);
        action.setButton(button);
        button.setEnabled(action.updateSelection(null));
        return button;
    }

    /**
     * Creates and returns a new push button with the given label and/or image.
     * 
     * @param parent parent control
     * @param label button label or <code>null</code>
     * @param image image of <code>null</code>
     * 
     * @return a new push button
     */
    private static Button createPushButton(Composite parent, String label, Image image) {
        Button button = new Button(parent, SWT.PUSH);
        button.setFont(parent.getFont());
        if (image != null) {
            button.setImage(image);
        }
        if (label != null) {
            button.setText(label);
        }
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        button.setLayoutData(gd);
        return button;
    }

    /**
     * Refreshes the classpath entries based on the current state of the given launch configuration.
     */
    public void refresh(IConfiguration configuration) {
        // boolean useDefault = true;
        // setErrorMessage(null);
        // try {
        // useDefault = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, true);
        // } catch (CoreException e) {
        // JDIDebugUIPlugin.log(e);
        // }
        //		
        // if (configuration == getLaunchConfiguration()) {
        // // no need to update if an explicit path is being used and this setting
        // // has not changed (and viewing the same config as last time)
        // if (!useDefault) {
        // setDirty(false);
        // return;
        // }
        // }
        //		
        // setLaunchConfiguration(configuration);
        // try {
        // createClasspathModel(configuration);
        // } catch (CoreException e) {
        // setErrorMessage(e.getMessage());
        // }
        //		
        // fClasspathViewer.setLaunchConfiguration(configuration);
        // fClasspathViewer.setInput(fModel);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.ui.ILaunchConfigurationTab#dispose()
     */
    @Override
    public void dispose() {
        if (libListViewer != null) {
            // fClasspathViewer.removeEntriesChangedListener(this);
        }
        super.dispose();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
     */
    // public Image getImage() {
    // return getClasspathImage();
    // }
    /**
     * Returns whether the bootpath should be displayed.
     * 
     * @return whether the bootpath should be displayed
     * @since 3.0
     */
    public boolean isShowBootpath() {
        return true;
    }

    public void setLibEntries(ILibEntry[] entries) {
        libListViewer.setEntries(entries);
    }

    public void setComponentPrefBean(ComponentPref componentPrefBean) {
        libListViewer.setConponentPrfBean(componentPrefBean);

    }
}
