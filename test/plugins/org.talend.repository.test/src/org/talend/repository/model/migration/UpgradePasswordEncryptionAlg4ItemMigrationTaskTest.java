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
package org.talend.repository.model.migration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.PasswordEncryptUtil;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.FTPConnection;
import org.talend.core.model.metadata.builder.connection.LDAPSchemaConnection;
import org.talend.core.model.metadata.builder.connection.SAPConnection;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.property.Property;
import org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ParametersType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.model.bridge.ReponsitoryContextBridge;
import org.talend.repository.ProjectManager;
import org.talend.repository.generic.model.genericMetadata.GenericConnection;
import org.talend.repository.items.importexport.handlers.ImportExportHandlersManager;
import org.talend.repository.items.importexport.handlers.model.ImportItem;
import org.talend.repository.items.importexport.manager.ResourcesManager;
import org.talend.repository.items.importexport.ui.managers.FileResourcesUnityManager;
import org.talend.repository.items.importexport.ui.managers.ResourcesManagerFactory;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.utils.security.PasswordMigrationUtil;

public class UpgradePasswordEncryptionAlg4ItemMigrationTaskTest {

    private static List<ImportItem> importedItems = null;

    @BeforeClass
    public static void setUp() throws Exception {

        ReponsitoryContextBridge.setProject(ProjectManager.getInstance().getCurrentProject().getEmfProject());

        ImportExportHandlersManager importManager = new ImportExportHandlersManager();
        URL testJobURL = FileLocator.find(Platform.getBundle("org.talend.repository.test"),
                new Path("/resources/migration_test.zip"), null);
        if (testJobURL != null) {
            testJobURL = FileLocator.toFileURL(testJobURL);
        }
        File srcFile = new File(testJobURL.getFile());
        FileResourcesUnityManager fileUnityManager = ResourcesManagerFactory.getInstance().createFileUnityManager(srcFile);
        ResourcesManager resManager = fileUnityManager.doUnify();
        importedItems = importManager.populateImportingItems(resManager, true, new NullProgressMonitor());
        assertNotNull(importedItems);
        assertTrue(importedItems.size() > 0);
        importManager.importItemRecords(new NullProgressMonitor(), resManager, importedItems, true,
                importedItems.toArray(new ImportItem[0]), null);

    }

    @Test
    public void checkJob() throws Exception {
        List<IRepositoryViewObject> objects = getAll(ERepositoryObjectType.PROCESS);
        assertNotNull(objects);
        for (IRepositoryViewObject object : objects) {
            ProcessItem processItem = (ProcessItem) object.getProperty().getItem();
            ProcessType processType = processItem.getProcess();
            if (processItem.getProperty().getLabel().contains("job_context")) {
                List<ContextType> contextTypeList = processType.getContext();
                verifyContext(contextTypeList, "talend");
            }
            if (processItem.getProperty().getLabel().contains("job_context")) {
                List<ContextType> contextTypeList = processType.getContext();
                verifyContext(contextTypeList, "talend");
            }
            if (processItem.getProperty().getLabel().contains("ldap_job")) {
                verifyJobSettingParameter(processType, "talend");
            }

        }
    }

    @Test
    public void checkJoblet() throws Exception {
        List<IRepositoryViewObject> objects = getAll(ERepositoryObjectType.JOBLET);
        assertNotNull(objects);
        for (IRepositoryViewObject object : objects) {
            JobletProcessItem jobletItem = (JobletProcessItem) object.getProperty().getItem();
            List<ContextType> contextTypeList = jobletItem.getJobletProcess().getContext();
            verifyContext(contextTypeList, "talend");
        }
    }

    private void verifyContext(List<ContextType> contextTypeList, String expectValue) throws Exception {
        if (contextTypeList != null) {
            for (ContextType type : contextTypeList) {
                List<ContextParameterType> paramTypes = type.getContextParameter();
                if (paramTypes != null) {
                    for (ContextParameterType param : paramTypes) {
                        String value = param.getValue();
                        if (value != null && PasswordEncryptUtil.isPasswordType(param.getType())) {
                            assertEquals(expectValue, PasswordMigrationUtil.decryptPassword(value));
                        }
                    }
                }
            }
        }
    }

    private void verifyJobSettingParameter(ProcessType processType, String expectValue) throws Exception {
        ParametersType parameters = processType.getParameters();
        if (parameters != null) {
            for (Object p : parameters.getElementParameter()) {
                if (p instanceof ElementParameterType) {
                    ElementParameterType param = (ElementParameterType) p;
                    // variable name used for Stat&Logs
                    if ("PASS".equals(param.getName())) { //$NON-NLS-1$
                        String orginValue = param.getValue();
                        assertEquals(expectValue, PasswordMigrationUtil.decryptPassword(orginValue));
                    }
                }
            }
        }
    }

    @Test
    public void checkDatabaseConnection() throws Exception {
        List<IRepositoryViewObject> objects = getAll(ERepositoryObjectType.METADATA_CONNECTIONS);
        assertNotNull(objects);
        for (IRepositoryViewObject object : objects) {
            ConnectionItem item = (ConnectionItem) object.getProperty().getItem();
            Connection connection = item.getConnection();
            assertNotNull(connection);
            if (connection instanceof DatabaseConnection) {
                DatabaseConnection connectionImp = (DatabaseConnection) connection;
                if ("mysql".equals(connectionImp.getLabel())) {
                    String pass = connectionImp.getPassword();
                    assertNotNull(pass);
                    assertEquals("talend", connectionImp.getValue(pass, false));
                }
            }
        }
    }

