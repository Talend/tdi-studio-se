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

import java.util.ArrayList;
import java.util.List;

import org.talend.core.utils.CsvArray;

/**
 * wchen class global comment. Detailled comment
 */
public class CSVArrayAndSeparator {

    private final CsvArray csvArray;

    private final List<String> separators;

    public CSVArrayAndSeparator() {
        csvArray = new CsvArray();
        separators = new ArrayList<String>();
    }

    public CsvArray getCsvArray() {
        return this.csvArray;
    }

    public List<String> getSeparators() {
        return this.separators;
    }

}
