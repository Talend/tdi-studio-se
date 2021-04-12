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
package org.talend.designer.dbmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.ui.runtime.image.ImageUtils.ICON_SIZE;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IODataComponent;
import org.talend.core.model.components.IODataComponentContainer;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.IConnection;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.core.ui.images.CoreImageProvider;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.dbmap.external.connection.IOConnection;
import org.talend.designer.dbmap.external.converter.ExternalDataConverter;
import org.talend.designer.dbmap.external.data.ExternalDbMapData;
import org.talend.designer.dbmap.external.data.ExternalDbMapUiProperties;
import org.talend.designer.dbmap.managers.MapperManager;
import org.talend.designer.dbmap.model.MapperModel;
import org.talend.designer.dbmap.model.table.InputTable;
import org.talend.designer.dbmap.model.table.OutputTable;
import org.talend.designer.dbmap.ui.MapperUI;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: MapperMain.java 1782 2007-02-03 07:57:38Z bqian $
 *
 */
public class MapperMain {

    private static boolean standAloneMode;

    private MapperManager mapperManager;

    private MapperModel mapperModel;

    private DbMapComponent connector;

    private List<IOConnection> ioInputConnections;

    private List<IOConnection> ioOutputConnections;

    public MapperMain(DbMapComponent connector) {
        super();
        mapperManager = new MapperManager(connector);
        this.connector = connector;
    }

    /**
     * Create a shell and init the mapper into it.
     *
     * @param display
     * @return the created shell
     */
    public Shell createUI(Display display) {
        Shell shell = new Shell(display, SWT.APPLICATION_MODAL | SWT.BORDER | SWT.RESIZE | SWT.CLOSE | SWT.MIN | SWT.MAX
                | SWT.TITLE);

        if (!MapperMain.isStandAloneMode()) {
            IComponent component = connector.getComponent();
            shell.setImage(CoreImageProvider.getComponentIcon(component, ICON_SIZE.ICON_32));
        }
        // Shell shell = new Shell(display);
        // shell.setImage(ImageProviderMapper.getImage(ImageInfo.MAPPER_ICON));
        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        String productName = brandingService.getFullProductName();
        shell.setText(productName + " - " + connector.getComponent().getName() + " - " + connector.getUniqueName()); //$NON-NLS-1$ //$NON-NLS-2$
        ExternalDbMapUiProperties uiProperties = mapperManager.getUiManager().getUiProperties();
        Rectangle boundsMapper = uiProperties.getBoundsMapper();
        if (uiProperties.isShellMaximized()) {
            shell.setMaximized(uiProperties.isShellMaximized());
        } else {
            // // move shell at outer of display zone to avoid visual effect on loading
            // Rectangle tmpBoundsMapper = new Rectangle(-boundsMapper.width, boundsMapper.y, boundsMapper.width,
            // boundsMapper.height);
            // shell.setBounds(tmpBoundsMapper);
            boundsMapper = uiProperties.getBoundsMapper();
            if (boundsMapper.x < 0) {
                boundsMapper.x = 0;
            }
            if (boundsMapper.y < 0) {
                boundsMapper.y = 0;
            }
            shell.setBounds(boundsMapper);
        }
        createUI(shell);
        shell.open();
        return shell;
    }

    /**
     * Init the mapper into the passed parent <code>Component</code>.
     *
     * @param parent
     */
    public void createUI(Composite parent) {
        MapperUI mapperUI = new MapperUI(parent, mapperManager);
        mapperUI.init(mapperModel);
    }

    public MapperManager getMapperManager() {
        return this.mapperManager;
    }

    public void loadModelFromInternalData() {
        this.mapperModel = new MapperModel(mapperManager.getInputTables(), mapperManager.getOutputTables());
    }

    /**
     *
     * DOC amaumont Comment method "getExternalData".
     *
     * @param refresh
     * @param loadFromInternalData else load from external data
     * @return
     */

    public ExternalDbMapData buildExternalData() {
        ExternalDataConverter converter = new ExternalDataConverter(mapperManager);
        return converter.prepareExternalData(mapperModel, mapperManager.getUiManager().getUiProperties());

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
            List<? extends IConnection> outgoingConnections, ExternalDbMapData externalData,
            List<IMetadataTable> outputMetadataTables, boolean checkProblems) {
        ioInputConnections = createIOConnections(incomingConnections);
        ioOutputConnections = createIOConnections(outgoingConnections);
        createModelFromExternalData(ioInputConnections, ioOutputConnections, outputMetadataTables, externalData, checkProblems);
    }

    /**
     * DOC amaumont Comment method "createIOConnections".
     *
     * @param connections
     * @return
     */
    public List<IOConnection> createIOConnections(List<? extends IConnection> connections) {
        ArrayList<IOConnection> ioConnections = new ArrayList<IOConnection>(connections.size());
        for (IConnection connection : connections) {
            ioConnections.add(new IOConnection(connection));
        }
        return Collections.unmodifiableList(ioConnections);
    }

    public void createModelFromExternalData(IODataComponentContainer ioDataContainer, List<IMetadataTable> outputMetadataTables,
            ExternalDbMapData externalData, boolean checkProblems) {
        initIOConnections(ioDataContainer);
        createModelFromExternalData(ioInputConnections, ioOutputConnections, outputMetadataTables, externalData, false);
    }

    public void initIOConnections(IODataComponentContainer ioDataContainer) {
        List<IODataComponent> inputsData = ioDataContainer.getInputs();
        List<IODataComponent> ouputsData = ioDataContainer.getOuputs();

        ioInputConnections = new ArrayList<IOConnection>(inputsData.size());
        for (IODataComponent iData : inputsData) {
            ioInputConnections.add(new IOConnection(iData));
        }
        ioOutputConnections = new ArrayList<IOConnection>(ouputsData.size());
        for (IODataComponent oData : ouputsData) {
            ioOutputConnections.add(new IOConnection(oData));
        }
    }

    public void createModelFromExternalData(List<IOConnection> inputs, List<IOConnection> outputs,
            List<IMetadataTable> outputMetadataTables, ExternalDbMapData externalData, boolean checkProblems) {
        if (externalData == null) {
            externalData = new ExternalDbMapData();
        }
        mapperManager.getUiManager().setUiProperties(externalData.getUiProperties());
        ExternalDataConverter converter = new ExternalDataConverter(mapperManager);
        if (this.mapperModel == null) {
            this.mapperModel = converter.prepareModel(inputs, outputs, outputMetadataTables, externalData, checkProblems);
        }
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
            metadataTables.add(table.getMetadataTable());
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

    public List<IOConnection> getIoInputConnections() {
        return this.ioInputConnections;
    }

    public List<IOConnection> getIoOutputConnections() {
        return this.ioOutputConnections;
    }

}