    @Test
    public void checkFTPConnection() throws Exception {
        List<IRepositoryViewObject> objects = getAll(ERepositoryObjectType.METADATA_FILE_FTP);
        assertNotNull(objects);
        for (IRepositoryViewObject object : objects) {
            ConnectionItem item = (ConnectionItem) object.getProperty().getItem();
            Connection connection = item.getConnection();
            assertNotNull(connection);
            if (connection instanceof FTPConnection) {
                FTPConnection connectionImp = (FTPConnection) connection;
                String pass = connectionImp.getPassword();
                assertNotNull(pass);
                assertEquals(connectionImp.getValue(pass, false), "talend");
                if (item.getProperty().getLabel().contains("socket")) {
                    String proxyPass = connectionImp.getProxypassword();
                    assertNotNull(proxyPass);
                    assertEquals(connectionImp.getValue(proxyPass, false), "talend");
                }
                if (item.getProperty().getLabel().contains("ftps")) {
                    String keyStorePass = connectionImp.getKeystorePassword();
                    assertNotNull(keyStorePass);
                    assertEquals(connectionImp.getValue(keyStorePass, false), "talend");
                }
                if (item.getProperty().getLabel().contains("sftp")) {
                    String passPharse = connectionImp.getPassphrase();
                    assertNotNull(passPharse);
                    assertEquals(connectionImp.getValue(passPharse, false), "talend");
                }
            }
        }
    }

    @Test
    public void checkSAPConnection() throws Exception {
        List<IRepositoryViewObject> objects = getAll(ERepositoryObjectType.METADATA_SAPCONNECTIONS);
        assertNotNull(objects);
        for (IRepositoryViewObject object : objects) {
            ConnectionItem item = (ConnectionItem) object.getProperty().getItem();
            Connection connection = item.getConnection();
            assertNotNull(connection);
            if (connection instanceof SAPConnection) {
                SAPConnection connectionImp = (SAPConnection) connection;
                String pass = connectionImp.getPassword();
                assertNotNull(pass);
                assertEquals(connectionImp.getValue(pass, false), "talend");
            }
        }
    }

    @Test
    public void checkLDAPConnection() throws Exception {
        List<IRepositoryViewObject> objects = getAll(ERepositoryObjectType.METADATA_LDAP_SCHEMA);
        assertNotNull(objects);
        for (IRepositoryViewObject object : objects) {
            ConnectionItem item = (ConnectionItem) object.getProperty().getItem();
            Connection connection = item.getConnection();
            assertNotNull(connection);
            if (connection instanceof LDAPSchemaConnection) {
                LDAPSchemaConnection connectionImp = (LDAPSchemaConnection) connection;
                String pass = connectionImp.getBindPassword();
                assertNotNull(pass);
                assertEquals(connectionImp.getValue(pass, false), "talend");
            }
        }
    }

    @Test
    public void checkSalesforceConnection() throws Exception {
        List<IRepositoryViewObject> objects = getAll(getSalesforceObjectType());
        assertNotNull(objects);
        for (IRepositoryViewObject object : objects) {
            ConnectionItem item = (ConnectionItem) object.getProperty().getItem();
            Connection connection = item.getConnection();
            assertNotNull(connection);
            if (connection instanceof GenericConnection) {
                GenericConnection connectionImp = (GenericConnection) connection;
                ComponentProperties properties = loadProperties(connectionImp.getCompProperties());
                for (NamedThing namedThing : properties.getProperties()) {
                    if ("userPassword".equals(namedThing.getName()) && namedThing instanceof PropertiesImpl) {
                        PropertiesImpl ps = (PropertiesImpl) namedThing;
                        for (NamedThing nt : ps.getProperties()) {
                            if ("password".equals(nt.getName())) {
                                assertNotNull(((Property) nt).getStoredValue());
                            }
                            if ("securityKey".equals(nt.getName())) {
                                assertNotNull(((Property) nt).getStoredValue());
                            }
                        }
                    }
                }
            }
        }
    }

    private ComponentProperties loadProperties(String serialized) {
        return (ComponentProperties) Properties.Helper.fromSerializedPersistent(serialized, ComponentProperties.class).object;
    }

    private ERepositoryObjectType getSalesforceObjectType() {
        return createRepositoryType("salesforce", "salesforce", "salesforce", "metadata/salesforce", 100);
    }

    private ERepositoryObjectType createRepositoryType(String type, String label, String alias, String folder, int ordinal) {
        Constructor<ERepositoryObjectType> dynamicConstructor = getConstructor(ERepositoryObjectType.class,
                new Class[] { String.class, String.class, String.class, String.class, int.class, String[].class });
        ERepositoryObjectType typeObject = null;
        try {
            dynamicConstructor.setAccessible(true);
            typeObject = dynamicConstructor.newInstance(type, label, alias, folder, ordinal,
                    new String[] { ERepositoryObjectType.PROD_DI });
            typeObject.setAParent(ERepositoryObjectType.METADATA);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return typeObject;
    }

    private <E> Constructor<E> getConstructor(Class<E> clazz, Class<?>[] argTypes) {
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            try {
                return (Constructor<E>) c.getDeclaredConstructor(argTypes);
            } catch (Exception e) {
                continue;
            }
        }
        return null;
    }

    @AfterClass
    public static void tearDown() throws Exception {
        if (importedItems != null && !importedItems.isEmpty()) {
            for (ImportItem item : importedItems) {
                IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                IRepositoryViewObject repObj = factory.getLastVersion(item.getItemId());
                if (repObj != null) {
                    factory.deleteObjectPhysical(repObj);
                }
            }
            importedItems = null;
        }
    }

    private List<IRepositoryViewObject> getAll(ERepositoryObjectType type) throws PersistenceException {
        return ProxyRepositoryFactory.getInstance().getAll(type);
    }

}
