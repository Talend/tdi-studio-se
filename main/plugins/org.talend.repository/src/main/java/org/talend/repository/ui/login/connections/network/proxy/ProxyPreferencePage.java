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
package org.talend.repository.ui.login.connections.network.proxy;

import org.eclipse.core.internal.net.ProxySelector;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.internal.net.NetUIMessages;

/**
 * Please refer to original class: org.eclipse.ui.internal.net.ProxyPreferencePage
 * 
 * @see org.eclipse.ui.internal.net.ProxyPreferencePage
 */
public class ProxyPreferencePage extends PreferencePage {

    private Label providerLabel;

    protected Combo providerCombo;

    private ProxyEntriesComposite proxyEntriesComposite;

    private NonProxyHostsComposite nonProxyHostsComposite;

    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        createProviderComposite(composite);
        createProxyEntriesComposite(composite);
        createNonProxiedHostsComposite(composite);

        applyDialogFont(composite);
        initializeValues();

        return composite;
    }

    private void createProviderComposite(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        providerLabel = new Label(composite, SWT.NONE);
        providerLabel.setText(NetUIMessages.ProxyPreferencePage_0);
        providerCombo = new Combo(composite, SWT.READ_ONLY | SWT.DROP_DOWN);
        providerCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                setProvider(ProxySelector.unlocalizeProvider(providerCombo.getText()));
            }
        });
    }

    private void createProxyEntriesComposite(Composite parent) {
        proxyEntriesComposite = new ProxyEntriesComposite(parent, SWT.NONE);
        proxyEntriesComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    }

    private void createNonProxiedHostsComposite(Composite parent) {
        nonProxyHostsComposite = new NonProxyHostsComposite(parent, SWT.NONE);
        nonProxyHostsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    }

    @Override
    protected void performApply() {
        refresh();
        int sel = providerCombo.getSelectionIndex();
        proxyEntriesComposite.performApply();
        nonProxyHostsComposite.performApply();
        ProxySelector.setActiveProvider(ProxySelector.unlocalizeProvider(providerCombo.getItem(sel)));
    }

    @Override
    protected void performDefaults() {
        int index = 1;
        if (providerCombo.getItemCount() == 3) {
            index = 2;
        }
        providerCombo.select(index);
        setProvider(ProxySelector.unlocalizeProvider(providerCombo.getItem(index)));
    }

    @Override
    public boolean performOk() {
        performApply();
        return super.performOk();
    }

    private void initializeValues() {
        String[] providers = ProxySelector.getProviders();
        String[] localizedProviders = new String[providers.length];
        for (int i = 0; i < localizedProviders.length; i++) {
            localizedProviders[i] = ProxySelector.localizeProvider(providers[i]);
        }
        providerCombo.setItems(localizedProviders);
        providerCombo.select(providerCombo.indexOf(ProxySelector.localizeProvider(ProxySelector.getDefaultProvider())));
    }

    protected void setProvider(String name) {
        proxyEntriesComposite.setProvider(name);
        nonProxyHostsComposite.setProvider(name);
        refresh();
    }

    private void refresh() {
        proxyEntriesComposite.refresh();
        nonProxyHostsComposite.refresh();
    }

}
