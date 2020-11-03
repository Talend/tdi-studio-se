// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.model.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.ui.IEditorPart;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.components.api.properties.VirtualComponentProperties;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.hadoop.HadoopConfJarBean;
import org.talend.core.hadoop.IHadoopClusterService;
import org.talend.core.hadoop.repository.HadoopRepositoryUtil;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.EComponentType;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.components.IMultipleComponentConnection;
import org.talend.core.model.components.IMultipleComponentItem;
import org.talend.core.model.components.IMultipleComponentManager;
import org.talend.core.model.components.IMultipleComponentParameter;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.metadata.builder.connection.ConditionType;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.RuleType;
import org.talend.core.model.metadata.builder.connection.ValidationRulesConnection;
import org.talend.core.model.process.AbstractConnection;
import org.talend.core.model.process.AbstractExternalNode;
import org.talend.core.model.process.AbstractNode;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.ElementParameterParser;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IExternalData;
import org.talend.core.model.process.IExternalNode;
import org.talend.core.model.process.IGenericElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.process.IReplaceNodeInProcess;
import org.talend.core.model.process.ReplaceNodesInProcessProvider;
import org.talend.core.model.process.UniqueNodeNameGenerator;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.ValidationRulesConnectionItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.ExternalNodesFactory;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.utils.NodeUtil;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.IAdditionalInfo;
import org.talend.core.runtime.services.IGenericDBService;
import org.talend.core.ui.IJobletProviderService;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.PropertiesVisitor;
import org.talend.daikon.properties.property.PropertyValueEvaluator;
import org.talend.designer.core.CheckLogManamger;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.AbstractBasicComponent;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.model.process.jobsettings.JobSettingsManager;
import org.talend.designer.core.model.process.statsandlogs.StatsAndLogsManager;
import org.talend.designer.core.model.utils.emf.talendfile.AbstractExternalData;
import org.talend.designer.core.model.utils.emf.talendfile.RoutinesParameterType;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.utils.ConnectionUtil;
import org.talend.designer.core.utils.JavaProcessUtil;
import org.talend.designer.core.utils.ValidationRulesUtil;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * This class will create the list of nodes that will be used to generate the code.
 *
 * $Id$
 *
 */
public class DataProcess implements IGeneratingProcess {

    private static final String COMBINED_COMPONENT = "COMBINED_COMPONENT"; //$NON-NLS-1$

    private static final String COMBINED_PARAMETERS = "COMBINED_PARAMETERS"; //$NON-NLS-1$

    private static final String HASH_COMPONENT_NAME = "tHash"; //$NON-NLS-1$

    private static final String FSNODE_COMPONENT_NAME = "tFSNode"; //$NON-NLS-1$

    private static final String ELTNODE_COMPONENT_NAME = "tELTNode"; //$NON-NLS-1$

    private Map<INode, INode> buildCheckMap = null;

    private Map<String, INode> parallCheckMap = null;

    private BidiMap buildGraphicalMap = null;

    private List<INode> checkRefList = null;

    private List<INode> checkValidationList = null;

    private Map<INode, INode> checkMultipleMap = null;

    private Map<INode, INode> checkFileScaleMap = null;

    private Map<INode, INode> checkEltMap = null;

    private Map<INode, INode> checktUniteMap = null;

    private List<IConnection> connectionsToIgnoreInMerge = null;

    private List<INode> dataNodeList;

    private final IProcess process;

    private IProcess duplicatedProcess;

    private List<String> shortUniqueNameList = null;

    public DataProcess(IProcess process) {
        this.process = process;
    }

    private void initialize() {
        buildCheckMap = new HashMap<INode, INode>();
        parallCheckMap = new HashMap<String, INode>();
        checkRefList = new ArrayList<INode>();
        checkValidationList = new ArrayList<INode>();
        checkMultipleMap = new HashMap<INode, INode>();
        checktUniteMap = new HashMap<INode, INode>();
        dataNodeList = new ArrayList<INode>();
        checkFileScaleMap = new HashMap<INode, INode>();
        checkEltMap = new HashMap<INode, INode>();
        buildGraphicalMap = new DualHashBidiMap();
        connectionsToIgnoreInMerge = new ArrayList<IConnection>();
        shortUniqueNameList = new ArrayList<String>();
    }

    protected void copyElementParametersValue(IElement sourceElement, IElement targetElement) {
        if (IAdditionalInfo.class.isInstance(sourceElement) && IAdditionalInfo.class.isInstance(targetElement)) {
            IAdditionalInfo.class.cast(sourceElement).cloneAddionalInfoTo((IAdditionalInfo) targetElement);
        }
        for (IElementParameter sourceParam : sourceElement.getElementParameters()) {
            IElementParameter targetParam = targetElement.getElementParameter(sourceParam.getName());
            if (targetParam != null) {
                if (sourceParam.getName().equals(EParameterName.DB_TYPE.getName())
                        && sourceParam.getValue().toString().matches("^.*[a|A][c|C][c|C][e|E][s|S][s|S].*$")) {

                    sourceElement.getElementParameter(EParameterName.DBNAME.getName())
                            .setValue(sourceElement.getElementParameter(EParameterName.DBFILE.getName()).getValue());
                }

                targetParam.setContextMode(sourceParam.isContextMode());
                targetParam.setValue(sourceParam.getValue());
                if (sourceElement instanceof INode && sourceParam instanceof IGenericElementParameter) {
                    IComponent component = ((INode) sourceElement).getComponent();
                    if (component instanceof AbstractBasicComponent
                            && EComponentType.GENERIC.equals(component.getComponentType())) {
                        org.talend.daikon.properties.property.Property property = ((IGenericElementParameter) sourceParam)
                                .getProperty();
                        if (sourceParam.getFieldType().equals(EParameterFieldType.CLOSED_LIST) && property != null) {
                            PropertyValueEvaluator evaluator = property.getValueEvaluator();
                            if (evaluator != null) {
                                targetParam.setValue(evaluator.evaluate(property, sourceParam.getValue()));
                            }
                        }
                    }
                }
                if (sourceParam.getValue() instanceof List) {
                    List sourceList = (List) sourceParam.getValue();
                    List targetList = new ArrayList();
                    // if HashMap in List ,need clone deeply
                    for (Object map : sourceList) {
                        if (map instanceof HashMap) {
                            HashMap oldMap = (HashMap) map;
                            targetList.add(oldMap.clone());
                        }
                    }
                    if (targetList.size() > 0) {
                        targetParam.setValue(targetList);
                    } else {
                        targetParam.setValue(new ArrayList(sourceList));
                    }
                }
                if (targetParam.getFieldType() == EParameterFieldType.TABLE) {

                    // targetParam.setValue( sourceParam.getValue());
                    targetParam.setListItemsValue(ArrayUtils.clone(sourceParam.getListItemsValue()));
                    targetParam.setListItemsDisplayCodeName(sourceParam.getListItemsDisplayCodeName());
                }
                for (String name : targetParam.getChildParameters().keySet()) {
                    IElementParameter targetChildParam = targetParam.getChildParameters().get(name);
                    if (sourceParam.getChildParameters() == null) {
                        continue;
                    }
                    IElementParameter sourceChildParam = sourceParam.getChildParameters().get(name);
                    targetChildParam.setValue(sourceChildParam.getValue());
                    if (targetChildParam.getFieldType() == EParameterFieldType.TABLE) {
                        targetChildParam.setListItemsValue(sourceChildParam.getListItemsValue());
                        targetChildParam.setListItemsDisplayCodeName(sourceChildParam.getListItemsDisplayCodeName());
                    }
                }
            } else { // for feature TDI-24448,we need the new parameter here,do not init() it in the Connnection
                ElementParameter newParam = (ElementParameter) sourceParam.getClone();
                List<IElementParameter> listParam = (List<IElementParameter>) targetElement.getElementParameters();
                listParam.add(newParam);
            }
        }
    }

    private void combineElementParameters(INode sourceElement, INode targetElement) {
        IElementParameter combinedParameters = targetElement.getElementParameter(COMBINED_PARAMETERS);
        if (combinedParameters == null) {
            combinedParameters = new ElementParameter(targetElement);
            combinedParameters.setName(COMBINED_PARAMETERS);
            combinedParameters.setDisplayName(COMBINED_PARAMETERS);
            combinedParameters.setFieldType(EParameterFieldType.TECHNICAL);
            combinedParameters.setCategory(EComponentCategory.SQL_PATTERN);
            combinedParameters.setReadOnly(false);
            combinedParameters.setRequired(false);
            combinedParameters.setShow(false);
            combinedParameters.setValue(new ArrayList<IElementParameter>());
            ((List<IElementParameter>) targetElement.getElementParameters()).add(combinedParameters);
        }
        IElementParameter combinedComponent = new ElementParameter(targetElement);
        combinedComponent.setName(COMBINED_COMPONENT);
        combinedComponent.setDisplayName(COMBINED_PARAMETERS);
        combinedComponent.setFieldType(EParameterFieldType.TECHNICAL);
        combinedComponent.setCategory(EComponentCategory.SQL_PATTERN);
        combinedComponent.setReadOnly(false);
        combinedComponent.setRequired(false);
        combinedComponent.setShow(false);
        combinedComponent.setValue(((AbstractNode) sourceElement).getComponent().getName());
        ((List<IElementParameter>) combinedParameters.getValue()).add(combinedComponent);

        for (IElementParameter sourceParam : sourceElement.getElementParameters()) {
            if (sourceParam.getFieldType() == EParameterFieldType.SCHEMA_TYPE
                    || sourceParam.getFieldType() == EParameterFieldType.SCHEMA_REFERENCE) {
                if ("".equals(sourceParam.getContext()) || sourceParam.getContext() == null) {
                    sourceParam.setValue(sourceElement.getMetadataList().get(0));
                } else {
                    sourceParam.setValue(sourceElement.getMetadataFromConnector(sourceParam.getContext()));
                }
            }
            combinedComponent.getChildParameters().put(sourceParam.getName(), sourceParam);
        }
    }

    // should only be called by a starting node
    @SuppressWarnings("unchecked")
    public INode buildDataNodeFromNode(final INode graphicalNode, String prefix) {
        if (buildCheckMap == null) {
            initialize();
        }
        if (buildCheckMap.containsKey(graphicalNode)) {
            return buildCheckMap.get(graphicalNode);
        }

        AbstractNode dataNode = createDataNode(graphicalNode, prefix);

        INode addedNode = addDataNode(dataNode);
        buildCheckMap.put(graphicalNode, addedNode);

        List<IConnection> outgoingConnections = new ArrayList<IConnection>();
        List<IConnection> incomingConnections = new ArrayList<IConnection>();
        dataNode.setIncomingConnections(incomingConnections);
        dataNode.setOutgoingConnections(outgoingConnections);

        // if the component is a hash, and that there is a lookup connection just after, don't generate the node.
        // if (graphicalNode.getComponent().isHashComponent()) {
        // if (graphicalNode.getOutgoingConnections(EConnectionType.FLOW_REF).size() != 0) {
        // dataNode.setSubProcessStart(false);
        // }
        // }

        for (IConnection connection : graphicalNode.getOutgoingConnections()) {
            if (!connection.isActivate()) {
                continue;
            }
            IElementParameter monitorParam = connection.getElementParameter(EParameterName.MONITOR_CONNECTION.getName());
            if (monitorParam != null && (!connection.getLineStyle().equals(EConnectionType.FLOW_REF))
                    && ((Boolean) monitorParam.getValue())) {
                addvFlowMeterBetween(dataNode, buildDataNodeFromNode(connection.getTarget(), prefix), connection,
                        graphicalNode.getProcess(), connection.getElementParameters());
            } else {
                INode target = buildDataNodeFromNode(connection.getTarget(), prefix);
                createDataConnection(dataNode, (AbstractNode) target, connection, prefix);
            }
        }

        dataNode.setRealGraphicalNode(graphicalNode);

        return dataNode;
    }

    private DataConnection createDataConnection(AbstractNode sourceDataNode, AbstractNode targetDataNode, IConnection connection,
            String prefix) {
        DataConnection dataConnec = new DataConnection();
        dataConnec.setActivate(connection.isActivate());
        dataConnec.setLineStyle(connection.getLineStyle());
        dataConnec.setTraceConnection(connection.isTraceConnection());
        dataConnec.setTracesCondition(connection.getTracesCondition());
        dataConnec.setMonitorConnection(connection.isMonitorConnection());
        dataConnec.setEnabledTraceColumns(connection.getEnabledTraceColumns());
        if ((connection.getLineStyle().hasConnectionCategory(IConnectionCategory.EXECUTION_ORDER))
                && (connection.getTarget().getMetadataList().size() > 0)) {
            dataConnec.setMetadataTable(connection.getTarget().getMetadataList().get(0));
        } else {
            dataConnec.setMetadataTable(connection.getMetadataTable());
        }
        String name = connection.getName();
        if (prefix != null) {
            name = prefix + name;
        }
        dataConnec.setName(name);
        String uniqueName2 = connection.getUniqueName();
        if (prefix != null) {
            uniqueName2 = prefix + uniqueName2;
        }
        dataConnec.setUniqueName(uniqueName2);
        dataConnec.setSource(sourceDataNode);
        dataConnec.setCondition(connection.getCondition());
        dataConnec.setRouteConnectionType(connection.getRouteConnectionType());
        dataConnec.setEndChoice(connection.getEndChoice());// TESB-8043
        dataConnec.setExceptionList(connection.getExceptionList());
        dataConnec.setConnectorName(connection.getConnectorName());
        dataConnec.setInputId(connection.getInputId());
        dataConnec.setOutputId(connection.getOutputId());
        if (connection.getLineStyle().equals(EConnectionType.ITERATE)) {
            IElementParameter param = new ElementParameter(dataConnec);
            param.setFieldType(EParameterFieldType.CHECK);
            param.setCategory(EComponentCategory.BASIC);
            param.setValue(Boolean.FALSE);
            param.setName("ENABLE_PARALLEL"); //$NON-NLS-1$
            param.setDisplayName(Messages.getString("DataProcess.enableParallel")); //$NON-NLS-1$
            param.setShow(true);
            param.setNumRow(1);
            ((List<IElementParameter>) dataConnec.getElementParameters()).add(param);

            param = new ElementParameter(dataConnec);
            param.setFieldType(EParameterFieldType.TEXT);
            param.setCategory(EComponentCategory.BASIC);
            // param.setListItemsDisplayName(new String[] { "2", "3", "4" });
            // param.setListItemsDisplayCodeName(new String[] { "2", "3", "4" });
            // param.setListItemsValue(new String[] { "2", "3", "4" });
            param.setValue("2"); //$NON-NLS-1$
            param.setName("NUMBER_PARALLEL"); //$NON-NLS-1$
            param.setDisplayName(Messages.getString("DataProcess.numberParallel")); //$NON-NLS-1$
            param.setShow(true);
            param.setShowIf("ENABLE_PARALLEL == 'true'"); //$NON-NLS-1$
            param.setNumRow(1);
            ((List<IElementParameter>) dataConnec.getElementParameters()).add(param);
        }
        if (dataConnec.getLineStyle().hasConnectionCategory(IConnectionCategory.FLOW)) {
            IElementParameter param = new ElementParameter(dataConnec);
            param.setName(EParameterName.TRACES_CONNECTION_ENABLE.getName());
            param.setDisplayName(EParameterName.TRACES_CONNECTION_ENABLE.getDisplayName());
            param.setFieldType(EParameterFieldType.CHECK);
            param.setValue(Boolean.FALSE);
            param.setCategory(EComponentCategory.ADVANCED);
            param.setShow(false);
            param.setNumRow(1);
            ((List<IElementParameter>) dataConnec.getElementParameters()).add(param);
        }
        if (PluginChecker.isTeamEdition()) {

            if ((connection.getLineStyle() == EConnectionType.ON_SUBJOB_OK
                    || connection.getLineStyle() == EConnectionType.ON_SUBJOB_ERROR
                    || connection.getLineStyle() == EConnectionType.RUN_IF
                    || connection.getLineStyle() == EConnectionType.ROUTE_WHEN
                    || connection.getLineStyle() == EConnectionType.ROUTE_CATCH
                    || connection.getLineStyle() == EConnectionType.ON_COMPONENT_OK
                    || connection.getLineStyle() == EConnectionType.ON_COMPONENT_ERROR)) {
                IElementParameter param = new ElementParameter(dataConnec);
                param.setName(EParameterName.RESUMING_CHECKPOINT.getName());
                param.setValue(Boolean.FALSE);
                param.setDisplayName(EParameterName.RESUMING_CHECKPOINT.getDisplayName());
                param.setFieldType(EParameterFieldType.CHECK);
                param.setCategory(EComponentCategory.RESUMING);
                param.setNumRow(2);
                param.setShow(true);
                ((List<IElementParameter>) dataConnec.getElementParameters()).add(param); // breakpoint
            }

            if (dataConnec.getLineStyle().hasConnectionCategory(IConnectionCategory.FLOW)) {
                IElementParameter param = new ElementParameter(dataConnec);
                param.setName(EParameterName.ACTIVEBREAKPOINT.getName());
                param.setDisplayName(EParameterName.ACTIVEBREAKPOINT.getDisplayName());
                param.setFieldType(EParameterFieldType.CHECK);
                param.setCategory(EComponentCategory.BREAKPOINT);
                param.setNumRow(13);
                param.setValue(false);
                param.setContextMode(false);
                param.setShow(true);

                ((List<IElementParameter>) dataConnec.getElementParameters()).add(param);
                IComponent component = ComponentsFactoryProvider.getInstance().get("tFilterRow",
                        ComponentCategory.CATEGORY_4_DI.getName());
                DataNode tmpNode = new DataNode(component, "breakpointNode");
                IElementParameter tmpParam = tmpNode.getElementParameter("LOGICAL_OP");
                if (tmpParam != null) {
                    tmpParam.setCategory(EComponentCategory.BREAKPOINT);
                    tmpParam.setNumRow(14);
                    ((List<IElementParameter>) dataConnec.getElementParameters()).add(tmpParam);
                }
                tmpParam = tmpNode.getElementParameter("CONDITIONS");
                if (tmpParam != null) {
                    tmpParam.setCategory(EComponentCategory.BREAKPOINT);
                    tmpParam.setNumRow(15);
                    ((List<IElementParameter>) dataConnec.getElementParameters()).add(tmpParam);
                }

                tmpParam = tmpNode.getElementParameter("USE_ADVANCED");
                if (tmpParam != null) {
                    tmpParam.setCategory(EComponentCategory.BREAKPOINT);
                    tmpParam.setNumRow(16);
                    ((List<IElementParameter>) dataConnec.getElementParameters()).add(tmpParam);
                }
                tmpParam = tmpNode.getElementParameter("ADVANCED_COND");
                if (tmpParam != null) {
                    tmpParam.setCategory(EComponentCategory.BREAKPOINT);
                    tmpParam.setNumRow(17);
                    ((List<IElementParameter>) dataConnec.getElementParameters()).add(tmpParam);
                }
            }
        }
        copyElementParametersValue(connection, dataConnec);

        dataConnec.setTarget(targetDataNode);
        List<IConnection> incomingConnections = (List<IConnection>) targetDataNode.getIncomingConnections();
        if (incomingConnections == null) {
            incomingConnections = new ArrayList<IConnection>();
            targetDataNode.setIncomingConnections(incomingConnections);
        }
        List<IConnection> outgoingConnections = (List<IConnection>) sourceDataNode.getOutgoingConnections();
        if (outgoingConnections == null) {
            outgoingConnections = new ArrayList<IConnection>();
            sourceDataNode.setOutgoingConnections(outgoingConnections);
        }

        outgoingConnections.add(dataConnec);
        incomingConnections.add(dataConnec);

        if (!connection.getName().equals(name)) {
            if (targetDataNode instanceof AbstractExternalNode) {
                // System.out.println("dataProcess: rename input:" + connection.getName() + " to " + name);
                ((AbstractExternalNode) targetDataNode).renameInputConnection(connection.getName(), name);
            }
            if (sourceDataNode instanceof AbstractExternalNode) {
                // System.out.println("dataProcess: rename output:" + connection.getName() + " to " + name);
                ((AbstractExternalNode) sourceDataNode).renameOutputConnection(connection.getName(), name);
            }
        }

        return dataConnec;
    }

