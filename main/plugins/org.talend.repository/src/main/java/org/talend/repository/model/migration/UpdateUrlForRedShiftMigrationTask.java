package org.talend.repository.model.migration;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;

public class UpdateUrlForRedShiftMigrationTask extends AbstractItemMigrationTask {

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2016, 4, 26, 16, 0, 0);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {
        if (item instanceof DatabaseConnectionItem) {
            Connection connection = ((DatabaseConnectionItem) item).getConnection();
            if (connection instanceof DatabaseConnection) {
                DatabaseConnection dbConn = (DatabaseConnection) connection;
                try {
                    if (EDatabaseTypeName.REDSHIFT.getDisplayName().equals(dbConn.getDatabaseType())) {
                        String oldUrl = dbConn.getURL(); // old url: //jdbc:[postgresql|paraccel]://localhost:5439/test
                        if (oldUrl != null) {
                            String newUrl = oldUrl.replaceFirst("jdbc:\\w+:", "jdbc:redshift:"); //$NON-NLS-1$ //$NON-NLS-2$
                            if (!oldUrl.equals(newUrl)) {
                                dbConn.setURL(newUrl);
                                ProxyRepositoryFactory.getInstance().save(item, true);
                                return ExecutionResult.SUCCESS_NO_ALERT;
                            }
                        }
                    }
                } catch (PersistenceException e) {
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
