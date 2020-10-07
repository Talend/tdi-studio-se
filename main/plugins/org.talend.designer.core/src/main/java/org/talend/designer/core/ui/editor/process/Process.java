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
package org.talend.designer.core.ui.editor.process;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.SnapToGeometry;
import org.eclipse.gef.SnapToGrid;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CommandStackEvent;
import org.eclipse.gef.commands.CommandStackEventListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.model.emf.EmfHelper;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.commons.ui.runtime.image.ImageUtils;
import org.talend.commons.utils.Hex;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ITDQRepositoryService;
import org.talend.core.PluginChecker;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.EComponentType;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.context.ContextUtils;
import org.talend.core.model.context.JobContextManager;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.general.Project;
import org.talend.core.model.metadata.IEbcdicConstant;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataSchemaType;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.process.AbstractNode;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IElementParameterDefaultValue;
import org.talend.core.model.process.IExternalNode;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.process.ISubjobContainer;
import org.talend.core.model.process.ProcessUtils;
import org.talend.core.model.process.UniqueNodeNameGenerator;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ItemState;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.properties.User;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.routines.RoutinesUtil;
import org.talend.core.model.update.IUpdateManager;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.utils.ConvertJobsUtil;
import org.talend.core.repository.utils.ProjectHelper;
import org.talend.core.repository.utils.XmiResourceManager;
import org.talend.core.runtime.process.TalendProcessArgumentConstant;
import org.talend.core.runtime.repository.item.ItemProductKeys;
import org.talend.core.runtime.util.ItemDateParser;
import org.talend.core.service.IScdComponentService;
import org.talend.core.ui.IJobletProviderService;
import org.talend.core.ui.ILastVersionChecker;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.core.ui.process.IGEFProcess;
import org.talend.core.utils.KeywordsValidator;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.ITestContainerGEFService;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.AbstractBasicComponent;
import org.talend.designer.core.model.components.DummyComponent;
import org.talend.designer.core.model.components.EOozieParameterName;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.model.metadata.MetadataEmfFactory;
import org.talend.designer.core.model.process.ConnectionManager;
import org.talend.designer.core.model.process.DataProcess;
import org.talend.designer.core.model.process.IGeneratingProcess;
import org.talend.designer.core.model.process.jobsettings.JobSettingsConstants;
import org.talend.designer.core.model.process.jobsettings.JobSettingsManager;
import org.talend.designer.core.model.utils.emf.talendfile.ConnectionType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.MetadataType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeContainerType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.NoteType;
import org.talend.designer.core.model.utils.emf.talendfile.ParametersType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.RoutinesParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.SubjobType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.cmd.ChangeConnTextCommand;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.jobletcontainer.AbstractJobletContainer;
import org.talend.designer.core.ui.editor.jobletcontainer.JobletContainer;
import org.talend.designer.core.ui.editor.jobletcontainer.JobletUtil;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.notes.Note;
import org.talend.designer.core.ui.editor.properties.controllers.ColumnListController;
import org.talend.designer.core.ui.editor.properties.controllers.ComponentListController;
import org.talend.designer.core.ui.editor.properties.controllers.ConnectionListController;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainer;
import org.talend.designer.core.ui.editor.subjobcontainer.sparkstreaming.SparkStreamingSubjobContainer;
import org.talend.designer.core.ui.preferences.TalendDesignerPrefConstants;
import org.talend.designer.core.ui.projectsetting.ProjectSettingManager;
import org.talend.designer.core.ui.views.contexts.ContextsView;
import org.talend.designer.core.ui.views.problems.Problems;
import org.talend.designer.core.utils.DesignerUtilities;
import org.talend.designer.core.utils.DetectContextVarsUtils;
import org.talend.designer.core.utils.JavaProcessUtil;
import org.talend.designer.core.utils.JobSettingVersionUtil;
import org.talend.designer.core.utils.UnifiedComponentUtil;
import org.talend.designer.core.utils.ValidationRulesUtil;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.designer.runprocess.ItemCacheManager;
import org.talend.model.bridge.ReponsitoryContextBridge;
import org.talend.repository.ProjectManager;
import org.talend.repository.UpdateRepositoryUtils;
import org.talend.repository.constants.Log4jPrefsConstants;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.migration.UpdateTheJobsActionsOnTable;
import org.talend.repository.ui.utils.Log4jPrefsSettingManager;

/**
 * The diagram will contain all elements (nodes, connections) The xml that describes the diagram will be saved from the
 * information of this class. <br/>
 *
 * $Id$
 *
 */
public class Process extends Element implements IProcess2, IGEFProcess, ILastVersionChecker {

    private static String UTF8 = "UTF-8";

    protected List<INode> nodes = new ArrayList<INode>();

    protected List<Element> elem = new ArrayList<Element>();

    protected List<SubjobContainer> subjobContainers = new ArrayList<SubjobContainer>();

    protected List<Note> notes = new ArrayList<Note>();

    protected List<RoutinesParameterType> routinesDependencies;

    private final String name = new String(Messages.getString("Process.Job")); //$NON-NLS-1$

    private boolean activate = true;

    // list where is stored each unique name for the connections
    private final List<String> uniqueConnectionNameList = new ArrayList<String>();

    // list where is stored each unique name for the nodes
    private final List<String> uniqueNodeNameList = new ArrayList<String>();

    private boolean readOnly;

    private GraphicalViewer viewer = null;

    private IContextManager contextManager;

    public static final int BREAKPOINT_STATUS = 1;

    public static final int ERROR_STATUS = 2;

    public static final int WARNING_STATUS = 4;

    public static final int INFO_STATUS = 16;

    public static final int VALIDATION_RULE_STATUS = 32;

    public static final int PARALLEL_STATUS = 8;

    public static final int WINDOW_STATUS = 64;
    
    public static final int BREAKPOINT_ACTIVE_STATUS = 128; //for route debugging

    private Property property;

    private boolean initDone = false;

    private AbstractMultiPageTalendEditor editor;

    private Map<Node, SubjobContainer> mapSubjobStarts = new HashMap<Node, SubjobContainer>();

    private boolean duplicate = false;

    protected IUpdateManager updateManager;

    protected Map<String, byte[]> screenshots = null;

    protected Map<Object, Object> additionalProperties = null;

    private List<byte[]> externalInnerContents = new ArrayList<byte[]>();

    private Set<String> neededRoutines;

    private Set<String> neededPigudf;

    private static final String SOURCE_JAVA_PIGUDF = "pigudf";

    private String componentsType;

    private boolean isNeedLoadmodules = true;

    private static Perl5Matcher matcher;

    private static Pattern pattern;

    static {
        matcher = new Perl5Matcher();
        Perl5Compiler compiler = new Perl5Compiler();
        try {
            pattern = compiler.compile("^[A-Za-z_][A-Za-z0-9_]*$"); //$NON-NLS-1$
        } catch (MalformedPatternException e) {
            throw new RuntimeException(e);
        }
    }

    public Process(Property property) {
        this.property = property;
        screenshots = new HashMap<String, byte[]>();
        contextManager = new JobContextManager();
        updateManager = new ProcessUpdateManager(this);
        createProcessParameters();
        init();
        loadAdditionalProperties();
        componentsType = ComponentCategory.CATEGORY_4_DI.getName();
    }

    @Override
    public void updateProperties() {
        try {
            setId(property.getId());
            setLabel(property.getLabel());
            setVersion(property.getVersion());
            setAuthor(property.getAuthor());
            setStatusCode(property.getStatusCode());
            setDescription(property.getDescription());
            setPurpose(property.getPurpose());
            if (getStatusCode() == null) {
                setStatusCode(""); //$NON-NLS-1$
            }
        } catch (Exception ex) {
            ExceptionHandler.process(ex);
        }
    }

    private void init() {
        if (!initDone) {
            updateProperties();
            initDone = true;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.getId() == null) ? 0 : this.getId().hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Process other = (Process) obj;
        if (!this.getId().equals(other.getId())) {
            return false;
        }
        return true;
    }

    /**
     * Add all parameters for a process.
     */
    private void createProcessParameters() {
        createMainParameters();
        createJobSettingsParameters();
        // TDI-8323:if we select "Add all user routines to job dependencies" in windows preference, when creating a new
        // job",we need to set its routineParameters for process
        createRoutineDependecnes();
    }

    /**
     * Add all routineParameters for a process.
     */
    private void createRoutineDependecnes() {
        ProcessType processType = getProcessType();
        if (processType != null && processType.getParameters() != null) {
            routinesDependencies = new ArrayList<RoutinesParameterType>(processType.getParameters().getRoutinesParameter());
        }
    }

    /**
     * create parameters for tabbed page 'Job Settings'.
     */
    protected void createJobSettingsParameters() {
        ((List<IElementParameter>) this.getElementParameters()).addAll(JobSettingsManager.getJobSettingsParameters(this));
    }

