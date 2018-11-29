package org.talend.designer.core.ui.editor.process;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentsService;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.ui.editor.nodes.Node;
public class ProcessUpdateManagerTest {

    private Process process;

    private ProcessUpdateManager updateManager;

    private IComponentsService compService;


    @Before
    public void setUp() throws Exception {
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        process = new Process(property);
        updateManager = new ProcessUpdateManager(process);
        compService = (IComponentsService) GlobalServiceRegister.getDefault()
                .getService(IComponentsService.class);
    }

    @Test
    public void testIsOldJDBC() throws Exception {
        IComponent com = null;
        Node node = null;
        DatabaseConnection connection = ConnectionFactory.eINSTANCE.createDatabaseConnection();

        com = ComponentsFactoryProvider.getInstance().get("tELTMap", ComponentCategory.CATEGORY_4_DI.getName());
        node = new Node(com, process);
        connection.setDatabaseType(EDatabaseTypeName.GENERAL_JDBC.getProduct());
        assertTrue(updateManager.isOldJDBC(node, connection));

        connection.setDatabaseType(EDatabaseTypeName.MYSQL.getProduct());
        assertFalse(updateManager.isOldJDBC(node, connection));

        com = ComponentsFactoryProvider.getInstance().get("tSqoopMerge", ComponentCategory.CATEGORY_4_DI.getName());
        node = new Node(com, process);
        connection.setDatabaseType(EDatabaseTypeName.GENERAL_JDBC.getProduct());
        assertTrue(updateManager.isOldJDBC(node, connection));

    }
}
