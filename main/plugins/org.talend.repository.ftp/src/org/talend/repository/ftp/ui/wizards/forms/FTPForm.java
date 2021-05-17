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
package org.talend.repository.ftp.ui.wizards.forms;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.talend.commons.ui.swt.formtools.LabelledCombo;
import org.talend.commons.ui.swt.formtools.LabelledFileField;
import org.talend.commons.ui.swt.formtools.LabelledText;
import org.talend.commons.ui.utils.PathUtils;
import org.talend.core.model.metadata.builder.connection.FTPConnection;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.metadata.managment.ui.wizard.AbstractForm;
import org.talend.repository.ftp.i18n.Messages;
import org.talend.repository.ftp.ui.wizards.pags.FTPPage;

/**
 * DOC hwang class global comment. Detailled comment
 */
public class FTPForm extends AbstractForm {

    private static final String ENCODING = "ISO-8859-15"; //$NON-NLS-1$

    private static final String PUBLIC_KEY = "Public key"; //$NON-NLS-1$

    private static final String CUSTOM = "CUSTOM";//$NON-NLS-1$

    private static final String PASSWORD = "Password"; //$NON-NLS-1$

    private FTPPage page;

    private LabelledText ftpUsernameText;

    private LabelledText ftpPasswordText;

    private LabelledText ftpPortText;

    private LabelledText ftpHostText;

    private LabelledText proxyUsernameText;

    private LabelledText proxyPasswordText;

    private LabelledText proxyPortText;

    private LabelledText proxyHostText;

    private LabelledFileField privatekeyText;

    private LabelledText passphraseText;

    private LabelledFileField keyFileText;

    private LabelledText keyPasswordText;

    private LabelledCombo connModelCombo;
    
    private LabelledText connTimeoutText;

    private LabelledCombo encodeCombo;

    private LabelledCombo methodCombo;

    private Composite tetsCom;

    private Text customText;

    private Button fnEncodingBtn;

    private Button sftpSuppBut;

    private Button ftpsSuppBut;

    private Button useSocksBut;

    private Group buildGroup;

    private Composite encodingComp;

    private Composite sftpChildCom;

    private Composite ftpsChildCom;

    private Composite proxyCom;

    private Composite proxyChildCom;

    private GridData sftpChildComGridData;

    private GridData ftpsChildComGridData;

    private GridData proxyChildComGridData;

    private GridData fnEncodingBtnGD;

    /**
     * DOC Administrator FTPForm constructor comment.
     *
     * @param parent
     * @param style
     */
    public FTPForm(Composite parent, ConnectionItem connectionItem, String[] existingNames, FTPPage page) {
        super(parent, SWT.NONE, existingNames);
        this.connectionItem = connectionItem;
        this.page = page;
        setConnectionItem(connectionItem); // must be first.
        setupForm(false);
        layoutForm();
    }

