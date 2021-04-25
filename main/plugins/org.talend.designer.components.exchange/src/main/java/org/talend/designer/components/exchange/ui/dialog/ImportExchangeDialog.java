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
package org.talend.designer.components.exchange.ui.dialog;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.download.DownloadHelper;
import org.talend.core.runtime.util.SharedStudioUtils;
import org.talend.designer.components.exchange.i18n.Messages;
import org.talend.designer.components.exchange.model.Category;
import org.talend.designer.components.exchange.model.VersionRevision;
import org.talend.designer.components.exchange.util.ExchangeUtils;
import org.talend.designer.components.exchange.util.ExchangeWebService;

/**
 * hcyi A dialog for user to select filters that will be used to search components.
 *
 * This dialog will open when Browse Talend Exchange button push.
 */
public class ImportExchangeDialog extends Dialog {

    private List<Category> fCategorys = new ArrayList<Category>();

    private List<VersionRevision> fVersionRevisions = new ArrayList<VersionRevision>();

    private ImportExchangeProperty downloadproperty =new ImportExchangeProperty();

    private String selectFile;

    private URL url;

    private File tempFile;

    protected ImportExchangeDialog(Shell shell) {
        super(shell);
        this.setShellStyle(this.getShellStyle() | SWT.MIN | SWT.MAX | SWT.RESIZE);
        // init
        Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {
                fCategorys.clear();
                fCategorys = ExchangeWebService.searchCategoryExtensionJSONArray(ExchangeUtils.TYPEEXTENSION);
                fVersionRevisions.clear();
                fVersionRevisions = ExchangeWebService.searchVersionRevisionJSONArray(ExchangeUtils.TYPEEXTENSION);
            }
        });
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.getString("ImportExchangeDialog.dialogTitle")); //$NON-NLS-1$
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite exchangeDialogCom = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.makeColumnsEqualWidth = false;
        layout.marginWidth = 0;
        exchangeDialogCom.setLayout(layout);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        exchangeDialogCom.setLayoutData(gridData);
        new ImportCompatibleEcoComponentsComposite(exchangeDialogCom, exchangeDialogCom.getShell(), false, fCategorys, fVersionRevisions, downloadproperty);
        return parent;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.FINISH_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    @Override
    protected void okPressed() {
        IPath tempPath = SharedStudioUtils.getTempFolderPath();
        File pathFile = tempPath.toFile();
        if (downloadproperty.getFileName() == null || downloadproperty.getFileName() == null) {
            MessageBox box = new MessageBox(Display.getCurrent().getActiveShell(), SWT.ICON_WARNING | SWT.OK);
            box.setText(Messages.getString("ImportExchangeDialog.WARNING")); //$NON-NLS-1$
            box.setMessage(Messages.getString("ImportExchangeDialog.NOTSELECTWARNING")); //$NON-NLS-1$
            box.open();
            return;
        }
        tempFile = new File(pathFile, downloadproperty.getFileName());
        try {
            url = new URL(downloadproperty.getDownloadUrl());
        } catch (MalformedURLException e1) {
            ExceptionHandler.process(e1);
        }
        if (!pathFile.exists()) {
            pathFile.mkdirs();
        }

        ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell()
                .getShell());
        IRunnableWithProgress runnable = new IRunnableWithProgress() {

            @Override
            public void run(IProgressMonitor monitor) {
                monitor.beginTask(Messages.getString("ImportExchangeDialog.downloadProgressBar"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
                Display.getDefault().syncExec(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            new DownloadHelper().download(url, tempFile);
                        } catch (Exception e) {
                            ExceptionHandler.process(e);
                        }
                    }
                });
                monitor.done();
            }
        };
        try {
            progressDialog.run(true, true, runnable);
        } catch (InvocationTargetException e1) {
            ExceptionHandler.process(e1);
        } catch (InterruptedException e1) {
            ExceptionHandler.process(e1);
        }

        selectFile = tempFile.toString();
        super.okPressed();
    }

    public String getSelectFile() {
        return selectFile;
    }

    public void setSelectFile(String selectFile) {
        this.selectFile = selectFile;
    }
}
