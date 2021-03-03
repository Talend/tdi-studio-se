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
package org.talend.designer.webservice;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.core.model.components.IComponent;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.webservice.data.ExternalWebServiceUIProperties;
import org.talend.designer.webservice.managers.WebServiceManager;
import org.talend.designer.webservice.ui.dialog.WebServiceDialog;

/**
 * gcui class global comment. Detailled comment
 */
public class WebServiceComponentMain {

    private WebServiceComponent connector;

    private WebServiceManager webServiceManager;

    private WebServiceDialog dialog;

    public WebServiceDialog getDialog() {
        return this.dialog;
    }

    public WebServiceComponentMain(WebServiceComponent connector) {
        super();
        this.connector = connector;
        this.webServiceManager = new WebServiceManager(connector, 0);
    }

    public void loadInitialParamters() {
        //
    }

    public Dialog createDialog(Shell parentShell) {
        dialog = new WebServiceDialog(parentShell, this);
        if (EmfComponent.REPOSITORY
                .equals(this.connector.getElementParameter(EParameterName.PROPERTY_TYPE.getName()).getValue())) {
            dialog.setRepositoryMode(true);
        }
        IComponent component = connector.getComponent();
        dialog.setTitle("Talend Integration Suite - " + connector.getUniqueName());

        Rectangle boundsMapper = ExternalWebServiceUIProperties.getBoundsMapper();
        if (ExternalWebServiceUIProperties.isShellMaximized()) {
            dialog.setMaximized(ExternalWebServiceUIProperties.isShellMaximized());
        } else {
            boundsMapper = ExternalWebServiceUIProperties.getBoundsMapper();
            if (boundsMapper.x < 0) {
                boundsMapper.x = 0;
            }
            if (boundsMapper.y < 0) {
                boundsMapper.y = 0;
            }
            dialog.setSize(boundsMapper);
        }
        dialog.open();
        return dialog;
    }

    public void createUI(Display display) {
        Shell shell = DisplayUtils.getDefaultShell(false);
        createDialog(shell);
    }

    public WebServiceManager getWebServiceManager() {
        return this.webServiceManager;
    }

    public int getDialogResponse() {
        return webServiceManager.getUIManager().getDialogResponse();
    }

    public WebServiceComponent getWebServiceComponent() {
        return this.connector;
    }
}