    private void layoutForm() {
        GridLayout layout = (GridLayout) getLayout();
        layout.marginHeight = 0;
        setLayout(layout);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.swt.utils.AbstractForm#adaptFormToReadOnly()
     */

    @Override
    protected void adaptFormToReadOnly() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.swt.utils.AbstractForm#addFields()
     */

    @Override
    protected void addFields() {
        Group ftpParameterGroup = new Group(this, SWT.NULL);
        ftpParameterGroup.setText("Server"); //$NON-NLS-1$
        GridLayout ftpParameterLayout = new GridLayout();
        ftpParameterLayout.numColumns = 2;
        ftpParameterGroup.setLayout(ftpParameterLayout);

        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        ftpParameterGroup.setLayoutData(gridData);

        ftpUsernameText = new LabelledText(ftpParameterGroup, Messages.getString("FTPForm_ftpUsernameText"), true); //$NON-NLS-1$

        ftpPasswordText = new LabelledText(ftpParameterGroup,
                Messages.getString("FTPForm_ftpPasswordText"), 1, SWT.BORDER | SWT.PASSWORD); //$NON-NLS-1$

        ftpHostText = new LabelledText(ftpParameterGroup, Messages.getString("FTPForm_ftpHostText"), true); //$NON-NLS-1$

        ftpPortText = new LabelledText(ftpParameterGroup, Messages.getString("FTPForm_ftpPortText"), true); //$NON-NLS-1$

        encodingComp = new Composite(ftpParameterGroup, SWT.NONE);
        GridData encodingCompGD = new GridData(SWT.FILL, SWT.CENTER, true, false);
        encodingCompGD.horizontalSpan = 2;
        encodingComp.setLayoutData(encodingCompGD);
        GridLayout encodingCompLayout = new GridLayout(4, false);
        encodingCompLayout.marginWidth = 0;
        encodingComp.setLayout(encodingCompLayout);
        fnEncodingBtn = new Button(encodingComp, SWT.CHECK);
        fnEncodingBtn.setText("Filename encoding"); //$NON-NLS-1$
        fnEncodingBtnGD = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
        fnEncodingBtn.setLayoutData(fnEncodingBtnGD);
        List<String> codeList = new ArrayList<String>();
        codeList.add(ENCODING);
        codeList.add("UTF-8"); //$NON-NLS-1$
        codeList.add(CUSTOM);
        encodeCombo = new LabelledCombo(encodingComp, "Encoding", "", codeList); //$NON-NLS-1$ //$NON-NLS-2$
        if (getConnection().getEcoding() == null || "".equals(getConnection().getEcoding())) { //$NON-NLS-1$
            encodeCombo.setText(ENCODING);
            getConnection().setEcoding(encodeCombo.getText());
        }

        customText = new Text(encodingComp, SWT.BORDER | SWT.SINGLE);
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.horizontalSpan = 1;
        customText.setLayoutData(gd);

        List<String> connList = new ArrayList<String>();
        connList.add("Passive"); //$NON-NLS-1$
        connList.add("Active"); //$NON-NLS-1$
        connModelCombo = new LabelledCombo(ftpParameterGroup, Messages.getString("FTPForm_conn_model"), "", connList); //$NON-NLS-1$ //$NON-NLS-2$
        if (getConnection().getMode() == null || "".equals(getConnection().getMode())) { //$NON-NLS-1$
            connModelCombo.setText(Messages.getString("FTPForm_passive")); //$NON-NLS-1$
            getConnection().setMode(connModelCombo.getText());
        }
        
        connTimeoutText = new LabelledText(ftpParameterGroup, Messages.getString("FTPForm_conn_timeout"), true); //$NON-NLS-1$
        connTimeoutText.setToolTipText(Messages.getString("FTPForm_conn_timeunit")); //$NON-NLS-1$
        connTimeoutText.setText("0");
        if (getConnection().getMode() == null || "".equals(getConnection().getMode())) { //$NON-NLS-1$
            connModelCombo.setText(Messages.getString("FTPForm_passive")); //$NON-NLS-1$
            getConnection().setMode(connModelCombo.getText());
        }

        buildGroup = new Group(this, SWT.NULL);
        buildGroup.setText("Parameter"); //$NON-NLS-1$
        GridLayout layoutGroup = new GridLayout(1, false);
        buildGroup.setLayout(layoutGroup);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        buildGroup.setLayoutData(gridData);

        Composite checkButtonCom = new Composite(buildGroup, SWT.NONE);
        layoutGroup = new GridLayout(2, false);
        checkButtonCom.setLayout(layoutGroup);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        checkButtonCom.setLayoutData(gridData);

        sftpSuppBut = new Button(checkButtonCom, SWT.CHECK);
        sftpSuppBut.setText(Messages.getString("FTPForm_sftp_suport")); //$NON-NLS-1$
        ftpsSuppBut = new Button(checkButtonCom, SWT.CHECK);
        ftpsSuppBut.setText(Messages.getString("FTPForm_ftps_support")); //$NON-NLS-1$
        String[] methodComboStr = { PUBLIC_KEY, PASSWORD };

        tetsCom = new Composite(buildGroup, SWT.NONE);
        layoutGroup = new GridLayout(1, false);
        tetsCom.setLayout(layoutGroup);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        tetsCom.setLayoutData(gridData);

        sftpChildCom = new Composite(tetsCom, SWT.NONE);
        layoutGroup = new GridLayout(3, false);
        sftpChildCom.setLayout(layoutGroup);
        sftpChildComGridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.minimumWidth = 300;
        gridData.minimumHeight = 120;
        gridData.widthHint = 300;
        gridData.heightHint = 110;
        sftpChildCom.setLayoutData(sftpChildComGridData);

        methodCombo = new LabelledCombo(sftpChildCom,
                Messages.getString("FTPForm_authen_method"), "", methodComboStr, 2, false, SWT.NONE); //$NON-NLS-1$ //$NON-NLS-2$

        // file Field
        String[] extensions = { "*.*" }; //$NON-NLS-1$
        privatekeyText = new LabelledFileField(sftpChildCom, Messages.getString("FTPForm_privatekeyText"), extensions); //$NON-NLS-1$
        passphraseText = new LabelledText(sftpChildCom,
                Messages.getString("FTPForm_passphraseText"), 1, SWT.BORDER | SWT.PASSWORD); //$NON-NLS-1$

        ftpsChildCom = new Composite(tetsCom, SWT.NONE);
        layoutGroup = new GridLayout(3, false);
        ftpsChildCom.setLayout(layoutGroup);
        ftpsChildComGridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.minimumWidth = 300;
        gridData.minimumHeight = 120;
        gridData.widthHint = 300;
        gridData.heightHint = 120;
        ftpsChildCom.setLayoutData(ftpsChildComGridData);
        keyFileText = new LabelledFileField(ftpsChildCom, Messages.getString("FTPForm_keyFileText"), extensions); //$NON-NLS-1$
        keyPasswordText = new LabelledText(ftpsChildCom,
                Messages.getString("FTPForm_keyPasswordText"), 1, SWT.BORDER | SWT.PASSWORD); //$NON-NLS-1$

        proxyCom = new Composite(buildGroup, SWT.NONE);
        layoutGroup = new GridLayout(1, false);
        proxyCom.setLayout(layoutGroup);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        proxyCom.setLayoutData(gridData);

        useSocksBut = new Button(proxyCom, SWT.CHECK);
        useSocksBut.setText(Messages.getString("FTPForm_sccks_proxy")); //$NON-NLS-1$

        proxyChildCom = new Composite(proxyCom, SWT.NONE);
        layoutGroup = new GridLayout(2, false);
        proxyChildCom.setLayout(layoutGroup);
        proxyChildComGridData = new GridData(GridData.FILL_HORIZONTAL);
        proxyChildCom.setLayoutData(proxyChildComGridData);
        proxyHostText = new LabelledText(proxyChildCom, Messages.getString("FTPForm_proxyHostText"), true); //$NON-NLS-1$
        proxyPortText = new LabelledText(proxyChildCom, Messages.getString("FTPForm_proxyPortText"), true); //$NON-NLS-1$
        proxyUsernameText = new LabelledText(proxyChildCom, Messages.getString("FTPForm_proxyUsernameText"), true); //$NON-NLS-1$
        proxyPasswordText = new LabelledText(proxyChildCom,
                Messages.getString("FTPForm_proxyPasswordText"), 1, SWT.BORDER | SWT.PASSWORD); //$NON-NLS-1$

        checkFieldsValue();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.swt.utils.AbstractForm#addFieldsListeners()
     */

    @Override
    protected void addFieldsListeners() {
        ftpUsernameText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                getConnection().setUsername(ftpUsernameText.getText());
                checkFieldsValue();
            }
        });

