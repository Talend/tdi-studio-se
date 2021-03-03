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
package org.talend.repository.utils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.Folder;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryObject;
import org.talend.core.model.routines.RoutinesUtil;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ParametersType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.RoutinesParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class JobVersionUtilsTest {

    IProxyRepositoryFactory reponsfactory = ProxyRepositoryFactory.getInstance();

    IProxyRepositoryFactory repositoryFactory;

    String folderName = "aa";

    Folder folder;

    IPath itemPath;

    IRepositoryViewObject repositoryObject;

    /**
     * DOC Administrator Comment method "setUp".
     *
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        folder = reponsfactory.createFolder(ERepositoryObjectType.PROCESS, new Path(""), folderName);
    }

    /**
     * DOC Administrator Comment method "tearDown".
     *
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        repositoryFactory.deleteObjectPhysical(repositoryObject);
        reponsfactory.deleteFolder(folder.getRepositoryObjectType(), new Path("process/aa"));
    }

    /**
     * Test method for
     * {@link org.talend.repository.utils.JobVersionUtils#getCurrentVersion(org.talend.repository.model.RepositoryNode)}
     * .
     *
     * @throws PersistenceException
     */
    @Test
    public void testGetCurrentVersion() throws PersistenceException {

        repositoryObject = createRepositoryObject("job1");

        RepositoryNode childrenNode = mock(RepositoryNode.class);
        when(childrenNode.getObject()).thenReturn(repositoryObject);
        when(childrenNode.getId()).thenReturn(repositoryObject.getProperty().getId());

        List<IRepositoryNode> children = new ArrayList<IRepositoryNode>();
        children.add(childrenNode);

        RepositoryNode folderNode = mock(RepositoryNode.class);
        when(folderNode.getObject()).thenReturn(folder);
        when(folderNode.getId()).thenReturn(folder.getId());
        when(folderNode.getChildren()).thenReturn(children);
        // test,one situation
        String vesion = JobVersionUtils.getCurrentVersion(folderNode);
        assertEquals(vesion, "0.1");
        // test,two situation
        repositoryObject.getProperty().setVersion("0.2");
        String vesion2 = JobVersionUtils.getCurrentVersion(folderNode);
        assertEquals(vesion2, "0.2");

    }

    private IRepositoryViewObject createRepositoryObject(String lable) throws PersistenceException {

        // create item
        itemPath = new Path(folderName);

        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
        property.setVersion(VersionUtils.DEFAULT_VERSION);
        property.setStatusCode(""); //$NON-NLS-1$

        ProcessItem processItem = PropertiesFactory.eINSTANCE.createProcessItem();
        processItem.setProperty(property);

        repositoryFactory = RepositoryPlugin.getDefault().getRepositoryService().getProxyRepositoryFactory();

        property.setId(repositoryFactory.getNextId());
        property.setLabel(lable);

        ProcessType process = TalendFileFactory.eINSTANCE.createProcessType();
        ParametersType parameterType = TalendFileFactory.eINSTANCE.createParametersType();
        // add depended routines.
        List<RoutinesParameterType> dependenciesInPreference = RoutinesUtil.createDependenciesInPreference();
        parameterType.getRoutinesParameter().addAll(dependenciesInPreference);
        process.setParameters(parameterType);
        processItem.setProcess(process);

        repositoryFactory.create(processItem, itemPath);

        return new RepositoryObject(property);
    }

}
