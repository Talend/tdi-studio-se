// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.service.ICommandLineService;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * created by hcyi on May 9, 2018
 * Detailled comment
 *
 */
public class AuditProjectSettingPage extends ProjectSettingPage {

    private Button button;

    private String generatePath;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout(4, false);
        composite.setLayout(layout);

        button = new Button(composite, SWT.NONE);
        button.setText(Messages.getString("AuditProjectSettingPage.generateAuditReport")); //$NON-NLS-1$
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (factory.isUserReadOnlyOnCurrentProject()) {
            composite.setEnabled(false);
        }
        addListeners();
        return composite;
    }

    private void addListeners() {
        button.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog dial = new DirectoryDialog(getShell(), SWT.NONE);
                String directory = dial.open();
                if (StringUtils.isNotEmpty(directory)) {
                    generatePath = Path.fromOSString(directory).toPortableString();
                    generatePath += "/"; //$NON-NLS-1$
                } else {
                    MessageDialog.openError(getShell(), "Error", //$NON-NLS-1$
                            Messages.getString("AuditProjectSettingPage.selectAuditReportFolder")); //$NON-NLS-1$
                    return;
                }

                ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(
                        PlatformUI.getWorkbench().getDisplay().getActiveShell().getShell());
                IRunnableWithProgress runnable = new IRunnableWithProgress() {

                    @Override
                    public void run(IProgressMonitor monitor) {
                        monitor.beginTask(Messages.getString("AuditProjectSettingPage.generateAuditReportProgressBar"), //$NON-NLS-1$
                                IProgressMonitor.UNKNOWN);
                        Display.getDefault().syncExec(new Runnable() {

                            @Override
                            public void run() {
                                if (GlobalServiceRegister.getDefault().isServiceRegistered(ICommandLineService.class)) {
                                    ICommandLineService service = (ICommandLineService) GlobalServiceRegister.getDefault()
                                            .getService(ICommandLineService.class);
                                    String path = "";//$NON-NLS-1$
                                    File tempFolder = null;
                                    try {
                                        File createTempFile = File.createTempFile("AuditReport", ""); //$NON-NLS-1$ //$NON-NLS-2$
                                        path = createTempFile.getPath();
                                        createTempFile.delete();
                                        tempFolder = new File(path);
                                        tempFolder.mkdir();
                                        path = path.replace("\\", "/");//$NON-NLS-1$//$NON-NLS-2$

                                        // Just use the h2 as default now, later will add support for others
                                        service.populateAudit(
                                                "jdbc:h2:" + path + "/database/audit;AUTO_SERVER=TRUE;lock_timeout=15000", //$NON-NLS-1$ //$NON-NLS-2$
                                                "org.h2.Driver", "tisadmin", "tisadmin"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                        service.generateAuditReport(generatePath);
                                    } catch (IOException e) {
                                        // nothing
                                    } finally {
                                        FilesUtils.deleteFile(tempFolder, true);
                                    }
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
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.PreferencePage#performApply()
     */
    @Override
    protected void performApply() {
        super.performApply();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        return super.performOk();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.preference.ProjectSettingPage#refresh()
     */
    @Override
    public void refresh() {
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();
    }
}