        ftpPasswordText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                FTPConnection conn = getConnection();
                conn.setPassword(conn.getValue(ftpPasswordText.getText(), true));
                checkFieldsValue();
            }
        });
        ftpPortText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                getConnection().setPort(ftpPortText.getText());
                checkFieldsValue();
            }
        });
        ftpHostText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                getConnection().setHost(ftpHostText.getText());
                checkFieldsValue();
            }
        });
        proxyUsernameText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                getConnection().setProxyuser(proxyUsernameText.getText());
            }
        });
        proxyPasswordText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                FTPConnection conn = getConnection();
                conn.setProxypassword(conn.getValue(proxyPasswordText.getText(), true));
            }
        });
        proxyPortText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                getConnection().setProxyport(proxyPortText.getText());
            }
        });
        proxyHostText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                getConnection().setProxyhost(proxyHostText.getText());
            }
        });
        keyFileText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                getConnection().setKeystoreFile(PathUtils.getPortablePath(keyFileText.getText()));
                checkFilePathAndManageIt();
            }
        });
        keyPasswordText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                FTPConnection conn = getConnection();
                conn.setKeystorePassword(conn.getValue(keyPasswordText.getText(), true));
            }
        });
        connTimeoutText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                getConnection().setTimeout(connTimeoutText.getText());
                checkFieldsValue();
            }
        });
        connModelCombo.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                getConnection().setMode(connModelCombo.getText());
            }
        });
        encodeCombo.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                getConnection().setEcoding(encodeCombo.getText());
                if (CUSTOM.equals(getConnection().getEcoding())) {
                    customText.setVisible(true);
                    customText.setText(ENCODING);
                    getConnection().setCustomEncode(customText.getText());
                } else {
                    getConnection().setCustomEncode(getConnection().getEcoding());
                    customText.setVisible(false);
                }
            }
        });

        customText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                getConnection().setCustomEncode(customText.getText());
            }
        });

        methodCombo.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                FTPConnection conn = getConnection();
                conn.setMethod(methodCombo.getText());
                if (PUBLIC_KEY.equals(conn.getMethod())) {
                    privatekeyText.setVisible(true);
                    passphraseText.setVisible(true);
                    privatekeyText.setText(conn.getPrivatekey() != null ? conn.getPrivatekey() : ""); //$NON-NLS-1$
                    // decrypt password
                    passphraseText.setText(conn.getValue(conn.getPassphrase(), false));
                } else {
                    privatekeyText.setVisible(false);
                    passphraseText.setVisible(false);
                }
                checkFieldsValue();
            }
        });

        privatekeyText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                getConnection().setPrivatekey(PathUtils.getPortablePath(privatekeyText.getText()));
                checkFilePathAndManageIt();
            }
        });

        passphraseText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                FTPConnection conn = getConnection();
                conn.setPassphrase(conn.getValue(passphraseText.getText(), true));
            }
        });
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.swt.utils.AbstractForm#addUtilsButtonListeners()
     */

    @Override
    protected void addUtilsButtonListeners() {
        sftpSuppBut.addSelectionListener(new SelectionAdapter() {

            /*
             * (non-Javadoc)
             *
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */

            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean show = sftpSuppBut.getSelection();
                if (show) {
                    sftpChildComGridData.exclude = false;
                    fnEncodingBtnGD.exclude = false;
                    ftpsChildComGridData.exclude = true;
                    encodeCombo.setHideWidgets(!fnEncodingBtn.getSelection());
                    customText.setVisible(fnEncodingBtn.getSelection() && CUSTOM.equals(getConnection().getEcoding()));
                } else {
                    sftpChildComGridData.exclude = true;
                    fnEncodingBtnGD.exclude = true;
                    encodeCombo.setHideWidgets(false);
                    customText.setVisible(CUSTOM.equals(getConnection().getEcoding()));
                }
                if (show && ftpsSuppBut.getSelection()) {
                    getConnection().setFTPS(!show);
                    ftpsSuppBut.setSelection(!show);
                }
                if (PUBLIC_KEY.equals(methodCombo.getText())) {
                    privatekeyText.setVisible(show);
                    passphraseText.setVisible(show);
                } else {
                    privatekeyText.setVisible(false);
                    passphraseText.setVisible(false);
                }
                connModelCombo.setEnabled(!show);
                getConnection().setSFTP(show);
                checkFieldsValue();
                sftpChildCom.setVisible(show);
                ftpsChildCom.setVisible(!show && ftpsSuppBut.getSelection());
                fnEncodingBtn.setVisible(show);
                encodingComp.layout();
                encodingComp.getParent().layout();
                sftpChildCom.layout();
                ftpsChildCom.layout();
                tetsCom.layout();
                buildGroup.layout();
            }

        });

        ftpsSuppBut.addSelectionListener(new SelectionAdapter() {

            /*
             * (non-Javadoc)
             *
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */

            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean show = ftpsSuppBut.getSelection();
                if (show) {
                    ftpsChildComGridData.exclude = false;
                    sftpChildComGridData.exclude = true;
                } else {
                    ftpsChildComGridData.exclude = true;
                }
                if (show && sftpSuppBut.getSelection()) {
                    getConnection().setSFTP(!show);
                    sftpSuppBut.setSelection(!show);
                }
                connModelCombo.setEnabled(!show);
                getConnection().setFTPS(show);
                checkFieldsValue();
                sftpChildCom.setVisible(!show && sftpSuppBut.getSelection());
                ftpsChildCom.setVisible(show);
                sftpChildCom.layout();
                ftpsChildCom.layout();
                tetsCom.layout();
                buildGroup.layout();
            }

        });

        useSocksBut.addSelectionListener(new SelectionAdapter() {

            /*
             * (non-Javadoc)
             *
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */

            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean show = useSocksBut.getSelection();
                if (show) {
                    proxyChildComGridData.exclude = false;
                } else {
                    proxyChildComGridData.exclude = true;
                }
                getConnection().setUsesocks(show);
                proxyChildCom.setVisible(show);
                proxyChildCom.layout();
                proxyCom.layout();
                tetsCom.layout();
                buildGroup.layout();
                layout();
            }
        });

        fnEncodingBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean show = fnEncodingBtn.getSelection();
                encodeCombo.setHideWidgets(!show);
                customText.setVisible(show && CUSTOM.equals(getConnection().getEcoding()));
                encodingComp.layout();
                getConnection().setUseFileNameEncoding(show && getConnection().isSFTP());
            }
        });
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.swt.utils.AbstractForm#checkFieldsValue()
     */

    @Override
    public boolean checkFieldsValue() {
        if (ftpHostText.getCharCount() == 0) {
            updateStatus(IStatus.ERROR, Messages.getString("FTPForm_ftpHostText_check")); //$NON-NLS-1$
            return false;
        }

        if (ftpPortText.getCharCount() == 0) {
            updateStatus(IStatus.ERROR, Messages.getString("FTPForm_ftpPortText_check")); //$NON-NLS-1$
            return false;
        }

        if (ftpUsernameText.getCharCount() == 0) {
            updateStatus(IStatus.ERROR, Messages.getString("FTPForm_ftpUsernameText_check")); //$NON-NLS-1$
            return false;
        }
        
        try {
            Long.parseLong(connTimeoutText.getText());
        } catch (NumberFormatException e) {
            updateStatus(IStatus.ERROR, Messages.getString("FTPForm_ftpTimeoutText_check")); //$NON-NLS-1$
            return false;
        }

        // only check the Password model
        if (sftpSuppBut.getSelection() && methodCombo != null && PASSWORD.equals(methodCombo.getText())) {
            if (ftpPasswordText.getCharCount() == 0) {
                updateStatus(IStatus.ERROR, Messages.getString("FTPForm_ftpPasswordText_check")); //$NON-NLS-1$
                return false;
            }
        }

        if (sftpSuppBut.getSelection() && methodCombo != null && PUBLIC_KEY.equals(methodCombo.getText())
                && privatekeyText.getText() == "") { //$NON-NLS-1$
            updateStatus(IStatus.ERROR, Messages.getString("FTPForm_filepathAlert")); //$NON-NLS-1$
            return false;
        }

        if (ftpsSuppBut.getSelection() && keyFileText.getText() == "") { //$NON-NLS-1$
            updateStatus(IStatus.ERROR, Messages.getString("FTPForm_filepathAlert")); //$NON-NLS-1$
            return false;
        }

        updateStatus(IStatus.OK, null);
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.swt.utils.AbstractForm#initialize()
     */

    @Override
    protected void initialize() {
        FTPConnection conn = getConnection();
        ftpUsernameText.setText(conn.getUsername());
        // decrypt password
        ftpPasswordText.setText(conn.getValue(conn.getPassword(), false));
        ftpPortText.setText(conn.getPort());
        ftpHostText.setText(conn.getHost());
        encodeCombo.setText(conn.getEcoding());
        
        if(conn.getTimeout() == null || "".equals(conn.getTimeout())){
            connTimeoutText.setText("0");
        }else {
            connTimeoutText.setText(String.valueOf(conn.getTimeout()));
        }
        
        if (CUSTOM.equals(encodeCombo.getText())) {
            customText.setVisible(true);
        } else {
            customText.setVisible(false);
        }
        if (conn.getCustomEncode() == null) {
            customText.setText(ENCODING);
        } else {
            customText.setText(conn.getCustomEncode());
        }
        connModelCombo.setText(conn.getMode());
        if (conn.isSFTP()) {
            fnEncodingBtn.setSelection(conn.isUseFileNameEncoding());
            sftpChildComGridData.exclude = false;
            ftpsChildComGridData.exclude = true;
            fnEncodingBtnGD.exclude = false;
            fnEncodingBtn.setVisible(true);
            sftpChildCom.setVisible(true);
            ftpsChildCom.setVisible(false);
            encodeCombo.setHideWidgets(!fnEncodingBtn.getSelection());
            encodingComp.layout();
            sftpChildCom.layout();
            ftpsChildCom.layout();
            tetsCom.layout();
            buildGroup.layout();
            connModelCombo.setEnabled(false);
            sftpSuppBut.setSelection(conn.isSFTP());
            methodCombo.setText(conn.getMethod());
            if (PUBLIC_KEY.equals(conn.getMethod())) {
                privatekeyText.setVisible(true);
                passphraseText.setVisible(true);
                privatekeyText.setText(conn.getPrivatekey() != null ? conn.getPrivatekey() : "");//$NON-NLS-1$
                // decrypt password
                passphraseText.setText(conn.getValue(conn.getPassphrase(), false));
            } else {
                privatekeyText.setVisible(false);
                passphraseText.setVisible(false);
            }
        } else {
            sftpChildComGridData.exclude = true;
            fnEncodingBtnGD.exclude = true;
            encodeCombo.setHideWidgets(false);
            fnEncodingBtn.setVisible(false);
            sftpChildCom.setVisible(false);
            sftpChildCom.layout();
            encodingComp.layout();
            tetsCom.layout();
            buildGroup.layout();
        }
        if (conn.isFTPS()) {
            ftpsChildComGridData.exclude = false;
            sftpChildComGridData.exclude = true;
            sftpChildCom.setVisible(false);
            ftpsChildCom.setVisible(true);
            sftpChildCom.layout();
            ftpsChildCom.layout();
            tetsCom.layout();
            buildGroup.layout();
            connModelCombo.setEnabled(false);
            ftpsSuppBut.setSelection(conn.isFTPS());
            keyFileText.setText(conn.getKeystoreFile());
            // decrypt password
            keyPasswordText.setText(conn.getValue(conn.getKeystorePassword(), false));
        } else {
            ftpsChildComGridData.exclude = true;
            ftpsChildCom.setVisible(false);
            ftpsChildCom.layout();
            tetsCom.layout();
            buildGroup.layout();
        }
        if (conn.isUsesocks()) {
            proxyChildComGridData.exclude = false;
            proxyChildCom.setVisible(true);
            proxyChildCom.layout();
            proxyCom.layout();
            buildGroup.layout();
            this.layout();
            useSocksBut.setSelection(conn.isUsesocks());
            proxyHostText.setText(conn.getProxyhost());
            proxyPortText.setText(conn.getProxyport());
            proxyUsernameText.setText(conn.getProxyuser());
            // decrypt password
            proxyPasswordText.setText(conn.getValue(conn.getProxypassword(), false));
        } else {
            proxyChildComGridData.exclude = true;
            proxyChildCom.setVisible(false);
            proxyChildCom.layout();
            proxyCom.layout();
            buildGroup.layout();
            this.layout();
        }
    }

    public void removeHideValue() {
        FTPConnection conn = getConnection();
        if (!connModelCombo.getCombo().isVisible()) {
            conn.setMode(""); //$NON-NLS-1$
        }
        if (!sftpSuppBut.isVisible()) {
            conn.setSFTP(false);
        }
        if (!ftpsSuppBut.isVisible()) {
            conn.setFTPS(false);
        }
        if (!useSocksBut.isVisible()) {
            conn.setUsesocks(false);
        }
        if (!fnEncodingBtn.isVisible()) {
            conn.setUseFileNameEncoding(false);
        }
        if (!methodCombo.getCombo().isVisible()) {
            conn.setMethod(""); //$NON-NLS-1$
        }
        if (!keyPasswordText.getTextControl().isVisible()) {
            conn.setKeystorePassword(""); //$NON-NLS-1$
        }
        if (!keyFileText.getTextControl().isVisible()) {
            conn.setKeystoreFile(""); //$NON-NLS-1$
        }
        if (!proxyHostText.getTextControl().isVisible()) {
            conn.setProxyhost(""); //$NON-NLS-1$
        }
        if (!proxyPortText.getTextControl().isVisible()) {
            conn.setProxyport(""); //$NON-NLS-1$
        }
        if (!proxyPasswordText.getTextControl().isVisible()) {
            conn.setProxypassword(""); //$NON-NLS-1$
        }
        if (!proxyUsernameText.getTextControl().isVisible()) {
            conn.setProxyuser(""); //$NON-NLS-1$
        }
        if (!customText.isVisible()) {
            if (conn.getCustomEncode() == null || "".equals(conn.getCustomEncode())) { //$NON-NLS-1$
                conn.setCustomEncode(ENCODING);
            }
        }
        if (conn.getCustomEncode() == null || "".equals(conn.getCustomEncode())) { //$NON-NLS-1$
            conn.setCustomEncode(ENCODING);
        }
    }

    protected FTPConnection getConnection() {
        return (FTPConnection) connectionItem.getConnection();
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        checkFieldsValue();
    }

    /**
     * checkFileFieldsValue active fileViewer if file exist.
     */
    private void checkFilePathAndManageIt() {
        updateStatus(IStatus.OK, null);
        String fileStr = privatekeyText.getText();
        checkFieldsValue();
    }
}
