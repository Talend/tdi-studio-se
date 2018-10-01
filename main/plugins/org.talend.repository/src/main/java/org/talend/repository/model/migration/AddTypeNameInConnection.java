package org.talend.repository.model.migration;

import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.lang.StringUtils;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.metadata.builder.connection.impl.DatabaseConnectionImpl;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.repository.model.ProxyRepositoryFactory;

/**
 * 
 * DOC jding class global comment. Detailled comment
 */
public class AddTypeNameInConnection extends AbstractItemMigrationTask {


    @Override
    public ExecutionResult execute(Item item) {
        try {
            boolean modified = addTypeName(item);
            if (modified) {
                return ExecutionResult.SUCCESS_NO_ALERT;
            } else {
                return ExecutionResult.NOTHING_TO_DO;
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    private boolean addTypeName(Item item) throws PersistenceException {
        if (item instanceof ConnectionItem) {
            ConnectionItem conn = (ConnectionItem) item;
            if (StringUtils.isBlank(conn.getTypeName())) {
                if (conn.getConnection() instanceof DatabaseConnectionImpl) {
                    ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                    DatabaseConnectionImpl connection = (DatabaseConnectionImpl) conn.getConnection();
                    conn.setTypeName(connection.getDatabaseType());
                    factory.save(item, true);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2018, 6, 6, 12, 0, 0);
        return gc.getTime();
    }
}
