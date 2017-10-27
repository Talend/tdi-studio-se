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
package org.talend.componentdesigner.exception;

import org.apache.log4j.Level;
import org.apache.log4j.Priority;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.talend.componentdesigner.i18n.internal.Messages;

/**
 * Exception handling via message box.<br/>
 * 
 * $Id: MessageBoxExceptionHandler.java 3889 2007-06-17 18:14:27 +0000 (星期日, 17 六月 2007) mhelleboid $
 * 
 */
public final class MessageBoxExceptionHandler {

    private static Throwable lastShowedAction;

    /**
     * Empty constructor.
     */
    private MessageBoxExceptionHandler() {
    }

    /**
     * Log the exeption then open a message box showing a generic message and exception message.
     * 
     * @param ex - exception to log
     */
    public static void process(Throwable ex) {
        process(ex, null);
    }

    public static void process(Throwable ex, Shell shell) {
        ExceptionHandler.process(ex);

        if (shell == null) {
            try {
                shell = new Shell();
            } catch (Exception e) {
                // ignore me
            }
        }

        if (shell != null) {
            showMessage(ex, shell);
        }
    }

    /**
     * Open a message box showing a generic message and exception message.
     * 
     * @param ex - exception to show
     */
    protected static void showMessage(Throwable ex, Shell shell) {
        if (ex.equals(lastShowedAction)) {
            return;
        }
        lastShowedAction = ex;

        // TODO smallet use ErrorDialogWidthDetailArea ?
        String title = Messages.getString("commons.error"); //$NON-NLS-1$
        String msg = Messages.getString("exception.errorOccured", ex.getMessage()); //$NON-NLS-1$
        Priority priority = Level.ERROR;

        if (priority == Level.FATAL || priority == Level.ERROR) {
            MessageDialog.openError(shell, title, msg);
        } else if (priority == Level.WARN) {
            MessageDialog.openWarning(shell, title, msg);
        } else if (priority == Level.INFO) {
            MessageDialog.openInformation(shell, title, msg);
        } else {
            MessageDialog.openInformation(shell, title, msg);
        }
    }
}
