package org.talend.designer.core.utils;

import java.util.List;
import java.util.Map;

import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.process.IProcess;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;

public class ConnectionUtil {

    public static String generateUniqueConnectionName(EConnectionType connectType, IProcess process) {
        if (connectType.hasConnectionCategory(EConnectionType.FLOW)) {
            return process.generateUniqueConnectionName(Process.DEFAULT_ROW_CONNECTION_NAME);
        } else if (connectType.hasConnectionCategory(IConnectionCategory.TABLE)) {
            return process.generateUniqueConnectionName(Process.DEFAULT_TABLE_CONNECTION_NAME);
        } else if (connectType.equals(EConnectionType.ITERATE)) {
            return process.generateUniqueConnectionName(Process.DEFAULT_ITERATE_CONNECTION_NAME);
        } else if (connectType.equals(EConnectionType.ROUTE) || connectType.equals(EConnectionType.ROUTE_ENDBLOCK)) {
            return process.generateUniqueConnectionName(Process.DEFAULT_ROUTE_CONNECTION_NAME);
        } else if (connectType.equals(EConnectionType.ROUTE_WHEN)) {
            return process.generateUniqueConnectionName(Process.DEFAULT_WHEN_CONNECTION_NAME);
        } else if (connectType.equals(EConnectionType.ROUTE_OTHER)) {
            return process.generateUniqueConnectionName(Process.DEFAULT_OTHER_CONNECTION_NAME);
        } else if (connectType.equals(EConnectionType.ROUTE_CATCH)) {
            return process.generateUniqueConnectionName(Process.DEFAULT_CATCH_CONNECTION_NAME);
        } else if (connectType.equals(EConnectionType.ROUTE_FINALLY)) {
            return process.generateUniqueConnectionName(Process.DEFAULT_FINALLY_CONNECTION_NAME);
        } else if (connectType.equals(EConnectionType.ROUTE_TRY)) {
            return process.generateUniqueConnectionName(Process.DEFAULT_TRY_CONNECTION_NAME);
        }
        return null;
    }

    public static String generateUniqueConnectionName(EConnectionType connectType, IProcess process, INodeConnector connector) {
        if (connector == null) {
            return generateUniqueConnectionName(connectType, process);
        }
        String linkName = connector.getLinkName();
        if (linkName == null) {
            return generateUniqueConnectionName(connectType, process);
        }
        linkName = linkName.toUpperCase();
        linkName = linkName.replaceAll("\\s", "_");
        if (!process.checkValidConnectionName(linkName, false)) {
            linkName = connector.getName();
        }
        return process.generateUniqueConnectionName(linkName.toLowerCase());
    }

    /**
     * 
     * In order to unify the name for "UNIQUE" connection.
     */
    public static String getConnectionUnifiedName(IConnection conn) {

        // if connecion is belong on joblet.
        INode jobletNode = conn.getSource().getJobletNode();
        if (jobletNode != null && (jobletNode instanceof Node)) {
            boolean expanded = !((Node) jobletNode).getNodeContainer().isCollapsed();
            // when joblet is expanded. and the connection only belong one joblet(inner connection of joblet).
            if (expanded && jobletNode == conn.getTarget().getJobletNode()) {
                // unify with the JobletProcessProvider.addJobletPrefix
                return jobletNode.getUniqueName() + '_' + conn.getUniqueName();
            }

        }
        return conn.getUniqueName();

        /*
         * Some places use the getName, like trace before.
         */
        // return conn.getName();

    }
    
    public static void resetDriverValue(Object repValue){
        if(repValue instanceof List){
            List lines = (List) repValue;
            for(Object obj: lines){
                if(!(obj instanceof Map)){
                    continue;
                }
                Map line = (Map) obj;
                Object value = line.get("drivers");
                if(value == null){
                    continue;
                }
                if(value instanceof String){
                    if(((String)value).contains("/")){
                        line.put("drivers", ((String)value).split("/")[1]+".jar");
                    }
                    
                }
            }
        }
    }
    
    public static void getDriverJar(Object value){
        if(value instanceof List){
            List objs = (List) value;
            for(Object obj : objs){
                if(obj instanceof Map){
                    Map map = (Map) obj;
                    String driver = (String) map.get("drivers");
                    if(driver.contains("/")){
                        driver = driver.split("/")[1]+".jar";
                    }
                    map.put("drivers", driver);
                }
            }
        }
    }
}
