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
package org.talend.repository.model.migration;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.cwm.helper.PackageHelper;
import org.talend.cwm.helper.TableHelper;
import org.talend.cwm.helper.ViewHelper;
import org.talend.cwm.relational.RelationalFactory;
import org.talend.cwm.relational.TdColumn;
import org.talend.cwm.relational.TdSqlDataType;
import org.talend.cwm.relational.TdTable;
import org.talend.cwm.relational.TdView;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * DOC hywang class global comment. All the databaseConnection need have name because the shareing of metadata between
 * top and tos
 */
public class FillParametersForDatabaseConnectionMigrationTask extends AbstractItemMigrationTask {

    /**
     * Name the java.sql.Types.NULL type
     */
    private static final String NULL_SQL_TYPE_NAME = "NULL"; //$NON-NLS-1$

    private IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

    @Override
    public ExecutionResult execute(Item item) {
        if (item instanceof DatabaseConnectionItem) {
            DatabaseConnectionItem dbItem = (DatabaseConnectionItem) item;
            Connection connection = dbItem.getConnection();
            DatabaseConnection dbconn = (DatabaseConnection) connection;
            EList<orgomg.cwm.objectmodel.core.Package> pkgs = dbconn.getDataPackage();
            fillParametersForColumns(pkgs); // get all tdtables and set sqldatatype
            dbconn.setName(dbItem.getProperty().getLabel());
            try {
                factory.save(dbItem, true);
                return ExecutionResult.SUCCESS_WITH_ALERT;
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }
        return ExecutionResult.SUCCESS_NO_ALERT;
    }

    private void fillParametersForColumns(EList<orgomg.cwm.objectmodel.core.Package> pkgs) {
        for (orgomg.cwm.objectmodel.core.Package pkg : pkgs) {
            // handle tables
            List<TdTable> tables = PackageHelper.getTables(pkg);
            for (TdTable table : tables) {
                fillParamaters(TableHelper.getColumns(table));
            }
            // handle views
            List<TdView> views = PackageHelper.getViews(pkg);
            for (TdView view : views) {
                fillParamaters(ViewHelper.getColumns(view));
            }
        }
    }

    /**
     * DOC sgandon Comment method "fillParamaters".
     *
     * @param tcol
     */
    private void fillParamaters(List<TdColumn> allColumns) {
        for (TdColumn tdCol : allColumns) {
            TdSqlDataType sqlDataType = RelationalFactory.eINSTANCE.createTdSqlDataType();
            // it is impossible to find out what is the java data type from the type name
            // because every jdbc constructor may implement their own types
            // see http://download.oracle.com/javase/1.3/docs/guide/jdbc/getstart/mapping.html#table1
            // so we set it to NULL type and 0.
            // TOP may test it and retreive it from the DB when necessary.
            sqlDataType.setName(NULL_SQL_TYPE_NAME);
            sqlDataType.setJavaDataType(java.sql.Types.NULL);
            tdCol.setSqlDataType(sqlDataType);
        }
    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2010, 9, 25, 15, 30, 0);
        return gc.getTime();
    }

}
