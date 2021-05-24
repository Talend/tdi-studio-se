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
package org.talend.designer.rowgenerator.external.data;

import java.io.Serializable;

import org.eclipse.swt.graphics.Rectangle;

/**
 * qzhang class global comment. Detailled comment <br/>
 *
 * $Id: ExternalRowGeneratorUiProperties.java,v 1.2 2007/01/31 05:20:52 pub Exp $
 *
 */
public class ExternalRowGeneratorUiProperties implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 145468748235L;

    // must be null by default to unmarshal correctly
    private static int[] weightsMainSashForm = new int[0];

    public static final int[] DEFAULT_WEIGHTS_MAIN_SASH_FORM = new int[] { 70, 30 };

    // must be null by default to unmarshal correctly
    private static int[] weightsDatasFlowViewSashForm = new int[0];

    public static final int[] DEFAULT_WEIGHTS_DATAS_FLOW_SASH_FORM = new int[] { 33, 33, 34 };

    private static Rectangle boundsRowGen = new Rectangle(50, 50, 800, 600);

    private static boolean shellMaximized = false;

    private static String[] showColumnsList;

    public static boolean isShellMaximized() {
        return shellMaximized;
    }

    public static void setShellMaximized(boolean shellMaximize) {
        shellMaximized = shellMaximize;
    }

    public static String[] getShowColumnsList() {
        return showColumnsList;
    }


    public static void setShowColumnsList(String[] showColumnsList) {
        ExternalRowGeneratorUiProperties.showColumnsList = showColumnsList;
    }


    public static Rectangle getBoundsRowGen() {
        return boundsRowGen;
    }


    public static void setBoundsRowGen(Rectangle boundsRowGen) {
        ExternalRowGeneratorUiProperties.boundsRowGen = boundsRowGen;
    }


    public static int[] getWeightsDatasFlowViewSashForm() {
        return weightsDatasFlowViewSashForm;
    }


    public static void setWeightsDatasFlowViewSashForm(int[] weightsDatasFlowViewSashForm) {
        ExternalRowGeneratorUiProperties.weightsDatasFlowViewSashForm = weightsDatasFlowViewSashForm;
    }


    public static int[] getWeightsMainSashForm() {
        return weightsMainSashForm;
    }


    public static void setWeightsMainSashForm(int[] weightsMainSashForm) {
        ExternalRowGeneratorUiProperties.weightsMainSashForm = weightsMainSashForm;
    }
}
