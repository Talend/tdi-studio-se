package org.talend.designer.core.ui.editor.process;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.ui.editor.nodes.Node;
public class ProcessUpdateManagerTest {

    private Process process;

    private ProcessUpdateManager updateManager;

    private final Method methods[] = ProcessUpdateManager.class.getDeclaredMethods();

    private Method method;

    @Before
    public void setUp() throws Exception {
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        process = new Process(property);
        updateManager = new ProcessUpdateManager(process);
        for (int i = 0; i < methods.length; ++i) {
            if (methods[i].getName().equals("isOldJDBC")) {
                method = methods[i];
                method.setAccessible(true);
            }
        }
    }

    @Test
    public void testIsOldJDBC() throws Exception {
        IComponent com = null;
        Node node = null;
        Object para[] = null;
        DatabaseConnection connection = ConnectionFactory.eINSTANCE.createDatabaseConnection();

        com = ComponentsFactoryProvider.getInstance().get("tELTMap", ComponentCategory.CATEGORY_4_DI.getName());
        node = new Node(com, process);
        connection.setDatabaseType("JDBC");
        para = new Object[] { node, connection };
        assertTrue((Boolean) (method.invoke(updateManager, para)));

        com = ComponentsFactoryProvider.getInstance().get("tSqoopMerge", ComponentCategory.CATEGORY_4_DI.getName());
        node = new Node(com, process);
        connection.setDatabaseType("JDBC");
        para = new Object[] { node, connection };
        assertTrue((Boolean) (method.invoke(updateManager, para)));

        com = ComponentsFactoryProvider.getInstance().get("tELTMap", ComponentCategory.CATEGORY_4_DI.getName());
        node = new Node(com, process);
        connection.setDatabaseType("MySQL");
        para = new Object[] { node, connection };
        assertFalse((Boolean) (method.invoke(updateManager, para)));

    }
}
