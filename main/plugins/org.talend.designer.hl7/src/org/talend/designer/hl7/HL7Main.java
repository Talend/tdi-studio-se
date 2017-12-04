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
package org.talend.designer.hl7;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.ui.runtime.image.ImageUtils.ICON_SIZE;
import org.talend.core.model.components.IComponent;
import org.talend.core.ui.images.CoreImageProvider;
import org.talend.designer.hl7.managers.HL7Manager;
import org.talend.designer.hl7.managers.HL7OutputManager;
import org.talend.designer.hl7.ui.HL7MultiSchemaUI;
import org.talend.designer.hl7.ui.HL7OutputLinkUI;
import org.talend.designer.hl7.ui.HL7UI;

/**
 * This class is responsible for contacting the RowGeneratorComponent and FOXUI. <br/>
 * 
 * $Id: FOXMain.java,v 1.1 2007/06/12 07:20:38 gke Exp $
 * 
 */
public class HL7Main {

    private boolean standAloneMode = false;

    private HL7UI generatorUI;

    private HL7InputComponent connector;

    private HL7Manager hl7Manager;

    public HL7Main(HL7InputComponent connector) {
        this.connector = connector;
        if (connector.isHL7Output()) {
            this.hl7Manager = new HL7OutputManager(connector);
        } else {
            this.hl7Manager = new HL7Manager(connector);
        }
    }

    /**
     * create UI".
     * 
     * @param parent
     * @return
     */
    public void createUI(Composite parent) {
        if (connector.isHL7Output()) {
            generatorUI = new HL7OutputLinkUI(parent, this.hl7Manager);
            generatorUI.init();
            return;
        }
        generatorUI = new HL7MultiSchemaUI(parent, hl7Manager);
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

    public HL7Manager getHl7Manager() {
        return this.hl7Manager;
    }

}
