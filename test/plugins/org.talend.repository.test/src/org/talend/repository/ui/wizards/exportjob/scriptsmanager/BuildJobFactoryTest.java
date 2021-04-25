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
package org.talend.repository.ui.wizards.exportjob.scriptsmanager;

import org.junit.Assert;
import org.junit.Test;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.runtime.process.IBuildJobHandler;
import org.talend.core.runtime.process.TalendProcessArgumentConstant;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.repository.ui.wizards.exportjob.JavaJobScriptsExportWSWizardPage.JobExportType;
import org.talend.repository.ui.wizards.exportjob.handler.BuildJobHandler;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class BuildJobFactoryTest {

    private ProcessItem createTestProcessItem() {
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
        property.setLabel("ABC");
        property.setVersion(VersionUtils.DEFAULT_VERSION);
        property.setStatusCode(""); //$NON-NLS-1$
        ProcessItem processItem = PropertiesFactory.eINSTANCE.createProcessItem();
        processItem.setProperty(property);
        ProcessType processType = TalendFileFactory.eINSTANCE.createProcessType();
        processItem.setProcess(processType);
        return processItem;
    }

    @Test
    public void test_createBuildJobHandler_JobExportType_OSGi() {
        ProcessItem processItem = createTestProcessItem();
        Property property = processItem.getProperty();

        IBuildJobHandler handler = BuildJobFactory.createBuildJobHandler(processItem, "Default", property.getVersion(),
                JobScriptsManagerFactory.getDefaultExportChoiceMap(), JobExportType.OSGI);
        Assert.assertNotNull("Can't build job for OSGi", handler);
        Assert.assertEquals("use another BuildJobHandler for OSGi", BuildJobHandler.class.getName(), handler.getClass().getName());
    }

    @Test
    public void test_createBuildJobHandler_JobExportType_POJO() {
        ProcessItem processItem = createTestProcessItem();
        Property property = processItem.getProperty();

        IBuildJobHandler handler = BuildJobFactory.createBuildJobHandler(processItem, "Default", property.getVersion(),
                JobScriptsManagerFactory.getDefaultExportChoiceMap(), JobExportType.POJO);
        Assert.assertNotNull("Can't build job for standalone job", handler);
        Assert.assertEquals(BuildJobHandler.class.getName(), handler.getClass().getName());
    }

    @Test
    public void test_createBuildJobHandler_JobExportType_null() {
        ProcessItem processItem = createTestProcessItem();
        Property property = processItem.getProperty();

        IBuildJobHandler handler = BuildJobFactory.createBuildJobHandler(processItem, "Default", property.getVersion(),
                JobScriptsManagerFactory.getDefaultExportChoiceMap(), (JobExportType) null);
        Assert.assertNotNull("Can't build job for standalone job", handler);
        Assert.assertEquals(BuildJobHandler.class.getName(), handler.getClass().getName());
    }

    @Test
    public void test_createBuildJobHandler_String_standalone() {
        ProcessItem processItem = createTestProcessItem();
        Property property = processItem.getProperty();

        IBuildJobHandler handler = BuildJobFactory.createBuildJobHandler(processItem, "Default", property.getVersion(),
                JobScriptsManagerFactory.getDefaultExportChoiceMap(), "STANDALONE");
        Assert.assertNotNull("Can't build job for standalone job", handler);
        Assert.assertEquals(BuildJobHandler.class.getName(), handler.getClass().getName());
    }

    @Test
    public void test_createBuildJobHandler_String_default_withoutSetting() {
        ProcessItem processItem = createTestProcessItem();
        Property property = processItem.getProperty();

        IBuildJobHandler handler = BuildJobFactory.createBuildJobHandler(processItem, "Default", property.getVersion(),
                JobScriptsManagerFactory.getDefaultExportChoiceMap(), (String) null);
        Assert.assertNotNull("Can't build job for standalone job", handler);
        Assert.assertEquals(BuildJobHandler.class.getName(), handler.getClass().getName());
    }

    @Test
    public void test_createBuildJobHandler_String_default_withStandaloneSetting() {
        ProcessItem processItem = createTestProcessItem();
        Property property = processItem.getProperty();

        property.getAdditionalProperties().put(TalendProcessArgumentConstant.ARG_BUILD_TYPE, "STANDALONE");

        IBuildJobHandler handler = BuildJobFactory.createBuildJobHandler(processItem, "Default", property.getVersion(),
                JobScriptsManagerFactory.getDefaultExportChoiceMap(), (String) null);
        Assert.assertNotNull("Can't build job for standalone job", handler);
        Assert.assertEquals(BuildJobHandler.class.getName(), handler.getClass().getName());
    }
}
