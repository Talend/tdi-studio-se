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
package org.talend.repository.ui.login.connections.settings;

import java.net.URI;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.service.IStudioLiteP2Service;
import org.talend.core.service.IStudioLiteP2Service.UpdateSiteConfig;

/**
 * DOC cmeng class global comment. Detailled comment
 */
public class UpdatesitePreferencePage extends PreferencePage {

    IStudioLiteP2Service p2Service = IStudioLiteP2Service.get();

    private Text releaseUriText;

    private Text updateUriText;

    @Override
    protected Control createContents(Composite parent) {
        Composite panel = new Composite(parent, SWT.NONE);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 2;
        panel.setLayoutData(data);

        FillLayout layout = new FillLayout();
        layout.marginHeight = 5;
        layout.marginWidth = 10;
        panel.setLayout(layout);

        Composite composite = new Composite(panel, SWT.NONE);
        GridLayout compLayout = new GridLayout(2, false);
        compLayout.marginHeight = 0;
        compLayout.marginWidth = 0;
        composite.setLayout(compLayout);

        Label releaseLabel = new Label(composite, SWT.NONE);
        releaseLabel.setText("Release Url");
        GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        releaseLabel.setLayoutData(gd);

        releaseUriText = new Text(composite, SWT.BORDER);
        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        releaseUriText.setLayoutData(gd);

        Label updateLabel = new Label(composite, SWT.NONE);
        updateLabel.setText("Update Url");
        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        updateLabel.setLayoutData(gd);

        updateUriText = new Text(composite, SWT.BORDER);
        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        updateUriText.setLayoutData(gd);

        init();
        addListener();
        return parent;
    }

    private void init() {
        try {
            UpdateSiteConfig config = p2Service.getUpdateSiteConfig();
            URI release = config.getRelease();
            releaseUriText.setText(release == null ? "" : release.toString());
            URI update = config.getUpdate();
            updateUriText.setText(update == null ? "" : update.toString());
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    private void addListener() {
        releaseUriText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                refresh();
            }
        });
        updateUriText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                refresh();
            }
        });
    }

    @Override
    public boolean performOk() {
        if (this.isControlCreated()) {
            try {
                UpdateSiteConfig config = p2Service.getUpdateSiteConfig();
                String release = releaseUriText.getText();
                config.setRelease(StringUtils.isBlank(release) ? null : new URI(release));
                String update = updateUriText.getText();
                config.setUpdate(StringUtils.isBlank(update) ? null : new URI(update));
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }
        return super.performOk();
    }

    @Override
    protected void performDefaults() {
        if (this.isControlCreated()) {
            // TODO using talend offical?
        }
        super.performDefaults();
    }

    private void refresh() {
        this.updateApplyButton();
        this.getContainer().updateButtons();
    }

    private boolean validate() {
        this.setErrorMessage(null);
        if (StringUtils.equals(releaseUriText.getText(), updateUriText.getText())) {
            this.setErrorMessage("Release and Update should be different");
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (this.isControlCreated() && validate());
    }

}
