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
package org.talend.repository.model;

import java.util.ArrayList;
import java.util.List;

import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.core.model.metadata.types.AutoConversionType;

/**
 *
 * created by hcyi on Aug 18, 2016 Detailled comment
 *
 */
public class AutoConversionTypeModel extends ExtendedTableModel<AutoConversionType> {

    public AutoConversionTypeModel() {
        super();
    }

    public AutoConversionTypeModel(List<AutoConversionType> beanList, String name) {
        super(name);
        setAutoConversionTypes(beanList);
    }

    public void setAutoConversionTypes(List<AutoConversionType> beans) {
        registerDataList(beans);
    }

    public List<AutoConversionType> createAutoConversionType() {
        return new ArrayList<>();
    }
}
