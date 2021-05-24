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
package org.talend.repository.preference;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.runtime.maven.MavenConstants;
import org.talend.core.runtime.projectsetting.AbstractProjectSettingPage;
import org.talend.designer.maven.DesignerMavenPlugin;
import org.talend.designer.maven.tools.AggregatorPomsHelper;
import org.talend.designer.maven.utils.PomIdsHelper;
import org.talend.repository.ProjectManager;
import org.talend.repository.i18n.Messages;

/**
 * DOC zwxue class global comment. Detailled comment
 */
public class MavenGroupIdProjectSettingPage extends AbstractProjectSettingPage {

    private Text groupIdText;

    private Button appendFolderButton;

    private Label skipGroupIdLabel;

    private Button skipGroupIdButton;

    private String oldGroupId;

    private boolean oldAppendFolder;

    private boolean oldSkipGroupId;

    public MavenGroupIdProjectSettingPage() {
        noDefaultAndApplyButton();
    }

    @Override
    protected String getPreferenceName() {
        return DesignerMavenPlugin.PLUGIN_ID;
    }

    @Override
    protected void createFieldEditors() {
        Composite parent = getFieldEditorParent();
        parent.setLayout(new GridLayout());

        Composite textCompsite = new Composite(parent, SWT.NONE);
        textCompsite.setLayout(new GridLayout(2, false));

        Label groupIdLabel = new Label(textCompsite, SWT.NONE);
        groupIdLabel.setText(Messages.getString("MavenGroupIdProjectSettingPage.groupIdLabel")); //$NON-NLS-1$

        groupIdText = new Text(textCompsite, SWT.BORDER);
        GC gc = new GC(groupIdText);
        String defaultGroupId = "org.example." + ProjectManager.getInstance().getCurrentProject().getTechnicalLabel(); //$NON-NLS-1$
        Point labelSize = gc.stringExtent(defaultGroupId);
        gc.dispose();
        int hint = labelSize.x + (ITabbedPropertyConstants.HSPACE * 15);
        GridData textData = new GridData();
        textData.widthHint = hint;
        groupIdText.setLayoutData(textData);

        Composite checkComposite = new Composite(parent, SWT.NONE);
        checkComposite.setLayout(new GridLayout(2, false));

        appendFolderButton = new Button(checkComposite, SWT.CHECK);
        Label appendFolderButtonLabel = new Label(checkComposite, SWT.NONE);
        appendFolderButtonLabel.setText(Messages.getString("MavenGroupIdProjectSettingPage.appendFolderLabel")); //$NON-NLS-1$

        skipGroupIdButton = new Button(checkComposite, SWT.CHECK);
        skipGroupIdLabel = new Label(checkComposite, SWT.NONE);
        skipGroupIdLabel.setText(Messages.getString("MavenGroupIdProjectSettingPage.skipGroupIdLabel")); //$NON-NLS-1$

        initFields();
        addListeners();

    }

    private void initFields() {
        oldGroupId = getPreferenceStore().getString(MavenConstants.PROJECT_GROUPID);
        groupIdText.setText(oldGroupId);

        oldAppendFolder = getPreferenceStore().getBoolean(MavenConstants.APPEND_FOLDER_TO_GROUPID);
        appendFolderButton.setSelection(oldAppendFolder);

        oldSkipGroupId = getPreferenceStore().getBoolean(MavenConstants.SKIP_BASE_GROUPID);
        skipGroupIdButton.setSelection(oldSkipGroupId);

        skipGroupIdButton.setEnabled(oldAppendFolder);
        skipGroupIdLabel.setEnabled(oldAppendFolder);
    }

    private void addListeners() {
        groupIdText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                String text = groupIdText.getText();
                if (!PomIdsHelper.isValidGroupId(text)) {
                    setErrorMessage(Messages.getString("MavenGroupIdProjectSettingPage.groupIdErrMsg")); //$NON-NLS-1$
                    setValid(false);
                } else {
                    setErrorMessage(null);
                    setValid(true);
                }

            }
        });

        appendFolderButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                skipGroupIdButton.setEnabled(appendFolderButton.getSelection());
                skipGroupIdLabel.setEnabled(appendFolderButton.getSelection());
            }

        });
    }

    @Override
    public boolean performOk() {
        if (groupIdText != null && !groupIdText.isDisposed()) {
            String groupId = groupIdText.getText();
            boolean appendFolder = appendFolderButton.getSelection();
            boolean skipGroupId = skipGroupIdButton.getSelection();
            boolean needSync = false;
            if (!oldGroupId.equals(groupId)) {
                getPreferenceStore().setValue(MavenConstants.PROJECT_GROUPID, groupId);
                needSync = true;
            }
            if (oldAppendFolder != appendFolder) {
                getPreferenceStore().setValue(MavenConstants.APPEND_FOLDER_TO_GROUPID, appendFolder);
                needSync = true;
            }
            if (oldSkipGroupId != skipGroupId) {
                if (!appendFolder) {
                    getPreferenceStore().setValue(MavenConstants.SKIP_BASE_GROUPID, false);
                } else {
                    getPreferenceStore().setValue(MavenConstants.SKIP_BASE_GROUPID, skipGroupId);
                    needSync = true;
                }
            }
            if (needSync) {
                try {
                    new AggregatorPomsHelper().syncAllPoms();
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }
        }
        return super.performOk();
    }

}
