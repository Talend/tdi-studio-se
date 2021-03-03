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

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;

/**
 * DOC ggu class global comment. Detailled comment <br/>
 *
 */
public class ConvertOldPerlTypesMigrationTask extends AbstractItemMigrationTask {

    private static final ProxyRepositoryFactory FACTORY = ProxyRepositoryFactory.getInstance();

    private boolean changed = false;

    @Override
    public ExecutionResult execute(Item item) {
        return ExecutionResult.NOTHING_TO_DO; // perl have been removed, so nothing to do.

        // if (getProject().getLanguage() == ECodeLanguage.JAVA) {
        // }
        // try {
        // convertItem(item);
        // if (changed) {
        // return ExecutionResult.SUCCESS_WITH_ALERT;
        // } else {
        // return ExecutionResult.SUCCESS_NO_ALERT;
        // }
        // } catch (PersistenceException e) {
        // ExceptionHandler.process(e);
        // return ExecutionResult.FAILURE;
        // }
    }

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        // toReturn.add(ERepositoryObjectType.PROCESS);
        // toReturn.add(ERepositoryObjectType.CONTEXT);
        // toReturn.add(ERepositoryObjectType.ROUTINES);
        // toReturn.add(ERepositoryObjectType.METADATA_CONNECTIONS);
        // toReturn.add(ERepositoryObjectType.METADATA_FILE_DELIMITED);
        // toReturn.add(ERepositoryObjectType.METADATA_FILE_POSITIONAL);
        // toReturn.add(ERepositoryObjectType.METADATA_FILE_REGEXP);
        // toReturn.add(ERepositoryObjectType.METADATA_FILE_XML);
        // toReturn.add(ERepositoryObjectType.METADATA_FILE_EXCEL);
        // toReturn.add(ERepositoryObjectType.METADATA_FILE_LDIF);
        // toReturn.add(ERepositoryObjectType.METADATA_GENERIC_SCHEMA);
        return toReturn;
    }

    // private void convertItem(Item item) throws PersistenceException {
    // ERepositoryObjectType itemType = ERepositoryObjectType.getItemType(item);
    //
    // if (itemType == ERepositoryObjectType.PROCESS) {
    // convertJobs((ProcessItem) item);
    // } else if (itemType == ERepositoryObjectType.CONTEXT) {
    // convertContext((ContextItem) item);
    // } else if (itemType == ERepositoryObjectType.ROUTINES) {
    // convertRoutines((RoutineItem) item);
    // } else if (itemType == ERepositoryObjectType.METADATA_CONNECTIONS
    // || itemType == ERepositoryObjectType.METADATA_FILE_DELIMITED
    // || itemType == ERepositoryObjectType.METADATA_FILE_POSITIONAL
    // || itemType == ERepositoryObjectType.METADATA_FILE_REGEXP || itemType == ERepositoryObjectType.METADATA_FILE_XML
    // || itemType == ERepositoryObjectType.METADATA_FILE_EXCEL || itemType == ERepositoryObjectType.METADATA_FILE_LDIF
    // || itemType == ERepositoryObjectType.METADATA_GENERIC_SCHEMA) {
    // changeMetadataConnections((ConnectionItem) item);
    // }
    // }
    //
    // /**
    // *
    // * DOC ggu Comment method "convertJobs".<br>
    // *
    // * Convert the Process(Job)
    // *
    // * @return
    // */
    // private void convertJobs(ProcessItem processItem) throws PersistenceException {
    // List<IRepositoryViewObject> processList = FACTORY.getAll(ERepositoryObjectType.PROCESS, true);
    // PerlItemOldTypesConverter converter = new PerlItemOldTypesConverter(processItem);
    //
    // if (converter.isModified()) {
    // FACTORY.save(processItem, true);
    // changed = true;
    // }
    // }
    //
    // /**
    // *
    // * DOC ggu Comment method "convertContext".<br>
    // *
    // * convert old Perl types in the Context.
    // *
    // * @param contextItem
    // * @return
    // * @throws PersistenceException
    // */
    // private void convertContext(ContextItem contextItem) throws PersistenceException {
    // PerlItemOldTypesConverter converter = new PerlItemOldTypesConverter(contextItem);
    //
    // if (converter.isModified()) {
    // FACTORY.save(contextItem);
    // changed = true;
    // }
    // }
    //
    // /**
    // *
    // * DOC ggu Comment method "convertRoutines".<br>
    // *
    // * convert the Routines
    // *
    // * @return
    // */
    // private void convertRoutines(RoutineItem routineItem) throws PersistenceException {
    // PerlItemOldTypesConverter converter = new PerlItemOldTypesConverter(routineItem);
    //
    // if (converter.isModified()) {
    // FACTORY.save(routineItem);
    // changed = true;
    // }
    // }
    //
    // private void changeMetadataConnections(ConnectionItem connectionItem) throws PersistenceException {
    // PerlItemOldTypesConverter converter = new PerlItemOldTypesConverter(connectionItem);
    //
    // if (converter.isModified()) {
    // FACTORY.save(connectionItem);
    // changed = true;
    // }
    // }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2008, 2, 17, 12, 0, 0);
        return gc.getTime();
    }
}
