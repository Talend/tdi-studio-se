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
package org.talend.designer.alfrescooutput;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.core.model.process.AbstractNode;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;
import org.talend.designer.alfrescooutput.data.AlfrescoOutputModelManager;
import org.talend.designer.alfrescooutput.i18n.Messages;
import org.talend.designer.alfrescooutput.ui.AlfrescoModelDialog;
import org.talend.designer.alfrescooutput.util.AlfrescoOutputException;


/**
 * This class holds the model and UI managers on behalf of the component.
 *
 * @author Marc Dutoo - Open Wide SA
 *
 * $Id: AlfrescoOutputManager.java,v 1.1 2008/10/07 21:27:31 mdutoo Exp $
 */
public class AlfrescoOutputManager {

	private AlfrescoOutputComponent alfrescoOutputComponent;
	private AlfrescoOutputModelManager modelManager;
	private AlfrescoModelDialog alfrescoModelDialog = null;


	public AlfrescoOutputManager(AlfrescoOutputComponent alfrescoOutputComponent) {
		this.alfrescoOutputComponent = alfrescoOutputComponent;
        modelManager = new AlfrescoOutputModelManager(alfrescoOutputComponent);
	}


	public AlfrescoOutputComponent getAlfrescoOutputComponent() {
		return alfrescoOutputComponent;
	}

	public AlfrescoOutputModelManager getModelManager() {
		return modelManager;
	}

    public AlfrescoModelDialog getAlfrescoModelDialog() {
		return alfrescoModelDialog;
	}


    /**
     * Checks the connections and creates the UI (a dialog actually)
     *
     * @param parent
     * @return
     */
    public AlfrescoModelDialog createUI(Composite parent) {
        IConnection inConn = null;
        AbstractNode connector = this.alfrescoOutputComponent;
		for (IConnection conn : connector .getIncomingConnections()) {
            if ((conn.getLineStyle().equals(EConnectionType.FLOW_MAIN))
                    || (conn.getLineStyle().equals(EConnectionType.FLOW_REF))) {
                inConn = conn;
                break;
            }
        }
        if (inConn != null && inConn.getMetadataTable() != null) {
            if (!inConn.getMetadataTable().sameMetadataAs(connector.getMetadataList().get(0))) {
                MessageBox messageBox = new MessageBox(parent.getShell(), SWT.APPLICATION_MODAL | SWT.OK);
                messageBox.setText(Messages.getString("AlfrescoOutputManager.schemaError.title")); //$NON-NLS-1$
                messageBox.setMessage(Messages.getString("AlfrescoOutputManager.schemaError.msg")); //$NON-NLS-1$
                if (messageBox.open() == SWT.OK) {
                    ((Shell) parent).close();
                    return null;
                }
            }
        }

        // first load the model :
        try {
			modelManager.load(); // NB. or when modelManager is created
		} catch (AlfrescoOutputException aoex) {
            MessageDialog.openError(DisplayUtils.getDefaultShell(false),
					Messages.getString("AlfrescoOutputManager.failedLoadModel"), aoex.getMessage()); //$NON-NLS-1$
			modelManager.clear();
		}

        // then create and open the model dialog :
		AlfrescoModelDialog alfrescoModelDialog = new AlfrescoModelDialog(parent.getShell(), this);
        alfrescoModelDialog.open();
        // NB. this dialog is non-blocking ; model save is done in its okPressed()
        return alfrescoModelDialog;
    }

    /**
     * Checks the connections and creates the UI (a dialog actually)
     *
     * @param display
     * @return
     */
    public Shell createUI(Display display) {
        this.alfrescoModelDialog = createUI(display.getActiveShell());
        return this.alfrescoModelDialog.getShell();
    }

}
