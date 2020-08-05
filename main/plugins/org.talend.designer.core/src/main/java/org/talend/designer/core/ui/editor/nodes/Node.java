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
package org.talend.designer.core.ui.editor.nodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.avro.Schema;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.emf.common.util.EList;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.CommonExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.components.EComponentType;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IMultipleComponentItem;
import org.talend.core.model.components.IMultipleComponentManager;
import org.talend.core.model.components.IODataComponent;
import org.talend.core.model.general.ILibrariesService;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.metadata.IEbcdicConstant;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.metadata.MetadataToolAvroHelper;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.metadata.types.JavaType;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.metadata.types.PerlTypesManager;
import org.talend.core.model.metadata.types.TypesManager;
import org.talend.core.model.process.BlockCode;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.ElementParameterParser;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IExternalData;
import org.talend.core.model.process.IExternalNode;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.process.INodeReturn;
import org.talend.core.model.process.IPerformance;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.process.IReplaceNodeHandler;
import org.talend.core.model.process.Problem;
import org.talend.core.model.process.Problem.ProblemStatus;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.ExternalNodesFactory;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.model.utils.NodeUtil;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.utils.ConvertJobsUtil;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.service.IMRProcessService;
import org.talend.core.service.IStormProcessService;
import org.talend.core.services.ICoreTisService;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.IJobletProviderService;
import org.talend.core.ui.ISparkJobletProviderService;
import org.talend.core.ui.ISparkStreamingJobletProviderService;
import org.talend.core.ui.ITestContainerProviderService;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.core.ui.metadata.dialog.MetadataDialog;
import org.talend.core.ui.process.IGEFProcess;
import org.talend.core.ui.process.IGraphicalNode;
import org.talend.core.ui.services.IDesignerCoreUIService;
import org.talend.designer.core.CheckNodeManager;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.ICheckNodesService;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.AbstractBasicComponent;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.model.components.NodeReturn;
import org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.ActiveProcessTracker;
import org.talend.designer.core.ui.editor.AbstractTalendEditor;
import org.talend.designer.core.ui.editor.cmd.ChangeMetadataCommand;
import org.talend.designer.core.ui.editor.cmd.ConnectionCreateCommand;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.jobletcontainer.JobletContainer;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.editor.properties.NodeQueryCheckUtil;
import org.talend.designer.core.ui.editor.properties.controllers.ColumnListController;
import org.talend.designer.core.ui.editor.properties.controllers.SynchronizeSchemaHelper;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainer;
import org.talend.designer.core.ui.preferences.TalendDesignerPrefConstants;
import org.talend.designer.core.ui.projectsetting.ElementParameter2ParameterType;
import org.talend.designer.core.ui.views.problems.Problems;
import org.talend.designer.core.utils.TRunjobUtil;
import org.talend.designer.core.utils.UnifiedComponentUtil;
import org.talend.designer.core.utils.UpgradeElementHelper;
import org.talend.designer.joblet.model.JobletNode;
import org.talend.designer.joblet.model.JobletProcess;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.designer.runprocess.ItemCacheManager;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.utils.AutoConvertTypesUtils;

/**
 * Object that describes the node. All informations on nodes are stored in this class. <br/>
 *
 * $Id$
 *
 */
public class Node extends Element implements IGraphicalNode {

    private static Logger log = Logger.getLogger(Node.class);

    // true if this node is set as a start node.
    private boolean start;

    // true if this node is activated.
    private boolean activate = true;

    private int currentStatus, oldStatus = 0;

    private boolean needlibrary = false;

    // properties
    public static final String LOCATION = "nodeLocation"; //$NON-NLS-1$

    public static final String SIZE = "nodeSize"; //$NON-NLS-1$

    public static final String INPUTS = "inputs"; //$NON-NLS-1$

    public static final String OUTPUTS = "outputs"; //$NON-NLS-1$

    public static final String PERFORMANCE_DATA = "perfData"; //$NON-NLS-1$

    public static final String TRACE_DATA = "traceData"; //$NON-NLS-1$

    public static final String UPDATE_STATUS = "addStatus"; //$NON-NLS-1$

    public static final String MODIFY_NODELABEL = "modifyNodeLabel"; //$NON-NLS-1$

    public static final String RETURNS_CHANGED = "returns changed"; //$NON-NLS-1$

    public static final String ICON_CHANGE = "iconChange";//$NON-NLS-1$

    public static final int DEFAULT_SIZE = 32;

    protected Point location = new Point(0, 0);

    protected String name, label;

    private final List<IConnection> outputs = new ArrayList<IConnection>();

    private final List<IConnection> inputs = new ArrayList<IConnection>();

    private NodeLabel nodeLabel;

    private NodeError nodeError;

    private NodeProgressBar nodeProgressBar;

    private List<IMetadataTable> metadataList;

    protected List<? extends INodeReturn> listReturn;

    protected List<? extends INodeConnector> listConnector;

    private IComponent delegateComponent;

    private IComponent component;

    private String showHintText;

    private String connectionName;

    private String labelToParse;

    private String hintToParse;

    private String connectionToParse;

    private IExternalNode externalNode = null;

    private NodeContainer nodeContainer;

    private String performanceData;

    private IProcess2 process = null;

    private boolean readOnly = false;

    private boolean forceReadOnly = false;

    private static final String COMPARE_STR1 = "tDBInput"; //$NON-NLS-1$

    private static final String COMPARE_STR2 = "_MySchema_"; //$NON-NLS-1$

    private Dimension size;

    private boolean dummy;

    private final IComponent oldcomponent;

    private List<String> errorList = new ArrayList<String>(), warningList = new ArrayList<String>();

    private boolean schemaSynchronized = true;

    private boolean reloadingComponent = false;

    private boolean showHint;

    private boolean errorFlag;

    private boolean compareFlag;

    private boolean hasValidationRule;

    private String errorInfo;

    private boolean checkProperty;

    private boolean insertSet = false;

    private boolean template = false;

    private boolean isJunitCreate = false;

    private boolean generatedByJobscriptBool = false;

    private Node jobletNode = null;

    private Node junitNode = null;

    private String inOutUniqueName;

    private String joblet_unique_name;

    private String index;

    private Boolean takeSchema = null;

    private EConnectionType virtualLinkTo;

    private static final String TPREJOB_STR = "tPrejob"; //$NON-NLS-1$

    private static final String TPOSTJOB_STR = "tPostjob"; //$NON-NLS-1$

    private boolean subtreeStart;

    boolean isMapReduceStart = false;

    boolean isJunitStart = false;

    // as the talend job contains multiple mapreduce jobs, use this to indicate which mapreduce job contains this
    // graphic node
    private Integer mrGroupId;

    // for the component which will generate multiple mapreduce jobs, count the size of mapreduce jobs.
    private Integer mrJobInGroupCount;

    private Integer mrJobIDInGroup;

    // mrContainsReduce of is true,then show Reduce progressbar
    private boolean mrContainsReduce;

    private boolean isUpdate;

    private ComponentProperties componentProperties;

    private List<String> previousCustomLibs = null;

    private IReplaceNodeHandler replaceNodeHandler;

    /**
     * Getter for index.
     *
     * @return the index
     */
    public String getIndex() {
        return this.index;
    }

    /**
     * Sets the takeSchema.
     *
     * @param takeSchema the takeSchema to set
     */
    public void setTakeSchema(Boolean takeSchema) {
        this.takeSchema = takeSchema;
    }

    /**
     * Sets the index.
     *
     * @param index the index to set
     */
    public void setIndex(String index) {
        this.index = index;
    }

    @Override
    public boolean isGeneratedByJobscriptBool() {
        return this.generatedByJobscriptBool;
    }

    public void setGeneratedByJobscriptBool(boolean generatedByJobscriptBool) {
        this.generatedByJobscriptBool = generatedByJobscriptBool;
    }

    @Override
    public boolean isTemplate() {
        return this.template;
    }

    public void setTemplate(boolean template) {
        this.template = template;
    }

    public boolean isJunitCreate() {
        return this.isJunitCreate;
    }

    public void setJunitCreate(boolean isJunitCreate) {
        this.isJunitCreate = isJunitCreate;
    }

    @Override
    public boolean isCheckProperty() {
        return this.checkProperty;
    }

    @Override
    public void setCheckProperty(boolean checkProperty) {
        this.checkProperty = checkProperty;
    }

    public boolean isErrorFlag() {
        return this.errorFlag;
    }

    @Override
    public void setErrorFlag(boolean errorFlag) {
        this.errorFlag = errorFlag;
    }

    public boolean isCompareFlag() {
        return this.compareFlag;
    }

    @Override
    public void setCompareFlag(boolean compareFlag) {
        this.compareFlag = compareFlag;
    }

    public String getErrorInfo() {
        return this.errorInfo;
    }

    @Override
    public void setErrorInfo(String errorInfo) {
        this.errorInfo = errorInfo;
    }

    public boolean isHasValidationRule() {
        return this.hasValidationRule;
    }

    public void setHasValidationRule(boolean hasValidationRule) {
        this.hasValidationRule = hasValidationRule;
        firePropertyChange(EParameterName.VALIDATION_RULES.getName(), null, null);
    }

    /**
     * This constructor is called when the node is created from the palette the unique name will be determined with the
     * number of components of this type.
     *
     * @param component
     */
    public Node(IComponent component) {
        this.oldcomponent = component;
        this.delegateComponent = UnifiedComponentUtil.getDelegateComponent(component);
        this.process = ActiveProcessTracker.getCurrentProcess();
        currentStatus = 0;

        init(component);
        initDefaultElementParameters();
        IElementParameter param = getElementParameter(EParameterName.REPOSITORY_ALLOW_AUTO_SWITCH.getName());
        if (param != null) {
            param.setValue(Boolean.TRUE);
        }

        updateComponentStatusIfNeeded(true);
    }

    public Node(IComponent delegateComponent, IComponent emfComponent) {
        this.oldcomponent = emfComponent;
        this.delegateComponent = delegateComponent;
        this.component = emfComponent;
        this.process = ActiveProcessTracker.getCurrentProcess();
        currentStatus = 0;

        init(component);
        initDefaultElementParameters();
        IElementParameter param = getElementParameter(EParameterName.REPOSITORY_ALLOW_AUTO_SWITCH.getName());
        if (param != null) {
            param.setValue(Boolean.TRUE);
        }

        updateComponentStatusIfNeeded(true);
    }

    public Node(IComponent component, IProcess2 process, boolean insertSet, boolean template) {
        this.oldcomponent = component;
        this.delegateComponent = UnifiedComponentUtil.getDelegateComponent(component);
        this.insertSet = insertSet;
        this.template = template;
        this.process = ActiveProcessTracker.getCurrentProcess();
        if (this.process == null) {
            this.process = process;
        }
        currentStatus = 0;

        init(component);
        IElementParameter param = getElementParameter(EParameterName.REPOSITORY_ALLOW_AUTO_SWITCH.getName());
        if (param != null) {
            param.setValue(Boolean.TRUE);
        }
    }

    public Node(IComponent component, IProcess2 process) {
        this.oldcomponent = component;
        this.delegateComponent = UnifiedComponentUtil.getDelegateComponent(component);
        this.process = process;
        init(component);
        IElementParameter param = getElementParameter(EParameterName.REPOSITORY_ALLOW_AUTO_SWITCH.getName());
        if (param != null) {
            param.setValue(Boolean.TRUE);
        }
        needlibrary = false;
    }

    public Node(IComponent component, IProcess2 process, String inOutUniqueName) {
        this.oldcomponent = component;
        this.delegateComponent = UnifiedComponentUtil.getDelegateComponent(component);
        this.inOutUniqueName = inOutUniqueName;
        this.process = process;
        init(component);
        needlibrary = false;
    }

    public Node(INode oldNode, IProcess2 process) {
        this.oldcomponent = oldNode.getComponent();
        this.delegateComponent = UnifiedComponentUtil.getDelegateComponent(oldNode.getComponent());
        this.process = process;
        init(oldNode.getComponent());
        if (component != null && component instanceof AbstractBasicComponent) {
            AbstractBasicComponent comp = (AbstractBasicComponent) component;
            comp.initNodeProperties(this, oldNode);
        }
        needlibrary = false;
    }

    private MetadataTable getNewMetadataTable() {
        // All component types use the same MetadataTable implementation.
        return new MetadataTable();
    }

    private void init(IComponent newComponent) {
        this.component = UnifiedComponentUtil.getEmfComponent(this, newComponent);
        this.label = component.getDisplayName();
        updateComponentStatusIfNeeded(true);
        IPreferenceStore store = DesignerPlugin.getDefault().getPreferenceStore();

        labelToParse = store.getString(TalendDesignerPrefConstants.DEFAULT_LABEL);
        hintToParse = store.getString(TalendDesignerPrefConstants.DEFAULT_HINT);
        connectionToParse = store.getString(TalendDesignerPrefConstants.DEFAULT_CONNECTION_FORMAT);
        showHint = store.getBoolean(TalendDesignerPrefConstants.DEFAULT_HINT_USED);
        if (nodeLabel == null) {
            nodeLabel = new NodeLabel(label, this);
        }

        if (nodeError == null) {
            nodeError = new NodeError(this);
        }

        if (nodeProgressBar == null) {
            nodeProgressBar = new NodeProgressBar(this);
        }

        listConnector = this.component.createConnectors(this);
        metadataList = new ArrayList<IMetadataTable>();

        String uniqueName2 = null;
        IElementParameter unparam = getElementParameter(EParameterName.UNIQUE_NAME.getName());
        if (unparam != null && !"".equals(unparam.getValue())) { //$NON-NLS-1$
            uniqueName2 = (String) unparam.getValue();
        }
        createElementParameters();

        // if (hasMetadata) {
        boolean hasSchemaType = false;
        for (IElementParameter param : getElementParameters()) {
            if (param.getFieldType().equals(EParameterFieldType.SCHEMA_TYPE)
                    || param.getFieldType().equals(EParameterFieldType.DCSCHEMA)
                    || param.getFieldType().equals(EParameterFieldType.SCHEMA_REFERENCE)) {
                IMetadataTable table = getNewMetadataTable();
                table.setAttachedConnector(param.getContext());
                metadataList.add(table);
                hasSchemaType = true;
            }
        }
        boolean hasMetadata = false;

        for (INodeConnector curConnector : getListConnector()) {
            if (curConnector.getDefaultConnectionType().hasConnectionCategory(IConnectionCategory.DATA)) {
                if (!curConnector.isMultiSchema()
                        && (curConnector.getMaxLinkInput() != 0 || curConnector.getMaxLinkOutput() != 0)) {
                    hasMetadata = true;
                    break;
                }
            }
        }
        if (hasMetadata && !hasSchemaType) {
            // add a default metadata on the current component
            String mainConnector;
            if (isELTComponent()) {
                mainConnector = EConnectionType.TABLE.getName();
            } else {
                mainConnector = EConnectionType.FLOW_MAIN.getName();
            }
            IMetadataTable table = getNewMetadataTable();
            table.setAttachedConnector(mainConnector);
            metadataList.add(table);
        }
        // }
        listReturn = this.component.createReturns(this);

        if (!reloadingComponent && (uniqueName2 == null || "".equals(uniqueName2))) { //$NON-NLS-1$
            if (this.inOutUniqueName != null) {
                uniqueName2 = inOutUniqueName;
            } else {
                uniqueName2 = ((Process) getProcess()).generateUniqueNodeName(this);
            }
            ((Process) getProcess()).addUniqueNodeName(uniqueName2);
        }

        setPropertyValue(EParameterName.UNIQUE_NAME.getName(), uniqueName2);
        /*
         * for implements [TESB-10335], need to replace "__NODE_UNIQUE_NAME__" to node unique name in expression.
         */
        for (IElementParameter param : getElementParameters()) {
            if (param.getValue() != null && param.getValue() instanceof String) {
                String value = (String) param.getValue();
                if (value.contains("__NODE_UNIQUE_NAME__")) {
                    value = value.replace("__NODE_UNIQUE_NAME__", uniqueName2); //$NON-NLS-1$
                    param.setValue(value);
                }
            }
        }

        IElementParameter mappingParameter = MetadataToolHelper
                .getMappingParameter((List<IElementParameter>) this.getElementParameters());

        for (IMetadataTable table : metadataList) {
            if (table.getAttachedConnector() != null && (table.getAttachedConnector().equals(EConnectionType.FLOW_MAIN.getName())
                    || table.getAttachedConnector().equals(EConnectionType.TABLE.getName()))) {
                table.setTableName(uniqueName2);
            } else {
                table.setTableName(table.getAttachedConnector());
            }
            if (mappingParameter != null) {
                if (mappingParameter.getValue() != null && (!mappingParameter.getValue().equals(""))) { //$NON-NLS-1$
                    table.setDbms((String) mappingParameter.getValue());
                }
            }

            for (int i = 0; i < getElementParameters().size(); i++) {
                IElementParameter param = getElementParameters().get(i);
                if (param.getFieldType().equals(EParameterFieldType.MAPPING_TYPE)) {
                    table.setDbms((String) param.getValue());
                }
                if ((param.getFieldType().equals(EParameterFieldType.SCHEMA_TYPE)
                        || param.getFieldType().equals(EParameterFieldType.DCSCHEMA)
                        || param.getFieldType().equals(EParameterFieldType.SCHEMA_REFERENCE)) && param.getContext() != null
                        && param.getContext().equals(table.getAttachedConnector())) {
                    if (param.getValue() instanceof IMetadataTable) {
                        IMetadataTable paramTable = (IMetadataTable) param.getValue();
                        table.getListColumns().addAll(paramTable.getListColumns());
                        table.setOriginalColumns(paramTable.getOriginalColumns());
                        table.setReadOnly(paramTable.isReadOnly());
                    } else if (param.getFieldType().equals(EParameterFieldType.SCHEMA_REFERENCE)) {
                        Schema schema = (Schema) componentProperties.getValuedProperty(param.getName()).getValue();
                        org.talend.core.model.metadata.builder.connection.MetadataTable defaultEmfTable = MetadataToolAvroHelper
                                .convertFromAvro(schema);
                        IMetadataTable defaultTable = MetadataToolHelper.convert(defaultEmfTable);
                        IMetadataTable myTable = getMetadataFromConnector(param.getContext());
                        myTable.getListColumns().addAll(defaultTable.getListColumns());
                        myTable.setReadOnly(defaultTable.isReadOnly());
                    }
                }

            }
        }

        for (int i = 0; i < getElementParameters().size(); i++) {
            IElementParameter param = getElementParameters().get(i);
            Object obj = param.getValue();
            if (obj != null) {
                if (param.getName().equals(EParameterName.LABEL.getName())) {
                    labelToParse = (String) obj;
                } else if (param.getName().equals(EParameterName.HINT.getName())) {
                    hintToParse = (String) obj;
                } else if (param.getName().equals(EParameterName.CONNECTION_FORMAT.getName())) {
                    connectionToParse = (String) obj;
                } else if (param.getName().equals(EParameterName.VALIDATION_RULES.getName())) {
                    hasValidationRule = (Boolean) obj;
                }
            }
        }
        if (!reloadingComponent) {
            setPropertyValue(EParameterName.LABEL.getName(), labelToParse);
            setPropertyValue(EParameterName.HINT.getName(), hintToParse);
            setPropertyValue(EParameterName.CONNECTION_FORMAT.getName(), connectionToParse);
            connectionName = ElementParameterParser.parse(this, connectionToParse);
            setPropertyValue(EParameterName.SHOW_HINT.getName(), new Boolean(showHint));
            setPropertyValue(EParameterName.VALIDATION_RULES.getName(), hasValidationRule);
        }
        setHasValidationRule(hasValidationRule);
        if (component.getPluginExtension() != null) {
            externalNode = ExternalNodesFactory.getInstance(component.getPluginExtension());
        }

        if (isExternalNode()) {
            IExternalNode eternalNode = getExternalNode();
            eternalNode.initialize();
            //
            // boolean contains;
            // if (getMetadataList().size() != eternalNode.getMetadataList().size()) {
            // for (IMetadataTable table : eternalNode.getMetadataList()) {
            // contains = false;
            // for (IMetadataTable t : getMetadataList()) {
            // if (table.getTableName().equals(t.getTableName())) {
            // contains = true;
            // }
            // }
            // if (!contains) {
            // getMetadataList().add(table);
            // }
            // }
            // }
        }

        if (!getProcess().isDuplicate() || CommonsPlugin.isHeadless() || this.process.getEditor() == null) { // only for
                                                                                                             // graphical
                                                                                                             // process
            // note that the subtree start is calculated just after load the process
            // so it's no use when load without editor
            calculateSubtreeStartAndEnd();
        }
        updateComponentStatusIfNeeded(false);
    }

    private void createElementParameters() {
        List<IElementParameter> createElementParameters = new ArrayList<IElementParameter>();
        if (UnifiedComponentUtil.isDelegateComponent(delegateComponent)) {
            UnifiedComponentUtil.createParameters(this, createElementParameters, delegateComponent, component);
        }
        if (!UnifiedComponentUtil.isDelegateComponent(component)) {
            createElementParameters.addAll(component.createElementParameters(this));
        }
        setElementParameters(createElementParameters);
    }

    @Override
    public IProcess getProcess() {
        return process;
    }

    private void updateComponentStatusIfNeeded(boolean isInitializing) {
        if (component instanceof AbstractBasicComponent) {
            ((AbstractBasicComponent) component).setInitializing(isInitializing);
        }
    }

    public void initDefaultElementParameters() {
        boolean enable = RepositoryPlugin.getDefault().getPreferenceStore()
                .getBoolean(AutoConvertTypesUtils.ENABLE_AUTO_CONVERSION);
        IElementParameter convertElemParam = getElementParameter(AutoConvertTypesUtils.ENABLE_AUTO_CONVERT_TYPE);
        if (enable && convertElemParam != null) {
            convertElemParam.setValue(enable);
        }
    }

    /**
     *
     * Note that if there is several connectors of the same type, it will return the first one.
     *
     * @param connType
     * @return
     */
    @Override
    public INodeConnector getConnectorFromType(final EConnectionType connType) {
        INodeConnector nodeConnector = null;
        List<INodeConnector> listConnectors = new ArrayList<INodeConnector>();
        int nbConn = 0;

        EConnectionType testedType;

        if (connType.hasConnectionCategory(IConnectionCategory.FLOW)) {
            testedType = EConnectionType.FLOW_MAIN;
        } else {
            testedType = connType;
        }

        while ((nodeConnector == null) && (nbConn < listConnector.size())) {
            INodeConnector connector = listConnector.get(nbConn);
            if (connector.getDefaultConnectionType() == testedType) {
                nodeConnector = listConnector.get(nbConn);
                listConnectors.add(nodeConnector);
            }
            nbConn++;
        }
        return nodeConnector;
    }

    /**
     *
     * Note that if there is several connectors the same name, it will return the first one.
     *
     * @param connName
     * @return
     */
    @Override
    public INodeConnector getConnectorFromName(final String connName) {
        for (INodeConnector nodeConnector : listConnector) {
            if (connName.equals(nodeConnector.getName())) {
                return nodeConnector;
            }
        }
        return null;
    }

    @Override
    public List<INodeConnector> getConnectorsFromType(final EConnectionType connType) {
        INodeConnector nodeConnector = null;
        List<INodeConnector> listConnectors = new ArrayList<INodeConnector>();
        int nbConn = 0;

        EConnectionType testedType;

        if (connType.hasConnectionCategory(IConnectionCategory.FLOW)) {
            testedType = EConnectionType.FLOW_MAIN;
        } else {
            testedType = connType;
        }

        while (nbConn < listConnector.size()) {
            INodeConnector connector = listConnector.get(nbConn);
            if (connector.getDefaultConnectionType() == testedType) {
                nodeConnector = listConnector.get(nbConn);
                listConnectors.add(nodeConnector);
            }
            nbConn++;
        }

        return listConnectors;
    }

    public void setShowHint(final Boolean showHint) {
        this.showHint = showHint;
        firePropertyChange(EParameterName.HINT.getName(), null, null);

        IElementParameter param = getElementParameter(EParameterName.SHOW_HINT.getName());
        param.setValue(new Boolean(showHint));
    }

    public boolean isSetShowHint() {
        return showHint;
    }

    public void setShowHintText(final String showHintText) {
        this.showHintText = showHintText;

        firePropertyChange(EParameterName.HINT.getName(), null, null);
    }

    public String getShowHintText() {
        return showHintText;
    }

    /**
     * Get the ImageDescriptor of the component which is taken from the xml.
     *
     * @return ImageDescriptor
     */
    public ImageDescriptor getIcon32() {
        return delegateComponent.getIcon32();
    }

    public ImageDescriptor getIcon24() {
        return delegateComponent.getIcon24();
    }

    /**
     * Gives the unique name of the node.
     *
     * @return unique name
     */
    @Override
    public String getUniqueName() {
        String uniqueName = null;
        IElementParameter param = getElementParameter(EParameterName.UNIQUE_NAME.getName());
        if (param != null) {
            uniqueName = (String) param.getValue();
        }
        return uniqueName;
    }

    // can only be set with the properties
    private void setUniqueName(String uniqueName) {
        ((Process) getProcess()).removeUniqueNodeName(getUniqueName());
        ((Process) getProcess()).addUniqueNodeName(uniqueName);
    }

    @Override
    public List<? extends INodeReturn> getReturns() {
        List<INodeReturn> allReturns = new ArrayList<INodeReturn>();

        if (this.component.getName().equals("tSetGlobalVar")) { //$NON-NLS-1$
            getSetGlobalVarReturns(allReturns);
        }
        if (this.component.getName().equals("tFlowToIterate")) { //$NON-NLS-1$
            getFlowToIterateReturns(allReturns);
        }

        for (INodeReturn returnNode : listReturn) {
            if (returnNode.isShow(this.getElementParameters())) {
                allReturns.add(returnNode);
            }
        }

        return allReturns;
    }

