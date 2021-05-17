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
package org.talend.designer.fileoutputxml;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.ui.runtime.image.ImageUtils.ICON_SIZE;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;
import org.talend.core.ui.images.CoreImageProvider;
import org.talend.designer.fileoutputxml.i18n.Messages;
import org.talend.designer.fileoutputxml.managers.FOXManager;
import org.talend.designer.fileoutputxml.managers.MultiFOXManager;
import org.talend.designer.fileoutputxml.ui.FOXMultiSchemaUI;
import org.talend.designer.fileoutputxml.ui.FOXUI;

/**
 * This class is responsible for contacting the RowGeneratorComponent and FOXUI. <br/>
 *
 * $Id: FOXMain.java,v 1.1 2007/06/12 07:20:38 gke Exp $
 *
 */
public class FOXMain {

    private boolean standAloneMode = false;

    private FOXUI generatorUI;

    private FileOutputXMLComponent connector;

    private FOXManager foxManager;

    public FOXMain(FileOutputXMLComponent connector) {
        this.connector = connector;
        // add by wzhang. for component tFileOutputMSXML
        if (connector.istFileOutputMSXML()) {
            this.foxManager = new MultiFOXManager(connector);
        } else {
            this.foxManager = new FOXManager(connector);
        }
    }

    /**
     * create UI".
     *
     * @param parent
     * @return
     */
    public void createUI(Composite parent) {
        if (!connector.istWriteXMLField() && !connector.istMDMOutput()) {
            IConnection inConn = null;
            List<? extends IConnection> incomeConnections = connector.getIncomingConnections();
            if (connector.istFileOutputMSXML() && incomeConnections.size() < 1) {
                MessageBox message = new MessageBox(parent.getShell(), SWT.APPLICATION_MODAL | SWT.OK);
                message.setText(Messages.getString("FOXMain.inputlinkError")); //$NON-NLS-1$
                message.setMessage(Messages.getString("FOXMain.noInputLink")); //$NON-NLS-1$
                if (message.open() == SWT.OK) {
                    ((Shell) parent).close();
                    return;
                }
                for (IConnection conn : incomeConnections) {
                    if ((conn.getLineStyle().equals(EConnectionType.FLOW_MAIN))
                            || (conn.getLineStyle().equals(EConnectionType.FLOW_REF))) {
                        inConn = conn;
                        break;
                    }
                }
                if (inConn != null) {
                    if (!inConn.getMetadataTable().sameMetadataAs(connector.getMetadataList().get(0))) {
                        MessageBox messageBox = new MessageBox(parent.getShell(), SWT.APPLICATION_MODAL | SWT.OK);
                        messageBox.setText(Messages.getString("FOXMain.0")); //$NON-NLS-1$
                        messageBox.setMessage(Messages.getString("FOXMain.1")); //$NON-NLS-1$
                        if (messageBox.open() == SWT.OK) {
                            ((Shell) parent).close();
                            return;
                        }
                    }
                }
            }
        }
        // add by wzhang. for component tFileOutputMSXML
        if (connector.istFileOutputMSXML()) {
            generatorUI = new FOXMultiSchemaUI(parent, foxManager);
        } else {
            generatorUI = new FOXUI(parent, foxManager);
        }
        generatorUI.init();

    }

    /**
     * qzhang Comment method "createUI".
     *
     * @param display
     * @return
     */
    public Shell createUI(Display display) {
        Shell shell = new Shell(display, SWT.APPLICATION_MODAL | SWT.BORDER | SWT.RESIZE | SWT.CLOSE | SWT.MIN | SWT.MAX
                | SWT.TITLE);
        IComponent component = connector.getComponent();
        Image createImage = CoreImageProvider.getComponentIcon(component, ICON_SIZE.ICON_32);
        shell.setImage(createImage);
        shell.setText(connector.getUniqueName());
        Rectangle boundsRG = new Rectangle(50, 50, 800, 600);
        shell.setBounds(boundsRG);
        createUI(shell);
        if (!shell.isDisposed()) {
            shell.open();
        }
        return shell;
    }

    /**
     * Getter for standAloneMode.
     *
     * @return the standAloneMode
     */
    public boolean isStandAloneMode() {
        return this.standAloneMode;
    }

    /**
     * Sets the standAloneMode.
     *
     * @param standAloneMode the standAloneMode to set
     */
    public void setStandAloneMode(boolean standAloneMode) {
        this.standAloneMode = standAloneMode;
    }

    public FOXManager getFoxManager() {
        return foxManager;
    }

}