    private AbstractNode createDataNode(final INode graphicalNode, String prefix) {
        AbstractNode dataNode;
        if (graphicalNode.getExternalNode() == null) {
            dataNode = new DataNode();
        } else {
            // mapper
            dataNode = (AbstractNode) ExternalNodesFactory.getInstance(graphicalNode.getComponent().getPluginExtension());
            IExternalData externalData = graphicalNode.getExternalData();
            IExternalNode externalNode = graphicalNode.getExternalNode();
            if (externalData != null) {
                ((IExternalNode) dataNode).setExternalData(externalData);
            }
            // xmlmap
            if (externalNode != null) {
                ((IExternalNode) dataNode).setExternalEmfData(externalNode.getExternalEmfData());
                ((IExternalNode) dataNode).setInternalMapperModel(externalNode.getInternalMapperModel());
            }
        }
        dataNode.setReplaceNodeHandler(graphicalNode.getReplaceNodeHandler());
        dataNode.setActivate(graphicalNode.isActivate());
        dataNode.setStart(graphicalNode.isStart());

        // ==to fix the bug:TDI-28390, the metadataList should have flow metadata at 0 index==
        List<IMetadataTable> metadataList = new ArrayList<IMetadataTable>(graphicalNode.getMetadataList());
        // no need for externalnode, ELTNode
        // exist both flow and reject connectors, then we need to reorder it
        if (!dataNode.isExternalNode() && !dataNode.isELTComponent() && metadataList.size() > 1) {

            if ("REJECT".equalsIgnoreCase(metadataList.get(0).getAttachedConnector())) {
                IMetadataTable reject = metadataList.get(0);

                for (int i = 1; i < metadataList.size(); i++) {
                    metadataList.set(i - 1, metadataList.get(i));
                }
                metadataList.set(metadataList.size() - 1, reject);
            }
        }
        // ==========bug:TDI-28390 end=====================

        dataNode.setMetadataList(metadataList);
        dataNode.setComponent(graphicalNode.getComponent());
        if (graphicalNode.getComponentProperties() != null && graphicalNode.getComponent() != null
                && graphicalNode.getComponent() instanceof AbstractBasicComponent) {
            AbstractBasicComponent comp = (AbstractBasicComponent) graphicalNode.getComponent();
            comp.initNodePropertiesFromSerialized(dataNode, graphicalNode.getComponentProperties().toSerialized());
        }
        dataNode.setElementParameters(graphicalNode.getComponent().createElementParameters(dataNode));
        dataNode.setListConnector(graphicalNode.getListConnector());
        dataNode.setSubProcessContainTraceBreakpoint(graphicalNode.isSubProcessContainTraceBreakpoint());
        copyElementParametersValue(graphicalNode, dataNode);
        String uniqueName = graphicalNode.getUniqueName();
        if (prefix != null) {
            uniqueName = prefix + uniqueName;
        }
        dataNode.setUniqueName(uniqueName);
        dataNode.setSubProcessStart(graphicalNode.isSubProcessStart());
        dataNode.setThereLinkWithHash(graphicalNode.isThereLinkWithHash());
        dataNode.setHasConditionalOutputs(graphicalNode.hasConditionalOutputs());
        dataNode.setIsMultiplyingOutputs(graphicalNode.isMultiplyingOutputs());
        dataNode.setProcess(graphicalNode.getProcess());

        if (graphicalNode.isDummy() && !graphicalNode.isActivate()) {
            IComponent component = ComponentsFactoryProvider.getInstance().get("tDummyRow", //$NON-NLS-1$
                    ComponentCategory.CATEGORY_4_DI.getName());
            if (component != null) { // only if component is available
                dataNode = new DataNode(component, uniqueName);
                dataNode.setActivate(true);
                dataNode.setStart(graphicalNode.isStart());
                dataNode.setMetadataList(graphicalNode.getMetadataList());
                dataNode.setSubProcessStart(graphicalNode.isSubProcessStart());
                dataNode.setThereLinkWithHash(graphicalNode.isThereLinkWithHash());
                dataNode.setHasConditionalOutputs(false);
                dataNode.setIsMultiplyingOutputs(graphicalNode.isMultiplyingOutputs());
                dataNode.setProcess(graphicalNode.getProcess());
            }
        }
        dataNode.setDesignSubjobStartNode(graphicalNode.getDesignSubjobStartNode());
        return dataNode;
    }

    public INode buildDataNodeFromNode(final INode graphicalNode) {
        return buildDataNodeFromNode(graphicalNode, null);
    }

    @SuppressWarnings("unchecked")
    private INode addvFlowMeterBetween(INode sourceNode, INode targetNode, IConnection connection, IProcess process,
            List<? extends IElementParameter> parameters) {

        if (ComponentsFactoryProvider.getInstance().get("tFlowMeter", ComponentCategory.CATEGORY_4_DI.getName()) == null) { //$NON-NLS-1$
            return targetNode;
        }
        // from current node to vFlowMeter node.
        DataConnection dataConnec = new DataConnection();
        dataConnec.setActivate(connection.isActivate());
        if (connection.getLineStyle().hasConnectionCategory(IConnectionCategory.FLOW)) {
            dataConnec.setLineStyle(EConnectionType.FLOW_MAIN);
            IElementParameter param2 = new ElementParameter(dataConnec);
            param2.setName(EParameterName.TRACES_CONNECTION_ENABLE.getName());
            param2.setDisplayName(EParameterName.TRACES_CONNECTION_ENABLE.getDisplayName());
            param2.setFieldType(EParameterFieldType.CHECK);
            param2.setValue(Boolean.TRUE);
            param2.setCategory(EComponentCategory.ADVANCED);
            param2.setShow(false);
            param2.setNumRow(1);
            ((List<IElementParameter>) dataConnec.getElementParameters()).add(param2);
        } else {
            dataConnec.setLineStyle(connection.getLineStyle());
        }
        dataConnec.setTraceConnection(connection.isTraceConnection());
        dataConnec.setMonitorConnection(connection.isMonitorConnection());
        dataConnec.setTracesCondition(connection.getTracesCondition());
        dataConnec.setEnabledTraceColumns(connection.getEnabledTraceColumns());
        dataConnec.setMetadataTable(connection.getMetadataTable());
        dataConnec.setName(connection.getName());
        dataConnec.setUniqueName(connection.getUniqueName());
        dataConnec.setSource(sourceNode);
        dataConnec.setCondition(connection.getCondition());
        dataConnec.setConnectorName(connection.getConnectorName());
        dataConnec.setInputId(connection.getInputId());
        DataNode meterNode = new DataNode(
                ComponentsFactoryProvider.getInstance().get("tFlowMeter", ComponentCategory.CATEGORY_4_DI.getName()), //$NON-NLS-1$
                "vFlowMeter_" //$NON-NLS-1$
                        + connection.getUniqueName());
        meterNode.getMetadataList().get(0).setListColumns(connection.getMetadataTable().getListColumns());
        meterNode.setActivate(connection.isActivate());
        meterNode.setProcess(process);
        meterNode.setDesignSubjobStartNode(sourceNode.getDesignSubjobStartNode());
        for (IElementParameter param : parameters) {
            IElementParameter meterParam = meterNode.getElementParameter(param.getName());
            // for bug the same time use monitored and trace
            ((List<IElementParameter>) dataConnec.getElementParameters()).add(param);
            if (meterParam != null) {
                meterParam.setValue(param.getValue());
            }
        }
        dataConnec.setTarget(meterNode);
        ((List<IConnection>) meterNode.getIncomingConnections()).add(dataConnec);
        ((List<IConnection>) sourceNode.getOutgoingConnections()).add(dataConnec);
        addDataNode(meterNode);

        // from vFlowMeter node to next node.
        dataConnec = new DataConnection();
        dataConnec.setActivate(connection.isActivate());
        dataConnec.setLineStyle(connection.getLineStyle());

        dataConnec.setTraceConnection(connection.isTraceConnection());
        dataConnec.setTracesCondition(connection.getTracesCondition());
        dataConnec.setMonitorConnection(connection.isMonitorConnection());
        dataConnec.setEnabledTraceColumns(connection.getEnabledTraceColumns());
        dataConnec.setMetadataTable(meterNode.getMetadataList().get(0));
        dataConnec.setName(connection.getName());
        dataConnec.setUniqueName("meterRow" + connection.getUniqueName()); //$NON-NLS-1$
        dataConnec.setSource(meterNode);
        dataConnec.setCondition(connection.getCondition());
        dataConnec.setConnectorName(connection.getConnectorName());
        dataConnec.setInputId(connection.getInputId());
        dataConnec.setTarget(targetNode);
        if (connection instanceof DataConnection) {
            dataConnec.setLinkNodeForHash(((DataConnection) connection).getLinkNodeForHash());
            ((DataConnection) connection).setLinkNodeForHash(null);
        }
        ((List<IConnection>) targetNode.getIncomingConnections()).add(dataConnec);
        ((List<IConnection>) meterNode.getOutgoingConnections()).add(dataConnec);

        return meterNode;
    }

    /**
     * nrousseau Comment method "addMultipleNode".
     *
     * @param graphicalNode
     * @param multipleComponentManager
     * @return
     */
    @SuppressWarnings("unchecked")
    private void addMultipleNode(INode graphicalNode, List<IMultipleComponentManager> multipleComponentManagers)
            throws Exception {
        AbstractNode dataNode = null;
        // prepare all the nodes

        INode previousNode = buildCheckMap.get(graphicalNode);
        dataNodeList.remove(previousNode);

        for (IMultipleComponentManager multipleComponentManager : multipleComponentManagers) {
            if (multipleComponentManager.isLookupMode()) {
                // setup are only for hash component used in lookup
                continue;
            }

            Map<IMultipleComponentItem, AbstractNode> itemsMap = new HashMap<IMultipleComponentItem, AbstractNode>();
            prepareAllMultipleComponentNodes(itemsMap, multipleComponentManager, graphicalNode);
            setMultipleComponentParameters(multipleComponentManager, itemsMap, graphicalNode);

            // set the first one (input) with the properties of the graphical node.
            dataNode = itemsMap.get(multipleComponentManager.getInput());
            if (dataNode == null) {
                continue;
            }
            if (!dataNode.isActivate()) {
                dataNode = itemsMap.get(multipleComponentManager.getOutput());
                itemsMap.put(multipleComponentManager.getInput(), null);
            }
            dataNode.setStart(graphicalNode.isStart());
            dataNode.setSubProcessStart(graphicalNode.isSubProcessStart());
            dataNode.setThereLinkWithHash(graphicalNode.isThereLinkWithHash());
            List<IConnection> incomingConnections = (List<IConnection>) dataNode.getIncomingConnections();
            for (IConnection connection : previousNode.getIncomingConnections()) {
                if (connection.getLineStyle().hasConnectionCategory(IConnectionCategory.FLOW)) {
                    if (multipleComponentManager.isSetConnector()) {
                        String parameterString = "SCHEMA_" + multipleComponentManager.getConnector() + ":CONNECTION"; //$NON-NLS-1$ //$NON-NLS-2$
                        IElementParameter param = previousNode.getElementParameter(parameterString);
                        if (param != null) {
                            String tempLinkName = (String) param.getValue();
                            if (!connection.getName().equals(tempLinkName)) {
                                continue;
                            }
                        }
                    }
                }
                AbstractConnection asbractConnect = (AbstractConnection) connection;
                asbractConnect.setTarget(dataNode);
                incomingConnections.add(connection);
            }
            List<IConnection> outgoingConnections = (List<IConnection>) dataNode.getOutgoingConnections();

            // RunBefore / RunAfter Links won't be linked to the output but on the first element of the subprocess.
            for (IConnection connection : previousNode.getOutgoingConnections()) {
                if (connection.getLineStyle().hasConnectionCategory(IConnectionCategory.EXECUTION_ORDER)) {
                    AbstractConnection asbractConnect = (AbstractConnection) connection;
                    asbractConnect.setSource(dataNode);
                    outgoingConnections.add(0, connection);
                }
            }
            // for feature TDI-17041, Impossible to add imports from a virtual component.
            dataNode.getModulesNeeded().addAll(graphicalNode.getModulesNeeded());

            // set informations for the last node, so the outgoing connections.
            INode outputNode = itemsMap.get(multipleComponentManager.getOutput());
            outgoingConnections = (List<IConnection>) outputNode.getOutgoingConnections();
            if (multipleComponentManager.existsLinkTo()) {
                // RunBefore / RunAfter Links won't be linked to the output but on the first element of the subprocess.
                for (IConnection connection : previousNode.getOutgoingConnections()) {
                    if (!connection.getLineStyle().hasConnectionCategory(IConnectionCategory.EXECUTION_ORDER)) {
                        AbstractConnection asbractConnect = (AbstractConnection) connection;
                        asbractConnect.setSource(outputNode);
                        outgoingConnections.add(connection);
                    }
                }
            }

            // adds all connections between these nodes
            addAllMultipleComponentConnections(itemsMap, multipleComponentManager, graphicalNode, dataNode, previousNode);

            // return dataNode;
        }
    }

    /**
     * nrousseau Comment method "addAllMultipleComponentConnections".
     *
     * @param itemsMap
     * @param multipleComponentManager
     * @param graphicalNode
     * @param dataNode
     */
    @SuppressWarnings("unchecked")
    private void addAllMultipleComponentConnections(Map<IMultipleComponentItem, AbstractNode> itemsMap,
            IMultipleComponentManager multipleComponentManager, INode graphicalNode, AbstractNode dataNode, INode previousNode) {
        List<IConnection> incomingConnections, outgoingConnections;
        int inputID = 0;
        for (IMultipleComponentItem curItem : multipleComponentManager.getItemList()) {
        	AbstractNode nodeSource = itemsMap.get(curItem);
        	if (nodeSource == null) {
        		// This happens in case of virtual tHMap_Out component in first position (see prepareAllMultipleComponentNodes)
        		continue;
        	}
            for (IMultipleComponentConnection curConnec : curItem.getOutputConnections()) {

                AbstractNode nodeTarget;

                nodeTarget = itemsMap.get(curConnec.getTarget());

                DataConnection dataConnec = new DataConnection();
                dataConnec.setActivate(graphicalNode.isActivate());
                dataConnec.setLineStyle(EConnectionType.getTypeFromName(curConnec.getConnectionType()));
                dataConnec.setConnectorName(curConnec.getConnectorName());
                if (nodeSource.getMetadataList() != null) {
                    dataConnec.setMetadataTable(nodeSource.getMetadataList().get(0));
                }
                // INodeConnector connector = nodeSource.getConnectorFromName(curConnec.getConnectionType());
                // dataConnec.setLineStyle(connector.getDefaultConnectionType());
                // dataConnec.setConnectorName(curConnec.getConnectionType());
                // dataConnec.setMetadataTable(nodeSource.getMetadataFromConnector(curConnec.getConnectionType()));

                dataConnec.setName(EConnectionType.getTypeFromName(curConnec.getConnectionType()).getDefaultLinkName());

                switch (dataConnec.getLineStyle()) {
                case FLOW_MAIN:
                    dataConnec.setName("row_" + itemsMap.get(curItem).getUniqueName()); //$NON-NLS-1$
                    break;
                case FLOW_MERGE:
                    if (curConnec.equals(multipleComponentManager.getInput())) {
                        dataConnec.setInputId(1);
                    } else {
                        dataConnec.setInputId(1 + inputID++);
                    }
                    dataConnec.setName("merge_" + itemsMap.get(curItem).getUniqueName()); //$NON-NLS-1$
                    break;
                // case RUN_BEFORE:
                case ON_SUBJOB_ERROR:
                case ON_SUBJOB_OK:
                    // case RUN_AFTER:
                    if (nodeSource.equals(dataNode)) {
                        INode dataStartNode = ((DataNode) nodeSource).getSubProcessStartNode(false);
                        if (dataStartNode != previousNode) {
                            nodeSource = (AbstractNode) dataStartNode;
                        }
                        nodeTarget.setSubProcessStart(true);

                        List<IConnection> connectionsToRemoveFromList = new ArrayList<IConnection>();
                        outgoingConnections = (List<IConnection>) nodeSource.getOutgoingConnections();
                        for (IConnection connec : outgoingConnections) {
                            if (connec.getLineStyle().equals(curConnec.getConnectionType())) {
                                connectionsToRemoveFromList.add(connec);
                                AbstractConnection connection = (AbstractConnection) connec;
                                connection.setSource(nodeTarget);
                                nodeTarget.getIncomingConnections().remove(connec);
                                ((List<IConnection>) connec.getTarget().getIncomingConnections()).add(connec);
                            }
                        }
                        outgoingConnections.removeAll(connectionsToRemoveFromList);
                        outgoingConnections = (List<IConnection>) nodeTarget.getOutgoingConnections();
                        outgoingConnections.addAll(connectionsToRemoveFromList);

                        // notice: here deal with the case, if the tSortRow is in the second branch of the merge node.
                        if (graphicalNode.isThereLinkWithMerge()) {
                            Map<INode, Integer> linkedMergeInfo = graphicalNode.getLinkedMergeInfo();
                            INode firstMergeNode = NodeUtil.getFirstMergeNode(graphicalNode);
                            if (linkedMergeInfo.get(firstMergeNode) > 1) {
                                dataConnec.setLineStyle(EConnectionType.RUN_AFTER);
                                dataConnec.setConnectorName(EConnectionType.RUN_AFTER.getName());
                                dataConnec.setName(EConnectionType.ON_SUBJOB_OK.getDefaultLinkName());
                                AbstractNode tmp = nodeSource;
                                nodeSource = nodeTarget;
                                nodeTarget = tmp;
                            }
                        }

                    }

                    break;
                case ON_COMPONENT_OK:
                case ON_COMPONENT_ERROR:
                    nodeTarget.setSubProcessStart(true);
                    break;
                default:
                    break;
                }
                dataConnec.setSource(nodeSource);
                dataConnec.setTarget(nodeTarget);
                dataConnec.setCondition(""); //$NON-NLS-1$
                outgoingConnections = (List<IConnection>) nodeSource.getOutgoingConnections();
                int indexOfFirstThenRun = 0;
                for (IConnection connection : outgoingConnections) {
                    if (connection.getLineStyle().equals(EConnectionType.ON_SUBJOB_OK)) {
                        break;
                    }
                    indexOfFirstThenRun++;
                }
                // outgoingConnections.add(indexOfFirstThenRun, dataConnec);
                outgoingConnections.add(0, dataConnec);
                incomingConnections = (List<IConnection>) nodeTarget.getIncomingConnections();
                incomingConnections.add(dataConnec);
            }
        }
    }

    /**
     * nrousseau Comment method "prepareAllMultipleComponentNodes".
     *
     * @param itemsMap
     * @param multipleComponentManager
     * @param graphicalNode
     */
    private void prepareAllMultipleComponentNodes(Map<IMultipleComponentItem, AbstractNode> itemsMap,
            IMultipleComponentManager multipleComponentManager, INode graphicalNode) {

        List<IMultipleComponentItem> itemList = multipleComponentManager.getItemList();

        for (IMultipleComponentItem curItem : itemList) {
            String uniqueName = graphicalNode.getUniqueName() + "_" + curItem.getName(); //$NON-NLS-1$
            IComponentsFactory componentsFactory = ComponentsFactoryProvider.getInstance();
            String currentComponent = curItem.getComponent();
            IComponent component = componentsFactory.get(currentComponent, process.getComponentsType());
            if (component == null) { // If cannot find the component then find it by default DI category by default.
                component = componentsFactory.get(currentComponent, ComponentCategory.CATEGORY_4_DI.getName());
            }
            if (component == null) {
                continue;
            }

            AbstractNode curNode;
            if (component.getPluginExtension() == null) {
                curNode = new DataNode(component, uniqueName);
            } else {
                // mapper
                curNode = (AbstractNode) ExternalNodesFactory.getInstance(component.getPluginExtension());
                IExternalData externalData = graphicalNode.getExternalData();
                IExternalNode externalNode = graphicalNode.getExternalNode();
                if (externalData != null) {
                    ((IExternalNode) curNode).setExternalData(externalData);
                }
                // xmlmap
                if (externalNode != null) {
                    ((IExternalNode) curNode).setExternalEmfData(externalNode.getExternalEmfData());
                    ((IExternalNode) curNode).setInternalMapperModel(externalNode.getInternalMapperModel());
                }

                curNode.setStart(graphicalNode.isStart());
                curNode.setElementParameters(graphicalNode.getComponent().createElementParameters(curNode));
                curNode.setListConnector(graphicalNode.getListConnector());
                copyElementParametersValue(graphicalNode, curNode);
                curNode.setUniqueName(uniqueName);
                curNode.setSubProcessStart(graphicalNode.isSubProcessStart());
                curNode.setThereLinkWithHash(graphicalNode.isThereLinkWithHash());
                curNode.setHasConditionalOutputs(graphicalNode.hasConditionalOutputs());
                curNode.setIsMultiplyingOutputs(graphicalNode.isMultiplyingOutputs());

                // fixed 5379 bug by xzhang
                // curNode.setComponent(graphicalNode.getComponent());
                curNode.setComponent(component);
                List<IMetadataTable> metaList = new ArrayList<IMetadataTable>();
                IMetadataTable metaTable = new MetadataTable();
                metaTable.setTableName(uniqueName);
                metaList.add(metaTable);
                curNode.setMetadataList(metaList);
            }

            updateVirtualComponentProperties(graphicalNode.getComponentProperties(), curItem, curNode);

            curNode.setActivate(graphicalNode.isActivate());

            IMetadataTable newMetadata = null;
            if (multipleComponentManager.isSetConnector()) {
                newMetadata = graphicalNode.getMetadataFromConnector(multipleComponentManager.getConnector()).clone();
            } else {
                String sourceConnector = null;
                for (IMultipleComponentParameter param : multipleComponentManager.getParamList()) {
                    if (curItem.getName().equals(param.getTargetComponent())) {
                        for (IElementParameter paramComp : graphicalNode.getElementParameters()) {
                            if (param.getSourceValue().equals(paramComp.getName())) {
                                sourceConnector = paramComp.getContext();
                            }
                        }
                    }
                }
                if (sourceConnector == null) {
                    sourceConnector = EConnectionType.FLOW_MAIN.getName();
                }
                IMetadataTable metadataTable = graphicalNode.getMetadataFromConnector(sourceConnector);
                if (metadataTable != null) {
                    newMetadata = metadataTable.clone();
                } else if (!graphicalNode.getMetadataList().isEmpty()) {
                    newMetadata = graphicalNode.getMetadataList().get(0).clone();
                }
            }
            if (newMetadata != null) {
                newMetadata.setTableName(uniqueName);
            }
            if (graphicalNode.isDesignSubjobStartNode()) {
                curNode.setDesignSubjobStartNode(null);
            } else {
                curNode.setDesignSubjobStartNode(graphicalNode.getDesignSubjobStartNode());
            }

            // propagate metadataLists for output component. only apply to multi-input virtual component
            boolean isSAPBapi = graphicalNode.getComponent() != null && "tSAPBapi".equals(graphicalNode.getComponent().getName());//$NON-NLS-1$
            if (multipleComponentManager.isSetConnector()
                    && (multipleComponentManager.getOutputName().equals(curItem.getName()) || isSAPBapi)) {
                // deactivate dummy component
                if (curNode.getComponentName().equals("tDummyRow")) {// or use //$NON-NLS-1$
                    // "!multipleComponentManager.existsLinkTo()"
                    curNode.setActivate(false);
                } else {
                    // propagate all metadataTables
                    List<IMetadataTable> newMetadataList = new ArrayList<IMetadataTable>();
                    if (graphicalNode.getMetadataList() != null) {
                        for (IMetadataTable metadataTable : graphicalNode.getMetadataList()) {
                            newMetadataList.add(metadataTable.clone());
                        }
                    }
                    curNode.setMetadataList(newMetadataList);
                }

            } else {
                if (curNode.getMetadataList() != null && newMetadata != null) {
                    curNode.getMetadataList().remove(0);
                    curNode.getMetadataList().add(newMetadata);
                }
            }

            List<IConnection> outgoingConnections = new ArrayList<IConnection>();
            List<IConnection> incomingConnections = new ArrayList<IConnection>();
            curNode.setIncomingConnections(incomingConnections);
            curNode.setOutgoingConnections(outgoingConnections);
            curNode.setProcess(graphicalNode.getProcess());
            curNode.setVirtualGenerateNode(true);
            addDataNode(curNode);
            curNode.setRealGraphicalNode(graphicalNode);
            itemsMap.put(curItem, curNode);
            boolean rowsEnd = false;
            if (!curItem.getOutputConnections().isEmpty()) {
                rowsEnd = curItem.getOutputConnections().get(0).getConnectionType().equals(EConnectionType.ON_ROWS_END.getName());
            }
            if (curNode.isDesignSubjobStartNode() && rowsEnd && curItem.getComponent().equals("tHMapOut")) {
                curNode.setActivate(false);
                curNode.setStart(false);
            }
            curItem.updateNode(curNode, graphicalNode);
        }
    }

