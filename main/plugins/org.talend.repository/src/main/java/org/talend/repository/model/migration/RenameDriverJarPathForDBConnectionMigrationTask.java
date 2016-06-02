package org.talend.repository.model.migration;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;

public class RenameDriverJarPathForDBConnectionMigrationTask extends AbstractItemMigrationTask {

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2016, 4, 29, 11, 0, 0);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {
        if (item instanceof DatabaseConnectionItem) {
            Connection connection = ((DatabaseConnectionItem) item).getConnection();
            if (connection instanceof DatabaseConnection) {
                DatabaseConnection dbConn = (DatabaseConnection) connection;
                String oldJarPath = dbConn.getDriverJarPath();
                String newJarPath = null;
                try {
                    if (oldJarPath != null && !"".equals(oldJarPath.trim())) { //$NON-NLS-1$
                        IPath path = Path.fromOSString(oldJarPath);
                        if (path.isAbsolute()) {
                            newJarPath = path.lastSegment();
                        }
                    }
                    if (newJarPath != null) {
                        dbConn.setDriverJarPath(newJarPath);
                        ProxyRepositoryFactory.getInstance().save(item, true);
                        return ExecutionResult.SUCCESS_NO_ALERT;
                    }
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                    return ExecutionResult.FAILURE;
                }
            }
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.METADATA_CONNECTIONS);
        return toReturn;
    }

}
