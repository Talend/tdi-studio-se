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
package org.talend.designer.core.ui.editor.properties.controllers;

import java.beans.PropertyChangeListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.lang.StringUtils;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.PropertySheet;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.utils.ControlUtils;
import org.talend.commons.ui.swt.dialogs.ModelSelectionDialog;
import org.talend.commons.ui.swt.dialogs.ModelSelectionDialog.EEditSelection;
import org.talend.commons.ui.swt.dialogs.ModelSelectionDialog.ESelectionType;
import org.talend.commons.ui.swt.proposal.ContentProposalAdapterExtended;
import org.talend.commons.ui.utils.TypedTextCommandExecutor;
import org.talend.commons.utils.generation.CodeGenerationUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.context.RepositoryContext;
import org.talend.core.database.EDatabase4DriverClassName;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.database.conn.ConnParameterKeys;
import org.talend.core.database.conn.version.EDatabaseVersion4Drivers;
import org.talend.core.language.CodeProblemsChecker;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.ICodeProblemsChecker;
import org.talend.core.model.components.EComponentType;
import org.talend.core.model.components.IMultipleComponentManager;
import org.talend.core.model.metadata.IMetadataConnection;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.QueryUtil;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.database.ExtractMetaDataUtils;
import org.talend.core.model.metadata.designerproperties.EParameterNameForComponent;
import org.talend.core.model.param.EConnectionParameterName;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.process.Problem;
import org.talend.core.model.process.Problem.ProblemStatus;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Information;
import org.talend.core.model.properties.InformationLevel;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.services.IGenericService;
import org.talend.core.sqlbuilder.util.ConnectionParameters;
import org.talend.core.sqlbuilder.util.TextUtil;
import org.talend.core.ui.process.IGraphicalNode;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.core.ui.proposal.TalendProposalUtils;
import org.talend.core.ui.services.ISQLBuilderService;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.IMultiPageTalendEditor;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.FakeElement;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.model.process.jobsettings.JobSettingsConstants;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.cmd.ChangeValuesFromRepository;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.cmd.QueryGuessCommand;
import org.talend.designer.core.ui.editor.connections.TracesConnectionUtils;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.editor.properties.ContextParameterExtractor;
import org.talend.designer.core.ui.preferences.TalendDesignerPrefConstants;
import org.talend.designer.core.ui.projectsetting.ImplicitContextLoadElement;
import org.talend.designer.core.ui.projectsetting.StatsAndLogsElement;
import org.talend.designer.core.ui.viewer.ReconcilerStyledText;
import org.talend.designer.core.ui.views.jobsettings.AbstractPreferenceComposite;
import org.talend.designer.core.ui.views.jobsettings.JobSettingsView;
import org.talend.designer.core.ui.views.problems.Problems;
import org.talend.designer.core.ui.views.properties.ComponentSettingsView;
import org.talend.designer.core.ui.views.properties.MultipleThreadDynamicComposite;
import org.talend.designer.core.ui.views.properties.WidgetFactory;
import org.talend.designer.core.utils.UpgradeParameterHelper;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.hadoop.distribution.constants.HiveConstant;
import org.talend.hadoop.distribution.constants.ImpalaConstant;
import org.talend.metadata.managment.repository.ManagerConnection;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.model.IMetadataService;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * DOC yzhang class global comment. Detailled comment <br/>
 *
 * $Id: talend-code-templates.xml 1 2006-09-29 17:06:40 +0000 (鏄熸湡浜�, 29 涔濇湀 2006) yzhang $
 *
 */

public abstract class AbstractElementPropertySectionController implements PropertyChangeListener {

    protected static final String SQLEDITOR = "SQLEDITOR"; //$NON-NLS-1$

    private Map<String, Dialog> sqlbuilers = new HashMap<String, Dialog>();

    protected IDynamicProperty dynamicProperty;

    protected Composite composite;

    protected BidiMap hashCurControls;

    protected IElement elem;

    protected IMultiPageTalendEditor part;

    protected EComponentCategory section;

    protected EditionControlHelper editionControlHelper;

    private int additionalHeightSize;

    private List<Problem> proForJavaErrorMark = null;

    protected static final String VARIABLE_TOOLTIP = Messages
            .getString("AbstractElementPropertySectionController.variableTooltip"); //$NON-NLS-1$

    protected static final String NAME = "NAME"; //$NON-NLS-1$

    protected static final String COLUMN = "COLUMN"; //$NON-NLS-1$

    // PTODO qzhang use PARAMETER_NAME it for bug 853.
    protected static final String PARAMETER_NAME = TypedTextCommandExecutor.PARAMETER_NAME;

    protected static final int MAX_PERCENT = 100;

    protected static final int STANDARD_LABEL_WIDTH = 100;

    protected static final int STANDARD_HEIGHT = 20;

    protected static final int STANDARD_BUTTON_WIDTH = 25;

    protected static final String DOTS_BUTTON = "icons/dots_button.gif"; //$NON-NLS-1$s

    private static Map<String, Problem> proForPerlErrorMark = new HashMap<String, Problem>();

    protected EParameterFieldType paramFieldType;

    private List<Problem> codeProblems;

    // for job settings extra.(feature 2710)
    protected IElementParameter curParameter;

    protected IContextManager contextManager;

    public static Map<String, String> connKeyMap = new HashMap<String, String>(10);