    private void updateVirtualComponentProperties(ComponentProperties componentProperties, IMultipleComponentItem curItem,
            INode curNode) {
        if (componentProperties instanceof VirtualComponentProperties) {
            VirtualComponentProperties virtualComponentProperties = (VirtualComponentProperties) componentProperties;
            if (curItem.getOutputConnections().size() > 0) { // input
                curNode.setComponentProperties(virtualComponentProperties.getInputComponentProperties());
            } else { // output
                curNode.setComponentProperties(virtualComponentProperties.getOutputComponentProperties());
            }
        }
    }

    /**
     * nrousseau Comment method "setMultipleComponentParameters".
     *
     * @param multipleComponentManager
     * @param itemsMap
     * @param graphicalNode
     */
    private void setMultipleComponentParameters(IMultipleComponentManager multipleComponentManager,
            Map<IMultipleComponentItem, AbstractNode> itemsMap, INode graphicalNode) {
        updateGenericComponentParameters(itemsMap, graphicalNode);
        List<IMultipleComponentItem> itemList = multipleComponentManager.getItemList();
        boolean targetFound;
        INode targetNode = null;
        for (int i = 0; i < itemList.size(); i++) {
            targetNode = itemsMap.get(itemList.get(i));
            if (targetNode == null) {
                continue;
            }
            // We set the type of the connection which linked two components in case of a virtual component. ONLY in
            // case of the first component
            if (itemList.get(i).getOutputConnections().size() > 0) {
                targetNode.setVirtualLinkTo(
                        EConnectionType.getTypeFromName(itemList.get(i).getOutputConnections().get(0).getConnectionType()));
            }
            targetFound = false;
            if (targetNode != null) {
                for (int j = 0; j < targetNode.getElementParameters().size() && !targetFound; j++) {
                    if (targetNode.getElementParameters().get(j).getName().equals(EParameterName.TSTATCATCHER_STATS.getName())) {
                        IElementParameter param = targetNode.getElementParameters().get(j);
                        boolean statsFound = false;
                        for (int k = 0; k < graphicalNode.getElementParameters().size() && !statsFound; k++) {
                            IElementParameter currentParam = graphicalNode.getElementParameters().get(k);
                            if (currentParam.getName().equals(EParameterName.TSTATCATCHER_STATS.getName())) {
                                param.setValue(currentParam.getValue());
                                statsFound = true;
                            }
                        }
                        targetFound = true;
                    }
                }
            } else {
                return;
            }
        }

        // set the specific parameters value.
        for (IMultipleComponentParameter param : multipleComponentManager.getParamList()) {
            INode sourceNode = null;
            boolean sourceFound = false;
            targetFound = false;
            for (int i = 0; i < itemList.size() && !targetFound; i++) {
                if (itemList.get(i).getName().equals(param.getTargetComponent())) {
                    targetNode = itemsMap.get(itemList.get(i));
                    targetFound = true;
                }
            }
            if (param.getSourceComponent() != null) { // target.value = source.value
                if (param.getSourceComponent().equals("self")) { //$NON-NLS-1$
                    sourceNode = graphicalNode;
                } else {
                    for (int i = 0; i < itemList.size() && !sourceFound; i++) {
                        if (itemList.get(i).getName().equals(param.getSourceComponent())) {
                            sourceNode = itemsMap.get(itemList.get(i));
                            sourceFound = true;
                        }
                    }
                }
                if ((sourceNode != null) && (targetNode != null)) {
                    sourceFound = false;
                    targetFound = false;
                    IElementParameter paramSource = null, paramTarget = null;
                    for (int i = 0; i < sourceNode.getElementParameters().size() && !sourceFound; i++) {
                        if (sourceNode.getElementParameters().get(i).getName().equals(param.getSourceValue())) {
                            paramSource = sourceNode.getElementParameters().get(i);
                            sourceFound = true;
                        }
                    }

                    for (int i = 0; i < targetNode.getElementParameters().size() && !targetFound; i++) {
                        if (targetNode.getElementParameters().get(i).getName().equals(param.getTargetValue())) {
                            paramTarget = targetNode.getElementParameters().get(i);
                            targetFound = true;
                        }
                    }
                    if ((paramSource != null) && (paramTarget != null)) {
                        paramTarget.setDefaultClosedListValue(paramSource.getDefaultClosedListValue());
                        paramTarget.setListItemsDisplayCodeName(paramSource.getListItemsDisplayCodeName());
                        paramTarget.setListItemsValue(paramSource.getListItemsValue());

                        // adjust destination value based on the connector name.(only apply to multi-input virtual
                        // component)
                        if (multipleComponentManager.isSetConnector() && param.getSourceComponent().equals("self") //$NON-NLS-1$
                                && param.getSourceValue().equals("UNIQUE_NAME") && param.getTargetValue().equals("DESTINATION")) { //$NON-NLS-1$ //$NON-NLS-2$
                            paramTarget.setValue(paramSource.getValue() + multipleComponentManager.getConnector());
                        } else {
                            paramTarget.setValue(paramSource.getValue());
                        }
                    }
                    if ((paramSource == null) && (paramTarget != null)) {
                        // set connection name to paramTarget
                        if (param.getSourceValue().endsWith(":CONNECTION")) { //$NON-NLS-1$
                            paramTarget.setValue(sourceNode.getElementParameter(param.getSourceValue()).getValue());
                        }
                    }
                }
            } else {
                if (param.getSourceValue() != null && targetFound) { // target.value = value
                    targetFound = false;
                    IElementParameter paramTarget = null;
                    for (int i = 0; i < targetNode.getElementParameters().size() && !targetFound; i++) {
                        if (targetNode.getElementParameters().get(i).getName().equals(param.getTargetValue())) {
                            paramTarget = targetNode.getElementParameters().get(i);
                            targetFound = true;
                        }
                    }
                    if (paramTarget != null) {
                        paramTarget.setValue(param.getSourceValue());
                    }
                }
            }
        }
    }

