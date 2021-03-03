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
package org.talend.designer.mapper;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.talend.core.model.components.IODataComponent;
import org.talend.core.model.components.IODataComponentContainer;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.IConnection;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.mapper.external.connection.IOConnection;
import org.talend.designer.mapper.external.converter.ExternalDataConverter;
import org.talend.designer.mapper.external.data.ExternalMapperData;
import org.talend.designer.mapper.managers.MapperManager;
import org.talend.designer.mapper.managers.UIManager;
import org.talend.designer.mapper.model.MapperModel;
import org.talend.designer.mapper.model.table.InputTable;
import org.talend.designer.mapper.model.table.OutputTable;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class MapperMain {

    private static boolean standAloneMode;

    private MapperManager mapperManager;

    private MapperModel mapperModel;

    private MapperComponent component;

    public MapperMain(MapperComponent component) {
        super();
        mapperManager = new MapperManager(component);
        this.component = component;
    }

    /**
     * Create a shell and init the mapper into it.
     *
     * @param display
     * @return the created shell
     */
    public Shell createUI(Display display) {
        UIManager uiManager = mapperManager.getUiManager();
        return uiManager.createUI(display, this.mapperModel);
    }

    /**
     * Init the mapper into the passed parent <code>Component</code>.
     *
     * @param parent
     */
    public void createUI(Composite parent) {
        UIManager uiManager = mapperManager.getUiManager();
        uiManager.createUI(parent, this.mapperModel);
    }

    public MapperManager getMapperManager() {
        return this.mapperManager;
    }

    public void loadModelFromInternalData() {
        this.mapperModel = new MapperModel(mapperManager.getInputTables(), mapperManager.getOutputTables(), mapperManager
                .getVarsTables());
    }

    /**
     *
     * DOC amaumont Comment method "getExternalData".
     *
     * @param refresh
     * @param loadFromInternalData else load from external data
     * @return
     */

    public ExternalMapperData buildExternalData() {
        ExternalDataConverter converter = new ExternalDataConverter(mapperManager);
        return converter.prepareExternalData(mapperModel, mapperManager.getUiManager().getUiProperties());

    }

    public ExternalMapperData buildTMAPExternalData() {
        ExternalDataConverter converter = new ExternalDataConverter(mapperManager);
        return converter.setTMapMess(mapperModel, mapperManager.getUiManager().getUiProperties());
    }

    /**
     * DOC amaumont Comment method "loadFromExternalData".
     *
     * @param incomingConnections
     * @param outgoingConnections
     * @param externalData
     * @param checkProblems TODO
     * @param metadataList
     */
    public void createModelFromExternalData(List<? extends IConnection> incomingConnections,
            List<? extends IConnection> outgoingConnections, ExternalMapperData externalData,
            List<IMetadataTable> outputMetadataTables, boolean checkProblems) {
        ArrayList<IOConnection> inputs = createIOConnections(incomingConnections);
        ArrayList<IOConnection> outputs = createIOConnections(outgoingConnections);
        createModelFromExternalData(inputs, outputs, outputMetadataTables, externalData, checkProblems);
    }

    /**
     * DOC amaumont Comment method "createIOConnections".
     *
     * @param connections
     * @return
     */
    public ArrayList<IOConnection> createIOConnections(List<? extends IConnection> connections) {
        ArrayList<IOConnection> ioConnections = new ArrayList<IOConnection>(connections.size());
        for (IConnection connection : connections) {
            ioConnections.add(new IOConnection(connection));
        }
        return ioConnections;
    }

    public void createModelFromExternalData(IODataComponentContainer ioDataContainer, List<IMetadataTable> outputMetadataTables,
            ExternalMapperData externalData, boolean checkProblems) {
        List<IODataComponent> inputsData = ioDataContainer.getInputs();
        List<IODataComponent> ouputsData = ioDataContainer.getOuputs();

        ArrayList<IOConnection> inputs = new ArrayList<IOConnection>(inputsData.size());
        for (IODataComponent iData : inputsData) {
            inputs.add(new IOConnection(iData));
        }
        ArrayList<IOConnection> outputs = new ArrayList<IOConnection>(ouputsData.size());
        for (IODataComponent oData : ouputsData) {
            outputs.add(new IOConnection(oData));
        }
        createModelFromExternalData(inputs, outputs, outputMetadataTables, externalData, false);
    }

    public void createModelFromExternalData(List<IOConnection> inputs, List<IOConnection> outputs,
            List<IMetadataTable> outputMetadataTables, ExternalMapperData externalData, boolean checkProblems) {
        if (externalData == null) {
            externalData = new ExternalMapperData();
        }
        mapperManager.getUiManager().setUiProperties(externalData.getUiProperties());
        ExternalDataConverter converter = new ExternalDataConverter(mapperManager);
        this.mapperModel = converter.prepareModel(inputs, outputs, outputMetadataTables, externalData, checkProblems);
    }

    /**
     * DOC amaumont Comment method "getResponse".
     *
     * @return
     */
    public int getMapperDialogResponse() {
        return mapperManager.getUiManager().getMapperResponse();
    }

    /**
     * DOC amaumont Comment method "getMetadataListIn".
     */
    public List<IMetadataTable> getMetadataListIn() {
        List<InputTable> tables = mapperManager.getInputTables();
        List<IMetadataTable> metadataTables = new ArrayList<IMetadataTable>(tables.size());
        for (InputTable table : tables) {
            metadataTables.add(table.getMetadataTable());
        }
        return metadataTables;
    }

    /**
     * DOC amaumont Comment method "getMetadataListOut".
     *
     * @return
     */
    public List<IMetadataTable> getMetadataListOut() {
        List<OutputTable> tables = mapperManager.getOutputTables();
        List<IMetadataTable> metadataTables = new ArrayList<IMetadataTable>(tables.size());
        for (OutputTable table : tables) {
            if (table.getIsJoinTableOf() == null) {
                metadataTables.add(table.getMetadataTable());
            }
        }
        return metadataTables;
    }

    public static boolean isStandAloneMode() {
        return standAloneMode;
    }

    public static void setStandAloneMode(boolean standAloneMode) {
        MapperMain.standAloneMode = standAloneMode;
    }

    public void loadInitialParamters() {
        this.mapperManager.updateEmfParameters(EParameterName.PREVIEW.getName());
    }

}
