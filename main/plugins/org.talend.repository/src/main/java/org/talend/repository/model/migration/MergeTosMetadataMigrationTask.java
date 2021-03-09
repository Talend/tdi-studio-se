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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.m2m.atl.core.ATLCoreException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.database.EDatabaseSchemaOrCatalogMapping;
import org.talend.core.model.metadata.builder.database.ExtractMetaDataUtils;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.utils.URIHelper;
import org.talend.cwm.helper.CatalogHelper;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.cwm.helper.SwitchHelpers;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.metadata.managment.ui.utils.ConnectionContextHelper;
import org.talend.model.migration.TosMetadataMigrationFrom400to410;
import org.talend.repository.ProjectManager;
import org.talend.repository.constants.FileConstants;

import orgomg.cwm.resource.relational.Catalog;
import orgomg.cwm.resource.relational.RelationalFactory;
import orgomg.cwm.resource.relational.Schema;

/**
 * DOC hywang class global comment. Detailled comment
 */
public class MergeTosMetadataMigrationTask extends AbstractItemMigrationTask {

    private static Logger log = Logger.getLogger(MergeTosMetadataMigrationTask.class);

    private TosMetadataMigrationFrom400to410 metadata400to410;

    static final HashMap<String, Object> XML_SAVE_OTIONS_1_0 = new HashMap<String, Object>(2);

    static final HashMap<String, Object> XML_SAVE_OTIONS_1_1 = new HashMap<String, Object>(2);
    static {
        XML_SAVE_OTIONS_1_0.put(XMLResource.OPTION_ENCODING, "UTF-8"); //$NON-NLS-1$
        XML_SAVE_OTIONS_1_0.put(XMLResource.OPTION_XML_VERSION, "1.0"); //$NON-NLS-1$
        XML_SAVE_OTIONS_1_1.put(XMLResource.OPTION_ENCODING, "UTF-8"); //$NON-NLS-1$
        XML_SAVE_OTIONS_1_1.put(XMLResource.OPTION_XML_VERSION, "1.1"); //$NON-NLS-1$
    }

    public ResourceSet resourceSet = new ResourceSetImpl();

