package org.talend.repository.model.migration;

import java.util.List;

import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.EList;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.properties.ItemState;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.RepositoryObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ContextParameterTypeImpl;


public class AddMissingContextMigrationTaskTest {

    private ProcessItem testItem;

    @Before
    public void setUp() throws Exception {
        testItem = createTempProcessItem();
    }

    @After
    public void tearDown() throws Exception {
        RepositoryObject objToDelete = new RepositoryObject(testItem.getProperty());
        ProxyRepositoryFactory.getInstance().deleteObjectPhysical(objToDelete);
        testItem = null;
    }

    @Test
    public void testAddMissingContext() {
        // dev
        String[] paramDev = new String[] { "param1", "param2", "param3" };
        ContextType devGroup = createContextType("dev", paramDev);
        testItem.getProcess().setDefaultContext("dev");
        // test
        String[] paramTest = new String[] { "param1", "param3" }; // test group missing param2
        ContextType testGroup = createContextType("test", paramTest);
        // prop
        String[] paramProp = new String[] { "param1", "param2" }; // prop group missing param3
        ContextType propGroup = createContextType("prop", paramProp);

        testItem.getProcess().getContext().add(devGroup);
        testItem.getProcess().getContext().add(testGroup);
        testItem.getProcess().getContext().add(propGroup);
        AddMissingContextMigrationTask migration = new AddMissingContextMigrationTask();
        migration.execute(testItem);
        EList<ContextType> contexts = testItem.getProcess().getContext();
        boolean hasParam2 = false;
        List<ContextParameterTypeImpl> contextParams = contexts.get(1).getContextParameter();
        for(ContextParameterTypeImpl cp:contextParams) {
            if("param2".equals(cp.getName())){
                hasParam2 = true;
            }
        }
        Assert.assertTrue(hasParam2);
        boolean hasParam3 = false;
        List<ContextParameterTypeImpl> contextParams2 = contexts.get(2).getContextParameter();
        for (ContextParameterTypeImpl cp : contextParams2) {
            if ("param3".equals(cp.getName())) {
                hasParam3 = true;
            }
        }
        Assert.assertTrue(hasParam3);
    }


    private ProcessItem createTempProcessItem() throws PersistenceException {
        ProcessItem processItem = PropertiesFactory.eINSTANCE.createProcessItem();
        Property myProperty = PropertiesFactory.eINSTANCE.createProperty();
        myProperty.setId(ProxyRepositoryFactory.getInstance().getNextId());
        ItemState itemState = PropertiesFactory.eINSTANCE.createItemState();
        itemState.setDeleted(false);
        itemState.setPath("");
        processItem.setState(itemState);
        processItem.setProperty(myProperty);
        myProperty.setLabel("addMissingContextTestJob");
        myProperty.setVersion("0.1");
        processItem.setProcess(TalendFileFactory.eINSTANCE.createProcessType());
        ProxyRepositoryFactory.getInstance().create(processItem, new Path(""));
        return processItem;
    }

    private ContextType createContextType(String contextName, String[] paramNames) {
        ContextType context = TalendFileFactory.eINSTANCE.createContextType();
        context.setName(contextName);
        for (String paramName : paramNames) {
            ContextParameterType param = TalendFileFactory.eINSTANCE.createContextParameterType();
            param.setName(paramName);
            param.setType("id_String");
            context.getContextParameter().add(param);
        }
        return context;
    }
}