    static {
        connKeyMap.put("SERVER_NAME", "HOST"); //$NON-NLS-1$ //$NON-NLS-2$
        connKeyMap.put("PORT", "PORT"); //$NON-NLS-1$ //$NON-NLS-2$
        connKeyMap.put("SID", "DBNAME"); //$NON-NLS-1$ //$NON-NLS-2$
        connKeyMap.put("SCHEMA", "SCHEMA_DB"); //$NON-NLS-1$ //$NON-NLS-2$
        connKeyMap.put("USERNAME", "USER"); //$NON-NLS-1$ //$NON-NLS-2$
        connKeyMap.put("PASSWORD", "PASS"); //$NON-NLS-1$ //$NON-NLS-2$
        connKeyMap.put("PROPERTIES_STRING", "PROPERTIES"); //$NON-NLS-1$ //$NON-NLS-2$
        connKeyMap.put("DIRECTORY", "DIRECTORY"); //$NON-NLS-1$ //$NON-NLS-2$
        connKeyMap.put("FILE", "FILE"); //$NON-NLS-1$ //$NON-NLS-2$
        connKeyMap.put("DATASOURCE", "DATASOURCE"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * DOC yzhang Comment method "createControl".
     *
     * Create control within the tabbed property setcion.
     *
     * @param subComposite. The composite selected in the editor or view, transfered from super class of tabbed
     * properties framwork.
     * @param param. The paramenter from EMF.
     * @param numInRow. The ID of the control in a row.
     * @param nbInRow. The total quantity of the control in a row.
     * @param top
     * @param rowSize height that can take the control (0 if default size)
     * @param lastControl. The latest control created beside current being created. @return. The control created by this
     * method will be the paramenter of next be called createControl method for position calculate.
     */
    public abstract Control createControl(final Composite subComposite, final IElementParameter param, final int numInRow,
            final int nbInRow, final int top, final Control lastControl);

    public abstract int estimateRowSize(final Composite subComposite, final IElementParameter param);

    protected int getColorStyledTextRowSize(int nbLines) {

        return 0;
    }

    protected boolean isInWizard() {
        if (dynamicProperty != null) {
            Element element = dynamicProperty.getElement();
            if (element instanceof FakeElement) {
                return true;
            }
        }
        return false;
    }

    /**
     * Will return true of false depends if the control has dynamic size or not.
     *
     * @return
     */
    public boolean hasDynamicRowSize() {
        return false;
    }

    /**
     * Used only to force the rowSize if the size is dynamic.
     *
     * @param height
     */
    public void setAdditionalHeightSize(int height) {
        this.additionalHeightSize = height;
    }

    /**
     * Used only to force the rowSize if the size is dynamic.
     *
     * @return the height
     */
    public int getAdditionalHeightSize() {
        return additionalHeightSize;
    }

    /**
     * DOC yzhang AbstractElementPropertySectionController constructor comment.
     */
    public AbstractElementPropertySectionController(IDynamicProperty dp) {
        init(dp);
    }

    protected String getRepositoryItemFromRepositoryName(IElementParameter param, String repositoryName) {
        String value = (String) param.getValue();
        Object[] valuesList = param.getListItemsValue();
        String[] originalList = param.getListItemsDisplayCodeName();
        for (int i = 0; i < valuesList.length; i++) {
            if (valuesList[i].equals(value)) {
                if ("DB_VERSION".equals(repositoryName) || HiveConstant.DISTRIBUTION_PARAMETER.equals(repositoryName)
                        || HiveConstant.VERSION_PARAMETER.equals(repositoryName)
                        || ImpalaConstant.DISTRIBUTION_PARAMETER.equals(repositoryName)
                        || ImpalaConstant.VERSION_PARAMETER.equals(repositoryName)) {
                    return valuesList[i].toString();
                }
                return originalList[i];
            }
        }
        return ""; //$NON-NLS-1$
    }

    protected String getValueFromRepositoryName(String repositoryName) {
        for (IElementParameter param : (List<IElementParameter>) elem.getElementParameters()) {
            if (param.getRepositoryValue() != null) {
                if (param.getRepositoryValue().equals(repositoryName)) {
                    if (param.getFieldType().equals(EParameterFieldType.CLOSED_LIST)) {
                        return getRepositoryItemFromRepositoryName(param, repositoryName);
                    }
                    if (param.getValue() instanceof String) {
                        return (String) param.getValue();
                    } else if (param.getValue() instanceof List) {
                        // for jdbc parm driver jar
                        String value = "";
                        List list = (List) param.getValue();
                        for (int i = 0; i < list.size(); i++) {
                            Object object = list.get(i);
                            if (object instanceof Map) {
                                Map valueMap = (Map) object;
                                if (valueMap.get("JAR_NAME") != null) {
                                    if (value.equals("")) {
                                        value = value + valueMap.get("JAR_NAME");
                                    } else {
                                        value = value + ";" + valueMap.get("JAR_NAME");
                                    }
                                }else if (valueMap.get("drivers") != null) {
                                    if (value.equals("")) {
                                        value = value + valueMap.get("drivers");
                                    } else {
                                        value = value + ";" + valueMap.get("drivers");
                                    }
                                }
                            }
                        }
                        return value;
                    }
                }
            }
        }
        return ""; //$NON-NLS-1$
    }

    protected String getValueFromRepositoryName(IElement elem2, String repositoryName) {
        return getValueFromRepositoryName(elem2, repositoryName, null);
    }

    protected String getValueFromRepositoryName(IElement elem2, String repositoryName, IElementParameter baseRepositoryParameter) {

        for (IElementParameter param : (List<IElementParameter>) elem2.getElementParameters()) {
            // for job settings extra.(feature 2710)
            if (!sameExtraParameter(param)) {
                continue;
            }
            // if ("TYPE".equals(repositoryName) && "CONNECTION_TYPE".equals(param.getName())) {
            // return (String) param.getValue();
            // }
            if (param.getRepositoryValue() != null) {
                if (param.getRepositoryProperty() != null && baseRepositoryParameter != null
                        && !param.getRepositoryProperty().equals(baseRepositoryParameter.getName())) {
                    continue;
                }
                if (param.getRepositoryValue().equals(repositoryName)) {
                    if (param.getFieldType().equals(EParameterFieldType.CLOSED_LIST)) {
                        return getRepositoryItemFromRepositoryName(param, repositoryName);
                    }
                    if (param.getValue() instanceof String) {
                        return (String) param.getValue();
                    } else if (param.getValue() instanceof Boolean) {
                        return String.valueOf(param.getValue());
                    } else if (param.getValue() instanceof List) {
                        // for jdbc parm driver jar
                        String value = "";
                        List list = (List) param.getValue();
                        for (int i = 0; i < list.size(); i++) {
                            Object object = list.get(i);
                            if (object instanceof Map) {
                                Map valueMap = (Map) object;
                                if (valueMap.get("JAR_NAME") != null) {
                                    if (value.equals("")) {
                                        value = value + valueMap.get("JAR_NAME");
                                    } else {
                                        value = value + ";" + valueMap.get("JAR_NAME");
                                    }
                                }else if (valueMap.get("drivers") != null) {
                                    if (value.equals("")) {
                                        value = value + valueMap.get("drivers");
                                    } else {
                                        value = value + ";" + valueMap.get("drivers");
                                    }
                                }
                            }
                        }
                        return value;
                    }

                }
            }
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * DOC zli Comment method "getValueFromRepositoryName".
     *
     * @param elem2
     * @param repositoryName
     * @param parameterName
     * @return
     */
    protected String getValueFromRepositoryNameAndParameterName(IElement elem2, String repositoryName, String parameterName) {

        for (IElementParameter param : (List<IElementParameter>) elem2.getElementParameters()) {
            if (!sameExtraParameter(param)) {
                continue;
            }
            if (param.getRepositoryValue() != null) {
                if (param.getRepositoryValue().equals(repositoryName)) {
                    if (param.getName().contains(parameterName)) {
                        if (param.getValue() instanceof String) {
                            return (String) param.getValue();
                        } else if (param.getValue() instanceof List) {
                            // for jdbc parm driver jar
                            String value = "";
                            List list = (List) param.getValue();
                            for (int i = 0; i < list.size(); i++) {
                                Object object = list.get(i);
                                if (object instanceof Map) {
                                    Map valueMap = (Map) object;
                                    if (valueMap.get("JAR_NAME") != null) {
                                        if (value.equals("")) {
                                            value = value + valueMap.get("JAR_NAME");
                                        } else {
                                            value = value + ";" + valueMap.get("JAR_NAME");
                                        }
                                    }else if (valueMap.get("drivers") != null) {
                                        if (value.equals("")) {
                                            value = value + valueMap.get("drivers");
                                        } else {
                                            value = value + ";" + valueMap.get("drivers");
                                        }
                                    }
                                }
                            }
                            return value;
                        }
                    }
                }
            }
        }
        return ""; //$NON-NLS-1$
    }

    protected String getParaNameFromRepositoryName(String repositoryName, IElementParameter basePropertyParameter) {
        return getParaNameFromRepositoryName(elem, repositoryName, basePropertyParameter);
    }

    protected String getParaNameFromRepositoryName(IElement elem2, String repositoryName, IElementParameter basePropertyParameter) {
        for (IElementParameter param : (List<IElementParameter>) elem2.getElementParameters()) {
            // for job settings extra.(feature 2710)
            if (!sameExtraParameter(param)) {
                continue;
            }
            if (param.getRepositoryValue() != null) {
                if (param.getRepositoryProperty() != null && basePropertyParameter != null
                        && !param.getRepositoryProperty().equals(basePropertyParameter.getName())) {
                    // in case the parameter name is not linked to the current property tested (cf like tSqoopImport)
                    continue;
                }
                if (param.getRepositoryValue().equals(repositoryName)) {
                    return param.getName();
                }
            }
        }
        return null;
    }

    /**
     * DOC yzhang Comment method "init".
     *
     * Configuration for necessay parameters from class DynamicTabbedPropertiesSection.
     */
    public void init(IDynamicProperty dp) {
        this.dynamicProperty = dp;
        hashCurControls = dp.getHashCurControls();
        elem = dp.getElement();
        Object obj = dp.getPart();
        if (obj instanceof IMultiPageTalendEditor) {
            part = (IMultiPageTalendEditor) obj;
        } else {
            // throw new RuntimeException("Type IMultiPageTalendEditor is requried.");
        }
        section = dp.getSection();
        composite = dp.getComposite();

        editionControlHelper = new EditionControlHelper();
        // elem.addPropertyChangeListener(this);
    }

    /**
     * Getter for dynamicTabbedPropertySection.
     *
     * @return the dynamicTabbedPropertySection
     */
    public IDynamicProperty getDynamicProperty() {
        return this.dynamicProperty;
    }

    static WidgetFactory widgetFactory = null;

    /**
     * DOC yzhang Comment method "getWidgetFactory".
     *
     * Get the TabbedPropertySheetWidgetFactory for control creating.
     *
     * @return
     */
    protected WidgetFactory getWidgetFactory() {
        if (widgetFactory == null) {
            widgetFactory = new WidgetFactory();
        }
        return widgetFactory;
    }

    /**
     *
     * DOC amaumont DynamicTabbedPropertySection class global comment. Detailled comment <br/>
     *
     * @author amaumont $Id: DynamicTabbedPropertySection.java 344 2006-11-08 14:29:42 +0000 (mer., 08 nov. 2006)
     * smallet $
     *
     */
    public class EditionControlHelper {

        private final CheckErrorsHelper checkErrorsHelper;

        protected UndoRedoHelper undoRedoHelper;

        private ContentProposalAdapterExtended extendedProposal;

        /**
         * DOC amaumont EditionListenerManager constructor comment.
         */
        public EditionControlHelper() {
            this(new CheckErrorsHelper(), new UndoRedoHelper());
        }

        public EditionControlHelper(CheckErrorsHelper checkErrorsHelper, UndoRedoHelper undoRedoHelper) {
            this.checkErrorsHelper = checkErrorsHelper;
            this.undoRedoHelper = undoRedoHelper;
        }

        /**
         * DOC amaumont Comment method "checkErrors".
         *
         * @param t
         * @param b
         */
        public void checkErrors(Control control) {
            this.checkErrorsHelper.checkErrors(control);
        }

        /**
         * DOC amaumont Comment method "register".
         *
         * @param parameterName
         * @param control
         * @param checkSyntax
         */
        public void register(final String parameterName, final Control control) {
            if (parameterName == null || control == null) {
                throw new NullPointerException();
            }
            IElementParameter param = elem.getElementParameter(parameterName);
            if (param != null && !param.isNoContextAssist() && !param.isReadOnly() && !(control instanceof ReconcilerStyledText)) {

                final IProcess2 process = (IProcess2) getProcess(elem, part);

                if (elem instanceof INode) {
                    this.extendedProposal = TalendProposalUtils.installOn(control, process, (INode) elem);
                } else {
                    this.extendedProposal = TalendProposalUtils.installOn(control, process);
                }

                if (!elem.getElementParameter(parameterName).isNoCheck()) {
                    this.checkErrorsHelper.register(control, extendedProposal);
                }
                extendedProposal.addContentProposalListener(new IContentProposalListener() {

                    @Override
                    public void proposalAccepted(IContentProposal proposal) {
                        if (control instanceof Text) {
                            ContextParameterExtractor.saveContext(parameterName, elem, ((Text) control).getText(), process);
                        } else if (control instanceof StyledText) {
                            ContextParameterExtractor.saveContext(parameterName, elem, ((StyledText) control).getText(), process);
                        }
                    }
                });
                // this.checkErrorsHelper.checkErrors(control, false);
                ContextParameterExtractor.installOn(control, process, parameterName, elem);
            }

            this.undoRedoHelper.register(control);
        }

        public void unregisterUndo(Control control) {
            this.undoRedoHelper.unregister(control);
        }

        /**
         * DOC amaumont Comment method "register".
         *
         * @param control
         */
        public void unregister(Control control) {
            this.checkErrorsHelper.unregister(control);
            this.undoRedoHelper.unregister(control);
        }

    }

    private static Map<Control, ControlProperties> controlToProp = new HashMap<Control, ControlProperties>();

    /**
     *
     * DOC amaumont DynamicTabbedPropertySection class global comment. Detailled comment <br/>
     *
     * @author amaumont $Id: DynamicTabbedPropertySection.java 344 2006-11-08 14:29:42 +0000 (mer., 08 nov. 2006)
     * smallet $
     *
     */
    public class CheckErrorsHelper {

        /**
         * DOC amaumont CheckSyntaxHelper constructor comment.
         */
        public CheckErrorsHelper() {
            super();
        }

        private final FocusListener focusListenerForCheckingError = new FocusListener() {

            @Override
            public void focusGained(FocusEvent event) {
                focusGainedExecute((Control) event.widget);
            }

            @Override
            public void focusLost(FocusEvent event) {
                if (!extendedProposal.isProposalOpened()) {
                    Control control = (Control) event.widget;
                    setCodeProblems(null);
                    checkErrorsForPropertiesOnly(control);
                    IWorkbench workbench = PlatformUI.getWorkbench();
                    Property property = null;
                    if (workbench != null) {
                        IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
                        if (page != null) {
                            IEditorPart editorPart = page.getActiveEditor();
                            if (editorPart instanceof AbstractMultiPageTalendEditor) {
                                AbstractMultiPageTalendEditor multiPageTalendEditor = ((AbstractMultiPageTalendEditor) editorPart);
                                property = multiPageTalendEditor.getProcess().getProperty();
                            }
                        }
                    }
                    if (property == null) {
                        return;
                    }
                    ECodeLanguage language = ((RepositoryContext) org.talend.core.CorePlugin.getContext().getProperty(
                            org.talend.core.context.Context.REPOSITORY_CONTEXT_KEY)).getProject().getLanguage();
                    property.getInformations().clear();
                    if (language == ECodeLanguage.JAVA && proForJavaErrorMark != null) {
                        for (Problem problem : proForJavaErrorMark) {
                            if (ProblemStatus.ERROR.equals(problem.getStatus())) {
                                String problemResource = problem.getDescription();
                                Information information = PropertiesFactory.eINSTANCE.createInformation();
                                if (problemResource != null) {
                                    information.setText(problemResource);
                                }
                                information.setLevel(InformationLevel.ERROR_LITERAL);
                                property.getInformations().add(information);
                            }
                        }
                    } else if (language == ECodeLanguage.PERL && proForPerlErrorMark != null) {
                        for (String componentName : proForPerlErrorMark.keySet()) {
                            Problem problem = proForPerlErrorMark.get(componentName);
                            if (ProblemStatus.ERROR.equals(problem.getStatus())) {
                                String problemResource = problem.getDescription();
                                Information information = PropertiesFactory.eINSTANCE.createInformation();
                                if (problemResource != null) {
                                    information.setText(problemResource);
                                }
                                information.setLevel(InformationLevel.ERROR_LITERAL);
                                property.getInformations().add(information);
                            }
                        }

                    }
                    Problems.computePropertyMaxInformationLevel(property, false);
                    Problems.refreshRepositoryView();
                    Problems.refreshProblemTreeView();
                }
            }

        };

        private final KeyListener keyListenerForCheckingError = new KeyListener() {

            @Override
            public void keyPressed(KeyEvent event) {
                Control control = (Control) event.widget;
                resetErrorState(control);
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

        };

        private ContentProposalAdapterExtended extendedProposal;

        public void register(Control control, ContentProposalAdapterExtended extendedProposal) {
            control.addFocusListener(focusListenerForCheckingError);
            control.addKeyListener(keyListenerForCheckingError);
            this.extendedProposal = extendedProposal;
        }

        /**
         * DOC amaumont Comment method "unregister".
         *
         * @param control
         */
        public void unregister(Control control) {
            control.removeFocusListener(focusListenerForCheckingError);
            control.removeKeyListener(keyListenerForCheckingError);
        }

        private void focusGainedExecute(Control control) {
            resetErrorState(control);
        }

        private void refreshNode() {
            boolean flag = CorePlugin.getDefault().getPreferenceStore()
                    .getBoolean(TalendDesignerPrefConstants.PROPERTY_CODE_CHECK);
            if (flag) {
                return;
            }
            if (elem instanceof IGraphicalNode) {
                IGraphicalNode errorNode = (IGraphicalNode) elem;
                if (errorNode == null) {
                    return;
                }
                if (errorNode.isCheckProperty()) {
                    errorNode.setCheckProperty(false);
                    errorNode.setErrorFlag(false);
                    errorNode.setCompareFlag(false);
                    errorNode.setErrorInfo(null);
                    ((Node) errorNode).getNodeError().updateState("UPDATE_STATUS", false);//$NON-NLS-1$
                    errorNode.setErrorInfoChange("ERRORINFO", false);//$NON-NLS-1$
                }
            }

        }

        /**
         * DOC amaumont Comment method "checkSyntax".
         *
         * @param control
         * @param modifying
         */
        public void checkErrors(final Control control) {
            refreshNode();
            IElementParameter elementParameter = elem.getElementParameter(getParameterName(control));

            if (elementParameter.isReadOnly() || elementParameter.isNoCheck()) {
                return;
            }

            final Color bgColorError = control.getDisplay().getSystemColor(SWT.COLOR_RED);
            final Color fgColorError = control.getDisplay().getSystemColor(SWT.COLOR_WHITE);

            final ECodeLanguage language = ((RepositoryContext) org.talend.core.CorePlugin.getContext().getProperty(
                    org.talend.core.context.Context.REPOSITORY_CONTEXT_KEY)).getProject().getLanguage();

            IRunProcessService service = DesignerPlugin.getDefault().getRunProcessService();
            final ICodeProblemsChecker syntaxChecker = service.getSyntaxChecker(language);

            final String valueFinal = ControlUtils.getText(control);

            ControlProperties existingControlProperties = controlToProp.get(control);

            List<Problem> problems = getCodeProblems();
            proForJavaErrorMark = new ArrayList<Problem>();
            if (valueFinal != null) {
                String key = CodeGenerationUtils.buildProblemKey(elem.getElementName(), elementParameter.getName());
                if (language == ECodeLanguage.PERL) {
                    problems = syntaxChecker.checkProblemsForExpression(valueFinal);
                    getAllPerlProblem(key, problems);
                    showErrorMarkForPerl(elem);
                } else if (language == ECodeLanguage.JAVA) {
                    if (problems == null) {
                        problems = syntaxChecker.checkProblemsFromKey(key, null);
                    } else {
                        ((CodeProblemsChecker) syntaxChecker).updateNodeProblems(problems, key);
                    }
                    proForJavaErrorMark = syntaxChecker.checkProblemsForErrorMark(key, null);
                    showErrorMarkForJava(proForJavaErrorMark, elem);

                }
            }

            boolean isRequired = elem.getElementParameter(getParameterName(control)).isRequired();
            if (problems != null) {
                if (isRequired && (valueFinal == null || valueFinal.trim().length() == 0)) {
                    problems.add(new Problem(null,
                            Messages.getString("AbstractElementPropertySectionController.fieldRequired"), ProblemStatus.ERROR)); //$NON-NLS-1$
                }
            }

            if (problems != null && problems.size() > 0) {
                if (existingControlProperties == null) {
                    ControlProperties properties = new ControlProperties();
                    controlToProp.put(control, properties);
                    // store original properties to restore them when error will be corrected
                    properties.originalBgColor = control.getBackground();
                    properties.originalFgColor = control.getForeground();
                    properties.originalToolTip = control.getToolTipText();
                }

                control.setBackground(bgColorError);
                control.setForeground(fgColorError);
                String tooltip = Messages.getString("AbstractElementPropertySectionController.syntaxError"); //$NON-NLS-1$

                for (Problem problem : problems) {
                    tooltip += "\n" + problem.getDescription(); //$NON-NLS-1$
                }
                control.setToolTipText(tooltip);
            } else {
                resetErrorState(control);
            }
        }

        private void getAllPerlProblem(String key, List<Problem> problems) {
            if (proForPerlErrorMark != null && proForPerlErrorMark.size() > 0) {
                int indexM = key.indexOf(":");//$NON-NLS-1$
                String keyAfter = "";//$NON-NLS-1$
                if (indexM > 0) {
                    keyAfter = key.substring(0, key.indexOf(":"));//$NON-NLS-1$
                } else {
                    keyAfter = key;
                }
                Set<Map.Entry<String, Problem>> set = proForPerlErrorMark.entrySet();
                for (Iterator<Map.Entry<String, Problem>> ite = set.iterator(); ite.hasNext();) {
                    Map.Entry<String, Problem> tmp = ite.next();
                    if (tmp == null) {
                        continue;
                    }

                    String proKey = tmp.getKey();
                    if (proKey == null) {
                        proKey = "";//$NON-NLS-1$
                    }
                    int indMark = proKey.indexOf(":");//$NON-NLS-1$
                    if (indMark > 0) {
                        proKey = proKey.substring(0, proKey.indexOf(":"));//$NON-NLS-1$
                    }
                    if (!proKey.equals(keyAfter)) {
                        ite.remove();
                    }
                }
            }
            if (problems != null && problems.size() > 0) {
                for (Problem problem : problems) {
                    if (problem != null) {
                        proForPerlErrorMark.put(key, problem);
                    } else {
                        proForPerlErrorMark.put(key, new Problem());
                    }
                    return;
                }

            } else {
                proForPerlErrorMark.put(key, new Problem());
            }
        }

        private void showErrorMarkForPerl(IElement elem) {
            if (elem instanceof IGraphicalNode) {
                IGraphicalNode errorNode = (IGraphicalNode) elem;
                if (errorNode == null) {
                    return;
                }
                StringBuffer errorMessage = new StringBuffer(256);
                if (proForPerlErrorMark != null && proForPerlErrorMark.size() > 0) {
                    Set<Map.Entry<String, Problem>> set = proForPerlErrorMark.entrySet();

                    for (Entry<String, Problem> tmp : set) {
                        if (tmp == null) {
                            continue;
                        }
                        Problem pro = tmp.getValue();
                        if (pro == null) {
                            continue;
                        }
                        String description = pro.getDescription();
                        if (!"".equals(description) && description != null) {//$NON-NLS-1$
                            errorMessage.append(description.replaceFirst("\r\n", ""));//$NON-NLS-1$
                            errorMessage.append("\n");//$NON-NLS-1$
                        }

                    }

                    if ((!"".equals(errorMessage.toString())) && errorMessage != null) {
                        if (errorNode.isCheckProperty() == false) {
                            errorNode.setCheckProperty(true);
                            errorNode.setErrorFlag(true);
                            errorNode.setCompareFlag(false);
                            errorNode.setErrorInfo(errorMessage.toString());
                            ((Node) errorNode).getNodeError().updateState("UPDATE_STATUS", true);//$NON-NLS-1$
                            errorNode.setErrorInfoChange("ERRORINFO", true);//$NON-NLS-1$
                        }
                    } else {
                        if (errorNode.isCheckProperty()) {
                            errorNode.setCheckProperty(false);
                            errorNode.setErrorFlag(false);
                            errorNode.setCompareFlag(false);
                            errorNode.setErrorInfo(null);
                            ((Node) errorNode).getNodeError().updateState("UPDATE_STATUS", false);//$NON-NLS-1$
                            errorNode.setErrorInfoChange("ERRORINFO", false);//$NON-NLS-1$
                        }
                    }

                } else {
                    if (errorNode.isCheckProperty()) {

                        errorNode.setCheckProperty(false);
                        errorNode.setErrorFlag(false);
                        errorNode.setCompareFlag(false);
                        errorNode.setErrorInfo(null);
                        ((Node) errorNode).getNodeError().updateState("UPDATE_STATUS", false);//$NON-NLS-1$
                        errorNode.setErrorInfoChange("ERRORINFO", false);//$NON-NLS-1$
                    }

                }
            }
        }

        private void showErrorMarkForJava(List<Problem> problems, IElement elem) {
            Node errorNode = null;
            if (elem instanceof Node) {
                errorNode = (Node) elem;
                if (errorNode == null) {
                    return;
                }
                if (problems != null && problems.size() > 0) {
                    StringBuffer errorMessage = new StringBuffer(256);
                    for (Problem pro : problems) {
                        if (pro == null || pro.getDescription() == null) {
                            continue;
                        }
                        if (pro.getKey() != null) {
                            int indMark = pro.getKey().indexOf(":");//$NON-NLS-1$
                            String proKey = "";//$NON-NLS-1$
                            if (indMark > 0) {
                                proKey = pro.getKey().substring(0, pro.getKey().indexOf(":"));//$NON-NLS-1$
                            } else {
                                proKey = pro.getKey();
                            }
                            if (errorNode.getUniqueName().equals(proKey)) {
                                errorMessage.append(pro.getDescription());
                                errorMessage.append("\n");//$NON-NLS-1$
                            }
                        }
                    }
                    if (errorNode.isCheckProperty() == false) {
                        if ((!"".equals(errorMessage)) && errorMessage != null) {//$NON-NLS-1$
                            errorNode.setCheckProperty(true);
                            errorNode.setErrorFlag(true);
                            errorNode.setCompareFlag(false);
                            errorNode.setErrorInfo(errorMessage.toString());
                            errorNode.getNodeError().updateState("UPDATE_STATUS", true);//$NON-NLS-1$
                            errorNode.setErrorInfoChange("ERRORINFO", true);//$NON-NLS-1$
                        }
                    }
                } else {
                    if (errorNode.isCheckProperty()) {

                        errorNode.setCheckProperty(false);
                        errorNode.setErrorFlag(false);
                        errorNode.setCompareFlag(false);
                        errorNode.setErrorInfo(null);
                        errorNode.getNodeError().updateState("UPDATE_STATUS", false);//$NON-NLS-1$
                        errorNode.setErrorInfoChange("ERRORINFO", false);//$NON-NLS-1$
                    }
                }

            }

        }

        /**
         * DOC amaumont Comment method "resetErrorState".
         *
         * @param control
         * @param previousProblem
         */
        private void resetErrorState(final Control control) {
            ControlProperties existingControlProperties = controlToProp.get(control);
            if (existingControlProperties != null) {
                control.setToolTipText(existingControlProperties.originalToolTip);
                control.setBackground(existingControlProperties.originalBgColor);
                control.setForeground(existingControlProperties.originalFgColor);
                controlToProp.remove(control);
            }
        }
    }

    /**
     *
     * Container of original properties of Control. <br/>
     *
     * $Id: DynamicTabbedPropertySection.java 865 2006-12-06 06:14:57 +0000 (鏄熸湡涓�, 06 鍗佷簩鏈� 2006) bqian $
     *
     */
    class ControlProperties {

        private Color originalBgColor;

        private Color originalFgColor;

        private String originalToolTip;

        /**
         * DOC amaumont ControlProperties constructor comment.
         */
        public ControlProperties() {
            super();
        }
    }

    protected Command getTextCommandForHelper(String paramName, String text) {
        return new PropertyChangeCommand(elem, paramName, text);
    }

    /**
     *
     * DOC amaumont DynamicTabbedPropertySection class global comment. Detailled comment <br/>
     *
     * @author amaumont
     *
     * $Id: DynamicTabbedPropertySection.java 865 2006-12-06 06:14:57 +0000 (鏄熸湡涓�, 06 鍗佷簩鏈� 2006) bqian $
     *
     */
    public class UndoRedoHelper {

        protected TypedTextCommandExecutor typedTextCommandExecutor;

        /**
         * DOC amaumont Comment method "unregister".
         *
         * @param control
         */
        public void unregister(Control control) {
            // ControlUtils.removeModifyListener(control, modifyListenerForUndoRedo);
            typedTextCommandExecutor.unregister(control);
        }

        public UndoRedoHelper() {
            this.typedTextCommandExecutor = new TypedTextCommandExecutor() {

                @Override
                public void addNewCommand(Control control) {
                    String name = getParameterName(control);
                    String text = ControlUtils.getText(control);
                    Command cmd = getTextCommandForHelper(name, text);
                    // getCommandStack().execute(cmd);
                    executeCommand(cmd);
                }

                @Override
                public void updateCommand(Control control) {
                    CommandStack commandStack = getCommandStack();

                    String name = getParameterName(control);
                    String text = ControlUtils.getText(control);

                    if (commandStack == null) {
                        executeCommand(new PropertyChangeCommand(elem, name, text));
                        return;
                    }

                    Object[] commands = commandStack.getCommands();

                    if (commands.length == 0 || commandStack.getRedoCommand() != null) {
                        addNewCommand(control);
                    } else {
                        Object lastCommandObject = commands[commands.length - 1];
                        if (lastCommandObject instanceof PropertyChangeCommand) {
                            PropertyChangeCommand lastCommand = (PropertyChangeCommand) lastCommandObject;
                            if (name.equals(lastCommand.getPropName()) && (lastCommand.getElement() == elem)) {
                                lastCommand.dispose();
                                // commandStack.execute(new PropertyChangeCommand(elem, name, text));
                                executeCommand(new PropertyChangeCommand(elem, name, text));
                                // lastCommand.modifyValue(text);
                            }
                        }
                    }
                }

            };

        }

        /**
         * DOC amaumont Comment method "register".
         *
         * @param control
         */
        public void register(Control control) {
            // ControlUtils.addModifyListener(control, modifyListenerForUndoRedo);
            typedTextCommandExecutor.register(control);
        }
    }

    /**
     * DOC amaumont Comment method "getParameterName".
     *
     * @param control
     * @return
     */
    public String getParameterName(Control control) {

        String name = (String) control.getData(PARAMETER_NAME);
        if (name == null) { // if the control don't support this property, then take in the list.
            name = (String) hashCurControls.getKey(control);
        }
        if (name == null) {
            throw new IllegalStateException(
                    "parameterName shouldn't be null or you call this method too early ! (control value : '" //$NON-NLS-1$
                            + ControlUtils.getText(control) + "')"); //$NON-NLS-1$
        }
        return name;
    }

    /**
     * Get the command stack of the Gef editor.
     *
     * @return
     */
    protected CommandStack getCommandStack() {
        if (dynamicProperty != null && dynamicProperty instanceof MultipleThreadDynamicComposite) {
            CommandStack commandStack = ((MultipleThreadDynamicComposite) dynamicProperty).getCommandStack();
            if (commandStack != null) {
                return commandStack;
            }
        }
        if (part == null) {
            return null;
        }
        Object adapter = part.getAdapter(CommandStack.class);
        return (CommandStack) adapter;
    }

    protected void executeCommand(Command c) {
        if (c == null) {
            return;
        }

        if (getCommandStack() != null) {
            getCommandStack().execute(c);
        } else {
            // if can't find command stack, just execute it.
            c.execute();
        }
    }

    private void refreshDynamicProperty() {
        if (this.dynamicProperty == null) {
            return;
        }
        dynamicProperty.refresh();
    }

    /**
     * Accept Text and StyledText control.
     *
     * @param labelText
     */
    public void addDragAndDropTarget(final Control textControl) {
        DropTargetListener dropTargetListener = new DropTargetListener() {

            String propertyName = null;

            @Override
            public void dragEnter(final DropTargetEvent event) {
            }

            @Override
            public void dragLeave(final DropTargetEvent event) {
            }

            @Override
            public void dragOperationChanged(final DropTargetEvent event) {
            }

            @Override
            public void dragOver(final DropTargetEvent event) {
                if (TextTransfer.getInstance().isSupportedType(event.currentDataType)) {
                    propertyName = getParameterName(textControl);
                    for (int i = 0; i < elem.getElementParameters().size(); i++) {
                        IElementParameter param = elem.getElementParameters().get(i);
                        if (param.getName().equals(propertyName)) {
                            if (param.isReadOnly()) {
                                event.detail = DND.ERROR_INVALID_DATA;
                            }
                        }
                    }
                }
            }

            @Override
            public void drop(final DropTargetEvent event) {
                if (propertyName != null) {
                    String text;
                    if (textControl instanceof StyledText) {
                        text = ((StyledText) textControl).getText() + (String) event.data;
                        ((StyledText) textControl).setText(text);

                    } else {
                        text = ((Text) textControl).getText() + (String) event.data;
                        ((Text) textControl).setText(text);
                    }
                    Command cmd = new PropertyChangeCommand(elem, propertyName, text);
                    // getCommandStack().execute(cmd);
                    executeCommand(cmd);
                }
            }

            @Override
            public void dropAccept(final DropTargetEvent event) {
            }
        };

        DropTarget target = new DropTarget(textControl, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT);
        Transfer[] transfers = new Transfer[] { TextTransfer.getInstance() };
        target.setTransfer(transfers);
        target.addDropListener(dropTargetListener);

    }

    /**
     * Sets the elem.
     *
     * @param elem the elem to set
     */
    protected void setElem(Element elem) {
        this.elem = elem;
    }

    /**
     * Sets the hashCurControls.
     *
     * @param hashCurControls the hashCurControls to set
     */
    protected void setHashCurControls(BidiMap hashCurControls) {
        this.hashCurControls = hashCurControls;
    }

    /**
     * Sets the part.
     *
     * @param part the part to set
     */
    protected void setPart(AbstractMultiPageTalendEditor part) {
        this.part = part;
    }

    /**
     * Sets the section.
     *
     * @param section the section to set
     */
    protected void setSection(EComponentCategory section) {
        this.section = section;
    }

    /**
     * DOC amaumont Comment method "checkErrors".
     *
     * @param control must be or extends <code>Text</code> or <code>StyledText</code>
     */
    protected void checkErrorsForPropertiesOnly(Control control) {
        if (this.section == EComponentCategory.BASIC) {
            editionControlHelper.checkErrors(control);
        }
    }

    public abstract void refresh(IElementParameter param, boolean check);

    /**
     * qzhang Comment method "fixedCursorPosition".
     *
     * @param param
     * @param labelText
     * @param value
     * @param valueChanged
     */
    protected void fixedCursorPosition(IElementParameter param, Control labelText, Object value, boolean valueChanged) {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IWorkbenchPart workbenchPart = page.getActivePart();
        // tacokit
        boolean update = false;
        if (param != null && param instanceof ElementParameter && param.getFieldType().equals(EParameterFieldType.TEXT)) {
            Object sourceName = ((ElementParameter) param).getTaggedValue("org.talend.sdk.component.source"); //$NON-NLS-1$
            if ("tacokit".equalsIgnoreCase(String.valueOf(sourceName))) { //$NON-NLS-1$
                update = true;
            }
        }

        if ((workbenchPart instanceof PropertySheet) || (workbenchPart instanceof JobSettingsView)
                || (workbenchPart instanceof ComponentSettingsView) || update) {
            Object control = editionControlHelper.undoRedoHelper.typedTextCommandExecutor.getActiveControl();
            if (param.getName().equals(control) && valueChanged && !param.isRepositoryValueUsed()) {
                String previousText = editionControlHelper.undoRedoHelper.typedTextCommandExecutor.getPreviousText2();
                String currentText = String.valueOf(value);
                labelText.setFocus();
                ControlUtils.setCursorPosition(labelText, getcursorPosition(previousText, currentText));
            }
        }
    }

    /**
     * qzhang Comment method "getcursorPosition".
     *
     * @param previousText
     * @param currentText
     * @return
     */
    private int getcursorPosition(String previousText, String currentText) {
        if (previousText.length() == currentText.length() + 1) {
            return getLeftCharPosition(currentText, previousText, false);
        } else if (previousText.length() == currentText.length() - 1) {
            return getLeftCharPosition(previousText, currentText, true);
        }
        return 0;
    }

    /**
     * qzhang Comment method "getLeftCharPosition".
     *
     * @param previousText
     * @param currentText
     * @return
     */
    private int getLeftCharPosition(String previousText, String currentText, boolean add) {
        int i = 0;
        for (; i < currentText.length() - 1; i++) {
            if (currentText.charAt(i) != previousText.charAt(i)) {
                break;
            }
        }
        if (add) {
            return i + 1;
        } else {
            return i;
        }
    }

    public void openSqlBuilderBuildIn(final ConnectionParameters connParameters, final String propertyName) {
        ISQLBuilderService service = GlobalServiceRegister.getDefault().getService(ISQLBuilderService.class);
        service.openSQLBuilderDialog(connParameters, composite, elem, propertyName, getCommandStack(), this, part);
    }

    protected ConnectionParameters connParameters;

    private void setAllConnectionParameters(String typ, IElement element) {
        IElementParameter basePropertyParameter = null;
        for (IElementParameter param : elem.getElementParameters()) {
            if (param.getFieldType() == EParameterFieldType.PROPERTY_TYPE) {
                if (param.getRepositoryValue().startsWith("DATABASE")) {
                    basePropertyParameter = param;
                    break;
                }
            }
        }
        // jobsetting view load the db info from current selected category
        IElementParameter updateBasePropertyParameter = updateBasePropertyParameter();
        if (updateBasePropertyParameter != null && !updateBasePropertyParameter.equals(basePropertyParameter)) {
            basePropertyParameter = updateBasePropertyParameter;
        }
        String type = null;
        ExtractMetaDataUtils extractMeta = ExtractMetaDataUtils.getInstance();
        if (typ != null && !typ.equals("")) { //$NON-NLS-1$
            type = typ;
        } else {
            type = getValueFromRepositoryName(element, "TYPE", basePropertyParameter); //$NON-NLS-1$
        }
        if (type.equals("Oracle") || type.contains("OCLE")) {
            IElementParameter ele = element.getElementParameter("CONNECTION_TYPE");
            if (ele != null) {
                type = (String) ele.getValue();
            } else {
                type = "ORACLE_SID"; //$NON-NLS-1$
            }
        }
        if (type.equalsIgnoreCase("REDSHIFT")) {
       	 IElementParameter ele = element.getElementParameter("JDBC_URL");
       	  if (ele != null&&((String)ele.getValue()).equals("SSO")) {
                type = EDatabaseTypeName.REDSHIFT_SSO.getXmlName();
             } else {
                type = EDatabaseTypeName.REDSHIFT.getXmlName(); // $NON-NLS-1$
             }
       }
        // Get real hsqldb type
        if (type.equals(EDatabaseTypeName.HSQLDB.name())
                && getValueFromRepositoryName(element, "RUNNING_MODE", basePropertyParameter).equals("HSQLDB_INPROGRESS_PERSISTENT")) {//$NON-NLS-1$
            type = EDatabaseTypeName.HSQLDB_IN_PROGRESS.getDisplayName();
        }
        // If the dbtype has been setted don't reset it again unless the dbtype of connParameters is null.
        if (StringUtils.trimToNull(type) == null && StringUtils.trimToNull(connParameters.getDbType()) == null) {
            type = EDatabaseTypeName.GENERAL_JDBC.getXmlName();
        }
        if (StringUtils.trimToNull(type) != null) {
            connParameters.setDbType(type);
        }

        String frameWorkKey = getValueFromRepositoryName(element, "FRAMEWORK_TYPE", basePropertyParameter); //$NON-NLS-1$
        connParameters.setFrameworkType(frameWorkKey);

        String schema = getValueFromRepositoryName(element, EConnectionParameterName.SCHEMA.getName(), basePropertyParameter);
        connParameters.setSchema(schema);

        if ((elem instanceof Node) && (((Node)elem).getComponent().getComponentType().equals(EComponentType.GENERIC)
                || (element instanceof INode
                        && ((INode) element).getComponent().getComponentType().equals(EComponentType.GENERIC)))) {
            String userName = getValueFromRepositoryName(element, EConnectionParameterName.GENERIC_USERNAME.getDisplayName(), basePropertyParameter);
            connParameters.setUserName(userName);

            String password = getValueFromRepositoryName(element, EConnectionParameterName.GENERIC_PASSWORD.getDisplayName(), basePropertyParameter);
            connParameters.setPassword(password);

            String url = getValueFromRepositoryName(element, EConnectionParameterName.GENERIC_URL.getDisplayName(), basePropertyParameter);
            connParameters.setUrl(TalendTextUtils.removeQuotes(url));

            String driverJar = getValueFromRepositoryName(element, EConnectionParameterName.GENERIC_DRIVER_JAR.getDisplayName(),
                    basePropertyParameter);
            connParameters.setDriverJar(TalendTextUtils.removeQuotes(driverJar));

            String driverClass = getValueFromRepositoryName(element, EConnectionParameterName.GENERIC_DRIVER_CLASS.getDisplayName(),
                    basePropertyParameter);
            connParameters.setDriverClass(TalendTextUtils.removeQuotes(driverClass));
        }else{
            String userName = getValueFromRepositoryName(element, EConnectionParameterName.USERNAME.getName(), basePropertyParameter);
            connParameters.setUserName(userName);

            String password = getValueFromRepositoryName(element, EConnectionParameterName.PASSWORD.getName(), basePropertyParameter);
            connParameters.setPassword(password);

         // General jdbc
            String url = getValueFromRepositoryName(element, EConnectionParameterName.URL.getName(), basePropertyParameter);
            if (StringUtils.isEmpty(url)) {
                // for oracle RAC
                // url = getValueFromRepositoryName(element, "RAC_" + EConnectionParameterName.URL.getName());
                // Changed by Marvin Wang on Feb. 14, 2012 for bug TDI-19597. Above is the original code, below is new code
                // to get the Oracle RAC url.
                if (EDatabaseTypeName.ORACLE_CUSTOM.getXmlName().equals(type)) {
                    url = getValueFromRepositoryName(element, "RAC_" + EConnectionParameterName.URL.getName(), basePropertyParameter);
                }
            }
            connParameters.setUrl(TalendTextUtils.removeQuotes(url));

            String driverJar = getValueFromRepositoryName(element, EConnectionParameterName.DRIVER_JAR.getName(),
                    basePropertyParameter);
            connParameters.setDriverJar(TalendTextUtils.removeQuotes(driverJar));

            String driverClass = getValueFromRepositoryName(element, EConnectionParameterName.DRIVER_CLASS.getName(),
                    basePropertyParameter);
            String driverName = getValueFromRepositoryName(element, "DB_VERSION", basePropertyParameter); //$NON-NLS-1$
            if (StringUtils.isBlank(driverName) && EDatabaseTypeName.MSSQL.getDisplayName().equals(connParameters.getDbType())) {
                driverName = getValueFromRepositoryName(element, "DRIVER", basePropertyParameter); //$NON-NLS-1$
            }
            String dbVersionName = EDatabaseVersion4Drivers.getDbVersionName(type, driverName);
            connParameters.setDbVersion(dbVersionName);
            if (EDatabaseVersion4Drivers.VERTICA_5_1.getVersionValue().equals(dbVersionName)
                    || EDatabaseVersion4Drivers.VERTICA_6.getVersionValue().equals(dbVersionName)
                    || EDatabaseVersion4Drivers.VERTICA_6_1_X.getVersionValue().equals(dbVersionName)
                    || EDatabaseVersion4Drivers.VERTICA_7.getVersionValue().equals(dbVersionName) 
                    || EDatabaseVersion4Drivers.VERTICA_7_1_X.getVersionValue().equals(dbVersionName)) {
                driverClass = EDatabase4DriverClassName.VERTICA2.getDriverClass();
            }

            connParameters.setDriverClass(TalendTextUtils.removeQuotes(driverClass));

            if (driverClass != null && !"".equals(driverClass)
                    && !EDatabaseTypeName.GENERAL_JDBC.getDisplayName().equals(connParameters.getDbType())) {
                if (driverClass.startsWith("\"") && driverClass.endsWith("\"")) {
                    driverClass = TalendTextUtils.removeQuotes(driverClass);
                }
                String dbTypeByClassName = "";
                if (driverJar != null && !"".equals(driverJar)) {
                    dbTypeByClassName = extractMeta.getDbTypeByClassNameAndDriverJar(driverClass, driverJar);
                } else {
                    dbTypeByClassName = extractMeta.getDbTypeByClassName(driverClass);
                }

                if (dbTypeByClassName != null) {
                    connParameters.setDbType(dbTypeByClassName);
                }
            }
        }

        String host = getValueFromRepositoryName(element, EConnectionParameterName.SERVER_NAME.getName(), basePropertyParameter);
        connParameters.setHost(host);

        String port = getValueFromRepositoryName(element, EConnectionParameterName.PORT.getName(), basePropertyParameter);
        connParameters.setPort(port);

        boolean https = Boolean.parseBoolean(getValueFromRepositoryName(element, EConnectionParameterName.HTTPS.getName(),
                basePropertyParameter));
        connParameters.setHttps(https);

        boolean isOracleOCI = type.equals(EDatabaseTypeName.ORACLE_OCI.getXmlName())
                || type.equals(EDatabaseTypeName.ORACLE_OCI.getDisplayName());
        if (isOracleOCI) {
            String localServiceName = getValueFromRepositoryNameAndParameterName(element, EConnectionParameterName.SID.getName(),
                    EParameterName.LOCAL_SERVICE_NAME.getName());
            // sid is the repository value both for DBName and Local_service_name
            connParameters.setLocalServiceName(localServiceName);
        }

        String datasource = getValueFromRepositoryName(element, EConnectionParameterName.DATASOURCE.getName(),
                basePropertyParameter);
        connParameters.setDatasource(datasource);

        // qli modified to fix the bug "7364".

        String dbName = getValueFromRepositoryName(element, EConnectionParameterName.SID.getName(), basePropertyParameter);
        if (EDatabaseTypeName.EXASOL.getDisplayName().equals(connParameters.getDbType())) {
            if (dbName.contains("\\\"")) {
                dbName = dbName.replace("\\\"", "");
            }
            dbName = TextUtil.removeQuots(dbName);
        } else if (EDatabaseTypeName.GENERAL_JDBC.getDisplayName().equals(connParameters.getDbType())) {
            dbName = ""; //$NON-NLS-1$
        }
        connParameters.setDbName(dbName);
        EDatabaseTypeName dbtype = EDatabaseTypeName.getTypeFromDbType(type);
        if (ManagerConnection.isSchemaFromSidOrDatabase(dbtype)) {
        	connParameters.setSchema(dbName);
        }
        if (connParameters.getDbType().equals(EDatabaseTypeName.SQLITE.getXmlName())
                || connParameters.getDbType().equals(EDatabaseTypeName.ACCESS.getXmlName())
                || connParameters.getDbType().equals(EDatabaseTypeName.FIREBIRD.getXmlName())) {
            String file = getValueFromRepositoryName(element, EConnectionParameterName.FILE.getName(), basePropertyParameter);
            connParameters.setFilename(file);
        }

        String dir = getValueFromRepositoryName(element, EConnectionParameterName.DIRECTORY.getName(), basePropertyParameter);
        if (type.equals(EDatabaseTypeName.HSQLDB_IN_PROGRESS.getDisplayName())) {
            dir = getValueFromRepositoryName(elem, EConnectionParameterName.DBPATH.getName(), basePropertyParameter);
        }
        connParameters.setDirectory(dir);

        String jdbcProps = getValueFromRepositoryName(element, EConnectionParameterName.PROPERTIES_STRING.getName(),
                basePropertyParameter);
        if (EDatabaseTypeName.ORACLE_CUSTOM.getDbType().equals(typ)) {
            // for ssl
            String useSSL = getValueFromRepositoryName(element, "USE_SSL"); //$NON-NLS-1$
            connParameters.getParameters().put(ConnParameterKeys.CONN_PARA_KEY_USE_SSL, useSSL);
            // trustStore
            String trustStore = getValueFromRepositoryName(element, "SSL_TRUSTSERVER_TRUSTSTORE");
            connParameters.getParameters().put(ConnParameterKeys.CONN_PARA_KEY_SSL_TRUST_STORE_PATH,
                    TalendQuoteUtils.removeQuotesIfExist(trustStore));
            // trusstStore pwd
            String trustStorePwd = getValueFromRepositoryName(element, "SSL_TRUSTSERVER_PASSWORD");
            connParameters.getParameters().put(ConnParameterKeys.CONN_PARA_KEY_SSL_TRUST_STORE_PASSWORD,
                    TalendQuoteUtils.removeQuotesIfExist(trustStorePwd));
            // clientAuth
            String clientAuth = getValueFromRepositoryName(element, "NEED_CLIENT_AUTH");
            connParameters.getParameters().put(ConnParameterKeys.CONN_PARA_KEY_NEED_CLIENT_AUTH, clientAuth);
            // keyStore
            String keyStore = getValueFromRepositoryName(element, "SSL_KEYSTORE");
            connParameters.getParameters().put(ConnParameterKeys.CONN_PARA_KEY_SSL_KEY_STORE_PATH,
                    TalendQuoteUtils.removeQuotesIfExist(keyStore));
            // keyStorePwd
            String keyStorePwd = getValueFromRepositoryName(element, "SSL_KEYSTORE_PASSWORD");
            connParameters.getParameters().put(ConnParameterKeys.CONN_PARA_KEY_SSL_KEY_STORE_PASSWORD,
                    TalendQuoteUtils.removeQuotesIfExist(keyStorePwd));
        }
        connParameters.setJdbcProperties(jdbcProps);

        String realTableName = null;
        if (EmfComponent.REPOSITORY.equals(elem.getPropertyValue(EParameterName.SCHEMA_TYPE.getName()))) {
            final Object propertyValue = elem.getPropertyValue(EParameterName.REPOSITORY_SCHEMA_TYPE.getName());
            IMetadataTable metadataTable = null;

            String connectionId = propertyValue.toString().split(" - ")[0];
            String tableLabel = propertyValue.toString().split(" - ")[1];

            IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
            Item item = null;
            try {
                IRepositoryViewObject repobj = factory.getLastVersion(connectionId);
                if (repobj != null) {
                    Property property = repobj.getProperty();
                    if (property != null) {
                        item = property.getItem();
                    }
                }
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
            if (item != null && item instanceof ConnectionItem) {
                Connection connection = ((ConnectionItem) item).getConnection();
                for (org.talend.core.model.metadata.builder.connection.MetadataTable table : ConnectionHelper
                        .getTables(connection)) {
                    if (table.getLabel().equals(tableLabel)) {
                        metadataTable = ConvertionHelper.convert(table);
                        break;
                    }
                }
            }

            if (metadataTable != null) {
                realTableName = metadataTable.getTableName();
            }
        }
        connParameters.setDbType(type);
        if (!EDatabaseTypeName.GENERAL_JDBC.getDisplayName().equals(type)) {
            connParameters.setDriverClass(EDatabase4DriverClassName.getDriverClassByDbType(type));
        }
        connParameters.setSchemaName(QueryUtil.getTableName(elem, connParameters.getMetadataTable(),
                TalendTextUtils.removeQuotes(schema), type, realTableName));
    }

    protected void initAlternateSchema(IElement element, IContext context) {
        String schemaName = getParameterValueWithContext(element,
                "ALTERNATE_SCHEMA", context, null);
        if (schemaName != null && !schemaName.trim().isEmpty()) {
            connParameters.setSchema(schemaName);
        }
    }

    protected void initConnectionParametersWithContext(IElement element, IContext context) {

        IElementParameter basePropertyParameter = null;
        for (IElementParameter param : elem.getElementParameters()) {
            if (param.getFieldType() == EParameterFieldType.PROPERTY_TYPE) {
                if (param.getRepositoryValue().startsWith("DATABASE")) {
                    basePropertyParameter = param;
                    break;
                }
            }
        }
        // jobsetting view load the db info from current selected category
        IElementParameter updateBasePropertyParameter = updateBasePropertyParameter();
        if (updateBasePropertyParameter != null && !updateBasePropertyParameter.equals(basePropertyParameter)) {
            basePropertyParameter = updateBasePropertyParameter;
        }
        // qli modified to fix the bug "7364".
        if (connParameters == null) {
            connParameters = new ConnectionParameters();
        }

        String dbType = connParameters.getDbType();
        Object value = elem.getPropertyValue("USE_EXISTING_CONNECTION"); //$NON-NLS-1$
        IElementParameter compList = elem.getElementParameterFromField(EParameterFieldType.COMPONENT_LIST);
        if (value != null && (value instanceof Boolean) && ((Boolean) value) && compList != null) {
            if (connectionNode == null) {
                Object compValue = compList.getValue();
                if (compValue != null && !compValue.equals("")) { //$NON-NLS-1$
                    List<? extends INode> nodes = part.getProcess().getGeneratingNodes();

                    for (INode node : nodes) {
                        if (node.getUniqueName().equals(compValue) && (node instanceof INode)) {
                            connectionNode = node;
                            break;
                        }
                    }

                }
            }
            if (connectionNode != null) {
                element = connectionNode;
            }
        }
        String dbName = getParameterValueWithContext(element, EConnectionParameterName.SID.getName(), context,
                basePropertyParameter);
        if (EDatabaseTypeName.EXASOL.getDisplayName().equals(dbType)) {
            if (dbName.contains("\\\"")) {
                dbName = dbName.replace("\\\"", "");
            }
            dbName = TextUtil.removeQuots(dbName);
        } else if (EDatabaseTypeName.GENERAL_JDBC.getDisplayName().equals(connParameters.getDbType())) {
            dbName = ""; //$NON-NLS-1$
        }
        connParameters.setDbName(dbName);

        if ((elem instanceof Node) && (((Node)elem).getComponent().getComponentType().equals(EComponentType.GENERIC)
                || (element instanceof INode
                        && ((INode) element).getComponent().getComponentType().equals(EComponentType.GENERIC)))) {
            connParameters.setUserName(getParameterValueWithContext(element, EConnectionParameterName.GENERIC_USERNAME.getDisplayName(), context,
                    basePropertyParameter));
            connParameters.setPassword(getParameterValueWithContext(element, EConnectionParameterName.GENERIC_PASSWORD.getDisplayName(), context,
                    basePropertyParameter));
            String url = TalendTextUtils.removeQuotesIfExist(getParameterValueWithContext(element, EConnectionParameterName.GENERIC_URL.getDisplayName(), context,
                    basePropertyParameter));
            connParameters.setUrl(url);
            String jar = TalendTextUtils.removeQuotesIfExist(getParameterValueWithContext(element, EConnectionParameterName.GENERIC_DRIVER_JAR.getDisplayName(), context,
                    basePropertyParameter));
            connParameters.setDriverJar(jar);
            String driverClass = TalendTextUtils.removeQuotesIfExist(getParameterValueWithContext(element, EConnectionParameterName.GENERIC_DRIVER_CLASS.getDisplayName(), context,
                    basePropertyParameter));
            connParameters.setDriverClass(driverClass);
        }else{
            IElementParameter elementParameter = element.getElementParameter("PASS");//$NON-NLS-1$
            boolean containContextParam = ContextParameterUtils
                    .isContainContextParam((elementParameter != null && elementParameter.getValue() != null) ? elementParameter.getValue().toString()
                            : ""); //$NON-NLS-1$
            connParameters.setPassword(getParameterValueWithContext(element, EConnectionParameterName.PASSWORD.getName(), context,
                    basePropertyParameter),containContextParam);
            connParameters.setUserName(getParameterValueWithContext(element, EConnectionParameterName.USERNAME.getName(), context,
                    basePropertyParameter));
            String url = TalendTextUtils.removeQuotesIfExist(getParameterValueWithContext(element,
                    EConnectionParameterName.URL.getName(), context, basePropertyParameter));
            if (StringUtils.isEmpty(url)) {
                // try to get url for oracle RAC.
                // url = TalendTextUtils.removeQuotesIfExist(getParameterValueWithContext(element,
                // "RAC_" + EConnectionParameterName.URL.getName(), context));
                // Changed by Marvin Wang on Feb. 14, 2012 for bug TDI-19597. Above is the original code, below is new code
                // to get the Oracle RAC url.
                if (EDatabaseTypeName.ORACLE_CUSTOM.getDisplayName().equals(dbType)) {
                    url = TalendTextUtils.removeQuotesIfExist(getParameterValueWithContext(element, "RAC_"
                            + EConnectionParameterName.URL.getName(), context, basePropertyParameter));
                }
            }
            connParameters.setUrl(url);
            String driverClass = TalendTextUtils.removeQuotesIfExist(getParameterValueWithContext(element,
                    EConnectionParameterName.DRIVER_CLASS.getName(), context, basePropertyParameter));
            if (element != null) {
                String dbVersion = getValueFromRepositoryName(element, "DB_VERSION");
                if (EDatabaseVersion4Drivers.VERTICA_6.getVersionValue().equals(dbVersion)
                        || EDatabaseVersion4Drivers.VERTICA_5_1.getVersionValue().equals(dbVersion)
                        || EDatabaseVersion4Drivers.VERTICA_6_1_X.getVersionValue().equals(dbVersion)
                        || EDatabaseVersion4Drivers.VERTICA_7.getVersionValue().equals(dbVersion) 
                        || EDatabaseVersion4Drivers.VERTICA_7_1_X.getVersionValue().equals(dbVersion)) {
                    driverClass = EDatabase4DriverClassName.VERTICA2.getDriverClass();
                }
            }
            if (EDatabaseTypeName.GENERAL_JDBC.getDisplayName().equals(dbType)) {
                connParameters.setDriverClass(driverClass);// tJDBCSCDELT
            } else {
                connParameters.setDriverClass(EDatabase4DriverClassName.getDriverClassByDbType(dbType));
            }
            connParameters.setDriverJar(TalendTextUtils.removeQuotesIfExist(getParameterValueWithContext(element,
                    EConnectionParameterName.DRIVER_JAR.getName(), context, basePropertyParameter)));
        }

        connParameters.setPort(getParameterValueWithContext(element, EConnectionParameterName.PORT.getName(), context,
                basePropertyParameter));
        connParameters.setSchema(getParameterValueWithContext(element, EConnectionParameterName.SCHEMA.getName(), context,
                basePropertyParameter));
        connParameters.setHost(getParameterValueWithContext(element, EConnectionParameterName.SERVER_NAME.getName(), context,
                basePropertyParameter));

        String dir = getParameterValueWithContext(element, EConnectionParameterName.DIRECTORY.getName(), context,
                basePropertyParameter);
        if (dbType.equals(EDatabaseTypeName.HSQLDB_IN_PROGRESS.getDisplayName())) {
            dir = getParameterValueWithContext(element, EConnectionParameterName.DBPATH.getName(), context, basePropertyParameter);
        }
        if (connParameters.getSchema() == null || connParameters.getSchema().equals("")) {
            if (EDatabaseTypeName.IBMDB2.getDisplayName().equals(dbType)
                    || EDatabaseTypeName.IBMDB2ZOS.getDisplayName().equals(dbType)) {
                connParameters.setSchema(getParameterValueWithContext(element, EParameterName.SCHEMA_DB_DB2.getDisplayName(),
                        context, basePropertyParameter));
            }
        }
        connParameters.setDirectory(dir);
        connParameters.setHttps(Boolean.parseBoolean(getParameterValueWithContext(element,
                EConnectionParameterName.HTTPS.getName(), context, basePropertyParameter)));
        // for jdbc connection from reposiotry
        final String dbTypeByClassName = ExtractMetaDataUtils.getInstance().getDbTypeByClassName(connParameters.getDriverClass());
        if (connParameters.getDbType() == null || EDatabaseTypeName.MYSQL.getDisplayName().equals(connParameters.getDbType())
                && !EDatabaseTypeName.MYSQL.getProduct().equals(dbTypeByClassName)) {
            if (dbTypeByClassName != null && !"".equals(dbTypeByClassName)) {
                connParameters.setDbType(dbTypeByClassName);
            }
        }

        if (connParameters.getDbType().equals(EDatabaseTypeName.SQLITE.getXmlName())
                || connParameters.getDbType().equals(EDatabaseTypeName.ACCESS.getXmlName())
                || connParameters.getDbType().equals(EDatabaseTypeName.FIREBIRD.getXmlName())) {
            connParameters.setFilename(getParameterValueWithContext(element, EConnectionParameterName.FILE.getName(), context,
                    basePropertyParameter));
        }
        connParameters.setJdbcProperties(getParameterValueWithContext(element,
                EConnectionParameterName.PROPERTIES_STRING.getName(), context, basePropertyParameter));
        connParameters.setDatasource(getParameterValueWithContext(element, EConnectionParameterName.DATASOURCE.getName(),
                context, basePropertyParameter));
        EDatabaseTypeName dbtypeName = EDatabaseTypeName.getTypeFromDbType(dbType);
        if (ManagerConnection.isSchemaFromSidOrDatabase(dbtypeName) 
        		&& (connParameters.getSchema() == null || connParameters.getSchema().length() <= 0)) {
        	connParameters.setSchema(dbName);
        }
    }

    private String getParameterValueWithContext(IElement elem, String key, IContext context,
            IElementParameter basePropertyParameter) {
        if (elem == null || key == null) {
            return ""; //$NON-NLS-1$
        }
        String actualKey = this.getParaNameFromRepositoryName(elem, key, basePropertyParameter);// connKeyMap.get(key);
        if (actualKey != null) {
            return fetchElementParameterValue(elem, context, actualKey);
        } else {
            return fetchElementParameterValue(elem, context, key);
        }
    }

    /**
     * DOC yexiaowei Comment method "fetchElementParameterValude".
     *
     * @param elem
     * @param context
     * @param actualKey
     * @return
     */
    private String fetchElementParameterValue(IElement elem, IContext context, String actualKey) {
        IElementParameter elemParam = elem.getElementParameter(actualKey);
        if (elemParam != null) {
            Object value = elemParam.getValue();

            if (value instanceof String) {
                return ContextParameterUtils.parseScriptContextCode((String) value, context);
            } else if (value instanceof List) {
                // for jdbc parm driver jars
                String jarValues = "";
                List list = (List) value;
                for (int i = 0; i < list.size(); i++) {
                    Object object = list.get(i);
                    if (object instanceof Map) {
                        Map valueMap = (Map) object;
                        if (valueMap.get("JAR_NAME") != null) {
                            if (jarValues.equals("")) {
                                jarValues = jarValues + valueMap.get("JAR_NAME");
                            } else {
                                jarValues = jarValues + ";" + valueMap.get("JAR_NAME");
                            }
                        }else if (valueMap.get("drivers") != null) {
                            if (jarValues.equals("")) {
                                jarValues = jarValues + valueMap.get("drivers");
                            } else {
                                jarValues = jarValues + ";" + valueMap.get("drivers");
                            }
                        }
                    }
                }
                return ContextParameterUtils.parseScriptContextCode(jarValues, context);
            }

        }
        return "";
    }

    INode connectionNode = null;

    /**
     * DOC zli Comment method "getImplicitRepositoryId".
     *
     * @return
     */
    protected String getImplicitRepositoryId() {
        // TDI-17078:when db connection with jdbc work as the implicit context,the elem is Process intance ,it also need
        // get the ImplicitRepositoryId
        if (elem instanceof ImplicitContextLoadElement || elem instanceof Process) {
            IElementParameter implicitContext = elem.getElementParameter("PROPERTY_TYPE_IMPLICIT_CONTEXT");
            if (implicitContext != null) {
                Map<String, IElementParameter> childParameters = implicitContext.getChildParameters();
                if (childParameters != null) {
                    if (childParameters.get("PROPERTY_TYPE").getValue().equals("REPOSITORY")) {
                        IElementParameter iElementParameter = childParameters.get("REPOSITORY_PROPERTY_TYPE");
                        if (iElementParameter != null) {
                            Object value = iElementParameter.getValue();
                            if (value != null) {
                                return value.toString();
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * DOC zli Comment method "getStatsLogRepositoryId".
     *
     * @return
     */
    protected String getStatsLogRepositoryId() {
        if (elem instanceof StatsAndLogsElement || elem instanceof Process) {
            IElementParameter statsLogContext = elem.getElementParameter("PROPERTY_TYPE");
            if (statsLogContext != null) {
                Map<String, IElementParameter> childParameters = statsLogContext.getChildParameters();
                if (childParameters != null) {
                    if (childParameters.get("PROPERTY_TYPE").getValue().equals("REPOSITORY")) {
                        IElementParameter iElementParameter = childParameters.get("REPOSITORY_PROPERTY_TYPE");
                        if (iElementParameter != null) {
                            Object value = iElementParameter.getValue();
                            if (value != null) {
                                return value.toString();
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private void setHiveRelatedParams(IElement element) {
        // hive embedded model, all parameters below should not be null
        String distroKey = getValueFromRepositoryName(elem, "DISTRIBUTION");
        connParameters.getParameters().put(ConnParameterKeys.CONN_PARA_KEY_HIVE_DISTRIBUTION, distroKey);

        String distroVersion = getValueFromRepositoryName(elem, "HIVE_VERSION");
        connParameters.getParameters().put(ConnParameterKeys.CONN_PARA_KEY_HIVE_VERSION, distroVersion);

        String hiveModel = getValueFromRepositoryName(elem, "CONNECTION_MODE");
        connParameters.getParameters().put(ConnParameterKeys.CONN_PARA_KEY_HIVE_MODE, hiveModel);

        String hiveServerVersion = getValueFromRepositoryName(elem, "HIVE_SERVER");
        connParameters.getParameters().put(ConnParameterKeys.HIVE_SERVER_VERSION, hiveServerVersion);

        String nameNodeURI = getValueFromRepositoryName(element, EParameterNameForComponent.PARA_NAME_FS_DEFAULT_NAME.getName());
        connParameters.getParameters().put(ConnParameterKeys.CONN_PARA_KEY_NAME_NODE_URL, nameNodeURI);

        String jobTrackerURI = getValueFromRepositoryName(element, EParameterNameForComponent.PARA_NAME_MAPRED_JT.getName());
        connParameters.getParameters().put(ConnParameterKeys.CONN_PARA_KEY_JOB_TRACKER_URL, jobTrackerURI);

        // for ssl
        String useSSL = getValueFromRepositoryName(elem, "USE_SSL"); //$NON-NLS-1$
        connParameters.getParameters().put(ConnParameterKeys.CONN_PARA_KEY_USE_SSL, useSSL);

        String trustStorePath = TalendQuoteUtils.removeQuotes(getValueFromRepositoryName(elem, "SSL_TRUST_STORE")); //$NON-NLS-1$
        connParameters.getParameters().put(ConnParameterKeys.CONN_PARA_KEY_SSL_TRUST_STORE_PATH, trustStorePath);

        String trustStorePassword = TalendQuoteUtils.removeQuotes(getValueFromRepositoryName(elem, "SSL_TRUST_STORE_PASSWORD")); //$NON-NLS-1$
        connParameters.getParameters().put(ConnParameterKeys.CONN_PARA_KEY_SSL_TRUST_STORE_PASSWORD, trustStorePassword);

        String additionalJDBCSetting = TalendQuoteUtils.removeQuotes(getValueFromRepositoryName(elem, "HIVE_ADDITIONAL_JDBC")); //$NON-NLS-1$
        if (StringUtils.isNotEmpty(additionalJDBCSetting)) {
            connParameters.getParameters().put(ConnParameterKeys.CONN_PARA_KEY_HIVE_ADDITIONAL_JDBC_SETTINGS,
                    additionalJDBCSetting);
        }

        String hiveEnableHa = getValueFromRepositoryName(elem, "ENABLE_HIVE_HA"); //$NON-NLS-1$
        connParameters.getParameters().put(ConnParameterKeys.CONN_PARA_KEY_HIVE_ENABLE_HA, hiveEnableHa);

        String hiveMetastoreUris = TalendQuoteUtils.removeQuotes(getValueFromRepositoryName(elem, "HIVE_METASTORE_URIS")); //$NON-NLS-1$
        connParameters.getParameters().put(ConnParameterKeys.CONN_PARA_KEY_HIVE_METASTORE_URIS, hiveMetastoreUris);

        String hiveThriftPort = TalendQuoteUtils.removeQuotes(getValueFromRepositoryName(elem, "THRIFTPORT")); //$NON-NLS-1$
        connParameters.getParameters().put(ConnParameterKeys.CONN_PARA_KEY_HIVE_THRIFTPORT, hiveThriftPort);

    }

    /**
     * DOC nrousseau Comment method "getGuessQueryCommand".
     *
     * @return
     */
    protected QueryGuessCommand getGuessQueryCommand() {
        // Map<String, IMetadataTable> repositoryTableMap = null;
        IMetadataTable newRepositoryMetadata = null;
        String realTableName = null;
        String realTableId = null;
        String schemaName = "";

        // Only for getting the real table name.
        if (elem.getPropertyValue(EParameterName.SCHEMA_TYPE.getName()).equals(EmfComponent.REPOSITORY)) {

            IElementParameter repositorySchemaTypeParameter = elem
                    .getElementParameter(EParameterName.REPOSITORY_SCHEMA_TYPE.getName());

            if (repositorySchemaTypeParameter != null) {
                final Object value = repositorySchemaTypeParameter.getValue();
                if (elem instanceof Node) {
                    /* value can be devided means the value like "connectionid - label" */
                    String[] keySplitValues = value.toString().split(" - ");
                    if (keySplitValues.length > 1) {

                        String connectionId = value.toString().split(" - ")[0];
                        String tableLabel = value.toString().split(" - ")[1];
                        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                        Item item = null;
                        try {
                            IRepositoryViewObject repobj = factory.getLastVersion(connectionId);
                            if (repobj != null) {
                                Property property = repobj.getProperty();
                                if (property != null) {
                                    item = property.getItem();
                                }
                            }
                        } catch (PersistenceException e) {
                            ExceptionHandler.process(e);
                        }
                        if (item != null && item instanceof ConnectionItem) {
                            Connection connection = ((ConnectionItem) item).getConnection();
                            for (org.talend.core.model.metadata.builder.connection.MetadataTable table : ConnectionHelper
                                    .getTables(connection)) {
                                // bug 20365
                                if (table.getLabel().equals(tableLabel)) {
                                    IMetadataTable repositoryMetadata = ConvertionHelper.convert(table);
                                    realTableName = repositoryMetadata.getTableName();
                                    realTableId = repositoryMetadata.getId();
                                    // if (table.eContainer() != null && table.eContainer() instanceof SchemaImpl) {
                                    // SchemaImpl schemaImpl = (SchemaImpl) table.eContainer();
                                    // schemaName = schemaImpl.getName();
                                    // dynamicProperty.getTableIdAndDbSchemaMap().put(realTableId, schemaName);
                                    // }
                                    break;
                                }
                            }
                        }
                    }
                    // if (repositoryTableMap.containsKey(value)) {
                    // IMetadataTable repositoryMetadata = repositoryTableMap.get(value);
                    // realTableName = repositoryMetadata.getTableName();
                    // realTableId = repositoryMetadata.getId();
                    // }
                }
            }
            // }
            // }
        } // Ends

        Connection repositoryConnection = null;
        boolean useExisting = false;
        IElementParameter elementParameter = elem.getElementParameter(EParameterName.USE_EXISTING_CONNECTION.name());
        if (elem instanceof Node) {
            IProcess process = ((Node) elem).getProcess();
            if (elementParameter != null && Boolean.valueOf(String.valueOf(elementParameter.getValue()))) {
                String connName = (String) elem.getPropertyValue("CONNECTION");
                for (INode node : process.getGraphicalNodes()) {
                    if (node.getElementName().equals(connName)) {
                        useExisting = true;
                        final Object propertyValue = node.getPropertyValue(EParameterName.REPOSITORY_PROPERTY_TYPE.getName());
                        if (propertyValue != null) {
                            IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                            Item item = null;
                            try {
                                IRepositoryViewObject repobj = factory.getLastVersion(propertyValue.toString());
                                if (repobj != null) {
                                    Property property = repobj.getProperty();
                                    if (property != null) {
                                        item = property.getItem();
                                    }
                                }
                            } catch (PersistenceException e) {
                                ExceptionHandler.process(e);
                            }
                            if (item != null && item instanceof ConnectionItem) {
                                repositoryConnection = ((ConnectionItem) item).getConnection();
                            } else {
                                initConnectionParameters();
                                repositoryConnection = TracesConnectionUtils.createConnection(connParameters);

                            }
                        }
                        break;
                    }
                }
            }
        }
        if (!useExisting && elem.getPropertyValue(EParameterName.PROPERTY_TYPE.getName()).equals(EmfComponent.REPOSITORY)) {
            final Object propertyValue = elem.getPropertyValue(EParameterName.REPOSITORY_PROPERTY_TYPE.getName());
            if (propertyValue != null) {
                IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                Item item = null;
                try {
                    IRepositoryViewObject repobj = factory.getLastVersion(propertyValue.toString());
                    if (repobj != null) {
                        Property property = repobj.getProperty();
                        if (property != null) {
                            item = property.getItem();
                        }
                    }
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                }
                if (item != null && item instanceof ConnectionItem) {
                    repositoryConnection = ((ConnectionItem) item).getConnection();
                }
            }
        } else {
            initConnectionParameters();
            repositoryConnection = TracesConnectionUtils.createConnection(connParameters);
        }

        QueryGuessCommand cmd = null;
        INode node = null;
        if (elem instanceof INode) {
            node = (INode) elem;
        } else { // else instanceof Connection
            node = ((IConnection) elem).getSource();
        }

        List<IMetadataTable> metadataList = node.getMetadataList();
        newRepositoryMetadata = metadataList.get(0);
        // for tInformixRow
        if (newRepositoryMetadata.getListColumns().size() == 0 && metadataList.size() > 1) {
            newRepositoryMetadata = metadataList.get(1);
        }

        if (newRepositoryMetadata == null) {
            String schemaSelected = (String) node.getPropertyValue(EParameterName.REPOSITORY_SCHEMA_TYPE.getName());
            if (schemaSelected != null) {
                // repositoryMetadata = repositoryTableMap.get(schemaSelected);
            } else if (newRepositoryMetadata == null) {
                MessageDialog.openWarning(DisplayUtils.getDefaultShell(false), Messages.getString("QueryTypeController.alert"), //$NON-NLS-1$
                        Messages.getString("QueryTypeController.nothingToGuess")); //$NON-NLS-1$
                return cmd;
            }
        }
        cmd = new QueryGuessCommand(node, newRepositoryMetadata, repositoryConnection);

        cmd.setMaps(dynamicProperty.getTableIdAndDbTypeMap(), dynamicProperty.getTableIdAndDbSchemaMap(), null);
        String type = getValueFromRepositoryName("TYPE"); //$NON-NLS-1$
        if ("Oracle".equalsIgnoreCase(type)) {
            type = EDatabaseTypeName.ORACLEFORSID.getDisplayName();
        }
        cmd.setParameters(realTableId, realTableName, type);
        return cmd;
    }

    protected void initConnectionParameters() {

        connParameters = null;

        IElementParameter basePropertyParameter = null;
        for (IElementParameter param : elem.getElementParameters()) {
            if (param.getFieldType() == EParameterFieldType.PROPERTY_TYPE) {
                if (param.getRepositoryValue().startsWith("DATABASE")) {
                    basePropertyParameter = param;
                    break;
                }
            }
        }
        // jobsetting view load the db info from current selected category
        IElementParameter updateBasePropertyParameter = updateBasePropertyParameter();
        if (updateBasePropertyParameter != null && !updateBasePropertyParameter.equals(basePropertyParameter)) {
            basePropertyParameter = updateBasePropertyParameter;
        }
        connParameters = new ConnectionParameters();
        String type = getValueFromRepositoryName(elem, "TYPE", basePropertyParameter); //$NON-NLS-1$
        Object isUseExistingConnection = elem.getPropertyValue("USE_EXISTING_CONNECTION"); //$NON-NLS-1$
        boolean isUserExistionConnectionType = false;
        if (type.equals("Oracle") || type.contains("OCLE")) {
            IElementParameter ele = elem.getElementParameter("CONNECTION_TYPE");
            if (ele != null) {
                type = (String) ele.getValue();
            } else {
                type = "ORACLE_SID"; //$NON-NLS-1$
            }
            if ((isUseExistingConnection instanceof Boolean) && ((Boolean) isUseExistingConnection)) {
                isUserExistionConnectionType = true;
            }
        } else if (EDatabaseTypeName.HIVE.getProduct().equalsIgnoreCase(type)) {
            // if (EDatabaseVersion4Drivers.HIVE_EMBEDDED.getVersionValue().equals(
            // elem.getElementParameter("CONNECTION_MODE").getValue())) {
            setHiveRelatedParams(elem);
            // }
        } else if (EDatabaseTypeName.IMPALA.getProduct().equalsIgnoreCase(type)) {
            String distroKey = getValueFromRepositoryName(elem, "DISTRIBUTION");
            connParameters.getParameters().put(ConnParameterKeys.CONN_PARA_KEY_IMPALA_DISTRIBUTION, distroKey);

            String distroVersion = getValueFromRepositoryName(elem, "IMPALA_VERSION");
            connParameters.getParameters().put(ConnParameterKeys.CONN_PARA_KEY_IMPALA_VERSION, distroVersion);
        }
        // Get real hsqldb type
        if (type.equals(EDatabaseTypeName.HSQLDB.name())
                && getValueFromRepositoryName(elem, "RUNNING_MODE").equals("HSQLDB_INPROGRESS_PERSISTENT")) {//$NON-NLS-1$
            type = EDatabaseTypeName.HSQLDB_IN_PROGRESS.getDisplayName();
        }
        connParameters.setDbType(type);

        String driverName = getValueFromRepositoryName(elem, "DB_VERSION", basePropertyParameter); //$NON-NLS-1$
        if (StringUtils.isBlank(driverName) && EDatabaseTypeName.MSSQL.getDisplayName().equals(connParameters.getDbType())) {
            driverName = getValueFromRepositoryName(elem, "DRIVER", basePropertyParameter); //$NON-NLS-1$
        }
        String dbVersionName = EDatabaseVersion4Drivers.getDbVersionName(type, driverName);
        if (EDatabaseTypeName.HIVE.getProduct().equalsIgnoreCase(type)) {
            IElementParameter connectionMode = elem.getElementParameter("CONNECTION_MODE");
            if (connectionMode != null
                    && EDatabaseVersion4Drivers.HIVE_EMBEDDED.getVersionValue().equals(connectionMode.getValue())) {
                connParameters.setDbVersion(EDatabaseVersion4Drivers.HIVE_EMBEDDED.getVersionValue());
            } else {
                connParameters.setDbVersion(EDatabaseVersion4Drivers.HIVE.getVersionValue());
            }
        } else {
            connParameters.setDbVersion(dbVersionName);
        }

        connParameters.setNode(elem);
        String selectedComponentName = (String) elem.getPropertyValue(EParameterName.UNIQUE_NAME.getName());
        connParameters.setSelectedComponentName(selectedComponentName);
        connParameters.setFieldType(paramFieldType);
        if (elem instanceof Node && !((Node) elem).getMetadataList().isEmpty()) {
            connParameters.setMetadataTable(((Node) elem).getMetadataList().get(0));
        }

        connParameters.setSchemaRepository(EmfComponent.REPOSITORY.equals(elem.getPropertyValue(EParameterName.SCHEMA_TYPE
                .getName())));
        connParameters.setFromDBNode(true);

        connParameters.setQuery(""); //$NON-NLS-1$

        List<? extends IElementParameter> list = elem.getElementParameters();
        boolean end = false;
        for (int i = 0; i < list.size() && !end; i++) {
            IElementParameter param = list.get(i);
            if (param.getFieldType() == EParameterFieldType.MEMO_SQL) {
                connParameters.setNodeReadOnly(param.isReadOnly());
                end = true;
            }

        }

        IElementParameter compList = elem.getElementParameterFromField(EParameterFieldType.COMPONENT_LIST);
        if (isUseExistingConnection != null && (isUseExistingConnection instanceof Boolean) && ((Boolean) isUseExistingConnection) && compList != null) {
            Object compValue = compList.getValue();

            if (compValue != null && !compValue.equals("")) { //$NON-NLS-1$
                List<? extends INode> nodes = part.getProcess().getGraphicalNodes();
                for (INode node : nodes) {
                    if (node.getUniqueName().equals(compValue) && (node instanceof Node)) {
                        connectionNode = node;
                        break;
                    }
                }
                if (connectionNode == null) {
                    nodes = part.getProcess().getGeneratingNodes();
                    for (INode node : nodes) {
                        if (node.getUniqueName().equals(compValue) && (node instanceof INode)) {
                            connectionNode = node;
                            break;
                        }
                    }
                }
                if (connectionNode == null) {
                    INode node = null;
                    if (elem instanceof INode) {
                        node = (INode) elem;
                    } else { // else instanceof Connection
                        node = ((IConnection) elem).getSource();
                    }
                    if (node != null) {
                        List<IMultipleComponentManager> multipleComponentManagers = node.getComponent()
                                .getMultipleComponentManagers();
                        for (IMultipleComponentManager manager : multipleComponentManagers) {
                            String inName = manager.getInput().getName();
                            String componentValue = compValue + "_" + inName;
                            for (INode gnode : nodes) {
                                if (gnode.getUniqueName().equals(componentValue) && (gnode instanceof INode)) {
                                    connectionNode = gnode;
                                    break;
                                }
                            }
                        }
                    }
                }
                if (connectionNode != null) {
                    if (isUserExistionConnectionType) {
                        IElementParameter ele = connectionNode.getElementParameter("CONNECTION_TYPE");
                        if (ele != null) {
                            type = (String) ele.getValue();
                            if ("ORACLE_RAC".equals(ele.getValue())) {
                                type = "ORACLE_CUSTOM";
                            }
                        }
                    }
                    setAllConnectionParameters(type, connectionNode);
                }
            }
        } else {
            setAllConnectionParameters(null, elem);
        }

        if (connectionNode != null) {
            setConnectionParameterNames(connectionNode, connParameters, basePropertyParameter);
        } else {
            setConnectionParameterNames(elem, connParameters, basePropertyParameter);
        }
    }

    protected boolean checkExistConnections(IMetadataConnection metadataConnection) {
        java.sql.Connection connection = null;
        try {
            List list = new ArrayList();
            list = ExtractMetaDataUtils.getInstance().connect(metadataConnection.getDbType(), metadataConnection.getUrl(),
                    metadataConnection.getUsername(), metadataConnection.getPassword(), metadataConnection.getDriverClass(),
                    metadataConnection.getDriverJarPath(), metadataConnection.getDbVersionString(),
                    metadataConnection.getAdditionalParams());
            if (list != null && list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i) instanceof Connection) {
                        connection = (java.sql.Connection) list.get(i);
                    }
                }
            }
        } catch (SQLException e) {
            return false;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                //
            }
        }
        return true;
    }

    protected boolean isConnectionExist() {

        ISQLBuilderService service = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ISQLBuilderService.class)) {
            service = GlobalServiceRegister.getDefault().getService(ISQLBuilderService.class);
        }
        if (service == null) {
            return false;
        }

        if (contextManager != null && contextManager.getDefaultContext().getContextParameterList().size() != 0) {
            initConnectionParametersWithContext(elem, contextManager.getDefaultContext());
        }
        DatabaseConnection connection = service.createConnection(connParameters);
        if (connection != null) {
            IMetadataConnection metadataConnection = null;
            metadataConnection = ConvertionHelper.convert(connection);
            return checkExistConnections(metadataConnection);
        }
        return false;
    }

    private void setConnectionParameterNames(IElement element, ConnectionParameters connParameters,
            IElementParameter basePropertyParameter) {

        addConnectionParameter(element, connParameters, EConnectionParameterName.SCHEMA.getName(), basePropertyParameter);

        addConnectionParameter(element, connParameters, EConnectionParameterName.USERNAME.getName(), basePropertyParameter);

        addConnectionParameter(element, connParameters, EConnectionParameterName.PASSWORD.getName(), basePropertyParameter);

        addConnectionParameter(element, connParameters, EConnectionParameterName.SERVER_NAME.getName(), basePropertyParameter);

        addConnectionParameter(element, connParameters, EConnectionParameterName.PORT.getName(), basePropertyParameter);

        addConnectionParameter(element, connParameters, EConnectionParameterName.DATASOURCE.getName(), basePropertyParameter);

        addConnectionParameter(element, connParameters, EConnectionParameterName.SID.getName(), basePropertyParameter);

        addConnectionParameter(element, connParameters, EConnectionParameterName.FILE.getName(), basePropertyParameter);

        addConnectionParameter(element, connParameters, EConnectionParameterName.DIRECTORY.getName(), basePropertyParameter);

        addConnectionParameter(element, connParameters, EConnectionParameterName.URL.getName(), basePropertyParameter);

        addConnectionParameter(element, connParameters, EConnectionParameterName.DRIVER_CLASS.getName(), basePropertyParameter);

        addConnectionParameter(element, connParameters, EConnectionParameterName.DRIVER_JAR.getName(), basePropertyParameter);

        addConnectionParameter(element, connParameters, EConnectionParameterName.PROPERTIES_STRING.getName(),
                basePropertyParameter);

    }

    private void addConnectionParameter(IElement element, ConnectionParameters connParameters, String repositoryName,
            IElementParameter basePropertyParameter) {
        final String paraNameFromRepositoryName = getParaNameFromRepositoryName(element, repositoryName, basePropertyParameter);
        if (paraNameFromRepositoryName != null) {
            connParameters.getRepositoryNameParaName().put(repositoryName, paraNameFromRepositoryName);
        }
    }

    /**
     *
     * DOC ggu Comment method "isExtra".
     *
     * for extra db setting.
     */
    private boolean sameExtraParameter(IElementParameter param) {
        // for job settings extra.(feature 2710)
        if (curParameter != null) {
            boolean extra = JobSettingsConstants.isExtraParameter(this.curParameter.getName());
            boolean paramFlag = JobSettingsConstants.isExtraParameter(param.getName());
            return extra == paramFlag;
        }
        return true;
    }

    protected boolean isUseExistingConnection() {
        IElementParameter elementParameter = elem.getElementParameter(EParameterName.USE_EXISTING_CONNECTION.getName());
        if (elementParameter != null) {
            Boolean value = (Boolean) elementParameter.getValue();
            return value;
        }
        return false;
    }

    protected boolean isUseAlternateSchema() {
        IElementParameter elementParameter = elem.getElementParameter("USE_ALTERNATE_SCHEMA");
        if (elementParameter != null) {
            Boolean value = (Boolean) elementParameter.getValue();
            return value;
        }
        return false;
    }

    /**
     * DOC qzhang Comment method "openSQLBuilder".
     *
     * @param repositoryType
     * @param propertyName
     * @param query
     */
    protected String openSQLBuilder(String repositoryType, String propertyName, String query) {
        if (repositoryType.equals(EmfComponent.BUILTIN)) {
            connParameters.setQuery(query, true);
            if (connParameters.isShowConfigParamDialog()) {
                if (!isUseExistingConnection()) {
                    initConnectionParametersWithContext(elem, part.getProcess().getContextManager().getDefaultContext());
                } else {
                    initConnectionParametersWithContext(connectionNode, part.getProcess().getContextManager().getDefaultContext());
                }
            }
            // add for bug TDI-20335
            if (part == null) {
                Shell parentShell = DisplayUtils.getDefaultShell(false);
                ISQLBuilderService service = GlobalServiceRegister.getDefault().getService(
                        ISQLBuilderService.class);
                Dialog sqlBuilder = service.openSQLBuilderDialog(parentShell, "", connParameters);
                sqlBuilder.open();
            } else {
                openSqlBuilderBuildIn(connParameters, propertyName);
            }

        } else if (repositoryType.equals(EmfComponent.REPOSITORY)) {
            String repositoryName2 = ""; //$NON-NLS-1$
            String repositoryId = null;
            IElementParameter memoParam = elem.getElementParameter(propertyName);
            IElementParameter repositoryParam = null;
            for (IElementParameter param : elem.getElementParameters()) {
                if (param.getFieldType() == EParameterFieldType.PROPERTY_TYPE
                        && param.getRepositoryValue().startsWith("DATABASE")) {
                    if (memoParam != null && param.getCategory().equals(memoParam.getCategory())) {
                        repositoryParam = param;
                        break;
                    }

                }
            }
            // in case no database property found, take the first property (to keep compatibility with old code)
            if (repositoryParam == null) {
                for (IElementParameter param : elem.getElementParameters()) {
                    if (param.getFieldType() == EParameterFieldType.PROPERTY_TYPE) {
                        repositoryParam = param;
                        break;
                    }
                }
            }

            if (repositoryParam != null) {
                IElementParameter itemFromRepository = repositoryParam.getChildParameters().get(
                        EParameterName.REPOSITORY_PROPERTY_TYPE.getName());
                String value = (String) itemFromRepository.getValue();
                repositoryId = value;
                // for (String key : this.dynamicProperty.getRepositoryConnectionItemMap().keySet()) {
                // if (key.equals(value)) {
                // repositoryName2 =
                // this.dynamicProperty.getRepositoryConnectionItemMap().get(key).getProperty().getLabel();
                // }
                // }
                /* get connection item dynamictly,not from cache ,see 16969 */
                IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                try {
                    IRepositoryViewObject repobj = factory.getLastVersion(value);
                    if (repobj != null) {
                        Property property = repobj.getProperty();
                        if (property != null) {
                            repositoryName2 = property.getLabel();
                        }
                    }
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                }
            }
            // When no repository avaiable on "Repository" mode, open a MessageDialog.
            if (repositoryName2 == null || repositoryName2.length() == 0) {
                MessageDialog.openError(composite.getShell(), Messages.getString("NoRepositoryDialog.Title"), Messages //$NON-NLS-1$
                        .getString("NoRepositoryDialog.Text")); //$NON-NLS-1$
                return null;
            }

            // Part maybe not exist
            String processName = "";//$NON-NLS-1$
            String key = "";//$NON-NLS-1$

            if (elem instanceof Node) {
                processName = ((Node) elem).getProcess().getName();
                key = processName + ((Node) elem).getUniqueName();
            } else if (elem instanceof IProcess) {
                processName = ((IProcess) elem).getName();
                key = processName;
            }
            key += repositoryName2;

            final Dialog builderDialog = sqlbuilers.get(key);
            if (!composite.isDisposed() && builderDialog != null && builderDialog.getShell() != null
                    && !builderDialog.getShell().isDisposed()) {
                builderDialog.getShell().setActive();
            } else {
                connParameters.setRepositoryName(repositoryName2);
                if (repositoryId != null) {
                    connParameters.setRepositoryId(repositoryId);
                }
                Shell parentShell = DisplayUtils.getDefaultShell(false);
                String nodeLabel = null;
                if (elem instanceof Node) {
                    nodeLabel = (String) ((Node) elem).getElementParameter(EParameterName.LABEL.getName()).getValue();
                }
                TextUtil.setDialogTitle(processName, nodeLabel, elem.getElementName());

                ISQLBuilderService service = GlobalServiceRegister.getDefault().getService(
                        ISQLBuilderService.class);

                connParameters.setQuery(query);
                connParameters.setFirstOpenSqlBuilder(true); // first open Sql Builder,set true

                Dialog sqlBuilder = service.openSQLBuilderDialog(parentShell, processName, connParameters);

                sqlbuilers.put(key, sqlBuilder);
                if (Window.OK == sqlBuilder.open()) {
                    if (!composite.isDisposed() && !connParameters.isNodeReadOnly()) {
                        String sql = connParameters.getQuery();
                        // modified by hyWang
                        if (!connParameters.getIfContextButtonCheckedFromBuiltIn()) {
                            sql = QueryUtil.checkAndAddQuotes(sql);
                        }
                        return sql;
                    }
                }

            }
        }
        return null;
    }

    public void dispose() {
        if (widgetFactory != null) {
            widgetFactory.dispose();
        }
        widgetFactory = null;
        sqlbuilers.clear();
        sqlbuilers = null;
        dynamicProperty = null;
        composite = null;
        hashCurControls = null;
        elem = null;
        part = null;
        section = null;
        editionControlHelper = null;
        curParameter = null;
    }

    public void addRepositoryPropertyListener(Control control) {
        boolean flag = false;

        if (this.curParameter != null) {
            final EComponentCategory category = this.curParameter.getCategory();
            final IElement element = this.curParameter.getElement();
            if (element instanceof FakeElement
                    || AbstractPreferenceComposite.inUseProjectSettingMode(element, category,
                            EParameterName.STATANDLOG_USE_PROJECT_SETTINGS)
                    || AbstractPreferenceComposite.inUseProjectSettingMode(element, category,
                            EParameterName.IMPLICITCONTEXT_USE_PROJECT_SETTINGS)) {
                flag = true; // don't add the listener.
            }
        }
        if (!flag) {
            control.addMouseListener(listenerSelection);
        }
    }

    MouseListener listenerSelection = new MouseAdapter() {

        @Override
        public void mouseDown(MouseEvent e) {

            ModelSelectionDialog modelSelect = new ModelSelectionDialog(((Control) e.getSource()).getShell(),
                    ESelectionType.NORMAL);

            if (modelSelect.open() == ModelSelectionDialog.OK) {
                if (modelSelect.getOptionValue() == EEditSelection.BUILDIN) {

                    // getCommandStack().execute(changeToBuildInCommand((Control) e.getSource()));
                    executeCommand(changeToBuildInCommand((Control) e.getSource()));
                }
                if (modelSelect.getOptionValue() == EEditSelection.REPOSITORY) {

                    // getCommandStack().execute(refreshConnectionCommand((Control) e.getSource()));
                    executeCommand(refreshConnectionCommand((Control) e.getSource()));
                }
            }
        }
    };

    private Command changeToBuildInCommand(Control control) {
        final String typeName = ":" + EParameterName.PROPERTY_TYPE.getName(); //$NON-NLS-1$

        if (curParameter != null) {
            String parentName = null;
            if (curParameter.getCategory() == EComponentCategory.EXTRA) {
                parentName = JobSettingsConstants.getExtraParameterName(EParameterName.PROPERTY_TYPE.getName());
            } else if (curParameter.getCategory() == EComponentCategory.STATSANDLOGS) {
                parentName = EParameterName.PROPERTY_TYPE.getName();
            }
            if (parentName != null) {
                return new ChangeValuesFromRepository(elem, null, parentName + typeName, EmfComponent.BUILTIN);
            }
        }
        Object objProperty = control.getData(PARAMETER_NAME);
        String property = null;
        if (objProperty != null && elem != null) {
            String curSubParam = objProperty.toString().trim();
            if (!curSubParam.isEmpty()) {
                IElementParameter iElementParam = elem.getElementParameter(curSubParam);
                if (iElementParam != null) {
                    property = iElementParam.getRepositoryProperty();
                }
            }
        }
        if (property == null || property.trim().isEmpty()) {
            property = UpgradeParameterHelper.PROPERTY;
        }
        return new ChangeValuesFromRepository(elem, null, property + typeName, EmfComponent.BUILTIN);
    }

    private Command refreshConnectionCommand(Control control) {
        if (control.getData(PARAMETER_NAME) != null && control.getData(PARAMETER_NAME) instanceof String) {
            String paramName = (String) control.getData(PARAMETER_NAME);

            IElementParameter param = elem.getElementParameter(paramName);
            String propertyParamName = null;
            if (param.getRepositoryProperty() != null) {
                propertyParamName = param.getRepositoryProperty();
            } else {
                propertyParamName = elem.getElementParameterFromField(EParameterFieldType.PROPERTY_TYPE).getName();
            }
            final IElementParameter propertyParam = elem.getElementParameter(propertyParamName);

            if (propertyParam != null) {
                final IElementParameter repositoryParam = propertyParam.getChildParameters().get(
                        EParameterName.REPOSITORY_PROPERTY_TYPE.getName());
                if (repositoryParam != null) {
                    try {
                        IRepositoryViewObject o = RepositoryPlugin.getDefault().getRepositoryService()
                                .getProxyRepositoryFactory().getLastVersion((String) repositoryParam.getValue());
                        // for bug 14535
                        if (o != null && elem instanceof INode) {
                            INode node = (INode) elem;
                            IMetadataService metadataService = CorePlugin.getDefault().getMetadataService();
                            if (metadataService != null) {
                                metadataService.openMetadataConnection(o, node);
                            }
                            // TDI-21143 : Studio repository view : remove all refresh call to repo view
                            // IRepositoryView view = RepositoryManagerHelper.findRepositoryView();
                            // if (view != null) {
                            // view.refresh();
                            // }
                        }
                    } catch (Exception e) {
                        ExceptionHandler.process(e);
                    }
                }
            }
        }
        return null;
    }

    protected IProcess getProcess(final IElement elem, final IMultiPageTalendEditor part) {
        IProcess process = null;
        if (part == null) {
            // achen modify to fix 0005991 part is null
            if (elem instanceof INode) {
                process = ((INode) elem).getProcess();
            }
        } else {
            process = part.getProcess();
        }
        return process;
    }

    /**
     *
     * cli Comment method "addResourceDisposeListener".
     *
     * When dispose the control, dispose resource at the same time. (bug 6916)
     */
    protected void addResourceDisposeListener(final Control parent, final Resource res) {
        if (parent != null) {
            parent.addDisposeListener(new DisposeListener() {

                @Override
                public void widgetDisposed(DisposeEvent e) {
                    if (res != null && !res.isDisposed()) {
                        res.dispose();
                    }
                    parent.removeDisposeListener(this);
                }
            });
        }

    }

    public List<Problem> getCodeProblems() {
        return this.codeProblems;
    }

    public void updateCodeProblems(List<Problem> codeProblems) {
        if (codeProblems != null) {
            this.codeProblems = new ArrayList<Problem>(codeProblems);
        }
    }

    public void setCodeProblems(List<Problem> codeProblems) {
        this.codeProblems = codeProblems;
    }

    private IElementParameter updateBasePropertyParameter() {
        if (EComponentCategory.EXTRA.equals(section)) {
            return elem.getElementParameter("PROPERTY_TYPE_IMPLICIT_CONTEXT"); //$NON-NLS-1$
        }
        if (EComponentCategory.STATSANDLOGS.equals(section)) {
            return elem.getElementParameter("PROPERTY_TYPE"); //$NON-NLS-1$
        }
        return null;
    }

    protected void callBeforeActive(IElementParameter param) {
        IGenericService service = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IGenericService.class)) {
            service = GlobalServiceRegister.getDefault().getService(IGenericService.class);
        }
        if (service != null) {
            service.callBeforeActivate(param);
        }
    }

    protected boolean canAddRepositoryDecoration(IElementParameter param) {
        return !(elem instanceof FakeElement) && param.isRepositoryValueUsed();
    }

}
