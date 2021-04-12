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
package org.talend.designer.mapper.utils;

import static org.junit.Assert.*;

import org.junit.Test;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * created by nrousseau on Mar 21, 2017 Detailled comment
 *
 */
public class MapperHelperTest {

    @Test
    public void test() {
        Property property1 = PropertiesFactory.eINSTANCE.createProperty();
        property1.setId("property1"); //$NON-NLS-1$
        property1.setVersion("0.1"); //$NON-NLS-1$
        property1.setLabel("test1");//$NON-NLS-1$
        Process process = new Process(property1);

        IComponent sparkComponent = ComponentsFactoryProvider.getInstance().get("tMap",
                ComponentCategory.CATEGORY_4_SPARK.getName());
        Node sparkTMap = new Node(sparkComponent, process);
        assertFalse("Spark tMap should not be a virtual component", MapperHelper.isGeneratedAsVirtualComponent(sparkTMap));


        IComponent sparkStreamingComponent = ComponentsFactoryProvider.getInstance().get("tMap",
                ComponentCategory.CATEGORY_4_SPARKSTREAMING.getName());
        Node sparkStreamingTMap = new Node(sparkStreamingComponent, process);
        assertFalse("Spark Streaming tMap should not be a virtual component", MapperHelper.isGeneratedAsVirtualComponent(sparkStreamingTMap));

        IComponent mapReduceComponent = ComponentsFactoryProvider.getInstance().get("tMap",
                ComponentCategory.CATEGORY_4_MAPREDUCE.getName());
        Node mapReduceTMap = new Node(mapReduceComponent, process);
        assertFalse("Spark Streaming tMap should not be a virtual component", MapperHelper.isGeneratedAsVirtualComponent(mapReduceTMap));
    }

}
