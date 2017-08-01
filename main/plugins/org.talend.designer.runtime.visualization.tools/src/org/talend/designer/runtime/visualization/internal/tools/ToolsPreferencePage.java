/*******************************************************************************
 * Copyright (c) 2010 JVM Monitor project. All rights reserved. 
 * 
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.talend.designer.runtime.visualization.internal.tools;

import java.io.File;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.talend.designer.runtime.visualization.tools.Activator;

/**
 * The preference page at preference dialog: Java > Monitor > Tools.
 * <p>
 * On this preference page, user can configure the settings of two features: one
 * is to detect the running JVMs on local host, and the other is to view the
 * heap histogram. Those features work only with JVM based on SUN JDK (e.g. Sun
 * JDK, Open JDK, Java for Mac, and maybe JRockit), since it requires a vender
 * specific package contained in <tt>tools.jar</tt> on Windows/Linux, or
 * <tt>classes.jar</tt> on Mac.
 */
public class ToolsPreferencePage extends PreferencePage implements
        IWorkbenchPreferencePage, IConstants {

    /** The help context id for this page. */
    private static final String JAVA_MONITOR_TOOLS_PREFERENCE_PAGE = Activator.PLUGIN_ID
            + '.' + "java_monitor_tools_preference_page_context"; //$NON-NLS-1$

    /** The minimum value of update period. */
    private static final int MIN_UPDATE_PERIOD = 100;

    /** The text field to specify JDK root directory. */
    Text jdkRootDirectoryText;

    /** The text field to specify update period in milliseconds. */
    private Text updatePeriodText;

    /** The max number of classes for heap. */
    private Text maxNumberOfClassesText;

    /*
     * @see PreferencePage#createContents(Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        if (!Util.isMac() || !Tools.getInstance().isReady()) {
            Label label = new Label(composite, SWT.WRAP);
            label.setText(Messages.toolsPreferencePageLabel);
            GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
            layoutData.widthHint = 300;
            label.setLayoutData(layoutData);
            createJdkRootDirectoryGroup(composite);
        }
        createUpdatePeriodTextField(composite);
        createMemoryGroup(composite);

        applyDialogFont(composite);

        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(parent, JAVA_MONITOR_TOOLS_PREFERENCE_PAGE);

        return composite;
    }

    /*
     * @see IWorkbenchPreferencePage#init(IWorkbench)
     */
    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

    /*
     * @see PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        if (jdkRootDirectoryText != null) {
            getPreferenceStore().setValue(IConstants.JDK_ROOT_DIRECTORY, jdkRootDirectoryText.getText());
        }
        getPreferenceStore().setValue(IConstants.UPDATE_PERIOD,
                Long.valueOf(updatePeriodText.getText()));
        getPreferenceStore().setValue(IConstants.MAX_CLASSES_NUMBER,
                Integer.valueOf(maxNumberOfClassesText.getText()));
        return true;
    }

    /*
     * @see PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        IPreferenceStore store = getPreferenceStore();
        String updatePeriod = String.valueOf(store
                .getDefaultInt(IConstants.UPDATE_PERIOD));
        updatePeriodText.setText(updatePeriod);
        maxNumberOfClassesText.setText(String.valueOf(getPreferenceStore()
                .getDefaultInt(IConstants.MAX_CLASSES_NUMBER)));

        super.performDefaults();
    }

    /**
     * Creates the update period text field.
     * 
     * @param parent
     *            The parent composite
     */
    private void createUpdatePeriodTextField(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setLayout(new GridLayout(2, false));
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setText(Messages.autoDetectGroupLabel);

        Label label = new Label(group, SWT.NONE);
        label.setText(Messages.updatePeriodLabel);

        updatePeriodText = new Text(group, SWT.BORDER);
        updatePeriodText.setText(String.valueOf(getPreferenceStore().getInt(
                IConstants.UPDATE_PERIOD)));
        updatePeriodText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        updatePeriodText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                validateUpdatePeriod();
            }
        });
    }

    /**
     * Creates the JDK root directory group.
     * 
     * @param parent
     *            The parent composite
     */
    private void createJdkRootDirectoryGroup(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(3, false));
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label label = new Label(composite, SWT.NONE);
        label.setText(Messages.jdkRootDirectoryLabel);

        jdkRootDirectoryText = new Text(composite, SWT.BORDER);
        jdkRootDirectoryText.setText(getPreferenceStore().getString(
                IConstants.JDK_ROOT_DIRECTORY));
        jdkRootDirectoryText.setLayoutData(new GridData(
                GridData.FILL_HORIZONTAL));
        jdkRootDirectoryText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                validateJdkRootDirectory();
            }
        });

        Button button = new Button(composite, SWT.NONE);
        button.setText(Messages.browseButton);
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog dialog = new DirectoryDialog(e.display
                        .getActiveShell());
                dialog.setMessage(Messages.selectJdkRootDirectoryMsg);
                String path = dialog.open();
                if (path == null) {
                    return;
                }

                File file = new File(path);
                if (file.isDirectory()) {
                    jdkRootDirectoryText.setText(path);
                }
            }
        });
    }

    /**
     * Creates the memory group.
     * 
     * @param parent
     *            The parent composite
     */
    private void createMemoryGroup(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setText(Messages.memoryGroupLabel);
        GridLayout layout = new GridLayout(2, false);
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label label = new Label(group, SWT.NONE);
        label.setText(Messages.maxNumberOfClassesLabel);
        maxNumberOfClassesText = new Text(group, SWT.BORDER);
        maxNumberOfClassesText.setText(String.valueOf(getPreferenceStore()
                .getInt(IConstants.MAX_CLASSES_NUMBER)));
        maxNumberOfClassesText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                validateMaxNumberOfClasses();
            }
        });

        maxNumberOfClassesText.setLayoutData(new GridData(
                GridData.FILL_HORIZONTAL));
    }

    /**
     * Validates the JDK root directory.
     */
    void validateJdkRootDirectory() {

        // check if text is empty
        String jdkRootDirectory = jdkRootDirectoryText.getText();
        if (jdkRootDirectory.isEmpty()) {
            setMessage(Messages.jdkRootDirectoryNotEnteredMsg,
                    IMessageProvider.WARNING);
            return;
        }

        String message = Tools.getInstance().validateJdkRootDirectory(
                jdkRootDirectory);
        setMessage(message, IMessageProvider.WARNING);
    }

    /**
     * Validates the update period.
     */
    void validateUpdatePeriod() {

        // check if text is empty
        String period = updatePeriodText.getText();
        if (period.isEmpty()) {
            setMessage(Messages.updatePeriodNotEnteredMsg,
                    IMessageProvider.WARNING);
            return;
        }

        // check if text is integer
        try {
            Integer.parseInt(period);
        } catch (NumberFormatException e) {
            setMessage(Messages.illegalUpdatePeriodMsg, IMessageProvider.ERROR);
            return;
        }

        // check if the value is within valid range
        if (Integer.valueOf(period) < MIN_UPDATE_PERIOD) {
            setMessage(Messages.updatePeriodOutOfRangeMsg,
                    IMessageProvider.ERROR);
            return;
        }

        setMessage(null);
    }

    /**
     * Validates the max number of classes.
     * 
     */
    void validateMaxNumberOfClasses() {

        // check if text is empty
        String period = maxNumberOfClassesText.getText();
        if (period.isEmpty()) {
            setMessage(Messages.enterMaxNumberOfClassesMsg,
                    IMessageProvider.WARNING);
            return;
        }

        // check if text is integer
        try {
            Integer.parseInt(period);
        } catch (NumberFormatException e) {
            setMessage(Messages.maxNumberOfClassesInvalidMsg,
                    IMessageProvider.ERROR);
            return;
        }

        // check if the value is within valid range
        if (Integer.valueOf(period) <= 0) {
            setMessage(Messages.maxNumberOfClassesOutOfRangeMsg,
                    IMessageProvider.ERROR);
            return;
        }

        setMessage(null);
    }
}
