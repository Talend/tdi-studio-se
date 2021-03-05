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

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.talend.components.api.component.SupportedProduct;
import org.talend.core.model.components.ComponentCategory;

/**
 * created by ycbai on 2017年4月1日 Detailled comment
 *
 */
public class GenericComponentCategoryFactoryTest {

    @Test
    public void testGetPaletteTypes() {
        List<String> paletteTypes = GenericComponentCategoryFactory.getPaletteTypes(SupportedProduct.ALL);
        // assertThat(paletteTypes, is(new ArrayList<>(ComponentCategory.getAllCategoryNames())));
        // TODO: Regard the product as DI temporarily for now. Remove below line and use the above line after components
        // are all upadated to fill the right supported products.
        assertThat(paletteTypes, is(Arrays.asList(ComponentCategory.CATEGORY_4_DI.getName())));

        paletteTypes = GenericComponentCategoryFactory.getPaletteTypes(SupportedProduct.DI);
        assertThat(paletteTypes, is(Arrays.asList(ComponentCategory.CATEGORY_4_DI.getName())));

        paletteTypes = GenericComponentCategoryFactory.getPaletteTypes(SupportedProduct.MAP_REDUCE);
        assertThat(paletteTypes, is(Arrays.asList(ComponentCategory.CATEGORY_4_MAPREDUCE.getName())));

        paletteTypes = GenericComponentCategoryFactory.getPaletteTypes(SupportedProduct.STORM);
        assertThat(paletteTypes, is(Arrays.asList(ComponentCategory.CATEGORY_4_STORM.getName())));

        paletteTypes = GenericComponentCategoryFactory.getPaletteTypes(SupportedProduct.SPARK);
        assertThat(paletteTypes, is(Arrays.asList(ComponentCategory.CATEGORY_4_SPARK.getName())));

        paletteTypes = GenericComponentCategoryFactory.getPaletteTypes(SupportedProduct.SPARKSTREAMING);
        assertThat(paletteTypes, is(Arrays.asList(ComponentCategory.CATEGORY_4_SPARKSTREAMING.getName())));
    }

}
