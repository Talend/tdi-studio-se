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
package org.talend.designer.core.generic.palette;

import java.util.Arrays;
import java.util.List;

import org.talend.components.api.component.SupportedProduct;
import org.talend.core.model.components.ComponentCategory;

/**
 * created by ycbai on 2017年3月31日 Detailled comment
 *
 */
public class GenericComponentCategoryFactory {

    public static List<String> getPaletteTypes(String productType) {
        if (SupportedProduct.ALL.equals(productType)) {
            // return new ArrayList<>(ComponentCategory.getAllCategoryNames());
            // TODO: Regard the product as DI temporarily for now. Remove below line and use the above line after
            // components are all upadated to fill the right supported products.
            return Arrays.asList(ComponentCategory.CATEGORY_4_DI.getName());
        } else if (SupportedProduct.DI.equals(productType)) {
            return Arrays.asList(ComponentCategory.CATEGORY_4_DI.getName());
        } else if (SupportedProduct.MAP_REDUCE.equals(productType)) {
            return Arrays.asList(ComponentCategory.CATEGORY_4_MAPREDUCE.getName());
        } else if (SupportedProduct.STORM.equals(productType)) {
            return Arrays.asList(ComponentCategory.CATEGORY_4_STORM.getName());
        } else if (SupportedProduct.SPARK.equals(productType)) {
            return Arrays.asList(ComponentCategory.CATEGORY_4_SPARK.getName());
        } else if (SupportedProduct.SPARKSTREAMING.equals(productType)) {
            return Arrays.asList(ComponentCategory.CATEGORY_4_SPARKSTREAMING.getName());
        }
        return null;
    }

}
