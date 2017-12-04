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
package org.talend.componentdesigner.ui.progress;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;

/**
 * DOC rli  class global comment. Detailled comment
 * <br/>
 *
 */
public class ProgressUI {

	public static void popProgressDialog(IRunnableWithProgress runnable,
			Shell shell) throws InvocationTargetException, InterruptedException {
		// ProgressMonitorDialog dialog = new ProgressMonitorJobsDialog(this.getViewSite().getShell());
		final ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
		dialog.run(true, true, runnable);
	}
}