    // gcui:if in the job have tSetGlobalVar component.This method will set it variables to be global variables.
    private void getSetGlobalVarReturns(List<INodeReturn> allReturns) {

        IElementParameter varParam = this.getElementParameter(EParameterName.VARIABLES.getName());
        List<Map<String, String>> globalNode = (List<Map<String, String>>) varParam.getValue();
        String[] codeName = varParam.getListItemsDisplayCodeName();
        if (globalNode != null && codeName != null && codeName.length > 1) {
            for (Map<String, String> line : globalNode) {

                INodeReturn globalVarReturn = new NodeReturn() {

                    @Override
                    public String getVarName() {
                        String varName = super.getVarName();
                        switch (LanguageManager.getCurrentLanguage()) {
                        case PERL:
                            varName = varName.replace(UNIQUE_NAME, ""); //$NON-NLS-1$
                            break;
                        case JAVA:
                            varName = varName.replace(UNIQUE_NAME + "_", ""); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                        return varName;
                    }

                };
                final String key = line.get(codeName[0]);
                final String value = line.get(codeName[1]);

                String varName = TalendTextUtils.removeQuotes(key);
                globalVarReturn.setName(varName);
                globalVarReturn.setVarName(varName);
                globalVarReturn.setType(JavaTypesManager.STRING.getId());
                globalVarReturn.setDisplayName(value);
                globalVarReturn.setAvailability("AFTER"); //$NON-NLS-1$

                allReturns.add(globalVarReturn);
            }
        }
    }

    // gcui:if in the job have tFlowToIterate component.This method will set it variables to be global variables.
    private void getFlowToIterateReturns(List<INodeReturn> allReturns) {

        List<? extends IConnection> inMainConns = this.getIncomingConnections();
        String inputRowName = null;
        IConnection inMainConn = null;
        if (inMainConns != null && !inMainConns.isEmpty()) {
            inMainConn = inMainConns.get(0);
            inputRowName = inMainConn.getName();
            boolean useDefault = ElementParameterParser.getValue(this, "__DEFAULT_MAP__").equals("true"); //$NON-NLS-1$ //$NON-NLS-2$
            final String showInputRowName = inputRowName;
            if (useDefault) {
                IMetadataTable metadata = inMainConn.getMetadataTable();
                // The if statement is added by Marvin Wang on May 7, 2013 for bug TDI-25659.
                if (metadata != null) {
                    List<IMetadataColumn> listColumns = metadata.getListColumns();

                    for (IMetadataColumn column : listColumns) {

                        INodeReturn flowToIterateReturn = new NodeReturn() {

                            @Override
                            public String getVarName() {
                                String varName = super.getVarName();
                                switch (LanguageManager.getCurrentLanguage()) {
                                case PERL:
                                    varName = varName.replace(UNIQUE_NAME, showInputRowName + "."); //$NON-NLS-1$
                                    break;
                                case JAVA:
                                    varName = varName.replace(UNIQUE_NAME + "_", showInputRowName + "."); //$NON-NLS-1$ //$NON-NLS-2$
                                }
                                return varName;
                            }

                        };
                        String columnLabel = column.getLabel();
                        String columnType = column.getTalendType();
                        flowToIterateReturn.setName(columnLabel);
                        flowToIterateReturn.setDisplayName(columnLabel);
                        flowToIterateReturn.setType(columnType);
                        flowToIterateReturn.setVarName(columnLabel);
                        flowToIterateReturn.setAvailability("AFTER"); //$NON-NLS-1$

                        allReturns.add(flowToIterateReturn);
                    }
                }
            } else {
                Object obj = ElementParameterParser.getObjectValue(this, "__MAP__"); //$NON-NLS-1$
                if (obj != null) {
                    List<Map<String, String>> map = (List<Map<String, String>>) obj;
                    IMetadataTable metadata = inMainConn.getMetadataTable();
                    // The if statement is added by Marvin Wang on May 7, 2013 for bug TDI-25659.
                    if (map != null && metadata != null) {
                        List<IMetadataColumn> listColumns = metadata.getListColumns();

                        for (Map<String, String> line : map) {
                            String keyName = TalendTextUtils.removeQuotes(line.get("KEY")); //$NON-NLS-1$

                            INodeReturn flowToIterateReturn = new NodeReturn() {

                                @Override
                                public String getVarName() {
                                    String varName = super.getVarName();
                                    switch (LanguageManager.getCurrentLanguage()) {
                                    case PERL:
                                        varName = varName.replace(UNIQUE_NAME, ""); //$NON-NLS-1$
                                        break;
                                    case JAVA:
                                        varName = varName.replace(UNIQUE_NAME + "_", ""); //$NON-NLS-1$ //$NON-NLS-2$
                                    }
                                    return varName;
                                }
                            };

                            String cueeName = line.get("VALUE"); //$NON-NLS-1$
                            for (IMetadataColumn listColumn : listColumns) {
                                String columnName = listColumn.getLabel();
                                if (columnName.equals(cueeName)) {
                                    String columnType = listColumn.getTalendType();
                                    flowToIterateReturn.setType(columnType);
                                }

                            }

                            flowToIterateReturn.setName(keyName);
                            flowToIterateReturn.setDisplayName(cueeName);
                            flowToIterateReturn.setVarName(keyName);
                            flowToIterateReturn.setAvailability("AFTER"); //$NON-NLS-1$

                            allReturns.add(flowToIterateReturn);

                        }
                    }
                }
            }
        }

    }

    /**
     * Set this node as the start of the diagram.
     *
     * @param start boolean that will give the status
     */
    @Override
    public void setStart(final boolean start) {
        IElementParameter param = getElementParameter(EParameterName.START.getName());
        if (param == null) {
            return;
        }
        param.setValue(new Boolean(start));
        this.start = start;
        firePropertyChange(EParameterName.START.getName(), null, null);

    }

    /**
     * Return the start status of this node.
     *
     * @return
     */
    @Override
    public boolean isStart() {
        return start;
    }

    /**
     * Set the location of the node.
     *
     * @param location Point
     */
    @Override
    public void setLocation(final Point location) {
        if (location == null) {
            return;
        }
        float tempX = (float) location.x / (float) DEFAULT_SIZE;
        float tempY = (float) location.y / (float) DEFAULT_SIZE;
        location.x = (int) (Math.rint(tempX) * DEFAULT_SIZE);
        location.y = (int) (Math.rint(tempY) * DEFAULT_SIZE);
        // TDI-22649
        if (this.location.equals(location)) {
            return;
        }
        this.location = location;
        nodeLabel.setLocation(location);
        nodeError.setLocation(location);
        nodeProgressBar.setLocation(location);
        firePropertyChange(LOCATION, null, location);
    }

    /**
     * Gives the location of the node.
     *
     * @return Point
     */
    @Override
    public Point getLocation() {
        return location;
    }

    /**
     * Set the label of a node. <br/>
     * <b> /!\ This is the text of the label, not the name of the component</b>
     *
     * @param titleName
     */
    @Override
    public void setLabel(final String label) {
        this.label = label;
        if (nodeLabel.getLabelText() != label) {
            nodeLabel.setLabelText(label);
        }
        firePropertyChange(MODIFY_NODELABEL, null, null);
    }

    private Map<String, IElementParameter> createVariableMap(List<? extends IElementParameter> currentParams,
            List<? extends IElementParameter> connParams) {
        Map<String, IElementParameter> map = new HashMap<String, IElementParameter>();
        for (IElementParameter param : currentParams) {
            map.put(param.getVariableName(), param);
        }

        // overwrite param by existing connection
        for (IElementParameter param : connParams) {
            if (param.getCategory().equals(EComponentCategory.BASIC)) {
                map.put(param.getVariableName(), param);
            }
        }
        return map;
    }

    /**
     * DOC hcw Comment method "updateVisibleDataForExistingConnection".
     *
     * @param connNode
     */
    private void updateVisibleDataForExistingConnection(Node connNode) {
        IElementParameter useConn = this.getElementParameter("USE_EXISTING_CONNECTION"); //$NON-NLS-1$
        IElementParameter connParam = null;
        if (useConn != null) {
            connParam = this.getElementParameter("CONNECTION"); //$NON-NLS-1$
        }
        if (useConn != null && connParam != null && Boolean.TRUE.equals(useConn.getValue())) {
            String connName = (String) connParam.getValue();
            if (connNode.getUniqueName().equals(connName)) {
                String newLabel = label;
                String newShowHintText = showHintText;
                String newConnectionName = connectionName;
                Map<String, IElementParameter> variableMap = createVariableMap(this.getElementParameters(),
                        connNode.getElementParameters());
                newLabel = ElementParameterParser.replaceWithExistingConnection(labelToParse, variableMap);
                newShowHintText = ElementParameterParser.replaceWithExistingConnection(hintToParse, variableMap);
                newConnectionName = ElementParameterParser.replaceWithExistingConnection(connectionToParse, variableMap);
                if (!newLabel.equals(label)) {
                    setLabel(newLabel);
                }

                if (!newShowHintText.equals(showHintText)) {
                    setShowHintText(newShowHintText);
                }

                if (!newConnectionName.equals(connectionName)) {
                    setConnectionName(newConnectionName);
                }
            }
        }
    }

    public void updateVisibleData() {
        // if it's a duplicate process, it's a process used only for code generation, so no need to update any graphical
        // data.
        if (this.process.isDuplicate() || CommonsPlugin.isHeadless() || this.process.getEditor() == null) {
            return;
        }
        String newLabel = label;
        String newShowHintText = showHintText;
        String newConnectionName = connectionName;
        // label may be replaced with variable from exiting connection. see 0005456: Label Format __DBNAME__ not valid
        // when using existing connection
        boolean useConnection = false;
        IElementParameter useConn = this.getElementParameter("USE_EXISTING_CONNECTION"); //$NON-NLS-1$
        IElementParameter connParam = null;
        if (useConn != null) {
            connParam = this.getElementParameter("CONNECTION"); //$NON-NLS-1$
        }
        
        boolean isGeneric = this.getComponent().getComponentType() == EComponentType.GENERIC;
        if(isGeneric && labelToParse != null) {
        	labelToParse = labelToParse.replaceAll("__TABLE__", "__tableSelection.tablename__"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        
        if (useConn != null && connParam != null && Boolean.TRUE.equals(useConn.getValue())) {

            String connName = (String) connParam.getValue();
            INode connNode = null;
            List<? extends INode> nodeList = this.getProcess().getGraphicalNodes();
            for (INode node : nodeList) {
                if (node.getUniqueName().equals(connName)) {
                    connNode = node;
                    break;
                }
            }

            if (connNode != null) {

                Map<String, IElementParameter> variableMap = createVariableMap(this.getElementParameters(),
                        connNode.getElementParameters());
                newLabel = ElementParameterParser.replaceWithExistingConnection(labelToParse, variableMap);
                newShowHintText = ElementParameterParser.replaceWithExistingConnection(hintToParse, variableMap);
                newConnectionName = ElementParameterParser.replaceWithExistingConnection(connectionToParse, variableMap);
                useConnection = true;
            }
        }

        if (useConnection == false) {
            // if it does not use existing connection, do as before
            newLabel = ElementParameterParser.parse(this, labelToParse);
            newShowHintText = ElementParameterParser.parse(this, hintToParse);
            newConnectionName = ElementParameterParser.parse(this, connectionToParse);

            // this node may be connection node used by other nodes
            if (useConn == null && connParam == null) {
                List<? extends INode> nodeList = this.getProcess().getGraphicalNodes();
                for (INode node : nodeList) {
                    if (node == this || !(node instanceof Node)) {
                        continue;
                    }
                    ((Node) node).updateVisibleDataForExistingConnection(this);
                }
            }
        }

        if (!newLabel.equals(label)) {
            setLabel(newLabel);
        }

        if (!newShowHintText.equals(showHintText)) {
            setShowHintText(newShowHintText);
        }

        if (!newConnectionName.equals(connectionName)) {
            setConnectionName(newConnectionName);
        }
    }

    /**
     * Gives the label of the node.
     *
     * @return
     */
    @Override
    public String getLabel() {
        return label;
    }

    /**
     * Gives the object of the model part for the label.
     *
     * @return
     */
    public NodeLabel getNodeLabel() {
        return nodeLabel;
    }

    public NodeError getNodeError() {
        return nodeError;
    }

    public NodeProgressBar getNodeProgressBar() {
        return this.nodeProgressBar;
    }

    /**
     * Add a new connection input to the node.
     *
     * @param connection
     */
    @Override
    public void addInput(final IConnection conn) {
        this.inputs.add(conn);
        fireStructureChange(INPUTS, conn);

        if (conn instanceof Connection) {
            Connection connection = (Connection) conn;

            if (!ConnectionCreateCommand.isCreatingConnection()) {
                calculateSubtreeStartAndEnd();
                return;
            }

            INodeConnector mainConnector;
            boolean isFlowMain = false;
            if (isELTComponent()) {
                mainConnector = this.getConnectorFromType(EConnectionType.TABLE);
            } else {
                isFlowMain = true;
                mainConnector = this.getConnectorFromType(EConnectionType.FLOW_MAIN);
            }

            if (insertSet) {
                return;
            }
            if (!mainConnector.isMultiSchema()
                    && (connection.getLineStyle() == EConnectionType.FLOW_MAIN
                            || (connection.getLineStyle() == EConnectionType.TABLE)
                            || (connection.getLineStyle() == EConnectionType.FLOW_MERGE))
                    && ((Process) getProcess()).isActivate()) {

                boolean repositoryMode = false;
                IMetadataTable mainTargetTable = this.getMetadataFromConnector(mainConnector.getName());
                for (IElementParameter param : getElementParameters()) {
                    if ((EParameterFieldType.SCHEMA_TYPE.equals(param.getFieldType())
                            || EParameterFieldType.SCHEMA_REFERENCE.equals(param.getFieldType()))
                            && (param.getContext().equals(mainConnector.getName()))) {
                        IElementParameter schemaTypeParam = param.getChildParameters().get(EParameterName.SCHEMA_TYPE.getName());
                        if (schemaTypeParam.getValue().equals(EmfComponent.REPOSITORY)) {
                            repositoryMode = true;
                            break;
                        }
                    }
                }
                String inputConnector = null;
                IMetadataTable inputTable = connection.getMetadataTable();
                // if current table has custom columns, input parameter used will be from currrent connector
                // if no custom columns, it will use main connector, so it will allow to propagate repository
                // information.
                if (MetadataToolHelper.hasCustomColumns(inputTable)) {
                    inputConnector = conn.getConnectorName();
                } else {
                    inputConnector = mainConnector.getName();
                }
                IElementParameter inputSchemaParam = null;

                for (IElementParameter param : conn.getSource().getElementParameters()) {
                    if ((EParameterFieldType.SCHEMA_TYPE.equals(param.getFieldType())
                            || EParameterFieldType.SCHEMA_REFERENCE.equals(param.getFieldType()))
                            && (param.getContext().equals(inputConnector))) {
                        inputSchemaParam = param;
                        break;
                    }
                }
                // initializeGenericSchemaFromInput(inputTable);

                if (!generatedByJobscriptBool && component.isSchemaAutoPropagated() && !repositoryMode
                        && (inputTable != null && inputTable.getListColumns().size() != 0)) {

                    // if the selected connector's schema type is in repository
                    // mode or read only, then don't propagate.
                    for (INodeConnector connector : getListConnector()) {
                        if (mainConnector.getName().equals(connector.getBaseSchema())
                                && (isFlowMain ? connector.getMaxLinkInput() > 0 : true)) {
                            /**
                             * For FLOW(not include TABLE), I think, only for the input metatable is enough,
                             * because:<br>
                             * 1. The following called ChangeMetadataCommand are always seem everytime<br>
                             * 2. If the input table is not changed, maybe output table should not change too, because
                             * output data is come from input data<br>
                             * 3. The called ChangeMetadataCommand will change all the output tables every time for
                             * FLOW, and seems will not change the output tables for TABLE if the connection is TABLE
                             * type; so for TABLE type just keep like before, maybe need review.<br>
                             * 4. While column datas in output tables are more than datas in input tables, if call multy
                             * times for output table, maybe will make all the column datas in output table same with
                             * the columns datas of the output table which is called last time.
                             */

                            IMetadataTable targetTable = this.getMetadataFromConnector(connector.getName());
                            boolean tmpTableCreated = false;
                            if (targetTable == null) {
                                if (connector.getMaxLinkInput() > 0) {
                                    // create fake table only for the propagation
                                    targetTable = new MetadataTable();
                                    targetTable.setAttachedConnector("FLOW");
                                    targetTable.setTableName(this.getUniqueName());
                                    tmpTableCreated = true;
                                } else {
                                    continue;
                                }
                            }
                            boolean customFound = false;
                            int customColNumber = 0;
                            for (IMetadataColumn column : targetTable.getListColumns()) {
                                if (column.isCustom()) {
                                    customColNumber++;
                                }
                            }
                            customFound = customColNumber > 0;
                            int nonCustomColNumber = targetTable.getListColumns().size() - customColNumber;
                            if (nonCustomColNumber == 0 && (((customFound && targetTable.isReadOnly()) || (outputs.size() == 0)
                                    || (connection.getLineStyle() == EConnectionType.FLOW_MERGE))
                                    && (inputTable.getListColumns().size() != 0))) {
                                // For the auto propagate.
                                // MetadataTool.copyTable(inputTable, targetTable);
                                // add by wzhang for feature 7611.
                                String dbmsId = targetTable.getDbms();
                                MetadataToolHelper.copyTable(inputTable, targetTable, null, false);
                                MetadataToolHelper.setDBType(targetTable, dbmsId);
                                ChangeMetadataCommand cmc = new ChangeMetadataCommand(this, null,
                                        tmpTableCreated ? targetTable : null, targetTable, inputSchemaParam);
                                cmc.execute();

                                ColumnListController.updateColumnList(this, null, true);
                            } else if (connection.getTarget().getComponent().getName().equals("tFilterRow")) { //$NON-NLS-1$
                                String dbmsId = targetTable.getDbms();
                                MetadataToolHelper.copyTable(dbmsId, inputTable, targetTable);
                                ChangeMetadataCommand cmc = new ChangeMetadataCommand(this, null, null, targetTable,
                                        inputSchemaParam);
                                cmc.execute();
                            }
                        }
                    }
                } else {
                    if (mainTargetTable == null) {
                        return;
                    }
                    boolean haveNonCustomColumn = false;
                    for (IMetadataColumn column : mainTargetTable.getListColumns()) {
                        if (!column.isCustom()) {
                            haveNonCustomColumn = true;
                            break;
                        }
                    }
                    if ((!haveNonCustomColumn) || mainTargetTable.sameMetadataAs(connection.getMetadataTable(),
                            IMetadataColumn.OPTIONS_IGNORE_KEY | IMetadataColumn.OPTIONS_IGNORE_NULLABLE
                                    | IMetadataColumn.OPTIONS_IGNORE_COMMENT | IMetadataColumn.OPTIONS_IGNORE_PATTERN
                                    | IMetadataColumn.OPTIONS_IGNORE_DBCOLUMNNAME | IMetadataColumn.OPTIONS_IGNORE_DBTYPE
                                    | IMetadataColumn.OPTIONS_IGNORE_DEFAULT | IMetadataColumn.OPTIONS_IGNORE_BIGGER_SIZE)) {
                        return;
                    }
                    IConnection outputConnection = null;
                    // schema not auto-propagated or in repository mode or job generated by jobscript file
                    if ((connection.getSource().getSchemaParameterFromConnector(mainConnector.getName()) != null)) {

                        if (connection.getSource().getOutgoingConnections(connection.getConnectorName()).size() == 1) {
                            outputConnection = connection.getSource().getOutgoingConnections(connection.getConnectorName())
                                    .get(0);
                        }
                        if (template || isJunitCreate) {
                            if (takeSchema == null) {
                                takeSchema = true;
                            }
                        } else {
                            if (takeSchema == null) {
                                takeSchema = getTakeSchema();
                            }
                        }
                        boolean isJunitInput = false;
                        if (GlobalServiceRegister.getDefault().isServiceRegistered(ITestContainerProviderService.class)) {
                            ITestContainerProviderService testContainerService = GlobalServiceRegister
                                    .getDefault().getService(ITestContainerProviderService.class);
                            if (testContainerService != null
                                    && testContainerService.isTestCaseComponent(connection.getSource().getComponent())) {
                                if (connection.getSource().getComponent().getInputType() != null) {
                                    isJunitInput = connection.getSource().getComponent().getInputType().equals("Input");//$NON-NLS-1$
                                }
                            }
                        }
                        if (takeSchema && !isJunitInput) {
                            ((Node) connection.getSource()).takeSchemaFrom(this, mainConnector.getName());
                        }
                    } else if (connection.getSourceNodeConnector().isMultiSchema()) {
                        if (template || isJunitCreate) {
                            if (takeSchema == null) {
                                takeSchema = true;
                            }
                        } else {
                            if (takeSchema == null) {
                                takeSchema = getTakeSchema();
                            }
                        }
                        if (takeSchema.booleanValue()) {
                            String connectionLabel = connection.getMetadataTable().getLabel();
                            MetadataToolHelper.copyTable(mainTargetTable, connection.getMetadataTable());
                            if (connection.getSource().isELTComponent()) {
                                connection.getMetadataTable().setLabel(connectionLabel);
                            }
                            if (connection.getTarget().isELTComponent()) {
                                IElementParameter elemParam = connection.getTarget().getElementParameter("ELT_TABLE_NAME"); //$NON-NLS-1$
                                if (elemParam != null && elemParam.getFieldType().equals(EParameterFieldType.TEXT)) {
                                    String removeQuotes = TalendTextUtils.removeQuotes(elemParam.getValue().toString());
                                    if (!removeQuotes.equals("") && "Default".equals(connection.getName())) { //$NON-NLS-1$ //$NON-NLS-2$
                                        connection.setName(removeQuotes);
                                    }
                                }
                            }
                        } else { // add for feature TDI-17358
                            IMetadataTable sourceTable = connection.getMetadataTable();
                            if (sourceTable != null) {
                                MetadataDialog dialog = new MetadataDialog(DisplayUtils.getDefaultShell(false),
                                        sourceTable.clone(), connection.getSource(), null);
                                dialog.setInputReadOnly(false);
                                dialog.setOutputReadOnly(false);
                                if (dialog.open() == MetadataDialog.OK) {
                                    final IMetadataTable oldTable = sourceTable;
                                    final IMetadataTable newTable = dialog.getOutputMetaData();
                                    if (!oldTable.sameMetadataAs(newTable, IMetadataColumn.OPTIONS_NONE)) {
                                        CommandStack cmdStack = getCommandStack();
                                        if (cmdStack != null) {
                                            cmdStack.execute(new Command() {

                                                @Override
                                                public void execute() {
                                                    oldTable.setListColumns(newTable.getListColumns());
                                                }

                                            });
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        ((Node) connection.getSource()).checkAndRefreshNode();
                        checkAndRefreshNode();
                    }
                }
            }
            calculateSubtreeStartAndEnd();
        }
    }

    private boolean getTakeSchema() {
        return MessageDialog.openQuestion(DisplayUtils.getDefaultShell(false), "", Messages.getString("Node.getSchemaOrNot")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public IElementParameter getSchemaParameterFromConnector(String connector) {
        for (IElementParameter param : getElementParameters()) {
            if ((param.getFieldType().equals(EParameterFieldType.SCHEMA_TYPE)
                    || param.getFieldType().equals(EParameterFieldType.SCHEMA_REFERENCE))
                    && param.getContext().equals(connector)) {
                return param;
            }
        }
        return null;
    }

    public void takeSchemaFrom(Node nodeTarget, String connector) {
        IElementParameter paramTarget = nodeTarget.getSchemaParameterFromConnector(connector);
        IMetadataTable tableTarget = nodeTarget.getMetadataFromConnector(connector);

        IElementParameter schemaParamTarget = paramTarget.getChildParameters().get(EParameterName.SCHEMA_TYPE.getName());

        INodeConnector mainConnector = this.getConnectorFromType(EConnectionType.FLOW_MAIN);
        INodeConnector outputConnector = mainConnector;
        if (mainConnector.getMaxLinkOutput() == 0) {
            // check each connector
            // if any of the connector is not based on FLOW_MAIN
            // if this connector has any MaxLinkOutput > 0 => use this connector instead of main
            for (INodeConnector currentConnector : this.getListConnector()) {
                if (!currentConnector.getBaseSchema().equals(EConnectionType.FLOW_MAIN.getName())
                        && currentConnector.getMaxLinkOutput() > 0) {
                    outputConnector = currentConnector;
                    break;
                }
            }
        }

        IElementParameter param = getSchemaParameterFromConnector(outputConnector.getName());

        ChangeMetadataCommand cmc = new ChangeMetadataCommand(this, param, null, tableTarget);
        CommandStack cmdStack = getCommandStack();
        if (cmdStack != null) {
            cmdStack.execute(cmc);
        }

        if (schemaParamTarget.getValue().equals(EmfComponent.REPOSITORY)) {
            IElementParameter repositorySchemaParamTarget = paramTarget.getChildParameters()
                    .get(EParameterName.REPOSITORY_SCHEMA_TYPE.getName());
            /*
             * param.getChildParameters() .get(EParameterName.SCHEMA_TYPE.getName()).setValue( EmfComponent.REPOSITORY);
             */
            if (param != null) {
                param.getChildParameters().get(EParameterName.REPOSITORY_SCHEMA_TYPE.getName())
                        .setValue(repositorySchemaParamTarget.getValue());
                this.setPropertyValue(EParameterName.SCHEMA_TYPE.getName(), EmfComponent.REPOSITORY);
            }
        }
    }

    private CommandStack getCommandStack() {
        CommandStack cmdStack = null;
        if (template && process instanceof Process) {
            cmdStack = ((Process) process).getCommandStack();
        } else {
            if (process.getEditor() != null) {
                AbstractTalendEditor talendEditor = ((AbstractMultiPageTalendEditor) process.getEditor()).getTalendEditor();
                cmdStack = (CommandStack) talendEditor.getAdapter(CommandStack.class);
            }
        }

        return cmdStack;
    }

    /**
     * Add a new connection output to the node.
     *
     * @param connection
     */
    @Override
    public void addOutput(final IConnection conn) {
        // add the connection on the position of the order before delete the connection
        if (conn instanceof Connection) {
            int order = ((Connection) conn).getOrder();
            if (order != -1) {
                if (order > outputs.size()) {
                    outputs.add(conn);
                } else {
                    outputs.add(order, conn);
                }
                calculateSubtreeStartAndEnd();
                fireStructureChange(OUTPUTS, conn);
                return;
            }
        }
        List<IConnection> listNm = (List<IConnection>) conn.getSource().getOutgoingConnections(conn.getLineStyle());
        int deactiveNum = 0;
        for (IConnection output : outputs) {
            if (!output.isActivate()) {
                deactiveNum = deactiveNum + 1;
            }
        }
        if (outputs != null && outputs.size() > 0) {
            this.outputs.add(outputs.size() - deactiveNum, conn);
        } else {
            this.outputs.add(conn);
        }

        // achen add to fix 6601 add schema when connection
        Node target = (Node) conn.getTarget();
        if (isBasedOnInputSchemas(target)) {
            IElementParameter elementParameter = target.getElementParameter(EParameterName.SCHEMAS.getName());
            List<Map<String, String>> vlist = (List<Map<String, String>>) elementParameter.getValue();

            IMetadataTable table = conn.getMetadataTable();
            if (table != null) {
                table.setTableName(conn.getName());
                String label = table.getTableName();
                IMetadataTable metadataTable = MetadataToolHelper.getMetadataTableFromNode(target, label);
                if (metadataTable == null) {
                    table.setLabel(label);
                    target.metadataList.add(table);
                    Map<String, String> map = new HashMap<String, String>();
                    map.put(IEbcdicConstant.FIELD_SCHEMA, label);
                    // map.put(EParameterName.CONNECTION.getName(), conn.getName());
                    vlist.add(map);
                }
            }
        }
        calculateSubtreeStartAndEnd();
        fireStructureChange(OUTPUTS, conn);
    }

    public boolean isBasedOnInputSchemas(Node target) {
        Object isBaseonInputSchema = ElementParameter2ParameterType.getParameterValue(target,
                EParameterName.BASED_ON_INPUT_SCHEMAS.getName());
        if (isBaseonInputSchema != null) {
            // if base on input schema,add node's schema to target's metatlist.
            if (Boolean.valueOf(isBaseonInputSchema.toString())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gives all incoming connections (only).
     *
     * @return List of Connection
     */
    @Override
    public List<? extends IConnection> getIncomingConnections() {
        List<IConnection> jobletInputs = new ArrayList<IConnection>();
        if (this.isJoblet() && this.getNodeContainer() != null && !this.getNodeContainer().isCollapsed()) {
            jobletInputs.addAll(this.getNodeContainer().getInputs());
        } else {
            return this.inputs;
        }
        return jobletInputs;
    }

    public List<? extends IConnection> getInputs() {
        return this.inputs;
    }

    public List<? extends IConnection> getOutputs() {
        return this.outputs;
    }

    @Override
    public void setIncomingConnections(List<? extends IConnection> connections) {
        this.inputs.clear();
        this.inputs.addAll(connections);
    }

    @Override
    public void setOutgoingConnections(List<? extends IConnection> connections) {
        this.outputs.clear();
        this.outputs.addAll(connections);
    }

    /**
     * Gives all outgoing connections (only).
     *
     * @return List of Connection
     */
    @Override
    public List<? extends IConnection> getOutgoingConnections() {

        if (this.isJoblet() && this.getNodeContainer() != null && !this.getNodeContainer().isCollapsed()) {
            List<IConnection> jobletOutputs = new ArrayList<IConnection>();
            jobletOutputs.addAll(this.getNodeContainer().getOutputs());
            return jobletOutputs;
        }
        return this.outputs;

    }

    /**
     * Remove a connection input.
     *
     * @param connection
     */
    @Override
    public void removeInput(final IConnection connection) {
        this.inputs.remove(connection);
        INodeConnector mainConnector;
        if (isELTComponent()) {
            mainConnector = this.getConnectorFromType(EConnectionType.TABLE);
        } else {
            mainConnector = this.getConnectorFromType(EConnectionType.FLOW_MAIN);
        }

        if (!mainConnector.isMultiSchema() && component.isSchemaAutoPropagated()
                && (connection.getLineStyle() == EConnectionType.FLOW_MAIN)) {

            for (INodeConnector connector : getListConnector()) {
                if (mainConnector.getName().equals(connector.getBaseSchema())) {
                    IElementParameter schemaParam = getSchemaParameterFromConnector(connector.getName());
                    IMetadataTable originTable = getMetadataFromConnector(connector.getName());
                    if ((schemaParam == null || !schemaParam.isReadOnly()) && originTable != null && originTable.isReadOnly()) {
                        List<IMetadataColumn> columnToSave = new ArrayList<IMetadataColumn>();
                        for (IMetadataColumn column : originTable.getListColumns()) {
                            // if (column.isCustom()) {
                            columnToSave.add(column);
                            // }
                        }
                        // statement cause added for major 2635.
                        if (!originTable.getTableName().equals("REJECT")) { //$NON-NLS-1$
                            originTable.getListColumns().clear();
                            originTable.getListColumns().addAll(columnToSave);
                        }
                        originTable.sortCustomColumns();
                    }
                }
            }
        }
        removeTargetMetaData(connection);
        calculateSubtreeStartAndEnd();
        fireStructureChange(INPUTS, connection);
    }

    /**
     *
     * only invoked when target node basedOnInputSchemas.
     *
     * @param connection
     */
    private void removeTargetMetaData(IConnection connection) {
        // achen add to fix 6601 remove the schema when delete the connection
        Node target = (Node) connection.getTarget();
        if (isBasedOnInputSchemas(target)) {
            IElementParameter elementParameter = target.getElementParameter(EParameterName.SCHEMAS.getName());
            List<Map<String, String>> vlist = (List<Map<String, String>>) elementParameter.getValue();

            IMetadataTable table = connection.getMetadataTable();
            if (table != null) { // hywang add for bug 0009593
                String label = table.getTableName();
                IMetadataTable metadataTable = MetadataToolHelper.getMetadataTableFromNode(target, label);
                if (metadataTable != null) {
                    target.metadataList.remove(metadataTable);
                    int pos = getIndex(vlist, label);
                    if (pos != -1) {
                        vlist.remove(pos);
                    }
                }
            }
        }
    }

    /**
     * Force connection draw update. This is only used when the subjobs are collapsed or uncollapsed
     */
    public void forceConnectionsUpdate() {
        fireStructureChange(OUTPUTS, null);
    }

    /**
     * Remove a connection output.
     *
     * @param connection
     */
    @Override
    public void removeOutput(final IConnection connection) {
        // remember the order of the connection in outputs before delete
        if (connection instanceof Connection) {
            int order = getOrder(connection);
            if (order != -1) {
                ((Connection) connection).setOrder(order);
            }
        }
        this.outputs.remove(connection);
        // update the order wehn remove the connection
        ((Connection) connection).updateAllId();
        removeTargetMetaData(connection);
        calculateSubtreeStartAndEnd();
        fireStructureChange(OUTPUTS, connection);
    }

    public int countConnectionsUsingMetadata(IMetadataTable metaTable) {
        int retValue = 0;
        List<? extends IConnection> connList = this.getOutgoingConnections();
        if (connList != null) {
            for (IConnection iconn : connList) {
                if (metaTable == iconn.getMetadataTable()) {
                    retValue++;
                }
            }
        }
        return retValue;
    }

    private int getIndex(List<Map<String, String>> vlist, String label) {
        int i = 0;
        for (Map<String, String> map : vlist) {
            if (map.get(IEbcdicConstant.FIELD_SCHEMA).equals(label)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    private int getOrder(IConnection connection) {
        for (int i = 0; i < outputs.size(); i++) {
            if (connection == outputs.get(i)) {
                return i;
            }
        }
        return -1;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.Element#getPropertyValue(java.lang.Object)
     */
    @Override
    public Object getPropertyValue(final String id) {
        if (id.equals(EParameterName.UNIQUE_NAME.getName())) {
            return getUniqueName();
        }

        return super.getPropertyValue(id);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.Element#setPropertyValue(java.lang.Object, java.lang.Object)
     */
    @Override
    public void setPropertyValue(final String id, Object value) {
        if (id.equals(EParameterName.REPAINT.getName())) {
            // this repaint parameter maybe not exists in the node paramters
            firePropertyChange(id, null, null);
            return;
        }
        IElementParameter parameter = getElementParameter(id);
        if (id.contains(EParameterName.SCHEMA_TYPE.getName()) || id.contains(EParameterName.QUERYSTORE_TYPE.getName())
                || id.contains(EParameterName.PROPERTY_TYPE.getName())
                || id.contains(EParameterName.PROCESS_TYPE_PROCESS.getName())
                || id.contains(EParameterName.VALIDATION_RULES.getName())
                || id.contains(EParameterName.VALIDATION_RULE_TYPE.getName())) {
            setPropertyValue(EParameterName.UPDATE_COMPONENTS.getName(), Boolean.TRUE);
        }
        if (parameter == null) { // in case we try to set a value to a
            // parameter that doesn't exists
            return;
        }
        if (id.equals(EParameterName.LABEL.getName())) {
            labelToParse = (String) value;
            String newValue = ElementParameterParser.parse(this, labelToParse);
            setLabel(newValue);
        }

        if (id.equals(EParameterName.CONNECTION_FORMAT.getName())) {
            connectionToParse = (String) value;
            boolean isTestCase = false;
            if (GlobalServiceRegister.getDefault().isServiceRegistered(ITestContainerProviderService.class)) {
                ITestContainerProviderService testContainerService = GlobalServiceRegister
                        .getDefault().getService(ITestContainerProviderService.class);
                isTestCase = getProcess() != null && testContainerService.isTestContainerProcess(getProcess());
            }
            if (!isTestCase) {
                setConnectionName(ElementParameterParser.parse(this, connectionToParse));
            }
            // to check
            // String newValue = ElementParameterParser.parse(this, connectionToParse);
            // setConnectionName(newValue);
        }

        if (id.equals(EParameterName.START.getName())) {
            setStart((Boolean) value);
        }

        if (id.equals(EParameterName.ACTIVATE.getName())) {
            setActivate(Boolean.valueOf(value.toString()));
        }

        if (id.equals(EParameterName.DUMMY.getName())) {
            setDummy((Boolean) value);
        }

        if (id.equals(EParameterName.VALIDATION_RULES.getName())) {
            setHasValidationRule((Boolean) value);
        }

        if (id.equals(EParameterName.HINT.getName())) {
            hintToParse = (String) value;
            String newValue = ElementParameterParser.parse(this, hintToParse);
            setShowHintText(newValue);
        }
        // unique name can only be set when the process is loaded
        if (id.equals(EParameterName.UNIQUE_NAME.getName())) {
            setUniqueName((String) value);
            parameter.setValue(value);
        }
        if (id.equals(EParameterName.SHOW_HINT.getName())) {
            setShowHint((Boolean) value);
        }

        final String processPrefix = "PROCESS:"; //$NON-NLS-1$
        if (id.equals(processPrefix + EParameterName.PROCESS_TYPE_CONTEXT.getName())) { // is child
            if (!CommonsPlugin.isHeadless()) {
                if (((Process) getProcess()).getEditor() != null && (!((Process) getProcess()).isDuplicate())) {
                    // ((Process) getProcess()).get

                    // final String jobId = (String) getPropertyValue(processPrefix +
                    // EParameterName.PROCESS_TYPE_PROCESS.getName());
                    // ProcessorUtilities.generateCode(jobId, (String) value, null, false, false,
                    // ProcessorUtilities.GENERATE_MAIN_ONLY);

                    // fix bug 0005678: tRunJob properties are very slow
                    // commnets the following code. do not know why it's added
                    ((AbstractMultiPageTalendEditor) ((Process) getProcess()).getEditor()).updateChildrens();
                }
            }
        }

        if (parameter.getFieldType().equals(EParameterFieldType.MAPPING_TYPE)) {
            for (IMetadataTable table : getMetadataList()) {
                table.setDbms((String) value);
            }
        }

        boolean refreshMRSub = false;
        if (!id.equals(EParameterName.LABEL.getName()) && !id.equals(EParameterName.UNIQUE_NAME.getName())
                && !id.equals(EParameterName.UPDATE_COMPONENTS.getName())) {
            needlibrary = true;
            if (!this.process.isDuplicate() && !CommonsPlugin.isHeadless()) {
                if (isMapReduce()) {
                    refreshMRSub = true;
                    if (needCleared()) {
                        refreshNodeContainer();
                    }
                } else {
                    // Fix for TDI-30185:statistics should not be cleard on preoperty change
                    // IConnection[] conns = process.getAllConnections(null);
                    // for (IConnection conn : conns) {
                    // if (conn instanceof IPerformance) {
                    // ((IPerformance) conn).setPerformanceData(""); //$NON-NLS-1$
                    // }
                    // }
                }
            }
        }

        parameter.setValue(value);

        if (id.equals(EParameterName.INFORMATION.getName())) {
            firePropertyChange(UPDATE_STATUS, null, new Integer(this.currentStatus));
        }

        if (id.equals(EParameterName.FORLOOP.getName())) {
            firePropertyChange(RETURNS_CHANGED, null, null);
        }

        if (id.equals(EParameterName.VARIABLES.getName()) /* tSetGlovarVar */
                || id.equals("MAP") || id.equals(EParameterName.DEFAULT_MAP.getName()))/* tFlowToIterate */ { //$NON-NLS-1$
            firePropertyChange(RETURNS_CHANGED, null, null);
        }

        updateVisibleData();

        if (id.equals("WAIT_FOR") && this.getComponent().getName().equals("tParallelize")) { //$NON-NLS-1$ //$NON-NLS-2$
            List<Connection> synchroConnList = (List<Connection>) this.getOutgoingConnections(EConnectionType.SYNCHRONIZE);
            for (Connection synchroConn : synchroConnList) {
                synchroConn.updateName();
            }
        }

    }

    @Override
    public void setErrorInfoChange(final String id, Object value) {
        if (id.equals("ERRORINFO")) { //$NON-NLS-1$
            firePropertyChange(UPDATE_STATUS, null, null);
        } else if (id.equals("COMPAREINFO")) { //$NON-NLS-1$
            firePropertyChange(UPDATE_STATUS, null, null);
        }
    }

    @Override
    public List<IMetadataTable> getMetadataList() {
        return this.metadataList;
    }

    @Override
    public void setMetadataList(final List<IMetadataTable> metaDataList) {
        this.metadataList = metaDataList;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.Element#getElementName()
     */
    @Override
    public String getElementName() {
        return getUniqueName();
    }

    @Override
    public boolean isExternalNode() {
        if (externalNode != null) {
            return true;
        }
        return false;
    }

    @Override
    public IExternalNode getExternalNode() {
        if (externalNode != null) {
            externalNode.setActivate(isActivate());
            externalNode.setStart(isStart());
            List<IMetadataTable> copyOfMetadataList = new ArrayList<IMetadataTable>();
            for (IMetadataTable metaTable : getMetadataList()) {
                copyOfMetadataList.add(metaTable.clone(true));
            }
            externalNode.setMetadataList(copyOfMetadataList);
            externalNode.setIncomingConnections(inputs);
            externalNode.setOutgoingConnections(outputs);
            externalNode.setElementParameters(getElementParameters());
            externalNode.setUniqueName(getUniqueName());
            externalNode.setSubProcessStart(isSubProcessStart());
            externalNode.setProcess(getProcess());
            externalNode.setComponent(getComponent());
        }
        return this.externalNode;
    }

    public void setExternalNode(final IExternalNode externalNode) {
        this.externalNode = externalNode;
    }

    @Override
    public IExternalData getExternalData() {
        if (externalNode != null) {
            return externalNode.getExternalData();
        }
        return null;
    }

    public void setExternalData(final IExternalData persistantData) {
        if (externalNode != null) {
            this.externalNode.setExternalData(persistantData);
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public NodeContainer getNodeContainer() {
        return this.nodeContainer;
    }

    public void setNodeContainer(NodeContainer nodeContainer) {
        this.nodeContainer = nodeContainer;
    }

    /**
     * @see org.talend.core.model.process.INode#setPerformanceData(java.lang.String)
     */
    @Override
    public void setPerformanceData(String perfData) {
        // not used anymore
    }

    /**
     * Getter for performanceData.
     *
     * @return the performanceData
     */
    public String getPerformanceData() {
        return this.performanceData;
    }

    @Override
    public boolean isActivate() {
        return this.activate;
    }

    @SuppressWarnings("unchecked")
    public void setActivate(final boolean activate) {
        this.activate = activate;
        nodeLabel.setActivate(activate);
        nodeError.setActivate(activate);
        nodeProgressBar.setActivate(activate);
        List<Connection> connectionsOutputs = (List<Connection>) this.getOutgoingConnections();
        List<Connection> connectionsInputs = (List<Connection>) this.getIncomingConnections();

        boolean hasActivatedOutput = false;
        for (Connection connection : connectionsOutputs) {
            if (connection.isActivate() && connection.getLineStyle().hasConnectionCategory(IConnectionCategory.FLOW)) {
                hasActivatedOutput = true;
            }
        }
        if (!hasActivatedOutput || activate) {
            setPropertyValue(EParameterName.DUMMY.getName(), false);
        }

        if (!isDummy()) {
            for (Connection connection : connectionsOutputs) {
                if (connection.getTarget().isActivate() || connection.getTarget().isActivate() == activate
                        || connection.getSource().isDummy()) {
                    connection.setPropertyValue(EParameterName.ACTIVATE.getName(), activate);
                }
                if (!connection.getTarget().isActivate() && !activate) {
                    if (connection instanceof IPerformance) {
                        ((IPerformance) connection).setPerformanceData(""); //$NON-NLS-1$
                    }
                }
            }
            for (Connection connection : connectionsInputs) {
                if (connection.getSource().isActivate() || connection.getSource().isDummy()) {
                    connection.setPropertyValue(EParameterName.ACTIVATE.getName(), activate);
                }
                if (!connection.getSource().isActivate() && !activate) {
                    // check if the input has activated outputs
                    hasActivatedOutput = false;
                    for (Connection sourceConn : (List<Connection>) connection.getSource().getOutgoingConnections()) {
                        if (sourceConn.isActivate()
                                && sourceConn.getLineStyle().hasConnectionCategory(IConnectionCategory.FLOW)) {
                            hasActivatedOutput = true;
                        }
                    }
                    if (!hasActivatedOutput) {
                        connection.getSource().setPropertyValue(EParameterName.DUMMY.getName(), false);
                        connection.getSource().setPropertyValue(EParameterName.ACTIVATE.getName(), false);
                    }
                    if (connection instanceof IPerformance) {
                        ((IPerformance) connection).setPerformanceData(""); //$NON-NLS-1$
                    }
                }
            }
        } else {
            for (Connection connection : connectionsInputs) {
                if (!connection.getSource().isActivate() && !activate) {
                    if (connection instanceof IPerformance) {
                        ((IPerformance) connection).setPerformanceData(""); //$NON-NLS-1$
                    }
                }
            }
        }
        firePropertyChange(EParameterName.ACTIVATE.getName(), null, null);
    }

    /**
     * DOC nrousseau Comment method "isDummy".
     *
     * @return
     */
    @Override
    public boolean isDummy() {
        return dummy;
    }

    /**
     * DOC nrousseau Comment method "setDummy".
     *
     * @param value
     */
    public void setDummy(Boolean value) {
        dummy = value;
    }

    @Override
    public boolean hasRunIfLink() {
        boolean runIf = false;
        Connection connec;
        if (isActivate()) {
            for (int j = 0; j < getIncomingConnections().size() && !runIf; j++) {
                connec = (Connection) getIncomingConnections().get(j);
                if (connec.isActivate()) {
                    if ((connec.getLineStyle().equals(EConnectionType.RUN_IF)
                            || connec.getLineStyle().equals(EConnectionType.ON_COMPONENT_ERROR)
                            || connec.getLineStyle().equals(EConnectionType.ON_COMPONENT_OK)
                            || connec.getLineStyle().equals(EConnectionType.STARTS))) {
                        runIf = true;
                    }
                    if (!runIf) {
                        runIf = connec.getSource().hasRunIfLink();
                    }
                }
            }
        }
        return runIf;
    }

    @Override
    public boolean isSubProcessStart() {
        IConnection connec;
        if (isActivate()) {
            if (!isELTComponent()) {
                for (int j = 0; j < getIncomingConnections().size(); j++) {
                    connec = getIncomingConnections().get(j);
                    if (connec.isActivate()) {
                        if (connec.getLineStyle().hasConnectionCategory(IConnectionCategory.MAIN)) {
                            return false;
                        }
                    }
                }
            } else {
                List<? extends IConnection> tableRefs = getOutgoingConnections(EConnectionType.TABLE_REF);
                if (tableRefs != null && 0 < tableRefs.size()) {
                    return false;
                }
                if (!(Boolean) getPropertyValue(EParameterName.STARTABLE.getName())) {
                    return false;
                }
            }
        }
        if (isDummy()) {
            return false;
        }
        return true;
    }

    @Override
    public IMetadataTable getMetadataTable(String metaName) {
        for (IMetadataTable element : metadataList) {
            String tableName = element.getTableName();
            if (tableName != null && tableName.equals(metaName)) {
                return element;
            }
        }
        return null;
    }

    @Override
    public IMetadataTable getMetadataFromConnector(String connector) {
        for (IMetadataTable table : metadataList) {
            if (table != null && table.getAttachedConnector() != null) {
                if (table.getAttachedConnector().equals(connector)) {
                    return table;
                }
            }
        }

        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.INode#hasConditionnalOutputs()
     */
    @Override
    public boolean hasConditionalOutputs() {
        return component.hasConditionalOutputs();
    }

    @Override
    public boolean isMultiplyingOutputs() {
        return component.isMultiplyingOutputs();
    }

    @Override
    public List<BlockCode> getBlocksCodeToClose() {
        return null;
    }

    /**
     * Will return the first item of the subprocess. If "withCondition" is true, if there is links from type RunIf /
     * RunAfter / RunBefore, it will return the first element found. If "withCondition" is false, it will return the
     * first element with no active link from type Main/Ref/Iterate.<br>
     * <i><b>Note:</b></i> This function doesn't work if the node has several start points (will return a random start
     * node).
     *
     * @param withCondition
     * @return Start Node found.
     */
    @Override
    public INode getSubProcessStartNode(boolean withConditions) {
        if (!withConditions) {
            // if there is the dummy state, we know we're still in the same subjob as the previous node.
            boolean found = false;
            for (IConnection connection : getIncomingConnections()) {
                INode source = connection.getSource();
                if (source.getJobletNode() != null) {
                    source = source.getJobletNode();
                }
                INode target = connection.getTarget();
                if (target.getJobletNode() != null) {
                    target = target.getJobletNode();
                }

                if (connection.getLineStyle().hasConnectionCategory(IConnectionCategory.MAIN)) {
                    if (source.isActivate() == target.isActivate()) {
                        found = true;
                        break;
                    }
                    if (((Node) source).isDummy() || isDummy()) {
                        return source.getSubProcessStartNode(withConditions);
                    }
                }
            }
            if (!found) {
                return this;
            }
        } else {
            boolean found = false;
            for (IConnection connection : getIncomingConnections()) {
                INode source = connection.getSource();
                if (source.getJobletNode() != null) {
                    source = source.getJobletNode();
                }
                INode target = connection.getTarget();
                if (target.getJobletNode() != null) {
                    target = target.getJobletNode();
                }
                if ((source.isActivate() == target.isActivate())) {
                    found = true;
                    break;
                }
                if (((Node) source).isDummy() || isDummy()) {
                    return source.getSubProcessStartNode(withConditions);
                }
            }
            if (!found) {
                return this;
            }
        }
        IConnection connec;

        // List<IConnection> connection = (List<IConnection>) getIncomingConnections();
        //
        // Collections.sort(connection, new Comparator<IConnection>() {
        //
        // @Override
        // public int compare(IConnection arg0, IConnection arg1) {
        // if (arg1.getLineStyle().hasConnectionCategory(IConnectionCategory.DATA)) {
        // return 1;
        // }
        // return 0;
        // }
        //
        // });

        for (int j = 0; j < getIncomingConnections().size(); j++) {
            connec = getIncomingConnections().get(j);
            if (!connec.getLineStyle().hasConnectionCategory(IConnectionCategory.USE_HASH)) {
                INode source = connec.getSource();
                if (source.getJobletNode() != null) {
                    source = source.getJobletNode();
                }
                return source.getSubProcessStartNode(withConditions);
            }
        }
        return null;
    }

    private INode getMainBranch(List<INode> visitedNodes) {
        if (visitedNodes.contains(this)) {
            return this;
        } else {
            visitedNodes.add(this);
        }
        Node targetWithRef = null;
        for (int i = 0; i < getOutgoingConnections().size() && targetWithRef == null; i++) {
            IConnection connection = getOutgoingConnections().get(i);
            INode nodeTmp = connection.getTarget();
            if (nodeTmp.getJobletNode() != null) {
                nodeTmp = nodeTmp.getJobletNode();
            }
            if ((connection.getLineStyle().hasConnectionCategory(IConnectionCategory.USE_HASH)
                    || nodeTmp.getOutgoingConnections(EConnectionType.TABLE).size() != 0
                    || nodeTmp.getIncomingConnections(EConnectionType.TABLE).size() != 0
                    || nodeTmp.getOutgoingConnections(EConnectionType.TABLE_REF).size() != 0
                    || nodeTmp.getIncomingConnections(EConnectionType.TABLE_REF).size() != 0)
                    && !connection.getLineStyle().hasConnectionCategory(IConnectionCategory.DEPENDENCY)) {
                // System.out.println(" ** Ref Link Found in:" + nodeTmp + "
                // from:" + this);
                targetWithRef = (Node) nodeTmp;
            } else {
                if (this.process.isThereLinkWithHash(nodeTmp)) {
                    // System.out.println(" ** Ref Link Found in:" + nodeTmp + "
                    // from:" + this);
                    targetWithRef = (Node) nodeTmp;
                }
            }
        }
        if (targetWithRef == null || targetWithRef.equals(this)) {
            // System.out.println(" ** No Ref Links found from:" + this);
            Map<INode, Integer> mergeInfo = getLinkedMergeInfo();
            if (mergeInfo.size() > 0) {
                // if all merge info got 1, then it's the main branch.
                for (INode node : mergeInfo.keySet()) {
                    if (mergeInfo.get(node) != 1) {
                        // get the first merge connection to have the main branch (id 1 for merge connection)
                        List<? extends IConnection> connections = NodeUtil.getIncomingConnections(node,
                                IConnectionCategory.MERGE);
                        if (connections.size() > 0) {
                            IConnection connection = connections.get(0);
                            return ((Node) connection.getSource()).getMainBranch(visitedNodes);
                        } else {
                            return this;
                        }
                    }
                }
                // if go here, then this component is on the main branch.
                return this;
            }
            return this;
        } else {
            // System.out.println(" ** Check Ref Links in:" + targetWithRef + "
            // from:" + this);
            return targetWithRef.getMainBranch(visitedNodes);
        }
    }

    private INode getMainBranch() {
        return getMainBranch(new ArrayList<INode>());
    }

    @Override
    public Node getProcessStartNode(boolean withConditions) {
        // System.out.println(" --- Checking :" + this + " ---");

        // First getMainBranch is in case there is a merge
        // Then take the first component of the subjob (in case there is a lookup)
        // Then if there is a lookup, get the main branch
        // Then take the first component of the main branch.
        // >> Can be optimized for simple cases.

        int nbLoops = 0;

        Node mainBranchNode = (Node) getMainBranch().getSubProcessStartNode(false);
        Node mainSubBranchNode = (Node) mainBranchNode.getMainBranch().getSubProcessStartNode(withConditions);
        if (mainSubBranchNode == null) {
            return mainBranchNode;
        }
        while (mainBranchNode != mainSubBranchNode && nbLoops < 3) {
            mainBranchNode = (Node) mainSubBranchNode.getMainBranch().getSubProcessStartNode(withConditions);
            mainSubBranchNode = (Node) mainBranchNode.getMainBranch().getSubProcessStartNode(withConditions);
            nbLoops++;
        }
        // code bellow is for debug only
        // if (nbLoops >= 3) {
        // System.out.println("**pb Start on component:" + this.getUniqueName() + " / mainBranchNode="
        // + mainBranchNode.getUniqueName() + " / mainSubBranchNode=" + mainSubBranchNode.getUniqueName());
        // } else {
        // System.out.println("**component:" + this.getUniqueName() + " set start to " +
        // mainSubBranchNode.getUniqueName());
        // }

        return mainSubBranchNode;
    }

    @Override
    public boolean sameProcessAs(INode node, boolean withConditions) {
        // System.out.println("from:" + this + " -- to:" + node);

        Node currentNode = (Node) getSubProcessStartNode(withConditions);
        if (!currentNode.isStart()) {
            currentNode = currentNode.getProcessStartNode(withConditions);
        }
        Node otherNode = (Node) node.getSubProcessStartNode(withConditions);
        if (!otherNode.isStart()) {
            otherNode = otherNode.getProcessStartNode(withConditions);
        }
        // System.out.println("source start:" + currentNode + " -- target
        // start:" + otherNode);
        return currentNode.equals(otherNode);
    }

    @Override
    public boolean isReadOnly() {
        return this.readOnly;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public boolean isForceReadOnly() {
        return this.forceReadOnly;
    }

    @Override
    public void setForceReadOnly(boolean forceReadOnly) {
        this.forceReadOnly = forceReadOnly;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.INode#setProcess(org.talend.core.model.process.IProcess)
     */
    @Override
    public void setProcess(IProcess process) {
        if (process instanceof Process) {
            this.process = (Process) process;
        }
    }

    public void updateStatus() {
        boolean toUpdate = false;
        if (oldStatus != currentStatus) {
            toUpdate = true;
        } else {

            List<String> newErrorList = Problems.getStatusList(ProblemStatus.ERROR,
                    nodeContainer == null ? this : nodeContainer.getNode());
            List<String> newWarningList = Problems.getStatusList(ProblemStatus.WARNING,
                    nodeContainer == null ? this : nodeContainer.getNode());

            if (newErrorList.size() != errorList.size()) {
                toUpdate = true;
            } else if (newWarningList.size() != warningList.size()) {
                toUpdate = true;
            } else {
                for (String error : newErrorList) {
                    if (!errorList.contains(error)) {
                        toUpdate = true;
                        break;
                    }
                }
                if (!toUpdate) {
                    for (String warning : newWarningList) {
                        if (!warningList.contains(warning)) {
                            toUpdate = true;
                            break;
                        }
                    }
                }
            }

            warningList = newWarningList;
            errorList = newErrorList;
        }

        if (toUpdate) {
            firePropertyChange(UPDATE_STATUS, null, new Integer(this.currentStatus));
        }
        oldStatus = currentStatus;
    }

    public void addStatus(int status) {
        if ((this.currentStatus & status) != 0) {
            return;
        }
        this.currentStatus = this.currentStatus | status;
        updateStatus();
    }

    public void removeStatus(int status) {
        if ((this.currentStatus & status) == 0) {
            return;
        }
        this.currentStatus = this.currentStatus ^ status;
        updateStatus();
    }

    public int getStatus() {
        return currentStatus;
    }

    @SuppressWarnings("unchecked")
    private void checkParameters() {
        // Check whether or not we need check column existence.
        // We just check the condition which just have one MetadataTable or Pre-MetadataTable.
        boolean checkColumnExist = false;
        IMetadataTable metadataTable = null;
        List<IMetadataTable> tables = getMetadataList();
        if (tables != null && tables.size() == 1) {
            metadataTable = tables.get(0);
            checkColumnExist = true;
        }
        boolean checkPreColumnExist = false;
        IMetadataTable preMetadataTable = null;
        int preTableCount = 0;
        List<? extends IConnection> incomingConnections = getIncomingConnections();
        if (incomingConnections != null && incomingConnections.size() > 0) {
            for (IConnection incomingConnection : incomingConnections) {
                if (incomingConnection.getLineStyle().hasConnectionCategory(IConnectionCategory.DATA)) {
                    IMetadataTable schemaTable = incomingConnection.getMetadataTable();
                    if (schemaTable != null) {
                        ++preTableCount;
                        if (preTableCount > 1) {
                            break;
                        }
                        preMetadataTable = schemaTable;
                    }
                }
            }
        }
        if (preTableCount == 1) {
            checkPreColumnExist = true;
        }
        List<String> currentColumns = new ArrayList<String>();
        if (checkColumnExist) {
            currentColumns = getColumnLabels(metadataTable);
        }
        List<String> preColumns = new ArrayList<String>();
        if (checkPreColumnExist) {
            preColumns = getColumnLabels(preMetadataTable);
        }

        for (IElementParameter param : this.getElementParametersWithChildrens()) {
            if (param.getMaxlength() > 0) {
                String paramValue = param.getValue().toString();
                paramValue.length();
                String tmpValue;
                if (paramValue.startsWith("\"") && paramValue.endsWith("\"")) { //$NON-NLS-1$ //$NON-NLS-2$
                    tmpValue = paramValue.replaceAll("\"", ""); //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    // tmpValue = paramValue;
                    // don't count if don't start with "
                    continue;
                }
                String factor = "\\\\\\\\"; //$NON-NLS-1$
                Pattern pattern = Pattern.compile(factor);
                Matcher matcher = pattern.matcher(tmpValue);
                int lenth = 0;
                matcher.groupCount();
                while (matcher.find()) {
                    lenth++;
                }
                String last = tmpValue.replaceAll(factor, ""); //$NON-NLS-1$
                last = last.replaceAll("\\\\", ""); //$NON-NLS-1$ //$NON-NLS-2$
                int realLength = last.length() + lenth;
                if (realLength > param.getMaxlength()) {
                    String errorMessage = Messages.getString("Node.overLength", param.getDisplayName(), param.getMaxlength()); //$NON-NLS-1$
                    Problems.add(ProblemStatus.ERROR, this, errorMessage);
                }
            }

            if (param.getFieldType() == EParameterFieldType.CLOSED_LIST) {
                String showIf = param.getShowIf();
                String notShowIf = param.getNotShowIf();
                IElementParameter dbTypeEle = this.getElementParameter("DBTYPE");//$NON-NLS-1$
                if (dbTypeEle != null && showIf != null && notShowIf == null) {
                    String dbType = (String) dbTypeEle.getValue();
                    boolean show = false;
                    if (showIf.contains(dbType)) {
                        show = true;
                    }
                    if (show && !ArrayUtils.contains(param.getListItemsValue(), param.getValue())) {
                        Problems.add(ProblemStatus.ERROR, this,
                                "Unknown value in the list / Value set not supported by the component"); //$NON-NLS-1$
                    }
                } else if ("DQRULES_LIST".equals(param.getName()) || "PATTERN_LIST".equals(param.getName())) { //$NON-NLS-1$ //$NON-NLS-2$
                    // MOD for TDI-19063 Do not check value for these 2 parameters.
                } else {
                    if (param.isShow(this.getElementParameters())
                            && !ArrayUtils.contains(param.getListItemsValue(), param.getValue()) && !param.isDynamicSettings()) {
                        Problems.add(ProblemStatus.ERROR, this, "Unknown value in the list [" + param.getDisplayName() //$NON-NLS-1$
                                + "] / Value set not supported by the component"); //$NON-NLS-1$
                    }
                }
            }

            if (param.getFieldType() == EParameterFieldType.TABLE) {
                // Check columns which not existing. Note: just check string type of parameter value.
                Object[] tableItemsValue = param.getListItemsValue();
                List<Map<String, Object>> tableValues = (List<Map<String, Object>>) param.getValue();

                List<String> columnListParamNames = new ArrayList<String>();
                List<String> preColumnListParamNames = new ArrayList<String>();
                if (tableItemsValue != null && tableItemsValue.length > 0) {
                    for (Object tabItemValue : tableItemsValue) {
                        if (tabItemValue instanceof IElementParameter) {
                            IElementParameter itemParameter = (IElementParameter) tabItemValue;
                            if (itemParameter.getFieldType() == EParameterFieldType.COLUMN_LIST) {
                                columnListParamNames.add(itemParameter.getName());
                            }
                            if (itemParameter.getFieldType() == EParameterFieldType.PREV_COLUMN_LIST) {
                                preColumnListParamNames.add(itemParameter.getName());
                            }
                            if (itemParameter.getFieldType() == EParameterFieldType.CONTEXT_PARAM_NAME_LIST
                                    && tableValues.size() > 0) {
                                Object[] itemsValue = itemParameter.getListItemsValue();
                                if (itemParameter.getListItemsDisplayName() == null
                                        || itemParameter.getListItemsDisplayName().length == 0) {
                                    itemsValue = getContextParamsFromProcess();
                                }

                                for (int index = 0; index < tableValues.size(); index++) {
                                    Map<String, Object> tabMap = tableValues.get(index);
                                    Object value = tabMap.get(itemParameter.getName());

                                    if (itemParameter.getListItemsValue() != null && value != null) {
                                        boolean found = false;
                                        for (Object o : itemsValue) {
                                            if (o.equals(value)) {
                                                found = true;
                                                break;
                                            }
                                        }
                                        if (!found) {
                                            String warnMessage = Messages.getString("Node.notExistedContextName", value, index, //$NON-NLS-1$
                                                    itemParameter.getDisplayName());
                                            Problems.add(ProblemStatus.WARNING, this, warnMessage);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                StringBuffer inexistentColumns = new StringBuffer();
                if (tableValues != null) {
                    for (Map<String, Object> tabMap : tableValues) {
                        int row = tableValues.indexOf(tabMap) + 1;
                        if (checkColumnExist) {
                            for (String paramName : columnListParamNames) {
                                Object columnLineValue = tabMap.get(paramName);
                                if (columnLineValue instanceof String && !currentColumns.contains(columnLineValue)) {
                                    inexistentColumns.append(columnLineValue).append("[Line:" + row + "]").append(","); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                }
                            }
                        }
                        if (checkPreColumnExist) {
                            for (String paramName : preColumnListParamNames) {
                                Object columnLineValue = tabMap.get(paramName);
                                if (columnLineValue instanceof String && !preColumns.contains(columnLineValue)) {
                                    inexistentColumns.append(columnLineValue).append("[Line:" + row + "]").append(","); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                }
                            }
                        }
                        Object type = tabMap.get("TYPE"); //$NON-NLS-1$
                        if (type != null && type.toString().equals("SINGLE")) { //$NON-NLS-1$
                            Object code = tabMap.get("SCHEMA"); //$NON-NLS-1$
                            IMetadataTable metaTable = this.getMetadataTable(code.toString());
                            if (metaTable != null) {
                                if (metaTable.getListColumns(true).size() > 1) {
                                    String warnMessage = Messages.getString("Node.hasMoreThenOneColumn", metaTable.getLabel()); //$NON-NLS-1$
                                    Problems.add(ProblemStatus.WARNING, this, warnMessage);
                                }
                            }
                        }

                    }
                }
                if (inexistentColumns.length() > 0) {
                    inexistentColumns.deleteCharAt(inexistentColumns.length() - 1);
                    String warnMessage = Messages.getString("Node.hasInexistentColumn", inexistentColumns.toString(), //$NON-NLS-1$
                            param.getDisplayName());
                    Problems.add(ProblemStatus.WARNING, this, warnMessage);
                }
                // add a warning message on the component if the source key is empty
                if (param.getName().equals("SOURCE_KEYS")) { //$NON-NLS-1$
                    if (tableValues == null || tableValues.isEmpty()) {
                        String warnMessage = Messages.getString("Node.hasMoreThenOneSourceKey"); //$NON-NLS-1$
                        Problems.add(ProblemStatus.WARNING, this, warnMessage);
                    }
                }
            }

            if (param.getName().equals(EParameterName.COMMENT.getName())) {
                String infoValue = (String) param.getValue();
                if (infoValue != null && !infoValue.equals("")) { //$NON-NLS-1$
                    Problems.add(ProblemStatus.INFO, this, infoValue);
                }
            }
            List<IElementParameter> emptyParamList = Collections.emptyList();
            Boolean noConditionOnShow = StringUtils.isEmpty(param.getShowIf()) && StringUtils.isEmpty(param.getNotShowIf());
            // no condition on show means if the field is displayed or hidden all the time

            /**
             * ***Some explain for the following check***
             *
             * "if (param.isRequired(getElementParameters()) && !param.isShow(emptyParamList)" <br>
             *
             * 1. in fact it's simply for components who have a table never displayed<br>
             * like tAdvancedOutput / tRowGenerator etc<br>
             * 2. as long as there is any show_if / not_show_if.... means there is no point to check any error here,
             * since the table is not displayed with the current config of the component<br>
             * 3. if table is always hidden, then we should force some checks
             */
            // if the parameter is required but empty, an error will be added
            if (param.isRequired(getElementParameters()) && !param.isShow(emptyParamList) && noConditionOnShow
                    && this.externalNode != null) {
                if (param.getFieldType().equals(EParameterFieldType.TABLE)) {
                    List<Map<String, String>> tableValues = (List<Map<String, String>>) param.getValue();
                    // add by wzhang. all schemas need loop element.
                    if (tableValues != null && "tFileOutputMSXML".equalsIgnoreCase(component.getName()) //$NON-NLS-1$
                            && param.getName().equals("LOOP")) { //$NON-NLS-1$
                        checkFileOutputMSXML(param, tableValues);
                    } else if (tableValues != null && "tAdvancedFileOutputXML".equalsIgnoreCase(component.getName()) //$NON-NLS-1$
                            && param.getName().equals("LOOP") && tableValues.size() != 0) { //$NON-NLS-1$
                        // for bug 10108
                        if (((Boolean) this.getElementParameter("MERGE").getValue()) == true) { //$NON-NLS-1$
                            List<Map<String, String>> listGroup = (List<Map<String, String>>) externalNode
                                    .getElementParameter("GROUP").getValue(); //$NON-NLS-1$
                            List<Map<String, String>> listLoop = (List<Map<String, String>>) externalNode
                                    .getElementParameter("LOOP").getValue(); //$NON-NLS-1$
                            if (listGroup.size() == 0 || listLoop.size() == 0) {
                                String errorMessage = Messages.getString("Node.needLoopAndGroup", param.getDisplayName()); //$NON-NLS-1$
                                Problems.add(ProblemStatus.ERROR, this, errorMessage);
                            }
                        }
                    } else {
                        if (tableValues == null || tableValues.size() == 0) {
                            String errorMessage = Messages.getString("Node.needOneValue", param.getDisplayName()); //$NON-NLS-1$
                            Problems.add(ProblemStatus.ERROR, this, errorMessage);
                        }
                    }
                }
            }

            if (param.isRequired() && param.isShow(getElementParameters())) {
                EParameterFieldType fieldType = param.getFieldType();
                String value;
                List multiSchemaDelimetedSeparaor = new ArrayList();
                switch (fieldType) {
                case TABLE:
                    List<Map<String, String>> tableValues = (List<Map<String, String>>) param.getValue();
                    // add by wzhang. all schemas need loop element.
                    if (tableValues != null && "tFileOutputMSXML".equalsIgnoreCase(component.getName()) //$NON-NLS-1$
                            && param.getName().equals("LOOP")) { //$NON-NLS-1$
                        checkFileOutputMSXML(param, tableValues);
                    } else {
                        if (tableValues == null || tableValues.size() == 0) {
                            String errorMessage = Messages.getString("Node.needOneValue", param.getDisplayName()); //$NON-NLS-1$
                            Problems.add(ProblemStatus.ERROR, this, errorMessage);
                        } else if (this.getComponent().getName().equals("tFileInputXML")) {//$NON-NLS-1$
                            for (Map<String, String> map : tableValues) {
                                if (map != null) {
                                    if ("".equals(map.get("QUERY")) || map.get("QUERY") == null) {//$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
                                        Problems.add(ProblemStatus.ERROR, this, Messages.getString("Node.QueryLosed")); //$NON-NLS-1$
                                    }
                                }
                            }
                        }
                    }
                    break;
                case CHECK:
                    break;
                case RADIO:
                    break;
                case SCHEMA_TYPE:
                    break;
                case SCHEMA_REFERENCE:
                    break;
                case DCSCHEMA:
                    // IMetadataTable metadataTable = getMetadataTable(getUniqueName());
                    // if(metadataTable == null || !(metadataTable.getListColumns().size() > 0)) {
                    // String errorMessage = Messages.getString("Node.parameterEmpty", param.getDisplayName());
                    // //$NON-NLS-1$
                    // Problems.add(ProblemStatus.ERROR, this, errorMessage);
                    // }
                    break;
                case MEMO_SQL:
                    String errMessage = Messages.getString("Node.schemaDifferent", param.getDisplayName()); //$NON-NLS-1$
                    Object value2 = param.getValue();
                    if (value2 == null) {
                        break;
                    }
                    String currentQuery = value2.toString();

                    // Checks if current query is empty.
                    // if (currentQuery.equals("")) {
                    // Problems.add(ProblemStatus.WARNING, this, errMessage);
                    // break;
                    // }

                    // Checks current query was generated by clicking "Guess
                    // Query" button.
                    if (CorePlugin.getDefault().getPreferenceStore().getBoolean(ITalendCorePrefConstants.SQL_ADD_WARNING)) {
                        if (currentQuery.indexOf(COMPARE_STR1) != -1 || currentQuery.indexOf(COMPARE_STR2) != -1) {
                            Problems.add(ProblemStatus.WARNING, this, errMessage);
                            break;
                        }
                        if (!NodeQueryCheckUtil.checkQueryOK(this, currentQuery)) {
                            boolean show = true;
                            // match the query and check again
                            if (NodeQueryCheckUtil.isNeedMatchQuery()) {
                                String matchSql = NodeQueryCheckUtil.matchQueryComments(this, currentQuery);
                                NodeQueryCheckUtil.checkQueryOK(this, matchSql);
                                show = NodeQueryCheckUtil.isNeedMatchQuery();
                            }
                            if (show) {
                                Problems.add(ProblemStatus.WARNING, this, errMessage);
                            }
                            break;
                        }
                    }
                    break;
                case CLOSED_LIST:
                    value = (String) param.getValue();
                    if (value == null || value.equals("")) { //$NON-NLS-1$
                        String errorMessage = Messages.getString("Node.parameterEmpty", param.getDisplayName()); //$NON-NLS-1$
                        Problems.add(ProblemStatus.ERROR, this, errorMessage);
                    } else {
                        if (param.getListItemsValue().length != 0) {
                            boolean found = false;
                            for (int i = 0; i < param.getListItemsValue().length && !found; i++) {
                                if (param.getListItemsValue()[i].equals(value)) {
                                    found = true;
                                }
                            }
                            boolean isContextValue = ContextParameterUtils
                                    .isContainContextParam(String.valueOf(param.getValue()));
                            if (!found && !isContextValue) {
                                String errorMessage = Messages.getString("Node.parameterNotExist", param.getDisplayName(), value); //$NON-NLS-1$
                                Problems.add(ProblemStatus.ERROR, this, errorMessage);
                            }
                        }
                    }
                    break;
                case COMPONENT_LIST:
                    if (param != null) {
                        String errorMessage;
                        boolean isContextMode = false;
                        if (param.getValue() == null || "".equals(param.getValue())) { //$NON-NLS-1$
                            errorMessage = Messages.getString("Node.parameterEmpty", param.getDisplayName()); //$NON-NLS-1$
                        } else {
                            errorMessage = Messages.getString("Node.parameterNotExist", param.getDisplayName(), param.getValue()); //$NON-NLS-1$
                            isContextMode = param.isDynamicSettings();
                        }

                        if (!isContextMode && ((!hasUseExistingConnection(this)) || (isUseExistedConnetion(this)))) {
                            List<INode> list = (List<INode>) this.getProcess().getNodesOfType(param.getFilter());
                            if (list == null || list.size() == 0 || list.isEmpty()) {
                                Problems.add(ProblemStatus.ERROR, this, errorMessage);
                            } else {
                                List<INode> nodeList = new ArrayList<INode>();
                                for (INode datanode : list) {
                                    if (!datanode.isVirtualGenerateNode()) {
                                        nodeList.add(datanode);
                                    }
                                }
                                if (nodeList.size() == 0 || nodeList.isEmpty()) {
                                    Problems.add(ProblemStatus.ERROR, this, errorMessage);
                                    break;
                                }
                                boolean foundValue = false;
                                for (INode datanode : nodeList) {
                                    if (datanode.getUniqueName().equals(param.getValue())) {
                                        foundValue = true;
                                        break;
                                    }
                                }
                                errorMessage = Messages.getString("Node.parameterWrong", param.getDisplayName()); //$NON-NLS-1$
                                if (!foundValue) {
                                    Problems.add(ProblemStatus.ERROR, this, errorMessage);
                                    // TDI-35251 comment for TDI-33285
                                    // PropertyChangeCommand changeCommand = new PropertyChangeCommand(this,
                                    // param.getName(),
                                    // nodeList.get(0).getUniqueName());
                                    // if (this.getCommandStack() != null) {
                                    // this.getCommandStack().execute(changeCommand);
                                    // }
                                }
                            }
                        }
                    }
                    break;
                default:
                    if (param.getValue() != null && !(param.getValue() instanceof String)) {
                        break;
                    }
                    value = (String) param.getValue();
                    if (value == null || value.equals("")) { //$NON-NLS-1$
                        if (this.getComponent() != null && "tFileInputMSDelimited".equals(this.getComponent().getName())) { //$NON-NLS-1$
                            multiSchemaDelimetedSeparaor.add(param);
                            if (multiSchemaDelimetedSeparaor.size() == 2) {
                                String errorMessage = Messages.getString("Node.parameterEmpty", param.getDisplayName()); //$NON-NLS-1$
                                Problems.add(ProblemStatus.ERROR, this, errorMessage);
                            }
                        } else {
                            // TESB-6191
                            if (getComponent() != null && "cTalendJob".equals(getComponent().getName())) { //$NON-NLS-1$
                                IElementParameter fromRepository = getElementParameter("FROM_REPOSITORY_JOB"); //$NON-NLS-1$
                                Object isFromRepository = fromRepository.getValue();
                                if (isFromRepository != null && "true".equals(isFromRepository.toString())) { //$NON-NLS-1$
                                    String errorMessage = Messages.getString("Node.parameterEmpty", param.getDisplayName()); //$NON-NLS-1$
                                    Problems.add(ProblemStatus.ERROR, this, errorMessage);
                                } else {
                                    // ignore the cTalendJob validation if it's not use the "FROM_REPOSITORY_JOB"
                                    // DO nothing
                                }
                            } else {
                                String errorMessage = Messages.getString("Node.parameterEmpty", param.getDisplayName()); //$NON-NLS-1$
                                Problems.add(ProblemStatus.ERROR, this, errorMessage);
                            }
                        }
                    }
                    checkDataprepRun(param);
                }
            }
            checkValidationRule(param);

            checktAggregateRow(param);

            checkDynamicJobUsage(param);
        }

        checkJobletConnections();

        IElementParameter enableParallelizeParameter = getElementParameter(EParameterName.PARALLELIZE.getName());
        if (enableParallelizeParameter != null && enableParallelizeParameter.getValue() != null) {
            boolean x = (Boolean) enableParallelizeParameter.getValue();
            if (x) {
                addStatus(Process.PARALLEL_STATUS);
            }
        }

    }

    private Object[] getContextParamsFromProcess() {
        IElementParameter processTypeParam = this.getElementParameterFromField(EParameterFieldType.PROCESS_TYPE);
        if (processTypeParam == null) {
            processTypeParam = this.getElementParameterFromField(EParameterFieldType.ROUTE_INPUT_PROCESS_TYPE);
        }
        if (processTypeParam != null) {
            IElementParameter jobElemParam = processTypeParam.getChildParameters()
                    .get(EParameterName.PROCESS_TYPE_PROCESS.getName());
            IElementParameter jobVersionParam = processTypeParam.getChildParameters()
                    .get(EParameterName.PROCESS_TYPE_VERSION.getName());
            IElementParameter contextElemParam = processTypeParam.getChildParameters()
                    .get(EParameterName.PROCESS_TYPE_CONTEXT.getName());
            // get context list
            String processId = (String) jobElemParam.getValue();
            String contextName = (String) contextElemParam.getValue();
            if (contextName == null) {
                contextName = new String();
            }

            if (processId != null && contextName != null) {
                IElementParameter useDynamic = this.getElementParameter(EParameterName.USE_DYNAMIC_JOB.getName());
                if (useDynamic != null && Boolean.valueOf(String.valueOf(useDynamic.getValue()))) {
                    String[] split = processId.split(";");
                    processId = split[0];
                }
                ProcessItem processItem = ItemCacheManager.getProcessItem(processId, (String) jobVersionParam.getValue());
                if (processItem != null && processItem.getProcess() != null) {
                    final EList context = processItem.getProcess().getContext();
                    if (!context.isEmpty()) {
                        List<String> contextParameterNamesList = new ArrayList<String>();
                        final ContextType contextType = (ContextType) context.get(0);
                        for (Object p : contextType.getContextParameter()) {
                            if (p instanceof ContextParameterType) {
                                contextParameterNamesList.add(((ContextParameterType) p).getName());
                            }
                        }
                        return contextParameterNamesList.toArray(new String[0]);
                    }
                }
            }
        }
        return new String[0];
    }

    private List<String> getColumnLabels(IMetadataTable metadataTable) {
        List<String> columnLabels = new ArrayList<String>();
        if (metadataTable == null) {
            return columnLabels;
        }
        List<IMetadataColumn> columns = metadataTable.getListColumns();
        for (IMetadataColumn column : columns) {
            String columnLabel = column.getLabel();
            if (!columnLabels.contains(columnLabel)) {
                columnLabels.add(columnLabel);
            }
        }

        return columnLabels;
    }

    private void checktAggregateRow(IElementParameter param) {
        EParameterFieldType fieldType = param.getFieldType();
        if ("tAggregateRow".equals(component.getName()) && EParameterFieldType.TABLE.equals(fieldType) //$NON-NLS-1$
                && EParameterName.GROUPBYS.name().equals(param.getName())) {
            Set<String> duplicatedColumns = new HashSet<String>();
            List<String> existColumns = new ArrayList<String>();
            if (param.getValue() instanceof List) {
                for (Object obj : (List) param.getValue()) {
                    if (obj instanceof Map) {
                        Map childElement = (Map) obj;
                        Object outputColumnName = childElement.get(param.getListItemsDisplayCodeName()[0]);
                        if (outputColumnName instanceof String) {
                            if (existColumns.contains(outputColumnName)) {
                                duplicatedColumns.add((String) outputColumnName);
                            } else {
                                existColumns.add((String) outputColumnName);
                            }
                        }

                    }
                }
            }
            IElementParameter relatedParam = getElementParameter(EParameterName.OPERATIONS.name());
            if (relatedParam != null && relatedParam.getValue() instanceof List) {
                for (Object obj : (List) relatedParam.getValue()) {
                    if (obj instanceof Map) {
                        Map childElement = (Map) obj;
                        Object outputColumnName = childElement.get(relatedParam.getListItemsDisplayCodeName()[0]);
                        if (outputColumnName instanceof String) {
                            if (existColumns.contains(outputColumnName)) {
                                duplicatedColumns.add((String) outputColumnName);
                            } else {
                                existColumns.add((String) outputColumnName);
                            }
                        }
                    }
                }
            }
            if (!duplicatedColumns.isEmpty()) {
                String errorMessage = "Duplicated output columns exist in Group by or Operations table: "; //$NON-NLS-1$
                for (String columnName : duplicatedColumns) {
                    errorMessage += columnName + ","; //$NON-NLS-1$
                }
                errorMessage = errorMessage.substring(0, errorMessage.length() - 1);
                Problems.add(ProblemStatus.WARNING, this, errorMessage);
            }
        }
    }

    private void checkDynamicJobUsage(IElementParameter param) {
        if (!EParameterName.USE_DYNAMIC_JOB.getName().equals(param.getName())) {
            return;
        }
        boolean isSelectUseDynamic = false;
        Object paramValue = param.getValue();
        if (paramValue != null && paramValue instanceof Boolean) {
            isSelectUseDynamic = (Boolean) paramValue;
        }
        if (isSelectUseDynamic) {
            ERepositoryObjectType itemType = ERepositoryObjectType.getItemType(process.getProperty().getItem());
            if (ERepositoryObjectType.getAllTypesOfJoblet().contains(itemType)) {
                String warningMessage = Messages.getString("Node.checkJobletDynamicJobUsageWarning");
                Problems.add(ProblemStatus.WARNING, this, warningMessage);
            }
        }

    }

    private void checkJobletConnections() {
        if (this.isJoblet()) {
            IProcess2 comProcess = (IProcess2) component.getProcess();
            List<String> rowList = new ArrayList<String>();
            if (comProcess.getProperty().getItem() instanceof JobletProcessItem) {
                JobletProcess jobletProcess = ((JobletProcessItem) comProcess.getProperty().getItem()).getJobletProcess();
                List<JobletNode> jobletNodes = jobletProcess.getJobletNodes();
                out: for (JobletNode jNode : jobletNodes) {
                    if (jNode.isInput()) {
                        List list = jNode.getElementParameter();
                        in: for (Object object : list) {
                            if (object instanceof ElementParameterType) {
                                if (((ElementParameterType) object).getName().equals("UNIQUE_NAME")) { //$NON-NLS-1$
                                    IElementParameter elementParam = this
                                            .getElementParameter(((ElementParameterType) object).getValue());
                                    if (elementParam == null) {
                                        continue out;
                                    }
                                    for (String key : elementParam.getChildParameters().keySet()) {
                                        IElementParameter param = elementParam.getChildParameters().get(key);
                                        if (param != null && param.getName().equals("CONNECTION") && param.getValue() != null //$NON-NLS-1$
                                                && param.getValue().toString().length() > 0) {
                                            if (rowList.contains(param.getValue())) {
                                                String errorMessage = "Can't not have more than one input linked to the same connection"; //$NON-NLS-1$
                                                Problems.add(ProblemStatus.ERROR, this, errorMessage);
                                            } else {
                                                rowList.add((String) param.getValue());
                                            }
                                            break in;
                                        }
                                    }

                                    break in;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * DOC ycbai Comment method "checkValidationRule".
     *
     * @param param
     */
    public boolean checkValidationRule(IElementParameter param) {
        if (EParameterName.VALIDATION_RULES.getName().equals(param.getName())) {
            final IElementParameter paramValidation = getElementParameter(EParameterName.VALIDATION_RULES.getName());
            if (paramValidation != null && paramValidation.getValue() != null && paramValidation.getValue() instanceof Boolean
                    && (Boolean) paramValidation.getValue()) {
                final IElementParameter elementParameter = getElementParameter(
                        EParameterName.REPOSITORY_VALIDATION_RULE_TYPE.getName());
                String vItemId = String.valueOf(elementParameter.getValue());
                String errorMessage;
                if (StringUtils.trimToNull(vItemId) == null) {
                    errorMessage = "Must set the validation rule item id."; //$NON-NLS-1$
                    Problems.add(ProblemStatus.ERROR, this, errorMessage);
                    return false;
                } else {
                    IProxyRepositoryFactory factory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();
                    try {
                        if (factory.getLastVersion(vItemId) == null) {
                            errorMessage = "Validation rule: \"" + vItemId + "\" is not exsist."; //$NON-NLS-1$ //$NON-NLS-2$
                            Problems.add(ProblemStatus.ERROR, this, errorMessage);
                            return false;
                        }
                    } catch (PersistenceException e) {
                        ExceptionHandler.process(e);
                    }
                }
            }
        }
        return true;
    }

    /**
     * DOC wzhang Comment method "checkFileOutputXMLMultiSchemaComponent".
     *
     * @param param
     * @param tableValues
     */
    private void checkFileOutputMSXML(IElementParameter param, List<Map<String, String>> tableValues) {
        List<? extends IConnection> incomingConnections = NodeUtil.getIncomingConnections(externalNode, IConnectionCategory.FLOW);
        List<String> pathList = new ArrayList<String>();
        for (IConnection conn : incomingConnections) {
            String uniqueName = conn.getUniqueName();
            boolean isFirst = true;
            for (Map<String, String> loopMap : tableValues) {
                String newPath = loopMap.get("PATH"); //$NON-NLS-1$
                String columnName = loopMap.get("COLUMN"); //$NON-NLS-1$
                if (columnName != null && columnName.startsWith(uniqueName)) {
                    if (loopMap.get("ATTRIBUTE").equals("main")) { //$NON-NLS-1$ //$NON-NLS-2$
                        if (isFirst) {
                            pathList.add(newPath);
                        }
                        isFirst = false;
                    }
                }
            }
        }

        if (pathList.size() != incomingConnections.size()) {
            // if (tableValues.size() != incomingConnections.size()) {
            String errorMessage = Messages.getString("Node.eachRowNeedLoop", param.getDisplayName()); //$NON-NLS-1$
            Problems.add(ProblemStatus.ERROR, this, errorMessage);
            // }
        }
    }

    private void checkDataprepRun(IElementParameter param) {
        if (EParameterName.PREPARATION_ID.getName().equals(param.getName())) {
            final IElementParameter prepIdParam = getElementParameter(EParameterName.PREPARATION_ID.getName());
            if (prepIdParam == null || prepIdParam.getValue() == null
                    || "".equals(TalendTextUtils.removeQuotes(String.valueOf(prepIdParam.getValue())).trim())) {
                Problems.add(ProblemStatus.ERROR, this, "Must set the preparation id");

            }
        }
    }

    public int getCurrentActiveLinksNbInput(EConnectionType type) {
        int nb = 0;
        for (IConnection connection : inputs) {
            if (connection.isActivate() && connection.getLineStyle().equals(type)) {
                nb++;
            }
        }
        return nb;
    }

    public int getCurrentActiveLinksNbInput(int connCategory) {
        int nb = 0;
        for (IConnection connection : inputs) {
            if (connection.isActivate() && connection.getLineStyle().hasConnectionCategory(connCategory)) {
                nb++;
            }
        }
        return nb;
    }

    public int getCurrentActiveLinksNbOutput(EConnectionType type) {
        int nb = 0;
        for (IConnection connection : outputs) {
            if (connection.isActivate() && connection.getLineStyle().equals(type)) {
                nb++;
            }
        }
        return nb;
    }

    public int getCurrentActiveLinksNbOutput(int connCategory) {
        int nb = 0;
        for (IConnection connection : outputs) {
            if (connection.isActivate() && connection.getLineStyle().hasConnectionCategory(connCategory)) {
                nb++;
            }
        }
        return nb;
    }

    private void checkModules() {
        ILibrariesService moduleService = CorePlugin.getDefault().getLibrariesService();
        Problems.addAll(moduleService.getProblems(this, this));
    }

    /**
     *
     * DOC xye Comment method "checkMultiComponents".
     */
    private void checkMultiComponents() {
        List<IMultipleComponentManager> multipleComponentManagers = getComponent().getMultipleComponentManagers();
        if (multipleComponentManagers != null) {
            for (IMultipleComponentManager multipleComponentManager : multipleComponentManagers) {
                List<IMultipleComponentItem> itemList = multipleComponentManager.getItemList();
                if (itemList != null && !itemList.isEmpty()) {
                    for (IMultipleComponentItem curItem : itemList) {
                        IComponent component = ComponentsFactoryProvider.getInstance().get(curItem.getComponent(),
                                getProcess().getComponentsType());
                        if (component == null) {
                            // Notify an error
                            String errorMessage = Messages.getString("Node.componentNotExist", curItem.getComponent()); //$NON-NLS-1$
                            Problems.add(ProblemStatus.ERROR, this, errorMessage);
                        }
                    }
                }
            }
        }
    }

    /**
     *
     * DOC hcyi Comment method "checkHasMultiPrejobOrPostJobComponents".
     */
    private void checkHasMultiPrejobOrPostJobComponents() {
        Map<String, INode> multiNodes = new HashMap<String, INode>();
        if (PluginChecker.isJobLetPluginLoaded()) {
            IJobletProviderService jobletService = GlobalServiceRegister.getDefault()
                    .getService(IJobletProviderService.class);
            if (jobletService != null) {
                // need to check all node from the process
                List<INode> joblets = new ArrayList<INode>();
                for (INode proGraphicNode : process.getGraphicalNodes()) {
                    boolean jobletComponent = jobletService.isJobletComponent(proGraphicNode);
                    if (jobletComponent) {
                        joblets.add(proGraphicNode);
                    } else {
                        if (("preStaLogCon").equals(proGraphicNode.getUniqueName())) {
                            continue;
                        }
                        checktPreOrPost(multiNodes, proGraphicNode);
                    }
                }
                for (INode jNode : joblets) {
                    // change process.getGeneratingNodes() to
                    // jobletService.getGraphNodesForJoblet(proGraphicNode) for TDI-26472
                    for (INode node : jobletService.getGraphNodesForJoblet(jNode)) {
                        if (("preStaLogCon").equals(node.getUniqueName())) {
                            continue;
                        }
                        checktPreOrPost(multiNodes, node);
                    }
                }
            }
        }
    }

    private void checktPreOrPost(Map<String, INode> multiNodes, INode node) {
        String componentName = node.getComponent().getName();
        if (componentName != null && componentName.equals(TPREJOB_STR)) {
            if (!multiNodes.containsKey(TPREJOB_STR)) {
                multiNodes.put(TPREJOB_STR, node);
            } else {
                String errorMessage = Messages.getString("Node.checkHasMultiPrejobOrPostJobComponents", TPREJOB_STR); //$NON-NLS-1$
                Problems.add(ProblemStatus.WARNING, this, errorMessage);
            }
        }
        if (componentName != null && componentName.equals(TPOSTJOB_STR)) {
            if (!multiNodes.containsKey(TPOSTJOB_STR)) {
                multiNodes.put(TPOSTJOB_STR, node);
            } else {
                String errorMessage = Messages.getString("Node.checkHasMultiPrejobOrPostJobComponents", TPOSTJOB_STR); //$NON-NLS-1$
                Problems.add(ProblemStatus.WARNING, this, errorMessage);
            }
        }
    }

    public void checkLinks() {
        boolean isJoblet = false;
        if (PluginChecker.isJobLetPluginLoaded()) {
            IJobletProviderService service = GlobalServiceRegister.getDefault()
                    .getService(IJobletProviderService.class);
            if (service != null && service.isJobletComponent(this)) {
                isJoblet = true;
            }
        }
        // check not startable components not linked
        INodeConnector connectorFromType = getConnectorFromType(EConnectionType.FLOW_MAIN);
        if (!(Boolean) getPropertyValue(EParameterName.STARTABLE.getName())) {
            if (connectorFromType != null && connectorFromType.isShow()
                    && (getCurrentActiveLinksNbInput(EConnectionType.FLOW_MAIN) == 0)
                    && (connectorFromType.getMinLinkInput() == 0) & (connectorFromType.getMaxLinkInput() != 0)) {
                String errorMessage = Messages.getString("Node.noInputLink"); //$NON-NLS-1$
                Problems.add(ProblemStatus.WARNING, this, errorMessage);
            }
            if ((getCurrentActiveLinksNbInput(EConnectionType.FLOW_MAIN) == 0)
                    && (getCurrentActiveLinksNbInput(EConnectionType.FLOW_REF) > 0)) {
                String errorMessage = Messages.getString("Node.noRowMainLink"); //$NON-NLS-1$
                Problems.add(ProblemStatus.WARNING, this, errorMessage);
            }
        }

        // check not startable components not linked
        if (getComponentProperties() == null) {
            if (connectorFromType != null && connectorFromType.isShow() && (connectorFromType.getMaxLinkInput() == 0)
                    && (connectorFromType.getMaxLinkOutput() != 0)) {
                if ((getCurrentActiveLinksNbOutput(EConnectionType.FLOW_MAIN) == 0)
                        && (getCurrentActiveLinksNbOutput(EConnectionType.FLOW_MERGE) == 0)
                        && (getCurrentActiveLinksNbOutput(EConnectionType.FLOW_REF) == 0)
                        && (getCurrentActiveLinksNbOutput(EConnectionType.ITERATE) == 0)) {
                    String errorMessage = Messages.getString("Node.noOutputLink"); //$NON-NLS-1$
                    Problems.add(ProblemStatus.WARNING, this, errorMessage);
                }
            }
        } else if ((Boolean) getPropertyValue(EParameterName.STARTABLE.getName())) {
            for (INodeConnector connector : getConnectorsFromType(EConnectionType.FLOW_MAIN)) {
                if (connector.isShow() && (connector.getMaxLinkInput() == 0) && (connector.getMaxLinkOutput() != 0)) {
                    if ((getCurrentActiveLinksNbOutput(EConnectionType.FLOW_MAIN) == 0)
                            && (getCurrentActiveLinksNbOutput(EConnectionType.FLOW_MERGE) == 0)
                            && (getCurrentActiveLinksNbOutput(EConnectionType.FLOW_REF) == 0)
                            && (getCurrentActiveLinksNbOutput(EConnectionType.ITERATE) == 0)) {
                        String errorMessage = Messages.getString("Node.noOutputLink"); //$NON-NLS-1$
                        Problems.add(ProblemStatus.WARNING, this, errorMessage);
                    }
                }
            }
        }

        if (!isJoblet) {
            // Check if there's an output run after / before on a component that is
            // not a sub process start
            if (!isSubProcessStart() || (!(Boolean) getPropertyValue(EParameterName.STARTABLE.getName()))) {
                if ((getCurrentActiveLinksNbOutput(EConnectionType.ON_SUBJOB_OK) > 0)
                        || getCurrentActiveLinksNbOutput(EConnectionType.ON_SUBJOB_ERROR) > 0) {

                    String errorMessage = Messages.getString("Node.errorMessage1"); //$NON-NLS-1$
                    Problems.add(ProblemStatus.ERROR, this, errorMessage);
                }
            }

            // if (isSubProcessStart() && process.getMergelinkOrder(this) > 1) {
            // String errorMessage = "A component that is not a sub process start can not have any link run after / run
            // before in output.";
            // Problems.add(ProblemStatus.ERROR, this, errorMessage);
            // }

            // Check if there's an input run after / before on a component that is
            // not a sub process start
            if ((!isELTComponent() && !isSubProcessStart())
                    || (!(Boolean) getPropertyValue(EParameterName.STARTABLE.getName()))) {
                if ((getCurrentActiveLinksNbInput(EConnectionType.ON_SUBJOB_OK) > 0)
                        || (getCurrentActiveLinksNbInput(EConnectionType.RUN_IF) > 0)
                        || (getCurrentActiveLinksNbInput(EConnectionType.ON_COMPONENT_OK) > 0)
                        || (getCurrentActiveLinksNbInput(EConnectionType.ON_COMPONENT_ERROR) > 0)
                        || (getCurrentActiveLinksNbInput(EConnectionType.STARTS) > 0)
                /*
                 * http://jira.talendforge.org/browse/TESB-4858 Modified By LiXiaopeng 2012-2-13 ||
                 * (getCurrentActiveLinksNbInput(EConnectionType.ROUTE_WHEN) > 0)
                 */) {

                    String errorMessage = Messages.getString("Node.errorMessage2"); //$NON-NLS-1$
                    Problems.add(ProblemStatus.ERROR, this, errorMessage);
                }
            }

            if (isHL7Output()) {
                if (getIncomingConnections(EConnectionType.FLOW_MERGE).size() <= 0) {
                    String errorMessage = Messages.getString("Node.hl7HaveNoMergeLink"); //$NON-NLS-1$
                    Problems.add(ProblemStatus.ERROR, this, errorMessage);
                } else {
                    List<Map<String, String>> maps = (List<Map<String, String>>) ElementParameterParser.getObjectValue(this,
                            "__SCHEMAS__"); //$NON-NLS-1$
                    List<IMetadataTable> tables = this.getMetadataList();
                    for (IConnection connection : getIncomingConnections(EConnectionType.FLOW_MERGE)) {
                        IMetadataTable metadataTable = connection.getMetadataTable();
                        // metadataTable.setLabel(connection.getUniqueName());
                        // String metadataTableName = metadataTable.getLabel();
                        String rowName = connection.getUniqueName();
                        String schemaName = null;
                        if (maps != null) {
                            for (Map<String, String> map : maps) {
                                if (map.containsValue(rowName)) {
                                    if (map.get("PARENT_ROW") != null && map.get("PARENT_ROW").equals(rowName)) { //$NON-NLS-1$ //$NON-NLS-2$
                                        schemaName = map.get("SCHEMA"); //$NON-NLS-1$
                                        // int first = schemaName.indexOf("_"); //$NON-NLS-1$
                                        // int second = schemaName.lastIndexOf("_"); //$NON-NLS-1$
                                        // if (first > 0 && first < second) {
                                        // schemaName = schemaName.substring(first + 1, second);
                                        break;
                                        // }
                                    }
                                }
                            }
                        }
                        for (IMetadataTable nodeTable : tables) {
                            if (nodeTable.getTableName() != null && nodeTable.getTableName().equals(schemaName)) {
                                if (metadataTable != null
                                        && !metadataTable.sameMetadataAs(nodeTable, IMetadataColumn.OPTIONS_NONE)) {
                                    String errorMessage = Messages.getString("Node.schemaNotSynchronized"); //$NON-NLS-1$
                                    Problems.add(ProblemStatus.ERROR, this, errorMessage);
                                }
                            }
                        }
                    }
                }
            }
            // check if we have circle for current node
            if (checkNodeCircle(this)) {
                String errorMessage = Messages.getString("Node.notFormedLoop"); //$NON-NLS-1$
                Problems.add(ProblemStatus.WARNING, this, errorMessage);
            }
        }
        int tableOutLinkNum = 0;
        int tableRefOutLinkNum = 0;
        List<? extends IConnection> tableOutLinks = this.getOutgoingConnections(EConnectionType.TABLE);
        if (null != tableOutLinks) {
            tableOutLinkNum = tableOutLinks.size();
        }
        List<? extends IConnection> tableRefOutLinks = this.getOutgoingConnections(EConnectionType.TABLE_REF);
        if (null != tableRefOutLinks) {
            tableRefOutLinkNum = tableRefOutLinks.size();
        }
        int tableInLinkNum = 0;
        int tableRefInLinkNum = 0;
        List<? extends IConnection> tableInLinks = this.getIncomingConnections(EConnectionType.TABLE);
        if (null != tableInLinks) {
            tableInLinkNum = tableInLinks.size();
        }
        List<? extends IConnection> tableRefInLinks = this.getIncomingConnections(EConnectionType.TABLE_REF);
        if (null != tableRefInLinks) {
            tableRefInLinkNum = tableRefInLinks.size();
        }
        int jobletBuildConnectorNum = 0;
        // means this Node is an ELTDBMap, and it can use subQuery, so the check maybe a little different
        boolean subQueryMode = true;
        INodeConnector tableRefConnector = getConnectorFromType(EConnectionType.TABLE_REF);
        if (null != tableRefConnector) {
            if (tableRefConnector.getMaxLinkOutput() == 0) {
                subQueryMode = false;
            }
        }

        for (INodeConnector nodeConnector : listConnector) {
            if (!nodeConnector.getDefaultConnectionType().hasConnectionCategory(IConnectionCategory.USE_HASH)
                    && nodeConnector.getDefaultConnectionType() != EConnectionType.FLOW_MERGE) {
                boolean needCheckOutput = true;
                boolean needCheckInput = true;
                int nbMaxOut;
                nbMaxOut = nodeConnector.getMaxLinkOutput();
                int nbMaxIn;
                nbMaxIn = nodeConnector.getMaxLinkInput();
                int nbMinOut;
                nbMinOut = nodeConnector.getMinLinkOutput();
                int nbMinIn;
                nbMinIn = nodeConnector.getMinLinkInput();
                int curLinkOut;
                curLinkOut = nodeConnector.getCurLinkNbOutput();
                int curLinkIn;
                curLinkIn = nodeConnector.getCurLinkNbInput();
                String typeName = nodeConnector.getMenuName();

                boolean isCheckingTableLink = false;

                if (subQueryMode) {
                    if (nodeConnector.getDefaultConnectionType() == EConnectionType.TABLE) {
                        if (0 < tableRefOutLinkNum) {
                            needCheckOutput = false;
                            if (0 < tableOutLinkNum) {
                                Object[] errorParams = new String[] { EConnectionType.TABLE.getDefaultMenuName(),
                                        EConnectionType.TABLE_REF.getDefaultMenuName() };
                                String errorMessage = Messages.getString("Node.canNotMultiKindTableOutput", errorParams); //$NON-NLS-1$
                                Problems.add(ProblemStatus.ERROR, this, errorMessage);
                            }
                        }
                        if (0 < tableRefInLinkNum) {
                            needCheckInput = false;
                        }
                        isCheckingTableLink = true;
                        // A subjob can not have multi start node of ELTDBMap
                        if (this.getComponent().getName().endsWith("Map") && tableOutLinks != null) { //$NON-NLS-1$
                            for (IConnection iconn : tableOutLinks) {
                                INode newTarget = iconn.getTarget();
                                if (newTarget.isELTComponent() && newTarget.getComponent().getName().endsWith("Map")) { //$NON-NLS-1$
                                    Object[] errorParams = new String[] { this.getLabel(), newTarget.getLabel(),
                                            EConnectionType.TABLE_REF.getDefaultMenuName() };
                                    String errorMessage = Messages.getString("Node.ELTDBMap.canNotHaveMultiStartNode", //$NON-NLS-1$
                                            errorParams);
                                    Problems.add(ProblemStatus.ERROR, this, errorMessage);
                                }
                            }
                        }
                    } else if (nodeConnector.getDefaultConnectionType() == EConnectionType.TABLE_REF) {
                        if (0 < tableOutLinkNum) {
                            needCheckOutput = false;
                            if (0 < tableRefOutLinkNum) {
                                Object[] errorParams = new String[] { EConnectionType.TABLE.getDefaultMenuName(),
                                        EConnectionType.TABLE_REF.getDefaultMenuName() };
                                String errorMessage = Messages.getString("Node.canNotMultiKindTableOutput", errorParams); //$NON-NLS-1$
                                Problems.add(ProblemStatus.ERROR, this, errorMessage);
                            }
                        }
                        if (0 < tableInLinkNum) {
                            needCheckInput = false;
                        }
                        isCheckingTableLink = true;
                    }
                }

                if (isCheckingTableLink) {
                    typeName = EConnectionType.TABLE.getDefaultMenuName() + "/" + EConnectionType.TABLE_REF.getDefaultMenuName(); //$NON-NLS-1$
                }

                if (nodeConnector.getDefaultConnectionType() == EConnectionType.FLOW_MAIN) {
                    typeName = "Row"; //$NON-NLS-1$
                    if (isJoblet) {
                        if (nodeConnector.isBuiltIn()) {
                            jobletBuildConnectorNum++;
                        }
                        continue;
                    }
                }
                if (needCheckOutput) {
                    if (nbMaxOut != -1) {
                        boolean notShow = !nodeConnector.isShow() && curLinkOut > 0;
                        if (notShow || curLinkOut > nbMaxOut) {
                            String errorMessage = Messages.getString("Node.tooMuchTypeOutput", typeName); //$NON-NLS-1$
                            Problems.add(ProblemStatus.WARNING, this, errorMessage);
                        }
                    }
                    if (nbMinOut != 0) {
                        if (nodeConnector.isShow() && curLinkOut < nbMinOut) {
                            String errorMessage = Messages.getString("Node.notEnoughTypeOutput", typeName); //$NON-NLS-1$
                            Problems.add(ProblemStatus.WARNING, this, errorMessage);
                        }
                    }
                }

                // ingore input warning
                if (!isJoblet) {

                    if (nbMaxIn != -1) {
                        boolean notShow = !nodeConnector.isShow() && curLinkIn > 0;
                        if (notShow || curLinkIn > nbMaxIn) {
                            String errorMessage = Messages.getString("Node.tooMuchTypeInput", typeName); //$NON-NLS-1$
                            Problems.add(ProblemStatus.WARNING, this, errorMessage);
                        }
                    }
                    if (needCheckInput) {
                        if (nbMinIn != 0) {
                            if (nodeConnector.isShow() && curLinkIn < nbMinIn) {
                                String errorMessage = Messages.getString("Node.notEnoughTypeInput", typeName); //$NON-NLS-1$
                                Problems.add(ProblemStatus.WARNING, this, errorMessage);
                            }
                        }
                    }
                }
            }
        }
        if (isJoblet) { // bug 12764
            List<IConnection> outgoingConnections = (List<IConnection>) this.getOutgoingConnections(EConnectionType.FLOW_MAIN);
            outgoingConnections.addAll(this.getOutgoingConnections(EConnectionType.FLOW_REF));
            for (IConnection con : outgoingConnections) {
                INodeConnector connector = this.getConnectorFromName(con.getConnectorName());
                if (connector == null && con instanceof Connection) { // connector is lost.
                    ((Connection) con).disconnect();
                }
            }

            String typeName = "Row"; //$NON-NLS-1$
            outgoingConnections = (List<IConnection>) this.getOutgoingConnections(EConnectionType.FLOW_MAIN);
            outgoingConnections.addAll(this.getOutgoingConnections(EConnectionType.FLOW_REF));
            if (outgoingConnections.size() > jobletBuildConnectorNum) {
                String errorMessage = Messages.getString("Node.tooMuchTypeOutput", typeName); //$NON-NLS-1$
                Problems.add(ProblemStatus.WARNING, this, errorMessage);
            } else if (outgoingConnections.size() < jobletBuildConnectorNum) {
                String errorMessage = Messages.getString("Node.notEnoughTypeOutput", typeName); //$NON-NLS-1$
                Problems.add(ProblemStatus.WARNING, this, errorMessage);
            }
        }
        
        List<IConnection> subjobLinks = (List<IConnection>) this.getIncomingConnections(EConnectionType.ON_SUBJOB_OK);
        subjobLinks.addAll(this.getIncomingConnections(EConnectionType.ON_SUBJOB_ERROR));
        if(!subjobLinks.isEmpty() && !this.getUniqueName().equals(this.getDesignSubjobStartNode().getUniqueName())){
            String errorMessage = Messages.getString("Node.notSubjobStartNode2", this.getUniqueName()); //$NON-NLS-1$
            Problems.add(ProblemStatus.WARNING, this, errorMessage);
        }
        
    }

    private static boolean checkNodeCircle(INode currentNode) {
        if (currentNode.getIncomingConnections().size() > 1) {
            List<INode> nodeList = new ArrayList<INode>();
            Set<INode> nodeSet = new HashSet<INode>();
            getAllMainConnSourceNode(currentNode, nodeList);
            for (INode node : nodeList) {
                nodeSet.add(node);
            }
            return !(nodeSet.size() == nodeList.size());
        }
        return false;
    }

    private static void getAllMainConnSourceNode(INode source, List<INode> list) {
        List<? extends IConnection> connections = source.getIncomingConnections();
        for (IConnection connection : connections) {
            if (connection.getLineStyle() != EConnectionType.FLOW_MAIN) {
                continue;
            }
            INode node = connection.getSource();
            list.add(node);
            getAllMainConnSourceNode(node, list);
        }
    }

    public boolean isSchemaSynchronized() {
        return schemaSynchronized;
    }

    private void checkSchema() {
        boolean canEditSchema = false;
        boolean noSchema = false;
        for (IElementParameter param : this.getElementParameters()) {
            if (param.isShow(getElementParameters()) && (param.getFieldType().equals(EParameterFieldType.SCHEMA_TYPE)
                    || param.getFieldType().equals(EParameterFieldType.SCHEMA_REFERENCE))) {
                canEditSchema = true;
                break;
            }
        }
        INodeConnector mainConnector;
        if (isELTComponent()) {
            mainConnector = this.getConnectorFromType(EConnectionType.TABLE);
        } else {
            mainConnector = this.getConnectorFromType(EConnectionType.FLOW_MAIN);
        }
        List<INodeConnector> mainConnectors = this.getConnectorsFromType(EConnectionType.FLOW_MAIN);
        if (mainConnector != null && !isExternalNode()) {
            IMetadataTable table = getMetadataFromConnector(mainConnector.getName());

            if (canEditSchema && table != null) {
                if (LanguageManager.getCurrentLanguage() == ECodeLanguage.JAVA) {
                    for (IMetadataColumn column : table.getListColumns()) {
                        if (column.isCustom()) {
                            continue;
                        }
                        String value = column.getPattern();
                        String typevalue = column.getTalendType();
                        if (JavaTypesManager.DATE.getId().equals(typevalue) || PerlTypesManager.DATE.equals(typevalue)) {
                            if (value == null || "".equals(value)) { //$NON-NLS-1$
                                String errorMessage = Messages.getString("Node.PatterErrorMessage"); //$NON-NLS-1$
                                Problems.add(ProblemStatus.WARNING, this, errorMessage);
                                noSchema = true;
                            }
                        }
                    }
                }
                // display error in the tRunJob Component that the schema is empty when a output main link exists.
                if ((mainConnector.getMaxLinkInput() == 0 || ("tRunJob".equals(getComponent().getName()) //$NON-NLS-1$
                        && (mainConnector.getMaxLinkInput() != 0)
                        && getCurrentActiveLinksNbOutput(EConnectionType.FLOW_MAIN) > 0))
                        && (mainConnector.getMaxLinkOutput() != 0)) {
                    if (table.getListColumns().size() == 0) {
                        String errorMessage = Messages.getString("Node.noSchemaDefined"); //$NON-NLS-1$
                        Problems.add(ProblemStatus.WARNING, this, errorMessage);
                        noSchema = true;
                    }
                }
            } else {
                if ((mainConnector.getMaxLinkInput() != 0) && (mainConnector.getMaxLinkOutput() != 0)) {
                    if (table != null && table.getListColumns().size() == 0) {
                        noSchema = true;
                    }
                }
                if (getCurrentActiveLinksNbInput(EConnectionType.FLOW_MAIN) == 0 && noSchema) {
                    if ((getCurrentActiveLinksNbOutput(EConnectionType.FLOW_MAIN) > 0)
                            || (getCurrentActiveLinksNbOutput(EConnectionType.FLOW_REF) > 0)) {
                        if (!this.getComponent().getName().equals("tSAPBapi")) { //$NON-NLS-1$
                            String errorMessage = Messages.getString("Node.outputNeedInputLink"); //$NON-NLS-1$
                            Problems.add(ProblemStatus.ERROR, this, errorMessage);
                        }
                    }
                }
            }
        }
        ICoreTisService service = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ICoreTisService.class)) {
            service = GlobalServiceRegister.getDefault().getService(ICoreTisService.class);
        }

        // test in case several Dynamic type has been set or if Dynamic is not the last type in schema
        if (mainConnector != null && !noSchema && canEditSchema) {
            if (getMetadataList() != null) {
                for (IMetadataTable meta : getMetadataList()) {
                    // count how many Dynamic Type there is, normally there should be only one.
                    if (meta == null) {
                        continue;
                    }
                    int nbDynamic = 0;
                    for (IMetadataColumn col : meta.getListColumns()) {
                        if (col.getTalendType() != null && col.getTalendType().equals("id_Dynamic")) { //$NON-NLS-1$
                            nbDynamic++;
                        }
                    }

                    if (!isJoblet()) {
                        if (nbDynamic > 1) {
                            String errorMessage = Messages.getString("Node.onlyOneDynamicPerSchema"); //$NON-NLS-1$
                            Problems.add(ProblemStatus.ERROR, this, errorMessage);
                        }
                        if (nbDynamic > 0 && service == null) {
                            String errorMessage = Messages.getString("Node.dynamicNotSupported"); //$NON-NLS-1$
                            Problems.add(ProblemStatus.ERROR, this, errorMessage);
                        }
                        if (nbDynamic > 0 && service != null) {
                            if (!service.isSupportDynamicType(this)) {
                                String errorMessage = Messages.getString("Node.componentDoesntSupportDynamic"); //$NON-NLS-1$
                                Problems.add(ProblemStatus.ERROR, this, errorMessage);
                            }
                        }
                    }

                }
            }
        }

        // test empty schema in built in connections (several outputs with
        // different schema)
        if (mainConnector != null && !noSchema && (!canEditSchema || isExternalNode())) {
            if (mainConnector.isMultiSchema()) {
                if (getMetadataList() != null) {
                    for (IMetadataTable meta : getMetadataList()) {
                        String componentName = this.getComponent().getName();
                        if (!componentName.equals("tRESTRequest") && meta.getListColumns().size() == 0 //$NON-NLS-1$
                                && !isCheckMultiSchemaForMSField() && checkSchemaForEBCDIC(meta)) {
                            String tableLabel = meta.getTableName();
                            if (meta.getLabel() != null) {
                                tableLabel = meta.getLabel();
                            }
                            String errorMessage = Messages.getString("Node.noColumnDefined", tableLabel); //$NON-NLS-1$
                            Problems.add(ProblemStatus.ERROR, this, errorMessage);
                        }
                    }
                }
            }
        }

        schemaSynchronized = true;

        // see bug 22089:the getConnectorFromType() method,same connector type,but different connectors.In case
        // the filterRow,the connector is not correct,we add the
        // input/output schema check on it
        if ((component.isSchemaAutoPropagated() && getComponent().getName().equals("tFilterRow")) //$NON-NLS-1$
                && (getMetadataList().size() != 0)) {
            for (INodeConnector nodeConnector : mainConnectors) {
                INodeConnector actualConnector = nodeConnector;
                if (actualConnector != null) {
                    int maxFlowInput = actualConnector.getMaxLinkInput();
                    if (maxFlowInput <= 1 || getComponent().useLookup() || isELTComponent()) {
                        IMetadataTable inputMeta = null, outputMeta = getMetadataList().get(0);
                        if ((actualConnector.getMaxLinkInput() != 0) && (actualConnector.getMaxLinkOutput() != 0)) {
                            IConnection inputConnecion = null;
                            for (IConnection connection : inputs) {
                                if (connection.isActivate() && (connection.getLineStyle().equals(EConnectionType.FLOW_MAIN)
                                        || connection.getLineStyle().equals(EConnectionType.TABLE))) {
                                    inputMeta = connection.getMetadataTable();
                                    inputConnecion = connection;
                                }
                            }
                            if (inputMeta != null) {
                                if ((!outputMeta.sameMetadataAs(inputMeta,
                                        IMetadataColumn.OPTIONS_IGNORE_KEY | IMetadataColumn.OPTIONS_IGNORE_NULLABLE
                                                | IMetadataColumn.OPTIONS_IGNORE_COMMENT | IMetadataColumn.OPTIONS_IGNORE_PATTERN
                                                | IMetadataColumn.OPTIONS_IGNORE_DBCOLUMNNAME
                                                | IMetadataColumn.OPTIONS_IGNORE_DBTYPE | IMetadataColumn.OPTIONS_IGNORE_DEFAULT
                                                | IMetadataColumn.OPTIONS_IGNORE_BIGGER_SIZE))) {
                                    if (!outputMeta.sameMetadataAs(inputMeta, IMetadataColumn.OPTIONS_NONE)
                                            && outputMeta.sameMetadataAs(inputMeta, IMetadataColumn.OPTIONS_IGNORE_LENGTH)) {
                                        String warningMessage = Messages.getString("Node.lengthDiffWarning", //$NON-NLS-1$
                                                inputConnecion.getName());
                                        Problems.add(ProblemStatus.WARNING, this, warningMessage);
                                    } else {
                                        schemaSynchronized = false;
                                        String errorMessage = Messages.getString("Node.differentFromSchemaDefined", //$NON-NLS-1$
                                                inputConnecion.getName());
                                        Problems.add(ProblemStatus.ERROR, this, errorMessage);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        // test if the columns can be checked or not
        if (component.isSchemaAutoPropagated() && (getMetadataList().size() != 0)) {
            IConnection inputConnecion = null;
            INodeConnector nodeConn = getConnectorFromName(EConnectionType.FLOW_MAIN.getName());
            if (nodeConn != null) {
                int maxFlowInput = nodeConn.getMaxLinkInput();
                // if there is one only one input maximum or if the component use a lookup, that means
                if (maxFlowInput <= 1 || getComponent().useLookup() || isELTComponent()) {
                    IMetadataTable inputMeta = null, outputMeta = getMetadataList().get(0);
                    for (IConnection connection : inputs) {
                        if (connection.isActivate() && (connection.getLineStyle().equals(EConnectionType.FLOW_MAIN)
                                || connection.getLineStyle().equals(EConnectionType.TABLE))) {
                            inputMeta = connection.getMetadataTable();
                            inputConnecion = connection;
                        }
                    }

                    if (inputMeta != null) {
                        INodeConnector connector = getConnectorFromName(outputMeta.getAttachedConnector());
                        if (connector != null
                                && ((connector.getMaxLinkInput() != 0 && connector.getMaxLinkOutput() != 0)
                                        || getComponent().getComponentType() == EComponentType.JOBLET_INPUT_OUTPUT)
                                && (!outputMeta.sameMetadataAs(inputMeta,
                                        IMetadataColumn.OPTIONS_IGNORE_KEY | IMetadataColumn.OPTIONS_IGNORE_NULLABLE
                                                | IMetadataColumn.OPTIONS_IGNORE_COMMENT | IMetadataColumn.OPTIONS_IGNORE_PATTERN
                                                | IMetadataColumn.OPTIONS_IGNORE_DBCOLUMNNAME
                                                | IMetadataColumn.OPTIONS_IGNORE_DBTYPE | IMetadataColumn.OPTIONS_IGNORE_DEFAULT
                                                | IMetadataColumn.OPTIONS_IGNORE_BIGGER_SIZE))) {
                            if (!outputMeta.sameMetadataAs(inputMeta, IMetadataColumn.OPTIONS_NONE)
                                    && outputMeta.sameMetadataAs(inputMeta, IMetadataColumn.OPTIONS_IGNORE_LENGTH)) {
                                String warningMessage = Messages.getString("Node.lengthDiffWarning", //$NON-NLS-1$
                                        inputConnecion.getName());
                                Problems.add(ProblemStatus.WARNING, this, warningMessage);
                            } else {
                                schemaSynchronized = false;
                                String errorMessage = Messages.getString("Node.differentFromSchemaDefined", //$NON-NLS-1$
                                        inputConnecion.getName());
                                Problems.add(ProblemStatus.WARNING, this, errorMessage);
                            }
                        } else if (connector != null && connector.getMaxLinkInput() != 0 && connector.getMaxLinkOutput() == 0) {
                            if (!outputMeta.sameMetadataAs(inputMeta,
                                    IMetadataColumn.OPTIONS_IGNORE_KEY | IMetadataColumn.OPTIONS_IGNORE_NULLABLE
                                            | IMetadataColumn.OPTIONS_IGNORE_COMMENT | IMetadataColumn.OPTIONS_IGNORE_PATTERN
                                            | IMetadataColumn.OPTIONS_IGNORE_DBCOLUMNNAME | IMetadataColumn.OPTIONS_IGNORE_DBTYPE
                                            | IMetadataColumn.OPTIONS_IGNORE_DEFAULT
                                            | IMetadataColumn.OPTIONS_IGNORE_BIGGER_SIZE)) {
                                schemaSynchronized = false;
                                String errorMessage = Messages.getString("Node.differentFromSchemaDefined", //$NON-NLS-1$
                                        inputConnecion.getName());
                                Problems.add(ProblemStatus.WARNING, this, errorMessage);
                            }
                        }
                    }
                    if (outputMeta != null) {
                        for (int i = 0; i < outputMeta.getListColumns().size(); i++) {
                            IMetadataColumn column = outputMeta.getListColumns().get(i);
                            String sourceType = column.getType();
                            String typevalue = column.getTalendType();
                            String currentDbmsId = outputMeta.getDbms();
                            // TDI-21862:when drag/drop a special schema onto a component,need check if this schema's
                            // dbType compatible with this component
                            if (!typevalue.equals("id_Dynamic") && currentDbmsId != null //$NON-NLS-1$
                                    && !TypesManager.checkDBType(currentDbmsId, typevalue, sourceType)) {
                                String errorMessage = "the schema's dbType not correct for this component"; //$NON-NLS-1$
                                Problems.add(ProblemStatus.WARNING, this, errorMessage);
                            }
                            // just check the case:(javaType's primitiveClass is not null,
                            // ex:int|integer,char|Character,*|*,which input is not nullable and output is nullable.
                            if (inputMeta != null && column.isNullable()) {
                                if (i < inputMeta.getListColumns().size()) {
                                    IMetadataColumn columnInput = inputMeta.getListColumns().get(i);
                                    if (!columnInput.isNullable()) {
                                        String typevalueInput = columnInput.getTalendType();
                                        JavaType javaType = JavaTypesManager.getJavaTypeFromId(typevalueInput);
                                        Class primitiveClass = javaType.getPrimitiveClass();
                                        if (StringUtils.equals(typevalue, typevalueInput) && primitiveClass != null) {
                                            String errorMessage = "the schema's nullable not correct for this component"; //$NON-NLS-1$
                                            Problems.add(ProblemStatus.WARNING, this, errorMessage);
                                        }
                                    }
                                }

                            }

                        }
                    }
                }

            } else {
                // for each schema in the component, check if for the connector there is the option INPUT_LINK_SELECTION
                // if there is, check that the schema of the link selection is the same
                for (IElementParameter param : this.getElementParameters()) {
                    if (param.isShow(getElementParameters()) && param.getFieldType().equals(EParameterFieldType.SCHEMA_TYPE)) {
                        IMetadataTable table = getMetadataFromConnector(param.getContext());
                        IElementParameter connParam = param.getChildParameters().get(EParameterName.CONNECTION.getName());
                        if (table != null && connParam != null && !StringUtils.isEmpty((String) connParam.getValue())) {
                            for (IConnection connection : inputs) {
                                if (connection.isActivate() && connection.getName().equals(connParam.getValue())) {
                                    if (!table.sameMetadataAs(connection.getMetadataTable(), IMetadataColumn.OPTIONS_IGNORE_KEY
                                            | IMetadataColumn.OPTIONS_IGNORE_NULLABLE | IMetadataColumn.OPTIONS_IGNORE_COMMENT
                                            | IMetadataColumn.OPTIONS_IGNORE_PATTERN | IMetadataColumn.OPTIONS_IGNORE_DBCOLUMNNAME
                                            | IMetadataColumn.OPTIONS_IGNORE_DBTYPE | IMetadataColumn.OPTIONS_IGNORE_DEFAULT
                                            | IMetadataColumn.OPTIONS_IGNORE_BIGGER_SIZE)) {
                                        schemaSynchronized = false;
                                        String errorMessage = Messages.getString("Node.differentFromSchemaDefined", //$NON-NLS-1$
                                                connection.getName());
                                        Problems.add(ProblemStatus.ERROR, this, errorMessage);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if ((getComponent().getComponentType() == EComponentType.JOBLET)) {
            checkJobletSchema();
        }

        if (component.useMerge() && !this.getConnectorFromType(EConnectionType.FLOW_MERGE).isMergeAllowDifferentSchema()) {
            if (getMetadataList().get(0).getListColumns().size() == 0) {
                String errorMessage = Messages.getString("Node.noSchemaDefined"); //$NON-NLS-1$
                Problems.add(ProblemStatus.ERROR, this, errorMessage);
            } else {
                // see bug 0004139: Schema check error on tUnite.
                if (inputs.size() > 0) { // avoid index out of bound exception
                    boolean firstIsMerge = inputs.get(0).getLineStyle().equals(EConnectionType.FLOW_MERGE);
                    if (firstIsMerge) {
                        IMetadataTable firstSchema = inputs.get(0).getMetadataTable();
                        if (inputs.get(0).getLineStyle().equals(EConnectionType.FLOW_MERGE)) {
                            boolean isSame = firstSchema.sameMetadataAs(getMetadataList().get(0),
                                    IMetadataColumn.OPTIONS_IGNORE_DBCOLUMNNAME | IMetadataColumn.OPTIONS_IGNORE_DEFAULT
                                            | IMetadataColumn.OPTIONS_IGNORE_COMMENT | IMetadataColumn.OPTIONS_IGNORE_DBTYPE);
                            if (!isSame) {
                                String warningMessage = Messages.getString("Node.inputLinkDifferentFromSchemaDefined", //$NON-NLS-1$
                                        getUniqueName());
                                Problems.add(ProblemStatus.WARNING, this, warningMessage);
                            }
                        }
                    }
                }
            }

            if (inputs.size() > 1) {
                boolean firstIsMerge = inputs.get(0).getLineStyle().equals(EConnectionType.FLOW_MERGE);
                if (firstIsMerge) {
                    IMetadataTable firstSchema = inputs.get(0).getMetadataTable();
                    boolean isSame = true;
                    for (int i = 1; i < inputs.size(); i++) {
                        // ignore dbtype to make the schema as same, see bug 0004961: tUnite" should unite different
                        // datastreams
                        if (!firstSchema.sameMetadataAs(inputs.get(i).getMetadataTable(),
                                IMetadataColumn.OPTIONS_IGNORE_DBCOLUMNNAME | IMetadataColumn.OPTIONS_IGNORE_DEFAULT
                                        | IMetadataColumn.OPTIONS_IGNORE_COMMENT | IMetadataColumn.OPTIONS_IGNORE_DBTYPE)) {
                            isSame = false;
                            break;
                        }
                    }
                    if (!isSame) {
                        String warningMessage = Messages.getString("Node.schemaNotSame", getUniqueName()); //$NON-NLS-1$
                        Problems.add(ProblemStatus.WARNING, this, warningMessage);
                    }
                }
            }
        }
        syncSpecialSchema();
    }

    private void checkJobletSchema() {
        if (getMetadataList().size() <= 0) {
            return;
        }
        IConnection inputConnecion = null;
        INodeConnector nodeConn = getConnectorFromName(EConnectionType.FLOW_MAIN.getName());
        if (nodeConn != null) {
            IMetadataTable inputMeta = null, outputMeta = null;
            int maxFlowInput = nodeConn.getMaxLinkInput();
            if (maxFlowInput > 1) {
                List<? extends INode> listNode = this.getComponent().getProcess().getGraphicalNodes();
                out: for (INode node : listNode) {
                    inputMeta = null;
                    outputMeta = null;
                    if ((node instanceof Node) && (this.getComponent().getProcess() instanceof IProcess2)) {
                        IElementParameter elePa = this.getElementParameter(node.getUniqueName());
                        if (elePa != null) {
                            outputMeta = getMetadataTable(elePa.getName());
                            IElementParameter elechild = elePa.getChildParameters().get("CONNECTION"); //$NON-NLS-1$
                            if (elechild != null) {
                                in: for (IConnection connection : inputs) {
                                    if (connection.isActivate() && connection.getUniqueName().equals(elechild.getValue())) {
                                        inputMeta = connection.getMetadataTable();
                                        inputConnecion = connection;
                                        break in;
                                    }
                                }
                            }
                        }

                    }

                    if (inputMeta != null && outputMeta != null) {
                        INodeConnector connector = getConnectorFromName(outputMeta.getAttachedConnector());
                        if (connector != null && connector.getMaxLinkInput() != 0) {
                            if (!outputMeta.sameMetadataAs(inputMeta, IMetadataColumn.OPTIONS_NONE)) {
                                String warningMessage = Messages.getString("Node.lengthDiffWarning", //$NON-NLS-1$
                                        inputConnecion.getName());
                                Problems.add(ProblemStatus.WARNING, this, warningMessage);
                            }
                        }
                    }
                    if (outputMeta != null) {
                        for (IMetadataColumn column : outputMeta.getListColumns()) {
                            String sourceType = column.getType();
                            String typevalue = column.getTalendType();
                            String currentDbmsId = outputMeta.getDbms();
                            if (!typevalue.equals("id_Dynamic") && currentDbmsId != null //$NON-NLS-1$
                                    && !TypesManager.checkDBType(currentDbmsId, typevalue, sourceType)) {
                                String errorMessage = "the schema's dbType not correct for this component"; //$NON-NLS-1$
                                Problems.add(ProblemStatus.WARNING, this, errorMessage);
                            }
                        }
                    }

                }

            } else {

                outputMeta = getMetadataList().get(0);
                for (IConnection connection : getIncomingConnections()) {
                    if (connection.isActivate() && (connection.getLineStyle().equals(EConnectionType.FLOW_MAIN)
                            || connection.getLineStyle().equals(EConnectionType.TABLE)
                            || connection.getLineStyle().equals(EConnectionType.TABLE_REF))) {
                        inputMeta = connection.getMetadataTable();
                        inputConnecion = connection;
                    }
                }

                if (inputMeta != null && outputMeta != null) {
                    INodeConnector connector = getConnectorFromName(outputMeta.getAttachedConnector());
                    if (connector != null && connector.getMaxLinkInput() != 0 && (!outputMeta.sameMetadataAs(inputMeta,
                            IMetadataColumn.OPTIONS_IGNORE_KEY | IMetadataColumn.OPTIONS_IGNORE_NULLABLE
                                    | IMetadataColumn.OPTIONS_IGNORE_COMMENT | IMetadataColumn.OPTIONS_IGNORE_PATTERN
                                    | IMetadataColumn.OPTIONS_IGNORE_DBCOLUMNNAME | IMetadataColumn.OPTIONS_IGNORE_DBTYPE
                                    | IMetadataColumn.OPTIONS_IGNORE_DEFAULT | IMetadataColumn.OPTIONS_IGNORE_BIGGER_SIZE))) {
                        if (!outputMeta.sameMetadataAs(inputMeta, IMetadataColumn.OPTIONS_NONE)
                                && outputMeta.sameMetadataAs(inputMeta, IMetadataColumn.OPTIONS_IGNORE_LENGTH)) {
                            String warningMessage = Messages.getString("Node.lengthDiffWarning", //$NON-NLS-1$
                                    inputConnecion.getName());
                            Problems.add(ProblemStatus.WARNING, this, warningMessage);
                        } else {
                            boolean errorStatus = false;
                            out: for (IMetadataColumn outColumn : outputMeta.getListColumns()) {
                                boolean foundInInputMeta = false;
                                for (IMetadataColumn inColumn : inputMeta.getListColumns()) {
                                    if (inColumn.sameMetacolumnAs(outColumn, IMetadataColumn.OPTIONS_IGNORE_LENGTH
                                            | IMetadataColumn.OPTIONS_IGNORE_KEY | IMetadataColumn.OPTIONS_IGNORE_NULLABLE
                                            | IMetadataColumn.OPTIONS_IGNORE_COMMENT | IMetadataColumn.OPTIONS_IGNORE_PATTERN
                                            | IMetadataColumn.OPTIONS_IGNORE_DBCOLUMNNAME | IMetadataColumn.OPTIONS_IGNORE_DBTYPE
                                            | IMetadataColumn.OPTIONS_IGNORE_PRECISION | IMetadataColumn.OPTIONS_IGNORE_DEFAULT
                                            | IMetadataColumn.OPTIONS_IGNORE_BIGGER_SIZE)) {
                                        foundInInputMeta = true;
                                        break;
                                    }
                                }
                                if (!foundInInputMeta) {
                                    errorStatus = true;
                                    break;
                                }
                            }
                            if (errorStatus) {
                                schemaSynchronized = false;
                                String errorMessage = Messages.getString("Node.differentFromSchemaDefined", //$NON-NLS-1$
                                        inputConnecion.getName());
                                Problems.add(ProblemStatus.ERROR, this, errorMessage);
                            } else {
                                schemaSynchronized = false;
                                String errorMessage = Messages.getString("Node.differentFromSchemaDefined", //$NON-NLS-1$
                                        inputConnecion.getName());
                                Problems.add(ProblemStatus.WARNING, this, errorMessage);
                            }
                        }
                    } else if (connector != null && connector.getMaxLinkInput() != 0 && connector.getMaxLinkOutput() == 0) {
                        if (!outputMeta.sameMetadataAs(inputMeta, IMetadataColumn.OPTIONS_NONE)) {
                            schemaSynchronized = false;
                            String errorMessage = Messages.getString("Node.differentFromSchemaDefined", inputConnecion.getName()); //$NON-NLS-1$
                            Problems.add(ProblemStatus.WARNING, this, errorMessage);
                        }
                    }
                }

                if (outputMeta != null) {
                    for (IMetadataColumn column : outputMeta.getListColumns()) {
                        String sourceType = column.getType();
                        String typevalue = column.getTalendType();
                        String currentDbmsId = outputMeta.getDbms();
                        if (!typevalue.equals("id_Dynamic") && currentDbmsId != null //$NON-NLS-1$
                                && !TypesManager.checkDBType(currentDbmsId, typevalue, sourceType)) {
                            String errorMessage = "the schema's dbType not correct for this component"; //$NON-NLS-1$
                            Problems.add(ProblemStatus.WARNING, this, errorMessage);
                        }
                    }
                }
            }

        } else {
            // for each schema in the component, check if for the connector there is the option INPUT_LINK_SELECTION
            // if there is, check that the schema of the link selection is the same
            for (IElementParameter param : this.getElementParameters()) {
                if (param.isShow(getElementParameters()) && (param.getFieldType().equals(EParameterFieldType.SCHEMA_TYPE)
                        || param.getFieldType().equals(EParameterFieldType.SCHEMA_REFERENCE))) {
                    IMetadataTable table = getMetadataFromConnector(param.getContext());
                    IElementParameter connParam = param.getChildParameters().get(EParameterName.CONNECTION.getName());
                    if (table != null && connParam != null && !StringUtils.isEmpty((String) connParam.getValue())) {
                        for (IConnection connection : inputs) {
                            if (connection.isActivate() && connection.getName().equals(connParam.getValue())) {

                                if (!table.sameMetadataAs(connection.getMetadataTable(), IMetadataColumn.OPTIONS_NONE)) {
                                    schemaSynchronized = false;
                                    String warningMessage = Messages.getString("Node.lengthDiffWarning", //$NON-NLS-1$
                                            connection.getName());
                                    Problems.add(ProblemStatus.WARNING, this, warningMessage);
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    private void syncSpecialSchema() {
        if (!isSchemaSynchronized()) { // bug 11856
            IElementParameter synchronizeSchemaParam = getElementParameter(EParameterName.NOT_SYNCHRONIZED_SCHEMA.getName());
            if (synchronizeSchemaParam != null) {
                Command cmd = SynchronizeSchemaHelper.createCommand(this, synchronizeSchemaParam);
                boolean executed = false;
                if (process != null && process instanceof IGEFProcess) {
                    IDesignerCoreUIService designerCoreUIService = CoreUIPlugin.getDefault().getDesignerCoreUIService();
                    if (designerCoreUIService != null) {
                        executed = designerCoreUIService.executeCommand((IGEFProcess) process, cmd);
                    }
                }
                if (!executed) {
                    cmd.execute();
                }

            }
        }
    }

    @Override
    public void checkAndRefreshNode() {
        Problems.clearAll(this);
        checkNode();
        Problems.refreshOneNodeStatus(this);
        Problems.refreshProblemTreeView();
    }

    @Override
    public void checkNode() {
        if (isActivate()) {
            checkParameters();
            checkSchema();
            checkLinks();
            checkModules();
            checkMultiComponents();
            checkStartLinks();
            checkParallelizeStates();
            checkWindowStates();

            // TDI-21298
            checkHasMultiPrejobOrPostJobComponents();

            // TDI-25573
            checkTRunjobwithMRProcess();

            checkMultipleTRunjobVersion();

            checkNodeProblems();

            checkDependencyLibraries();
            new TRunjobUtil().checkTRunjobRecursiveLoop(this);

            // feature 2,add a new extension point to intercept the validation action for Uniserv
            List<ICheckNodesService> checkNodeServices = CheckNodeManager.getCheckNodesService();
            for (ICheckNodesService checkService : checkNodeServices) {
                checkService.checkNode(this);
            }

            // init external node firstly by method getExternalNode
            IExternalNode iExternalNode = getExternalNode();
            if (iExternalNode != null) {
                List<Problem> problems = iExternalNode.getProblems();
                if (problems != null) {
                    for (Problem current : problems) {
                        current.setElement(this);
                        Problems.add(current);
                    }
                }
            }

        }
    }

    /*
     * Includes new custom dependency libraries into bundle classpath by default
     */
    private void checkDependencyLibraries() {
        if (getComponent() != null && "cMQConnectionFactory".equals(getComponent().getName())) {

            Map<Object, Object> additionalProperties = process.getAdditionalProperties();
            if (additionalProperties == null) {
                return;
            }

            IElementParameter param = getElementParameter("OTHER_DRIVER_JAR");
            if (param == null) {
                return;
            }

            Object stored = additionalProperties.get(Constants.BUNDLE_CLASSPATH);
            if (stored == null) {
                return;
            }

            List<String> bundleClassPath = new ArrayList<>(Arrays.asList(stored.toString().split(",")));

            List<String> currentLibs = new ArrayList<>();

            for (Map m : (List<Map>) param.getValue()) {
                currentLibs.add((String) m.get("JAR_NAME"));
            }

            if (previousCustomLibs != null) {
                // 1. Remove all elements that are not in currentLibs from bundleClassPath
                for (String libName : previousCustomLibs) {
                    if (!currentLibs.contains(libName)) {
                        bundleClassPath.remove(libName);
                    }
                }

                // 2. Add to bundleClassPath all the elements that are in currentLibs, but not in previousCustomLibs
                for (String libName : currentLibs) {
                    if (!previousCustomLibs.contains(libName)) {
                        bundleClassPath.add(libName);
                    }
                }
            }

            previousCustomLibs = currentLibs;

            StringBuilder sb = new StringBuilder();
            for (String s : bundleClassPath) {
                sb.append(s).append(",");
            }

            additionalProperties.put(Constants.BUNDLE_CLASSPATH, sb.substring(0, sb.length() - 1));
        }
    }

    private void checkNodeProblems() {
        BundleContext bc = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        Collection<ServiceReference<NodeProblem>> nodeProblems = Collections.EMPTY_LIST;
        try {
            nodeProblems = bc.getServiceReferences(NodeProblem.class, null);
        } catch (InvalidSyntaxException e) {
            CommonExceptionHandler.process(e);
        }
        for (ServiceReference<NodeProblem> sr : nodeProblems) {
            NodeProblem np = bc.getService(sr);
            if (np != null && np.needsCheck(this)) {
                np.check(this);
            }
        }
    }

    /**
     *
     * DOC cmeng Comment method "checkELTTableReference".
     *
     * @return true >> is a ELTTable Reference, and set reference true<br>
     * false >> is not a ELTTable Reference, and set reference false
     */
    private boolean checkELTTableReference() {
        boolean isReference = false;
        // check reference
        if (this.isELTComponent() && this.getComponent().getName().endsWith("Map")) { //$NON-NLS-1$
            List<? extends IConnection> tableRefs = this.getOutgoingConnections(EConnectionType.TABLE_REF);
            if (tableRefs != null && 0 < tableRefs.size()) {
                isReference = true;
            }
            this.setPropertyValue(EParameterName.UPDATE_COMPONENTS.getName(), Boolean.TRUE);
        }
        return isReference;
    }

    /**
     * This methods adds a {@link org.talend.core.model.process.Problem} to the tRunJob if the box "Use an indenpendent
     * process" is not checked for a target Big Data job. DOC rdubois Comment method "checkTRunjobwithMRProcess".
     */
    private void checkTRunjobwithMRProcess() {
        // check tRunJob
        if (getComponent() != null && "tRunJob".equals(getComponent().getName())) { //$NON-NLS-1$ ;
            boolean targetIsBigdata = false;
            String bigDataType = "Batch"; //$NON-NLS-1$
            try {
                boolean isStormServiceRegistered = GlobalServiceRegister.getDefault()
                        .isServiceRegistered(IStormProcessService.class);
                boolean isMRServiceRegistered = GlobalServiceRegister.getDefault().isServiceRegistered(IMRProcessService.class);
                if (isStormServiceRegistered || isMRServiceRegistered) {
                    IElementParameter elementParameter = getElementParameter("PROCESS:PROCESS_TYPE_PROCESS"); //$NON-NLS-1$ ;
                    if (elementParameter != null) {
                        Object value = elementParameter.getValue();
                        if (value != null && !"".equals(value)) { //$NON-NLS-1$ ;
                            IRepositoryViewObject lastVersion;
                            lastVersion = DesignerPlugin.getDefault().getRepositoryService().getProxyRepositoryFactory()
                                    .getLastVersion(value.toString());
                            if (lastVersion != null) {
                                if (isMRServiceRegistered) {
                                    if (GlobalServiceRegister.getDefault()
                                            .getService(IMRProcessService.class)
                                                    .isMapReduceItem(lastVersion.getProperty().getItem())) {
                                        targetIsBigdata = true;
                                        bigDataType = "Batch"; //$NON-NLS-1$
                                    }
                                }
                                if (isStormServiceRegistered) {
                                    if (GlobalServiceRegister.getDefault()
                                            .getService(IStormProcessService.class)
                                                    .isStormItem(lastVersion.getProperty().getItem())) {
                                        targetIsBigdata = true;
                                        bigDataType = "Streaming"; //$NON-NLS-1$
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }

            if (targetIsBigdata) {
                IElementParameter indepedentElement = getElementParameter("USE_INDEPENDENT_PROCESS"); //$NON-NLS-1$ ;
                if (indepedentElement != null) {
                    if (!indepedentElement.isShow(getElementParameters())
                            || !Boolean.valueOf(String.valueOf(indepedentElement.getValue()))) {
                        String message = Messages.getString("Node.checkTRunjobwithMRProcess", indepedentElement.getDisplayName(), //$NON-NLS-1$
                                bigDataType); // ;
                        Problems.add(ProblemStatus.ERROR, this, message);
                    }
                }
            }
        }
    }

    /**
     *
     * DOC jding Comment method "checkMultipleTRunjobVersion".For a job,can't exist multiple same subjob with different
     * version
     */
    private void checkMultipleTRunjobVersion() {
        if (getComponent() != null && "tRunJob".equals(getComponent().getName())) {
            List<? extends INode> allNodes = process.getGraphicalNodes();
            IElementParameter thisElement = this.getElementParameter(EParameterName.PROCESS.getName());
            // at first, when create a new tRunJob, this process is empty,it will have value after second call
            if (StringUtils.isBlank(thisElement.getValue().toString())) {
                return;
            }
            Map<String, IElementParameter> childParameters = thisElement.getChildParameters();
            Object thisVersion = childParameters.get(EParameterName.PROCESS_TYPE_VERSION.getName()).getValue();
            String lastVersion = null;
            try {
                IRepositoryViewObject subprocess = null;
                for (ERepositoryObjectType type : ERepositoryObjectType.getAllTypesOfProcess()) {
                    subprocess = ProxyRepositoryFactory.getInstance().getLastVersion(
                            childParameters.get(EParameterName.PROCESS_TYPE_PROCESS.getName()).getValue().toString(), "", type);
                    if (subprocess != null) {
                        break;
                    }
                }
                if (subprocess == null) {
                    return;
                }
                lastVersion = subprocess.getProperty().getVersion();
            } catch (PersistenceException e) {
                CommonExceptionHandler.process(e);
            }

            if ("Latest".equals(thisVersion)) {
                thisVersion = lastVersion;
            }
            for (INode node : allNodes) {
                if (node instanceof Node && "tRunJob".equals(node.getComponent().getName())) {
                    if (this.getUniqueName().equals(node.getUniqueName())) {
                        continue;
                    }
                    IElementParameter element = node.getElementParameter(EParameterName.PROCESS.getName());
                    Object version = element.getChildParameters().get(EParameterName.PROCESS_TYPE_VERSION.getName()).getValue();
                    if ("Latest".equals(version)) {
                        version = lastVersion;
                    }
                    if (thisElement.getValue().equals(element.getValue())) {
                        if (!thisVersion.equals(version)) {
                            Problems.add(ProblemStatus.ERROR, this, Messages.getString("Node.checkMultipleTRunjobVersion"));
                        } else {
                            Problems.clearAll((Node) node);
                            Problems.refreshOneNodeStatus(node);
                            Problems.refreshProblemTreeView();
                        }
                    }

                }
            }
        }

    }
    

    /**
     * DOC xye Comment method "checkParallelizeStates".
     */
    private void checkParallelizeStates() {
        // see feature 5027
        Boolean parallelEnable = false;
        IElementParameter enableParallelizeParameter = getElementParameter(EParameterName.PARALLELIZE.getName());
        if (enableParallelizeParameter == null) {
            return;
        } else {
            parallelEnable = (Boolean) enableParallelizeParameter.getValue();
            if (parallelEnable) {

                // for bug TDI-33344.
                for (IConnection connection : getOutgoingConnections()) {
                    if (connection.getLineStyle().hasConnectionCategory(IConnectionCategory.FLOW)) {
                        Problems.add(ProblemStatus.ERROR, this, Messages.getString("ParallelExecutionAction.noOutputLink")); //$NON-NLS-1$
                    }
                }

                removeStatus(Process.PARALLEL_STATUS);
                addStatus(Process.PARALLEL_STATUS);
            } else {
                removeStatus(Process.PARALLEL_STATUS);
            }
        }
    }

    private void checkWindowStates() {
        // see feature 5027
        Boolean isThereATWindow = false;

        Node node = null;
        if (nodeContainer != null) {
            node = nodeContainer.getNode();
            if (node != null) {
                isThereATWindow = true;
            }
        }

        if (!isThereATWindow) {
            return;
        }

        IElementParameter windowDuration = node.getElementParameter(EParameterName.WINDOW_DURATION.getName());
        if (windowDuration == null) {
            removeStatus(Process.WINDOW_STATUS);
            return;
        } else {
            removeStatus(Process.WINDOW_STATUS);
            addStatus(Process.WINDOW_STATUS);
        }
    }

    @Override
    public IComponent getComponent() {
        return this.component;
    }

    @Override
    public void setComponent(IComponent component) {
        this.delegateComponent = UnifiedComponentUtil.getDelegateComponent(component);
        this.component = UnifiedComponentUtil.getEmfComponent(this, component);
    }

    public boolean canModifySchema() {
        boolean canModifySchema = false;
        List<? extends IElementParameter> listParam = this.getElementParameters();
        for (int i = 0; i < listParam.size(); i++) {
            IElementParameter param = listParam.get(i);
            if (param.isShow(listParam)) {
                if (param.getFieldType().equals(EParameterFieldType.SCHEMA_TYPE)
                        || param.getFieldType().equals(EParameterFieldType.SCHEMA_REFERENCE)) {
                    canModifySchema = true;
                }
            }
        }
        return canModifySchema;
    }

    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(getUniqueName() + " - "); //$NON-NLS-1$
        buff.append(" status(start=" + isStart() + ", subProcessStart=" + isSubProcessStart() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        buff.append(Messages.getString("Node.input")); //$NON-NLS-1$
        for (int i = 0; i < inputs.size(); i++) {
            buff.append(inputs.get(i).getName());
            if (i < (inputs.size() - 1)) {
                buff.append(","); //$NON-NLS-1$
            }
        }
        buff.append(") "); //$NON-NLS-1$
        buff.append(Messages.getString("Node.output")); //$NON-NLS-1$
        for (int i = 0; i < outputs.size(); i++) {
            buff.append(outputs.get(i).getName());
            if (i < (outputs.size() - 1)) {
                buff.append(","); //$NON-NLS-1$
            }
        }
        buff.append(")"); //$NON-NLS-1$
        return buff.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.INode#renameMetadataColumnName(java.lang.String, java.lang.String,
     * java.lang.String)
     */
    @Override
    public void metadataInputChanged(IODataComponent dataComponent, String connectionToApply) {
        log.trace("InputChanged : Node=" + this + ", IOData=[" + dataComponent + "] on " + connectionToApply); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        if (externalNode != null) {
            externalNode.metadataInputChanged(dataComponent, connectionToApply);
        }
    }

    @Override
    public void metadataOutputChanged(IODataComponent dataComponent, String connectionToApply) {
        log.trace("OutputChanged : Node=" + this + ", IOData=[" + dataComponent + "] on " + connectionToApply); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        if (externalNode != null) {
            externalNode.metadataOutputChanged(dataComponent, connectionToApply);
        }
    }

    @Override
    public boolean isELTComponent() {
        return getComponent().getOriginalFamilyName().startsWith("ELT"); //$NON-NLS-1$
    }

    public boolean isELTMapComponent() {
        if (isELTComponent()) {
            return getComponent().getName().endsWith("Map");//$NON-NLS-1$
        }
        return false;
    }

    @Override
    public boolean isFileScaleComponent() {
        return getComponent().getOriginalFamilyName().equals("FileScale"); //$NON-NLS-1$
    }

    public boolean isHL7Output() {
        return getComponent().getName().equals("tHL7Output"); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.INode#isThereLinkWithHash()
     */
    @Override
    public boolean isThereLinkWithHash() {
        return process.isThereLinkWithHash(this);
    }

    @Override
    public List<? extends IConnection> getOutgoingSortedConnections() {
        return org.talend.core.model.utils.NodeUtil.getOutgoingSortedConnections(this);
    }

    @Override
    public List<? extends IConnection> getMainOutgoingConnections() {
        return org.talend.core.model.utils.NodeUtil.getMainOutgoingConnections(this);
    }

    @Override
    public List<? extends IConnection> getOutgoingConnections(EConnectionType connectionType) {
        return org.talend.core.model.utils.NodeUtil.getOutgoingConnections(this, connectionType);
    }

    @Override
    public List<? extends IConnection> getOutgoingConnections(String connectorName) {
        return org.talend.core.model.utils.NodeUtil.getOutgoingConnections(this, connectorName);
    }

    @Override
    public void renameData(String oldName, String newName) {
        if (oldName.equals(newName)) {
            return;
        }
        if (isExternalNode()) {
            getExternalNode().renameData(oldName, newName);
            return;
        }

        UpgradeElementHelper.renameData(this, oldName, newName);
    }

    @Override
    public boolean useData(String name) {
        if (isExternalNode()) {
            return getExternalNode().useData(name);
        }
        return UpgradeElementHelper.isUseData(this, name);
    }

    @Override
    public boolean isThereLinkWithMerge() {
        return !getLinkedMergeInfo().isEmpty();
    }

    @Override
    public Map<INode, Integer> getLinkedMergeInfo() {
        return NodeUtil.getLinkedMergeInfo(this);
    }

    @Override
    public List<? extends IConnection> getIncomingConnections(EConnectionType connectionType) {
        return org.talend.core.model.utils.NodeUtil.getIncomingConnections(this, connectionType);
    }

    public List<? extends IConnection> getIncomingConnections(String connectorName) {
        return org.talend.core.model.utils.NodeUtil.getIncomingConnections(this, connectorName);
    }

    /**
     * Getter for size.
     *
     * @return the size
     */
    @Override
    public Dimension getSize() {
        if (size == null) {
            size = new Dimension();
            if (getIcon32() != null) {
                ImageData data = getIcon32().getImageData();
                size.height = data.height;
                size.width = data.width;
            } else {
                size.height = DEFAULT_SIZE;
                size.width = DEFAULT_SIZE;
            }
        }
        return size;
    }

    /**
     * Sets the size.
     *
     * @param size the size to set
     */
    @Override
    public void setSize(Dimension size) {
        this.size = size;
        firePropertyChange(SIZE, null, null);
    }

    /**
     * Getter for listConnector.
     *
     * @return the listConnector
     */
    @Override
    public List<? extends INodeConnector> getListConnector() {
        return listConnector;
    }

    public void setListConnector(List<? extends INodeConnector> listConnector) {
        this.listConnector = listConnector;
    }

    private boolean isELTSAPMapComponent() {
        return "tELTSAPMap".equals(this.getComponent().getName());
    }

    /**
     * Test if the current node can be the start of the job not.
     *
     * @return
     */
    @Override
    public boolean checkIfCanBeStart() {
        // tELTSAPMap component is more like a input component than ELT component as it output a flow stream.
        if (isELTSAPMapComponent()) {
            if (!isThereConditionLink() && isOnMainBranch()) {
                return true;
            }

            return false;
        } else if (isELTComponent()) {
            if (this.checkELTTableReference()) {
                return false;
            }

            return !isThereConditionLink();
        } else {
            boolean canBeStart = false;
            boolean isActivatedConnection = false;
            for (int j = 0; j < getIncomingConnections().size() && !isActivatedConnection; j++) {
                IConnection connection = getIncomingConnections().get(j);
                // connection that will generate a hash file are not
                // considered as activated for this test.
                if (connection.isActivate() && !connection.getLineStyle().hasConnectionCategory(IConnectionCategory.USE_HASH)) {
                    isActivatedConnection = true;
                }
            }
            boolean isOnMainBranch = isOnMainBranch();
            if (!isActivatedConnection) {
                if (!getProcess().isThereLinkWithHash(this) && isOnMainBranch) {
                    canBeStart = true;
                }
            } else {
                // TODO remove it as this expression will be always false
                if (getIncomingConnections().size() == 0 && isOnMainBranch) {
                    if (!getProcess().isThereLinkWithHash(this)) {
                        canBeStart = true;
                    }
                }
            }
            return canBeStart;
        }
    }

    private boolean isOnMainBranch() {
        boolean isOnMainBranch = true;
        Map<INode, Integer> mergeInfo = getLinkedMergeInfo();
        for (INode node : mergeInfo.keySet()) {
            if (mergeInfo.get(node) != 1) {
                isOnMainBranch = false;
            }
        }
        return isOnMainBranch;
    }

    private boolean isThereConditionLink() {
        // is there condition link, then can't set the start.
        boolean isThereConditionLink = false;
        for (int j = 0; j < getIncomingConnections().size() && !isThereConditionLink; j++) {
            IConnection connection = getIncomingConnections().get(j);
            if (connection.isActivate() && connection.getLineStyle().hasConnectionCategory(IConnectionCategory.DEPENDENCY)) {
                isThereConditionLink = true;
            }
        }
        return isThereConditionLink;
    }

    /**
     * yzhang Comment method "setConnectionName".
     *
     * @param name
     */
    public void setConnectionName(String name) {
        this.connectionName = name.replaceAll("\"", "").replaceAll(" ", ""); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        if (this.isUpdate) {
            return;
        }
        firePropertyChange(EParameterName.CONNECTION_FORMAT.getName(), null, this.connectionName);
    }

    /**
     * yzhang Comment method "getConnectionName".
     *
     * @param name
     * @return
     */
    public String getConnectionName() {
        return this.connectionName;
    }

    private String oldUniqueName;

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.INode#reloadComponent(org.talend.core.model.components.IComponent,
     * java.util.Map)
     */
    @Override
    public void reloadComponent(IComponent component, Map<String, Object> parameters, boolean isUpdate) {
        this.isUpdate = isUpdate;
        reloadingComponent = true;
        currentStatus = 0;
        oldStatus = 0;
        Object obj = parameters.get(INode.RELOAD_PARAMETER_ELEMENT_PARAMETERS);
        Map<String, Object> storeValueMap = storeValue(obj);
        if (UnifiedComponentUtil.isDelegateComponent(getDelegateComponent())) {
            setComponentProperties(null);
        }
        init(component);
        IElementParameter param = getElementParameter(EParameterName.REPOSITORY_ALLOW_AUTO_SWITCH.getName());
        if (param != null) {
            param.setValue(Boolean.TRUE);
        }
        boolean isJobletNode = false;
        if (PluginChecker.isJobLetPluginLoaded()) {
            IJobletProviderService service = GlobalServiceRegister.getDefault()
                    .getService(IJobletProviderService.class);
            if (service != null && service.isJobletComponent(this)) {
                isJobletNode = true;
            }
        }

        if (obj != null) {
            if (UnifiedComponentUtil.isDelegateComponent(getDelegateComponent())) {
                Object object = parameters.get(INode.OLD_UNIFIED_COMPONENT);
                String oldComponent = object == null ? null : object.toString();

                Object metadatObj = parameters.get(INode.SWITCH_NODE_METADATA_LIST);
                List<IMetadataTable> oldTables = null;
                if (metadatObj != null) {
                    oldTables = (List<IMetadataTable>) metadatObj;
                }
                Object connectorObj = parameters.get(INode.SWITCH_NODE_CONNECTORS);
                List<INodeConnector> oldConnectors = null;
                if (metadatObj != null) {
                    oldConnectors = (List<INodeConnector>) connectorObj;
                }

                UnifiedComponentUtil.switchComponent(this, component, oldComponent, (List<? extends IElementParameter>) obj,
                        oldTables, oldConnectors);
            } else {
                List<? extends IElementParameter> oldElementParameters = (List<? extends IElementParameter>) obj;
                for (IElementParameter sourceParam : oldElementParameters) {
                    IElementParameter targetParam = getElementParameter(sourceParam.getName());
                    // add for bug TDI-25654, each time should get the specific version or the joblet.
                    String sourceParamName = sourceParam.getName();
                    if (isJobletNode && (EParameterName.PROCESS_TYPE_VERSION.getName()).equals(sourceParamName)) {
                        Object versionObj = storeValueMap.get(sourceParamName);
                        if (versionObj != null && !versionObj.equals(sourceParam.getValue())) {
                            sourceParam.setValue(versionObj);
                        }
                    }
                    if (targetParam != null) {
                        if (sourceParam.getName().equals(EParameterName.LABEL.getName())) {
                            boolean update = false;
                            Object sourceParamObj = sourceParam.getValue();
                            if (sourceParamObj != null && StringUtils.isNotBlank(sourceParamObj.toString())
                                    && !(EComponentType.JOBLET.equals(component.getComponentType()) && isUpdate)) {
                                setPropertyValue(sourceParam.getName(), sourceParamObj);
                            } else {
                                update = true;
                            }
                            if (update) {
                                String name = null;
                                if (EComponentType.JOBLET.equals(component.getComponentType())) {
                                    name = component.getName();
                                } else {
                                    name = component.getProcess().getName();
                                }
                                setPropertyValue(sourceParam.getName(), name);
                            }
                        } else {
                            setPropertyValue(sourceParam.getName(), sourceParam.getValue());
                        }
                        if (targetParam.getFieldType() == EParameterFieldType.TABLE) {
                            targetParam.setListItemsValue(sourceParam.getListItemsValue());
                        }
                        for (String name : targetParam.getChildParameters().keySet()) {
                            IElementParameter targetChildParam = targetParam.getChildParameters().get(name);
                            IElementParameter sourceChildParam = sourceParam.getChildParameters().get(name);
                            if (sourceChildParam == null) {
                                continue;
                            }
                            String pname = sourceParam.getName() + ":" + sourceChildParam.getName();//$NON-NLS-1$
                            if (storeValueMap.get(pname) != null) {
                                setPropertyValue(pname, storeValueMap.get(pname));
                            } else {
                                setPropertyValue(pname, sourceChildParam.getValue());
                            }

                            if (targetChildParam.getFieldType() == EParameterFieldType.TABLE) {
                                targetChildParam.setListItemsValue(sourceChildParam.getListItemsValue());
                            } else if (targetChildParam.getFieldType() == EParameterFieldType.CONNECTION_LIST) {
                                if (((getPropertyValue(pname) == null || getPropertyValue(pname).toString().length() == 0))
                                        && component.getProcess() instanceof IProcess2
                                        && storeValueMap.containsKey(sourceParam.getName())
                                        && !storeValueMap.containsKey(pname)) {
                                    storeConn(sourceParam, pname);
                                }
                            }
                        }
                    }
                }
            }
        }
        obj = parameters.get(INode.RELOAD_PARAMETER_METADATA_LIST);
        if (obj != null) {
            setMetadataList((List<IMetadataTable>) obj);
        }

        obj = parameters.get(INode.RELOAD_PARAMETER_EXTERNAL_BYTES_DATA);
        if (obj != null && isExternalNode()) {
            if (obj instanceof IExternalData) {
                getExternalNode().setExternalData((IExternalData) obj);
            }
            getExternalNode().initialize();
        }
        obj = parameters.get(INode.RELOAD_PARAMETER_CONNECTORS);
        if (obj != null) {
            List<? extends INodeConnector> connectors = (List<? extends INodeConnector>) obj;
            if (isJobletNode) {
                listConnector = connectors;
            } else {
                for (INodeConnector currentConnector : listConnector) {
                    for (INodeConnector connector : connectors) {
                        if (currentConnector.getName().equals(connector.getName())) {
                            currentConnector.setCurLinkNbInput(connector.getCurLinkNbInput());
                            currentConnector.setCurLinkNbOutput(connector.getCurLinkNbOutput());
                            break;
                        }

                    }
                }
            }
        }
        // if (isJobletNode) {
        // IJobletProviderService service = (IJobletProviderService) GlobalServiceRegister.getDefault().getService(
        // IJobletProviderService.class);
        // if (service != null) {
        // service.reloadJobletProcess(this);
        // }
        // }
        reloadingComponent = false;
        this.isUpdate = false;
    }

    private Map<String, Object> storeValue(Object obj) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (obj == null) {
            return map;
        }
        List<? extends IElementParameter> oldElementParameters = (List<? extends IElementParameter>) obj;
        for (IElementParameter sourceParam : oldElementParameters) {
            map.put(sourceParam.getName(), sourceParam.getValue());

            for (String name : sourceParam.getChildParameters().keySet()) {
                IElementParameter sourceChildParam = sourceParam.getChildParameters().get(name);
                if (sourceChildParam == null) {
                    continue;
                }
                map.put(sourceParam.getName() + ":" + sourceChildParam.getName(), sourceChildParam.getValue()); //$NON-NLS-1$
            }
        }
        return map;
    }

    private void storeConn(IElementParameter sourceParam, String pname) {
        IProcess2 comProcess = (IProcess2) component.getProcess();

        if (comProcess.getProperty().getItem() instanceof JobletProcessItem) {
            JobletProcess jobletProcess = ((JobletProcessItem) comProcess.getProperty().getItem()).getJobletProcess();
            List<JobletNode> jobletNodes = jobletProcess.getJobletNodes();
            out: for (JobletNode jNode : jobletNodes) {
                if (jNode.isInput()) {
                    List list = jNode.getElementParameter();
                    in: for (Object object : list) {
                        if (object instanceof ElementParameterType) {
                            if (((ElementParameterType) object).getName().equals("UNIQUE_NAME") //$NON-NLS-1$
                                    && ((ElementParameterType) object).getValue().equals(sourceParam.getName())) {
                                EConnectionType deType = this.getConnectorFromName(sourceParam.getName())
                                        .getDefaultConnectionType();
                                if (this.getIncomingConnections(deType).size() > 0) {
                                    setPropertyValue(pname, this.getIncomingConnections(deType).get(0).getUniqueName());
                                    break out;
                                }
                                break in;
                            }
                        }
                    }
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.INode#getDesignSubjobStartNode()
     */
    @Override
    public INode getDesignSubjobStartNode() {
        return getProcessStartNode(false);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.INode#isDesignSubjobStartNode()
     */
    @Override
    public boolean isDesignSubjobStartNode() {
        return this.equals(getDesignSubjobStartNode());
    }

    /*
     * return false is ok, becase all nodes generated from virtual component are DataNode.
     *
     * @see org.talend.core.model.process.INode#isVirtualGenerateNode()
     */
    @Override
    public boolean isVirtualGenerateNode() {
        return false;
    }

    /**
     * ftang Comment method "checkStartLinks".
     */
    private void checkStartLinks() {
        boolean isFirstLinkOrder = process.getMergelinkOrder(this) > 1;
        if ((getCurrentActiveLinksNbInput(EConnectionType.ON_SUBJOB_OK) > 0
                || getCurrentActiveLinksNbInput(EConnectionType.ON_SUBJOB_ERROR) > 0) && isFirstLinkOrder) {
            String errorMessage = Messages.getString("Node.errorMessage1"); //$NON-NLS-1$
            Problems.add(ProblemStatus.ERROR, this, errorMessage);
        } else if ((getCurrentActiveLinksNbInput(EConnectionType.RUN_IF) > 0) && isFirstLinkOrder) {
            String errorMessage = Messages.getString("Node.errorMessage3"); //$NON-NLS-1$
            Problems.add(ProblemStatus.ERROR, this, errorMessage);
        } else if ((getCurrentActiveLinksNbInput(EConnectionType.ROUTE_WHEN) > 0) && isFirstLinkOrder) {
            String errorMessage = Messages.getString("Node.errorMessage5"); //$NON-NLS-1$
            Problems.add(ProblemStatus.ERROR, this, errorMessage);
        } else if ((getCurrentActiveLinksNbInput(EConnectionType.ON_COMPONENT_OK) > 0
                || getCurrentActiveLinksNbInput(EConnectionType.ON_COMPONENT_ERROR) > 0
                || getCurrentActiveLinksNbInput(EConnectionType.STARTS) > 0) && isFirstLinkOrder) {
            String errorMessage = Messages.getString("Node.errorMessage4"); //$NON-NLS-1$
            Problems.add(ProblemStatus.ERROR, this, errorMessage);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.INode#isGeneratedAsVirtualComponent()
     */
    @Override
    public boolean isGeneratedAsVirtualComponent() {
        List<IMultipleComponentManager> multipleComponentManagers = getComponent().getMultipleComponentManagers();
        return multipleComponentManagers.size() > 0;
    }

    // hywang add this method for feature 8221
    private boolean isCheckMultiSchemaForMSField() {
        boolean needMultiSchema = false;
        if (this.getElementParameter(EParameterName.COMPONENT_NAME.getName()).getValue().toString()
                .equals("tFileInputMSFieldDelimited") && this.getElementParameter("USE_MUL_SCHEMAS") != null) { //$NON-NLS-1$ //$NON-NLS-2$
            if (Boolean.parseBoolean(this.getElementParameter("USE_MUL_SCHEMAS").getValue().toString())) { //$NON-NLS-1$
                needMultiSchema = true;
            }
        }
        return needMultiSchema;
    }

    public boolean checkSchemaForEBCDIC(IMetadataTable meta) {
        boolean isCheckSchema = false;
        if (getComponent().getName().contains("EBCDIC")) { //$NON-NLS-1$
            String connector = meta.getAttachedConnector();
            IElementParameter param = getElementParameterFromField(EParameterFieldType.SCHEMA_TYPE);
            if (param == null) {
                param = getElementParameterFromField(EParameterFieldType.SCHEMA_REFERENCE);
            }
            // get the element parameter attached to the current metadata.
            // context normally should hold the connector information.
            if (param != null && StringUtils.equals(connector, param.getContext())) {
                // if we don't display the schema parameter, no need any check here.
                if (param.isShow(getElementParameters())) {
                    // if schema is displayed for ebcdic component, then we should check
                    // the content of this schema
                    isCheckSchema = true;
                }
            }
            // the normal table should check
            if (!meta.getTableName().equals(this.getUniqueName())) {
                isCheckSchema = true;
            }
        } else {
            isCheckSchema = true;
        }
        return isCheckSchema;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.INode#isUseLoopOnConditionalOutput(java.lang.String)
     */
    @Override
    public boolean isUseLoopOnConditionalOutput(String outputName) {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.INode#getUniqueShortName()
     */
    @Override
    public String getUniqueShortName() {
        // should't be call from here, should be called from something extends AbstractNode (DataNode, ExternalNode...).
        return null;
    }

    public List<INode> getNodesFromSubProcess() {
        List<INode> nodes = new ArrayList<INode>();
        List<SubjobContainer> subjobContainers = (List<SubjobContainer>) process.getSubjobContainers();
        if (subjobContainers.size() == 0) {
            process.updateSubjobContainers();
        }
        for (SubjobContainer sjContainer : subjobContainers) {
            // find in which subjobContainer is this current node
            boolean found = false;
            for (NodeContainer nContainer : sjContainer.getNodeContainers()) {
                if (nContainer.getNode().getUniqueName().equals(getUniqueName())) {
                    found = true;
                    break;
                }
            }

            // return all nodes from this sjContainer;
            if (found) {
                for (NodeContainer nContainer : sjContainer.getNodeContainers()) {
                    nodes.add(nContainer.getNode());
                }
                break;
            }
        }
        return nodes;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.INode#isSubProcessContainTraceBreakpoint()
     */
    @Override
    public boolean isSubProcessContainTraceBreakpoint() {
        boolean flag = false;

        for (IConnection connection : this.getOutgoingConnections()) {
            if (getNodesFromSubProcess().contains(connection.getSource())
                    && getNodesFromSubProcess().contains(connection.getTarget())) {
                IElementParameter param = connection.getElementParameter(EParameterName.ACTIVEBREAKPOINT.getName());
                if (param != null && Boolean.TRUE.equals(param.getValue())) {
                    flag = true;
                } else {
                    flag = connection.getTarget().isSubProcessContainTraceBreakpoint();
                }
                if (flag) {
                    break;
                }
            }
        }
        return flag;
    }

    @Override
    public Set<INode> fsComponentsInProgressBar() {
        return this.getNodeProgressBar().getIncludedNodesInProgress();
    }

    @Override
    public int getPosX() {
        return location.x;
    }

    @Override
    public int getPosY() {
        return location.y;
    }

    public boolean isNeedloadLib() {
        return needlibrary;
    }

    public void setNeedLoadLib(boolean isNeedLib) {
        // TODO Auto-generated method stub
        this.needlibrary = isNeedLib;
    }

    public boolean isStandardJoblet() {
        boolean isJoblet = false;
        if (PluginChecker.isJobLetPluginLoaded()) {
            IJobletProviderService service = GlobalServiceRegister.getDefault()
                    .getService(IJobletProviderService.class);
            if (service != null && service.isStandardJobletComponent(this)) {
                isJoblet = true;
            }
        }
        return isJoblet;
    }

    public boolean isJoblet() {
        boolean isJoblet = false;
        if (PluginChecker.isJobLetPluginLoaded()) {
            IJobletProviderService service = GlobalServiceRegister.getDefault()
                    .getService(IJobletProviderService.class);
            if (service != null && service.isJobletComponent(this)) {
                isJoblet = true;
            }
        }
        return isJoblet;
    }

    public boolean isSparkJoblet() {
        boolean isSparkJoblet = false;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ISparkJobletProviderService.class)) {
            ISparkJobletProviderService sparkJobletService = GlobalServiceRegister.getDefault()
                    .getService(ISparkJobletProviderService.class);
            if (sparkJobletService != null) {
                isSparkJoblet = sparkJobletService.isSparkJobletComponent(this);
            }
        }
        return isSparkJoblet;
    }

    public boolean isSparkStreamingJoblet() {
        boolean isSparkStreamingJoblet = false;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ISparkStreamingJobletProviderService.class)) {
            ISparkStreamingJobletProviderService sparkJobletService = GlobalServiceRegister
                    .getDefault().getService(ISparkStreamingJobletProviderService.class);
            if (sparkJobletService != null) {
                isSparkStreamingJoblet = sparkJobletService.isSparkStreamingJobletComponent(this);
            }
        }
        return isSparkStreamingJoblet;
    }

    public boolean isMapReduceStart() {
        if (!this.isActivate()) {
            return false;
        }
        if (this.getMrGroupId() == null) {
            return false;
        }
        if (this.isMapReduce() && this.getIncomingConnections().isEmpty() && this.getOutgoingConnections().isEmpty()) {
            return true;
        }
        return isMapReduceStart;
    }

    public boolean isMapReduce() {
        String sourceJobFramework = (String) ((Process) this.getProcess()).getProperty().getAdditionalProperties()
                .get(ConvertJobsUtil.FRAMEWORK);
        if (sourceJobFramework == null) {
            return false;
        }
        if (sourceJobFramework.equals(ConvertJobsUtil.MAPREDUCE_FRAMEWORK)) {
            return true;
        }
        return false;
    }

    public boolean isProgressBarNeeded() {
        boolean needBar = true;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IMRProcessService.class)) {
            IMRProcessService mrService = GlobalServiceRegister.getDefault()
                    .getService(IMRProcessService.class);
            needBar = mrService.isProgressBarNeeded(process);
        }
        return needBar;
    }

    public void defineAsSubjobMapReduceStart() {
        if ((this.getMrGroupId() == null) || (!this.isActivate())) {
            this.isMapReduceStart = false;
        } else {
            this.isMapReduceStart = true;
        }
    }

    public void checkForGroupMapReduceStart() {
        List<? extends INode> gNodeList = this.getProcess().getGraphicalNodes();
        boolean firstOfTheSubjob = true;
        for (INode node : gNodeList) {
            if (node instanceof Node) {
                if (((Node) node).isMapReduceStart) {
                    if (((Node) node).getUniqueName().equals(this.getUniqueName())) {
                        continue;
                    } else if (((Node) node).getMrGroupId() != null && ((Node) node).getMrGroupId().equals(this.getMrGroupId())) {
                        firstOfTheSubjob = false;
                    }
                }

            }
        }
        this.isMapReduceStart = firstOfTheSubjob;
    }

    @Override
    public INode getJobletNode() {
        return jobletNode;
    }

    public void setJobletnode(Node jobletNode) {
        this.jobletNode = jobletNode;
    }

    @Override
    public INode getJunitNode() {
        return junitNode;
    }

    public void setJunitnode(Node junitNode) {
        this.junitNode = junitNode;
    }

    public String getJoblet_unique_name() {
        return joblet_unique_name;
    }

    public void setJoblet_unique_name(String joblet_unique_name) {
        this.joblet_unique_name = joblet_unique_name;
    }

    @Override
    public List<? extends IConnection> getOutgoingCamelSortedConnections() {
        return org.talend.core.model.utils.NodeUtil.getOutgoingCamelSortedConnections(this);
    }

    @Override
    public List<ModuleNeeded> getModulesNeeded() {
        // same as the component, but an override is possible in the AbstractNode when generate the code
        return component.getModulesNeeded(this);
    }

    @Override
    public EConnectionType getVirtualLinkTo() {
        return this.virtualLinkTo;
    }

    @Override
    public void setVirtualLinkTo(EConnectionType virtualLinkTo) {
        this.virtualLinkTo = virtualLinkTo;
    }

    public void fireImageChage() {
        firePropertyChange(ICON_CHANGE, null, null);
    }

    public void calculateSubtreeStartAndEnd() {
        setSubtreeStart(isDesignSubjobStartNode() && !isThereLinkWithHash());
        IElementParameter param = getElementParameter(EParameterName.END_OF_FLOW.getName());
        if (param != null) {
            if (NodeUtil.getOutgoingConnections(this, IConnectionCategory.DATA).isEmpty()) {
                param.setValue(Boolean.TRUE);
            } else {
                param.setValue(Boolean.FALSE);
            }
        }
    }

    /**
     * Getter for subtreeStart.
     *
     * @return the subtreeStart
     */
    @Override
    public boolean isSubtreeStart() {
        return this.subtreeStart;
    }

    /**
     * Sets the subtreeStart.
     *
     * @param subtreeStart the subtreeStart to set
     */
    public void setSubtreeStart(boolean subtreeStart) {
        this.subtreeStart = subtreeStart;
        IElementParameter param = getElementParameter(EParameterName.SUBTREE_START.getName());
        if (param == null) {
            return;
        }
        param.setValue(new Boolean(subtreeStart));
    }

    @Override
    public void setComponentProperties(ComponentProperties props) {
        componentProperties = props;
    }

    @Override
    public ComponentProperties getComponentProperties() {
        return componentProperties;
    }

    public Integer getMrGroupId() {
        return mrGroupId;
    }

    public void setMrGroupId(Integer mrGroupId) {
        this.mrGroupId = mrGroupId;
    }

    public Integer getMrJobInGroupCount() {
        if (mrJobInGroupCount == null) {
            return 1;
        }
        return mrJobInGroupCount;
    }

    public void setMrJobInGroupCount(Integer mrJobInGroupCount) {
        if (this.mrJobInGroupCount == null) {
            this.mrJobInGroupCount = mrJobInGroupCount;
            return;
        }
        if (mrJobInGroupCount != this.mrJobInGroupCount) {
            this.mrJobInGroupCount = mrJobInGroupCount;
            Display.getDefault().asyncExec(new Runnable() {

                @Override
                public void run() {
                    refreshNodeContainer();
                }
            });
        }
    }

    public Integer getMrJobIDInGroup() {
        return mrJobIDInGroup;
    }

    public void setMrJobIDInGroup(Integer mrJobIDInGroup) {
        this.mrJobIDInGroup = mrJobIDInGroup;
    }

    public boolean isMrContainsReduce() {
        return mrContainsReduce;
    }

    public void setMrContainsReduce(boolean mrContainsReduce) {
        this.mrContainsReduce = mrContainsReduce;
    }

    private boolean needCleared() {
        boolean clear = false;
        List<? extends INode> nodeList = this.process.getGraphicalNodes();
        for (INode node : nodeList) {
            if ((node instanceof Node) && (((Node) node).isMapReduceStart())) {
                Double perc = ((JobletContainer) ((Node) node).getNodeContainer()).getPercentMap()
                        + ((JobletContainer) ((Node) node).getNodeContainer()).getPercentReduce();
                if (perc > 0) {
                    return true;
                }
            }
        }
        return clear;
    }

    public void refreshNodeContainer() {
        boolean isRunning = false;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IRunProcessService.class)) {
            IRunProcessService runProcessService = GlobalServiceRegister.getDefault()
                    .getService(IRunProcessService.class);
            if (runProcessService != null) {
                isRunning = runProcessService.isJobRunning();
            }
        }
        if (isRunning) {
            return;
        }
        List<? extends INode> nodeList = this.process.getGraphicalNodes();
        for (INode node : nodeList) {
            if ((node instanceof Node) && (((Node) node).isMapReduceStart())) {
                ((JobletContainer) ((Node) node).getNodeContainer()).updateState("UPDATE_STATUS", "CLEAR", new Double(0), //$NON-NLS-1$ //$NON-NLS-2$
                        new Double(0));
            }
        }

    }

    public boolean hasUseExistingConnection(IElement currentElem) {
        return currentElem.getElementParameter("USE_EXISTING_CONNECTION") != null;
    }

    public boolean isUseExistedConnetion(IElement currentElem) {
        IElementParameter param = currentElem.getElementParameter("USE_EXISTING_CONNECTION"); //$NON-NLS-1$
        if (param != null) {
            Object value = param.getValue();
            if (value instanceof Boolean) {
                return (Boolean) value;
            } else if (value instanceof String) {
                return Boolean.parseBoolean((String) value);
            }
        }
        return false;
    }

    public void setJunitStart(boolean isJunitStart) {
        this.isJunitStart = isJunitStart;
    }

    public boolean isJunitStart() {
        return this.isJunitStart;
    }

    /**
     * Getter for delegateComponent.
     *
     * @return the delegateComponent
     */
    public IComponent getDelegateComponent() {
        return this.delegateComponent;
    }

    @Override
    public IReplaceNodeHandler getReplaceNodeHandler() {
        return replaceNodeHandler;
    }

    public void setReplaceNodeHandler(IReplaceNodeHandler replaceNodeHandler) {
        this.replaceNodeHandler = replaceNodeHandler;
    }

}
