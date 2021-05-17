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
package org.talend.designer.core.model.metadata;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.talend.core.PluginChecker;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.process.EConnectionType;
import org.talend.designer.core.model.utils.emf.talendfile.ColumnType;
import org.talend.designer.core.model.utils.emf.talendfile.MetadataType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFilePackage;
import org.talend.designer.core.model.utils.emf.talendfile.util.TalendFileResourceImpl;

// import org.talend.core.model.metadata.MetadataTable;

/**
 * Class that will take either a metadata or an emf object for the metadata, and that can return after the emf object or
 * the metadata object.
 *
 * $Id: MetadataEmfFactory.java 51271 2010-11-15 08:40:42Z nrousseau $
 *
 */
public class MetadataEmfFactory {

    IMetadataTable metadataTable;

    MetadataType metadataType;

    @SuppressWarnings("unchecked")
    public void setMetadataTable(final IMetadataTable metadataTable) {
        this.metadataTable = metadataTable;
        TalendFileFactory fileFact = TalendFileFactory.eINSTANCE;
        metadataType = fileFact.createMetadataType();
        IMetadataColumn metaCol;
        ColumnType colType;
        EList listColType;

        metadataType.setComment(metadataTable.getComment());
        metadataType.setName(metadataTable.getTableName());
        metadataType.setLabel(metadataTable.getLabel());
        metadataType.setConnector(metadataTable.getAttachedConnector());
        metadataType.setType(metadataTable.getTableType());
        metadataType.getAdditionalProperties().putAll(metadataTable.getAdditionalProperties());
        listColType = metadataType.getColumn();

        if (metadataTable.getListColumns(true) != null) {
            for (int i = 0; i < metadataTable.getListColumns(true).size(); i++) {
                metaCol = metadataTable.getListColumns(true).get(i);
                colType = fileFact.createColumnType();
                colType.setComment(metaCol.getComment());
                colType.setKey(metaCol.isKey());
                colType.setNullable(metaCol.isNullable());
                if (metaCol.getLength() == null || metaCol.getLength().intValue() < 0) {
                    colType.setLength(-1);
                    // colType.unsetLength();
                } else {
                    colType.setLength(metaCol.getLength());
                }

                if (metaCol.getOriginalLength() != null) {
                    colType.setOriginalLength(metaCol.getOriginalLength());
                } else {
                    colType.setOriginalLength(-1);
                }

                if (metaCol.getAdditionalField().size() > 0) {
                    for (String key : metaCol.getAdditionalField().keySet()) {
                        colType.getAdditionalField().put(key, metaCol.getAdditionalField().get(key));
                    }
                }

                colType.setName(metaCol.getLabel());
                if (metaCol.getPrecision() == null || metaCol.getPrecision().intValue() < 0) {
                    colType.setPrecision(-1);
                    // colType.unsetPrecision();
                } else {
                    colType.setPrecision(metaCol.getPrecision());
                }
                if (!metaCol.getLabel().equals(metaCol.getOriginalDbColumnName())) {
                    colType.setOriginalDbColumnName(metaCol.getOriginalDbColumnName());
                }
                colType.setType(metaCol.getTalendType());
                colType.setSourceType(metaCol.getType());
                colType.setPattern(metaCol.getPattern());
                colType.setDefaultValue(metaCol.getDefault());
                if (PluginChecker.isDatacertPluginLoaded()) {
                    colType.setRelatedEntity(metaCol.getRelatedEntity());
                    colType.setRelationshipType(metaCol.getRelationshipType());
                }
                colType.setUsefulColumn(metaCol.isUsefulColumn());
                listColType.add(colType);
            }
        }
    }

    public void setMetadataType(final MetadataType metadataType) {
        metadataTable = new MetadataTable();
        setMetadataTypeCommon(metadataType);
    }

