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
package org.talend.designer.dbmap.external.data;

import java.io.Serializable;

import org.eclipse.swt.graphics.Rectangle;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: ExternalMapperUiProperties.java 34 2006-10-02 15:08:32Z amaumont $
 *
 */
public class ExternalDbMapUiProperties implements Serializable, Cloneable {

    /**
     *
     */
    private static final long serialVersionUID = -6720565045409231632L;

    // must be null by default to unmarshal correctly
    private int[] weightsMainSashForm = new int[0];

    public static final int[] DEFAULT_WEIGHTS_MAIN_SASH_FORM = new int[] { 70, 30 };

    // must be null by default to unmarshal correctly
    private int[] weightsDatasFlowViewSashForm = new int[0];

    public static final int[] DEFAULT_WEIGHTS_DATAS_FLOW_SASH_FORM = new int[] { 33, 33, 34 };

    private Rectangle boundsMapper = new Rectangle(50, 50, 800, 600);

    private boolean shellMaximized = false;

    public Rectangle getBoundsMapper() {
        return this.boundsMapper;
    }

    public void setBoundsMapper(Rectangle boundsMapper) {
        this.boundsMapper = boundsMapper;
    }

    public boolean isShellMaximized() {
        return this.shellMaximized;
    }

    public void setShellMaximized(boolean shellMaximized) {
        this.shellMaximized = shellMaximized;
    }

    public int[] getWeightsDatasFlowViewSashForm() {
        return this.weightsDatasFlowViewSashForm;
    }

    /**
     * Setter for array of exactly three values which represents width pixels of each zone (input, var and output).
     *
     * @param weightsDatasFlowViewSashForm array of three values which represents width pixels of each zone (input, var
     * and output)
     */
    public void setWeightsDatasFlowViewSashForm(int[] weightsDatasFlowViewSashForm) {
        // if (weightsDatasFlowViewSashForm.length != 3) {
        // throw new IllegalArgumentException("weightsDatasFlowViewSashForm must be an array of exactly 3 values");
        // }
        this.weightsDatasFlowViewSashForm = weightsDatasFlowViewSashForm;
    }

    public int[] getWeightsMainSashForm() {
        return this.weightsMainSashForm;
    }

    /**
     * Setter for array of exactly two values which represents height pixels of each zone (flow view, tabs view).
     *
     * @param weightsMainSashForm
     */
    public void setWeightsMainSashForm(int[] weightsMainSashForm) {
        // if (weightsMainSashForm.length != 2) {
        // throw new IllegalArgumentException("weightsMainSashForm must be an array of exactly 2 values");
        // }
        this.weightsMainSashForm = weightsMainSashForm;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        ExternalDbMapUiProperties cloned = (ExternalDbMapUiProperties) super.clone();
        cloned.boundsMapper = new Rectangle(this.boundsMapper.x, this.boundsMapper.y, this.boundsMapper.width,
                this.boundsMapper.height);
        cloned.weightsMainSashForm = weightsMainSashForm.clone();
        cloned.weightsDatasFlowViewSashForm = weightsDatasFlowViewSashForm.clone();
        return cloned;
    }

}
