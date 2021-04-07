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
package org.talend.designer.core.ui.views.properties;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CommandStackEvent;
import org.eclipse.gef.commands.CommandStackEventListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.swt.dialogs.EventLoopProgressMonitor;
import org.talend.commons.ui.swt.dialogs.ProgressDialog;
import org.talend.commons.utils.threading.ExecutionLimiter;
import org.talend.core.context.RepositoryContext;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.ICodeProblemsChecker;
import org.talend.core.model.general.Project;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.Problem;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.FileItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.LinkRulesItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.metadata.RepositoryObjectHelper;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.cmd.ChangeMetadataCommand;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController;
import org.talend.designer.core.ui.editor.properties.controllers.GroupController;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainer;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * yzhang class global comment. Detailled comment <br/>
 *
 * $Id: DynamicTabbedPropertySection.java 6579 2007-10-26 10:33:01Z ftang $
 *
 */
public class MultipleThreadDynamicComposite extends ScrolledComposite implements IDynamicProperty {

    protected AbstractMultiPageTalendEditor part;

    protected Element elem;

    protected Composite parent;

    protected BidiMap hashCurControls;

    protected String currentComponent;

    protected EComponentCategory section;

    protected int curRowSize;

    protected DynamicPropertyGenerator generator;

    private Map<String, FileItem> repositoryFileItemMap = new HashMap<String, FileItem>(); // hywang add for 6484

    private Map<String, LinkRulesItem> repositoryLinkRulesItemMap = new HashMap<String, LinkRulesItem>(); // hywang add

    // for 6484

    /* cache should be deleted ,bug 16969 */
    // private final Map<String, Query> repositoryQueryStoreMap;

    // private final Map<String, IMetadataTable> repositoryTableMap;
    private Map<String, String> tableIdAndDbTypeMap = new HashMap<String, String>();

    private Map<String, String> tableIdAndDbSchemaMap = new HashMap<String, String>();

    private boolean forceRedraw;

    private int lastCompositeSize = 0;

    private Process process;

    private boolean propertyResized;

    protected int minHeight;

    protected Composite composite;

    private final String updataComponentParamName;

    private boolean isCompactView;

    WidgetFactory widgetFactory = new WidgetFactory();

    @Override
    public String getRepositoryAliasName(ConnectionItem connectionItem) {
        String aliasName = RepositoryObjectHelper.getRepositoryAliasName(connectionItem);
        return aliasName != null ? aliasName : ""; //$NON-NLS-1$
    }

    /**
     *
     * cli Comment method "getAllRepositoryMetadata".
     *
     * fixed for 8971
     */
    private List<IRepositoryViewObject> getAllRepositoryMetadata() {
        IProxyRepositoryFactory factory = DesignerPlugin.getDefault().getProxyRepositoryFactory();
        ProjectManager pManager = ProjectManager.getInstance();
        pManager.getReferencedProjects(); // retrieve reference project.

        List<IRepositoryViewObject> repositoryObjects;
        try {
            repositoryObjects = retrieveMetadataFromProject(factory, pManager, pManager.getCurrentProject());
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        }
        return repositoryObjects;
    }

    /**
     *
     * cli Comment method "getAllRepositoryMetadata".
     *
     * fixed for 8971
     */
    private List<IRepositoryViewObject> retrieveMetadataFromProject(IProxyRepositoryFactory factory, ProjectManager pManager,
            Project parentProject) throws PersistenceException {
        List<IRepositoryViewObject> repositoryObjects = factory.getAll(parentProject, ERepositoryObjectType.METADATA);
        if (repositoryObjects == null) {
            repositoryObjects = new ArrayList<IRepositoryViewObject>();
        }
        List<Project> referencedProjects = pManager.getReferencedProjects(parentProject);
        if (referencedProjects != null) {
            for (Project p : referencedProjects) {
                List<IRepositoryViewObject> refRepObjects = retrieveMetadataFromProject(factory, pManager, p);
                if (refRepObjects != null) {
                    repositoryObjects.addAll(refRepObjects);
                }
            }
        }
        return repositoryObjects;
    }