    /**
     * Creates parameters for tabbed page 'Main'.
     */
    private void createMainParameters() {
        ElementParameter param;

        param = new ElementParameter(this);
        param.setName(EParameterName.COMP_DEFAULT_FILE_DIR.getName());
        param.setCategory(EComponentCategory.TECHNICAL);
        param.setFieldType(EParameterFieldType.DIRECTORY);
        param.setDisplayName(EParameterName.COMP_DEFAULT_FILE_DIR.getDisplayName());
        param.setNumRow(99);
        param.setShow(false);
        param.setValue(DesignerPlugin.getDefault().getPreferenceStore()
                .getString(TalendDesignerPrefConstants.COMP_DEFAULT_FILE_DIR));
        param.setReadOnly(true);
        addElementParameter(param);
        // For TDQ-11338 Add this path for tDqReportRun component on git remote project

        if (GlobalServiceRegister.getDefault().isServiceRegistered(ITDQRepositoryService.class)) {
            param = new ElementParameter(this);
            param.setName(EParameterName.TDQ_DEFAULT_PROJECT_DIR.getName());
            param.setCategory(EComponentCategory.TECHNICAL);
            param.setFieldType(EParameterFieldType.DIRECTORY);
            param.setDisplayName(EParameterName.TDQ_DEFAULT_PROJECT_DIR.getDisplayName());
            param.setNumRow(99);
            param.setShow(false);
            if (ReponsitoryContextBridge.getRootProject().getLocation() != null) {
                param.setValue(ReponsitoryContextBridge.getRootProject().getLocation().toPortableString());
            }
            param.setReadOnly(true);
            addElementParameter(param);
        }

        // for log4j activate
        param = new ElementParameter(this);
        param.setName(EParameterName.LOG4J_ACTIVATE.getName());
        param.setCategory(EComponentCategory.TECHNICAL);
        param.setFieldType(EParameterFieldType.CHECK);
        param.setDisplayName(EParameterName.LOG4J_ACTIVATE.getDisplayName());
        param.setNumRow(99);
        param.setShow(false);
        param.setValue(new Boolean(Log4jPrefsSettingManager.getInstance().isLog4jEnable()).toString());
        param.setReadOnly(true);
        addElementParameter(param);

        param = new ElementParameter(this);
        param.setCategory(EComponentCategory.TECHNICAL);
        param.setName(EParameterName.LOG4J2_ACTIVATE.getName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setDisplayName(EParameterName.LOG4J2_ACTIVATE.getDisplayName());
        param.setNumRow(99);
        param.setShow(false);
        param.setValue(new Boolean(Log4jPrefsSettingManager.getInstance().isSelectLog4j2()).toString());
        param.setReadOnly(true);
        addElementParameter(param);

        // MOD by zshen for TDQ_INSTALL_DIR bug 17622
        param = new ElementParameter(this);
        param.setName(EParameterName.PRODUCT_ROOT_DIR.getName());
        param.setCategory(EComponentCategory.TECHNICAL);
        param.setFieldType(EParameterFieldType.DIRECTORY);
        param.setDisplayName(EParameterName.PRODUCT_ROOT_DIR.getDisplayName());
        param.setNumRow(99);
        param.setShow(false);
        param.setValue(DesignerPlugin.getDefault().getPreferenceStore().getString(TalendDesignerPrefConstants.PRODUCT_ROOT_DIR));
        param.setReadOnly(true);
        addElementParameter(param);

        // param = new ElementParameter(this);
        // param.setName(EParameterName.COMP_DEFAULT_PROJECT_DIR.getName());
        // param.setCategory(EComponentCategory.TECHNICAL);
        // param.setFieldType(EParameterFieldType.DIRECTORY);
        // param.setDisplayName(EParameterName.COMP_DEFAULT_PROJECT_DIR.getDisplayName());
        // param.setNumRow(99);
        // param.setShow(false);
        // param.setValue(DesignerPlugin.getDefault().getPreferenceStore()
        // .getString(TalendDesignerPrefConstants.COMP_DEFAULT_PROJECT_DIR));
        // param.setReadOnly(true);
        // addElementParameter(param);

        param = new ElementParameter(this);
        param.setName(EParameterName.DQ_REPORTING_BUNDLE_DIR.getName());
        param.setCategory(EComponentCategory.TECHNICAL);
        param.setFieldType(EParameterFieldType.DIRECTORY);
        param.setDisplayName(EParameterName.DQ_REPORTING_BUNDLE_DIR.getDisplayName());
        param.setNumRow(99);
        param.setShow(false);
        param.setValue(DesignerPlugin.getDefault().getPreferenceStore()
                .getString(TalendDesignerPrefConstants.DQ_REPORTING_BUNDLE_DIR));
        param.setReadOnly(true);
        addElementParameter(param);

        param = new ElementParameter(this);
        param.setName(EParameterName.LOG4J_RUN_ACTIVATE.getName());
        param.setCategory(EComponentCategory.MAIN);
        param.setFieldType(EParameterFieldType.CHECK);
        param.setDisplayName(EParameterName.LOG4J_RUN_ACTIVATE.getDisplayName());
        param.setNumRow(99);
        param.setShow(false);
        param.setValue(false);
        param.setDefaultValue(param.getValue());
        addElementParameter(param);

        param = new ElementParameter(this);
        param.setName(EParameterName.LOG4J_RUN_LEVEL.getName());
        param.setCategory(EComponentCategory.MAIN);
        param.setFieldType(EParameterFieldType.CLOSED_LIST);
        param.setDisplayName(EParameterName.LOG4J_RUN_LEVEL.getDisplayName());
        param.setNumRow(99);
        param.setShow(false);
        param.setValue("Info");
        addElementParameter(param);

        param = new ElementParameter(this);
        param.setName(EParameterName.JOB_RUN_VM_ARGUMENTS.getName());
        param.setCategory(EComponentCategory.MAIN);
        param.setFieldType(EParameterFieldType.TEXT);
        param.setDisplayName(EParameterName.JOB_RUN_VM_ARGUMENTS.getDisplayName());
        param.setNumRow(99);
        param.setShow(false);
        IRunProcessService service = DesignerPlugin.getDefault().getRunProcessService();
        if (service != null) {
            param.setValue(service.getPreferenceStore().getString("vmarguments")); //$NON-NLS-1$
            param.setDefaultValue(param.getValue());
        }
        addElementParameter(param);

        param = new ElementParameter(this);
        param.setName(EParameterName.JOB_RUN_VM_ARGUMENTS_OPTION.getName());
        param.setCategory(EComponentCategory.MAIN);
        param.setFieldType(EParameterFieldType.CHECK);
        param.setDisplayName(EParameterName.JOB_RUN_VM_ARGUMENTS_OPTION.getDisplayName());
        param.setNumRow(99);
        param.setShow(false);
        param.setValue(false);
        param.setDefaultValue(param.getValue());
        addElementParameter(param);

        param = new ElementParameter(this);
        param.setName(EParameterName.AUTHOR.getName());
        param.setCategory(EComponentCategory.TECHNICAL);
        param.setFieldType(EParameterFieldType.TEXT);
        param.setDisplayName(EParameterName.AUTHOR.getDisplayName());
        param.setShow(false);
        param.setReadOnly(true);
        addElementParameter(param);

        param = new ElementParameter(this);
        param.setName(EParameterName.STATUS.getName());
        param.setCategory(EComponentCategory.TECHNICAL);
        param.setFieldType(EParameterFieldType.TEXT);
        param.setDisplayName(EParameterName.STATUS.getDisplayName());
        param.setShow(false);
        param.setReadOnly(true);
        addElementParameter(param);

        param = new ElementParameter(this);
        param.setName(EParameterName.NAME.getName());
        param.setCategory(EComponentCategory.TECHNICAL);
        param.setFieldType(EParameterFieldType.TEXT);
        param.setDisplayName(EParameterName.NAME.getDisplayName());
        param.setShow(false);
        param.setReadOnly(true);
        addElementParameter(param);

        param = new ElementParameter(this);
        param.setName(EParameterName.VERSION.getName());
        param.setCategory(EComponentCategory.TECHNICAL);
        param.setFieldType(EParameterFieldType.TEXT);
        param.setDisplayName(EParameterName.VERSION.getDisplayName());
        param.setShow(false);
        param.setReadOnly(true);
        addElementParameter(param);

        param = new ElementParameter(this);
        param.setName(EParameterName.PURPOSE.getName());
        param.setCategory(EComponentCategory.TECHNICAL);
        param.setFieldType(EParameterFieldType.TEXT);
        param.setDisplayName(EParameterName.PURPOSE.getDisplayName());
        param.setShow(false);
        param.setReadOnly(true);
        addElementParameter(param);

        param = new ElementParameter(this);
        param.setName(EParameterName.DESCRIPTION.getName());
        param.setCategory(EComponentCategory.TECHNICAL);
        param.setFieldType(EParameterFieldType.TEXT);
        param.setDisplayName(EParameterName.DESCRIPTION.getDisplayName());
        param.setShow(false);
        param.setReadOnly(true);
        addElementParameter(param);

        param = new ElementParameter(this);
        param.setName(SCREEN_OFFSET_X);
        param.setCategory(EComponentCategory.TECHNICAL);
        param.setFieldType(EParameterFieldType.TEXT);
        param.setShow(false);
        param.setReadOnly(false);
        addElementParameter(param);

        param = new ElementParameter(this);
        param.setName(SCREEN_OFFSET_Y);
        param.setCategory(EComponentCategory.TECHNICAL);
        param.setFieldType(EParameterFieldType.TEXT);
        param.setShow(false);
        param.setReadOnly(false);
        addElementParameter(param);

        param = new ElementParameter(this);
        param.setName(EParameterName.SCHEMA_OPTIONS.getName());
        param.setCategory(EComponentCategory.MAIN);
        param.setFieldType(EParameterFieldType.TEXT);
        param.setDisplayName(EParameterName.SCHEMA_OPTIONS.getDisplayName());
        param.setShow(false);
        param.setValue(DesignerPlugin.getDefault().getPluginPreferences().getString(TalendDesignerPrefConstants.SCHEMA_OPTIONS));
        param.setDefaultValue(param.getValue());
        param.setReadOnly(true);
        addElementParameter(param);

        param = new ElementParameter(this);
        param.setName("OEM_CUSTOM_ATTRIBUTE");
        param.setCategory(EComponentCategory.TECHNICAL);
        param.setFieldType(EParameterFieldType.TEXT);
        param.setShow(false);
        param.setValue("");
        param.setDefaultValue(param.getValue());
        param.setReadOnly(false);
        addElementParameter(param);

        param = new ElementParameter(this);
        param.setName(EOozieParameterName.HADOOP_APP_PATH.getName());
        param.setDisplayName(EOozieParameterName.HADOOP_APP_PATH.getDisplayName());
        param.setCategory(EComponentCategory.TECHNICAL);
        param.setFieldType(EParameterFieldType.TEXT);
        param.setShow(false);
        param.setValue("");
        param.setDefaultValue(param.getValue());
        param.setReadOnly(false);
        addElementParameter(param);

        param = new ElementParameter(this);
        param.setName(EOozieParameterName.JOBID_FOR_OOZIE.getName());
        param.setDisplayName(EOozieParameterName.JOBID_FOR_OOZIE.getDisplayName());
        param.setCategory(EComponentCategory.TECHNICAL);
        param.setFieldType(EParameterFieldType.TEXT);
        param.setShow(false);
        param.setValue("");
        param.setDefaultValue(param.getValue());
        param.setReadOnly(false);
        addElementParameter(param);

        param = new ElementParameter(this);
        param.setName(EOozieParameterName.REPOSITORY_CONNECTION_ID.getName());
        param.setDisplayName(EOozieParameterName.REPOSITORY_CONNECTION_ID.getDisplayName());
        param.setCategory(EComponentCategory.TECHNICAL);
        param.setFieldType(EParameterFieldType.TEXT);
        param.setShow(false);
        param.setValue("");
        param.setReadOnly(false);
        addElementParameter(param);

        // For adding the param definition in process item prepared
        for (EOozieParameterName oozieParam : EOozieParameterName.values()) {
            if (!"REPOSITORY_CONNECTION_ID".equals(oozieParam.getName()) && !"JOBID_FOR_OOZIE".equals(oozieParam.getName())
                    && !"HADOOP_APP_PATH".equals(oozieParam.getName())) {
                param = new ElementParameter(this);
                param.setName(oozieParam.getName());
                param.setDisplayName(oozieParam.getDisplayName());
                param.setCategory(EComponentCategory.TECHNICAL);
                param.setFieldType(EParameterFieldType.TEXT);
                param.setShow(false);
                param.setValue("");
                param.setReadOnly(false);
                addElementParameter(param);
            }
        }
        // TDI-24548
        param = new ElementParameter(this);
        param.setName(EParameterName.PROJECT_TECHNICAL_NAME.getName());
        param.setCategory(EComponentCategory.TECHNICAL);
        param.setFieldType(EParameterFieldType.TEXT);
        param.setDisplayName(EParameterName.PROJECT_TECHNICAL_NAME.getDisplayName());
        param.setValue(getProject().getTechnicalLabel());
        param.setShow(false);
        param.setReadOnly(true);
        addElementParameter(param);

        param = new ElementParameter(this);
        param.setName(EParameterName.HADOOP_ADVANCED_PROPERTIES.getName());
        param.setCategory(EComponentCategory.MAIN);
        param.setFieldType(EParameterFieldType.TABLE);
        param.setListItemsDisplayCodeName(new String[] { "PROPERTY", "VALUE" });
        ElementParameter newParam1 = new ElementParameter(this);
        newParam1.setFieldType(EParameterFieldType.TEXT);
        newParam1.setName("PROPERTY");
        newParam1.setValue("");
        ElementParameter newParam2 = new ElementParameter(this);
        newParam2.setFieldType(EParameterFieldType.TEXT);
        newParam2.setName("VALUE");
        newParam2.setValue("");
        Object[] items = new Object[2];
        items[0] = newParam1;
        items[1] = newParam2;
        param.setListItemsValue(items);
        param.setNumRow(99);
        param.setShow(false);
        param.setDisplayName(EParameterName.HADOOP_ADVANCED_PROPERTIES.getDisplayName());
        param.setValue(new ArrayList<Map<String, Object>>());
        param.setDefaultValue(param.getValue());
        addElementParameter(param);

    }

    /**
     * Add a new node to the diagram.
     *
     * @param node
     */
    public void addNodeContainer(final NodeContainer nodeContainer) {
        elem.add(nodeContainer);
        nodes.add(nodeContainer.getNode());

        // fireStructureChange(NEED_UPDATE_JOB, elem);
    }

    /**
     * Remove a node to the diagram.
     *
     * @param node
     */
    public void removeNodeContainer(final NodeContainer nodeContainer) {
        String uniqueName = nodeContainer.getNode().getUniqueName();
        removeUniqueNodeName(uniqueName);
        if (nodeContainer instanceof AbstractJobletContainer) {
            // use readedContainers to record the containers alreay be read, in case of falling into dead loop
            Set<NodeContainer> readedContainers = new HashSet<NodeContainer>();
            removeUniqueNodeNamesInJoblet((AbstractJobletContainer) nodeContainer, readedContainers);
        }
        removeNode(uniqueName);
        Element toRemove = nodeContainer;
        List<Element> toAdd = new ArrayList<Element>();
        for (Object o : elem) {
            if (o instanceof SubjobContainer) {
                SubjobContainer sjc = (SubjobContainer) o;
                if (sjc.deleteNodeContainer(uniqueName)) {
                    if (nodeContainer.getNode().isDesignSubjobStartNode()) {
                        subjobContainers.remove(sjc);
                        toAdd.addAll(sjc.getNodeContainers());
                        toRemove = sjc;
                        break;
                    }
                }
            }
        }

        elem.remove(toRemove);
        elem.addAll(toAdd);

        // fireStructureChange(NEED_UPDATE_JOB, elem);
    }

    private void removeUniqueNodeNamesInJoblet(AbstractJobletContainer jobletContainer, Set<NodeContainer> readedContainers) {
        List<NodeContainer> nodeContainers = jobletContainer.getNodeContainers();
        if (nodeContainers == null) {
            return;
        }
        Iterator<NodeContainer> iter = nodeContainers.iterator();
        // object is unique in Set
        readedContainers.add(jobletContainer);
        while (iter.hasNext()) {
            NodeContainer nodeContainer = iter.next();
            if (nodeContainer instanceof AbstractJobletContainer) {
                if (!readedContainers.contains(nodeContainer)) {
                    removeUniqueNodeNamesInJoblet((AbstractJobletContainer) nodeContainer, readedContainers);
                }
            } else {
                String uniqueName = nodeContainer.getNode().getUniqueName();
                removeUniqueNodeName(uniqueName);
            }
        }
    }

    /**
     * DOC ycbai Comment method "removeNode".
     *
     * @param nodeUniqueName
     */
    private void removeNode(String nodeUniqueName) {
        if (nodeUniqueName == null) {
            return;
        }
        Iterator<INode> nodeIter = nodes.iterator();
        while (nodeIter.hasNext()) {
            INode node = nodeIter.next();
            if (nodeUniqueName.equals(node.getUniqueName())) {
                nodeIter.remove();
            }
        }
    }

    /**
     * Get the list of all elements, Node and Connection.
     *
     * @return
     */
    @Override
    public List getElements() {
        return this.elem;
    }

    @Override
    public List<? extends INode> getGraphicalNodes() {
        return this.nodes;
    }

    protected IGeneratingProcess generatingProcess = null;

    boolean isBuilding;

    /**
     * Getter for isBuilding.
     *
     * @return the isBuilding
     */
    public synchronized boolean isBuilding() {
        return this.isBuilding;
    }

    /**
     * Sets the isBuilding.
     *
     * @param isBuilding the isBuilding to set
     */
    public synchronized void setBuilding(boolean isBuilding) {
        this.isBuilding = isBuilding;
    }

    @Override
    public List<? extends INode> getGeneratingNodes() {
        List<? extends INode> generatedNodeList = getGeneratingProcess().getNodeList();
        if (!isBuilding()) {
            if (isProcessModified() || routinesDependencies == null || routinesDependencies.isEmpty()) {
                checkRoutineDependencies();
            }
            if (isProcessModified()) {
                if (isBuilding()) {
                    return generatedNodeList;
                }
                setBuilding(true);
                List<INode> sortedFlow = sortNodes(nodes);
                if (sortedFlow.size() != nodes.size()) {
                    sortedFlow = nodes;
                }
                generatingProcess.buildFromGraphicalProcess(sortedFlow);
                generatedNodeList = generatingProcess.getNodeList();
                if (isActivate()) {
                    // if not activated, like during the loading of job, we will still rebuild the list of generated
                    // nodes
                    processModified = false;
                }
                setBuilding(false);
            }
        }
        return generatedNodeList;
    }

    /**
     *
     * DOC yexiaowei Comment method "sortNodes".
     *
     * @param nodes
     * @return
     */
    protected List<INode> sortNodes(List<INode> nodes) {

        if (nodes == null || nodes.size() <= 1) {
            return nodes;
        }

        List<INode> res = new ArrayList<INode>();

        List<List<INode>> mainStart = new ArrayList<List<INode>>();

        List<List<INode>> notMainStart = new ArrayList<List<INode>>();

        List<INode> starts = new ArrayList<INode>();

        for (INode node : nodes) {
            if (node.isStart() || node.isSubProcessStart()) {
                starts.add(node);
            }
        }

        for (INode node : starts) {
            List<INode> branch = new ArrayList<INode>();
            branch.add(node);
            findTargetAll(branch, node);
            if (node.isStart() && node.isSubProcessStart()) {
                mainStart.add(branch);
            } else {
                notMainStart.add(branch);
            }

        }

        // Must sort the mainStart first...
        List<List<INode>> tempStart = new ArrayList<List<INode>>();
        tempStart.addAll(mainStart);
        for (List<INode> preview : mainStart) {
            for (List<INode> now : mainStart) {
                if (!preview.equals(now) && now.contains(preview.get(0))) {
                    tempStart.remove(preview);
                    tempStart.add(tempStart.indexOf(now) + 1, preview);
                }
            }
        }

        for (List<INode> branch : tempStart) {
            for (INode n : branch) {
                if (!res.contains(n)) {
                    res.add(n);
                }
            }

            for (List<INode> ns : notMainStart) {

                for (INode node : ns) {
                    if (branch.contains(node)) {
                        for (INode nodeadd : ns) {
                            if (!res.contains(nodeadd)) {
                                res.add(nodeadd);
                            }
                            break;
                        }
                    }
                }

            }
        }
        return res;
    }

    private void findTargetAll(List<INode> res, INode current) {

        List<? extends IConnection> conns = current.getOutgoingConnections();

        if (CollectionUtils.isNotEmpty(conns)) {
            for (Object obj : conns) {
                IConnection con = (IConnection) obj;
                INode target = con.getTarget();
                if (target.getJobletNode() != null) {
                    target = target.getJobletNode();
                }
                if (!res.contains(target)) {
                    res.add(target);
                    findTargetAll(res, target);
                }
            }
        }
    }

    protected boolean processModified = true;

    private boolean loadScreenshots;

    @Override
    public boolean isProcessModified() {
        if (generatingProcess == null) {
            return true;
        }
        List<? extends INode> generatedNodeList = generatingProcess.getNodeList();
        if (generatedNodeList == null || generatedNodeList.isEmpty() || (this.getEditor() == null && processModified)) {
            return true;
        }
        return processModified;
    }

    /*
     * public double getZoom() { return zoom; }
     */
    private void retrieveAttachedViewer() {
        IEditorPart editorPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        if (editorPart instanceof AbstractMultiPageTalendEditor) {
            viewer = ((AbstractMultiPageTalendEditor) editorPart).getTalendEditor().getViewer();
        }
    }

    public void setViewer(GraphicalViewer viewer) {
        this.viewer = viewer;
    }

    /**
     * Returns true if the grid is enabled.
     *
     * @return
     */
    @Override
    public boolean isGridEnabled() {
        if (viewer == null) {
            retrieveAttachedViewer();
            if (viewer != null) {
                return (Boolean) viewer.getProperty(SnapToGrid.PROPERTY_GRID_ENABLED);
            }
        } else {
            return (Boolean) viewer.getProperty(SnapToGrid.PROPERTY_GRID_ENABLED);
        }
        return false;
    }

    /**
     * Returns true if the SnapToGeometry is enabled.
     *
     * @return
     */
    public boolean isSnapToGeometryEnabled() {
        if (viewer == null) {
            retrieveAttachedViewer();
            if (viewer != null) {
                return (Boolean) viewer.getProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED);
            }
        } else {
            return (Boolean) viewer.getProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED);
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    protected void saveElementParameters(TalendFileFactory fileFact, List<? extends IElementParameter> paramList,
            EList listParamType, ProcessType process) {
        IElementParameter param;
        // if it's a generic component,try to serialize the component to json,then save all in a new ElementParameter,
        // that name: PROPERTIES".Then later when load the job, if the component loaded is a generic component... if
        // yes,then deserialize the json to get back the properties / set each element parameter.
        String serializedProperties = null;
        for (int j = 0; j < paramList.size(); j++) {
            param = paramList.get(j);
            if (!param.isSerialized()) {
                saveElementParameter(param, process, fileFact, paramList, listParamType);
                for (String key : param.getChildParameters().keySet()) {
                    saveElementParameter(param.getChildParameters().get(key), process, fileFact, paramList, listParamType);
                }
            } else {
                if (serializedProperties == null) {
                    if (param.getElement() != null && param.getElement() instanceof INode) {
                        IComponent iComponent = ((INode) param.getElement()).getComponent();
                        if (iComponent instanceof AbstractBasicComponent) {
                            AbstractBasicComponent component = (AbstractBasicComponent) iComponent;
                            for (IElementParameter parameter : paramList) {
                                if (!param.isSerialized()) {
                                    continue;
                                }
                                if (parameter.isRepositoryValueUsed() && parameter.getRepositoryValue() != null) {
                                    component.setGenericPropertyValue(parameter);
                                }
                            }
                            serializedProperties = component.genericToSerialized(param);
                        }
                    }
                }
                // save the schema type
                if (EParameterFieldType.SCHEMA_REFERENCE.equals(param.getFieldType())) {
                    for (String key : param.getChildParameters().keySet()) {
                        saveElementParameter(param.getChildParameters().get(key), process, fileFact, paramList, listParamType);
                    }
                }
            }
        }
        if (serializedProperties != null) {
            ElementParameterType pType = fileFact.createElementParameterType();
            pType.setName("PROPERTIES"); //$NON-NLS-1$
            pType.setValue(serializedProperties);
            listParamType.add(pType);
        }
    }

    private void saveElementParameters(TalendFileFactory fileFact, List<? extends IElementParameter> paramList,
            EList listParamType, NodeType process) {
        IElementParameter param;
        ElementParameterType pType;
        for (int j = 0; j < paramList.size(); j++) {
            param = paramList.get(j);
            pType = fileFact.createElementParameterType();
            pType.setName(param.getName());
            Object value = param.getValue();
            if (value instanceof Boolean) {
                pType.setValue(((Boolean) value).toString());
            } else if (value instanceof String) {
                pType.setValue((String) value);
            }
            listParamType.add(pType);
        }
    }
    
    private boolean isDefaultValue(IElementParameter param) {
    	if (param != null && param.getName().equals(EParameterName.JOB_RUN_VM_ARGUMENTS.getName())) {
    		if(param.getElement() instanceof Process) {
    			IElementParameter jvmOptParam = ((Process)param.getElement()).getElementParameter(EParameterName.JOB_RUN_VM_ARGUMENTS_OPTION.getName());
    			if(jvmOptParam != null && param.isValueSetToDefault() && jvmOptParam.isValueSetToDefault()) {
    				return true;
    			}else {
    				return false;
    			}
    		}
        }
    	return param.isValueSetToDefault();
    }

    private void saveElementParameter(IElementParameter param, ProcessType process, TalendFileFactory fileFact,
            List<? extends IElementParameter> paramList, EList listParamType) {
        ElementParameterType pType;
        boolean isJoblet = false;
        if (param.getElement() instanceof INode && PluginChecker.isJobLetPluginLoaded()) {
            IJobletProviderService service = GlobalServiceRegister.getDefault().getService(
                    IJobletProviderService.class);
            if (service != null && service.isJobletComponent((INode) param.getElement())) {
                isJoblet = true;
            }
        }
        if (isDefaultValue(param)) {
        	return;
        }

        if (param.getFieldType().equals(EParameterFieldType.SCHEMA_TYPE)
                || param.getFieldType().equals(EParameterFieldType.SCHEMA_REFERENCE)
                || param.getFieldType().equals(EParameterFieldType.PROPERTY_TYPE)
                || param.getFieldType().equals(EParameterFieldType.VALIDATION_RULE_TYPE)
                || param.getFieldType().equals(EParameterFieldType.UNIFIED_COMPONENTS)
                || param.getName().equals(EParameterName.UPDATE_COMPONENTS.getName())) {
            return;
        }
        if (param.getParentParameter() != null) {
            if (param.getParentParameter().getFieldType().equals(EParameterFieldType.PROPERTY_TYPE)) {
                IElementParameter paramBuiltInRepository = param.getParentParameter().getChildParameters()
                        .get(EParameterName.PROPERTY_TYPE.getName());
                if (paramBuiltInRepository.getValue().equals(EmfComponent.BUILTIN)) {
                    return;
                }
            }
            if (param.getParentParameter().getFieldType().equals(EParameterFieldType.SCHEMA_TYPE)
                    || param.getParentParameter().getFieldType().equals(EParameterFieldType.SCHEMA_REFERENCE)) {
                IElementParameter paramBuiltInRepository = param.getParentParameter().getChildParameters()
                        .get(EParameterName.SCHEMA_TYPE.getName());
                if (isJoblet && param.getName().equals(EParameterName.CONNECTION.getName())) {
                    // save conenction value
                } else if (paramBuiltInRepository.getValue().equals(EmfComponent.BUILTIN)) {
                    return;
                }
            }
            if (param.getParentParameter().getFieldType().equals(EParameterFieldType.VALIDATION_RULE_TYPE)) {
                IElementParameter paramBuiltInRepository = param.getParentParameter().getChildParameters()
                        .get(EParameterName.VALIDATION_RULE_TYPE.getName());
                if (paramBuiltInRepository.getValue().equals(EmfComponent.BUILTIN)) {
                    return;
                }
            }
        }

        if (param.getElement() instanceof Process) {
            if (param.isReadOnly() && param.getCategory() == EComponentCategory.TECHNICAL) {
                return;
            }
            if (isJoblet) {
                if (param != null && !(param.getName().equals(EParameterName.STARTABLE.getName()))) {
                    return;
                }
            }
        }
        if (param.getElement() instanceof Node) {
            if (param != null && param.getName().equals(EParameterName.REPOSITORY_ALLOW_AUTO_SWITCH.getName())) {
                return;
            }
            if (param.isReadOnly() && param.getCategory() == EComponentCategory.TECHNICAL) {
                return;
            }
        }
        pType = fileFact.createElementParameterType();
        if (param.getParentParameter() != null) {
            pType.setName(param.getParentParameter().getName() + ":" + param.getName()); //$NON-NLS-1$
        } else {
            pType.setName(param.getName());
        }
        pType.setField(param.getFieldType().getName());
        if (param.isContextMode()) {
            pType.setContextMode(param.isContextMode());
        }
        Object value = param.getValue();
        if (isTable(param) && value != null) {
            List<Map<String, Object>> tableValues = (List<Map<String, Object>>) value;
            for (Map<String, Object> currentLine : tableValues) {
                for (int i = 0; i < param.getListItemsDisplayCodeName().length; i++) {
                    boolean isHexValue = false;
                    ElementValueType elementValue = fileFact.createElementValueType();
                    elementValue.setElementRef(param.getListItemsDisplayCodeName()[i]);
                    Object o = currentLine.get(param.getListItemsDisplayCodeName()[i]);
                    String strValue = ""; //$NON-NLS-1$
                    IElementParameter tmpParam = null;
                    Object[] listItemsValue = param.getListItemsValue();
                    if (listItemsValue.length > i) {
                        tmpParam = (IElementParameter) listItemsValue[i];
                    }
                    if (o instanceof Integer && tmpParam != null) {
                        if (tmpParam.getListItemsValue().length == 0) {
                            strValue = ""; //$NON-NLS-1$
                        } else {
                            strValue = (String) tmpParam.getListItemsValue()[(Integer) o];
                        }
                    } else {
                        if (o instanceof String) {
                            strValue = (String) o;
                            isHexValue = isNeedConvertToHex(strValue);
                            if (isHexValue) {
                                try {
                                    strValue = Hex.encodeHexString(strValue.getBytes(UTF8));
                                } catch (UnsupportedEncodingException e) {
                                    ExceptionHandler.process(e);
                                }
                            }
                        } else {
                            if (o instanceof Boolean) {
                                strValue = ((Boolean) o).toString();
                            }
                        }
                    }
                    if (tmpParam != null && EParameterFieldType.isPassword(tmpParam.getFieldType())) {
                        elementValue.setValue(strValue, true);
                    } else {
                        elementValue.setValue(strValue);
                    }
                    if (isHexValue) {
                        elementValue.setHexValue(true);
                    }
                    Object object = currentLine.get(param.getListItemsDisplayCodeName()[i] + IEbcdicConstant.REF_TYPE);
                    if (object != null) {
                        elementValue.setType((String) object);
                    }
                    pType.getElementValue().add(elementValue);
                }
            }
        } else if (param.getFieldType().equals(EParameterFieldType.TABLE_BY_ROW) && value != null) {
            List<Map<String, Object>> tableValues = (List<Map<String, Object>>) value;
            for (Map<String, Object> currentLine : tableValues) {
                Iterator it = currentLine.keySet().iterator();
                while (it.hasNext()) {
                    String key = (String) it.next();
                    if (!(currentLine.get(key) instanceof String)) {
                        continue;
                    }
                    String expression = (String) currentLine.get(key);

                    ElementValueType elementValue = fileFact.createElementValueType();
                    elementValue.setElementRef(key);
                    elementValue.setValue(expression);
                    pType.getElementValue().add(elementValue);
                }
            }
        } else if (EParameterFieldType.isPassword(param.getFieldType()) && value instanceof String) {
            pType.setRawValue((String) value);
        } else {
            if (value == null) {
                pType.setValue(""); //$NON-NLS-1$
            } else {
                if (value instanceof Boolean) {
                    pType.setValue(((Boolean) value).toString());
                } else {
                    if (value instanceof String) {
                        pType.setValue((String) value);
                    }
                }
            }
        }
        pType.setShow(param.isShow(paramList));
        listParamType.add(pType);
    }

    private boolean isTable(final IElementParameter parameter) {
        return parameter.getFieldType().equals(EParameterFieldType.TABLE) ||
                parameter.getFieldType().equals(EParameterFieldType.TACOKIT_SUGGESTABLE_TABLE);
    }

    protected boolean isNeedConvertToHex(String value) {
        if (value == null || "".equals(value.trim())) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            int ch = value.charAt(i);
            if (ch < 32) {
                return true;
            }
        }
        return false;
    }

