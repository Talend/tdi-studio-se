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
package org.talend.designer.core.ui.editor.properties.controllers;


import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * created by hwang on 2017-3-8 Detailled comment
 *
 */

public class ComponentListControllerTest {

    @Before
    public void before() {
    }


    @Test
    public void testrenameComponentUniqueName(){
      //---first
        String oldConnectionName = "joblet3_1";
        String newConnectionName = "joblet1_1_joblet2_1_joblet3_1";
        List<Node> nodesToUpdate = new ArrayList<Node>();

        IComponent component = ComponentsFactoryProvider.getInstance().get("tMysqlInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
        property.setVersion(VersionUtils.DEFAULT_VERSION);
        Process process = new Process(property);
        Node node = new Node(component, process);
        nodesToUpdate.add(node);
        IElementParameter param = node.getElementParameter("CONNECTION");
        param.setValue("joblet3_1_joblet4_1_joblet5_1_tMysqlConnection_1");

        ComponentListController.renameComponentUniqueName(oldConnectionName, newConnectionName, nodesToUpdate);
        assertTrue(param.getValue().equals("joblet1_1_joblet2_1_joblet3_1_joblet4_1_joblet5_1_tMysqlConnection_1"));
        //---second
        oldConnectionName = "tMysqlConnection_1";
        newConnectionName = "joblet1_1_joblet2_1_tMysqlConnection_1";
        param.setValue("tMysqlConnection_1_joblet4_1_joblet5_1_tMysqlConnection_1");
        ComponentListController.renameComponentUniqueName(oldConnectionName, newConnectionName, nodesToUpdate);
        assertTrue(param.getValue().equals("joblet1_1_joblet2_1_tMysqlConnection_1_joblet4_1_joblet5_1_tMysqlConnection_1"));

    }


}