    public void updateRepositoryList() {

        ProgressDialog progressDialog = new ProgressDialog(this.getShell(), 1000) {

            private IProgressMonitor monitorWrap;

            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                monitorWrap = new EventLoopProgressMonitor(monitor);
                IProxyRepositoryFactory factory = DesignerPlugin.getDefault().getProxyRepositoryFactory();
                List<IRepositoryViewObject> repositoryObjects = getAllRepositoryMetadata();

                int total = repositoryObjects.size(); // + elem.getElementParameters().size();
                monitorWrap.beginTask(Messages.getString("MultipleThreadDynamicComposite.gatherInformation"), total); //$NON-NLS-1$

                IElementParameter propertyParam = elem.getElementParameterFromField(EParameterFieldType.PROPERTY_TYPE, section);
                String repositoryValue = null;

                if (propertyParam != null) {
                    repositoryValue = propertyParam.getRepositoryValue();
                }

                if (repositoryObjects != null && (repositoryObjects.size() != 0)) {
                    tableIdAndDbTypeMap.clear();
                    tableIdAndDbSchemaMap.clear();

                    for (IRepositoryViewObject curObject : repositoryObjects) {

                        Item item = curObject.getProperty().getItem();
                        if (item instanceof ConnectionItem) {
                            ConnectionItem connectionItem = (ConnectionItem) item;
                            Connection connection = connectionItem.getConnection();
                            if (connection == null || connection.isReadOnly()) {
                                continue;
                            }
                            for (Object tableObj : ConnectionHelper.getTables(connection)) {
                                org.talend.core.model.metadata.builder.connection.MetadataTable table;

                                table = (org.talend.core.model.metadata.builder.connection.MetadataTable) tableObj;

                                if (factory.getStatus(connectionItem) != ERepositoryStatus.DELETED) {
                                    if (!factory.isDeleted(table)) {
                                        IMetadataTable newTable = ConvertionHelper.convert(table);
                                        //                                        repositoryTableMap.put(connectionItem.getProperty().getId() + " - " + table.getLabel(), //$NON-NLS-1$
                                        // newTable);
                                        if (connection instanceof DatabaseConnection) {
                                            String dbType = ((DatabaseConnection) connection).getDatabaseType();
                                            String schema = ((DatabaseConnection) connection).getUiSchema();
                                            tableIdAndDbTypeMap.put(newTable.getId(), dbType);
                                            if (schema != null && !schema.equals("")) { //$NON-NLS-1$
                                                tableIdAndDbSchemaMap.put(newTable.getId(), schema);
                                            }
                                        }
                                    }
                                }
                            }

                            monitorWrap.worked(1);
                        }

                        // hywang add for feature 6484
                        if (item instanceof FileItem) {
                            FileItem FileItem = (FileItem) item;
                            if (repositoryValue != null) {
                                if (repositoryValue.equals("RULE")) { //$NON-NLS-1$
                                    repositoryFileItemMap.put(FileItem.getProperty().getId(), FileItem);
                                }
                            }
                            monitorWrap.worked(1);
                        }

                        if (item instanceof LinkRulesItem) {
                            LinkRulesItem linkItem = (LinkRulesItem) item;
                            if (repositoryValue != null) {
                                if (repositoryValue.equals("RULE")) { //$NON-NLS-1$
                                    repositoryLinkRulesItemMap.put(linkItem.getProperty().getId(), linkItem);
                                }
                            }
                            monitorWrap.worked(1);
                        }

                    }
                }

                monitorWrap.done();
            }
        };

