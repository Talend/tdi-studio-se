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
package org.talend.repository;

import java.util.List;

import org.talend.core.model.metadata.IAutoConvertTypesService;
import org.talend.core.model.metadata.types.AutoConversionType;
import org.talend.repository.utils.AutoConvertTypesUtils;

/**
 * created by hcyi on Aug 22, 2016 Detailled comment
 *
 */
public class AutoConvertTypesService implements IAutoConvertTypesService {

    @Override
    public List<AutoConversionType> getAllAutoConversionTypes() {
        return AutoConvertTypesUtils.getAllAutoConversionTypes();
    }
}
