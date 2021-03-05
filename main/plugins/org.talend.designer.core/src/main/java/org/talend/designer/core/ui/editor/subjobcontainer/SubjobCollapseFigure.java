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
package org.talend.designer.core.ui.editor.subjobcontainer;

import org.eclipse.swt.graphics.Image;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.designer.core.ui.editor.connections.CollapseFigure;


/**
 * DOC nrousseau  class global comment. Detailled comment
 */
public class SubjobCollapseFigure extends CollapseFigure {

    /* (non-Javadoc)
     * @see org.talend.designer.core.ui.editor.connections.CollapseFigure#getCollapseFigure()
     */
    @Override
    protected Image getCollapseFigure() {
        return ImageProvider.getImage(EImage.SUBJOB_COLLAPSE);
    }

    /* (non-Javadoc)
     * @see org.talend.designer.core.ui.editor.connections.CollapseFigure#getExpandFigure()
     */
    @Override
    protected Image getExpandFigure() {
        return ImageProvider.getImage(EImage.SUBJOB_EXPAND);
    }

}
