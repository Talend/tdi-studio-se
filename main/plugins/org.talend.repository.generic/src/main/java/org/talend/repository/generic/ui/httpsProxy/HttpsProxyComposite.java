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
package org.talend.repository.generic.ui.httpsProxy;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.talend.commons.ui.swt.formtools.Form;
import org.talend.commons.ui.swt.formtools.UtilsButton;
import org.talend.repository.generic.i18n.Messages;

public class HttpsProxyComposite extends Composite {

    private static final String METADATA_PREFERENCE_PAGE = "org.talend.core.runtime.MetadataPrecisionPage";

    private static final String PROXY_PREFERENCE_PAGE = "org.eclipse.ui.net.NetPreferences";// org.eclipse.ui.net.proxy

    private UtilsButton metadataProxyBtn;

    private static final int HEIGHT_BUTTON_PIXEL = 30;

    public HttpsProxyComposite(Composite parent, int style) {
        super(parent, style);
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        setLayout(gridLayout);
        createControl();
    }

    private void createControl() {
        Composite proxyComposite = Form.startNewGridLayout(this, 2, true, SWT.CENTER, SWT.CENTER);
        GC gc = new GC(proxyComposite);
        String displayStr = Messages.getString("HttpsProxyComp.EnbaleHttpsProxy"); //$NON-NLS-1$
        Point buttonSize = gc.stringExtent(displayStr);
        metadataProxyBtn = new UtilsButton(proxyComposite, displayStr, buttonSize.x + 12, HEIGHT_BUTTON_PIXEL);
        metadataProxyBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                openMetadataPreference();
            }
        });
        gc.dispose();
    }

    private void openMetadataPreference() {
        PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(getShell(), METADATA_PREFERENCE_PAGE, // $NON-NLS-1$
                new String[] { METADATA_PREFERENCE_PAGE, PROXY_PREFERENCE_PAGE }, null); // $NON-NLS-1$
        dialog.open();
    }
}
