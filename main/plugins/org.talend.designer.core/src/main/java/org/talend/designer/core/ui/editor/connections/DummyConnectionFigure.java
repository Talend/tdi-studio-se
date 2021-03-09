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
package org.talend.designer.core.ui.editor.connections;

import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.swt.graphics.Color;
import org.talend.commons.ui.utils.image.ColorUtils;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionProperty;
import org.talend.core.model.process.INode;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.ui.preferences.TalendDesignerPrefConstants;
import org.talend.designer.core.utils.ResourceDisposeUtil;

/**
 * DOC hwang class global comment. Detailled comment
 */
public class DummyConnectionFigure extends PolylineConnection {

    /**
     * Used for standard connections.
     *
     * @param connection
     * @param connectionProperty
     * @param node
     */
    public DummyConnectionFigure(IConnection connection, IConnectionProperty connectionProperty, INode node) {
        setConnectionProperty(connectionProperty);
    }

    protected void setConnectionProperty(IConnectionProperty connectionProperty) {
        if (connectionProperty == null) {
            return;
        }
        setLineStyle(connectionProperty.getLineStyle());
        Color color = ColorUtils.getCacheColor(connectionProperty.getRGB());
        ResourceDisposeUtil.setAndCheckColor(this, color, true);
        if (!DesignerPlugin.getDefault().getPreferenceStore().getBoolean(TalendDesignerPrefConstants.EDITOR_LINESTYLE)) {
            this.setLineWidth(1);
        } else {
            this.setLineWidth(2);
        }
    }

    public void disposeColors() {
        // ResourceDisposeUtil.disposeColor(getForegroundColor());
    }

}
