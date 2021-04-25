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
package org.talend.designer.filemultischemas.data;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;

/**
 * cLi class global comment. Detailled comment
 */
public class ExternalMultiSchemasUIProperties {

    public static final String NUMBER_PATTERN = "[0-9]*"; //$NON-NLS-1$

    public static final String[] FILE_EXTENSIONS = new String[] { "*.txt", "*.*" }; //$NON-NLS-1$  //$NON-NLS-2$

    private static int[] ALL_SASHFORM_WEIGHTS = new int[] { 2, 1 };

    private static int[] HEADER_SASHFORM_WEIGHTS = new int[] { 3, 2 };

    public static final int SASHFORM_WIDTH = 3;

    public static final String FETCH_LABEL = "Fetch Codes";

    public static final String FILE_LABEL = "File name";

    public static final String OUTPUT_LABEL = "Output";

    public static final String SETTINGS_LABEL = "Settings";

    public static final String PREVIEW_LABEL = "Preview";

    public static final String PREVIEW_STRING = PREVIEW_LABEL + "..."; //$NON-NLS-1$

    public static final String COLUMN_KEY = "KEY";

    public static final String COLUMN_RECORD = "RECORD";

    public static final String COLUMN_SEPARATOR = "SEPARATOR";

    public static final int DIALOG_STYLE = SWT.APPLICATION_MODAL | SWT.BORDER | SWT.RESIZE | SWT.CLOSE | SWT.MIN | SWT.MAX
            | SWT.TITLE;

    public static final String DEFAULT_COLUMN_NAME = "Column"; //$NON-NLS-1$

    private static Rectangle boundsMapper = new Rectangle(50, 50, 900, 850);

    private static boolean shellMaximized = false;

    private static boolean schemaDetailsModel = false;

    public static Rectangle getBoundsMapper() {
        return boundsMapper;
    }

    public static void setBoundsMapper(Rectangle boundsMapper) {
        ExternalMultiSchemasUIProperties.boundsMapper = boundsMapper;
    }

    public static boolean isShellMaximized() {
        return shellMaximized;
    }

    public static void setShellMaximized(boolean shellMaximized) {
        ExternalMultiSchemasUIProperties.shellMaximized = shellMaximized;
    }

    public static int[] getAllSashformWeights() {
        return ALL_SASHFORM_WEIGHTS;
    }

    public static void setAllSashformWeights(int[] allSashformWeights) {
        ALL_SASHFORM_WEIGHTS = allSashformWeights;
    }

    public static int[] getHeaderSashformWeights() {
        return HEADER_SASHFORM_WEIGHTS;
    }

    public static void setHeaderSashformWeights(int[] headerSashformWeights) {
        HEADER_SASHFORM_WEIGHTS = headerSashformWeights;
    }

    public static boolean isSchemaDetailsModel() {
        return schemaDetailsModel;
    }

    public static void setSchemaDetailsModel(boolean schemaDetailsModel) {
        ExternalMultiSchemasUIProperties.schemaDetailsModel = schemaDetailsModel;
    }

}