    @SuppressWarnings("unchecked")
    private void setMetadataTypeCommon(final MetadataType metadataType) {
        this.metadataType = metadataType;

        IMetadataColumn metaCol;
        ColumnType colType;
        EList listColType;
        List<IMetadataColumn> listMetadataColumn;

        metadataTable.setComment(metadataType.getComment());
        metadataTable.setTableName(metadataType.getName());
        metadataTable.setLabel(metadataType.getLabel());
        metadataTable.setTableType(metadataType.getType());
        EMap<String, String> properties = metadataType.getAdditionalProperties();
        if (properties != null) {
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                metadataTable.getAdditionalProperties().put(entry.getKey(), entry.getValue());
            }
        }
        if (metadataType.getConnector() != null) {
            metadataTable.setAttachedConnector(metadataType.getConnector());
        } else {
            metadataTable.setAttachedConnector(EConnectionType.FLOW_MAIN.getName());
        }
        listColType = metadataType.getColumn();

        listMetadataColumn = new ArrayList<IMetadataColumn>();
        for (int i = 0; i < listColType.size(); i++) {
            colType = (ColumnType) listColType.get(i);
            metaCol = new MetadataColumn();
            metaCol.setComment(colType.getComment());
            metaCol.setKey(colType.isKey());
            metaCol.setNullable(colType.isNullable());
            if (colType.getLength() >= 0) {
                metaCol.setLength(new Integer(colType.getLength()));
            } else {
                metaCol.setLength(null);
            }

            if (colType.getOriginalLength() >= 0) {
                metaCol.setOriginalLength(new Integer(colType.getOriginalLength()));
            } else {
                metaCol.setOriginalLength(null);
            }

            if (colType.getAdditionalField().size() > 0) {
                Iterator it = colType.getAdditionalField().keySet().iterator();
                while (it.hasNext()) {
                    String key = (String) it.next();
                    metaCol.getAdditionalField().put(key, (String) colType.getAdditionalField().get(key));
                }
            }
            metaCol.setLabel(colType.getName());
            if (colType.getPrecision() >= 0) {
                metaCol.setPrecision(new Integer(colType.getPrecision()));
            } else {
                metaCol.setPrecision(null);
            }
            metaCol.setTalendType(colType.getType());
            metaCol.setType(colType.getSourceType());
            metaCol.setPattern(colType.getPattern());
            metaCol.setDefault(colType.getDefaultValue());

            metaCol.setRelatedEntity(colType.getRelatedEntity());
            metaCol.setRelationshipType(colType.getRelationshipType());
            if (colType.getOriginalDbColumnName() == null) {
                metaCol.setOriginalDbColumnName(colType.getName());
            } else {
                metaCol.setOriginalDbColumnName(colType.getOriginalDbColumnName());
            }
            metaCol.setUsefulColumn(colType.isUsefulColumn());
            listMetadataColumn.add(metaCol);
        }
        metadataTable.setListColumns(listMetadataColumn);
        List<String> columnNames = new ArrayList<String>();
        for (IMetadataColumn column : listMetadataColumn) {
            columnNames.add(column.getLabel());
        }
        metadataTable.setOriginalColumns(columnNames);
    }

    public MetadataType getMetadataType() {
        return metadataType;
    }

    public IMetadataTable getMetadataTable() {
        return metadataTable;
    }

    @SuppressWarnings("unchecked")
    public OutputStream getOutputStream() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        URI uri = URI.createURI(""); //$NON-NLS-1$
        Resource res = new TalendFileResourceImpl(uri);
        res.getContents().add(metadataType);

        res.save(out, null);
        return out;
    }

    public void setInputStream(final InputStream istream) throws IOException {
        URI uri = URI.createURI(""); //$NON-NLS-1$

        TalendFilePackage.eINSTANCE.getNsURI();
        Resource res = new TalendFileResourceImpl(uri);

        res.load(istream, null);
        setMetadataType((MetadataType) res.getContents().get(0));
    }
}