    private void updateGenericComponentParameters(Map<IMultipleComponentItem, AbstractNode> itemsMap, INode graphicalNode) {
        IComponent component = graphicalNode.getComponent();
        if (component instanceof AbstractBasicComponent && EComponentType.GENERIC.equals(component.getComponentType())) {
            AbstractBasicComponent theComponnent = (AbstractBasicComponent) component;
            Iterator<Entry<IMultipleComponentItem, AbstractNode>> itemsIterator = itemsMap.entrySet().iterator();
            while (itemsIterator.hasNext()) {
                Entry<IMultipleComponentItem, AbstractNode> itemsEntry = itemsIterator.next();
                AbstractNode node = itemsEntry.getValue();
                List<? extends IElementParameter> paramList = node.getElementParameters();
                for (IElementParameter param : paramList) {
                    if (!param.isSerialized()) {
                        continue;
                    }
                    Object object = theComponnent.getElementParameterValueFromComponentProperties(node, param);
                    param.setValue(object);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void checkFlowRefLink(final INode graphicalNode) {
        if (checkRefList.contains(graphicalNode)) {
            return;
        }
        checkRefList.add(graphicalNode);
        for (IConnection connection : graphicalNode.getOutgoingConnections()) {
            if (connection.getTarget().getJobletNode() != null && connection.getSource().getJobletNode() != null) {
                continue;
            }
            if (connection.isActivate() && connection.getLineStyle().hasConnectionCategory(IConnectionCategory.USE_HASH)) {

                boolean runAfter = true;
                INode target = connection.getTarget();
                if (target != null) {
                    IExternalNode externalNode = target.getExternalNode();
                    if (externalNode != null) {
                        runAfter = externalNode.isRunRefSubProcessAtStart(connection.getUniqueName());
                    }

                    // if (externalNode.getElementName().equals("tmap")) {
                    // INode parallelizeNode = addVParallelizeBetween();
                    // }
                }

                INode refSource = buildCheckMap.get(graphicalNode);

                if (refSource.getComponent().isHashComponent()) {
                    continue;// If hash component, No need to create any virtual components.
                }

                // retrieve the starts node of each current nodes to add a before link
                INode subNodeStartTarget = graphicalNode.getSubProcessStartNode(true);
                INode subNodeStartSource = connection.getTarget().getSubProcessStartNode(false);

                AbstractNode subDataNodeStartSource = (AbstractNode) buildCheckMap.get(subNodeStartSource);
                AbstractNode subDataNodeStartTarget = (AbstractNode) buildCheckMap.get(subNodeStartTarget);

                if (subDataNodeStartSource == null || subDataNodeStartTarget == null) {
                    // means the graphic process is not complete, so ignore it.
                    continue;
                }

                // if (subDataNodeStartSource.getMetadataList().isEmpty()) {
                // continue;
                // }

                List<IConnection> outgoingConnections = (List<IConnection>) subDataNodeStartSource.getOutgoingConnections();
                List<IConnection> incomingConnections = (List<IConnection>) subDataNodeStartTarget.getIncomingConnections();
                DataConnection dataConnec = null;

                if (runAfter) {
                    boolean isParall = false;
                    IComponent paralComponent = ComponentsFactoryProvider.getInstance().get("tParallelize", //$NON-NLS-1$
                            ComponentCategory.CATEGORY_4_DI.getName());
                    if (paralComponent != null) {
                        // if tParallelize component is available, then allow to use the feature lookup parallelize
                        for (IConnection conn : graphicalNode.getOutgoingConnections()) {
                            if (conn.getTarget().getComponent().getName().equals("tMap")) {//$NON-NLS-1$
                                IElementParameter elePara = conn.getTarget().getElementParameter("LKUP_PARALLELIZE");//$NON-NLS-1$
                                isParall = (Boolean) elePara.getValue();
                                break;
                            }
                        }
                    }

                    if (isParall) {
                        addVParallelizeBetween(subDataNodeStartSource, subDataNodeStartTarget, connection, duplicatedProcess,
                                connection.getElementParameters());
                    } else {
                        // create a link before between the two subprocess
                        dataConnec = new DataConnection();
                        dataConnec.setActivate(connection.isActivate());
                        dataConnec.setLineStyle(EConnectionType.RUN_AFTER);
                        dataConnec.setTraceConnection(connection.isTraceConnection());
                        dataConnec.setTracesCondition(connection.getTracesCondition());
                        dataConnec.setEnabledTraceColumns(connection.getEnabledTraceColumns());
                        dataConnec.setMonitorConnection(connection.isMonitorConnection());
                        // dataConnec.setLineStyle(EConnectionType.THEN_RUN);
                        if (!subDataNodeStartSource.getMetadataList().isEmpty()) {
                            dataConnec.setMetadataTable(subDataNodeStartSource.getMetadataList().get(0));
                        }
                        dataConnec.setName("after_" + subDataNodeStartSource.getUniqueName()); //$NON-NLS-1$
                        // dataConnec.setConnectorName(EConnectionType.THEN_RUN.getName());
                        dataConnec.setConnectorName(EConnectionType.RUN_AFTER.getName());
                        dataConnec.setSource(subDataNodeStartSource);
                        // dataConnec.setSource(subDataNodeStartTarget);
                        dataConnec.setTarget(subDataNodeStartTarget);
                        // dataConnec.setTarget(subDataNodeStartSource);

                        // the target component can't be start in all case, so no matter where it has been
                        // defined,remove
                        // the start state.
                        subDataNodeStartTarget.setStart(false);

                        outgoingConnections.add(dataConnec);
                        incomingConnections.add(dataConnec);
                    }

                }

                // add a new hash node
                // (to replace by a Node maybe that will take the informations of an IComponent)
                String uniqueName = null;
                IComponent component = null;
                String hashComponent = null;

                //TODO remove the two statements as we can call getTargetNodeConnector directly
                String baseConnector = connection.getSource().getConnectorFromName(connection.getConnectorName()).getBaseSchema();
                INodeConnector connector = connection.getTarget().getConnectorFromName(baseConnector);

                if(connector == null) {
                	connector = connection.getTargetNodeConnector();
                }

                if (connector != null) {
                    hashComponent = connector.getConnectionProperty(EConnectionType.FLOW_REF).getLinkedComponent();
                }

                if (hashComponent == null) {
                    uniqueName = HASH_COMPONENT_NAME + "_" + connection.getName(); //$NON-NLS-1$
                    component = ComponentsFactoryProvider.getInstance().get(HASH_COMPONENT_NAME,
                            ComponentCategory.CATEGORY_4_DI.getName());
                } else {
                    uniqueName = hashComponent + "_" + connection.getName(); //$NON-NLS-1$
                    component = ComponentsFactoryProvider.getInstance().get(hashComponent,
                            ComponentCategory.CATEGORY_4_DI.getName());
                }
                if (component == null) {
                    continue;
                }
                DataNode hashNode = new DataNode(component, uniqueName);
                hashNode.setActivate(connection.isActivate());
                hashNode.setStart(false);
                hashNode.setDesignSubjobStartNode(graphicalNode.getDesignSubjobStartNode());
                IMetadataTable newMetadata = connection.getMetadataTable().clone();
                newMetadata.setTableName(uniqueName);
                hashNode.getMetadataList().remove(0);
                hashNode.getMetadataList().add(newMetadata);
                hashNode.setSubProcessStart(false);
                hashNode.setProcess(graphicalNode.getProcess());
                outgoingConnections = new ArrayList<IConnection>();
                incomingConnections = new ArrayList<IConnection>();
                hashNode.setIncomingConnections(incomingConnections);
                hashNode.setOutgoingConnections(outgoingConnections);
                // hashNode.setVirtualGenerateNode(true);

                if (hashComponent != null) {
                    List<IMultipleComponentManager> mcms = connection.getTarget().getComponent().getMultipleComponentManagers();
                    for (IMultipleComponentManager mcm : mcms) {
                        if (mcm.isLookupMode()) {
                            Map<IMultipleComponentItem, AbstractNode> itemsMap = new HashMap<IMultipleComponentItem, AbstractNode>();
                            mcm.getItemList().clear();
                            mcm.addItem("LOOKUP", hashComponent);//$NON-NLS-1$
                            itemsMap.put(mcm.getItemList().get(0), hashNode);
                            setMultipleComponentParameters(mcm, itemsMap, connection.getTarget());
                        }
                    }
                }

                addDataNode(hashNode);

                // create a link flow_main between the node that had ref and the hash file
                dataConnec = new DataConnection();
                dataConnec.setActivate(connection.isActivate());
                dataConnec.setLineStyle(EConnectionType.FLOW_MAIN);
                dataConnec.setMetadataTable(newMetadata);
                dataConnec.setTraceConnection(connection.isTraceConnection());
                dataConnec.setMonitorConnection(connection.isMonitorConnection());
                dataConnec.setTracesCondition(connection.getTracesCondition());
                dataConnec.setEnabledTraceColumns(connection.getEnabledTraceColumns());
                dataConnec.setName(connection.getName());
                dataConnec.setSource(refSource);
                dataConnec.setTarget(hashNode);
                dataConnec.setLinkNodeForHash(buildCheckMap.get(connection.getTarget()));
                dataConnec.setConnectorName(connection.getConnectorName());

                IElementParameter monitorParam = connection.getElementParameter(EParameterName.MONITOR_CONNECTION.getName());
                // if there is a monitor on this connection, then add the vFlowMeter and move the base lookup connection
                // source from "graphicalNode" to the new meterNode.
                if (monitorParam != null && ((Boolean) monitorParam.getValue())) {
                    INode meterNode = addvFlowMeterBetween(refSource, hashNode, dataConnec, graphicalNode.getProcess(),
                            connection.getElementParameters());
                    // move the FLOW_REF link from "refSource" to the "meterNode".
                    List<IConnection> connectionRefList = (List<IConnection>) refSource
                            .getOutgoingConnections(EConnectionType.FLOW_REF);
                    IConnection connecToMove = null;
                    for (IConnection curConnection : connectionRefList) {
                        if (curConnection.getSource().equals(buildCheckMap.get(connection.getSource()))
                                && curConnection.getTarget().equals(buildCheckMap.get(connection.getTarget()))) {
                            connecToMove = curConnection;
                        }
                    }
                    if (connecToMove != null) {
                        ((List<IConnection>) refSource.getOutgoingConnections()).remove(connecToMove);
                        ((List<IConnection>) meterNode.getOutgoingConnections()).add(0, connecToMove);
                        ((DataConnection) connecToMove).setSource(meterNode);
                        ((DataConnection) connecToMove).setName(connecToMove.getName());
                        ((DataConnection) connecToMove).setUniqueName("meterRow" + connecToMove.getUniqueName()); //$NON-NLS-1$
                    }
                } else {
                    outgoingConnections = (List<IConnection>) refSource.getOutgoingConnections();
                    outgoingConnections.add(dataConnec);
                    incomingConnections = (List<IConnection>) hashNode.getIncomingConnections();
                    incomingConnections.add(dataConnec);
                }
            }
            checkFlowRefLink(connection.getTarget());
        }
    }

    /**
     * nrousseau Comment method "replaceMultipleComponents".
     *
     * @param node
     */
    private void replaceMultipleComponents(INode graphicalNode) {
        if (checkMultipleMap.containsKey(graphicalNode)) {
            // return checkMultipleMap.get(graphicalNode);
            return;
        }
        // if the original component is in dummy state, no need to replace it
        // but the next need to be checked.
        if (graphicalNode.isDummy() && !graphicalNode.isActivate()) {
            // return;
        } else {
            AbstractNode dataNode;
            dataNode = (AbstractNode) buildCheckMap.get(graphicalNode);
            checkMultipleMap.put(graphicalNode, dataNode);
            if (dataNode.isGeneratedAsVirtualComponent()) {
                List<IMultipleComponentManager> multipleComponentManagers = graphicalNode.getComponent()
                        .getMultipleComponentManagers();
                try {
                    addMultipleNode(graphicalNode, multipleComponentManagers);
                } catch (Exception e) {
                    Exception warpper = new Exception(Messages.getString("DataProcess.checkComponent", graphicalNode.getLabel()), //$NON-NLS-1$
                            e);
                    ExceptionHandler.process(warpper);
                }
            }
        }
        for (IConnection connection : graphicalNode.getOutgoingConnections()) {
            if (connection.isActivate()) {
                replaceMultipleComponents(connection.getTarget());
            }
        }
    }

    private void replaceEltComponents(INode graphicalNode) {
        if (checkEltMap.containsKey(graphicalNode)) {
            return;
        }
        // if the original component is in dummy state, no need to replace it
        // but the next need to be checked.
        if (graphicalNode.isDummy() && !graphicalNode.isActivate()) {
            for (IConnection connection : graphicalNode.getOutgoingConnections()) {
                if (connection.isActivate()) {
                    replaceEltComponents(connection.getTarget());
                }
            }
            return;
        }

        INode currentComponent = graphicalNode;
        AbstractNode dataNode;

        dataNode = (AbstractNode) buildCheckMap.get(graphicalNode);
        checkEltMap.put(graphicalNode, dataNode);
        boolean needCreateTELTNode = false;
        boolean loopEnd = dataNode == null || !ELTNODE_COMPONENT_NAME.equals(currentComponent.getComponent().getCombine());

        DataNode eltNode = null, oldFsNode = null;
        while (!loopEnd) {
            List<IConnection> flowConnections = (List<IConnection>) NodeUtil.getOutgoingConnections(currentComponent,
                    IConnectionCategory.FLOW);
            dataNodeList.remove(dataNode);
            buildCheckMap.remove(currentComponent);
            if (eltNode != null) {
                needCreateTELTNode = false;// needCreateNewEltNode(eltNode, dataNode);
                oldFsNode = eltNode;
            }

            // add the tELTNode component if this one is not already added to the list.
            if (eltNode == null || needCreateTELTNode) {
                // Create the new elt component
                IComponent component = ComponentsFactoryProvider.getInstance().get(ELTNODE_COMPONENT_NAME,
                        ComponentCategory.CATEGORY_4_DI.getName());
                if (component == null) {
                    break;
                }
                eltNode = new DataNode(component, currentComponent.getUniqueName());
                eltNode.setActivate(currentComponent.isActivate());
                eltNode.setStart(currentComponent.isStart());
                eltNode.setDesignSubjobStartNode(currentComponent.getDesignSubjobStartNode());

                IMetadataTable newMetadata = currentComponent.getMetadataList().get(0).clone();
                newMetadata.setTableName(currentComponent.getUniqueName());
                eltNode.getMetadataList().remove(0);
                eltNode.getMetadataList().addAll(currentComponent.getMetadataList());

                eltNode.setSubProcessStart(currentComponent.isSubProcessStart() || needCreateTELTNode);
                eltNode.setProcess(currentComponent.getProcess());
                List<IConnection> outgoingConnections = new ArrayList<IConnection>();
                List<IConnection> incomingConnections = new ArrayList<IConnection>();
                eltNode.setIncomingConnections(incomingConnections);
                eltNode.setOutgoingConnections(outgoingConnections);
                addDataNode(eltNode);
            } else {
                // bug15885, add metadatas of connector
                eltNode.getMetadataList().addAll(currentComponent.getMetadataList());
            }

            combineElementParameters(dataNode, eltNode);

            if (flowConnections.isEmpty() || buildCheckMap.get(flowConnections.get(0).getTarget()) == null) {
                loopEnd = true;
            } else {
                loopEnd = !ELTNODE_COMPONENT_NAME.equals(currentComponent.getComponent().getCombine());
            }

            setReplacedNodeConnections(eltNode, dataNode, oldFsNode, needCreateTELTNode, loopEnd);

            if (!flowConnections.isEmpty()) {
                // initialize with next component for next loop
                currentComponent = flowConnections.get(0).getTarget();
                dataNode = (AbstractNode) buildCheckMap.get(currentComponent);
                needCreateTELTNode = false;
            }
        }
        for (IConnection connection : currentComponent.getOutgoingConnections()) {
            if (connection.isActivate()) {
                replaceEltComponents(connection.getTarget());
            }
        }

    }

    private boolean needCreateNewEltNode(DataNode eltNode, AbstractNode dataNode) {
        boolean needCreateTFSNode = false;
        // check if use the same dbtype, db and table
        IElementParameter fsNodeParam = eltNode.getElementParameter("DBTYPE"); //$NON-NLS-1$
        IElementParameter param = dataNode.getElementParameter("DBTYPE"); //$NON-NLS-1$
        if (param != null) {
            if (!param.getValue().equals(fsNodeParam.getValue())) {
                needCreateTFSNode = true;
            }
            // else {
            // IElementParameter eltdbParam = eltNode.getElementParameter("COMPONENT_" + param.getValue());
            // //$NON-NLS-1$
            // IElementParameter dbParam = dataNode.getElementParameter("COMPONENT_" + param.getValue()); //$NON-NLS-1$
            // if (dbParam != null) {
            // if (!dbParam.getValue().equals(eltdbParam.getValue())) {
            // needCreateTFSNode = true;
            // // can check if the two node connect to the same database ,if yes no need to create a
            // // new node
            // //
            // } else {
            // IElementParameter eltSourcetableParam = eltNode.getElementParameter("TABLE_NAME"); //$NON-NLS-1$
            // // for tELTMerge
            // if (eltSourcetableParam == null) {
            // eltSourcetableParam = eltNode.getElementParameter("SOURCE_TABLE"); //$NON-NLS-1$
            // }
            // IElementParameter sourceTableParam = dataNode.getElementParameter("TABLE_NAME"); //$NON-NLS-1$
            // // for tELTMerge
            // if (sourceTableParam == null) {
            // sourceTableParam = dataNode.getElementParameter("SOURCE_TABLE"); //$NON-NLS-1$
            // }
            //
            // if (sourceTableParam != null && eltSourcetableParam != null) {
            // if (!sourceTableParam.getValue().equals(eltSourcetableParam.getValue())) {
            // needCreateTFSNode = true;
            // }
            // }
            // IElementParameter eltTargetTableParam = eltNode.getElementParameter("TABLE_NAME_TARGET"); //$NON-NLS-1$
            // if (eltTargetTableParam == null) {
            // eltTargetTableParam = eltNode.getElementParameter("TARGET_TABLE"); //$NON-NLS-1$
            // }
            // IElementParameter targetTableParam = dataNode.getElementParameter("TABLE_NAME_TARGET"); //$NON-NLS-1$
            // if (targetTableParam == null) {
            // targetTableParam = dataNode.getElementParameter("TARGET_TABLE"); //$NON-NLS-1$
            // }
            //
            // if (targetTableParam != null && eltTargetTableParam != null) {
            // if (!targetTableParam.getValue().equals(eltTargetTableParam.getValue())) {
            // needCreateTFSNode = true;
            // }
            // }
            //
            // }
            // }
            // }
        }

        return needCreateTFSNode;

    }

    private void setReplacedNodeConnections(DataNode realNode, AbstractNode dataNode, DataNode oldFsNode, boolean needCreateNode,
            boolean loopEnd) {
        // replug each non-flow output connection, to the realNode(tSFNode,tELTNode).
        // if end of the loop or need new realNode, need to copy all links
        for (IConnection connection : dataNode.getOutgoingConnections()) {
            if (connection.isActivate()) {
                if (loopEnd || !connection.getLineStyle().hasConnectionCategory(IConnectionCategory.FLOW)) {
                    ((List<IConnection>) realNode.getOutgoingConnections()).add(connection);
                    ((DataConnection) connection).setSource(realNode);
                }
            }
        }
        // replug each non-flow input connection, to the realNode.
        // if end of the loop or need new realNode, need to copy all links
        for (IConnection connection : dataNode.getIncomingConnections()) {
            if (connection.isActivate()) {
                if (!connection.getLineStyle().hasConnectionCategory(IConnectionCategory.FLOW)) {
                    ((List<IConnection>) realNode.getIncomingConnections()).add(connection);
                    ((DataConnection) connection).setTarget(realNode);
                }
                if (needCreateNode) {
                    // replug between different realNode.
                    ((List<IConnection>) realNode.getIncomingConnections()).add(connection);
                    ((List<IConnection>) oldFsNode.getOutgoingConnections()).add(0, connection);
                    ((DataConnection) connection).setTarget(realNode);
                    ((DataConnection) connection).setSource(oldFsNode);
                    ((DataConnection) connection).setLineStyle(EConnectionType.ON_SUBJOB_OK);
                    ((DataConnection) connection).setConnectorName(EConnectionType.ON_SUBJOB_OK.getName());
                }
            }
        }
    }

    private void replaceFileScalesComponents(INode graphicalNode) {
        if (checkFileScaleMap.containsKey(graphicalNode)) {
            // return checkMultipleMap.get(graphicalNode);
            return;
        }
        // if the original component is in dummy state, no need to replace it
        // but the next need to be checked
        if (graphicalNode.isDummy() && !graphicalNode.isActivate()) {
            for (IConnection connection : graphicalNode.getOutgoingConnections()) {
                if (connection.isActivate()) {
                    replaceFileScalesComponents(connection.getTarget());
                }
            }
            return;
        }

        INode currentComponent = graphicalNode;
        AbstractNode dataNode;

        dataNode = (AbstractNode) buildCheckMap.get(graphicalNode);
        checkFileScaleMap.put(graphicalNode, dataNode);
        boolean needCreateTFSNode = false;
        boolean loopEnd = dataNode == null || !FSNODE_COMPONENT_NAME.equals(currentComponent.getComponent().getCombine());

        Set<INode> progressBarList = null;
        DataNode fsNode = null, oldFsNode = null;
        while (!loopEnd) {
            List<IConnection> flowConnections = (List<IConnection>) NodeUtil.getOutgoingConnections(currentComponent,
                    IConnectionCategory.FLOW);
            // remove the current FS component from the list
            dataNodeList.remove(dataNode);
            buildCheckMap.remove(currentComponent);
            if (fsNode != null) {
                // check if use the same files, to see if must create a new tFSNode or not.
                IElementParameter fsNodeParam = fsNode.getElementParameter("INPUT_FILENAME"); //$NON-NLS-1$
                IElementParameter param = dataNode.getElementParameter("INPUT_FILENAME"); //$NON-NLS-1$
                if (param != null) {
                    if (!param.getValue().equals(fsNodeParam.getValue())) {
                        needCreateTFSNode = true;
                    }
                }
                fsNodeParam = fsNode.getElementParameter("OUTPUT_FILENAME"); //$NON-NLS-1$
                param = dataNode.getElementParameter("OUTPUT_FILENAME"); //$NON-NLS-1$
                if (param != null) {
                    if (!param.getValue().equals(fsNodeParam.getValue())) {
                        needCreateTFSNode = true;
                    }
                }
                oldFsNode = fsNode;
            }
            INode originalGraphicNode = null;
            if (buildGraphicalMap.getKey(currentComponent) != null) {
                originalGraphicNode = (INode) buildGraphicalMap.getKey(currentComponent);
            }

            // add the fs component if this one is not already added to the list.
            if (fsNode == null || needCreateTFSNode) {
                if (originalGraphicNode != null) {
                    progressBarList = originalGraphicNode.fsComponentsInProgressBar();
                    progressBarList.clear();
                }
                // Create the new FS component
                IComponent component = ComponentsFactoryProvider.getInstance().get(FSNODE_COMPONENT_NAME,
                        ComponentCategory.CATEGORY_4_DI.getName());
                if (component == null) {
                    break;
                }
                fsNode = new DataNode(component, currentComponent.getUniqueName());
                fsNode.setActivate(currentComponent.isActivate());
                fsNode.setStart(currentComponent.isStart());
                fsNode.setDesignSubjobStartNode(currentComponent.getDesignSubjobStartNode());

                IMetadataTable newMetadata = currentComponent.getMetadataList().get(0).clone();
                newMetadata.setTableName(currentComponent.getUniqueName());
                fsNode.getMetadataList().remove(0);
                // bug15885, for tFSJoin to support FS Combain link,
                // fsNode.getMetadataList().add(newMetadata);
                fsNode.getMetadataList().addAll(currentComponent.getMetadataList());

                fsNode.setSubProcessStart(currentComponent.isSubProcessStart() || needCreateTFSNode);
                fsNode.setProcess(currentComponent.getProcess());
                List<IConnection> outgoingConnections = new ArrayList<IConnection>();
                List<IConnection> incomingConnections = new ArrayList<IConnection>();
                fsNode.setIncomingConnections(incomingConnections);
                fsNode.setOutgoingConnections(outgoingConnections);
                addDataNode(fsNode);
            } else {
                // bug15885, add metadatas of connector
                fsNode.getMetadataList().addAll(currentComponent.getMetadataList());
            }
            if (progressBarList != null && originalGraphicNode != null) {
                progressBarList.add(originalGraphicNode);
            }

            // copy to remove once combine parameters is used in FS components
            copyElementParametersValue(dataNode, fsNode);

            combineElementParameters(dataNode, fsNode);

            if (flowConnections.isEmpty() || buildCheckMap.get(flowConnections.get(0).getTarget()) == null) {
                loopEnd = true;
            } else {
                loopEnd = !FSNODE_COMPONENT_NAME.equals(currentComponent.getComponent().getCombine());
            }
            setReplacedNodeConnections(fsNode, dataNode, oldFsNode, needCreateTFSNode, loopEnd);

            if (!flowConnections.isEmpty()) {
                // initialize with next component for next loop
                currentComponent = flowConnections.get(0).getTarget();
                dataNode = (AbstractNode) buildCheckMap.get(currentComponent);
                needCreateTFSNode = false;
            }
        }
        for (IConnection connection : currentComponent.getOutgoingConnections()) {
            if (connection.isActivate()) {
                replaceFileScalesComponents(connection.getTarget());
            }
        }
    }

    @Override
    public void buildFromGraphicalProcess(List<INode> graphicalNodeList) {
        initialize();

        if (graphicalNodeList.size() == 0) {
            return;
        }
        // Build a simple copy of the process (to have new objects, avoid to modify the ones in the designer..)
        List<INode> newGraphicalNodeList = buildCopyOfGraphicalNodeList(graphicalNodeList);
        replaceNodeFromProviders(newGraphicalNodeList);
        // job settings extra (feature 2710)
        if (JobSettingsManager.isImplicittContextLoadActived(duplicatedProcess)) {
            List<DataNode> contextLoadNodes = JobSettingsManager.createExtraContextLoadNodes(duplicatedProcess);
            for (DataNode node : contextLoadNodes) {
                buildCheckMap.put(node, node);
                addDataNode(node);
                replaceMultipleComponents(node);
            }
        }

        // build data nodes from graphical nodes.
        // DataNode are the real objects used by code generation (we don't use Node class)
        for (INode node : newGraphicalNodeList) {
            if (node.isSubProcessStart() && node.isActivate()) {
                buildDataNodeFromNode(node);
            }
        }

        // make sure the new tUnite incomingConnections order is the same as the old one. @see
        // Connection.setInputId(int id)
        for (INode graphicalNode : graphicalNodeList) {
            if (graphicalNode.getComponent().useMerge()) {
                for (INode dataNode : dataNodeList) {
                    if (graphicalNode.getUniqueName().equals(dataNode.getUniqueName())) {
                        adjustMergeOrderForDuplicateNode(graphicalNode, dataNode);
                        break;
                    }
                }
            }
        }

        // For lookup links (tMap / tJoin)
        for (INode node : newGraphicalNodeList) {
            if (node.isSubProcessStart() && node.isActivate()) {
                checkFlowRefLink(node);
            }
        }

        for (INode node : newGraphicalNodeList) {
            checkUseParallelize(node);
        }

        // calculate the merge info for every node
        for (INode node : dataNodeList) {
            Map<INode, Integer> mergeInfo = NodeUtil.getLinkedMergeInfo(node);
            if (!mergeInfo.isEmpty()) {
                ((AbstractNode) node).setThereLinkWithMerge(true);
                ((AbstractNode) node).setLinkedMergeInfo(mergeInfo);
            }
        }

        // change the design subjob start as the value stored while building process is the graphical node
        for (INode dataNode : dataNodeList) {
            if (dataNode instanceof AbstractNode) {
                INode graphicalNode = ((AbstractNode) dataNode).getDesignSubjobStartNode();
                INode currentDataNode = buildCheckMap.get(graphicalNode);
                if (currentDataNode == null || !dataNodeList.contains(currentDataNode)) {
                    ((AbstractNode) dataNode).setDesignSubjobStartNode(null);
                    // call the function to recalculate the subjobstart node
                    currentDataNode = ((AbstractNode) dataNode).getDesignSubjobStartNode();
                    // set the value with the code after the if,
                    // so it will avoid to calculate it at each call later.
                }
                ((AbstractNode) dataNode).setDesignSubjobStartNode(currentDataNode);
            }
        }

        checkPigLoadComponent();

        // if there is at least 2 merge components in the same flow, then use hash to keep temporary datas.
        // if only one merge in the same flow, don't use any hash system, to keep best performances / memory
        for (INode node : newGraphicalNodeList) {
            if (node.isSubProcessStart() && node.isActivate()) {
                if (!hasSingleMergeComponent(node)) {
                    checkMergeComponents(node);
                }
            }
        }

        for (INode node : newGraphicalNodeList) {
            if (node.isSubProcessStart() && node.isActivate()) {
                replaceForValidationRules(node);
            }
        }
        for (INode node : newGraphicalNodeList) {
            if (node.isSubProcessStart() && node.isActivate()) {
                replaceMultipleComponents(node);
            }
        }

        for (INode node : newGraphicalNodeList) {
            if (node.isSubProcessStart() && node.isActivate()) {
                replaceFileScalesComponents(node);
            }
        }

        for (INode node : newGraphicalNodeList) {
            if (node.isSubProcessStart() && node.isActivate()) {
                replaceEltComponents(node);
            }
        }
        // change the design subjob start as the value stored while building process is the graphical node
        for (INode dataNode : dataNodeList) {
            if (dataNode instanceof AbstractNode) {
                INode currentDataNode = null;
                // INode graphicalNode = ((AbstractNode) dataNode).getDesignSubjobStartNode();
                // INode currentDataNode = buildCheckMap.get(graphicalNode);
                // if (currentDataNode == null || !dataNodeList.contains(currentDataNode)) {
                ((AbstractNode) dataNode).setDesignSubjobStartNode(null);
                // call the function to recalculate the subjobstart node
                currentDataNode = ((AbstractNode) dataNode).getDesignSubjobStartNode();
                // set the value with the code after the if,
                // so it will avoid to calculate it at each call later.
                // }
                ((AbstractNode) dataNode).setDesignSubjobStartNode(currentDataNode);
            }
        }
        // job settings stats & logs
        if (JobSettingsManager.isStatsAndLogsActivated(duplicatedProcess)) {
            // will add the Stats & Logs managements
            Boolean realTimeStats = ((Boolean) duplicatedProcess
                    .getElementParameter(EParameterName.CATCH_REALTIME_STATS.getName()).getValue())
                    && duplicatedProcess.getElementParameter(EParameterName.CATCH_REALTIME_STATS.getName())
                            .isShow(duplicatedProcess.getElementParameters());

            if (!realTimeStats) {
                for (INode node : dataNodeList) {
                    IElementParameter param = node.getElementParameter(EParameterName.TSTATCATCHER_STATS.getName());
                    if (param != null) {
                        param.setValue(Boolean.FALSE);
                    }
                }
            }

            List<DataNode> statsAndLogsNodeList = JobSettingsManager.createStatsAndLogsNodes(duplicatedProcess);
            DataNode connNode = null;
            for (DataNode node : statsAndLogsNodeList) {
                if (node.getUniqueName().equals(StatsAndLogsManager.CONNECTION_UID)) {
                    connNode = node;
                    break;
                }
            }

            for (DataNode node : statsAndLogsNodeList) {
                buildCheckMap.put(node, node);
            }

            for (DataNode node : statsAndLogsNodeList) {
                addDataNode(node);
                replaceMultipleComponents(node);
                if (connNode == null) {
                    continue;
                }
                for (INode dataNode : dataNodeList) {
                    if (dataNode.getUniqueName().equals(node.getUniqueName() + "_DB")) {
                        IElementParameter refPara = dataNode.getElementParameter("referencedComponent");
                        if (refPara != null) {
                            refPara.setValue(connNode.getUniqueName());
                            IGenericDBService dbService = null;
                            if (GlobalServiceRegister.getDefault().isServiceRegistered(IGenericDBService.class)) {
                                dbService = (IGenericDBService) GlobalServiceRegister.getDefault()
                                        .getService(IGenericDBService.class);
                            }
                            if (dbService != null) {
                                dbService.initReferencedComponent(refPara, connNode.getUniqueName());
                            }
                        }
                    }
                }
            }
        }

        // calculate the merge info for every node
        for (INode node : dataNodeList) {
            Map<INode, Integer> mergeInfo = NodeUtil.getLinkedMergeInfo(node);
            if (!mergeInfo.isEmpty()) {
                ((AbstractNode) node).setThereLinkWithMerge(true);
                ((AbstractNode) node).setLinkedMergeInfo(mergeInfo);
            } else {
                ((AbstractNode) node).setThereLinkWithMerge(false);
                ((AbstractNode) node).setLinkedMergeInfo(null);
            }
        }
        // set the preStaLogCon must be first
        INode preStaLogConNode = null;
        for (INode node : dataNodeList) {
            if (node.getComponent().getName().equals(StatsAndLogsManager.TPREJOB)
                    && node.getUniqueName().equals(StatsAndLogsManager.PRE_STA_LOG_CON)) {
                preStaLogConNode = node;
                break;
            }
            ((AbstractNode) node).setUniqueShortName(UniqueNodeNameGenerator
                    .generateUniqueNodeName(((AbstractNode) node).getComponent().getShortName(), shortUniqueNameList));
            shortUniqueNameList.add(node.getUniqueShortName());
        }
        if (preStaLogConNode != null) {
            dataNodeList.remove(preStaLogConNode);
            dataNodeList.add(0, preStaLogConNode);
        }

        // tag all the components after Iterator(Parallel)
        // !iterator with parallel shouldn't work on merge!
        for (INode node : dataNodeList) {
            if (node.isSubProcessStart() && node.isActivate()) {
                tagComponentAfterParallelIterator(node);
            }
        }
        for (INode node : dataNodeList) {
            if (node.isSubProcessStart() && node.isActivate()) {
                tagSubProcessAfterParallelIterator(node);
            }
        }
        
        boolean isJoblet = false;
        if(GlobalServiceRegister.getDefault().isServiceRegistered(IJobletProviderService.class)) {
            IJobletProviderService service = (IJobletProviderService) GlobalServiceRegister.getDefault().getService(
                    IJobletProviderService.class);
            if (service != null && service.isJobletProcess(this.process)) {
            	isJoblet = true;
            }
        }
        
        if (duplicatedProcess.getComponentsType().equals(ComponentCategory.CATEGORY_4_DI.getName()) 
        		&& PluginChecker.isTIS() && !isJoblet) {
        	final String talendJobLogComponent = "tJobStructureCatcher";
            final String uid4TalendJobLogComponent = "talendJobLog";
        	IComponent jobStructComponent = ComponentsFactoryProvider.getInstance().get(talendJobLogComponent, ComponentCategory.CATEGORY_4_DI.getName());
        	if (jobStructComponent != null) {
	        	DataNode jobStructure = new DataNode(jobStructComponent, uid4TalendJobLogComponent);
	        	jobStructure.setActivate(true);
	        	jobStructure.setStart(true);
	        	jobStructure.setSubProcessStart(true);
	        	jobStructure.setProcess(duplicatedProcess);
	        	if(!CheckLogManamger.isSelectLog4j2()) {
	        		jobStructure.getElementParameter("LOG4J_VERSION").setValue("LOG4J1");
	        	} else {
	        		jobStructure.getElementParameter("LOG4J_VERSION").setValue("LOG4J2");
	        	}
	        	addDataNode(jobStructure);

	        	//TODO consider to remove it as may not necessary
	            shortUniqueNameList.clear();
	            for (INode node : dataNodeList) {
	                if (node.getComponent().getName().equals(talendJobLogComponent)
	                        && node.getUniqueName().equals(uid4TalendJobLogComponent)) {
	    	            ((AbstractNode) node).setUniqueShortName(UniqueNodeNameGenerator
	    	                    .generateUniqueNodeName(((AbstractNode) node).getComponent().getShortName(), shortUniqueNameList));
	    	            shortUniqueNameList.add(node.getUniqueShortName());
	                }
	            }
        	}
        }

        checkUseHadoopConfs(newGraphicalNodeList);

        // IGenericDBService dbService = null;
        // if (GlobalServiceRegister.getDefault().isServiceRegistered(IGenericDBService.class)) {
        // dbService = (IGenericDBService) GlobalServiceRegister.getDefault().getService(
        // IGenericDBService.class);
        // }
        // if(dbService != null){
        // for (INode node : dataNodeList) {
        // if(node.getComponent().getComponentType() == EComponentType.GENERIC){
        //
        // for(IMetadataTable iTable : node.getMetadataList()){
        // iTable = dbService.converTable(node, iTable);
        // }
        // }
        // }
        // }

        checkRefList = null;
        checkMultipleMap = null;
        checktUniteMap = null;
        buildCheckMap = null;
        buildGraphicalMap = null;
        connectionsToIgnoreInMerge = null;
        shortUniqueNameList = null;
        checkValidationList = null;

    }

    /**
     * DOC bchen Comment method "tagSubProcessAfterParallelIterator".
     *
     * @param node
     */
    private void tagSubProcessAfterParallelIterator(INode node) {
        if (node.getIncomingConnections() != null && node.getIncomingConnections().size() != 0) {
            IConnection connection = node.getIncomingConnections().get(0);
            if (connection.getLineStyle().equals(EConnectionType.ON_COMPONENT_OK)
                    || connection.getLineStyle().equals(EConnectionType.ON_COMPONENT_ERROR)
                    || connection.getLineStyle().equals(EConnectionType.ON_SUBJOB_OK)
                    || connection.getLineStyle().equals(EConnectionType.ON_SUBJOB_ERROR)
                    || connection.getLineStyle().equals(EConnectionType.RUN_IF)) {// add if you think it should be count
                                                                                  // for parallel Iterator
                if (((AbstractNode) connection.getSource()).getParallelIterator() != null) {
                    ((AbstractNode) node).setParallelIterator(((AbstractNode) connection.getSource()).getParallelIterator());
                    tagComponentAfterParallelIterator(node);
                }
            }
        }
    }

    /**
     * DOC bchen Comment method "tagComponentAfterParallelIterator".
     *
     * @param node
     */
    private void tagComponentAfterParallelIterator(INode node) {
        for (IConnection connection : node.getOutgoingSortedConnections()) {
            if (connection.getTarget().isActivate()) {

                if (connection.getLineStyle().hasConnectionCategory(IConnectionCategory.MAIN | IConnectionCategory.USE_ITERATE)) {
                    if (((AbstractNode) node).getParallelIterator() != null) {
                        ((AbstractNode) connection.getTarget()).setParallelIterator(((AbstractNode) node).getParallelIterator());
                    } else if (connection.getLineStyle().equals(EConnectionType.ITERATE) && Boolean.TRUE.toString()
                            .equals(ElementParameterParser.getValue(connection, "__ENABLE_PARALLEL__"))) {
                        ((AbstractNode) connection.getTarget()).setParallelIterator(connection.getTarget().getUniqueName());
                    }
                    tagComponentAfterParallelIterator(connection.getTarget());
                }
            }
        }
    }

    private void checkPigLoadComponent() {
        // if (PluginChecker.isPigudfPluginLoaded()) {
        // for (INode dataNode : dataNodeList) {
        // if (dataNode instanceof AbstractNode && "tPigLoad".equals(dataNode.getComponent().getName())) {
        // IElementParameter elementParameter = dataNode.getElementParameter("DRIVER_JAR");
        // if (elementParameter != null && elementParameter.getValue() instanceof List) {
        // List value = (List) elementParameter.getValue();
        // String jarName = "pigudf.jar";
        // boolean alreadyExist = false;
        // for (Object obj : value) {
        // if (obj instanceof Map) {
        // if (jarName.equals(((Map) obj).get("JAR_NAME"))) {
        // alreadyExist = true;
        // }
        // }
        // }
        // if (!alreadyExist) {
        // Map udfJar = new HashMap();
        // udfJar.put("JAR_NAME", jarName);
        // value.add(udfJar);
        // }
        // }
        // }
        // }
        // }

    }

    /**
     * DOC ycbai Comment method "replaceForValidationRules".
     *
     * @param node
     */
    private void replaceForValidationRules(INode node) {
        if (checkValidationList.contains(node)) {
            return;
        }
        checkValidationList.add(node);

        AbstractNode dataNode = (AbstractNode) buildCheckMap.get(node);
        // if use validation continue, else return
        if (ValidationRulesUtil.isHasValidationRule(dataNode)) {
            IElementParameter param = dataNode.getElementParameter(EParameterName.REPOSITORY_VALIDATION_RULE_TYPE.getName());
            if (param != null && param.getValue() != null) {
                IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                String linkedRepository = (String) param.getValue();
                try {
                    IRepositoryViewObject object = factory.getLastVersion(linkedRepository);
                    if (object != null && object.getProperty() != null) {
                        Item item = object.getProperty().getItem();
                        if (item != null && item instanceof ValidationRulesConnectionItem) {
                            ValidationRulesConnectionItem valItem = (ValidationRulesConnectionItem) item;
                            ValidationRulesConnection rulesConnection = (ValidationRulesConnection) valItem.getConnection();
                            List<IConnection> inputs = (List<IConnection>) NodeUtil.getIncomingConnections(dataNode,
                                    IConnectionCategory.DATA);
                            if (inputs.size() > 0) {// only for output component (have data input)
                                DataConnection connection = (DataConnection) inputs.get(0);
                                replaceValidationRules(node, rulesConnection, connection, true);
                            } else {// only for input component (have no data input)
                                List<IConnection> outputs = (List<IConnection>) NodeUtil.getOutgoingConnections(dataNode,
                                        IConnectionCategory.DATA);
                                if (outputs.size() > 0) {
                                    DataConnection connection = (DataConnection) outputs.get(0);
                                    replaceValidationRules(node, rulesConnection, connection, false);
                                }
                            }
                        }
                    }
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                }
            }
        }

        for (IConnection connection : node.getOutgoingConnections()) {
            if (connection.isActivate()) {
                INode target = connection.getTarget();
                replaceForValidationRules(target);
            }
        }
    }

    /**
     * DOC ycbai Comment method "replaceValidationRules".
     *
     * @param rulesConnection
     * @param connection
     * @param isOutput
     * @param node
     */
    private void replaceValidationRules(INode graphicalNode, ValidationRulesConnection rulesConnection, DataConnection connection,
            boolean isOutput) {
        if (rulesConnection.getType() == RuleType.BASIC) {
            replaceBasicOrCustomValidationRules(graphicalNode, connection, rulesConnection, false, isOutput);
        } else if (rulesConnection.getType() == RuleType.REFERENCE) {
            replaceReferenceValidationRules(graphicalNode, connection, rulesConnection, isOutput);
        } else if (rulesConnection.getType() == RuleType.CUSTOM) {
            replaceBasicOrCustomValidationRules(graphicalNode, connection, rulesConnection, true, isOutput);
        }
    }

    /**
     * DOC ycbai Comment method "replaceReferenceValidationRules".
     *
     * @param connection
     * @param rulesConnection
     * @param isOutput
     * @param graphicalNode
     */
    private void replaceReferenceValidationRules(INode graphicalNode, IConnection connection,
            ValidationRulesConnection rulesConnection, boolean isOutput) {
        INode node = getApplicableNode(connection, isOutput);
        INode endNode = null;
        if (!checkTriggerAndDBSettings(node, rulesConnection, isOutput)) {
            return;
        }
        String baseSchema = rulesConnection.getBaseSchema();
        String refSchema = rulesConnection.getRefSchema();
        if (baseSchema == null || refSchema == null) {
            return;
        }
        String[] values = refSchema.split(" - "); //$NON-NLS-1$
        String itemId = values[0];
        String tabName = values[1];
        if (itemId == null) {
            return;
        }
        IComponent inputComponent = ValidationRulesUtil.getComponentsFromItemId(itemId,
                ERepositoryObjectType.METADATA_CONNECTIONS, true, false);
        if (inputComponent == null) {
            return;
        }
        DatabaseConnection dbConnection = (DatabaseConnection) MetadataToolHelper.getConnectionFromRepository(refSchema);
        IMetadataTable refMetadataTable = MetadataToolHelper.getMetadataFromRepository(refSchema);

        AbstractNode nodeUseValidationRule = (AbstractNode) node;
        endNode = nodeUseValidationRule;
        List<IConnection> validRuleConnections;
        List<? extends IConnection> mainConnections;
        IMetadataTable rejectMetadataTable = null;
        IConnection dataConnection = null;
        if (isOutput) {
            validRuleConnections = (List<IConnection>) nodeUseValidationRule.getIncomingConnections();
            mainConnections = nodeUseValidationRule.getIncomingConnections("FLOW");//$NON-NLS-1$
            if (nodeUseValidationRule.getComponentProperties() != null) {
                mainConnections = nodeUseValidationRule.getIncomingConnections("MAIN");//$NON-NLS-1$
            }
        } else {
            validRuleConnections = (List<IConnection>) nodeUseValidationRule.getOutgoingConnections();
            mainConnections = nodeUseValidationRule.getOutgoingConnections("FLOW");//$NON-NLS-1$
            if (nodeUseValidationRule.getComponentProperties() != null) {
                mainConnections = nodeUseValidationRule.getOutgoingConnections("MAIN");//$NON-NLS-1$
            }
        }
        if (validRuleConnections == null || validRuleConnections.size() == 0) {
            return;
        }

        if (mainConnections != null && mainConnections.size() > 0) {
            dataConnection = mainConnections.get(0);
        }

        String originalConnector = null;
        if (dataConnection != null) {
            originalConnector = dataConnection.getConnectorName();
            validRuleConnections.remove(dataConnection);
        }

        // Change from Input => Ouptut, to Input => tJoin (with ref link) => Output

        // create tJoin component
        IComponent component = ComponentsFactoryProvider.getInstance().get("tJoin", ComponentCategory.CATEGORY_4_DI.getName()); //$NON-NLS-1$
        String typeStr;
        if (isOutput) {
            typeStr = "output"; //$NON-NLS-1$
        } else {
            typeStr = "input"; //$NON-NLS-1$
        }
        String uniqueName = component.getName() + "_" + typeStr; //$NON-NLS-1$
        if (dataConnection != null) {
            uniqueName += "_" + dataConnection.getName(); //$NON-NLS-1$
        }
        DataNode joinNode = new DataNode(component, uniqueName);
        joinNode.setActivate(connection.isActivate());
        joinNode.setStart(false);
        joinNode.setDesignSubjobStartNode(null);
        IMetadataTable joinNodeMetadataTable = null;
        if (dataConnection != null) {
            if (dataConnection.getMetadataTable() != null) {
                joinNodeMetadataTable = dataConnection.getMetadataTable();
            }
        } else {
            List<IMetadataTable> metadatas = nodeUseValidationRule.getMetadataList();
            if (metadatas != null && metadatas.size() > 0) {
                for (IMetadataTable metadataTable : metadatas) {
                    if ("FLOW".equals(metadataTable.getAttachedConnector())) { //$NON-NLS-1$
                        joinNodeMetadataTable = metadataTable;
                        break;
                    }
                }
            }
        }
        if (joinNodeMetadataTable != null) {
            IMetadataTable newMetadata = joinNodeMetadataTable.clone();
            newMetadata.setTableName(uniqueName);
            joinNode.getMetadataList().remove(0);
            joinNode.getMetadataList().add(newMetadata);
            newMetadata.setAttachedConnector("FLOW"); //$NON-NLS-1$
        }
        joinNode.setSubProcessStart(false);
        joinNode.setProcess(node.getProcess());
        joinNode.setElementParameters(component.createElementParameters(joinNode));
        updateJoinParametersWithValidRuleConnection(joinNode, rulesConnection);
        List<IConnection> tJoin_outgoingConnections = new ArrayList<IConnection>();
        List<IConnection> tJoin_incomingConnections = new ArrayList<IConnection>();
        joinNode.setIncomingConnections(tJoin_incomingConnections);
        joinNode.setOutgoingConnections(tJoin_outgoingConnections);
        addDataNode(joinNode);

        List<? extends INodeConnector> connectors = nodeUseValidationRule.getListConnector();
        INodeConnector rejectConnector = null;
        for (INodeConnector connector : connectors) {
            if ("VALIDATION_REJECT".equals(connector.getName())) {//$NON-NLS-1$
                rejectConnector = connector;
                break;
            }
        }
        ((List<INodeConnector>) joinNode.getListConnector()).add(rejectConnector);
        List<IMetadataTable> metadataList = nodeUseValidationRule.getMetadataList();

        for (IMetadataTable metadataTable : metadataList) {
            if ("VALIDATION_REJECT".equals(metadataTable.getTableName()) && !joinNode.getMetadataList().contains(metadataTable)) {//$NON-NLS-1$
                rejectMetadataTable = metadataTable;
                joinNode.getMetadataList().add(metadataTable);
                break;
            }
        }

        // set incomming or outgoing connection of the tJoin. add the current link
        if (dataConnection != null) {
            if (isOutput) {
                tJoin_incomingConnections.add(dataConnection);
                ((DataConnection) dataConnection).setTarget(joinNode);
            } else {
                tJoin_outgoingConnections.add(dataConnection);
                ((DataConnection) dataConnection).setSource(joinNode);
            }
        }

        // create new link for output or input of tJoin.
        DataConnection dataConnec = new DataConnection();
        dataConnec.setActivate(connection.isActivate());
        dataConnec.setLineStyle(EConnectionType.FLOW_MAIN);
        dataConnec.setTraceConnection(connection.isTraceConnection());
        dataConnec.setTracesCondition(connection.getTracesCondition());
        dataConnec.setEnabledTraceColumns(connection.getEnabledTraceColumns());
        dataConnec.setMonitorConnection(connection.isMonitorConnection());
        dataConnec.setConnectorName("FLOW"); //$NON-NLS-1$
        if (!nodeUseValidationRule.getMetadataList().isEmpty()) {
            dataConnec.setMetadataTable(nodeUseValidationRule.getMetadataList().get(0));
        }
        if (isOutput) {
            dataConnec.setName("before_" + nodeUseValidationRule.getUniqueName()); //$NON-NLS-1$
            dataConnec.setSource(joinNode);
            dataConnec.setTarget(nodeUseValidationRule);
            tJoin_outgoingConnections.add(dataConnec);
        } else {
            if (originalConnector != null) {
                dataConnec.setConnectorName(originalConnector);
                dataConnec.getMetadataTable().setAttachedConnector(originalConnector);
            } else {
                dataConnec.setConnectorName("FLOW"); //$NON-NLS-1$
            }
            dataConnec.setName("after_" + nodeUseValidationRule.getUniqueName()); //$NON-NLS-1$
            dataConnec.setSource(nodeUseValidationRule);
            dataConnec.setTarget(joinNode);
            tJoin_incomingConnections.add(dataConnec);
        }
        validRuleConnections.add(dataConnec);

        // create tInput component to load the referential data
        uniqueName = inputComponent.getName() + "_" + joinNode.getUniqueName(); //$NON-NLS-1$
        DataNode inputNode = new DataNode(inputComponent, uniqueName);
        inputNode.setActivate(true);
        inputNode.setStart(true);
        inputNode.setDesignSubjobStartNode(null);
        inputNode.getMetadataList().remove(0);
        inputNode.getMetadataList().add(refMetadataTable);
        inputNode.setSubProcessStart(true);
        inputNode.setProcess(node.getProcess());
        inputNode.setElementParameters(inputComponent.createElementParameters(inputNode));


        updateInputParametersWithDBConnection(inputNode, dbConnection, tabName);
        addDataNode(inputNode);
        List<IConnection> inputNode_outgoingConnections = new ArrayList<IConnection>();
        inputNode.setOutgoingConnections(inputNode_outgoingConnections);
        List<IConnection> inputNode_incomingConnections = new ArrayList<IConnection>();
        inputNode.setIncomingConnections(inputNode_incomingConnections);

        DataConnection ref_dataConnec = new DataConnection();
        ref_dataConnec.setActivate(connection.isActivate());
        ref_dataConnec.setLineStyle(EConnectionType.FLOW_REF);
        ref_dataConnec.setMetadataTable(refMetadataTable);
        ref_dataConnec.setName(joinNode.getUniqueName() + "_" + connection.getName());//$NON-NLS-1$
        ref_dataConnec.setSource(inputNode);
        ref_dataConnec.setTarget(joinNode);
        ref_dataConnec.setConnectorName("FLOW"); //$NON-NLS-1$

        tJoin_incomingConnections.add(ref_dataConnec);
        inputNode_outgoingConnections.add(ref_dataConnec);

        // retrieve the starts node of each current nodes to add a before link
        INode subNodeStartTarget = graphicalNode.getSubProcessStartNode(true);

        // input node from validation rules (reference)
        AbstractNode subDataNodeStartSource = (AbstractNode) buildCheckMap.get(subNodeStartTarget);

        // first component in the subprocess (where is applied the validation rules)
        AbstractNode subDataNodeStartTarget = inputNode;

        if (subDataNodeStartSource == null) {
            // means the graphic process is not complete, so ignore it.
            return;
        }

        // if (subDataNodeStartSource.getMetadataList().isEmpty()) {
        // continue;
        // }

        List<IConnection> outgoingConnections = (List<IConnection>) subDataNodeStartSource.getOutgoingConnections();
        // create a link before between the two subprocess
        dataConnec = new DataConnection();
        dataConnec.setActivate(connection.isActivate());
        dataConnec.setLineStyle(EConnectionType.RUN_AFTER);
        dataConnec.setTraceConnection(connection.isTraceConnection());
        dataConnec.setTracesCondition(connection.getTracesCondition());
        dataConnec.setEnabledTraceColumns(connection.getEnabledTraceColumns());
        dataConnec.setMonitorConnection(connection.isMonitorConnection());
        // dataConnec.setLineStyle(EConnectionType.THEN_RUN);
        if (!subDataNodeStartSource.getMetadataList().isEmpty()) {
            dataConnec.setMetadataTable(subDataNodeStartSource.getMetadataList().get(0));
        }
        dataConnec.setName("after_" + subDataNodeStartSource.getUniqueName()); //$NON-NLS-1$
        // dataConnec.setConnectorName(EConnectionType.THEN_RUN.getName());
        dataConnec.setConnectorName(EConnectionType.RUN_AFTER.getName());
        dataConnec.setSource(subDataNodeStartSource);
        // dataConnec.setSource(subDataNodeStartTarget);
        dataConnec.setTarget(subDataNodeStartTarget);
        // dataConnec.setTarget(subDataNodeStartSource);

        // the target component can't be start in all case, so no matter where it has been defined, remove
        // the start state.
        subDataNodeStartTarget.setStart(false);

        inputNode_incomingConnections.add(dataConnec);
        outgoingConnections.add(dataConnec);

        // add a new hash node
        // (to replace by a Node maybe that will take the informations of an IComponent)
        String baseConnector = inputNode.getConnectorFromName("FLOW").getBaseSchema(); //$NON-NLS-1$
        INodeConnector connector = joinNode.getConnectorFromName(baseConnector);
        String hashComponent = connector.getConnectionProperty(EConnectionType.FLOW_REF).getLinkedComponent();

        if (hashComponent == null) {
            uniqueName = HASH_COMPONENT_NAME + "_" + connection.getName(); //$NON-NLS-1$
            component = ComponentsFactoryProvider.getInstance().get(HASH_COMPONENT_NAME,
                    ComponentCategory.CATEGORY_4_DI.getName());
        } else {
            uniqueName = hashComponent + "_" + connection.getName(); //$NON-NLS-1$
            component = ComponentsFactoryProvider.getInstance().get(hashComponent, ComponentCategory.CATEGORY_4_DI.getName());
        }
        if (component == null) {
            return;
        }
        DataNode hashNode = new DataNode(component, uniqueName);
        hashNode.setActivate(connection.isActivate());
        hashNode.setStart(false);
        hashNode.setDesignSubjobStartNode(null);
        IMetadataTable hashMetadata = inputNode.getMetadataList().get(0).clone();
        hashMetadata.setTableName(uniqueName);
        hashNode.getMetadataList().remove(0);
        hashNode.getMetadataList().add(hashMetadata);
        hashNode.setSubProcessStart(false);
        hashNode.setProcess(node.getProcess());
        List<IConnection> hash_outgoingConnections = new ArrayList<IConnection>();
        List<IConnection> hash_incomingConnections = new ArrayList<IConnection>();
        hashNode.setIncomingConnections(hash_incomingConnections);
        hashNode.setOutgoingConnections(hash_outgoingConnections);
        addDataNode(hashNode);

        // create a link flow_main between the node that had ref and the hash file
        dataConnec = new DataConnection();
        dataConnec.setActivate(connection.isActivate());
        dataConnec.setLineStyle(EConnectionType.FLOW_MAIN);
        dataConnec.setMetadataTable(hashMetadata);
        dataConnec.setTraceConnection(connection.isTraceConnection());
        dataConnec.setMonitorConnection(connection.isMonitorConnection());
        dataConnec.setTracesCondition(connection.getTracesCondition());
        dataConnec.setEnabledTraceColumns(connection.getEnabledTraceColumns());
        dataConnec.setName(joinNode.getUniqueName() + "_" + connection.getName());//$NON-NLS-1$
        dataConnec.setSource(joinNode);
        dataConnec.setTarget(hashNode);
        dataConnec.setLinkNodeForHash(buildCheckMap.get(connection.getTarget()));
        dataConnec.setConnectorName(connection.getConnectorName());

        inputNode_outgoingConnections.add(dataConnec);
        hash_incomingConnections.add(dataConnec);

        // handle reject link.
        if (endNode.getOutgoingConnections("VALIDATION_REJECT") != null //$NON-NLS-1$
                && endNode.getOutgoingConnections("VALIDATION_REJECT").size() == 1) {//$NON-NLS-1$
            IConnection conn = endNode.getOutgoingConnections("VALIDATION_REJECT").get(0);//$NON-NLS-1$
            INode targetNode = conn.getTarget();
            List<IConnection> outputconns = (List<IConnection>) endNode.getOutgoingConnections();
            List<IConnection> inputconnns = (List<IConnection>) targetNode.getIncomingConnections();
            outputconns.remove(conn);
            inputconnns.remove(conn);
            DataConnection rejectLink = new DataConnection();
            rejectLink.setActivate(connection.isActivate());
            rejectLink.setLineStyle(EConnectionType.FLOW_MAIN);
            rejectLink.setMetadataTable(rejectMetadataTable);
            rejectLink.setConnectorName("REJECT"); //$NON-NLS-1$
            rejectLink.setName(conn.getName());
            rejectLink.setSource(joinNode);
            rejectLink.setTarget(targetNode);
            ((List<IConnection>) joinNode.getOutgoingConnections()).add(rejectLink);
            ((List<IConnection>) targetNode.getIncomingConnections()).add(rejectLink);
        }
    }

    /**
     * DOC ycbai Comment method "replaceBasicOrCustomValidationRules".
     *
     * @param connection
     * @param rulesConnection
     * @param isCustom
     * @param isOutput
     */
    private void replaceBasicOrCustomValidationRules(INode graphicalNode, IConnection connection,
            ValidationRulesConnection rulesConnection, boolean isCustom, boolean isOutput) {
        INode node = getApplicableNode(connection, isOutput);
        INode endNode = null;

        if (!checkTriggerAndDBSettings(node, rulesConnection, isOutput)) {
            return;
        }
        INode nodeUseValidationRule = node;
        endNode = nodeUseValidationRule;
        DataNode filterNode = null;
        List<IConnection> validRuleConnections;
        List<? extends IConnection> mainConnections;
        IMetadataTable rejectMetadataTable = null;
        DataConnection dataConnection = null;
        EConnectionType connectionType = connection.getLineStyle();
        if (isOutput) {
            validRuleConnections = (List<IConnection>) nodeUseValidationRule.getIncomingConnections();
            mainConnections = nodeUseValidationRule.getIncomingConnections(connectionType);
        } else {
            validRuleConnections = (List<IConnection>) nodeUseValidationRule.getOutgoingConnections();
            mainConnections = nodeUseValidationRule.getOutgoingConnections(connectionType);
        }

        if (validRuleConnections == null || validRuleConnections.size() == 0) {
            return;
        }

        if (mainConnections != null && mainConnections.size() > 0) {
            dataConnection = (DataConnection) mainConnections.get(0);
        }

        if (dataConnection != null) {
            validRuleConnections.remove(dataConnection);
        }

        // create tFilterRow
        IComponent component = ComponentsFactoryProvider.getInstance().get("tFilterRow", //$NON-NLS-1$
                ComponentCategory.CATEGORY_4_DI.getName());
        String typeStr;
        if (isOutput) {
            typeStr = "output"; //$NON-NLS-1$
        } else {
            typeStr = "input"; //$NON-NLS-1$
        }
        String uniqueName = component.getName() + "_" + typeStr; //$NON-NLS-1$
        if (dataConnection != null) {
            uniqueName += "_" + dataConnection.getName(); //$NON-NLS-1$
        }
        filterNode = new DataNode(component, uniqueName);
        filterNode.setActivate(connection.isActivate());
        filterNode.setStart(false);
        filterNode.setDesignSubjobStartNode(null);
        IMetadataTable filterNodeMetadataTable = null;
        String originalConnector = null;
        if (dataConnection != null) {
            originalConnector = dataConnection.getConnectorName();
            if (dataConnection.getMetadataTable() != null) {
                filterNodeMetadataTable = dataConnection.getMetadataTable();
            }
        } else {
            List<IMetadataTable> metadatas = nodeUseValidationRule.getMetadataList();
            if (metadatas != null && metadatas.size() > 0) {
                for (IMetadataTable metadataTable : metadatas) {
                    if ("FLOW".equals(metadataTable.getAttachedConnector())) { //$NON-NLS-1$
                        filterNodeMetadataTable = metadataTable;
                        break;
                    }
                }
            }
        }
        if (filterNodeMetadataTable != null) {
            IMetadataTable newMetadata = filterNodeMetadataTable.clone();
            newMetadata.setTableName(uniqueName);
            filterNode.getMetadataList().remove(0);
            filterNode.getMetadataList().add(newMetadata);
        }
        filterNode.setSubProcessStart(false);
        filterNode.setProcess(node.getProcess());
        filterNode.setElementParameters(component.createElementParameters(filterNode));
        updateFilterParametersWithValidRuleConnection(filterNode, rulesConnection, isCustom);
        List<IConnection> outgoingConnections = new ArrayList<IConnection>();
        List<IConnection> incomingConnections = new ArrayList<IConnection>();
        filterNode.setIncomingConnections(incomingConnections);
        filterNode.setOutgoingConnections(outgoingConnections);
        addDataNode(filterNode);
        List<? extends INodeConnector> connectors = nodeUseValidationRule.getListConnector();
        INodeConnector rejectConnector = null;
        for (INodeConnector connector : connectors) {
            if ("VALIDATION_REJECT".equals(connector.getName())) { //$NON-NLS-1$
                rejectConnector = connector;
                break;
            }
        }
        ((List<INodeConnector>) filterNode.getListConnector()).add(rejectConnector);

        List<IMetadataTable> metadataList = nodeUseValidationRule.getMetadataList();
        for (IMetadataTable metadataTable : metadataList) {
            if ("VALIDATION_REJECT".equals(metadataTable.getTableName()) //$NON-NLS-1$
                    && !filterNode.getMetadataList().contains(metadataTable)) {
                rejectMetadataTable = metadataTable;
                filterNode.getMetadataList().add(metadataTable);
                break;
            }
        }

        // set incomming or outgoing connection of the tFilterRow. add the current link
        if (dataConnection != null) {
            if (isOutput) {
                incomingConnections.add(dataConnection);
                dataConnection.setTarget(filterNode);
            } else {
                outgoingConnections.add(dataConnection);
                dataConnection.setSource(filterNode);
                dataConnection.setTarget(connection.getTarget());
                dataConnection.setConnectorName("FILTER"); //$NON-NLS-1$
            }
        }

        // create new link for output or input of tFilterRow.
        DataConnection dataConnec = new DataConnection();
        dataConnec.setActivate(connection.isActivate());
        dataConnec.setLineStyle(EConnectionType.FLOW_MAIN);
        dataConnec.setTraceConnection(connection.isTraceConnection());
        dataConnec.setTracesCondition(connection.getTracesCondition());
        dataConnec.setEnabledTraceColumns(connection.getEnabledTraceColumns());
        dataConnec.setMonitorConnection(connection.isMonitorConnection());
        if (!nodeUseValidationRule.getMetadataList().isEmpty()) {
            dataConnec.setMetadataTable(nodeUseValidationRule.getMetadataList().get(0));
        }
        if (isOutput) {
            dataConnec.setConnectorName("FILTER"); //$NON-NLS-1$
            dataConnec.setName("before_" + nodeUseValidationRule.getUniqueName()); //$NON-NLS-1$
            dataConnec.setSource(filterNode);
            dataConnec.setTarget(nodeUseValidationRule);
            outgoingConnections.add(dataConnec);
        } else {
            if (originalConnector != null) {
                dataConnec.setConnectorName(originalConnector);
                dataConnec.getMetadataTable().setAttachedConnector(originalConnector);
            } else {
                dataConnec.setConnectorName("FLOW"); //$NON-NLS-1$
            }
            dataConnec.setName("after_" + nodeUseValidationRule.getUniqueName()); //$NON-NLS-1$
            dataConnec.setSource(nodeUseValidationRule);
            dataConnec.setTarget(filterNode);
            incomingConnections.add(dataConnec);
        }
        validRuleConnections.add(dataConnec);

        // handle reject link.
        if (endNode.getOutgoingConnections("VALIDATION_REJECT") != null //$NON-NLS-1$
                && endNode.getOutgoingConnections("VALIDATION_REJECT").size() == 1) {//$NON-NLS-1$
            IConnection conn = endNode.getOutgoingConnections("VALIDATION_REJECT").get(0);//$NON-NLS-1$
            INode targetNode = conn.getTarget();
            List<IConnection> outputconns = (List<IConnection>) endNode.getOutgoingConnections();
            List<IConnection> inputconnns = (List<IConnection>) targetNode.getIncomingConnections();
            outputconns.remove(conn);
            inputconnns.remove(conn);
            DataConnection rejectLink = new DataConnection();
            rejectLink.setActivate(connection.isActivate());
            rejectLink.setLineStyle(EConnectionType.FLOW_MAIN);
            rejectLink.setMetadataTable(rejectMetadataTable);
            rejectLink.setConnectorName("REJECT"); //$NON-NLS-1$
            rejectLink.setName(conn.getName());
            rejectLink.setSource(filterNode);
            rejectLink.setTarget(targetNode);
            ((List<IConnection>) filterNode.getOutgoingConnections()).add(rejectLink);
            ((List<IConnection>) targetNode.getIncomingConnections()).add(rejectLink);
        }

    }

    private INode getApplicableNode(IConnection connection, boolean isOutput) {
        INode node;
        if (isOutput) {
            node = connection.getTarget();
        } else {
            node = connection.getSource();
        }
        return node;
    }

    /**
     * DOC ycbai Comment method "checkTriggerAndDBSettings".
     *
     * @param node
     * @param rulesConnection
     * @param isOutput
     * @return
     */
    private boolean checkTriggerAndDBSettings(INode node, ValidationRulesConnection rulesConnection, boolean isOutput) {
        if (isOutput) {
            boolean isInsertSelect = false;
            boolean isUpdateSelect = false;
            boolean isDeleteSelect = false;
            if (rulesConnection.isIsInsert()) {
                isInsertSelect = true;
            }
            if (rulesConnection.isIsUpdate()) {
                isUpdateSelect = true;
            }
            if (rulesConnection.isIsDelete()) {
                isDeleteSelect = true;
            }
            if (!isInsertSelect && !isUpdateSelect && !isDeleteSelect) {
                return false;
            }

            // action for components
            IElementParameter action = node.getElementParameter("DATA_ACTION"); //$NON-NLS-1$
            boolean isInsertEnable = true;
            boolean isUpdateEnable = true;
            boolean isDeleteEnable = true;
            if (action != null && action.getValue() != null) {
                String value = String.valueOf(action.getValue());
                if (!value.contains("INSERT")) {//$NON-NLS-1$
                    isInsertEnable = false;
                }
                if (!value.contains("UPDATE")) {//$NON-NLS-1$
                    isUpdateEnable = false;
                }
                if (!value.contains("DELETE")) {//$NON-NLS-1$
                    isDeleteEnable = false;
                }
                if (!isInsertSelect && isInsertEnable) {
                    return false;
                }
                if (!isUpdateSelect && isUpdateEnable) {
                    return false;
                }
                if (!isDeleteSelect && isDeleteEnable) {
                    return false;
                }
            }
        } else {
            boolean onSelect = rulesConnection.isIsSelect();
            if (!onSelect) {
                return false;
            }
        }
        return true;
    }

    /**
     * DOC ycbai Comment method "updateParametersWithValidationRuleConnection".
     *
     * @param node
     * @param rulesConnection
     */
    private void updateFilterParametersWithValidRuleConnection(INode node, ValidationRulesConnection rulesConnection,
            boolean isCustom) {
        List<ConditionType> conditions = rulesConnection.getConditions();
        String op = rulesConnection.getLogicalOperator().getLiteral();
        String javaCondition = rulesConnection.getJavaCondition();
        String sqlCondition = rulesConnection.getSqlCondition();

        if (!isCustom) {
            node.getElementParameter("USE_ADVANCED").setValue(false);//$NON-NLS-1$
            node.getElementParameter("LOGICAL_OP").setValue(op);//$NON-NLS-1$
            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            if (conditions != null && conditions.size() > 0) {
                for (ConditionType condition : conditions) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("INPUT_COLUMN", condition.getInputColumn());//$NON-NLS-1$
                    map.put("FUNCTION", condition.getFunction().getLiteral()); //$NON-NLS-1$
                    map.put("OPERATOR", condition.getOperator().getLiteral()); //$NON-NLS-1$
                    map.put("RVALUE", condition.getValue()); //$NON-NLS-1$
                    list.add(map);
                }
            }
            node.getElementParameter("CONDITIONS").setValue(list);//$NON-NLS-1$
        } else {
            node.getElementParameter("USE_ADVANCED").setValue(true);//$NON-NLS-1$
            if (javaCondition != null) {
                node.getElementParameter("ADVANCED_COND").setValue(javaCondition);//$NON-NLS-1$
            } else if (sqlCondition != null) {
                node.getElementParameter("ADVANCED_COND").setValue(sqlCondition);//$NON-NLS-1$
            }
        }
    }

    /**
     * DOC ycbai Comment method "updateJoinParametersWithValidRuleConnection".
     *
     * @param node
     * @param rulesConnection
     */
    private void updateJoinParametersWithValidRuleConnection(INode node, ValidationRulesConnection rulesConnection) {
        node.getElementParameter("USE_INNER_JOIN").setValue(true);//$NON-NLS-1$
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        EMap<String, String> map = rulesConnection.getInnerJoins();
        for (Entry<String, String> entry : map) {
            Map<String, Object> tabMap = new HashMap<String, Object>();
            tabMap.put("INPUT_COLUMN", entry.getKey());//$NON-NLS-1$
            tabMap.put("LOOKUP_COLUMN", entry.getValue()); //$NON-NLS-1$
            list.add(tabMap);
        }
        node.getElementParameter("JOIN_KEY").setValue(list);//$NON-NLS-1$
    }

    private void updateInputParametersWithDBConnection(INode node, DatabaseConnection dbConnection, String tabName) {
        EDatabaseTypeName typeFromDbType = EDatabaseTypeName.getTypeFromDbType(dbConnection.getDatabaseType());
        String query = TalendTextUtils.addQuotes("select * from " + tabName + ";"); //$NON-NLS-1$ //$NON-NLS-2$
        node.getElementParameter("QUERY").setValue(query); //$NON-NLS-1$
        if (dbConnection.isContextMode()) {
            node.getElementParameter("HOST").setValue(dbConnection.getServerName());//$NON-NLS-1$
            node.getElementParameter("PORT").setValue(dbConnection.getPort());//$NON-NLS-1$
            node.getElementParameter("DBNAME").setValue(dbConnection.getSID());//$NON-NLS-1$
            node.getElementParameter("TYPE").setValue(dbConnection.getDatabaseType());//$NON-NLS-1$
            node.getElementParameter("USER").setValue(dbConnection.getUsername());//$NON-NLS-1$
            node.getElementParameter("PASS").setValue(dbConnection.getPassword());//$NON-NLS-1$
            if (typeFromDbType != null && EDatabaseTypeName.ORACLESN.getProduct().equals(typeFromDbType.getProduct())) {
                if (EDatabaseTypeName.ORACLE_OCI == typeFromDbType) {
                    node.getElementParameter("LOCAL_SERVICE_NAME").setValue(dbConnection.getSID());//$NON-NLS-1$
                } else if (EDatabaseTypeName.ORACLE_CUSTOM == typeFromDbType) {
                    node.getElementParameter("RAC_URL").setValue(dbConnection.getURL());//$NON-NLS-1$
                }
            }
        } else {
            node.getElementParameter("HOST").setValue(TalendTextUtils.addQuotes(dbConnection.getServerName()));//$NON-NLS-1$
            node.getElementParameter("PORT").setValue(TalendTextUtils.addQuotes(dbConnection.getPort()));//$NON-NLS-1$
            node.getElementParameter("DBNAME").setValue(TalendTextUtils.addQuotes(dbConnection.getSID()));//$NON-NLS-1$
            node.getElementParameter("TYPE").setValue(TalendTextUtils.addQuotes(dbConnection.getDatabaseType()));//$NON-NLS-1$
            node.getElementParameter("USER").setValue(TalendTextUtils.addQuotes(dbConnection.getUsername()));//$NON-NLS-1$
            node.getElementParameter("PASS").setValue(TalendTextUtils.addQuotes(dbConnection.getRawPassword()));//$NON-NLS-1$
            if (typeFromDbType != null && EDatabaseTypeName.ORACLESN.getProduct().equals(typeFromDbType.getProduct())) {
                if (EDatabaseTypeName.ORACLE_OCI == typeFromDbType) {
                    node.getElementParameter("LOCAL_SERVICE_NAME").setValue(TalendTextUtils.addQuotes(dbConnection.getSID()));//$NON-NLS-1$
                } else if (EDatabaseTypeName.ORACLE_CUSTOM == typeFromDbType) {
                    node.getElementParameter("RAC_URL").setValue(TalendTextUtils.addQuotes(dbConnection.getURL()));//$NON-NLS-1$
                }
            }
        }
        if (typeFromDbType != null) {
            if (EDatabaseTypeName.MSSQL.getProduct().equals(typeFromDbType.getProduct())) {
                node.getElementParameter("DRIVER").setValue(dbConnection.getDbVersionString());//$NON-NLS-1$
            } else if (EDatabaseTypeName.ORACLESN.getProduct().equals(typeFromDbType.getProduct())) {
                node.getElementParameter("CONNECTION_TYPE") //$NON-NLS-1$
                        .setValue(typeFromDbType.getXmlName());
                node.getElementParameter("DB_VERSION").setValue(dbConnection.getDbVersionString());//$NON-NLS-1$
                // fix for java.sql.SQLSyntaxErrorException: ORA-00911: invalid character
                node.getElementParameter("QUERY").setValue(TalendTextUtils.addQuotes("select * from " + tabName)); //$NON-NLS-1$
            }
        }
    }

    private boolean hasSingleMergeComponent(INode node) {
        return hasSingleMergeComponent(node, new ArrayList<INode>(), false);
    }

    private boolean hasSingleMergeComponent(final INode node, final List<INode> checkedNodes, final boolean mergeFound) {
        if (checkedNodes.contains(node)) {
            return true;
        }
        checkedNodes.add(node);
        boolean merge = false;
        AbstractNode dataNode;
        dataNode = (AbstractNode) buildCheckMap.get(node);
        if (dataNode.getComponent().useMerge()) {
            merge = true;
            if (mergeFound) {
                return false;
            }
            if (dataNode.getLinkedMergeInfo() != null && dataNode.getLinkedMergeInfo().size() > 0) {
                // if a tUnite hold any merge information, means it holds information from another tUnite.
                // so in this case there is at least 2 tUnite in the same flow
                return false;
            }
        }
        for (IConnection connection : node.getOutgoingConnections()) {
            if (connection.getLineStyle() == EConnectionType.ON_SUBJOB_OK
                    || connection.getLineStyle() == EConnectionType.ON_SUBJOB_ERROR) {
                continue;
            }

            if (connection.isActivate()) {
                if (!hasSingleMergeComponent(connection.getTarget(), checkedNodes, mergeFound || merge)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void checkMergeComponents(INode node) {
        if (checktUniteMap.containsKey(node)) {
            // return checkMultipleMap.get(graphicalNode);
            return;
        }
        // if the original component is in dummy state, no need to replace it
        // but the next need to be checked
        if (node.isDummy() && !node.isActivate()) {
            // return;
        } else {
            AbstractNode dataNode;

            dataNode = (AbstractNode) buildCheckMap.get(node);
            checktUniteMap.put(node, dataNode);
            if (dataNode.getComponent().useMerge()) {
                try {
                    changeForMultipleMergeComponents(node);
                } catch (Exception e) {
                    Exception wrapper = new Exception(Messages.getString("DataProcess.checkComponent", node.getLabel()), e); //$NON-NLS-1$
                    ExceptionHandler.process(wrapper);
                }
            }
        }
        List<IConnection> outputConnections = new ArrayList<IConnection>(node.getOutgoingConnections());
        for (IConnection connection : outputConnections) {
            if (connection.isActivate()) {
                checkMergeComponents(connection.getTarget());
            }
        }
    }

    private void changeForMultipleMergeComponents(INode graphicalNode) {
        String hashMergeOutput = "tHashOutput";//$NON-NLS-1$
        String hashMergeInput = "tHashInput";//$NON-NLS-1$

        INode mergeDataNode = buildCheckMap.get(graphicalNode);

        IComponent hashMergeOutputComponent = ComponentsFactoryProvider.getInstance().get(hashMergeOutput,
                ComponentCategory.CATEGORY_4_DI.getName());
        IComponent hashMergeInputComponent = ComponentsFactoryProvider.getInstance().get(hashMergeInput,
                ComponentCategory.CATEGORY_4_DI.getName());

        if (hashMergeOutputComponent == null || hashMergeInputComponent == null) {
            return;
        }
        String hashOutputUniqueName = hashMergeOutput + "_" + mergeDataNode.getUniqueName();//$NON-NLS-1$

        // Merge components should have only one output row MAIN in all case, but can have a lookup row also in case of
        // tMap just after.
        List mergeOutputConnections = NodeUtil.getOutgoingConnections(mergeDataNode, IConnectionCategory.MAIN);

        if (mergeOutputConnections.size() == 0) {
            return;
        }

        IConnection mergeOutputConnection = ((List<IConnection>) mergeOutputConnections).get(0);

        DataNode hashNode = new DataNode(hashMergeOutputComponent, hashOutputUniqueName);
        hashNode.setActivate(mergeDataNode.isActivate());
        hashNode.setStart(false);
        hashNode.setDesignSubjobStartNode(null);
        hashNode.getMetadataList().remove(0);
        IMetadataTable newMetadata = mergeDataNode.getMetadataList().get(0).clone();
        newMetadata.setTableName(hashOutputUniqueName);
        hashNode.getMetadataList().add(newMetadata);
        hashNode.setSubProcessStart(false);
        hashNode.setProcess(graphicalNode.getProcess());
        List<IConnection> outgoingConnections = new ArrayList<IConnection>();
        List<IConnection> incomingConnections = new ArrayList<IConnection>();
        hashNode.setIncomingConnections(incomingConnections);
        hashNode.setOutgoingConnections(outgoingConnections);
        hashNode.setVirtualGenerateNode(false);

        addDataNode(hashNode);

        incomingConnections.add(mergeOutputConnection);
        INode oldNodeTarget = ((DataConnection) mergeOutputConnection).getTarget();
        ((DataConnection) mergeOutputConnection).setTarget(hashNode);
        oldNodeTarget.getIncomingConnections().remove(mergeOutputConnection);

        String hashInputUniqueName = hashMergeInput + "_" + mergeDataNode.getUniqueName();

        hashNode = new DataNode(hashMergeInputComponent, hashInputUniqueName);
        hashNode.setActivate(mergeDataNode.isActivate());
        hashNode.setStart(false);
        hashNode.setDesignSubjobStartNode(null);
        newMetadata = mergeDataNode.getMetadataList().get(0).clone();
        newMetadata.setTableName(hashInputUniqueName);
        hashNode.getMetadataList().remove(0);
        hashNode.getMetadataList().add(newMetadata);
        hashNode.setSubProcessStart(true);
        hashNode.setProcess(graphicalNode.getProcess());
        outgoingConnections = new ArrayList<IConnection>();
        incomingConnections = new ArrayList<IConnection>();
        hashNode.setIncomingConnections(incomingConnections);
        hashNode.setOutgoingConnections(outgoingConnections);
        hashNode.setVirtualGenerateNode(false);
        hashNode.getElementParameter("LINK_WITH").setValue(Boolean.TRUE);
        hashNode.getElementParameter("LIST").setValue(hashOutputUniqueName);

        hashNode.getElementParameter("RELEASE_CACHE").setValue(Boolean.TRUE);

        DataConnection dataConnec = new DataConnection();
        dataConnec.setActivate(mergeOutputConnection.isActivate());
        dataConnec.setLineStyle(mergeOutputConnection.getLineStyle());
        dataConnec.setConnectorName(mergeOutputConnection.getConnectorName());
        dataConnec.setMetadataTable(newMetadata);
        dataConnec.setTraceConnection(mergeOutputConnection.isTraceConnection());
        dataConnec.setMonitorConnection(mergeOutputConnection.isMonitorConnection());
        dataConnec.setTracesCondition(mergeOutputConnection.getTracesCondition());
        dataConnec.setEnabledTraceColumns(mergeOutputConnection.getEnabledTraceColumns());
        dataConnec.setName(mergeOutputConnection.getName());
        dataConnec.setSource(hashNode);
        dataConnec.setLinkNodeForHash(((DataConnection) mergeOutputConnection).getLinkNodeForHash());
        dataConnec.setElementParameters(mergeOutputConnection.getElementParameters());
        dataConnec.setUniqueName(mergeOutputConnection.getUniqueName());
        mergeOutputConnection.setElementParameters(new ArrayList<IElementParameter>());
        dataConnec.setTarget(oldNodeTarget);

        ((DataConnection) mergeOutputConnection).setName(hashNode.getUniqueName() + "_" + mergeOutputConnection.getName());
        ((DataConnection) mergeOutputConnection).setUniqueName(hashNode.getUniqueName() + "_" + mergeOutputConnection.getName());

        int inputId = mergeOutputConnection.getInputId();
        if (inputId == 0) { // can be 0 in case of lookup just on the tUnite
            List refOutputConnections = NodeUtil.getOutgoingConnections(mergeDataNode, EConnectionType.FLOW_REF);
            if (refOutputConnections.size() != 0) {
                // there can be only one.
                IConnection refConnection = ((List<IConnection>) refOutputConnections).get(0);
                inputId = refConnection.getInputId();
                mergeDataNode.getOutgoingConnections().remove(refConnection);
                ((DataConnection) refConnection).setSource(hashNode);
                ((List<IConnection>) hashNode.getOutgoingConnections()).add(refConnection);
            }
        }
        dataConnec.setInputId(inputId);
        outgoingConnections.add(dataConnec);
        // add for bug TDI-21740
        if (inputId == 1) {
            ((List<IConnection>) oldNodeTarget.getIncomingConnections()).add(0, dataConnec);
        } else {
            ((List<IConnection>) oldNodeTarget.getIncomingConnections()).add(dataConnec);
        }

        ((DataConnection) mergeOutputConnection).setLineStyle(EConnectionType.FLOW_MAIN);
        ((DataConnection) mergeOutputConnection).setConnectorName(EConnectionType.FLOW_MAIN.getName());
        addDataNode(hashNode);

        INode afterMergeStart = hashNode.getSubProcessStartNode(false);
        INode mergeSubNodeStart = mergeDataNode.getSubProcessStartNode(false);

        boolean changeStartNode = false;
        if (mergeDataNode.getLinkedMergeInfo() == null || mergeDataNode.getLinkedMergeInfo().isEmpty()) {
            changeStartNode = true;
        } else {
            // if the next merge node don't have output connection, such as tFileOutputMSXML, means the next merge node
            // will not be changed to tHash component. And the connection between this node and the next merge node is
            // the first merge link, this node should be the start node
            Map<INode, Integer> nextMergeNodes = mergeDataNode.getLinkedMergeInfo();
            if (nextMergeNodes.size() == 1) {
                INode followMergeNode = nextMergeNodes.keySet().iterator().next();
                List nextMergeOutputConnections = NodeUtil.getOutgoingConnections(followMergeNode, IConnectionCategory.DATA);
                if (nextMergeNodes.get(followMergeNode) == 1) {
                    if (nextMergeOutputConnections == null || nextMergeOutputConnections.isEmpty()) {
                        changeStartNode = true;
                    }
                }
            }
        }
        if (changeStartNode) {
            INode oldStartNode = null;
            if (mergeDataNode.isThereLinkWithHash()) {
                oldStartNode = mergeDataNode.getSubProcessStartNode(false);
            } else {
                oldStartNode = mergeDataNode.getDesignSubjobStartNode();
            }
            ((AbstractNode) afterMergeStart).setStart(oldStartNode.isStart());
            ((AbstractNode) oldStartNode).setStart(false);
            for (INode curNode : dataNodeList) {
                if (curNode instanceof AbstractNode) {
                    if (curNode.getDesignSubjobStartNode().equals(oldStartNode)) {
                        ((AbstractNode) curNode).setDesignSubjobStartNode(afterMergeStart);
                    }
                }
            }

            // move all dependency output links from source node to new start node.
            // (EXECUTION_ORDER is enough, to keep the onComponentOk on the component the user wanted)
            List<IConnection> depConnections = (List<IConnection>) NodeUtil.getOutgoingConnections(oldStartNode,
                    IConnectionCategory.EXECUTION_ORDER);
            oldStartNode.getOutgoingConnections().removeAll(depConnections);
            ((List<IConnection>) afterMergeStart.getOutgoingConnections()).addAll(depConnections);
            for (IConnection curDepConnection : depConnections) {
                ((AbstractConnection) curDepConnection).setSource(afterMergeStart);
            }

            // move all dependency input links from source node to new start node.
            // (DEPENDENCY is needed for this one)
            depConnections = (List<IConnection>) NodeUtil.getIncomingConnections(oldStartNode, IConnectionCategory.DEPENDENCY);
            for (IConnection connection : connectionsToIgnoreInMerge) {
                if (depConnections.contains(connection)) {
                    depConnections.remove(connection);
                }
            }
            oldStartNode.getIncomingConnections().removeAll(depConnections);
            ((List<IConnection>) afterMergeStart.getIncomingConnections()).addAll(depConnections);
            for (IConnection curDepConnection : depConnections) {
                ((AbstractConnection) curDepConnection).setTarget(afterMergeStart);
            }

        }

        dataConnec = new DataConnection();
        dataConnec.setActivate(mergeOutputConnection.isActivate());
        dataConnec.setLineStyle(EConnectionType.RUN_AFTER);
        dataConnec.setConnectorName(EConnectionType.RUN_AFTER.getName());
        dataConnec.setTraceConnection(false);
        dataConnec.setName(hashNode.getUniqueName() + "_" + EConnectionType.RUN_AFTER.getDefaultLinkName()); //$NON-NLS-1$
        dataConnec.setSource(afterMergeStart);
        dataConnec.setTarget(mergeSubNodeStart);

        connectionsToIgnoreInMerge.add(dataConnec);

        ((List<IConnection>) mergeSubNodeStart.getIncomingConnections()).add(dataConnec);
        ((List<IConnection>) afterMergeStart.getOutgoingConnections()).add(dataConnec);
    }

    /**
     * nrousseau Comment method "checkUseParallelize".
     *
     * @param node
     */
    @SuppressWarnings("unchecked")
    private void checkUseParallelize(INode graphicalNode) {
        // check if the component can use Parallelize first
        if (!graphicalNode.getComponent().canParallelize()) {
            return;
        }
        IElementParameter enableParallelizeParameter = graphicalNode.getElementParameter(EParameterName.PARALLELIZE.getName());
        Boolean parallelEnabled = Boolean.FALSE;
        if (enableParallelizeParameter != null) {
            parallelEnabled = (Boolean) enableParallelizeParameter.getValue();
        }
        if (!parallelEnabled) {
            return;
        }
        INode refNode = buildCheckMap.get(graphicalNode);
        // for joblet node not active => not included in buildCheckMap
        if (refNode == null) {
            return;
        }
        List<? extends IConnection> connections = refNode.getIncomingConnections(EConnectionType.FLOW_MAIN);
        if (connections.size() == 0) {
            return;
        }
        DataConnection connection = (DataConnection) connections.get(0);

        // remove this connection from input list, as the input will be tAsyncIn
        refNode.getIncomingConnections().remove(connection);
        // take only the first connection, as this kind of component won't support multi input connections

        String suffix = graphicalNode.getUniqueName();

        // create tAsyncOut component
        IComponent component = ComponentsFactoryProvider.getInstance().get("tAsyncOut", //$NON-NLS-1$
                ComponentCategory.CATEGORY_4_DI.getName());
        if (component == null) {
            return;
        }
        DataNode asyncOutNode = new DataNode(component, "tAsyncOut_" + suffix); //$NON-NLS-1$
        asyncOutNode.setActivate(connection.isActivate());
        asyncOutNode.setStart(false);
        asyncOutNode.setDesignSubjobStartNode(refNode.getDesignSubjobStartNode());
        IMetadataTable newMetadata = refNode.getMetadataList().get(0).clone();// connection.getMetadataTable().clone();
        newMetadata.setTableName("tAsyncOut_" + suffix); //$NON-NLS-1$
        asyncOutNode.getMetadataList().remove(0);
        asyncOutNode.getMetadataList().add(newMetadata);
        asyncOutNode.setSubProcessStart(false);
        asyncOutNode.setProcess(graphicalNode.getProcess());
        List outgoingConnections = new ArrayList<IConnection>();
        List incomingConnections = new ArrayList<IConnection>();
        asyncOutNode.setIncomingConnections(incomingConnections);
        asyncOutNode.setOutgoingConnections(outgoingConnections);
        IElementParameter settingsParam = asyncOutNode.getProcess()
                .getElementParameter(EParameterName.PARALLELIZE_UNIT_SIZE.getName());
        IElementParameter asyncParam = asyncOutNode.getElementParameter("UNIT_SIZE"); //$NON-NLS-1$
        if (settingsParam != null && asyncParam != null) {
            asyncParam.setValue(settingsParam.getValue());
        }
        IElementParameter componentParam = graphicalNode.getElementParameter(EParameterName.PARALLELIZE_NUMBER.getName());
        asyncParam = asyncOutNode.getElementParameter(EParameterName.PARALLELIZE_NUMBER.getName());
        if (settingsParam != null && asyncParam != null) {
            asyncParam.setValue(componentParam.getValue());
        }
        componentParam = graphicalNode.getElementParameter(EParameterName.PARALLELIZE_KEEP_EMPTY.getName());
        asyncParam = asyncOutNode.getElementParameter(EParameterName.PARALLELIZE_KEEP_EMPTY.getName());
        if (settingsParam != null && asyncParam != null) {
            asyncParam.setValue(componentParam.getValue());
        }
        asyncParam = asyncOutNode.getElementParameter("DESTINATION"); //$NON-NLS-1$
        if (settingsParam != null && asyncParam != null) {
            asyncParam.setValue("tAsyncIn_" + suffix); //$NON-NLS-1$
        }

        // replace target to have the tAsyncOut component
        connection.setTarget(asyncOutNode);
        incomingConnections.add(connection);

        // create tAsyncIn component
        component = ComponentsFactoryProvider.getInstance().get("tAsyncIn", ComponentCategory.CATEGORY_4_DI.getName()); //$NON-NLS-1$
        if (component == null) {
            return;
        }
        DataNode asyncInNode = new DataNode(component, "tAsyncIn_" + suffix); //$NON-NLS-1$
        asyncInNode.setActivate(connection.isActivate());
        asyncInNode.setStart(true);
        asyncInNode.setDesignSubjobStartNode(asyncInNode);
        newMetadata = refNode.getMetadataList().get(0).clone(); // connection.getMetadataTable().clone();
        newMetadata.setTableName("tAsyncIn_" + suffix); //$NON-NLS-1$
        asyncInNode.getMetadataList().remove(0);
        asyncInNode.getMetadataList().add(newMetadata);
        asyncInNode.setSubProcessStart(true);
        asyncInNode.setProcess(graphicalNode.getProcess());
        outgoingConnections = new ArrayList<IConnection>();
        incomingConnections = new ArrayList<IConnection>();
        asyncInNode.setIncomingConnections(incomingConnections);
        asyncInNode.setOutgoingConnections(outgoingConnections);

        // create a new connection to make tAsyncIn -> output component
        DataConnection dataConnec = new DataConnection();
        dataConnec.setActivate(connection.isActivate());
        dataConnec.setConnectorName(EConnectionType.FLOW_MAIN.getName());
        dataConnec.setLineStyle(EConnectionType.FLOW_MAIN);
        dataConnec.setMetadataTable(newMetadata);
        dataConnec.setTraceConnection(connection.isTraceConnection());
        dataConnec.setTracesCondition(connection.getTracesCondition());
        dataConnec.setMonitorConnection(connection.isMonitorConnection());
        dataConnec.setEnabledTraceColumns(connection.getEnabledTraceColumns());
        dataConnec.setName("pRow_" + connection.getName()); //$NON-NLS-1$
        dataConnec.setSource(asyncInNode);
        dataConnec.setTarget(refNode);

        componentParam = graphicalNode.getElementParameter(EParameterName.PARALLELIZE_KEEP_EMPTY.getName());
        asyncParam = asyncInNode.getElementParameter(EParameterName.PARALLELIZE_KEEP_EMPTY.getName());
        if (settingsParam != null && asyncParam != null) {
            asyncParam.setValue(componentParam.getValue());
        }

        ((AbstractNode) refNode).setDesignSubjobStartNode(asyncInNode);

        outgoingConnections.add(dataConnec);
        ((List<DataConnection>) refNode.getIncomingConnections()).add(dataConnec);

        addDataNode(asyncInNode);
        addDataNode(asyncOutNode);
    }

    private void checkUseHadoopConfs(List<INode> newGraphicalNodes) {
        try {
            if (newGraphicalNodes == null || newGraphicalNodes.isEmpty()) {
                return;
            }
            List<INode> newGraphicalNodeList = new LinkedList<>(newGraphicalNodes);
            newGraphicalNodeList.sort((left, right) -> {
                try {
                    if (left == null || right == null) {
                        // keep the order same like before
                        return 0;
                    }

                    final String prejobCompName = "tPrejob";
                    INode leftStartNode = left.getSubProcessStartNode(true);
                    INode rightStartNode = right.getSubProcessStartNode(true);

                    boolean isLeftPrejob = Optional.ofNullable(leftStartNode).map(n -> n.getComponent()).map(c -> c.getName())
                            .map(n -> prejobCompName.equals(n)).orElse(false);
                    boolean isRightPrejob = Optional.ofNullable(rightStartNode).map(n -> n.getComponent()).map(c -> c.getName())
                            .map(n -> prejobCompName.equals(n)).orElse(false);
                    /**
                     * Same logic for prejob in joblet
                     */
                    if (isLeftPrejob && isRightPrejob) {
                        // keep the order same like before
                        return 0;
                    } else if (isLeftPrejob) {
                        return -1;
                    } else if (isRightPrejob) {
                        return 1;
                    }
                } catch (Throwable e) {
                    ExceptionHandler.process(e);
                }

                // keep the order same like before
                return 0;
            });
            String addedClusterId = null;
            boolean added = false;
            for (INode node : newGraphicalNodeList) {
                String hadoopClusterId = JavaProcessUtil.getHadoopClusterItemId(node);
                if (StringUtils.isBlank(hadoopClusterId)) {
                    continue;
                }
                if (added) {
                    if (StringUtils.equals(addedClusterId, hadoopClusterId)) {
                        ExceptionHandler.log(
                                "Duplicated defination of hadoop cluster metadata, using existing connection is recommanded");
                    } else {
                        ExceptionHandler.log("Each job can only load one hadoop configuration, loaded cluster metadata: "
                                + addedClusterId + ", ignored cluster metadata: " + hadoopClusterId);
                    }
                } else {
                    added = checkUseHadoopConfs(node, hadoopClusterId);
                    if (added) {
                        addedClusterId = hadoopClusterId;
                    }
                }
            }
        } catch (Throwable e) {
            ExceptionHandler.process(e);
        }
    }

    private boolean checkUseHadoopConfs(INode graphicalNode, String id) {
        boolean added = false;
        if (StringUtils.isNotBlank(id)) {
            DataNode confNode = createHadoopConfManagerNode(id, "tHadoopConfManager", ComponentCategory.CATEGORY_4_DI,
                    graphicalNode);
            if (confNode != null) {
                INode addedNode = addDataNode(confNode);
                added = (addedNode != null);
                if (!(addedNode instanceof AbstractNode)) {
                    return added;
                }
                AbstractNode addedConfDataNode = (AbstractNode) addedNode;
                INode bdDataNode = Optional.ofNullable(buildCheckMap).map(m -> m.get(graphicalNode)).orElse(null);
                if (bdDataNode != null && addedConfDataNode != null && bdDataNode.isActivate()
                        && addedConfDataNode.isActivate()) {
                    INode startNode = bdDataNode.getSubProcessStartNode(true);
                    if (Optional.ofNullable(startNode).map(n -> n.getComponent()).map(c -> c.getName())
                            .map(n -> "tPrejob".equals(n)).orElse(false)) {
                        IConnection conn = null;
                        try {
                            conn = findConnection2InsertHadoopConf(startNode, bdDataNode, new HashSet<>());
                        } catch (Throwable e) {
                            ExceptionHandler.process(e);
                        }
                        if (conn instanceof AbstractConnection) {
                            INode relinkNode = conn.getTarget();
                            if (relinkNode instanceof AbstractNode) {
                                ((AbstractConnection) conn).setTarget(addedConfDataNode);

                                addedConfDataNode.setStart(false);
                                addedConfDataNode.setSubProcessStart(relinkNode.isSubProcessStart());
                                addedConfDataNode.setDesignSubjobStartNode(null);

                                DataConnection onCompOkConn = new DataConnection();
                                onCompOkConn.setActivate(true);
                                onCompOkConn.setLineStyle(EConnectionType.ON_COMPONENT_OK);
                                onCompOkConn.setTraceConnection(false);
                                onCompOkConn
                                        .setName("after_" + addedConfDataNode.getUniqueName() + "_" + relinkNode.getUniqueName()); //$NON-NLS-1$ //$NON-NLS-2$
                                onCompOkConn.setSource(addedConfDataNode);
                                onCompOkConn.setTarget(relinkNode);
                                onCompOkConn.setConnectorName(EConnectionType.ON_COMPONENT_OK.getName());

                                ((AbstractNode) relinkNode).setSubProcessStart(true);
                                ((AbstractNode) relinkNode).setDesignSubjobStartNode(null);
                                createDataConnection(addedConfDataNode, (AbstractNode) relinkNode, onCompOkConn, null);
                            }
                        }
                    }
                }
            }
        }
        return added;
    }

    private IConnection findConnection2InsertHadoopConf(INode startNode, INode node, Set<INode> visited) {
        if (node == null || startNode == null) {
            return null;
        }
        if (visited.contains(node)) {
            return null;
        } else {
            visited.add(node);
        }
        List<? extends IConnection> inConns = node.getIncomingConnections();
        if (inConns == null) {
            return null;
        }
        for (IConnection inConn : inConns) {
            if (!inConn.isActivate()) {
                continue;
            }
            EConnectionType lineStyle = inConn.getLineStyle();
            if (lineStyle.hasConnectionCategory(IConnectionCategory.CONDITION)
                    && lineStyle.hasConnectionCategory(IConnectionCategory.DEPENDENCY)) {
                return inConn;
            }
            IConnection conn = findConnection2InsertHadoopConf(startNode, inConn.getSource(), visited);
            if (conn != null) {
                return conn;
            }
        }
        return null;
    }

    /**
     * Creates a hadoop configuration manager component
     *
     * @param hadoopClusterItemId the hadoop cluster metadata id
     * @param componentName the name of the component to instantiate
     * @param componentCategory the {@link ComponentCategory} of the component to instantiate
     * @param node the {@link INode} to which the hadoop cluster metadata is linked
     * @return the hadoop configuration manager {@link DataNode}
     */
    public static DataNode createHadoopConfManagerNode(String hadoopClusterItemId, String componentName,
            ComponentCategory componentCategory, INode node) {
        IHadoopClusterService hadoopClusterService = HadoopRepositoryUtil.getHadoopClusterService();
        if (!hadoopClusterService.isUseDynamicConfJar(hadoopClusterItemId)) {
            return null;
        }
        Optional<HadoopConfJarBean> confJarBean = hadoopClusterService.getCustomConfsJar(hadoopClusterItemId, false, false);
        if (!confJarBean.isPresent()) {
            return null;
        }
        IComponent component = ComponentsFactoryProvider.getInstance().get(componentName, componentCategory.getName());
        if (component != null) {
            DataNode confNode = new DataNode(component, component.getName() + "_" + node.getUniqueName()); //$NON-NLS-1$
            confNode.setActivate(node.isActivate());
            confNode.setStart(true);
            confNode.setSubProcessStart(true);
            confNode.setDesignSubjobStartNode(confNode);
            confNode.setProcess(node.getProcess());
            if (ComponentCategory.CATEGORY_4_DI == componentCategory) {
                IElementParameter clusterIdParam = confNode.getElementParameter("CLUSTER_ID"); //$NON-NLS-1$
                clusterIdParam.setValue(hadoopClusterItemId);
            }
            IElementParameter confLibParam = confNode.getElementParameter("CONF_LIB"); //$NON-NLS-1$
            confLibParam.setValue(TalendTextUtils.addQuotes(confJarBean.get().getCustomConfJarName()));
            IElementParameter setConfParam = confNode.getElementParameter("SET_HADOOP_CONF"); //$NON-NLS-1$
            if (setConfParam != null) {
                setConfParam.setValue(Boolean.valueOf(confJarBean.get().isOverrideCustomConf()));
            }
            IElementParameter confPathParam = confNode.getElementParameter("HADOOP_CONF_SPECIFIC_JAR"); //$NON-NLS-1$
            if (confPathParam != null) {
                String jarPath = null;
                if (confJarBean.get().isContextMode()) {
                    jarPath = confJarBean.get().getOverrideCustomConfPath();
                } else {
                    jarPath = TalendTextUtils.addQuotes(confJarBean.get().getOriginalOverrideCustomConfPath());
                }
                confPathParam.setValue(jarPath);
            }
            return confNode;
        }
        return null;
    }

    private Node cloneGraphicalNode(IProcess process, INode node) {
        IComponent component = node.getComponent();
        Node newGraphicalNode = null;
        if (EComponentType.GENERIC.equals(component.getComponentType())) {
            newGraphicalNode = new Node(node, (IProcess2) process);
        } else {
            newGraphicalNode = new Node(node.getComponent(), (IProcess2) process);
        }
        newGraphicalNode.setMetadataList(node.getMetadataList());

        IExternalNode externalNode = node.getExternalNode();
        if (externalNode != null) {
            AbstractExternalData externalEmfData = externalNode.getExternalEmfData();
            newGraphicalNode.getExternalNode().setExternalEmfData(externalEmfData);
            newGraphicalNode.getExternalNode().setInternalMapperModel(externalNode.getInternalMapperModel());
        }
        // fwang fixed bug TDI-8027
        IExternalData externalData = node.getExternalData();
        if (externalData != null) {
            try {
                newGraphicalNode.setExternalData(externalData.clone());
            } catch (CloneNotSupportedException e) {
                newGraphicalNode.setExternalData(externalData);
            }
        }

        copyElementParametersValue(node, newGraphicalNode);
        newGraphicalNode.setDummy(node.isDummy());

        ValidationRulesUtil.createRejectConnector(newGraphicalNode);
        ValidationRulesUtil.updateRejectMetatable(newGraphicalNode, node);
        return newGraphicalNode;
    }

    @SuppressWarnings("unchecked")
    public INode buildNodeFromNode(final INode graphicalNode, final IProcess process) {
        if (buildCheckMap == null) {
            initialize();
        }
        if (buildGraphicalMap.containsKey(graphicalNode)) {
            return (INode) buildGraphicalMap.get(graphicalNode);
        }

        Node newGraphicalNode = cloneGraphicalNode(process, graphicalNode);
        NodeContainer nc = ((Process) process).loadNodeContainer(newGraphicalNode, false);
        ((Process) process).addNodeContainer(nc);

        if (buildGraphicalMap == null) {
            initialize();
        }
        buildGraphicalMap.put(graphicalNode, newGraphicalNode);

        for (IConnection connection : (List<IConnection>) graphicalNode.getOutgoingConnections()) {
            if (!connection.isActivate()) {
                continue;
            }
            INode connTarget = connection.getTarget();
            if (connTarget.getJobletNode() != null) {
                connTarget = connTarget.getJobletNode();
            }
            INode target = buildNodeFromNode(connTarget, process);

            IConnection dataConnec = new Connection(newGraphicalNode, target, connection.getLineStyle(),
                    connection.getConnectorName(), connection.getMetaName(), connection.getName(), connection.getUniqueName(),
                    connection.isMonitorConnection());
            if (IAdditionalInfo.class.isInstance(connection) && IAdditionalInfo.class.isInstance(dataConnec)) {
                IAdditionalInfo.class.cast(connection).cloneAddionalInfoTo((IAdditionalInfo) dataConnec);
            }
            // incomingConnections = (List<Connection>) target.getIncomingConnections();
            // if (incomingConnections == null) {
            // incomingConnections = new ArrayList<Connection>();
            // }
            // outgoingConnections.add(dataConnec);
            // incomingConnections.add(dataConnec);
            copyElementParametersValue(connection, dataConnec);
            dataConnec.setTraceConnection(connection.isTraceConnection());
        }

        newGraphicalNode.setActivate(graphicalNode.isActivate());
        newGraphicalNode.setStart(graphicalNode.isStart());

        return newGraphicalNode;
    }

    private void buildGraphicalNodeForInputConnections(IProcess process, INode graphicalNode, INode newGraphicalNode,
            Set<INode> visitedNodes) {
        if (visitedNodes.contains(graphicalNode)) {
            return;
        } else {
            visitedNodes.add(graphicalNode);
        }
        List<IConnection> connections = (List<IConnection>) graphicalNode.getIncomingConnections();
        if (connections == null || connections.isEmpty()) {
            return;
        }
        for (IConnection connection : connections) {
            if (!connection.isActivate()) {
                continue;
            }
            INode sourceNode = connection.getSource();
            // if it exists in the essential nodes, means the input and output are already both created, then no need to
            // create it again
            INode newSourceNode = (INode) buildGraphicalMap.get(sourceNode);
            if (newSourceNode == null) {
                // if it not exists in the essential nodes, create a new one, and don't put it into the Map!
                newSourceNode = cloneGraphicalNode(process, sourceNode);
                NodeContainer nc = ((Process) process).loadNodeContainer((Node) newSourceNode, false);
                ((Process) process).addNodeContainer(nc);
                IConnection dataConnec = new Connection(newSourceNode, newGraphicalNode, connection.getLineStyle(),
                        connection.getConnectorName(), connection.getMetaName(), connection.getName(), connection.getUniqueName(),
                        connection.isMonitorConnection());
                if (IAdditionalInfo.class.isInstance(connection) && IAdditionalInfo.class.isInstance(dataConnec)) {
                    IAdditionalInfo.class.cast(connection).cloneAddionalInfoTo((IAdditionalInfo) dataConnec);
                }
                copyElementParametersValue(connection, dataConnec);
                dataConnec.setTraceConnection(connection.isTraceConnection());
                buildGraphicalNodeForInputConnections(process, sourceNode, newSourceNode, visitedNodes);
            }
        }
    }

    /**
     * nrousseau Comment method "buildCopyOfGraphicalNodeList".
     *
     * @param graphicalNodeList
     * @return
     */
    private List<INode> buildCopyOfGraphicalNodeList(List<INode> graphicalNodeList) {
        if (graphicalNodeList.size() == 0) {
            return Collections.emptyList();
        }
        buildGraphicalMap.clear();

        Property property = null;
        AbstractMultiPageTalendEditor editor = null;
        if (process instanceof IProcess2) {
            property = ((IProcess2) process).getProperty();
            IEditorPart editorPart = ((IProcess2) process).getEditor();
            if (editorPart instanceof AbstractMultiPageTalendEditor) {
                editor = (AbstractMultiPageTalendEditor) editorPart;
            }
        } else {
            property = PropertiesFactory.eINSTANCE.createProperty();
            property.setId(graphicalNodeList.get(0).getProcess().getId() + "_generated");
        }
        duplicatedProcess = new Process(property);
        ((Process) duplicatedProcess).setComponentsType(process.getComponentsType());
        duplicatedProcess.setDuplicate(true);
        duplicatedProcess.setActivate(false);
        ((Process) duplicatedProcess).setGeneratingProcess(this);
        ((Process) duplicatedProcess).setProcessModified(false);
        ((Process) duplicatedProcess).setNeededRoutines(process.getNeededRoutines());
        ((Process) duplicatedProcess).setEditor(editor);
        List<RoutinesParameterType> routines = null;
        if (process instanceof Process) {
            routines = ((Process) process).getRoutineDependencies();
        }
        ((Process) duplicatedProcess).setRoutineDependencies(routines);

        copyElementParametersValue(graphicalNodeList.get(0).getProcess(), duplicatedProcess);

        // keep the same instance of context manager as it won't be modified
        duplicatedProcess.setContextManager(process.getContextManager());
        for (INode node : graphicalNodeList) {
            if (node.isSubProcessStart() && node.isActivate()) {
                buildNodeFromNode(node, duplicatedProcess);
            }
        }
        Set<INode> visitedNodes = new HashSet<INode>();
        Set<Map.Entry> entrySet = buildGraphicalMap.entrySet();
        for (Map.Entry entry : entrySet) {
            INode node = (INode) entry.getKey();
            INode newNode = (INode) entry.getValue();
            buildGraphicalNodeForInputConnections(duplicatedProcess, node, newNode, visitedNodes);
        }

        // make sure the new tUnite incomingConnections order is the same as the old one. @see
        // Connection.setInputId(int id)
        for (INode oldNode : graphicalNodeList) {
            if (oldNode.getComponent().useMerge()) {
                INode newNode = (INode) buildGraphicalMap.get(oldNode);
                adjustMergeOrderForDuplicateNode(oldNode, newNode);
            }
        }

        List<INode> newBuildNodeList = new ArrayList<INode>();

        for (INode gnode : graphicalNodeList) {
            INode newNode = (INode) buildGraphicalMap.get(gnode);
            if (newNode != null) {
                newBuildNodeList.add(newNode);
            }

        }
        for (INode node : newBuildNodeList) {
            if (node.isExternalNode()) {
                node.getExternalNode().initialize();
            }
        }
        for (INode node : newBuildNodeList) {
            if (node.getComponentProperties() != null) {
                synRefProperties(node.getComponentProperties());
            }
        }

        duplicatedProcess.setActivate(true);
        duplicatedProcess.checkStartNodes();
        return newBuildNodeList;
    }

    /**
     * qzhang Comment method "replaceJoblets".
     *
     * @param graphicalNodeList
     * @return
     */
    public void replaceNodeFromProviders(List<INode> graphicalNodeList) {
        List<INode> orginalList = new ArrayList<INode>(graphicalNodeList);
        IJobletProviderService jobletService = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IJobletProviderService.class)) {
            jobletService = (IJobletProviderService) GlobalServiceRegister.getDefault().getService(IJobletProviderService.class);
        }
        boolean needReplaceForJoblet = false;
        for (INode node : orginalList) {
            if (jobletService != null && jobletService.isJobletComponent(node)) {
                if (ProcessorUtilities.isGeneratePomOnly()) {
                // skip any joblet contained during the pom generation
                continue;
                }
                needReplaceForJoblet = true;
            }
            for (IReplaceNodeInProcess replaceProvider : ReplaceNodesInProcessProvider.findReplaceNodesProvider()) {
                replaceProvider.rebuildGraphicProcessFromNode(node, graphicalNodeList);
            }
        }

        // All replace for Parallelization done by the start node, but joblet node maybe existed after
        // for the new nodes that Joblet added to graphicalNodeList, also need to consider replace node
        // such as ReplaceParallelization for Partition row in Joblet
        if (needReplaceForJoblet) {
            replaceNodeFromProviders(graphicalNodeList);
        }
    }

    private INode addDataNode(INode dataNode) {
        if (dataNode != null) {
            String addedUniqueName = dataNode.getUniqueName();
            for (INode node : getNodeList()) {
                String uniqueName = node.getUniqueName();
                if (addedUniqueName.equals(uniqueName)) {
                    return node;
                }
            }

            getNodeList().add(dataNode);
        }
        return dataNode;
    }

    @Override
    public List<INode> getNodeList() {
        return dataNodeList;
    }

    /**
     * Getter for duplicatedProcess.
     *
     * @return the duplicatedProcess
     */
    public IProcess getDuplicatedProcess() {
        return this.duplicatedProcess;
    }

    /**
     * xtan make sure the new tUnite incomingConnections order is the same as the old one.(bug:3417).
     *
     * @see Connection.setInputId(int id)
     *
     * @param oldtUnite
     * @param newtUnite
     */
    private void adjustMergeOrderForDuplicateNode(INode oldtUnite, INode newtUnite) {
        if (newtUnite == null) {
            return;
        }
        List<IConnection> incomingConnectionsOld = (List<IConnection>) oldtUnite.getIncomingConnections();
        List<IConnection> incomingConnectionsNew = (List<IConnection>) newtUnite.getIncomingConnections();

        Iterator<IConnection> iteratorOld = incomingConnectionsOld.iterator();
        int i = 0;
        while (iteratorOld.hasNext()) {
            IConnection connOld = iteratorOld.next();
            Iterator<IConnection> iteratorNew = incomingConnectionsNew.iterator();
            int j = 0;
            while (iteratorNew.hasNext()) {
                IConnection connNew = iteratorNew.next();
                if (connNew.getName().equals(connOld.getName()) && i != j) {
                    if (incomingConnectionsNew.size() > i) {
                        Collections.swap(incomingConnectionsNew, i, j);
                    }
                    break;
                }
                j++;
            }
            i++;
        }
    }

    @SuppressWarnings("unchecked")
    private INode addVParallelizeBetween(INode sourceNode, INode targetNode, IConnection connection, IProcess process,
            List<? extends IElementParameter> parameters) {

        IComponent tempNode = ComponentsFactoryProvider.getInstance().get("tParallelize", //$NON-NLS-1$
                ComponentCategory.CATEGORY_4_DI.getName());
        if (tempNode == null) {
            return targetNode;
        }

        DataNode parallelizeNode = null;
        boolean alreadyHave = false;
        String key = "tParallelize_" + sourceNode.getUniqueName();//$NON-NLS-1$
        if (parallCheckMap.get(key) != null) {
            parallelizeNode = (DataNode) parallCheckMap.get(key);
            alreadyHave = true;
        } else {
            String uniqueName = "tParallelize_" + connection.getUniqueName();//$NON-NLS-1$
            parallelizeNode = new DataNode(
                    ComponentsFactoryProvider.getInstance().get("tParallelize", ComponentCategory.CATEGORY_4_DI.getName()), //$NON-NLS-1$
                    uniqueName);

            // DataNode hashNode = new DataNode(component, uniqueName);
            parallelizeNode.setActivate(connection.isActivate());
            parallelizeNode.setStart(false);
            parallelizeNode.setDesignSubjobStartNode(sourceNode.getDesignSubjobStartNode());
            IMetadataTable newMetadata = connection.getMetadataTable().clone();
            newMetadata.setTableName(uniqueName);
            parallelizeNode.getMetadataList().remove(0);
            parallelizeNode.getMetadataList().add(newMetadata);
            parallelizeNode.setSubProcessStart(true);
            parallelizeNode.setProcess(process);

            // when using parallelize function in tMap, "Die when one of parallelize subjobs fails" of tParallelize
            // should be set true
            IElementParameter dieOnErrorParameters = parallelizeNode.getElementParameter("DIE_ON_ERROR");
            if (dieOnErrorParameters == null) {
                dieOnErrorParameters = new ElementParameter(parallelizeNode);
                dieOnErrorParameters.setName("DIE_ON_ERROR");
                dieOnErrorParameters.setFieldType(EParameterFieldType.CHECK);
                ((List<IElementParameter>) parallelizeNode.getElementParameters()).add(dieOnErrorParameters);
            }
            dieOnErrorParameters.setValue(Boolean.TRUE);

            List<IConnection> outgoingConnections = new ArrayList<IConnection>();
            List<IConnection> incomingConnections = new ArrayList<IConnection>();
            parallelizeNode.setIncomingConnections(incomingConnections);
            parallelizeNode.setOutgoingConnections(outgoingConnections);

            addDataNode(parallelizeNode);

            parallCheckMap.put(key, parallelizeNode);
        }
        if (!alreadyHave) {
            DataConnection dataConnec = new DataConnection();
            dataConnec.setActivate(connection.isActivate());
            dataConnec.setLineStyle(EConnectionType.RUN_AFTER);
            dataConnec.setTraceConnection(connection.isTraceConnection());
            dataConnec.setTracesCondition(connection.getTracesCondition());
            dataConnec.setEnabledTraceColumns(connection.getEnabledTraceColumns());
            dataConnec.setMonitorConnection(connection.isMonitorConnection());
            // dataConnec.setLineStyle(EConnectionType.THEN_RUN);
            if (!sourceNode.getMetadataList().isEmpty()) {
                dataConnec.setMetadataTable(sourceNode.getMetadataList().get(0));
            }
            dataConnec.setName("after_" + sourceNode.getUniqueName()); //$NON-NLS-1$
            // dataConnec.setConnectorName(EConnectionType.THEN_RUN.getName());
            dataConnec.setConnectorName(EConnectionType.RUN_AFTER.getName());
            dataConnec.setSource(sourceNode);
            // dataConnec.setSource(subDataNodeStartTarget);
            dataConnec.setTarget(parallelizeNode);
            // dataConnec.setTarget(subDataNodeStartSource);

            // the target component can't be start in all case, so no matter where it has been defined, remove
            // the start state.
            targetNode.setStart(false);

            ((List<IConnection>) parallelizeNode.getIncomingConnections()).add(dataConnec);
            ((List<IConnection>) sourceNode.getOutgoingConnections()).add(dataConnec);
        }

        // from current node to vFlowMeter node.
        DataConnection dataConnec = new DataConnection();
        dataConnec.setActivate(connection.isActivate());

        dataConnec.setLineStyle(EConnectionType.PARALLELIZE);
        IElementParameter param2 = new ElementParameter(dataConnec);
        param2.setName(EParameterName.TRACES_CONNECTION_ENABLE.getName());
        param2.setDisplayName(EParameterName.TRACES_CONNECTION_ENABLE.getDisplayName());
        param2.setFieldType(EParameterFieldType.CHECK);
        param2.setValue(Boolean.TRUE);
        param2.setCategory(EComponentCategory.ADVANCED);
        param2.setShow(false);
        param2.setNumRow(1);
        ((List<IElementParameter>) dataConnec.getElementParameters()).add(param2);

        dataConnec.setTraceConnection(connection.isTraceConnection());
        dataConnec.setMonitorConnection(connection.isMonitorConnection());
        dataConnec.setTracesCondition(connection.getTracesCondition());
        dataConnec.setEnabledTraceColumns(connection.getEnabledTraceColumns());
        dataConnec.setMetadataTable(connection.getMetadataTable());
        dataConnec.setName("parallelize_" + connection.getUniqueName());
        // dataConnec.setUniqueName(connection.getUniqueName());
        dataConnec.setSource(parallelizeNode);
        dataConnec.setTarget(targetNode);
        dataConnec.setCondition(connection.getCondition());
        dataConnec.setConnectorName(EConnectionType.PARALLELIZE.getName());
        dataConnec.setInputId(connection.getInputId());

        targetNode.setStart(false);
        ((List<IConnection>) targetNode.getIncomingConnections()).add(dataConnec);
        ((List<IConnection>) parallelizeNode.getOutgoingConnections()).add(dataConnec);

        return parallelizeNode;
    }

    private void synRefProperty(ComponentReferenceProperties<?> refProperties) {
        String refCompInstId = null;
        org.talend.daikon.properties.property.Property<String> refCompInstIdProp = refProperties.componentInstanceId;
        if (refCompInstIdProp != null) {
            refCompInstId = refCompInstIdProp.getValue();
        }
        if (refCompInstId != null && StringUtils.isNotEmpty(refCompInstId)) {
            for (INode curNode : getNodeList()) {
                if (curNode.getUniqueName().equals(refCompInstId)) {
                    refProperties.setReference(curNode.getComponentProperties());
                    break;
                }
            }
        } else {
            refProperties.setReference(null);
        }

    }

    private void synRefProperties(Properties properties) {
        properties.accept(new PropertiesVisitor() {

            @Override
            public void visit(Properties curProperties, Properties parent) {
                if (curProperties instanceof ComponentReferenceProperties<?>) {
                    synRefProperty((ComponentReferenceProperties) curProperties);
                }
            }
        }, null);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.process.IGeneratingProcess#generateAdditionalCode()
     */
    @Override
    public void generateAdditionalCode() {
        // nothing by default
    }

}
