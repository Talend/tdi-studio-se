// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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
import org.talend.repository.ui.wizards.exportjob.JavaJobScriptsExportWSWizardPage.JobExportType;
import org.talend.repository.ui.wizards.exportjob.handler.BuildJobHandler;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class BuildJobFactoryTest {

    @Test
    public void test_createBuildJobHandler_JobExportType_war() {
        IBuildJobHandler handler = BuildJobFactory.createBuildJobHandler(PropertiesFactory.eINSTANCE.createProcessItem(),
                "Default", "0.1", JobScriptsManagerFactory.getDefaultExportChoiceMap(), JobExportType.WSWAR);
        Assert.assertNull("Have supportted WAR, not support before", handler);
    }

    @Test
    public void test_createBuildJobHandler_JobExportType_ZIP() {
        IBuildJobHandler handler = BuildJobFactory.createBuildJobHandler(PropertiesFactory.eINSTANCE.createProcessItem(),
                "Default", "0.1", JobScriptsManagerFactory.getDefaultExportChoiceMap(), JobExportType.WSZIP);
        Assert.assertNull("Have supportted ZIP, not support before", handler);
    }

    @Test
    public void test_createBuildJobHandler_JobExportType_OSGi() {
        IBuildJobHandler handler = BuildJobFactory.createBuildJobHandler(PropertiesFactory.eINSTANCE.createProcessItem(),
                "Default", "0.1", JobScriptsManagerFactory.getDefaultExportChoiceMap(), JobExportType.OSGI);
        Assert.assertNull("Have supportted OSGi, not support before", handler);
    }

    @Test
    public void test_createBuildJobHandler_JobExportType_POJO() {
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
        property.setLabel("ABC");
        property.setVersion(VersionUtils.DEFAULT_VERSION);
        property.setStatusCode(""); //$NON-NLS-1$
        ProcessItem processItem = PropertiesFactory.eINSTANCE.createProcessItem();
        processItem.setProperty(property);

        IBuildJobHandler handler = BuildJobFactory.createBuildJobHandler(processItem, "Default", property.getVersion(),
                JobScriptsManagerFactory.getDefaultExportChoiceMap(), JobExportType.POJO);
        Assert.assertNotNull("Can't build job for standalone job", handler);
        Assert.assertEquals(BuildJobHandler.class.getName(), handler.getClass().getName());
    }

    @Test
    public void test_createBuildJobHandler_JobExportType_null() {
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
        property.setLabel("ABC");
        property.setVersion(VersionUtils.DEFAULT_VERSION);
        property.setStatusCode(""); //$NON-NLS-1$
        ProcessItem processItem = PropertiesFactory.eINSTANCE.createProcessItem();
        processItem.setProperty(property);

        IBuildJobHandler handler = BuildJobFactory.createBuildJobHandler(processItem, "Default", property.getVersion(),
                JobScriptsManagerFactory.getDefaultExportChoiceMap(), (JobExportType) null);
        Assert.assertNotNull("Can't build job for standalone job", handler);
        Assert.assertEquals(BuildJobHandler.class.getName(), handler.getClass().getName());
    }

    @Test
    public void test_createBuildJobHandler_String_standalone() {
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
        property.setLabel("ABC");
        property.setVersion(VersionUtils.DEFAULT_VERSION);
        property.setStatusCode(""); //$NON-NLS-1$
        ProcessItem processItem = PropertiesFactory.eINSTANCE.createProcessItem();
        processItem.setProperty(property);

        IBuildJobHandler handler = BuildJobFactory.createBuildJobHandler(processItem, "Default", property.getVersion(),
                JobScriptsManagerFactory.getDefaultExportChoiceMap(), "STANDALONE");
        Assert.assertNotNull("Can't build job for standalone job", handler);
        Assert.assertEquals(BuildJobHandler.class.getName(), handler.getClass().getName());
    }

    @Test
    public void test_createBuildJobHandler_String_default_withoutSetting() {
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
        property.setLabel("ABC");
        property.setVersion(VersionUtils.DEFAULT_VERSION);
        property.setStatusCode(""); //$NON-NLS-1$
        ProcessItem processItem = PropertiesFactory.eINSTANCE.createProcessItem();
        processItem.setProperty(property);

        IBuildJobHandler handler = BuildJobFactory.createBuildJobHandler(processItem, "Default", property.getVersion(),
                JobScriptsManagerFactory.getDefaultExportChoiceMap(), (String) null);
        Assert.assertNotNull("Can't build job for standalone job", handler);
        Assert.assertEquals(BuildJobHandler.class.getName(), handler.getClass().getName());
    }

    @Test
    public void test_createBuildJobHandler_String_default_withStandaloneSetting() {
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
        property.setLabel("ABC");
        property.setVersion(VersionUtils.DEFAULT_VERSION);
        property.setStatusCode(""); //$NON-NLS-1$
        ProcessItem processItem = PropertiesFactory.eINSTANCE.createProcessItem();
        processItem.setProperty(property);
        property.getAdditionalProperties().put(TalendProcessArgumentConstant.ARG_BUILD_TYPE, "STANDALONE");

        IBuildJobHandler handler = BuildJobFactory.createBuildJobHandler(processItem, "Default", property.getVersion(),
                JobScriptsManagerFactory.getDefaultExportChoiceMap(), (String) null);
        Assert.assertNotNull("Can't build job for standalone job", handler);
        Assert.assertEquals(BuildJobHandler.class.getName(), handler.getClass().getName());
    }

    @Test
    public void test_createBuildJobHandler_String_default_withOSGiSetting() {
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
        property.setLabel("ABC");
        property.setVersion(VersionUtils.DEFAULT_VERSION);
        property.setStatusCode(""); //$NON-NLS-1$
        ProcessItem processItem = PropertiesFactory.eINSTANCE.createProcessItem();
        processItem.setProperty(property);
        property.getAdditionalProperties().put(TalendProcessArgumentConstant.ARG_BUILD_TYPE, "OSGI");

        IBuildJobHandler handler = BuildJobFactory.createBuildJobHandler(processItem, "Default", property.getVersion(),
                JobScriptsManagerFactory.getDefaultExportChoiceMap(), (String) null);
        Assert.assertNull("Have supported OSGi build handler, not support before", handler);
    }
}
