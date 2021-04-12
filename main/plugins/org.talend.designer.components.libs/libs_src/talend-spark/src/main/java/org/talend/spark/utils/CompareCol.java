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
package org.talend.spark.utils;
import java.io.Serializable;

/**
 * Company : Altic - LIPN
 * User: Tugdual Sarazin
 * Date: 05/06/14
 * Time: 15:31
 */
public class CompareCol implements Serializable {
    /**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private Integer colId;
    private boolean ascending;
    private CompareType type = CompareType.LETTER;

    public CompareCol(Integer colId, boolean ascending, CompareType type) {
        this.colId = colId;
        this.ascending = ascending;
        this.type = type;
    }

    public Integer getColId() {
        return colId;
    }

    public boolean isAscending() {
        return ascending;
    }

    public CompareType getCompareType() {
        return type;
    }
}
