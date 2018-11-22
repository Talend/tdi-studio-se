package org.talend.sdk.component.studio.model.parameter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElement;
import org.talend.core.model.utils.NodeUtil;
import org.talend.core.runtime.IAdditionalInfo;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.sdk.component.studio.lang.Pair;
import org.talend.sdk.component.studio.model.action.AbstractActionParameter;


public class SchemaActionParameter extends AbstractActionParameter {

    private final SchemaElementParameter elementParameter;

    public SchemaActionParameter(final SchemaElementParameter elementParameter, final String actionParameter) {
        super(elementParameter.getName(), actionParameter);
        this.elementParameter = elementParameter;
    }

    @Override
    public Collection<Pair<String, String>> parameters() {
        final List<Pair<String, String>> parameters = new ArrayList<>();
        IElement elem = elementParameter.getElement();
        if(elem == null || !(elem instanceof Node)) {
            return parameters;
        }
        Node node = (Node) elem;
        final String connectionType = (String)elementParameter.getTaggedValue("org.talend.sdk.connection.type");
        if(connectionType == null) {
            return parameters;
        }
        final IConnection connection = findConnection(connectionType, node);
        if (connection != null) {
            IMetadataTable metaTable = connection.getMetadataTable();
            List<IMetadataColumn> columns = metaTable.getListColumns();
            for (int i = 0; i < columns.size(); i++) {
                IMetadataColumn column = columns.get(i);
                parameters.add(new Pair<String, String>(getParameter() + "[" + i + "]", column.getLabel()));
            }
        }
        return parameters;
    }

    private IConnection findConnection(final String connectionType, final Node node) {
        if("in".equalsIgnoreCase(connectionType)) {
            if(node.getComponent().useLookup()) {
                for (final IConnection conn : node.getIncomingConnections()) {
                    String input_name = null;
                    if (!(conn instanceof IAdditionalInfo)) {
                        continue;
                    }
                    input_name = (String)IAdditionalInfo.class.cast(conn).getInfo("INPUT_NAME");
                    if(input_name != null && input_name.equals(elementParameter.getContext())) {
                        return conn;
                    }
                }
            } else {
                final List<? extends IConnection> connections = NodeUtil.getIncomingConnections(node, elementParameter.getContext());
                if(connections != null && !connections.isEmpty()) {
                    return connections.get(0);
                }
            }
        } else {
            final List<? extends IConnection> connections = NodeUtil.getOutgoingConnections(node, elementParameter.getContext());
            if(connections != null && !connections.isEmpty()) {
                return connections.get(0);
            }
        }
        return null;
    }

}
