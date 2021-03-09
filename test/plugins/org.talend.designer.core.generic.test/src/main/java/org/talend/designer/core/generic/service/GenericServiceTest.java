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
package org.talend.designer.core.generic.service;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.talend.commons.utils.VersionUtils;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * DOC hwang  class global comment. Detailled comment
 */
public class GenericServiceTest {

    @Test
    public void testResetReferenceValue() {
          GenericService service = new GenericService();
        //---first
          String oldConnectionName = "joblet3_1";
          String newConnectionName = "joblet1_1_joblet2_1_joblet3_1";

          IComponent component = ComponentsFactoryProvider.getInstance().get("tJDBCInput",
                  ComponentCategory.CATEGORY_4_DI.getName());
          Property property = PropertiesFactory.eINSTANCE.createProperty();
          property.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
          property.setVersion(VersionUtils.DEFAULT_VERSION);
          Process process = new Process(property);
          Node node = new Node(component, process);

          ComponentProperties pros = node.getComponentProperties();
          ComponentReferenceProperties comPro = (ComponentReferenceProperties) pros.getProperties("referencedComponent"); //$NON-NLS-1$
          comPro.componentInstanceId.setValue("joblet3_1_joblet4_1_joblet5_1_tJDBCConnection_1");

          service.resetReferenceValue(node, oldConnectionName, newConnectionName);
          assertTrue(comPro.componentInstanceId.getStoredValue().equals("joblet1_1_joblet2_1_joblet3_1_joblet4_1_joblet5_1_tJDBCConnection_1"));
          //---second
          oldConnectionName = "tJDBCConnection_1";
          newConnectionName = "joblet1_1_joblet2_1_tJDBCConnection_1";
          comPro.componentInstanceId.setValue("tJDBCConnection_1_joblet4_1_joblet5_1_tJDBCConnection_1");
          service.resetReferenceValue(node, oldConnectionName, newConnectionName);
          assertTrue(comPro.componentInstanceId.getStoredValue().equals("joblet1_1_joblet2_1_tJDBCConnection_1_joblet4_1_joblet5_1_tJDBCConnection_1"));
    }

}