    private void loadElementParameters(Element elemParam, EList listParamType) {
        loadElementParameters(elemParam, listParamType, false);
    }

    protected void loadElementParameters(Element elemParam, EList listParamType, boolean isJunitLoad) {
        ElementParameterType pType;
        // if it's a generic component,try to serialize the component to json,then save all in a new ElementParameter,
        // that name: PROPERTIES".Then later when load the job, if the component loaded is a generic component... if
        // yes,then deserialize the json to get back the properties / set each element parameter.
        // Check if it's a generic component
        boolean generic = false;
        IComponent component = null;
        if (elemParam instanceof Node) {
            component = ((INode) elemParam).getComponent();
            if (EComponentType.GENERIC.equals(component.getComponentType())) {
                generic = true;
            }
        }
        if (generic) {
            for (Object element : listParamType) {
                pType = (ElementParameterType) element;
                if (pType != null) {
                    if ("PROPERTIES".equals(pType.getName())) {//$NON-NLS-1$
                        String pTypeValue = pType.getValue();
                        if (pTypeValue != null) {
                            if (component != null && component instanceof AbstractBasicComponent) {
                                AbstractBasicComponent comp = (AbstractBasicComponent) component;
                                comp.initNodePropertiesFromSerialized((INode) elemParam, pTypeValue);
                                List<? extends IElementParameter> paramList = elemParam.getElementParameters();
                                for (int m = 0; m < paramList.size(); m++) {
                                    IElementParameter param = paramList.get(m);
                                    if (!param.isSerialized()) {
                                        continue;
                                    }
                                    Object object = comp
                                            .getElementParameterValueFromComponentProperties((INode) elemParam, param);
                                    param.setValue(object);
                                }
                            }
                        }
                    } else {
                        IElementParameter param = null;
                        if (EParameterFieldType.SURVIVOR_RELATION.name().equals(pType.getField())) {
                            param = new ElementParameter(elemParam);
                            param.setValue(pType.getValue());
                            param.setName(pType.getName());
                            param.setCategory(EComponentCategory.TECHNICAL);
                            param.setFieldType(EParameterFieldType.SURVIVOR_RELATION);
                            param.setNumRow(99);
                            param.setShow(false);
                            param.setReadOnly(false);
                            elemParam.addElementParameter(param);
                            param = null;
                            continue;
                        }

                        param = elemParam.getElementParameter(pType.getName());
                        if (param != null) {
                            if ((param.isReadOnly() && !isJunitLoad && noNeedSetValue(param, pType.getValue()))
                                    && !(param.getName().equals(EParameterName.UNIQUE_NAME.getName()) || param.getName().equals(
                                            EParameterName.VERSION.getName()))) {
                                continue;
                            }
                            //
                            loadElementParameters(elemParam, pType, param, pType.getName(), pType.getValue(), false);
                        }
                    }
                }
            }
        } else {
            String tempLabel = null;
            String tempParaName = null;
            for (Object element : listParamType) {
                pType = (ElementParameterType) element;
                if (pType != null) {
                    IElementParameter param = null;
                    if (EParameterFieldType.SURVIVOR_RELATION.name().equals(pType.getField())) {
                        param = new ElementParameter(elemParam);
                        param.setValue(pType.getValue());
                        param.setName(pType.getName());
                        param.setCategory(EComponentCategory.TECHNICAL);
                        param.setFieldType(EParameterFieldType.SURVIVOR_RELATION);
                        param.setNumRow(99);
                        param.setShow(false);
                        param.setReadOnly(false);
                        elemParam.addElementParameter(param);
                        param = null;
                        continue;
                    }
                    param = elemParam.getElementParameter(pType.getName());
                    if (pType.getField() != null && pType.getField().equals(EParameterFieldType.DBTABLE.getName())
                            && param == null) {
                        tempLabel = pType.getValue();
                        tempParaName = pType.getName();
                    }
                    if (param != null) {
                        String paraValue = pType.getValue();
                        if ((param.isReadOnly() && !isJunitLoad && noNeedSetValue(param, paraValue))
                                && !(param.getName().equals(EParameterName.UNIQUE_NAME.getName()) || param.getName().equals(
                                        EParameterName.VERSION.getName()))) {
                            continue;
                        }
                        if (pType.getName().equals(EParameterName.LABEL.getName()) && tempLabel != null) {
                            if (tempParaName != null && pType.getValue().equals(DesignerUtilities.getParameterVar(tempParaName))) {
                                paraValue = tempLabel;
                            }
                        }
                        loadElementParameters(elemParam, pType, param, pType.getName(), paraValue, false);
                    } else {
                        boolean canAddElementParameter = false;
                        boolean isActiveDatabase = false;
                        String paramName = pType.getName();
                        if (EParameterName.ACTIVE_DATABASE_DELIMITED_IDENTIFIERS.getName().equals(paramName)) {
                            canAddElementParameter = true;
                            isActiveDatabase = true;
                        }
                        if (canAddElementParameter) {
                            param = new ElementParameter(elemParam);
                            param.setValue(pType.getValue());
                            param.setName(pType.getName());
                            param.setCategory(EComponentCategory.TECHNICAL);
                            String fieldName = pType.getField();
                            if (isActiveDatabase && fieldName == null) {
                            	fieldName = EParameterFieldType.TEXT.getName();
                            }
                            EParameterFieldType fieldType = null;
                            if (StringUtils.isNotBlank(fieldName)) {
                                fieldType = EParameterFieldType.valueOf(fieldName);
                            }
                            if (fieldType == null) {
                                ExceptionHandler.process(new Exception("Can't find filed of " + fieldName));
                                continue;
                            }
                            param.setFieldType(fieldType);
                            param.setNumRow(99);
                            param.setShow(false);
                            param.setReadOnly(false);
                            elemParam.addElementParameter(param);
                            param = null;
                            continue;
                        }
                    }
                }
            }
        }
    }

    protected boolean noNeedSetValue(IElementParameter param, String paraValue) {
        if (paraValue == null) {
            return true;
        }
        for (IElementParameterDefaultValue defaultValue : param.getDefaultValues()) {
            if (defaultValue.getDefaultValue() != null && defaultValue.getDefaultValue().equals(paraValue)) {
                return true;
            }
        }
        return false;
    }

