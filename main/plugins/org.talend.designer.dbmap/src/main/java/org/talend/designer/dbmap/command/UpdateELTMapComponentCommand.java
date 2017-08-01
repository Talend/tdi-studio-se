package org.talend.designer.dbmap.command;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.gef.commands.Command;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.node.IExternalMapTable;
import org.talend.designer.dbmap.external.data.ExternalDbMapEntry;
import org.talend.designer.dbmap.external.data.ExternalDbMapTable;

public class UpdateELTMapComponentCommand extends Command {

    private String oldConnectionName;

    private String newConnectionName;

    private IConnection connection;

    private List<? extends IExternalMapTable> newInputTables;

    private List<? extends IExternalMapTable> newOutputTables;

    public UpdateELTMapComponentCommand(INode node, IConnection connection, String oldConnectionName, String newConnectionName) {
        this.connection = connection;
        this.oldConnectionName = oldConnectionName;
        this.newConnectionName = newConnectionName;
        newInputTables = node.getExternalData().getInputTables();
        newOutputTables = node.getExternalData().getOutputTables();
    }

    @Override
    public void execute() {
        execute(oldConnectionName, newConnectionName);
    }

    private void execute(String oldValue, String newValue) {
        connection.setName(newValue);
        // may have several tables with different aliases.
        boolean isAliasIncludeTableName = false;
        // update table name
        for (IExternalMapTable input : newInputTables) {
            if (input instanceof ExternalDbMapTable) {
                ExternalDbMapTable dbMapTable = (ExternalDbMapTable) input;
                if (oldValue.equals(dbMapTable.getName()) || oldValue.equals(dbMapTable.getTableName())) {
                    dbMapTable.setName(newValue);
                    dbMapTable.setTableName(newValue);
                    String alias = dbMapTable.getAlias();
                    if (alias != null) {
                        if (alias.contains(oldValue)) {
                            isAliasIncludeTableName = true;
                        }
                    }
                }
            }
        }
        // update expression
        for (IExternalMapTable output : newOutputTables) {
            if (output instanceof ExternalDbMapTable) {
                List<ExternalDbMapEntry> entries = ((ExternalDbMapTable) output).getMetadataTableEntries();
                for (ExternalDbMapEntry entry : entries) {
                    String expression = entry.getExpression();
                    if (!StringUtils.isBlank(expression) && expression.contains(oldValue) && !isAliasIncludeTableName) {
                        entry.setExpression(expression.replace(oldValue, newValue));
                    }
                }
            }
        }
    }

    @Override
    public void undo() {
        execute(newConnectionName, oldConnectionName);
    }

}
