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
package org.talend.designer.core.generic.ui;

import java.util.List;
import java.util.Map;

/**
 * created by ycbai on 2016年9月5日 Detailled comment
 *
 */
public class JsonTableVO {

    private List<String> titles;

    private List<Map<String, Object>> data;

    /**
     * Getter for titles.
     *
     * @return the titles
     */
    public List<String> getTitles() {
        return this.titles;
    }

    /**
     * Sets the titles.
     *
     * @param titles the titles to set
     */
    public void setTitles(List<String> titles) {
        this.titles = titles;
    }

    /**
     * Getter for data.
     *
     * @return the data
     */
    public List<Map<String, Object>> getData() {
        return this.data;
    }

    /**
     * Sets the data.
     *
     * @param data the data to set
     */
    public void setData(List<Map<String, Object>> data) {
        this.data = data;
    }

}