    private void loadElementParameters(Element elemParam, ElementParameterType pType, IElementParameter param, String key,
            Object object, boolean isJunitLoad) {
        if (param != null) {
            if (pType.isSetContextMode()) {
                param.setContextMode(pType.isContextMode());
            } else {
                param.setContextMode(false);
            }
            String value = null;
            if (object != null) {
                value = object.toString();
            }
            if (param.getFieldType().equals(EParameterFieldType.CHECK) || param.getFieldType().equals(EParameterFieldType.RADIO)) {
                if ("false".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value) || !pType.isContextMode()) { //$NON-NLS-1$ //$NON-NLS-2$
                    Boolean boolean1 = new Boolean(value);
                    elemParam.setPropertyValue(key, boolean1);
                } else {
                    elemParam.setPropertyValue(key, value);
                }
            } else if (param.getFieldType().equals(EParameterFieldType.CLOSED_LIST)) {
                boolean valueSet = false;
                if (!ArrayUtils.contains(param.getListItemsValue(), value)) {
                    if (ArrayUtils.contains(param.getListItemsDisplayName(), value)) {
                        valueSet = true;
                        int index = ArrayUtils.indexOf(param.getListItemsDisplayName(), value);
                        elemParam.setPropertyValue(key, param.getListItemsValue()[index]);
                    }
                }
                if (!valueSet) {
                    elemParam.setPropertyValue(key, value);
                }
                if (param.getName().equals(EParameterName.DB_TYPE.getName())) {
                    IElementParameter elementParameter = elemParam.getElementParameter(EParameterName.DB_VERSION.getName());
                    JobSettingVersionUtil.setDbVersion(elementParameter, value, true);
                    IElementParameter elementParameter2 = elemParam.getElementParameter(EParameterName.SCHEMA_DB.getName());
                    DesignerUtilities.setSchemaDB(elementParameter2, param.getValue());
                } else if (param.getName().equals(JobSettingsConstants.getExtraParameterName(EParameterName.DB_TYPE.getName()))) {
                    IElementParameter elementParameter = elemParam.getElementParameter(JobSettingsConstants
                            .getExtraParameterName(EParameterName.DB_VERSION.getName()));
                    JobSettingVersionUtil.setDbVersion(elementParameter, value, true);
                    IElementParameter elementParameter2 = elemParam.getElementParameter(JobSettingsConstants
                            .getExtraParameterName(EParameterName.SCHEMA_DB.getName()));
                    DesignerUtilities.setSchemaDB(elementParameter2, param.getValue());
                }
            } else if (isTable(param)) {
                List<Map<String, Object>> tableValues = new ArrayList<Map<String, Object>>();
                String[] codeList = param.getListItemsDisplayCodeName();
                Map<String, Object> lineValues = null;
                for (ElementValueType elementValue : (List<ElementValueType>) pType.getElementValue()) {
                    boolean found = false;
                    for (int i = 0; i < codeList.length && !found; i++) {
                        if (codeList[i].equals(elementValue.getElementRef())) {
                            found = true;
                        }
                    }
                    if (found) {
                        if ((lineValues == null) || (lineValues.get(elementValue.getElementRef()) != null)) {
                            lineValues = new LinkedHashMap<String, Object>();
                            tableValues.add(lineValues);
                        }
                        boolean needRemoveQuotes = false;
                        IElementParameter tmpParam = null;
                        for (Object o : param.getListItemsValue()) {
                            if (o instanceof IElementParameter) {
                                IElementParameter tableParam = (IElementParameter) o;
                                if (tableParam.getName().equals(elementValue.getElementRef())) {
                                    tmpParam = tableParam;
                                    if (tableParam.getFieldType() == EParameterFieldType.CONNECTION_LIST) {
                                        needRemoveQuotes = true;
                                    }
                                }
                            }
                        }

                        String elemValue = elementValue.getValue();
                        if (tmpParam != null && EParameterFieldType.isPassword(tmpParam.getFieldType())) {
                            elemValue = elementValue.getRawValue();
                        }
                        if (elementValue.isHexValue() && elemValue != null) {
                            byte[] decodeBytes = Hex.decodeHex(elemValue.toCharArray());
                            try {
                                elemValue = new String(decodeBytes, UTF8);
                            } catch (UnsupportedEncodingException e) {
                                ExceptionHandler.process(e);
                            }
                        }
                        if (needRemoveQuotes) {
                            elemValue = TalendTextUtils.removeQuotes(elemValue);
                        }
                        lineValues.put(elementValue.getElementRef(), elemValue);
                        if (elementValue.getType() != null) {
                            lineValues.put(elementValue.getElementRef() + IEbcdicConstant.REF_TYPE, elementValue.getType());
                        }
                    }
                }
                // check missing codes in the table to have the default values.
                for (Map<String, Object> line : tableValues) {
                    for (int i = 0; i < codeList.length; i++) {
                        if (!line.containsKey(codeList[i])) {
                            IElementParameter itemParam = (IElementParameter) param.getListItemsValue()[i];
                            line.put(codeList[i], itemParam.getValue());
                        }
                    }
                }

                elemParam.setPropertyValue(key, tableValues);
            } else if (param.getFieldType().equals(EParameterFieldType.TABLE_BY_ROW)) {
                List<Map<String, Object>> tableValues = new ArrayList<Map<String, Object>>();
                Map<String, Object> lineValues = null;
                for (ElementValueType elementValue : (List<ElementValueType>) pType.getElementValue()) {
                    if ((lineValues == null) || (lineValues.get(elementValue.getElementRef()) != null)) {
                        lineValues = new HashMap<String, Object>();
                        tableValues.add(lineValues);
                    }
                    lineValues.put(elementValue.getElementRef(), elementValue.getValue());
                }
                elemParam.setPropertyValue(key, tableValues);
            } else if (param.getFieldType().equals(EParameterFieldType.ENCODING_TYPE)) {
                // fix for bug 2193
                boolean setToCustom = false;
                if (EmfComponent.REPOSITORY.equals(elemParam.getPropertyValue(EParameterName.PROPERTY_TYPE.getName()))
                        && param.getRepositoryValue() != null && param.getRepositoryValue().equals("ENCODING")) { //$NON-NLS-1$
                    setToCustom = true;
                }
                String tempValue = (String) param.getChildParameters().get(EParameterName.ENCODING_TYPE.getName()).getValue();
                if (!tempValue.equals(EmfComponent.ENCODING_TYPE_CUSTOM)) {
                    tempValue = tempValue.replace("'", ""); //$NON-NLS-1$ //$NON-NLS-2$
                    tempValue = tempValue.replace("\"", ""); //$NON-NLS-1$ //$NON-NLS-2$
                    tempValue = TalendTextUtils.addQuotes(tempValue);
                    if (!tempValue.equals(value)) {
                        setToCustom = true;
                    }
                }

                if (setToCustom) {
                    param.getChildParameters().get(EParameterName.ENCODING_TYPE.getName())
                            .setValue(EmfComponent.ENCODING_TYPE_CUSTOM);
                }
                elemParam.setPropertyValue(key, value);
                // end of fix for bug 2193
            } else if (EParameterFieldType.isPassword(param.getFieldType())) {
                boolean generic = false;
                IComponent component = null;
                if (elemParam instanceof Node) {
                    component = ((INode) elemParam).getComponent();
                    if (EComponentType.GENERIC.equals(component.getComponentType())) {
                        generic = true;
                    }
                }

                Object sourceName = null;
                if (param instanceof ElementParameter) {
                    sourceName = ((ElementParameter) param).getTaggedValue("org.talend.sdk.component.source");
                }
                if (generic && !"tacokit".equalsIgnoreCase(String.valueOf(sourceName))) {
                    param.setValue(value);
                } else {
                    param.setValue(pType.getRawValue());
                }
            } else if (param.getFieldType().equals(EParameterFieldType.SCHEMA_REFERENCE)) {
                elemParam.setPropertyValue(key, object);
            } else if (!param.getFieldType().equals(EParameterFieldType.SCHEMA_TYPE)) {
                if (param.getFieldType().equals(EParameterFieldType.COLOR)) {
                    if (value != null && value.length() > 2) {
                        elemParam.setPropertyValue(key, TalendTextUtils.removeQuotesIfExist(value)); // value.substring(1,
                    }
                } else {
                    elemParam.setPropertyValue(key, value);
                }
            }
        } else if (UpdateTheJobsActionsOnTable.isClear && "CLEAR_TABLE".equals(key) //$NON-NLS-1$
                && "true".equals(pType.getValue()) //$NON-NLS-1$
                && "NONE".equals(elemParam.getElementParameter(Process.TABLE_ACTION).getValue())) { //$NON-NLS-1$
            elemParam.setPropertyValue(Process.TABLE_ACTION, "CLEAR"); //$NON-NLS-1$
            UpdateTheJobsActionsOnTable.isClear = false;
        }
    }

    protected ProcessType createProcessType(TalendFileFactory fileFact) {
        return fileFact.createProcessType();
    }

    /**
     * Save the diagram in a Xml File.
     *
     * @param file
     * @return
     * @throws IOException
     */
    @Override
    public ProcessType saveXmlFile() throws IOException {
        return saveXmlFile(true);
    }

    @Override
    public ProcessType saveXmlFile(boolean checkJoblet) throws IOException {

        init();

        TalendFileFactory fileFact = TalendFileFactory.eINSTANCE;
        ProcessType processType = createProcessType(fileFact);
        processType.setParameters(fileFact.createParametersType());

        saveProcessElementParameters(processType);
        saveRoutinesDependencies(processType);
        saveAdditionalProperties();

        String sourceJobType = ConvertJobsUtil.getJobTypeFromFramework(this.getProperty().getItem());
        String sourceJobFramework = (String) this.getProperty().getAdditionalProperties().get(ConvertJobsUtil.FRAMEWORK);
        if (sourceJobType != null) {
            processType.setJobType(sourceJobType.replace(' ', '_'));
        }
        if (sourceJobFramework != null) {
            processType.setFramework(sourceJobFramework.replace(' ', '_'));
        }

        EList nList = processType.getNode();
        EList cList = processType.getConnection();
        MetadataEmfFactory factory = new MetadataEmfFactory();
        JobletUtil jutil = new JobletUtil();
        // fix for TUP-19820:avoid to save the same node several times
        List<Node> savedNodes = new ArrayList<Node>();
        // save according to elem order to keep zorder (children insertion) in
        // diagram
        for (Element element : elem) {
            if (element instanceof SubjobContainer) {
                saveSubjob(fileFact, processType, (SubjobContainer) element);
                for (NodeContainer container : ((SubjobContainer) element).getNodeContainers()) {
                    if (container instanceof AbstractJobletContainer) {
                        if (checkJoblet && container.getNode().isJoblet()) {
                            AbstractJobletContainer jobletCon = (AbstractJobletContainer) container;
                            saveJobletNode(jobletCon);
                        }
                        if (!savedNodes.contains(container.getNode())) {
                            saveNode(fileFact, processType, nList, cList, container.getNode(), factory);
                            savedNodes.add(container.getNode());
                        }
                    } else {
                        if (!savedNodes.contains(container.getNode())) {
                            saveNode(fileFact, processType, nList, cList, container.getNode(), factory);
                            savedNodes.add(container.getNode());
                        }
                    }
                }
            } else if (element instanceof AbstractJobletContainer) {
                if (checkJoblet && ((AbstractJobletContainer) element).getNode().isJoblet()) {
                    AbstractJobletContainer jobletCon = (AbstractJobletContainer) element;
                    saveJobletNode(jobletCon);
                }
                if (!savedNodes.contains(((NodeContainer) element).getNode())) {
                    saveNode(fileFact, processType, nList, cList, ((NodeContainer) element).getNode(), factory);
                    savedNodes.add(((NodeContainer) element).getNode());
                }
            } else if (element instanceof NodeContainer) {
                if (!savedNodes.contains(((NodeContainer) element).getNode())) {
                    saveNode(fileFact, processType, nList, cList, ((NodeContainer) element).getNode(), factory);
                    savedNodes.add(((NodeContainer) element).getNode());
                }
            } else if (element instanceof Note) {
                saveNote(fileFact, processType, (Note) element);
            }
        }

        /**
         * Save the contexts informations
         */
        processType.setDefaultContext(contextManager.getDefaultContext().getName());
        if (!getScreenshots().isEmpty()) {
            for (String key : getScreenshots().keySet()) {
                processType.getScreenshots().put(key, getScreenshots().get(key));
            }
        }
        // getScreenshots().clear(); // once be saved, set the screenshot to null to free memory
        // getScreenshots().remove(PROCESS_SCREENSHOT_KEY);
        contextManager.saveToEmf(processType.getContext(), true);
        // fixe for TDI-24876
        EmfHelper.removeProxy(processType);
        return processType;

    }

    private void saveSubjob(TalendFileFactory fileFact, ProcessType process, SubjobContainer subjobContainer) {
        SubjobType sj = fileFact.createSubjobType();

        process.getSubjob().add(sj);

        List<? extends IElementParameter> paramList = subjobContainer.getElementParameters();
        saveElementParameters(fileFact, paramList, sj.getElementParameter(), process);
    }

    private void saveNote(TalendFileFactory fileFact, ProcessType process, Note note) {
        NoteType noteType = fileFact.createNoteType();
        noteType.setPosX(note.getLocation().x);
        noteType.setPosY(note.getLocation().y);
        noteType.setSizeWidth(note.getSize().width);
        noteType.setSizeHeight(note.getSize().height);
        noteType.setOpaque(note.isOpaque());
        noteType.setText(note.getText());
        List<? extends IElementParameter> paramList = note.getElementParameters();
        saveElementParameters(fileFact, paramList, noteType.getElementParameter(), process);
        process.getNote().add(noteType);
    }

    protected void saveNode(TalendFileFactory fileFact, ProcessType process, EList nList, EList cList, Node node,
            MetadataEmfFactory factory) {
        NodeType nType;
        List<Connection> connList;
        Connection connec;
        ConnectionType cType;
        List<? extends IElementParameter> paramList;
        List<IMetadataTable> listMetaData;
        EList listParamType;
        EList listMetaType;
        IMetadataTable metaData;
        nType = createNodeType(fileFact, process, nList, node);
        nType.setComponentVersion(node.getComponent().getVersion());
        nType.setComponentName(node.getComponent().getName());
        nType.setPosX(node.getLocation().x);
        nType.setPosY(node.getLocation().y);
        nType.setOffsetLabelX(node.getNodeLabel().getOffset().x);
        nType.setOffsetLabelY(node.getNodeLabel().getOffset().y);
        if ((node.getSize().width != Node.DEFAULT_SIZE) || (node.getSize().height != Node.DEFAULT_SIZE)) {
            nType.setSizeX(node.getSize().width);
            nType.setSizeY(node.getSize().height);
        }
        if (node.getExternalNode() != null) {
            nType.setNodeData(node.getExternalNode().getExternalEmfData());
        }
        // if (node.getNodeContainer() != null) {
        // NodeContainerType ncType = createNodeContainerType(fileFact);
        // nType.setNodeContainer(ncType);
        // EList ncParaType = ncType.getElementParameter();
        // List<? extends IElementParameter> ncParaist = node.getNodeContainer().getElementParameters();
        // saveElementParameters(fileFact, ncParaist, ncParaType, nType);
        // }
        listParamType = nType.getElementParameter();
        paramList = node.getElementParameters();

        saveElementParameters(fileFact, paramList, listParamType, process);
        listMetaType = nType.getMetadata();
        listMetaData = node.getMetadataList();
        for (IMetadataTable element : listMetaData) {
            metaData = element;
            factory.setMetadataTable(metaData);
            listMetaType.add(factory.getMetadataType());
        }

        List<? extends IConnection> outgoingConnections = node.getOutgoingConnections();
        connList = new ArrayList<Connection>();
        for (IConnection connection : outgoingConnections) {
            if (connection instanceof Connection) {
                connList.add((Connection) connection);
            }
        }
        for (Connection element : connList) {
            connec = element;
            cType = fileFact.createConnectionType();
            cType.setSource(node.getUniqueName());
            INode jTarget = connec.getTarget();
            String targetUniqueName = jTarget.getUniqueName();
            if (jTarget instanceof Node) {
                Node jn = (Node) jTarget.getJobletNode();
                if (jn != null) {
                    targetUniqueName = jn.getUniqueName();
                }
            }
            cType.setTarget(targetUniqueName);
            cType.setLabel(connec.getName());
            cType.setLineStyle(connec.getLineStyleId());
            cType.setConnectorName(connec.getConnectorName());
            cType.setOffsetLabelX(connec.getConnectionLabel().getOffset().x);
            cType.setOffsetLabelY(connec.getConnectionLabel().getOffset().y);
            cType.setMetaname(connec.getMetaName());
            int id = connec.getOutputId();
            if (id >= 0) {
                cType.setOutputId(id);
            }
            INode connTarget = connec.getTarget();
            if (connTarget.getJobletNode() != null) {
                connTarget = connTarget.getJobletNode();
            }
            if (connTarget.getComponent().useMerge()) {
                cType.setMergeOrder(connec.getInputId());
            }
            listParamType = cType.getElementParameter();
            paramList = connec.getElementParameters();
            saveElementParameters(fileFact, paramList, listParamType, process);
            cList.add(cType);
        }

        if (node.getExternalNode() != null && node.getExternalNode().getScreenshot() != null) {
            byte[] saveImageToData = ImageUtils.saveImageToData(node.getExternalNode().getScreenshot());
            process.getScreenshots().put(node.getUniqueName(), saveImageToData);
        }
    }

    protected void checkRoutineDependencies() {
        if (routinesDependencies == null) {
            routinesDependencies = new ArrayList<RoutinesParameterType>();
        }
        try {
            Project targetProject = new Project(ProjectManager.getInstance().getProject(getProperty()));
            IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
            Set<IRepositoryViewObject> routines = new HashSet<>();
            routines.addAll(factory.getAll(targetProject, ERepositoryObjectType.ROUTINES));
            List<Project> referenceProjects = ProjectManager.getInstance().getAllReferencedProjects(targetProject, false);
            referenceProjects.stream().forEach(p -> {
                try {
                    routines.addAll(factory.getAll(p, ERepositoryObjectType.ROUTINES).stream()
                            .filter(o -> !((RoutineItem) o.getProperty().getItem()).isBuiltIn()).collect(Collectors.toList()));
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                }
            });
            Set<String> allRoutinesSet = routines.stream().map(o -> o.getProperty().getLabel()).collect(Collectors.toSet());
            Iterator<RoutinesParameterType> iterator = routinesDependencies.iterator();
            while (iterator.hasNext()) {
                RoutinesParameterType routine = iterator.next();
                if (!allRoutinesSet.contains(routine.getName())) {
                    iterator.remove();
                }
            }
            List<String> possibleRoutines = new ArrayList<String>();
            List<String> routinesToAdd = new ArrayList<String>();
            String additionalString = LanguageManager.getCurrentLanguage() == ECodeLanguage.JAVA ? "." : "";
            List<String> routinesAlreadySetup = new ArrayList<String>();
            for (RoutinesParameterType routine : routinesDependencies) {
                routinesAlreadySetup.add(routine.getName());
            }
            for (Project project : referenceProjects) {
                List<IRepositoryViewObject> refRoutines = factory.getAll(project, ERepositoryObjectType.ROUTINES);
                for (IRepositoryViewObject object : refRoutines) {
                    if (!((RoutineItem) object.getProperty().getItem()).isBuiltIn()) {
                        if (!possibleRoutines.contains(object.getLabel()) && !routinesAlreadySetup.contains(object.getLabel())) {
                            possibleRoutines.add(object.getLabel());
                            routines.add(object);
                        }
                    }
                }
            }
            // always add the system, others must be checked
            for (IRepositoryViewObject object : routines) {
                if (((RoutineItem) object.getProperty().getItem()).isBuiltIn()) {
                    if (routinesDependencies.isEmpty()) {
                        routinesToAdd.add(object.getLabel());
                    }
                    // TDI-8323:since we "Add all user routines to job dependencies" in windows preference,the
                    // routinesDependencies will not empty.
                    else if (!routinesAlreadySetup.contains(object.getLabel())) {
                        routinesToAdd.add(object.getLabel());
                    }
                } else if (!routinesAlreadySetup.contains(object.getLabel())) {
                    possibleRoutines.add(object.getLabel());
                }
            }
            for (Project project : referenceProjects) {
                List<IRepositoryViewObject> refRoutines = factory.getAll(project, ERepositoryObjectType.ROUTINES);
                for (IRepositoryViewObject object : refRoutines) {
                    if (!((RoutineItem) object.getProperty().getItem()).isBuiltIn()) {
                        if (!possibleRoutines.contains(object.getLabel()) && !routinesAlreadySetup.contains(object.getLabel())) {
                            possibleRoutines.add(object.getLabel());
                            routines.add(object);
                        }
                    }
                }
            }

            // check possible routines to setup in process
            for (IElementParameter param : (List<IElementParameter>) getElementParametersWithChildrens()) {
                for (String routine : possibleRoutines) {
                    if (!routinesToAdd.contains(routine) && param.getValue() != null && param.getValue() instanceof String
                            && ((String) param.getValue()).contains(routine + additionalString)) {
                        routinesToAdd.add(routine);
                    }
                    checkRoutinesInTable(routinesToAdd, additionalString, param, routine);
                }
            }

            // check possible routines to setup in nodes
            for (INode node : ((List<INode>) getGraphicalNodes())) {
                if (node.isExternalNode()) {
                    IExternalNode eNode = node.getExternalNode();
                    List<String> needToadd = eNode.checkNeededRoutines(possibleRoutines, additionalString);
                    if (needToadd != null) {
                        routinesToAdd.addAll(needToadd);
                    }
                }
                for (IElementParameter param : (List<IElementParameter>) node.getElementParametersWithChildrens()) {
                    for (String routine : possibleRoutines) {
                        if (!routinesToAdd.contains(routine)
                                && param.getValue() != null
                                && param.getValue() instanceof String
                                && (((String) param.getValue()).contains(routine + additionalString) || ((String) param
                                        .getValue()).contains(SOURCE_JAVA_PIGUDF + additionalString + routine))) {
                            routinesToAdd.add(routine);
                        }
                        checkRoutinesInTable(routinesToAdd, additionalString, param, routine);
                    }
                }
                // check possible routines to setup in connections
                for (IConnection connection : ((List<IConnection>) node.getOutgoingSortedConnections())) {
                    for (IElementParameter param : (List<IElementParameter>) connection.getElementParametersWithChildrens()) {
                        for (String routine : possibleRoutines) {
                            if (!routinesToAdd.contains(routine) && param.getValue() != null
                                    && param.getValue() instanceof String
                                    && ((String) param.getValue()).contains(routine + additionalString)) {
                                routinesToAdd.add(routine);
                            }
                            checkRoutinesInTable(routinesToAdd, additionalString, param, routine);
                        }
                    }
                }
            }

            //
            boolean isLimited = false;
            org.talend.core.model.properties.Project currProject = getProject().getEmfProject();
            org.talend.core.model.properties.Project project = ProjectManager.getInstance().getProject(this.property);
            if (currProject != null && project != null && !currProject.equals(project)) {
                int currOrdinal = ProjectHelper.getProjectTypeOrdinal(currProject);
                int ordinal = ProjectHelper.getProjectTypeOrdinal(project);
                if (currOrdinal > ordinal) {
                    isLimited = true;
                }
            }
            if (!isLimited) {
                for (IRepositoryViewObject object : routines) {
                    if (routinesToAdd.contains(object.getLabel()) && !routinesAlreadySetup.contains(object.getLabel())) {
                        RoutinesParameterType routinesParameterType = TalendFileFactory.eINSTANCE.createRoutinesParameterType();
                        routinesParameterType.setId(object.getId());
                        routinesParameterType.setName(object.getLabel());
                        routinesDependencies.add(routinesParameterType);
                    }
                }
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
    }

    private void checkRoutinesInTable(List<String> routinesToAdd, String additionalString, IElementParameter param, String routine) {
        if (param.getFieldType().equals(EParameterFieldType.TABLE) && param.getValue() != null) {
            List<Map<String, Object>> tableValues = (List<Map<String, Object>>) param.getValue();
            for (Map<String, Object> currentLine : tableValues) {
                for (int i = 0; i < param.getListItemsDisplayCodeName().length; i++) {
                    Object o = currentLine.get(param.getListItemsDisplayCodeName()[i]);
                    String strValue = ""; //$NON-NLS-1$
                    if (o instanceof Integer) {
                        IElementParameter tmpParam = (IElementParameter) param.getListItemsValue()[i];
                        if (tmpParam.getListItemsValue().length == 0) {
                            strValue = ""; //$NON-NLS-1$
                        } else {
                            strValue = (String) tmpParam.getListItemsValue()[(Integer) o];
                        }
                    } else {
                        if (o instanceof String) {
                            strValue = (String) o;
                        } else {
                            if (o instanceof Boolean) {
                                strValue = ((Boolean) o).toString();
                            }
                        }
                    }
                    if (!routinesToAdd.contains(routine) && strValue.contains(routine + additionalString)) {
                        routinesToAdd.add(routine);
                    }
                }
            }
        }
    }

    protected void saveProcessElementParameters(ProcessType processType) {
        saveElementParameters(TalendFileFactory.eINSTANCE, this.getElementParameters(), processType.getParameters()
                .getElementParameter(), processType);
    }

    protected void saveAdditionalProperties() {
        Map<Object, Object> additionalMap = getAdditionalProperties();

        if (getProperty() == null) {
            return;
        }

        for (Object key : additionalMap.keySet()) {
            getProperty().getAdditionalProperties().put(key, additionalMap.get(key));
        }

        // remove
        Map<Object, Object> removedAddition = new HashMap<Object, Object>();
        for (Object key : getProperty().getAdditionalProperties().keySet()) {
            if (!additionalMap.containsKey(key)) {
                removedAddition.put(key, getProperty().getAdditionalProperties().get(key));
            }
        }
        for (Object key : removedAddition.keySet()) {
            getProperty().getAdditionalProperties().remove(key);
        }
    }

    protected void saveRoutinesDependencies(ProcessType process) {
        /* if process is joblet,parameters will be null,so that create a new parametertype for joblet */
        if (process.getParameters() == null) {
            ParametersType parameterType = TalendFileFactory.eINSTANCE.createParametersType();
            process.setParameters(parameterType);
        }
        checkRoutineDependencies();
        List<RoutinesParameterType> toAddList = new ArrayList<RoutinesParameterType>();
        boolean found = false;
        for (RoutinesParameterType routineType : routinesDependencies) {
            found = false;
            for (Object o : process.getParameters().getRoutinesParameter()) {
                RoutinesParameterType type = (RoutinesParameterType) o;
                if (StringUtils.equals(type.getId(), routineType.getId())
                        || StringUtils.equals(type.getName(), routineType.getName())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                toAddList.add(EcoreUtil.copy(routineType));
            }
        }
        process.getParameters().getRoutinesParameter().addAll(toAddList);
    }

    public void addGeneratingRoutines(List<RoutinesParameterType> routinesParameters) {
        if (routinesParameters != null) {
            List<RoutinesParameterType> needList = new ArrayList<RoutinesParameterType>();
            boolean found = false;
            for (RoutinesParameterType type : routinesParameters) {
                found = false;
                for (RoutinesParameterType existedType : routinesDependencies) {
                    if (type.getId().equals(existedType.getId()) || type.getName().equals(existedType.getName())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    needList.add(type);
                }
            }
            routinesDependencies.addAll(needList);
        }
    }

    /**
     * DOC qzhang Comment method "createNodeType".
     *
     * @param fileFact
     * @param process
     * @param nList
     * @param node
     * @return
     */
    protected NodeType createNodeType(TalendFileFactory fileFact, ProcessType process, EList nList, Node node) {
        NodeType nType;
        nType = fileFact.createNodeType();
        nList.add(nType);
        return nType;
    }

    protected NodeContainerType createNodeContainerType(TalendFileFactory fileFact) {
        NodeContainerType ncType;
        ncType = fileFact.createNodeContainerType();
        return ncType;
    }

    protected ProcessType getProcessType() {
        final Item processItem = property.getItem();
        if (processItem instanceof ProcessItem) {
            return ((ProcessItem) processItem).getProcess();
        }
        return null;
    }

    /**
     * DOC mhelleboid Comment method "loadXmlFile".
     *
     * @param process
     */
    @Override
    public void loadXmlFile() {
        loadXmlFile(false);
    }

    @Override
    public void loadXmlFile(boolean loadScreenshots) {
        this.loadScreenshots = loadScreenshots;
        init();
        Hashtable<String, Node> nodesHashtable = new Hashtable<String, Node>();

        setActivate(false);

        ProcessType processType = getProcessType();
        EmfHelper.visitChilds(processType);

        if (processType.getParameters() != null) {
            routinesDependencies = new ArrayList<RoutinesParameterType>(processType.getParameters().getRoutinesParameter());
        }

        loadProjectParameters(processType);
        loadAdditionalProperties();

        loadContexts(processType);

        try {
            loadNodes(processType, nodesHashtable);
        } catch (PersistenceException e) {
            // there are some components unloaded.
            return;
        }

        repositoryId = processType.getRepositoryContextId();

        loadConnections(processType, nodesHashtable);

        loadRejectConnector(nodesHashtable);

        // feature 7410
        loadNotes(processType);
        loadSubjobs(processType);

        initExternalComponents();
        initJobletComponents();
        setActivate(true);
        checkStartNodes();
        // (bug 5365)
        checkNodeTableParameters();
        XmiResourceManager resourceManager = new XmiResourceManager();
        if (this.loadScreenshots) {
            // if it is expanding the joblet in job,the property's eResource is null,no need to loadScreenShots
            if (property.eResource() != null) {
                resourceManager.loadScreenshots(property, processType);
            }

        }

        // this fix caused another problem 14736 , project settings should be reload in
        // ImportItemUtil.importItemRecord()
        // bug 16351
        // checkProjectsettingParameters();

        // loadNodeContainer(processType);
        // bug 6158
        this.updateManager.retrieveRefInformation();

        // force a routine dependencies check, in case some dependencies are lost before.
        checkRoutineDependencies();
    }

    private void loadRejectConnector(Hashtable<String, Node> nodesHashtable) {
        Collection<Node> nodes = nodesHashtable.values();
        for (Node node : nodes) {
            ValidationRulesUtil.updateRejectMetatable(node, null);
        }
    }

    @SuppressWarnings("unchecked")
    private void checkNodeTableParameters() {
        for (INode node : getGraphicalNodes()) {
            ColumnListController.updateColumnList(node, null);

            for (IElementParameter param : node.getElementParameters()) {
                if (param.getFieldType() == EParameterFieldType.TABLE) {
                    Object[] itemsValue = param.getListItemsValue();
                    if (itemsValue != null && param.getValue() != null && param.getValue() instanceof List) {
                        List<Map<String, Object>> values = (List<Map<String, Object>>) param.getValue();
                        for (Object element : itemsValue) {
                            if (element instanceof IElementParameter) {
                                IElementParameter columnParam = (IElementParameter) element;
                                if (columnParam.getFieldType() == EParameterFieldType.COLUMN_LIST
                                        || columnParam.getFieldType() == EParameterFieldType.PREV_COLUMN_LIST
                                        || columnParam.getFieldType() == EParameterFieldType.LOOKUP_COLUMN_LIST) {
                                    for (Map<String, Object> columnMap : values) {
                                        Object column = columnMap.get(columnParam.getName());
                                        if (column == null || "".equals(column)) { //$NON-NLS-1$
                                            columnMap.put(columnParam.getName(), columnParam.getDefaultClosedListValue());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * DOC nrousseau Comment method "loadSubjobs".
     *
     * @param processType
     */
    private void loadSubjobs(ProcessType processType) {
        for (Iterator iter = processType.getSubjob().iterator(); iter.hasNext();) {
            SubjobType subjobType = (SubjobType) iter.next();

            SubjobContainer subjobContainer = null;
            // If the process is a SparkStreaming process, then we use an extension of SubjobContainer to display
            // different information
            if (getComponentsType().equals(ComponentCategory.CATEGORY_4_SPARKSTREAMING.getName())) {
                subjobContainer = new SparkStreamingSubjobContainer(this);
            } else {
                subjobContainer = new SubjobContainer(this);
            }

            loadElementParameters(subjobContainer, subjobType.getElementParameter());
            // look for the related node
            Node subjobStartNode = subjobContainer.getSubjobStartNode();
            if (subjobStartNode != null) {
                subjobContainer.addNodeContainer(subjobStartNode.getNodeContainer());
                subjobContainers.add(subjobContainer);
                elem.remove(subjobStartNode.getNodeContainer());
                elem.add(subjobContainer);
                mapSubjobStarts.put(subjobStartNode, subjobContainer);
            }
        }
    }

    private void loadNotes(ProcessType process) {
        for (Iterator iter = process.getNote().iterator(); iter.hasNext();) {
            NoteType noteType = (NoteType) iter.next();
            Note note = new Note();
            note.setLocation(new Point(noteType.getPosX(), noteType.getPosY()));
            note.setSize(new Dimension(noteType.getSizeWidth(), noteType.getSizeHeight()));
            note.setOpaque(noteType.isOpaque());
            note.setText(noteType.getText());
            note.setProcess(this);
            loadElementParameters(note, noteType.getElementParameter());
            addNote(note, false);
        }
    }

    private void initExternalComponents() {
        for (INode node : nodes) {
            if (node.isExternalNode()) {
                node.getExternalNode().initialize();
            }
        }
    }

    private void initJobletComponents() {
        for (INode node : nodes) {
            if ((node instanceof Node) && ((Node) node).isJoblet()) {
                List<? extends IElementParameter> parametersWithChildrens = node.getElementParametersWithChildrens();
                for (IElementParameter param : parametersWithChildrens) {
                    if (param.getFieldType() == EParameterFieldType.COMPONENT_LIST) {
                        if (param.getValue() == null || ((String) param.getValue()).length() <= 0) {
                            ComponentListController.updateComponentList(node, param);
                        }
                    }
                }
            }

        }
    }

    // private void checkProjectsettingParameters() {
    // boolean statsLog = (Boolean) this.getElementParameter(EParameterName.STATANDLOG_USE_PROJECT_SETTINGS.getName())
    // .getValue();
    // if (statsLog) {
    // LoadProjectSettingsCommand command = new LoadProjectSettingsCommand(this,
    // EParameterName.STATANDLOG_USE_PROJECT_SETTINGS.getName(), statsLog);
    // command.execute();
    // }
    // boolean implicit = (Boolean)
    // this.getElementParameter(EParameterName.IMPLICITCONTEXT_USE_PROJECT_SETTINGS.getName())
    // .getValue();
    // if (implicit) {
    // LoadProjectSettingsCommand command = new LoadProjectSettingsCommand(this,
    // EParameterName.IMPLICITCONTEXT_USE_PROJECT_SETTINGS.getName(), implicit);
    // command.execute();
    // }
    // }

    private List<NodeType> unloadedNode = null;

    protected void loadNodes(ProcessType process, Hashtable<String, Node> nodesHashtable) throws PersistenceException {
        EList nodeList;
        NodeType nType;
        nodeList = process.getNode();
        Node nc;

        EList listParamType;
        boolean isCurrentProject = ProjectManager.getInstance().isInCurrentMainProject(this.getProperty());
        unloadedNode = new ArrayList<NodeType>();
        for (Object element : nodeList) {
            nType = (NodeType) element;
            listParamType = nType.getElementParameter();
            String componentName = nType.getComponentName();
            IComponent component = ComponentsFactoryProvider.getInstance().get(componentName, componentsType);
            if (component == null) {
                component = UnifiedComponentUtil.getDelegateComponent(componentName, componentsType);
            }
            if (component != null) {
                if (component.getComponentType() == EComponentType.JOBLET) {
                    if (!isCurrentProject && !componentName.contains(":")) { //$NON-NLS-1$
                        component = getComponentFromRefWithProjectName(componentName, new Project(ProjectManager.getInstance()
                                .getProject(this.getProperty())));
                    }
                    if (component != null) {
                        for (Object element2 : listParamType) {
                            ElementParameterType pType = (ElementParameterType) element2;
                            if (EParameterName.PROCESS_TYPE_VERSION.name().equals(pType.getName())) {
                                String jobletVersion = pType.getValue();
                                if (!RelationshipItemBuilder.LATEST_VERSION.equals(jobletVersion)) {
                                    IJobletProviderService service = GlobalServiceRegister.getDefault()
                                            .getService(IJobletProviderService.class);
                                    if (service != null) {
                                        Property jobletProperty = service.getJobletComponentItem(component);
                                        org.talend.core.model.properties.Project project = ProjectManager.getInstance()
                                                .getProject(jobletProperty);
                                        String projTechLabel = null;
                                        if (project != null) {
                                            projTechLabel = project.getTechnicalLabel();
                                        }
                                        String componentProcessId = jobletProperty.getId();
                                        component = service.setPropertyForJobletComponent(projTechLabel, componentProcessId,
                                                jobletVersion);
                                    }
                                }
                                break;
                            }
                        }
                    }
                }

            }
            if (component == null) {
                unloadedNode.add(nType);
                continue;
            }
            if (EComponentType.GENERIC.equals(component.getComponentType())) {
                if (component instanceof AbstractBasicComponent) {
                    AbstractBasicComponent abbComponent = (AbstractBasicComponent) component;
                    boolean needMigration = component.getVersion() != null
                            && !component.getVersion().equals(nType.getComponentVersion());
                    if (!needMigration) {
                        needMigration = JavaProcessUtil.needMigration(component.getName(), nType.getElementParameter());
                    }
                    abbComponent.setNeedMigration(needMigration);
                }
            }
            nc = loadNode(nType, component, nodesHashtable, listParamType);

        }

        if (!unloadedNode.isEmpty()) {
            List<NodeType> tempNodes = new ArrayList<NodeType>(unloadedNode);
            JobletUtil jobletUtil = new JobletUtil();
            for (NodeType unNode : tempNodes) {
                listParamType = unNode.getElementParameter();
                String componentName = unNode.getComponentName();
                if (!isCurrentProject && !componentName.contains(":")) {
                    componentName = ProjectManager.getInstance().getProject(this.getProperty()).getLabel() + ":" + componentName; //$NON-NLS-1$
                } else if (jobletUtil.matchExpression(componentName) && !jobletUtil.isStrictJoblet()) {
                    String[] names = componentName.split(":"); //$NON-NLS-1$
                    componentName = names[1];
                }
                IComponent component = ComponentsFactoryProvider.getInstance().get(componentName, componentsType);
                if (component == null && jobletUtil.isJoblet(unNode) && !jobletUtil.isStrictJoblet()) {
                    component = ComponentsFactoryProvider.getInstance().getJobletComponent(componentName, componentsType);
                }
                if (component != null) {
                    unloadedNode.remove(unNode);
                    nc = loadNode(unNode, component, nodesHashtable, listParamType);
                }
            }
        }

        if (!unloadedNode.isEmpty()) {
            for (NodeType element : unloadedNode) {
                createDummyNode(element, nodesHashtable);
            }
        }
    }

    private IComponent getComponentFromRefWithProjectName(String componentName, Project project) {
        String componentNameWithPro = project.getLabel() + ":" + componentName; //$NON-NLS-1$
        IComponent component = ComponentsFactoryProvider.getInstance().get(componentNameWithPro, componentsType);
        if (component == null) {
            List<Project> referencedProjects = ProjectManager.getInstance().getReferencedProjects(project);
            for (Project refPro : referencedProjects) {
                component = getComponentFromRefWithProjectName(componentName, refPro);
                if (component != null) {
                    return component;
                }
            }
        }
        return component;
    }

    protected Node createDummyNode(NodeType nType, Hashtable<String, Node> nodesHashtable) {
        DummyComponent component = new DummyComponent(nType);
        Node nc;
        nc = new Node(component, this);
        nc.setLocation(new Point(nType.getPosX(), nType.getPosY()));
        Point offset = new Point(nType.getOffsetLabelX(), nType.getOffsetLabelY());
        nc.getNodeLabel().setOffset(offset);
        if (nType.isSetSizeX()) {
            nc.setSize(new Dimension(nType.getSizeX(), nType.getSizeY()));
        }
        loadElementParameters(nc, nType.getElementParameter());
        NodeContainer nodec = loadNodeContainer(nc, false);
        loadSchema(nc, nType);
        addNodeContainer(nodec);
        nodesHashtable.put(nc.getUniqueName(), nc);
        return nc;
    }

    /**
     * DOC qzhang Comment method "loadNode".
     *
     * @param nType
     * @param component
     * @return
     */
    protected Node loadNode(NodeType nType, IComponent component, Hashtable<String, Node> nodesHashtable, EList listParamType) {
        return loadNode(nType, component, nodesHashtable, listParamType, false);
    }

    /**
     * DOC qzhang Comment method "loadNode".
     *
     * @param nType
     * @param component
     * @return
     */
    protected Node loadNode(NodeType nType, IComponent component, Hashtable<String, Node> nodesHashtable, EList listParamType,
            boolean isJunitContainer) {
        Node nc;
        nc = new Node(component, this);
        nc.setLocation(new Point(nType.getPosX(), nType.getPosY()));
        Point offset = new Point(nType.getOffsetLabelX(), nType.getOffsetLabelY());
        nc.getNodeLabel().setOffset(offset);
        if (nType.isSetSizeX()) {
            nc.setSize(new Dimension(nType.getSizeX(), nType.getSizeY()));
        }

        loadElementParameters(nc, listParamType);
        if (nodesHashtable.containsKey(nc.getUniqueName())) {
            // if the uniquename is already in the list, there must be a problem with the job.
            // simply don't load the component
            return null;
        }

        // update the value of process type
        IElementParameter processParam = nc.getElementParameterFromField(EParameterFieldType.PROCESS_TYPE);

        if (processParam != null) {
            IElementParameter processIdParam = processParam.getChildParameters().get(
                    EParameterName.PROCESS_TYPE_PROCESS.getName());
            IElementParameter processVersionParam = processParam.getChildParameters().get(
                    EParameterName.PROCESS_TYPE_VERSION.getName());
            ProcessItem processItem = null;
            if (processVersionParam != null) {
                processItem = ItemCacheManager.getProcessItem((String) processIdParam.getValue(),
                        (String) processVersionParam.getValue());
            } else {
                processItem = ItemCacheManager.getProcessItem((String) processIdParam.getValue());
            }
            if (processItem != null) {
                org.talend.core.model.properties.Project project = ProjectManager.getInstance().getProject(processItem.getProperty());
                String itemLabel = ProcessUtils.getProjectProcessLabel(project.getTechnicalLabel(),processItem.getProperty().getLabel());
                nc.setPropertyValue(processParam.getName(), itemLabel);
            }
        }
        // nc.setData(nType.getBinaryData(), nType.getStringData());
        if (nc.getExternalNode() != null && nType.getNodeData() != null) {
            nc.getExternalNode().buildExternalData(EcoreUtil.copy(nType.getNodeData()));
            nc.setExternalData(nc.getExternalNode().getExternalData());
            nc.getExternalNode().setOriginalNode(nc);
        }

        loadSchema(nc, nType);

        // add a reject connector if the node has validation rule.
        ValidationRulesUtil.createRejectConnector(nc);

        loadColumnsBasedOnSchema(nc, listParamType);
        NodeContainer nodeContainer = loadNodeContainer(nc, isJunitContainer);

        addNodeContainer(nodeContainer);
        nodesHashtable.put(nc.getUniqueName(), nc);
        updateAllMappingTypes();
        nc.setNeedLoadLib(false);
        if (nc.isJoblet()) {
            IJobletProviderService service = GlobalServiceRegister.getDefault().getService(
                    IJobletProviderService.class);
            if (service != null) {
                // reload only for stuido ,because joblet can be changed in the job editor
                service.reloadJobletProcess(nc, !CommonsPlugin.isHeadless());
            }
        }
        return nc;
    }

    public NodeContainer loadNodeContainer(Node node, boolean isJunitContainer) {
        NodeContainer nodeContainer = null;
        if (isJunitContainer) {
            if (GlobalServiceRegister.getDefault().isServiceRegistered(ITestContainerGEFService.class)) {
                ITestContainerGEFService testContainerService = GlobalServiceRegister.getDefault()
                        .getService(ITestContainerGEFService.class);
                if (testContainerService != null) {
                    nodeContainer = testContainerService.createJunitContainer(node);
                }
            }
        } else if (node.isJoblet()) {
            nodeContainer = new JobletContainer(node);
        } else if (node.isMapReduce()) {
            nodeContainer = new JobletContainer(node);
        } else {
            nodeContainer = new NodeContainer(node);
        }
        return nodeContainer;
    }

    protected void loadColumnsBasedOnSchema(Node nc, EList listParamType) {
        List<IMetadataTable> metadataList = nc.getMetadataList();
        if (listParamType == null || metadataList == null || metadataList.size() == 0) {
            return;
        }
        List<IMetadataColumn> listColumns = metadataList.get(0).getListColumns();
        if (listColumns == null) {
            return;
        }
        for (IElementParameter parameter : nc.getElementParameters()) {
            if (EParameterFieldType.TABLE.equals(parameter.getFieldType()) && parameter.isColumnsBasedOnSchema()) {

                String[] listItemsDisplayValue = new String[listColumns.size()];
                String[] listItemsDisplayCodeValue = new String[listColumns.size()];
                Object[] listItemsValue = new Object[listColumns.size()];
                ElementParameter newParam = null;
                for (int i = 0; i < listColumns.size(); i++) {
                    IMetadataColumn column = listColumns.get(i);
                    listItemsDisplayCodeValue[i] = column.getLabel();
                    listItemsDisplayValue[i] = column.getLabel();
                    newParam = new ElementParameter(nc);
                    newParam.setName(column.getLabel());
                    newParam.setDisplayName(""); //$NON-NLS-1$
                    newParam.setFieldType(EParameterFieldType.TEXT);
                    newParam.setValue(""); //$NON-NLS-1$
                    listItemsValue[i] = newParam;
                }
                parameter.setListItemsDisplayName(listItemsDisplayValue);
                parameter.setListItemsDisplayCodeName(listItemsDisplayCodeValue);
                parameter.setListItemsValue(listItemsValue);

                for (Object element : listParamType) {
                    ElementParameterType pType = (ElementParameterType) element;
                    if (pType != null) {
                        if (parameter.getName().equals(pType.getName())) {
                            List<Map<String, Object>> tableValues = new ArrayList<Map<String, Object>>();
                            String[] codeList = parameter.getListItemsDisplayCodeName();
                            Map<String, Object> lineValues = null;
                            for (ElementValueType elementValue : (List<ElementValueType>) pType.getElementValue()) {
                                boolean found = false;
                                for (int i = 0; i < codeList.length && !found; i++) {
                                    if (codeList[i].equals(elementValue.getElementRef())) {
                                        found = true;
                                    }
                                }
                                if (found) {
                                    if ((lineValues == null) || (lineValues.get(elementValue.getElementRef()) != null)) {
                                        lineValues = new HashMap<String, Object>();
                                        tableValues.add(lineValues);
                                    }
                                    String elemValue = elementValue.getValue();
                                    if (elementValue.isHexValue() && elemValue != null) {
                                        byte[] decodeBytes = Hex.decodeHex(elemValue.toCharArray());
                                        try {
                                            elemValue = new String(decodeBytes, UTF8);
                                        } catch (UnsupportedEncodingException e) {
                                            ExceptionHandler.process(e);
                                        }
                                    }
                                    lineValues.put(elementValue.getElementRef(), elemValue);
                                    if (elementValue.getType() != null) {
                                        lineValues.put(elementValue.getElementRef() + IEbcdicConstant.REF_TYPE,
                                                elementValue.getType());
                                    }
                                }
                            }
                            // check missing codes in the table to have the default values.
                            for (Map<String, Object> line : tableValues) {
                                for (int i = 0; i < codeList.length; i++) {
                                    if (!line.containsKey(codeList[i])) {
                                        IElementParameter itemParam = (IElementParameter) parameter.getListItemsValue()[i];
                                        line.put(codeList[i], itemParam.getValue());
                                    }
                                }
                            }

                            nc.setPropertyValue(pType.getName(), tableValues);
                        }
                    }
                }

            }

        }

    }

    /**
     * to optimize.
     */
    private void updateAllMappingTypes() {
        for (INode node : this.getGraphicalNodes()) {
            for (IElementParameter param : node.getElementParameters()) {
                if (param.getFieldType().equals(EParameterFieldType.MAPPING_TYPE)) {
                    for (IMetadataTable table : node.getMetadataList()) {
                        table.setDbms((String) param.getValue());
                    }
                }
            }
        }
    }

    /**
     * Checks if there are unloaded nodes.If there are some nodes unloaded, throws PersistenceException.
     *
     * @throws PersistenceException PersistenceException
     */
    @Override
    public void checkLoadNodes() throws PersistenceException {
        if (unloadedNode == null || unloadedNode.isEmpty()) {
            return;
        }
        String errorMessage = null;
        if (unloadedNode.size() == 1) {
            errorMessage = Messages.getString("Process.component.notloaded", unloadedNode.get(0).getComponentName()); //$NON-NLS-1$
        } else {
            StringBuilder curentName = new StringBuilder();
            for (NodeType component : unloadedNode) {
                curentName.append(component.getComponentName()).append(","); //$NON-NLS-1$
            }
            curentName.deleteCharAt(curentName.length() - 1);

            errorMessage = Messages.getString("Process.components.notloaded", curentName.toString()); //$NON-NLS-1$

        }
        PersistenceException ex = new PersistenceException(errorMessage);
        throw ex;
    }

    /**
     * This class will be overrided by SparkProcess in order to use AvroMetadata instead of MetadataTable.
     */
    protected void setMetadatableToFactory(MetadataType mType, MetadataEmfFactory factory) {
        factory.setMetadataType(mType);
    }

    protected void loadSchema(Node nc, NodeType nType) {
        MetadataEmfFactory factory = new MetadataEmfFactory();
        MetadataType mType;
        EList listMetaType;

        List<IMetadataTable> listMetaData;
        listMetaType = nType.getMetadata();
        IMetadataTable metadataTable;
        listMetaData = new ArrayList<IMetadataTable>();
        // bug 6086
        Set<String> listNames = new HashSet<String>();

        for (Object element : listMetaType) {
            mType = (MetadataType) element;
            setMetadatableToFactory(mType, factory);
            metadataTable = factory.getMetadataTable();
            // add by wzhang
            // if a schema exist in node,won't add it again
            if (!listNames.contains(metadataTable.getTableName())) {
                listNames.add(metadataTable.getTableName());
                listMetaData.add(metadataTable);
                if (nc.getConnectorFromType(EConnectionType.FLOW_MAIN) != null
                        && nc.getConnectorFromType(EConnectionType.FLOW_MAIN).isMultiSchema()
                        && checkValidConnectionName(metadataTable.getTableName())) {
                    addUniqueConnectionName(metadataTable.getTableName());
                    // for tmap 11884
                    if (nc.getExternalData() != null) {
                        List<String> joinedTableNames = nc.getExternalData().getJoinedTableNames(metadataTable.getTableName());
                        if (joinedTableNames != null) {
                            for (String joinTableName : joinedTableNames) {
                                addUniqueConnectionName(joinTableName);
                            }
                        }
                    }
                } else {
                    if (metadataTable.getTableName() == null) {
                        metadataTable.setTableName(nc.getUniqueName());
                    }
                }
                MetadataToolHelper.initilializeSchemaFromElementParameters(metadataTable,
                        (List<IElementParameter>) nc.getElementParameters());
                // setup additional properties for sapbapi
                if (MetadataSchemaType.INPUT.name().equals(metadataTable.getTableType())) {
                    Map<String, String> properties = metadataTable.getAdditionalProperties();
                    if (properties != null) {
                        properties.put("isinput", "true"); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
            }
            if (GlobalServiceRegister.getDefault().isServiceRegistered(IScdComponentService.class)) {
                IScdComponentService service = GlobalServiceRegister.getDefault().getService(
                        IScdComponentService.class);
                service.updateOutputMetadata(nc, metadataTable);
            }
        }
        List<IMetadataTable> oldComponentMetadataList = new ArrayList<IMetadataTable>(nc.getMetadataList());
        nc.setMetadataList(listMetaData);

        for (IMetadataTable table : oldComponentMetadataList) {
            if (nc.getMetadataFromConnector(table.getAttachedConnector()) == null) {
                // if there is any new connector, then add the table to the
                // list.
                INodeConnector nodeC = nc.getConnectorFromName(table.getAttachedConnector());
                if (nodeC != null) {
                    String baseSchema = nc.getConnectorFromName(table.getAttachedConnector()).getBaseSchema();
                    IMetadataTable metadataFromConnector = nc.getMetadataFromConnector(baseSchema);
                    if (!table.getAttachedConnector().equals(baseSchema) && metadataFromConnector != null) {
                        MetadataToolHelper.copyTable(metadataFromConnector, table);
                    }
                }
                nc.getMetadataList().add(table);
            }
        }
    }

    /**
     *
     * DOC nrousseau Comment method "checkDifferenceWithRepository".
     *
     * @return true if a difference has been detected
     */
    public boolean checkDifferenceWithRepository() {
        return getUpdateManager().updateAll();
    }

    @Override
    public CommandStack getCommandStack() {
        if (getEditor() != null) {
            Object adapter = ((AbstractMultiPageTalendEditor) getEditor()).getTalendEditor().getAdapter(CommandStack.class);
            return (CommandStack) adapter;
        } else {
            return new CommandStack() {

                /*
                 * (non-Javadoc)
                 *
                 * @see org.eclipse.gef.commands.CommandStack#execute(org.eclipse.gef.commands.Command)
                 */
                @Override
                public void execute(Command command) {
                    command.execute();
                }
            };
        }
    }

    protected void loadConnections(ProcessType process, Hashtable<String, Node> nodesHashtable) {
        loadConnections(process, nodesHashtable, null);
    }

    protected void loadConnections(ProcessType process, Hashtable<String, Node> nodesHashtable, List<INode> testNodes) {
        EList listParamType;
        EList connecList;
        ConnectionType cType;
        connecList = process.getConnection();
        Connection connec = null;
        Node source, target;

        List<String> connectionsProblems = new ArrayList<String>();

        Hashtable<ConnectionType, Connection> connectionsHashtable = new Hashtable<ConnectionType, Connection>();
        List<String> connectionUniqueNames = new ArrayList<String>();
        for (Object element : connecList) {
            cType = (ConnectionType) element;
            source = nodesHashtable.get(cType.getSource());
            target = nodesHashtable.get(cType.getTarget());
            // see the feature 6294
            // qli
            if (source == null || target == null) {
                continue;
            }
            if (testNodes != null) {
                if (!testNodes.contains(target) || !testNodes.contains(source)) {
                    continue;
                }
            }
            Integer lineStyleId = new Integer(cType.getLineStyle());
            String connectorName = cType.getConnectorName();
            boolean connectionTypeFound = false;
            if (connectorName != null) {
                // check if the connector exists and if the line style is
                // correct
                // (used for automatic component upgrade, to avoid migration
                // each time)
                if (source.getConnectorFromName(connectorName) != null
                        && (source.getConnectorFromName(connectorName).getConnectionProperty(
                                EConnectionType.getTypeFromId(lineStyleId)) != null)) {
                    connectionTypeFound = true;
                }
            }
            String connectionLabel = cType.getLabel();

            // fix to correct the bug of the metaname after renaming the output of a tMap
            String metaname = cType.getMetaname();
            if ((source.getComponent().getName().equals("tMap")) && (source.getMetadataTable(metaname) == null)) { //$NON-NLS-1$
                metaname = cType.getLabel();
                // the label should be the original name of the metadata
                if (source.getMetadataTable(metaname) == null) {
                    // this problem should never appear, just in case.
                    if (source.getMetadataList().size() > 0) {
                        metaname = source.getMetadataList().get(0).getTableName();
                    }
                    connectionsProblems.add(connectionLabel);
                }
            }
            // end of fix

            boolean monitorConnection = getConnectionMonitorProperty(cType);
            if (connectionTypeFound) {
                if (!ConnectionManager.checkCircle(source, target)) {
                    connec = new Connection(source, target, EConnectionType.getTypeFromId(lineStyleId), connectorName, metaname,
                            cType.getLabel(), cType.getMetaname(), monitorConnection);
                } else {
                    ExceptionHandler.process(new Exception(Messages.getString("Process.errorCircleConnectionDetected", cType.getLabel(), source.getLabel(), target.getLabel()))); //$NON-NLS-1$
                }
            } else {
                if (PluginChecker.isJobLetPluginLoaded()) { // bug 12764
                    IJobletProviderService service = GlobalServiceRegister.getDefault().getService(
                            IJobletProviderService.class);
                    if (service != null && service.isJobletComponent(source)) {
                        continue;
                    }
                }
                if (!ConnectionManager.checkCircle(source, target)) {
                    EConnectionType type = EConnectionType.getTypeFromId(lineStyleId);
                    connec = new Connection(source, target, type, source.getConnectorFromType(type).getName(), metaname,
                            cType.getLabel(), cType.getMetaname(), monitorConnection);
                } else {
                    ExceptionHandler.process(new Exception(Messages.getString("Process.errorCircleConnectionDetected", //$NON-NLS-1$
                            cType.getLabel(), source.getLabel(), target.getLabel())));
                }
            }
            if (connec == null) {
                continue;
            }
            connectionsHashtable.put(cType, connec);
            listParamType = cType.getElementParameter();
            loadElementParameters(connec, listParamType);
            String uniqueName = connec.getUniqueName();
            if (connec.getLineStyle().hasConnectionCategory(IConnectionCategory.FLOW)) {
                if (!connec.getUniqueName().equals(connec.getName())) {
                    // here force a rename without call the ChangeConnTextCommand
                    // if goes here, it means simply there is a problem since the name is not the same as the unique
                    // name.
                    // we just force the name here since in all case the job was wrong first !
                    connec.setPropertyValue(EParameterName.UNIQUE_NAME.getName(), connec.getName());
                    uniqueName = connec.getName();
                }
            }
            // at this point we should have the uniquename set correctly in the connection.
            boolean isValidName = checkValidConnectionName(uniqueName, false);
            if (!connectionUniqueNames.contains(uniqueName) && isValidName) {
                try {
                    source.getProcess().addUniqueConnectionName(uniqueName);
                } catch (Exception e) {
                    // nothing, since it should be added already in fact.
                }
            } else {
                if (!isValidName) {
                    // fix for TDI-28468 ,some connection uniquename are not valid from some old
                    // version like uniquename = OnSubjobOk (OnSubjobOK(TRIGGER_OUTPUT_1))
                    // if base name is not valid ,generate a new one based on the default link name
                    uniqueName = connec.getLineStyle().getDefaultLinkName();
                }
                uniqueName = source.getProcess().generateUniqueConnectionName(uniqueName);
                if (connec.getLineStyle().hasConnectionCategory(IConnectionCategory.FLOW)) {
                    ChangeConnTextCommand cctc = new ChangeConnTextCommand(connec, uniqueName);
                    cctc.execute();
                } else {
                    if (connec.getLineStyle().equals(EConnectionType.RUN_IF)
                            || connec.getLineStyle().equals(EConnectionType.ITERATE)
                            || connec.getLineStyle().equals(EConnectionType.ON_COMPONENT_ERROR)
                            || connec.getLineStyle().equals(EConnectionType.ON_COMPONENT_OK)
                            || connec.getLineStyle().equals(EConnectionType.ON_SUBJOB_ERROR)
                            || connec.getLineStyle().equals(EConnectionType.ON_SUBJOB_OK)) {
                        // for other LineStyle also should aviod the uniqueName duplicate
                        connec.setUniqueName(uniqueName);
                    }
                    source.getProcess().addUniqueConnectionName(uniqueName);
                }
            }
            connectionUniqueNames.add(uniqueName);

            // if ((!source.isActivate()) || (!target.isActivate())) {
            // connec.setActivate(false);
            // }

            Point offset = new Point(cType.getOffsetLabelX(), cType.getOffsetLabelY());
            INodeConnector nodeConnectorSource = connec.getSourceNodeConnector();
            nodeConnectorSource.setCurLinkNbOutput(nodeConnectorSource.getCurLinkNbOutput() + 1);
            INodeConnector nodeConnectorTarget = connec.getTargetNodeConnector();
            nodeConnectorTarget.setCurLinkNbInput(nodeConnectorTarget.getCurLinkNbInput() + 1);
            connec.getConnectionLabel().setOffset(offset);
        }

        for (INode node : nodes) {
            if (node.getComponent().useMerge()) {
                for (Object element : connecList) {
                    cType = (ConnectionType) element;
                    if (cType.getTarget().equals(node.getUniqueName())) {
                        if (cType.isSetMergeOrder() && connectionsHashtable.get(cType) != null) {
                            Connection connection = connectionsHashtable.get(cType);
                            connection.setInputId(cType.getMergeOrder());
                            connection.updateName();
                        }
                    }
                }
            }
        }

        if (connectionsProblems.size() > 0) {
            String message = this.getLabel() + ":" + Messages.getString("Process.errorLoadingConnectionMessage"); //$NON-NLS-1$
            for (int i = 0; i < connectionsProblems.size(); i++) {
                message += connectionsProblems.get(i);
                if (i < (connectionsProblems.size() - 1)) {
                    message += ","; //$NON-NLS-1$
                }
            }
            final String message2 = message;
            if (!CommonsPlugin.isHeadless()) {
                final Display display = PlatformUI.getWorkbench().getDisplay();
                display.syncExec(new Runnable() {

                    @Override
                    public void run() {
                        MessageBox mb = new MessageBox(DisplayUtils.getDefaultShell(false), SWT.ICON_ERROR);
                        mb.setText(getLabel() + ":" + Messages.getString("Process.errorLoadingConnectionTitle")); //$NON-NLS-1$
                        mb.setMessage(message2);
                        mb.open();
                    }
                });

            } else {
                IStatus status = new Status(IStatus.WARNING, DesignerPlugin.ID, message);
                DesignerPlugin.getDefault().getLog().log(status);
            }
        }
    }

    /**
     *
     * DOC YeXiaowei Comment method "getConnectionMonitorProperty".
     *
     * @param type
     * @return
     */
    @SuppressWarnings("unchecked")
    private boolean getConnectionMonitorProperty(ConnectionType type) {
        EList params = type.getElementParameter();
        if (params == null || params.isEmpty()) {
            return false;
        }
        for (Object param2 : params) {
            ElementParameterType param = (ElementParameterType) param2;
            if (param != null) {
                if (param.getName() != null && param.getName().equals(EParameterName.MONITOR_CONNECTION.getName())) {
                    return Boolean.valueOf(param.getValue());
                }
            }
        }

        return false;
    }

    public void loadContexts() {
        loadContexts(getProcessType());
    }

    private void loadContexts(ProcessType process) {
        /**
         * Load the contexts informations
         */
        String defaultContextToLoad;
        defaultContextToLoad = process.getDefaultContext();

        EList contextList = process.getContext();
        if (contextList == null || contextList.isEmpty()) {
            contextManager = new JobContextManager();
        } else {
            contextManager = new JobContextManager(contextList, defaultContextToLoad);
        }
        updateContextBefore(contextManager);
    }

    /**
     *
     * this method work for the repositoryId existed in process before v2.2.
     *
     */
    private void updateContextBefore(IContextManager contextManager) {
        if (repositoryId != null && !"".equals(repositoryId)) { //$NON-NLS-1$

            ContextItem item = ContextUtils.getContextItemById(ContextUtils.getAllContextItem(), repositoryId);

            for (IContext context : contextManager.getListContext()) {
                for (IContextParameter param : context.getContextParameterList()) {
                    if (item != null && ContextUtils.updateParameterFromRepository(item, param, context.getName())) {
                        param.setSource(item.getProperty().getId());
                    } else {
                        param.setSource(IContextParameter.BUILT_IN);
                    }
                }
            }
        }

    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public boolean checkReadOnly() {
        IProxyRepositoryFactory repFactory = DesignerPlugin.getDefault().getProxyRepositoryFactory();
        boolean readOnlyLocal = !isLastVersion(property.getItem()) || !repFactory.isEditableAndLockIfPossible(property.getItem());
        this.setReadOnly(readOnlyLocal);
        return readOnlyLocal;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;

        for (INode node : nodes) {
            node.setReadOnly(readOnly);
            for (IConnection connec : (List<IConnection>) node.getOutgoingConnections()) {
                connec.setReadOnly(readOnly);
            }
        }

        for (Note note : notes) {
            note.setReadOnly(readOnly);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.Element#getElementName()
     */
    @Override
    public String getElementName() {
        return name;
    }

    @Override
    public String getName() {
        return this.getProperty().getLabel();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IRepositoryProcess#getAuthor()
     */
    @Override
    public User getAuthor() {
        return getProperty().getAuthor();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IRepositoryProcess#getId()
     */
    @Override
    public String getId() {
        return getProperty().getId();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IRepositoryProcess#getLabel()
     */
    @Override
    public String getLabel() {
        return getProperty().getLabel();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IRepositoryProcess#getStatus()
     */
    @Override
    public String getStatusCode() {
        return getProperty().getStatusCode();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IRepositoryProcess#getVersion()
     */
    @Override
    public String getVersion() {
        return getProperty().getVersion();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IRepositoryProcess#setAuthor(org.talend.core.model.temp.User)
     */
    @Override
    public void setAuthor(User author) {
        if (getProperty().getAuthor() == null && author != null || getProperty().getAuthor() != null
                && !getProperty().getAuthor().equals(author)) {
            getProperty().setAuthor(author);
        }
        if (author != null) {
            setPropertyValue(EParameterName.AUTHOR.getName(), author.toString());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IRepositoryProcess#setId(int)
     */
    @Override
    public void setId(String id) {
        if (getProperty().getId() == null && id != null || getProperty().getId() != null && !getProperty().getId().equals(id)) {
            getProperty().setId(id);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IRepositoryProcess#setLabel(java.lang.String)
     */
    @Override
    public void setLabel(String label) {
        if (getProperty().getLabel() == null && label != null || getProperty().getLabel() != null
                && !getProperty().getLabel().equals(label)) {
            getProperty().setLabel(label);
        }
        setPropertyValue(EParameterName.NAME.getName(), label);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IRepositoryProcess#setStatus(org.talend.core.model.process.EProcessStatus)
     */
    @Override
    public void setStatusCode(String statusCode) {
        if (getProperty().getStatusCode() == null && statusCode != null || getProperty().getStatusCode() != null
                && !getProperty().getStatusCode().equals(statusCode)) {
            getProperty().setStatusCode(statusCode);
        }
        setPropertyValue(EParameterName.STATUS.getName(), statusCode);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IRepositoryProcess#setVersion(int)
     */
    @Override
    public void setVersion(String version) {
        if (getProperty().getVersion() == null && version != null || getProperty().getVersion() != null
                && !getProperty().getVersion().equals(version)) {
            getProperty().setVersion(version);
        }
        setPropertyValue(EParameterName.VERSION.getName(), version);
    }

    // private InputStream content;
    private byte[] content;

    private String repositoryId;

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.temp.IXmlSerializable#getXmlStream()
     */
    public InputStream getXmlStream() {
        return new ByteArrayInputStream(content);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.temp.IXmlSerializable#setXmlStream(java.io.InputStream)
     */
    @Override
    public void setXmlStream(InputStream xmlStream) {
        ByteArrayOutputStream st = new ByteArrayOutputStream();

        int byteLu;
        try {
            while ((byteLu = xmlStream.read()) != -1) {
                st.write(byteLu);
            }
        } catch (IOException e) {
            // TODO SML Auto-generated catch block
            // e.printStackTrace();
            ExceptionHandler.process(e);
        } finally {
            try {
                xmlStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        this.content = st.toByteArray();
    }

    public boolean isActivate() {
        return activate;
    }

    @Override
    public void setActivate(boolean activate) {
        this.activate = activate;
    }

    /**
     * Check if the given name will be unique in the process. If another link already exists with that name, false will
     * be returned.
     *
     * @param uniqueName
     * @param checkEsists
     * @return true if the name is unique
     */
    @Override
    public boolean checkValidConnectionName(String connectionName, boolean checkExists) {
        // test if name already exist but with ignore case (contains test only with same case)

        if (checkExists && checkIgnoreCase(connectionName)) {
            return false;
        }

        if (!matcher.matches(connectionName, pattern)) {
            return false;
        }

        return !KeywordsValidator.isKeyword(connectionName);
    }

    // hshen
    // qli modified to fix the bug "7312".
    public boolean checkIgnoreCase(String connectionName) {
        if (connectionName.equals("")) {//$NON-NLS-1$
            return true;
        }
        if (uniqueConnectionNameList != null) {
            for (String value : uniqueConnectionNameList) {
                if (value.equalsIgnoreCase(connectionName)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check if the given name will be unique in the process. If another link already exists with that name, false will
     * be returned.
     *
     * @param uniqueName
     * @return true if the name is unique
     */
    @Override
    public boolean checkValidConnectionName(String connectionName) {
        return checkValidConnectionName(connectionName, true);
    }

    /**
     * Manage to find a unique name with the given name.
     *
     * @param titleName
     */
    @Override
    public String generateUniqueConnectionName(String baseName) {
        if (baseName == null) {
            throw new IllegalArgumentException("baseName can't be null"); //$NON-NLS-1$
        }
        String uniqueName = baseName + 1;

        int counter = 1;
        boolean exists = true;
        while (exists) {
            exists = !checkValidConnectionName(uniqueName);
            if (!exists) {
                break;
            }
            uniqueName = baseName + counter++;
        }
        return uniqueName;
    }

    @Override
    public String generateUniqueConnectionName(String baseName, String tableName) {
        if (baseName == null || tableName == null) {
            throw new IllegalArgumentException("baseName or tableName can't be null"); //$NON-NLS-1$
        }
        String uniqueName = baseName + 1;

        int counter = 1;
        boolean exists = true;
        String fullName = "";
        while (exists) {
            fullName = uniqueName + "_" + tableName;
            exists = !checkValidConnectionName(fullName);
            if (!exists) {
                break;
            }
            uniqueName = baseName + counter++;
        }
        return fullName;
    }

    @Override
    public void addUniqueConnectionName(String uniqueConnectionName) {
        if (uniqueConnectionName != null) {
            if (checkValidConnectionName(uniqueConnectionName)) {
                uniqueConnectionNameList.add(uniqueConnectionName);
            } else {
                throw new IllegalArgumentException("The name of the connection is not valid: " + uniqueConnectionName); //$NON-NLS-1$
            }
        }
    }

    @Override
    public void removeUniqueConnectionName(String uniqueConnectionName) {
        if (uniqueConnectionName != null) {
            uniqueConnectionNameList.remove(uniqueConnectionName);
        }
    }

    public String generateUniqueNodeName(INode node) {
        IComponent component = node.getComponent();
        if (node instanceof Node) {
            component = ((Node) node).getDelegateComponent();
        }
        String baseName = component.getOriginalName();
        if (EComponentType.GENERIC.equals(component.getComponentType())) {
            baseName = component.getDisplayName();
        }
        return UniqueNodeNameGenerator.generateUniqueNodeName(baseName, uniqueNodeNameList);
    }

    /**
     * This function will take a unique name and update the list with the given name. This function should be private
     * only and should be called only when the xml file is loaded.
     *
     * @param uniqueName
     */
    @Override
    public void addUniqueNodeName(final String uniqueName) {
        if (!uniqueNodeNameList.contains(uniqueName)) {
            uniqueNodeNameList.add(uniqueName);
        }
    }

    @Override
    public void removeUniqueNodeName(final String uniqueName) {
        if (uniqueName != null && !uniqueName.equals("")) { //$NON-NLS-1$
            uniqueNodeNameList.remove(uniqueName);
        }
    }


    @SuppressWarnings("unchecked")
    private void setActivateSubjob(INode node, boolean active, INode activateNode, boolean oneComponent, List chekedNodes) {
        INode mainSubProcess = node.getSubProcessStartNode(false);
        chekedNodes.add(node);
        // if the selected node is the start node, then everything will be
        // desacticated
        if (activateNode.isStart()) {
            for (Connection connec : (List<Connection>) node.getIncomingConnections()) {
                if (connec.getSource().isActivate() != active || !chekedNodes.contains(connec.getSource())) {
                    if (connec.getLineStyle().hasConnectionCategory(IConnectionCategory.DATA)
                            || connec.getLineStyle().hasConnectionCategory(IConnectionCategory.USE_ITERATE)) {
                        if (connec.getSource().getSubProcessStartNode(false).isActivate() != active) {
                            setActivateSubjob(connec.getSource().getSubProcessStartNode(false), active, activateNode,
                                    oneComponent, chekedNodes);
                        }
                    }
                }
            }
            ((Element) node).setPropertyValue(EParameterName.ACTIVATE.getName(), new Boolean(active));
            for (Connection connec : (List<Connection>) node.getOutgoingConnections()) {
                if (!oneComponent) {
                    if (connec.getTarget().isActivate() != active || !chekedNodes.contains(connec.getTarget())) {
                        setActivateSubjob(connec.getTarget(), active, activateNode, oneComponent, chekedNodes);
                    }
                } else {
                    if (connec.getLineStyle().hasConnectionCategory(IConnectionCategory.DATA)
                            || connec.getLineStyle().hasConnectionCategory(IConnectionCategory.USE_ITERATE)) {
                        if (connec.getTarget().isActivate() != active || !chekedNodes.contains(connec.getTarget())) {
                            setActivateSubjob(connec.getTarget(), active, activateNode, oneComponent, chekedNodes);
                        }
                    }
                }

            }

        } else {
            if (node.getSubProcessStartNode(false).equals(mainSubProcess)) {
                ((Element) node).setPropertyValue(EParameterName.ACTIVATE.getName(), new Boolean(active));
                for (Connection connec : (List<Connection>) node.getIncomingConnections()) {
                    if (connec.getLineStyle().hasConnectionCategory(IConnectionCategory.DATA)
                            || connec.getLineStyle().hasConnectionCategory(IConnectionCategory.USE_ITERATE)) {
                        if (connec.getSource().isActivate() != active || !chekedNodes.contains(connec.getSource())) {
                            setActivateSubjob(connec.getSource(), active, activateNode, oneComponent, chekedNodes);
                        }
                    }
                }
                for (Connection connec : (List<Connection>) node.getOutgoingConnections()) {
                    if (connec.getLineStyle().hasConnectionCategory(IConnectionCategory.DATA)
                            || connec.getLineStyle().hasConnectionCategory(IConnectionCategory.USE_ITERATE)) {
                        if (connec.getTarget().isActivate() != active || !chekedNodes.contains(connec.getTarget())) {
                            setActivateSubjob(connec.getTarget(), active, activateNode, oneComponent, chekedNodes);
                        }
                    }
                }
            }
            ((Element) node).setPropertyValue(EParameterName.ACTIVATE.getName(), new Boolean(active));
        }
    }

    public void setActivateSubjob(Node node, boolean active, boolean oneComponent) {
        // desactive first the process to avoid to check the process during the
        // activation / desactivation
        setActivate(false);
        List chekedNodes = new ArrayList();
        setActivateSubjob(node, active, node, oneComponent, chekedNodes);
        // now that everything is set, reactivate the process
        setActivate(true);
    }

    @Override
    public void checkStartNodes() {
        for (INode node : new ArrayList<>(nodes)) {
            if ((Boolean) node.getPropertyValue(EParameterName.STARTABLE.getName())) {
                if (node.isActivate()) {
                    boolean checkIfCanBeStart = node.checkIfCanBeStart();
                    node.setStart(checkIfCanBeStart);
                }
            }
        }
        if (!isDuplicate()) {
            for (INode inode : nodes) {
                Node node = (Node) inode;
                node.calculateSubtreeStartAndEnd();
            }
            ConnectionListController.updateConnectionList(this);
            updateSubjobContainers();
        }
    }

    @Override
    public int getMergelinkOrder(final INode node) {
        return getMergelinkOrder(node, new HashSet<INode>());
    }

    /**
     * If the node link with the merge node, it will return the merge link order, or it will return -1 Purpose: only in
     * the branch of the first merge link can be as a start node.
     *
     * @param node
     * @return
     */
    private int getMergelinkOrder(final INode node, final Set<INode> checkedNode) {

        List<? extends IConnection> outgoingConnections = node.getOutgoingConnections();
        int returnValue = -1;
        checkedNode.add(node);
        for (int i = 0; (i < outgoingConnections.size()) && (returnValue == -1); i++) {
            IConnection connec = outgoingConnections.get(i);
            if (connec.isActivate()) {
                if (connec.getLineStyle().hasConnectionCategory(EConnectionType.MERGE)) {
                    returnValue = connec.getInputId();
                    break;
                } else if (connec.getLineStyle().hasConnectionCategory(EConnectionType.MAIN) && connec.getTarget() != null
                        && (!connec.getTarget().equals(node)) && (!checkedNode.contains(connec.getTarget()))) {
                    returnValue = getMergelinkOrder(connec.getTarget(), checkedNode);
                }
            }
        }

        return returnValue;
    }

    @Override
    public boolean isThereLinkWithHash(final INode node) {
        return isThereLinkWithHash(node, new HashSet<INode>());
    }

    /**
     * This function check if in this subprocess there should be a start or not depends on the ref links. If in this
     * subprocess there is only one main flow and one ref then this function will return true. If there is several flow
     * in the output of one component in this subprocess,it'll return false.
     *
     * @param node
     * @return
     */
    public boolean isThereLinkWithHash(final INode node, final Set<INode> checkedNode) {
        boolean refLink = false;

        if (checkedNode.contains(node)) {
            return false;
        }
        checkedNode.add(node);

        for (int i = 0; i < node.getOutgoingConnections().size() && !refLink; i++) {
            IConnection connec = node.getOutgoingConnections().get(i);
            if (connec.isActivate()) {
                if (connec.getLineStyle().hasConnectionCategory(IConnectionCategory.USE_HASH)) {
                    refLink = true;
                } else {
                    if (connec.getLineStyle().equals(EConnectionType.FLOW_MAIN)
                            || connec.getLineStyle().equals(EConnectionType.FLOW_MERGE)
                            || connec.getLineStyle().equals(EConnectionType.ITERATE)
                            || connec.getLineStyle().hasConnectionCategory(IConnectionCategory.EXECUTION_ORDER)) {
                        INode nodeTarget = connec.getTarget();
                        if (nodeTarget.getJobletNode() != null) {
                            nodeTarget = nodeTarget.getJobletNode();
                        }
                        refLink = isThereLinkWithHash(nodeTarget, checkedNode);
                    }
                }
            }
        }
        return refLink;
    }

    /**
     * DOC nrousseau Comment method "checkProcess".
     *
     * @param propagate
     */
    @Override
    public void checkProcess() {
        if (isActivate() && !isDuplicate()) {
            checkProblems();
        }
    }

    protected void checkProblems() {
        Problems.removeProblemsByProcess(this);

        for (INode node : nodes) {
            if (node.isActivate()) {
                node.checkNode();
            }
        }
        Problems.refreshProcessAllNodesStatus(this);
        Problems.refreshProblemTreeView();
    }

    /**
     * check the problems of node.compare with the checkProblems(),this method can't refresh problems view.
     */
    public void checkNodeProblems() {
        if (isActivate()) {
            Problems.removeProblemsByProcess(this);

            for (INode node : nodes) {
                if (node.isActivate()) {
                    node.checkNode();
                }
            }
        }
    }

    /**
     * if delete a process item , should remove the problems of this .
     */
    @Override
    public void removeProblems4ProcessDeleted() {
        if (isActivate()) {
            Problems.removeProblemsByProcess(this, true);
            Problems.refreshProblemTreeView();
        }
    }

    @Override
    public String toString() {
        return "Process:" + getLabel(); //$NON-NLS-1$
    }

    @Override
    public ERepositoryObjectType getRepositoryObjectType() {
        return ERepositoryObjectType.PROCESS;
    }

    @Override
    public IContextManager getContextManager() {
        return contextManager;
    }

    // PTODO mhelleboid remove
    @Override
    public Date getCreationDate() {
        return ItemDateParser.parseAdditionalDate(additionalProperties, ItemProductKeys.DATE.getCreatedKey());
    }

    @Override
    public String getDescription() {
        return getProperty().getDescription();
    }

    @Override
    public Date getModificationDate() {
        return ItemDateParser.parseAdditionalDate(additionalProperties, ItemProductKeys.DATE.getModifiedKey());
    }

    @Override
    public String getPurpose() {
        return getProperty().getPurpose();
    }

    @Override
    public void setCreationDate(Date value) {
    }

    @Override
    public void setDescription(String value) {
        if (getProperty().getDescription() == null) {
            if (value != null) {
                getProperty().setDescription(value);
            }
        } else {
            if (!getProperty().getDescription().equals(value)) {
                getProperty().setDescription(value);
            }
        }
        setPropertyValue(EParameterName.DESCRIPTION.getName(), value);
    }

    @Override
    public void setModificationDate(Date value) {
    }

    @Override
    public void setPurpose(String value) {
        if (getProperty().getPurpose() == null) {
            if (value != null) {
                getProperty().setPurpose(value);
            }
        } else {
            if (!getProperty().getPurpose().equals(value)) {
                getProperty().setPurpose(value);
            }
        }
        setPropertyValue(EParameterName.PURPOSE.getName(), value);
    }

    @Override
    public void setPropertyValue(String id, Object value) {
        if (id.equals(EParameterName.SCHEMA_TYPE.getName()) || id.equals(EParameterName.QUERYSTORE_TYPE.getName())
                || id.equals(EParameterName.PROPERTY_TYPE.getName())
                // || id.equals(JobSettingsConstants.getExtraParameterName(EParameterName.PROPERTY_TYPE.getName()))
                || id.equals(EParameterName.PROCESS_TYPE_PROCESS.getName())) {
            String updataComponentParamName = null;
            // if (JobSettingsConstants.isExtraParameter(id)) {
            // updataComponentParamName =
            // JobSettingsConstants.getExtraParameterName(EParameterName.UPDATE_COMPONENTS.getName());
            // } else {
            updataComponentParamName = EParameterName.UPDATE_COMPONENTS.getName();
            // }
            setPropertyValue(updataComponentParamName, Boolean.TRUE);
        }
        super.setPropertyValue(id, value);
    }

    @Override
    public Property getProperty() {
        return property;
    }

    @Override
    public void setProperty(Property property) {
        this.property = property;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryObject#getChildren()
     */
    @Override
    public List<IRepositoryViewObject> getChildren() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<INode> getNodesOfType(String componentName) {
        List<INode> matchingNodes = new ArrayList<INode>();
        List<INode> generatingNodes = new ArrayList<INode>();
        generatingNodes = (List<INode>) getGeneratingNodes();
        getMatchingNodes(componentName, matchingNodes, generatingNodes);

        generatingNodes = getRealGraphicalNodesFromVirtrualNodes(generatingNodes);
        getMatchingNodes(componentName, matchingNodes, generatingNodes);

        generatingNodes = (List<INode>) getGraphicalNodes();
        getMatchingNodes(componentName, matchingNodes, generatingNodes);

        return matchingNodes;
    }

    private List<INode> getRealGraphicalNodesFromVirtrualNodes(List<INode> generatingNodes) {
        Set<INode> set = new HashSet<INode>();
        if (generatingNodes != null) {
            for (INode node : generatingNodes) {
                if (node.isVirtualGenerateNode() && node instanceof AbstractNode
                        && ((AbstractNode) node).getRealGraphicalNode() != null) {
                    set.add(((AbstractNode) node).getRealGraphicalNode());
                }
            }
        }
        return new ArrayList<INode>(set);
    }

    private void getMatchingNodes(String componentName, List<INode> matchingNodes, List<INode> generatingNodes) {
        for (INode node : new ArrayList<>(generatingNodes)) {
            if (node.isActivate()) {
                if (componentName == null) { // means all nodes will be
                    // returned
                    addNodeIfNotInTheList(matchingNodes, node);
                } else if (componentName.startsWith("FAMILY:")) { //$NON-NLS-1$
                    String familly = componentName.substring("FAMILY:".length()); //$NON-NLS-1$
                    if (node.getComponent().getOriginalFamilyName().startsWith(familly)) {
                        addNodeIfNotInTheList(matchingNodes, node);
                    }
                } else if (componentName.startsWith("REGEXP:")) { //$NON-NLS-1$
                    Perl5Matcher matcher = new Perl5Matcher();
                    Perl5Compiler compiler = new Perl5Compiler();
                    Pattern pattern;

                    String regexp = componentName.substring("REGEXP:".length()); //$NON-NLS-1$
                    try {
                        pattern = compiler.compile(regexp);
                        if (matcher.matches(node.getComponent().getName(), pattern)) {
                            addNodeIfNotInTheList(matchingNodes, node);
                        }
                    } catch (MalformedPatternException e) {
                        throw new RuntimeException(e);
                    }
                } else if ((node.getComponent().getName() != null)) {
                    if (node.getComponent().getName().compareTo(componentName) == 0) {
                        addNodeIfNotInTheList(matchingNodes, node);
                    } else if (node.getComponent() instanceof EmfComponent) {
                        EmfComponent component = (EmfComponent) node.getComponent();
                        String eqCompName = component.getEquivalent();
                        if (componentName.equals(eqCompName)) {
                            addNodeIfNotInTheList(matchingNodes, node);
                        }
                    }
                }
            }
        }
    }

    // }

    private void addNodeIfNotInTheList(List<INode> matchingNodes, INode node) {
        for (INode currentNode : matchingNodes) {
            if (currentNode.getUniqueName().equals(node.getUniqueName())) {
                return; // don't add
            }
        }
        matchingNodes.add(node);
    }

    /**
     * Comment method "getAllConnections".
     *
     * @param filter only return the filter matched connections
     * @return
     */
    @Override
    public IConnection[] getAllConnections(String filter) {
        List<? extends INode> nodes = getGraphicalNodes();
        Set<IConnection> conns = new HashSet<IConnection>();

        for (INode node : nodes) {
            if (node.isActivate()) {
                conns.addAll(node.getIncomingConnections());
                conns.addAll(node.getOutgoingConnections());
            }
        }

        if ((filter != null) && (filter.startsWith("TYPE:"))) { //$NON-NLS-1$
            // construct filter array
            String[] f = filter.substring("TYPE:".length()).split("\\|"); //$NON-NLS-1$ //$NON-NLS-2$
            List<String> filterArray = new ArrayList<String>(f.length);
            for (String element : f) {
                filterArray.add(element.trim());
            }

            for (Iterator<IConnection> iter = conns.iterator(); iter.hasNext();) {
                IConnection con = iter.next();
                if (!filterArray.contains(con.getLineStyle().toString())) {
                    iter.remove();
                }
            }
        }
        return conns.toArray(new IConnection[conns.size()]);
    }

    public Project getProject() {
        return ProjectManager.getInstance().getCurrentProject();
    }

    public void setAsMasterJob() {
        getProject().setMasterJobId(this.getId());
    }

    public ProcessItem getMasterJob() {
        ProcessItem item = null;
        IProxyRepositoryFactory factory = DesignerPlugin.getDefault().getProxyRepositoryFactory();

        try {
            IRepositoryViewObject repositoryObject = factory.getMetadata(ERepositoryObjectType.PROCESS).getMember(
                    getProject().getMasterJobId());
            if (repositoryObject.getRepositoryObjectType() == ERepositoryObjectType.PROCESS) {
                item = (ProcessItem) repositoryObject.getProperty().getItem();
            }
        } catch (PersistenceException e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
            ExceptionHandler.process(e);
        }

        return item;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public void addNote(Note note, boolean fireUpdate) {
        elem.add(note);
        notes.add(note);
        if (fireUpdate) {
            fireStructureChange(NEED_UPDATE_JOB, elem);
        }
    }

    public void addNote(Note note) {
        addNote(note, true);
    }

    public void removeNote(Note note) {
        elem.remove(note);
        notes.remove(note);
        fireStructureChange(NEED_UPDATE_JOB, elem);
    }

    @Override
    public Set<String> getNeededLibraries(int options) {
        return JavaProcessUtil.getNeededLibraries(this, options);
    }

    /**
     * Getter for notes.
     *
     * @return the notes
     */
    public List<Note> getNotes() {
        return notes;
    }

    /**
     * Getter for editor.
     *
     * @return the editor
     */
    @Override
    public IEditorPart getEditor() {
        if (this.editor instanceof AbstractMultiPageTalendEditor) {
            return this.editor;
        }
        return null;
    }

    CommandStackEventListener commandStackEventListener = new CommandStackEventListener() {

        @Override
        public void stackChanged(CommandStackEvent event) {
            processModified = true;
            setNeedRegenerateCode(true);
        }
    };

    IPreferenceChangeListener preferenceEventListener = new IPreferenceChangeListener() {

        @Override
        public void preferenceChange(PreferenceChangeEvent event) {
            if (event.getKey().equals(Log4jPrefsConstants.LOG4J_ENABLE_NODE)) {
                if (getCommandStack() != null) {
                    Process.this.getCommandStack().execute(new PropertyChangeCommand(Process.this,
                            EParameterName.LOG4J_ACTIVATE.getName(), event.getNewValue()));
                }
            }
            if (event.getKey().equals(Log4jPrefsConstants.LOG4J_SELECT_VERSION2)) {
                if (getCommandStack() != null) {
                    Process.this.getCommandStack().execute(new PropertyChangeCommand(Process.this,
                            EParameterName.LOG4J2_ACTIVATE.getName(), event.getNewValue()));
                }
            }
        }
    };

    private IContext lastRunContext;

    private boolean needRegenerateCode;

    private Map<INode, SubjobContainer> copySubjobMap;

    private Boolean lastVersion;

    /**
     * Sets the editor.
     *
     * @param editor the editor to set
     */
    public void setEditor(AbstractMultiPageTalendEditor editor) {
        AbstractMultiPageTalendEditor oldEditor = this.editor;
        this.editor = editor;
        if (editor != null && !duplicate) {
            if (oldEditor == null) {
                List<? extends INode> graphicalNodes = getGraphicalNodes();
                for (INode node : graphicalNodes) {
                    if (node instanceof Node) {
                        ((Node) node).updateVisibleData();
                    }
                }
            }
            CommandStack commandStack = (CommandStack) editor.getTalendEditor().getAdapter(CommandStack.class);
            commandStack.addCommandStackEventListener(commandStackEventListener);
            if (!isReadOnly()) { // when readonly. don't check the modifications.
                getUpdateManager().updateAll();
            }
            // ProjectPreferences projectPreferences = (ProjectPreferences) Log4jPrefsSettingManager.getInstance()
            // .getLog4jPreferences(Log4jPrefsConstants.LOG4J_ENABLE_NODE, false);
            IEclipsePreferences projectPreferences = (IEclipsePreferences) Log4jPrefsSettingManager.getInstance()
                    .getLog4jPreferences(Log4jPrefsConstants.LOG4J_ENABLE_NODE, false);
            projectPreferences.addPreferenceChangeListener(preferenceEventListener);

            IEclipsePreferences projectPreferencesLog4jVersion = (IEclipsePreferences) Log4jPrefsSettingManager.getInstance()
                    .getLog4jPreferences(Log4jPrefsConstants.LOG4J_SELECT_VERSION2, false);
            if (projectPreferencesLog4jVersion != null) {
                projectPreferencesLog4jVersion.addPreferenceChangeListener(preferenceEventListener);
            }
        }
    }

    @Override
    public void dispose() {
        if (editor != null && !duplicate) {
            CommandStack commandStack = (CommandStack) editor.getTalendEditor().getAdapter(CommandStack.class);
            commandStack.removeCommandStackEventListener(commandStackEventListener);

            // ProjectPreferences projectPreferences = (ProjectPreferences) Log4jPrefsSettingManager.getInstance()
            // .getLog4jPreferences(Log4jPrefsConstants.LOG4J_ENABLE_NODE, false);
            IEclipsePreferences projectPreferences = (IEclipsePreferences) Log4jPrefsSettingManager.getInstance()
                    .getLog4jPreferences(Log4jPrefsConstants.LOG4J_ENABLE_NODE, false);
            if (projectPreferences != null) {
                projectPreferences.removePreferenceChangeListener(preferenceEventListener);
            }
            IEclipsePreferences projectPreferencesLog4jVersion = (IEclipsePreferences) Log4jPrefsSettingManager.getInstance()
                    .getLog4jPreferences(Log4jPrefsConstants.LOG4J_SELECT_VERSION2, false);
            if (projectPreferencesLog4jVersion != null) {
                projectPreferencesLog4jVersion.removePreferenceChangeListener(preferenceEventListener);
            }
        }
        generatingProcess = null;
        editor = null;
        viewer = null;
        for (byte data[] : externalInnerContents) {
            ImageUtils.disposeImages(data);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess2#disableRunJobView()
     */
    @Override
    public boolean disableRunJobView() {
        return false;
    }

    /**
     * Sets the processModified.
     *
     * @param processModified the processModified to set
     */
    @Override
    public void setProcessModified(boolean processModified) {
        this.processModified = processModified;
    }

    /**
     * Sets the contextManager.
     *
     * @param contextManager the contextManager to set
     */
    @Override
    public void setContextManager(IContextManager contextManager) {
        this.contextManager = contextManager;
    }

    /**
     * Sets the generatingProcess.
     *
     * @param generatingProcess the generatingProcess to set
     */
    public void setGeneratingProcess(IGeneratingProcess generatingProcess) {
        this.generatingProcess = generatingProcess;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess#getNodesWithImport()
     */
    @Override
    public List<INode> getNodesWithImport() {
        List<INode> nodesWithImport = new ArrayList<INode>();
        for (INode node : getGeneratingNodes()) {
            if (node.getComponent().useImport()) {
                nodesWithImport.add(node);
            }
        }
        return nodesWithImport;
    }

    @Override
    public void updateSubjobContainers() {
        // check all old subjobStart to see if their status changed (to remove the subjob if needed)
        Set<SubjobContainer> updatedSubjobContainers = new HashSet<SubjobContainer>();
        for (SubjobContainer sjc : new ArrayList<SubjobContainer>(subjobContainers)) {
            Node node = sjc.getSubjobStartNode();
            // if this node is not anymore a subjob start, then set it back to the element list.
            // this one will be reaffected to a new subjob after
            if (node == null || !node.isDesignSubjobStartNode()) {
                // for bug 13314
                // if (node == null
                // || !(node.getOutgoingConnections(EConnectionType.TABLE).size() != 0 || node.getIncomingConnections(
                // EConnectionType.TABLE).size() != 0)
                // ) {
                elem.addAll(sjc.getNodeContainers());
                sjc.getNodeContainers().clear();
                elem.remove(sjc);
                subjobContainers.remove(sjc);
                // }
                // subjob are never removed from the map, so if the user do any "undo"
                // the name of the subjob or configuration will be kept.
            } else {
                for (NodeContainer nodeContainer : new ArrayList<NodeContainer>(sjc.getNodeContainers())) {
                    if (!nodeContainer.getNode().getDesignSubjobStartNode().equals(node)) {
                        sjc.getNodeContainers().remove(nodeContainer);
                        elem.add(nodeContainer);
                        updatedSubjobContainers.add(sjc);
                    } else if (nodeContainer.getNode().isMapReduceStart()) {
                        updatedSubjobContainers.add(sjc);
                    }
                }
            }
        }

        // make one loop first for the subjob starts
        // once all the subjobs are created, make another loop for the other nodes
        for (Element element : new ArrayList<Element>(elem)) {
            // if there is any NodeContainer, need to reaffect them to a new subjob
            if (element instanceof NodeContainer) {
                Node node = ((NodeContainer) element).getNode();
                if (node.isDesignSubjobStartNode()) {
                    // if the subjob already exist in the list take it, or if not exist create a new one.
                    SubjobContainer sjc = mapSubjobStarts.get(node);
                    if (sjc == null) {
                        // If the process is a SparkStreaming process, then we use an extension of SubjobContainer to
                        // display different information.
                        if (getComponentsType().equals(ComponentCategory.CATEGORY_4_SPARKSTREAMING.getName())) {
                            sjc = new SparkStreamingSubjobContainer(this);
                        } else {
                            sjc = new SubjobContainer(this);
                        }
                        sjc.setSubjobStartNode(node);
                        fillSubjobTitle(node, sjc);
                        mapSubjobStarts.put(node, sjc);
                    }
                    sjc.getNodeContainers().clear();
                    sjc.addNodeContainer(node.getNodeContainer());
                    subjobContainers.add(sjc);
                    updatedSubjobContainers.add(sjc);
                    elem.remove(node.getNodeContainer());
                    elem.add(sjc);
                }
            }
        }

        // if there is any NodeContainer, need to reaffect them to an existing subjob
        int n = 0;
        for (Element element : new ArrayList<Element>(elem)) {
            if (element instanceof NodeContainer) {
                Node node = ((NodeContainer) element).getNode();
                SubjobContainer sjc = mapSubjobStarts.get(node.getDesignSubjobStartNode());
                if (sjc != null) {
                    sjc.addNodeContainer(node.getNodeContainer());
                    if (!elem.remove(node.getNodeContainer())) {
                        elem.remove(n);
                    }
                    updatedSubjobContainers.add(sjc);
                }
            }
            n++;
        }
        fireStructureChange(NEED_UPDATE_JOB, elem);
        // update modified subjobs
        int i = updatedSubjobContainers.size();
        for (SubjobContainer sjc : updatedSubjobContainers) {
            sjc.updateSubjobContainer();
        }

        // at the end, there should be no Node / NodeContainer without SubjobContainer
    }

    /**
     * DOC bqian Comment method "fillSubjobTitle".
     *
     * @param node
     * @param sjc
     */
    private void fillSubjobTitle(Node node, SubjobContainer sjc) {
        if (copySubjobMap == null) {
            return;
        }
        SubjobContainer original = copySubjobMap.get(node);
        if (original != null) {
            sjc.getElementParameter(EParameterName.COLLAPSED.getName()).setValue(
                    original.getElementParameter(EParameterName.COLLAPSED.getName()).getValue());
            sjc.getElementParameter(EParameterName.SHOW_SUBJOB_TITLE.getName()).setValue(
                    original.getElementParameter(EParameterName.SHOW_SUBJOB_TITLE.getName()).getValue());
            sjc.getElementParameter(EParameterName.SUBJOB_TITLE.getName()).setValue(
                    original.getElementParameter(EParameterName.SUBJOB_TITLE.getName()).getValue());

            sjc.getElementParameter(EParameterName.SUBJOB_TITLE_COLOR.getName()).setValue(
                    original.getElementParameter(EParameterName.SUBJOB_TITLE_COLOR.getName()).getValue());

            sjc.getElementParameter(EParameterName.SUBJOB_COLOR.getName()).setValue(
                    original.getElementParameter(EParameterName.SUBJOB_COLOR.getName()).getValue());
        }
    }

    public List<NodeContainer> getAllNodeContainers() {
        List<NodeContainer> list = new ArrayList<NodeContainer>();
        for (SubjobContainer sjc : subjobContainers) {
            list.addAll(sjc.getNodeContainers());
        }
        return list;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess#getLastRunContext()
     */
    @Override
    public IContext getLastRunContext() {
        return lastRunContext;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess#setLastRunContext(org.talend.core.model.process.IContext)
     */
    @Override
    public void setLastRunContext(IContext context) {
        this.lastRunContext = context;

    }

    /**
     * Getter for duplicate.
     *
     * @return the duplicate
     */
    @Override
    public boolean isDuplicate() {
        return this.duplicate;
    }

    /**
     * Sets the duplicate.
     *
     * @param duplicate the duplicate to set
     */
    @Override
    public void setDuplicate(boolean duplicate) {
        this.duplicate = duplicate;
    }

    /**
     * Getter for subjobContainers.
     *
     * @return the subjobContainers
     */
    @Override
    public List<? extends ISubjobContainer> getSubjobContainers() {
        return this.subjobContainers;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess2#getUpdateManager()
     */
    @Override
    public IUpdateManager getUpdateManager() {
        return this.updateManager;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess2#isNeedRegenerateCode()
     */
    @Override
    public boolean isNeedRegenerateCode() {
        if (editor == null) {
            // if no editor linked, we just consider same as if there was all the time a modification
            return true;
        }
        return needRegenerateCode;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess2#setNeedRegenerateCode(boolean)
     */
    @Override
    public void setNeedRegenerateCode(boolean regenerateCode) {
        this.needRegenerateCode = regenerateCode;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryObject#getRepositoryNode()
     */
    @Override
    public RepositoryNode getRepositoryNode() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.core.model.repository.IRepositoryObject#setRepositoryNode(org.talend.repository.model.RepositoryNode)
     */
    @Override
    public void setRepositoryNode(IRepositoryNode node) {
        // TODO Auto-generated method stub

    }

    /**
     * <br>
     * see bug 0004882: Subjob title is not copied when copying/pasting subjobs from one job to another
     *
     * @param mapping
     */
    public void setCopyPasteSubjobMappings(Map<INode, SubjobContainer> mapping) {
        copySubjobMap = mapping;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess#getOutputMetadataTable()
     */

    // this function is create for feature 0006265
    @Override
    public IMetadataTable getOutputMetadataTable() {
        List<? extends Node> nodes = (List<? extends Node>) this.getGeneratingNodes();
        for (Node node : nodes) {
            String name = node.getComponent().getName();
            if (name.equals("tBufferOutput")) { //$NON-NLS-1$
                return node.getMetadataTable(node.getUniqueName());
            }
        }
        return null;

    }

    @Override
    public Map<String, byte[]> getScreenshots() {
        return this.screenshots;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.ui.ILastVersionChecker#isLastVersion(org.talend.core.model.properties.Item)
     */
    @Override
    public boolean isLastVersion(Item item) {
        if (lastVersion != null) { // status can be known without check below, to continue to optimize later.
            return lastVersion;
        }
        if (item.getProperty() != null) {
            try {
                List<IRepositoryViewObject> allVersion = null;
                ItemState state = property.getItem().getState();
                ERepositoryObjectType type = ERepositoryObjectType.PROCESS;
                if (item instanceof JobletProcessItem) {
                    type = ERepositoryObjectType.JOBLET;
                }
                boolean pathExist = false;
                if (state != null) {
                    String path = state.getPath();
                    if (path != null) {
                        File f = new File(path);
                        if (f.exists()) {
                            pathExist = true;
                        }
                    }
                }
                if (pathExist && type != null) {
                    allVersion = CorePlugin.getDefault().getRepositoryService().getProxyRepositoryFactory()
                            .getAllVersion(property.getId(), state.getPath(), type);
                } else {
                    allVersion = CorePlugin.getDefault().getRepositoryService().getProxyRepositoryFactory()
                            .getAllVersion(property.getId());
                }
                if (allVersion == null || allVersion.isEmpty()) {
                    return false;
                }
                String lastVersion = VersionUtils.DEFAULT_VERSION;

                for (IRepositoryViewObject object : allVersion) {
                    if (VersionUtils.compareTo(object.getVersion(), lastVersion) > 0) {
                        lastVersion = object.getVersion();
                    }
                }
                if (VersionUtils.compareTo(property.getVersion(), lastVersion) == 0) {
                    return true;
                }
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        }
        return false;
    }

    @Override
    public List<NodeType> getUnloadedNode() {
        return this.unloadedNode;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.ui.ILastVersionChecker#setLastVersion(java.lang.Boolean)
     */
    @Override
    public void setLastVersion(Boolean lastVersion) {
        this.lastVersion = lastVersion;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryViewObject#getInformationStatus()
     */
    @Override
    public ERepositoryStatus getInformationStatus() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryViewObject#getPath()
     */
    @Override
    public String getPath() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryViewObject#getProjectLabel()
     */
    @Override
    public String getProjectLabel() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryViewObject#getRepositoryStatus()
     */
    @Override
    public ERepositoryStatus getRepositoryStatus() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryViewObject#isDeleted()
     */
    @Override
    public boolean isDeleted() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess2#checkTableParameters()
     */
    @Override
    public void checkTableParameters() {
        checkNodeTableParameters();
    }

    private void refreshAllContextView() {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IViewPart view2 = page.findView(ContextsView.CTX_ID_DESIGNER);
        if (view2 instanceof ContextsView) {
            ((ContextsView) view2).updateAllContextView(true);
        }
    }

    private void loadProjectParameters(ProcessType processType) {
        ParametersType parameters = processType.getParameters();
        if (parameters != null) {
            loadElementParameters(this, parameters.getElementParameter());
            updateProSetingParameters(parameters.getElementParameter());
            loadRoutinesParameters(processType);
        }
    }

    private void loadAdditionalProperties() {
        if (additionalProperties == null) {
            additionalProperties = new HashMap<Object, Object>();
            try {
                if (property.getItem() != null && ERepositoryObjectType.getType(property) != null) {
                    boolean isRouteProcess = ERepositoryObjectType.getType(property).equals(ERepositoryObjectType.PROCESS_ROUTE);
                    if (!isRouteProcess && "ROUTE"
                            .equals(this.property.getAdditionalProperties().get(TalendProcessArgumentConstant.ARG_BUILD_TYPE))) {
                        this.property.getAdditionalProperties().remove(TalendProcessArgumentConstant.ARG_BUILD_TYPE);
                    }
                }
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }

            for (Object key : this.property.getAdditionalProperties().keySet()) {
                additionalProperties.put(key, this.property.getAdditionalProperties().get(key));
            }
        }
    }

    private void loadRoutinesParameters(ProcessType processType) {
        ParametersType parameters = processType.getParameters();
        if (parameters == null || parameters.getRoutinesParameter() == null) {
            List<RoutinesParameterType> dependenciesInPreference;
            try {
                dependenciesInPreference = RoutinesUtil.createDependenciesInPreference();
                ParametersType parameterType = TalendFileFactory.eINSTANCE.createParametersType();
                parameterType.getRoutinesParameter().addAll(dependenciesInPreference);
                processType.setParameters(parameterType);
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        }
        // else {
        // ProcessType process = this.getProcessType();
        // process.getParameters().getRoutinesParameter().addAll(parameters.getRoutinesParameter());
        // }
        routinesDependencies.clear();
        List newRoutinesList = parameters.getRoutinesParameter();
        if (newRoutinesList != null) {
            routinesDependencies.addAll(newRoutinesList);
        }
    }

    private void updateProSetingParameters(EList listParamType) {
        // TDI-28709:after import the ProjectSetting.xml,do not open job directly run job,should try to update
        // projcetSetting first
        Project project = ProjectManager.getInstance().getCurrentProject();
        boolean updateStandardLog = false;
        boolean updateImplicitContext = false;
        for (Object element : listParamType) {
            ElementParameterType pType = (ElementParameterType) element;
            if (Boolean.valueOf(pType.getValue())) {
                if (EParameterName.STATANDLOG_USE_PROJECT_SETTINGS.getName().equals(pType.getName())) {
                    ProjectSettingManager.reloadStatsAndLogFromProjectSettings(this, project, null);
                    updateStandardLog = true;
                } else if (EParameterName.IMPLICITCONTEXT_USE_PROJECT_SETTINGS.getName().equals(pType.getName())) {
                    Element elem = ProjectSettingManager.createImplicitContextLoadElement(project);
                    Map<String, Set<String>> contextVars = DetectContextVarsUtils.detectByPropertyType(elem, true);
                    boolean addContextModel = false;
                    List<ContextItem> allContextItems = null;
                    if (!contextVars.isEmpty()) {
                        org.talend.core.model.metadata.builder.connection.Connection connection = null;
                        IElementParameter ptParam = elem.getElementParameterFromField(EParameterFieldType.PROPERTY_TYPE);
                        if (ptParam != null) {
                            IElementParameter propertyElem = ptParam.getChildParameters().get(
                                    EParameterName.PROPERTY_TYPE.getName());
                            Object proValue = propertyElem.getValue();
                            if (proValue instanceof String && ((String) proValue).equalsIgnoreCase(EmfComponent.REPOSITORY)) {
                                IElementParameter repositoryElem = ptParam.getChildParameters().get(
                                        EParameterName.REPOSITORY_PROPERTY_TYPE.getName());
                                String value = (String) repositoryElem.getValue();
                                org.talend.core.model.properties.ConnectionItem connectionItem = UpdateRepositoryUtils
                                        .getConnectionItemByItemId(value);
                                connection = connectionItem.getConnection();
                                if (connection != null && connection.isContextMode()) {
                                    addContextModel = true;
                                    allContextItems = ContextUtils.getAllContextItem();
                                }
                            }
                        }
                    }
                    ProjectSettingManager.reloadImplicitValuesFromProjectSettings(this, project, null);
                    if (addContextModel && !contextVars.isEmpty() && allContextItems != null) {
                        ContextUtils.addInContextModelForProcessItem(property.getItem(), contextVars, allContextItems);
                    }
                    updateImplicitContext = true;
                }
            }
            if (updateStandardLog && updateImplicitContext) {
                break;
            }
        }
    }

    /**
     * DOC Administrator Comment method "updateProcess".
     *
     * @param processType
     */
    public void updateProcess(ProcessType processType) {
        setActivate(false);

        elem.clear();
        nodes.clear();
        notes.clear();
        subjobContainers.clear();
        setGeneratingProcess(null);

        // added for context
        contextManager.getListContext().clear();
        loadContexts(processType);
        refreshAllContextView();

        // added for projectSetting
        loadProjectParameters(processType);
        loadAdditionalProperties();

        // ((ProcessItem) property.getItem()).setProcess(processType);

        Hashtable<String, Node> nodesHashtable = new Hashtable<String, Node>();

        try {
            loadNodes(processType, nodesHashtable);
        } catch (PersistenceException e) {
            // there are some components unloaded.
            return;
        }

        repositoryId = processType.getRepositoryContextId();

        loadConnections(processType, nodesHashtable);

        // added for notes
        loadNotes(processType);
        // added for subjobs
        loadSubjobs(processType);

        setActivate(true);
        checkStartNodes();
        fireStructureChange(NEED_UPDATE_JOB, elem);
        checkProcess();
    }

    private void saveJobletNode(AbstractJobletContainer jobletContainer) {
        if (CommonsPlugin.isHeadless()) {
            return;
        }
        INode jobletNode = jobletContainer.getNode();
        IProcess jobletProcess = jobletNode.getComponent().getProcess();
        if (jobletProcess == null) {
            return;
        }
        if (jobletProcess instanceof IProcess2) {
            Item item = ((IProcess2) jobletProcess).getProperty().getItem();
            if (item instanceof JobletProcessItem) {
                JobletProcessItem jobletItem = ((JobletProcessItem) item);
                IJobletProviderService service = GlobalServiceRegister.getDefault().getService(
                        IJobletProviderService.class);
                if (service != null) {
                    service.saveJobletNode(jobletItem, jobletContainer);

                }
            }
        }

    }

    @Override
    public Set<String> getNeededRoutines() {
        return getNeededCodeItem(neededRoutines, ERepositoryObjectType.ROUTINES);
    }

    private Set<String> getNeededCodeItem(Set<String> neededCodeItem, ERepositoryObjectType itemType) {

        // this value is initialized only for a duplicate process (for code generation)
        if (neededCodeItem != null && duplicate) {
            return neededCodeItem;
        }
        if (routinesDependencies == null || routinesDependencies.isEmpty()) {
            checkRoutineDependencies();
        }
        // check in case routine dependencies hold invalid routines.
        Iterator<RoutinesParameterType> iterator = routinesDependencies.iterator();
        while (iterator.hasNext()) {
            RoutinesParameterType routine = iterator.next();
            if (StringUtils.isEmpty(routine.getId()) || StringUtils.isEmpty(routine.getName())) {
                iterator.remove();
            }
        }
        if (routinesDependencies.isEmpty()) {
            checkRoutineDependencies();
        }

        Set<String> listRoutines = new HashSet<String>();
        for (RoutinesParameterType routine : routinesDependencies) {
            listRoutines.add(routine.getName());
        }

        IJobletProviderService jobletService = null;
        if (PluginChecker.isJobLetPluginLoaded()) {
            jobletService = GlobalServiceRegister.getDefault().getService(IJobletProviderService.class);
            for (INode node : getGraphicalNodes()) {
                if (jobletService.isJobletComponent(node)) {
                    listRoutines.addAll(getJobletRoutines(jobletService, node));
                }
            }
        }

        // verify to remove non-existing routines from the list, just in case some have been deleted.
        List<IRepositoryViewObject> routines;
        try {
            routines = ProxyRepositoryFactory.getInstance().getAll(ProjectManager.getInstance().getCurrentProject(), itemType);
            for (Project project : ProjectManager.getInstance().getAllReferencedProjects(true)) {
                List<IRepositoryViewObject> routinesFromRef = ProxyRepositoryFactory.getInstance().getAll(project, itemType);
                for (IRepositoryViewObject routine : routinesFromRef) {
                    if (!((RoutineItem) routine.getProperty().getItem()).isBuiltIn()) {
                        routines.add(routine);
                    }
                }
            }

            // always add the system, others must be checked
            Set<String> nonExistingRoutines = new HashSet<String>();

            for (String routine : listRoutines) {
                boolean found = false;
                for (IRepositoryViewObject object : routines) {
                    if (routine.equals(object.getLabel())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    nonExistingRoutines.add(routine);
                }
            }
            listRoutines.removeAll(nonExistingRoutines);
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        return listRoutines;

    }

    private Set<String> getJobletRoutines(IJobletProviderService jobletService, INode jobletComponent) {
        List<Node> nodes = (List<Node>) jobletService.getGraphNodesForJoblet(jobletComponent);
        Set<String> listRoutines = new HashSet<String>();
        if (!nodes.isEmpty()) {
            for (RoutinesParameterType routine : ((Process) nodes.get(0).getProcess()).getRoutineDependencies()) {
                if (!StringUtils.isEmpty(routine.getId()) && !StringUtils.isEmpty(routine.getName())) {
                    listRoutines.add(routine.getName());
                }
            }

            for (Node node : nodes) {
                if (jobletService.isJobletComponent(node)) {
                    listRoutines.addAll(getJobletRoutines(jobletService, node));
                }
            }
        }
        return listRoutines;
    }

    @Override
    public void refreshProcess() {
        getUpdateManager().updateAll();
        List<Node> nodes = (List<Node>) getGraphicalNodes();
        List<Node> newNodes = new ArrayList<Node>();
        newNodes.addAll(nodes);
        for (Node node : newNodes) {
            node.getProcess().checkStartNodes();
            node.checkAndRefreshNode();
            // change active status here to force refersh node
            // IElementParameter ep = node.getElementParameter("ACTIVATE");
            // if (ep != null && ep.getValue().equals(Boolean.FALSE)) {
            // node.setPropertyValue(EParameterName.ACTIVATE.getName(), true);
            // node.setPropertyValue(EParameterName.ACTIVATE.getName(), false);
            // } else if (ep != null && ep.getValue().equals(Boolean.TRUE)) {
            // node.setPropertyValue(EParameterName.ACTIVATE.getName(), false);
            // node.setPropertyValue(EParameterName.ACTIVATE.getName(), true);
            // } else {
            // node.setPropertyValue(EParameterName.REPAINT.getName(), Boolean.TRUE);
            // }
            node.setPropertyValue(EParameterName.REPAINT.getName(), Boolean.TRUE);
        }
    }

    public void setNeededRoutines(Set<String> neededRoutines) {
        this.neededRoutines = neededRoutines;
    }

    public void setNeededPigudf(Set<String> neededPigudf) {
        this.neededPigudf = neededPigudf;
    }

    public List<RoutinesParameterType> getRoutineDependencies() {
        return routinesDependencies;
    }

    public void setRoutineDependencies(List<RoutinesParameterType> routinesDependencies) {
        this.routinesDependencies = routinesDependencies;
    }

    @Override
    public boolean isSubjobEnabled() {
        return true;
    }

    public String getBaseHelpLink() {
        return "org.talend.help.";
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.repository.IRepositoryViewObject#isModified()
     */
    @Override
    public boolean isModified() {
        return this.isProcessModified();
    }

    /**
     * Getter for componentsType.
     *
     * @return the componentsType
     */
    @Override
    public String getComponentsType() {
        return this.componentsType;
    }

    /**
     * Sets the componentsType.
     *
     * @param componentsType the componentsType to set
     */
    public void setComponentsType(String componentsType) {
        this.componentsType = componentsType;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess2#getAdditionalProperties()
     */
    @Override
    public Map<Object, Object> getAdditionalProperties() {
        if (this.additionalProperties == null) {
            this.additionalProperties = new HashMap<Object, Object>();
        }
        return this.additionalProperties;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess#getNeededModules(boolean)
     */
    @Override
    public Set<ModuleNeeded> getNeededModules(int options) {
        return JavaProcessUtil.getNeededModules(this, options);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess2#setMRData()
     */
    @Override
    public void setMRData() {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess2#isNeedLoadmodules()
     */
    @Override
    public boolean isNeedLoadmodules() {
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IProcess2#setNeedLoadmodules()
     */
    @Override
    public void setNeedLoadmodules(boolean isNeedLoadmodules) {
        this.isNeedLoadmodules = isNeedLoadmodules;
    }


    /**
     * Getter for generatingProcess.
     * @return the generatingProcess
     */
    public IGeneratingProcess getGeneratingProcess() {
        if (generatingProcess == null) {
            generatingProcess = new DataProcess(this);
        }
        return this.generatingProcess;
    }

}