        try {
            progressDialog.executeProcess();
        } catch (InvocationTargetException e) {
            ExceptionHandler.process(e);
            return;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return;
        }
    }

    /**
     * ftang Comment method "getElement".
     *
     * @return an instance of Element
     */
    @Override
    public Element getElement() {
        return elem;
    }

    /**
     *
     */
    private boolean checkErrorsWhenViewRefreshed;

    public void addComponents(boolean forceRedraw) {
        addComponents(forceRedraw, true, 0);
    }

    /**
     * yzhang Comment method "addcomponents".
     *
     * @param forceRedraw
     * @param reInitialize
     */
    public void addComponents(boolean forceRedraw, boolean reInitialize) {
        addComponents(forceRedraw, reInitialize, 0);
    }

    protected void disposeChildren() {
        if (composite != null && !composite.isDisposed()) {
            // Empty the composite before use (kinda refresh) :
            Control[] ct = composite.getChildren();
            for (Control element : ct) {
                if (element != null) {
                    if (element.getForeground() != null && !element.getForeground().isDisposed()) {
                        element.getForeground().dispose();
                    }
                    if (element.getBackground() != null && !element.getBackground().isDisposed()) {
                        element.getBackground().dispose();
                    }
                }
                element.dispose();
            }
        }
    }

    private static final int DEFAULT_GROUP_HEIGHT = 25;

    /**
     * Initialize all components for the defined section for this node.
     */
    public synchronized void addComponents(boolean forceRedraw, boolean reInitialize, int height) {
        placeComponents(forceRedraw, reInitialize, height);
    }

    /**
     * Initialize all SWT Controls for the defined section for this node.
     * This method allows to override Composite layout logic, but without loosing code, which is added in
     * MissingSettingsMultiThreadDynamicComposite.addComponents() and TopMessagesMultiThreadDynamicComposite.addComponents() child classes.
     * Before MissingSettingsMultiThreadDynamicComposite child class should override addComponents() method, which leads to loosing features.
     */
    protected synchronized void placeComponents(boolean forceRedraw, boolean reInitialize, int height) {
     // achen modifed to fix feature 0005991 if composite.isDisposed return
        if (elem == null || composite.isDisposed()) {
            return;
        }

        checkErrorsWhenViewRefreshed = true;
        int heightSize = 0, maxRowSize = 0, nbInRow, numInRow;
        int maxRow;
        boolean isCompute = false;

        if (!forceRedraw) {
            boolean needRedraw = isNeedRedraw();
            if (!needRedraw) {
                // System.out.println("no need redraw");
                return;
            }
        }

        Control lastControl = null;
        if (reInitialize) {
            if (currentComponent != null) {
                disposeChildren();
            }
        } else {
            heightSize = height;
        }
        final List<IElementParameter> currentValidParameters = new ArrayList<IElementParameter>(20);

        hashCurControls = new DualHashBidiMap();

        maxRow = 0;
        List<? extends IElementParameter> listParam = elem.getElementParametersWithChildrens();

        IElementParameter unifiedParam = elem.getElementParameterFromField(EParameterFieldType.UNIFIED_COMPONENTS);
        if (unifiedParam != null) {
            for (IElementParameter param : listParam) {
                int numRow = param.getNumRow();
                param.setNumRow(numRow + 1);
            }
        }

        Map<String, Integer> groupPosition = new HashMap<String, Integer>();
        for (int i = 0; i < listParam.size(); i++) {
            IElementParameter param = listParam.get(i);
            if (param.getCategory() == section && param.getFieldType() != EParameterFieldType.TECHNICAL
                    && param.isShow(listParam)) {

                currentValidParameters.add(param);
                if (param.getNumRow() > maxRow) {
                    maxRow = param.getNumRow();
                }
            }
        }

        IElementParameter synchronizeSchemaParam = elem.getElementParameter(EParameterName.NOT_SYNCHRONIZED_SCHEMA.getName());

        if (synchronizeSchemaParam != null) {
            // if the node don't contains a schema type and accept an input flow and is not synchronized
            // display a schema on the first line just the type to synchronize the schema
            synchronizeSchemaParam.setShow(!((Node) elem).isSchemaSynchronized());
            IElementParameter schemaTypeParam = synchronizeSchemaParam.getChildParameters().get(
                    EParameterName.SCHEMA_TYPE.getName());
            if (schemaTypeParam != null) {
                schemaTypeParam.setReadOnly(((Node) elem).isSchemaSynchronized());
            }
        }

        generator.initController(this);

        int additionalHeightSize = 0;
        boolean hasDynamicRow = false;
        for (IElementParameter curParam : currentValidParameters) {
            AbstractElementPropertySectionController controller = generator.getController(curParam.getFieldType(), this);

            if (controller == null) {
                continue;
            }
            if (controller.hasDynamicRowSize()) {
                hasDynamicRow = true;
                break;
            }
        }
        if (hasDynamicRow) {
            additionalHeightSize = estimatePropertyHeightSize(maxRow, listParam);
        }

        for (int curRow = 1; curRow <= maxRow; curRow++) {
            maxRowSize = 0;
            nbInRow = 0;
            for (IElementParameter curParam : currentValidParameters) {
                if (curParam.getNumRow() == curRow) {
                    nbInRow++;
                }
            }
            numInRow = 0;
            lastControl = null;
            curRowSize = 0;
            for (IElementParameter curParam : currentValidParameters) {
                updateParameter(curParam);
                if (curParam.getNumRow() == curRow && isShouldDisParameter(curParam)) {
                    numInRow++;
                    AbstractElementPropertySectionController controller = generator.getController(curParam.getFieldType(), this);

                    if (controller == null) {
                        continue;
                    }

                    if (controller.hasDynamicRowSize()) {
                        controller.setAdditionalHeightSize(additionalHeightSize);
                    }

                    String groupName = curParam.getGroup();
                    Composite subComposite = null;
                    Control cutLastControl = lastControl;
                    int curNumInRow = numInRow;
                    int curNbInRow = nbInRow;
                    int curTop = 0;

                    if (!isCompactView()) {
                        if (numInRow > 1 && nbInRow > 1) {
                            heightSize += maxRowSize;
                        }
                        curNumInRow = 1;
                        curNbInRow = 1;
                        cutLastControl = null;
                    }

                    if (groupName != null) {
                        if (!hashCurControls.containsKey(groupName)) {
                            if (groupPosition.size() > 0) {
                                heightSize += DEFAULT_GROUP_HEIGHT;
                            }
                            new GroupController(this).createControl(composite, curParam, numInRow, nbInRow, heightSize,
                                    lastControl);
                            groupPosition.put(groupName, heightSize);
                        }
                        subComposite = (Composite) hashCurControls.get(groupName);

                        Integer position = groupPosition.get(groupName);
                        //position can be null: if (show-if="...") return false for all the items of a group,
                        //then the group is not visible and then the groupPosition is null.
                        if (position!=null) {
                            curTop = heightSize - position;
//                        } else {
//                            System.out.println("curParam="+curParam+", numInRow="+numInRow+", nbInRow="+nbInRow+", "+"heightSize="+heightSize+", groupName="+groupName+", "+groupPosition.get(groupName));
                        }
                    } else {
                        subComposite = composite;
                        curTop = DEFAULT_GROUP_HEIGHT * (groupPosition.size() > 0 ? 1 : 0) + heightSize;
                    }
                    if (!isShouldCreateControl(curParam)) {
                        continue;
                    }
                    lastControl = controller.createControl(subComposite, curParam, curNumInRow, curNbInRow, curTop,
                            cutLastControl);

                    if (curRowSize > maxRowSize) {
                        maxRowSize = curRowSize;
                    }
                }
            }
            heightSize += maxRowSize;

        }
        if (synchronizeSchemaParam != null) {
            synchronizeSchemaParam.setShow(false);
        }
        minHeight = heightSize;
        resizeScrolledComposite();

        // change back row number after create control
        if (unifiedParam != null) {
            for (IElementParameter param : listParam) {
                int numRow = param.getNumRow();
                param.setNumRow(numRow - 1);
            }
        }
    }

    protected boolean isShouldCreateControl(IElementParameter curParam) {
        return true;
    }

    /**
     * this method to special which control should display on the composite
     *
     * in use in tmatchGroup component
     *
     * @param curParam
     * @return
     */
    protected boolean isShouldDisParameter(IElementParameter curParam) {
        return true;
    }

    /**
     *
     * only need a convert for MatchRuleComposite, because we split old composite into two different composite
     *
     * in use in tmatchGroup component
     *
     * @param curParam
     */
    protected void updateParameter(IElementParameter curParam) {
        // only need a convert for MatchRuleComposite
    }

    /**
     * DOC Administrator Comment method "isNeedRedraw".
     *
     * @return
     */
    protected boolean isNeedRedraw() {
        Boolean updateNeeded = (Boolean) elem.getPropertyValue(updataComponentParamName);
        if (updateNeeded) {
            return true;
        }
        boolean needRedraw = false;
        for (IElementParameter elementParameter : elem.getElementParametersWithChildrens()) {
            if (elementParameter.getCategory().equals(section)
                    && (elementParameter.getFieldType() != EParameterFieldType.SCHEMA_TYPE)
                    && (elementParameter.getFieldType() != EParameterFieldType.SCHEMA_REFERENCE)
                    && (elementParameter.getFieldType() != EParameterFieldType.QUERYSTORE_TYPE)) {
                // if the component must be displayed, then check if the
                // control exists or not.
                boolean show = elementParameter.isShow(elem.getElementParameters());
                Object control;
                if (elementParameter.getParentParameter() == null) {
                    control = hashCurControls.get(elementParameter.getName());
                } else {
                    control = hashCurControls.get(elementParameter.getParentParameter().getName() + ":" //$NON-NLS-1$
                            + elementParameter.getName());
                }
                if ((control == null && show) || (control != null && !show)) {
                    needRedraw = true;
                    break;

                }
            }
        }
        return needRedraw;
    }

    /**
     * DOC nrousseau Comment method "estimatePropertyHeightSize".
     *
     * @param maxRow
     * @param listParam
     * @param tabbedPropertyComposite
     */
    private int estimatePropertyHeightSize(int maxRow, List<? extends IElementParameter> listParam) {
        int estimatedHeightSize = 0, estimatedMaxRowSize = 0;
        int additionalHeightSize = 0;
        int compositeHeight = getParent().getBounds().height;

        // System.out.println("size composite:" + compositeHeight);

        int nbDynamic = 0;
        for (int curRow = 1; curRow <= maxRow; curRow++) {
            estimatedMaxRowSize = 0;
            for (int i = 0; i < listParam.size(); i++) {
                IElementParameter curParam = listParam.get(i);
                if (curParam.getCategory() == section) {
                    if (curParam.getNumRow() == curRow && (curParam.getFieldType() != EParameterFieldType.TECHNICAL)) {
                        // System.out.println("test:" + curParam.getName() + "
                        // field:"+curParam.getField());
                        if (curParam.isShow(listParam)) {
                            // System.out.println("show:" + curParam.getName()+
                            // " field:"+curParam.getField());
                            AbstractElementPropertySectionController controller = generator.getController(
                                    curParam.getFieldType(), this);

                            if (controller == null) {
                                break;
                            }
                            int estimatedSize = controller.estimateRowSize(composite, curParam);
                            if (controller.hasDynamicRowSize()) {
                                nbDynamic++;
                            }
                            // System.out.println("param:" + curParam.getName()
                            // + " - estimatedSize:" + estimatedSize);

                            if (estimatedSize > estimatedMaxRowSize) {
                                estimatedMaxRowSize = estimatedSize;
                            }
                        }
                    }
                }
            }
            estimatedHeightSize += estimatedMaxRowSize;
        }
        // System.out.println("*** ESTIMATED SIZE:" + estimatedHeightSize + "
        // ***");
        int emptySpace = compositeHeight - estimatedHeightSize;
        // System.out.println("--- EMPTY SPACE:" + emptySpace);
        if (emptySpace > 0 && nbDynamic > 0) {
            additionalHeightSize = emptySpace / nbDynamic;
            // System.out.println("--- DIVIDED ADDITIONAL HEIGHT (for each
            // dynamic):" + additionalHeightSize);
        }
        return additionalHeightSize;
    }

    protected void resizeScrolledComposite() {

        lastCompositeSize = getParent().getClientArea().height;

        // setMinSize(compositeSize);
        propertyResized = true;
    }

    @Override
    public void refresh() {
        // refactore because of TDI-24184
        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                operationInThread();
            }
        });

    }

    // refactore to be synchonized with the dispose() method because of TDI-24184
    // the synchronized methodis a quick fix but not the ideal one because this method is accessing many attributes
    // of the current class that may be modified by other thread (just like "elem" modified by the dispose() method.
    synchronized protected void operationInThread() {
        if (elem == null) {
            return;
        }
        List<? extends IElementParameter> listParam = elem.getElementParametersWithChildrens(); // hywang modified for

        Boolean updateNeeded = (Boolean) elem.getPropertyValue(updataComponentParamName);

        if (updateNeeded != null) {
            if (updateNeeded) {
                if (elem != null) {
                    addComponents(forceRedraw);
                    elem.setPropertyValue(updataComponentParamName, new Boolean(false));
                    forceRedraw = false;
                }
            }
        }

        final ECodeLanguage language = ((RepositoryContext) org.talend.core.CorePlugin.getContext().getProperty(
                org.talend.core.context.Context.REPOSITORY_CONTEXT_KEY)).getProject().getLanguage();

        IRunProcessService service = DesignerPlugin.getDefault().getRunProcessService();
        final ICodeProblemsChecker syntaxChecker = service.getSyntaxChecker(language);
        List<Problem> javaProblem = null;

        for (int i = 0; i < listParam.size(); i++) {
            if (listParam.get(i).getCategory() == section) {
                if (listParam.get(i).isShow(listParam)) {

                    final IElementParameter e = listParam.get(i);
                    e.isReadOnly();
                    e.isNoCheck();
                    if (language == ECodeLanguage.JAVA && javaProblem == null) {
                        if (!e.isReadOnly() && !e.isNoCheck()) {
                            javaProblem = syntaxChecker.checkProblems(null);
                        }
                    }
                    final List<Problem> nodePros = javaProblem;

                    if (generator != null) {
                        AbstractElementPropertySectionController controller = generator.getController(e.getFieldType(),
                                MultipleThreadDynamicComposite.this);
                        if (controller != null) {
                            controller.updateCodeProblems(nodePros);
                            controller.refresh(e, checkErrorsWhenViewRefreshed);
                        }
                    }

                }
            }
        }
        if (propertyResized) {
            try {
                removeListener(SWT.Resize, resizeListener);
                getParent().layout();

                composite.pack();
                propertyResized = false;
                addListener(SWT.Resize, resizeListener);
            } catch (Exception e) {
            }

        }

        checkErrorsWhenViewRefreshed = false;
        //        long time = TimeMeasure.timeSinceBegin("DC:refresh:" + getCurrentComponent()); //$NON-NLS-1$
        //        TimeMeasure.end("DC:refresh:" + getCurrentComponent()); //$NON-NLS-1$
        // if (DynamicTabbedPropertySection.DEBUG_TIME) {
        //            System.out.println("DC;total;" + getCurrentComponent() + ";" + time); //$NON-NLS-1$ //$NON-NLS-2$
        // }

    }

    protected final Listener resizeListener = new Listener() {

        @Override
        public void handleEvent(Event event) {
            resizeLimiter.resetTimer();
            resizeLimiter.startIfExecutable(true, null);
        }
    };

    private final ExecutionLimiter resizeLimiter = new ExecutionLimiter(250, true) {

        @Override
        public void execute(final boolean isFinalExecution, Object data) {
            if (!isDisposed()) {
                getDisplay().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        handleResize();
                    }

                });
            }
        }
    };

    protected void handleResize() {
        if (!isDisposed() && !getParent().isDisposed()) {
            int currentSize = getParent().getClientArea().height;
            if (getLastCompositeSize() != currentSize) {
                addComponents(true);
                refresh();
            }
        }

    }

    /**
     * Getter for isCompactView.
     *
     * @return the isCompactView
     */
    public boolean isCompactView() {
        return this.isCompactView;
    }

    /**
     * Sets the isCompactView.
     *
     * @param isCompactView the isCompactView to set
     */
    public void setCompactView(boolean isCompactView) {
        this.isCompactView = isCompactView;
    }

    public MultipleThreadDynamicComposite(Composite parentComposite, int styles, final EComponentCategory section,
            Element element, boolean isCompactView) {
        this(parentComposite, styles, section, element, isCompactView, Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
    }

    /**
     * Set the section of the tabbed property.
     *
     * @param section
     */
    public MultipleThreadDynamicComposite(Composite parentComposite, int styles, final EComponentCategory section,
            Element element, boolean isCompactView, Color backgroundColor) {
        super(parentComposite, styles);
        this.setMinSize(700, 450);
        setCompactView(isCompactView);
        updataComponentParamName = EParameterName.UPDATE_COMPONENTS.getName();
        FormData d = new FormData();
        d.left = new FormAttachment(0, 0);
        d.right = new FormAttachment(100, 0);
        d.top = new FormAttachment(0, 0);
        d.bottom = new FormAttachment(100, 0);
        setLayoutData(d);

        setBackground(backgroundColor);

        setExpandHorizontal(true);
        // setExpandVertical(true);
        composite = widgetFactory.createComposite(this, SWT.NO_FOCUS);
        setContent(composite);
        composite.setBackground(backgroundColor);

        generator = new DynamicPropertyGenerator();
        this.section = section;
        this.elem = element;
        if (elem instanceof Node) {
            process = (Process) ((Node) elem).getProcess();
        } else if (elem instanceof SubjobContainer) {
            process = (Process) ((SubjobContainer) elem).getProcess();
        }
        if (elem instanceof org.talend.designer.core.ui.editor.connections.Connection) {
            org.talend.designer.core.ui.editor.connections.Connection connection;
            connection = (org.talend.designer.core.ui.editor.connections.Connection) elem;
            process = (Process) connection.getSource().getProcess();
        }
        if (elem instanceof Process) {
            process = (Process) elem;
        }
        if (process != null) {
            part = (AbstractMultiPageTalendEditor) process.getEditor();
        }
        FormLayout layout = new FormLayout();
        layout.marginWidth = ITabbedPropertyConstants.HSPACE + 2;
        layout.marginHeight = ITabbedPropertyConstants.VSPACE;
        layout.spacing = ITabbedPropertyConstants.VMARGIN + 1;
        composite.setLayout(layout);

        hashCurControls = new DualHashBidiMap();

        if ((currentComponent == null) || (!currentComponent.equals(elem.getElementName()))) {
            forceRedraw = true;
            elem.setPropertyValue(updataComponentParamName, Boolean.TRUE);

        }
        currentComponent = elem.getElementName();

        propertyResized = true;
        addListener(SWT.Resize, resizeListener);

        if (getCommandStack() != null) {
            getCommandStack().addCommandStackEventListener(commandStackEventListener);
        }

        // add CSS class
        CoreUIPlugin.setCSSClass(this, MultipleThreadDynamicComposite.class.getSimpleName(), false);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.swt.widgets.Widget#dispose()
     */
    @Override
    synchronized public void dispose() {
        CommandStack cmdStack = getCommandStack();
        if (cmdStack != null) {
            cmdStack.removeCommandStackEventListener(commandStackEventListener);
        }
        disposeChildren();
        if (widgetFactory != null) {
            widgetFactory.dispose();
        }
        super.dispose();
        process = null;
        elem = null;
        part = null;
        generator.dispose();
        generator = null;
        hashCurControls.clear();
    }

    CommandStackEventListener commandStackEventListener = new CommandStackEventListener() {

        @Override
        public void stackChanged(CommandStackEvent event) {
            int detail = event.getDetail();
            if ((getElement() instanceof org.talend.designer.core.ui.editor.connections.Connection)
                    && (event.getCommand() instanceof ChangeMetadataCommand)
                    && (0 != (detail & CommandStack.POST_EXECUTE) || 0 != (detail & CommandStack.POST_REDO) //
                    || 0 != (detail & CommandStack.POST_REDO))) {
                addComponents(true);
                refresh();
            }
            if (0 != (detail & CommandStack.POST_EXECUTE) || 0 != (detail & CommandStack.POST_UNDO)
                    || 0 != (detail & CommandStack.POST_REDO)) {
                Boolean updateNeeded = (Boolean) elem.getPropertyValue(updataComponentParamName);
                if (updateNeeded) {
                    refresh();
                }
            }
        }
    };

    /**
     * yzhang Comment method "setCurRowSize" Sets the curRowSize.
     *
     * @param curRowSize int
     */
    @Override
    public void setCurRowSize(int curRowSize) {
        this.curRowSize = curRowSize;
    }

    /**
     * Getter for currentComponent.
     *
     * @return the currentComponent
     */
    public String getCurrentComponent() {
        return this.currentComponent;
    }

    /**
     * Getter for curRowSize.
     *
     * @return the curRowSize
     */
    @Override
    public int getCurRowSize() {
        return this.curRowSize;
    }

    /**
     * Getter for hashCurControls.
     *
     * @return the hashCurControls
     */
    @Override
    public BidiMap getHashCurControls() {
        return this.hashCurControls;
    }

    /**
     * Getter for part.
     *
     * @return the part
     */
    @Override
    public AbstractMultiPageTalendEditor getPart() {
        return this.part;
    }

    /**
     * Getter for section.
     *
     * @return the section
     */
    @Override
    public EComponentCategory getSection() {
        return this.section;
    }

    /**
     * Get the command stack of the Gef editor.
     *
     * @return
     */
    public CommandStack getCommandStack() {
        if (part != null && part.getTalendEditor() != null) {
            Object adapter = part.getTalendEditor().getAdapter(CommandStack.class);
            return (CommandStack) adapter;
        } else {
            return null;
        }
    }

    /**
     * qzhang Comment method "getDefaultRepository".
     *
     * @return
     */
    private String getDefaultRepository(IElementParameter baseParam, boolean istable, String defaultPropertyValue) {
        boolean metadataInput = false;
        if (((Node) elem).getCurrentActiveLinksNbInput(EConnectionType.FLOW_MAIN) > 0
                || ((Node) elem).getCurrentActiveLinksNbInput(EConnectionType.FLOW_REF) > 0
                || ((Node) elem).getCurrentActiveLinksNbInput(EConnectionType.TABLE) > 0) {
            metadataInput = true;
        }

        if (metadataInput && istable) {
            return (String) baseParam.getChildParameters().get(EParameterName.REPOSITORY_SCHEMA_TYPE.getName()).getValue();
        }
        Object propertyValue = elem.getPropertyValue(EParameterName.REPOSITORY_PROPERTY_TYPE.getName());
        if ((propertyValue == null || !(propertyValue instanceof String)) && defaultPropertyValue != null) {
            propertyValue = defaultPropertyValue;
        }
        if (propertyValue == null || propertyValue.equals("")) { //$NON-NLS-1$
            return ""; //$NON-NLS-1$
        }
        if (istable) {
            //
        } else {
            //
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * Getter for tableIdAndDbTypeMap.
     *
     * @return the tableIdAndDbTypeMap
     */
    @Override
    public Map<String, String> getTableIdAndDbTypeMap() {
        if (this.tableIdAndDbTypeMap.isEmpty()) {
            updateRepositoryList();
        }
        return this.tableIdAndDbTypeMap;
    }

    /**
     * Getter for tableIdAndDbSchemaMap.
     *
     * @return the tableIdAndDbSchemaMap
     */
    @Override
    public Map<String, String> getTableIdAndDbSchemaMap() {
        if (this.tableIdAndDbSchemaMap.isEmpty()) {
            updateRepositoryList();
        }
        return this.tableIdAndDbSchemaMap;
    }

    /**
     * hywang Comment method "getRepositoryFileItemMap".
     *
     * @see org.talend.core.ui.properties.tab.IDynamicProperty#getRepositoryFileItemMap()
     */
    public Map<String, FileItem> getRepositoryFileItemMap() {
        if (this.repositoryFileItemMap.isEmpty()) {
            updateRepositoryList();
        }
        return this.repositoryFileItemMap;
    }

    public Map<String, LinkRulesItem> getRepositoryLinkRulesItemMap() {
        return this.repositoryLinkRulesItemMap;
    }

    public void setRepositoryFileItemMap(Map<String, FileItem> repositoryFileItemMap) {
        this.repositoryFileItemMap = repositoryFileItemMap;
    }

    /**
     * Getter for lastCompositeSize.
     *
     * @return the lastCompositeSize
     */
    public int getLastCompositeSize() {
        return this.lastCompositeSize;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties.controllers.generator.IDynamicProperty#getComposite()
     */
    @Override
    public Composite getComposite() {
        return composite;
    }

    public boolean isPropertyResized() {
        return this.propertyResized;
    }

    public void setPropertyResized(boolean propertyResized) {
        this.propertyResized = propertyResized;
    }
}
