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
package org.talend.sdk.component.studio.ui.composite.problemmanager;

import java.util.Set;

import org.apache.commons.collections.BidiMap;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.ui.runtime.ColorConstants;
import org.talend.core.model.process.IElementParameter;
import org.talend.sdk.component.studio.ui.composite.TaCoKitComposite;
import org.talend.sdk.component.studio.util.TaCoKitUtil;

/**
 * DOC cmeng  class global comment. Detailled comment
 */
public class ColorfulProblemManager extends ProblemManager {

    private static Color COLOR_ERROR = ColorConstants.RED_COLOR;

    private static Color COLOR_WARN = ColorConstants.YELLOW_COLOR;

    private static Color COLOR_INFO = null;

    private static Color COLOR_WHITE = org.eclipse.draw2d.ColorConstants.white;

    @Override
    public void setError(IElementParameter ep, String errMsg) {
        super.setError(ep, errMsg);
        if (TaCoKitUtil.isBlank(errMsg)) {
            setColor(ep, null, null, null);
        } else {
            setColor(ep, COLOR_ERROR, COLOR_WHITE, errMsg);
        }
    }

    @Override
    public void setWarn(IElementParameter ep, String warnMsg) {
        super.setWarn(ep, warnMsg);
        if (TaCoKitUtil.isBlank(warnMsg)) {
            setColor(ep, null, null, null);
        } else {
            setColor(ep, COLOR_WARN, COLOR_WHITE, warnMsg);
        }
    }

    @Override
    public void setInfo(IElementParameter ep, String infoMsg) {
        super.setInfo(ep, infoMsg);
        if (TaCoKitUtil.isBlank(infoMsg)) {
            setColor(ep, null, null, null);
        } else {
            setColor(ep, COLOR_INFO, null, infoMsg);
        }
    }

    protected boolean cleanToolTipText() {
        return true;
    }

    private void setColor(IElementParameter ep, Color backgroundColor, Color forgroundColor, String msg) {
        Display.getDefault().asyncExec(() -> {
            Set<TaCoKitComposite> tckComposites = getTckComposites();
            for (TaCoKitComposite tckComposite : tckComposites) {
                if (tckComposite.isDisposed() || !tckComposite.isVisible()) {
                    continue;
                }
                BidiMap hashCurControls = tckComposite.getHashCurControls();
                if (hashCurControls != null) {
                    Object object = hashCurControls.get(ep.getName());
                    if (object instanceof Control) {
                        Control ctrl = (Control) object;
                        if (!ctrl.isDisposed()) {
                            ctrl.setBackground(backgroundColor);
                            ctrl.setForeground(forgroundColor);
                            if (!TaCoKitUtil.isBlank(msg) || cleanToolTipText()) {
                                ctrl.setToolTipText(msg);
                            }
                        }
                    }
                }
            }
        });
    }

}
