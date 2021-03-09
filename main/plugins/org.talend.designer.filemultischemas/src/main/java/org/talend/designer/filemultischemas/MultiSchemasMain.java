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
package org.talend.designer.filemultischemas;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.commons.ui.runtime.image.ImageUtils.ICON_SIZE;
import org.talend.core.model.components.IComponent;
import org.talend.core.ui.images.CoreImageProvider;
import org.talend.designer.filemultischemas.data.ExternalMultiSchemasUIProperties;
import org.talend.designer.filemultischemas.managers.MultiSchemasManager;
import org.talend.designer.filemultischemas.ui.dialog.MultiSchemaDialog;

/**
 * cLi class global comment. Detailled comment
 */
public class MultiSchemasMain {

    private MultiSchemasComponent connector;

    private MultiSchemasManager multiSchemaManager;

    public MultiSchemasMain(MultiSchemasComponent connector) {
        super();
        this.connector = connector;
        this.multiSchemaManager = new MultiSchemasManager(connector, 0);// add the parameter for feature 7373
    }

    public void loadInitialParamters() {
        //
    }

    public Dialog createDialog(Shell parentShell) {
        MultiSchemaDialog dialog = new MultiSchemaDialog(parentShell, this);

        IComponent component = connector.getComponent();
        dialog.setIcon(CoreImageProvider.getComponentIcon(component, ICON_SIZE.ICON_32));
        dialog.setTitle("Talend Open Studio - " + connector.getUniqueName());

        Rectangle boundsMapper = ExternalMultiSchemasUIProperties.getBoundsMapper();
        if (ExternalMultiSchemasUIProperties.isShellMaximized()) {
            dialog.setMaximized(ExternalMultiSchemasUIProperties.isShellMaximized());
            dialog.setSize(boundsMapper);
            parentShell.setBounds(boundsMapper);
        } else {
            boundsMapper = ExternalMultiSchemasUIProperties.getBoundsMapper();
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

    public MultiSchemasManager getMultiSchemaManager() {
        return this.multiSchemaManager;
    }

    public int getDialogResponse() {
        return multiSchemaManager.getUIManager().getDialogResponse();
    }
}