    @Override
    public ExecutionResult execute(Item item) {
        if (item instanceof ConnectionItem) {
            try {
                URI itemURI = getItemURI(item);
                if (itemURI != null) {
                    URI itemResourceURI = getItemResourceURI(itemURI);
                    if (metadata400to410 == null) {
                        metadata400to410 = new TosMetadataMigrationFrom400to410();
                    }
                    Resource migratedResource = metadata400to410.migrate(itemResourceURI.toString(), new NullProgressMonitor());
                    HashMap<String, Object> xmlSaveOtions = XML_SAVE_OTIONS_1_0;
                    if (migratedResource != null) {
                        // check for DB connection caus we need to setup Schema and Catalog properly
                        EObject content = migratedResource.getContents().get(0);
                        if (content != null && "DatabaseConnection".equals(content.eClass().getName())) {
                            // resource is dynamic EMF so convert it to static model by serialising it and reloading it
                            ByteArrayOutputStream tempStream = new ByteArrayOutputStream();
                            try {
                                // serialize into memory
                                try {
                                    migratedResource.save(tempStream, XML_SAVE_OTIONS_1_0);
                                } catch (Exception e) {
                                    // try with version 1.1
                                    tempStream = new ByteArrayOutputStream();
                                    xmlSaveOtions = XML_SAVE_OTIONS_1_1;
                                    migratedResource.save(tempStream, xmlSaveOtions);
                                } finally {
                                    tempStream.close();
                                }
                                // create a resource to laod the inmemory resource that should be a static EMF model
                                migratedResource = resourceSet.createResource(URI
                                        .createURI("http://talend/dummy_static.metadata")); //$NON-NLS-1$
                                migratedResource.load(new ByteArrayInputStream(tempStream.toByteArray()), xmlSaveOtions);
                                // check that DBConnection is firdt element
                                DatabaseConnection databaseConnection = SwitchHelpers.DATABASECONNECTION_SWITCH
                                        .doSwitch(migratedResource.getContents().get(0));
                                // do not check for null caus DB connection is already check above
                                String databaseType = databaseConnection.getDatabaseType();
                                databaseConnection.setDriverClass(ExtractMetaDataUtils.getInstance().getDriverClassByDbType(
                                        databaseType));
                                EDatabaseTypeName currentType = EDatabaseTypeName.getTypeFromDbType(databaseType);
                                EDatabaseSchemaOrCatalogMapping curCatalog = currentType.getCatalogMappingField();
                                EDatabaseSchemaOrCatalogMapping curSchema = currentType.getSchemaMappingField();
                                // all the DB connection are migrated with a Schema by default
                                if (!curCatalog.equals(EDatabaseSchemaOrCatalogMapping.None)) {
                                    List<Schema> schemas = ConnectionHelper.getSchema(databaseConnection);
                                    if (!curSchema.equals(EDatabaseSchemaOrCatalogMapping.None)) {
                                        // we need to place the current schemas into a catalogs
                                        ConnectionHelper.removeSchemas(schemas, databaseConnection);
                                        for (Schema schema : schemas) {
                                            // compute the name of the schema and the catalogs
                                            String schemaName = computeSchemaName(schema, databaseConnection, curSchema);
                                            String catalogName = computeCatalogName(databaseConnection, curCatalog);
                                            schema.setName(schemaName);
                                            Catalog catalog = RelationalFactory.eINSTANCE.createCatalog();
                                            // catalogs are not in a contained reference
                                            migratedResource.getContents().add(catalog);
                                            catalog.setName(catalogName);
                                            // add the schema to the catalog and the the catalog to the connection
                                            CatalogHelper.addSchemas(Collections.singleton(schema), catalog);
                                            ConnectionHelper.addCatalog(catalog, databaseConnection);
                                        }
                                    } else {
                                        // we need to replace the Schemas with a Catalogs
                                        for (Schema schema : schemas) {
                                            // compute the name the catalog
                                            String catalogName = computeCatalogName(databaseConnection, curCatalog);
                                            // use owned elements to get everything regardless of tables or views or
                                            // else
                                            Catalog catalog = RelationalFactory.eINSTANCE.createCatalog();
                                            // catalogs are not in a contained reference
                                            migratedResource.getContents().add(catalog);
                                            catalog.setName(catalogName);
                                            catalog.getOwnedElement().addAll(schema.getOwnedElement());
                                            ConnectionHelper.addCatalog(catalog, databaseConnection);
                                            ConnectionHelper.removeSchemas(Collections.singleton(schema), databaseConnection);
                                        }

                                    }
                                } else if (!curSchema.equals(EDatabaseSchemaOrCatalogMapping.None)) {
                                    List<Schema> schemas = ConnectionHelper.getSchema(databaseConnection);
                                    for (Schema schema : schemas) {
                                        String schemaName = computeSchemaName(schema, databaseConnection, curSchema);
                                        schema.setName(schemaName);
                                    }
                                }
                                // else no catalog so we keep the schema as is
                            } catch (Exception e) {
                                // we have an exception finalising the migration but we trap it caus we still try to
                                // save it
                                log.error("Cannot complete merge metadata migration on file:" + itemResourceURI.toString(), e);
                                ExceptionHandler.process(e);
                            } finally {
                                tempStream.close();
                            }
                        } // else not a DB connection so persist
                        OutputStream outputStream = item.eResource().getResourceSet().getURIConverter()
                                .createOutputStream(itemResourceURI, null);
                        try {
                            migratedResource.save(outputStream, xmlSaveOtions);
                        } finally {
                            outputStream.close();
                        }
                    }
                    return ExecutionResult.SUCCESS_WITH_ALERT;
                }
            } catch (ATLCoreException e) {
                log.error(e);
                ExceptionHandler.process(e);
                return ExecutionResult.SUCCESS_NO_ALERT;
            } catch (IOException e) {
                log.error(e);
                ExceptionHandler.process(e);
                return ExecutionResult.SUCCESS_NO_ALERT;
            } finally {
                resourceSet.getResources().clear();
            }
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

    /**
     * compute the schema name.
     *
     * @param schema the schema to compute the name from
     * @param databaseConnection the DB connection caus some of the name are issued from the DB connection
     * @param curSchema, the type of name for the schema
     * @return string corresponding to the naming convention of schemaNameType
     */
    private String computeSchemaName(Schema schema, DatabaseConnection connection, EDatabaseSchemaOrCatalogMapping schemaNameType) {
        String result = ""; //$NON-NLS-1$
        ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(null, connection, null, true);
        switch (schemaNameType) {
        case Login:
            result = connection.getUsername();
            break;
        case Schema:
            if (contextType != null) {
                result = ConnectionContextHelper.getOriginalValue(contextType, schema.getName());
            } else {
                /* if schema name is null,return a empty string is required,bug 0017244 */
                if (schema.getName() != null) {
                    result = schema.getName();
                }
            }
            break;
        case Sid:
            result = connection.getSID();
            break;
        case Default_Name:
            result = connection.getName();
            break;
        case None:
            // return an empty string
            break;
        default:// return an empty string
            break;
        }
        return result;
    }

    /**
     * compute the catalog name.
     *
     * @param databaseConnection the DB connection caus some of the name are issued from the DB connection
     * @param catalogNameType, the type of name for the catalog
     * @return the string corresponding to the naming convention of catalogNameType
     */
    private String computeCatalogName(DatabaseConnection connection, EDatabaseSchemaOrCatalogMapping catalogNameType) {
        String result = ""; //$NON-NLS-1$
        ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(null, connection, null, true);
        switch (catalogNameType) {
        case Login:
            result = connection.getUsername();
            break;
        case Sid:
            if (contextType != null) {
                result = ConnectionContextHelper.getOriginalValue(contextType, connection.getSID());
            } else {
                result = connection.getSID();
            }
            break;
        case Default_Name:
            result = connection.getName();
            break;
        case Schema:
        case None:
        default:// return an empty string
            break;
        }
        return result;
    }

    private URI getItemURI(Item item) {
        ProjectManager pManager = ProjectManager.getInstance();
        org.talend.core.model.general.Project project = new org.talend.core.model.general.Project(pManager.getProject(item));
        // referenced item
        if (project != null && !project.equals(pManager.getCurrentProject())) {
            String folder = ERepositoryObjectType.getFolderName(ERepositoryObjectType.getItemType(item));

            if (folder != null) {
                IPath path = new Path(project.getTechnicalLabel());
                path = path.append(folder);
                path = path.append(item.getState().getPath());
                Property property = item.getProperty();
                String itemStr = property.getLabel() + "_" + property.getVersion() + "." + FileConstants.PROPERTIES_EXTENSION; //$NON-NLS-1$ //$NON-NLS-2$
                path = path.append(itemStr);
                return URIHelper.convert(path);
            }
        }
        if (item.eResource() != null) { // the migrationtask execute two times,after reload in first tiem,this one will
            // be null
            return item.eResource().getURI();
        }
        return null;
    }

    private URI getItemResourceURI(URI propertyResourceURI) {
        return propertyResourceURI.trimFileExtension().appendFileExtension(FileConstants.ITEM_EXTENSION);
    }

    //
    // private Resource getItemResource(Item item) {
    // URI itemResourceURI = getItemResourceURI(getItemURI(item));
    // Resource itemResource = resourceSet.getResource(itemResourceURI, false);
    //
    // if (itemResource == null) {
    // if (item instanceof FileItem) {
    // itemResource = new ByteArrayResource(itemResourceURI);
    // resourceSet.getResources().add(itemResource);
    // }
    // itemResource = resourceSet.getResource(itemResourceURI, true);
    // }
    //
    // return itemResource;
    // }

    @Override
    public Date getOrder() {
        // TODO Auto-generated method stub
        GregorianCalendar gc = new GregorianCalendar(2000, 6, 29, 10, 20, 0);
        return gc.getTime();
    }
}
