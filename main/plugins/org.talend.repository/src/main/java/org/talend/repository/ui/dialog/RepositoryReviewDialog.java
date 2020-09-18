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
package org.talend.repository.ui.dialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.time.TimeMeasure;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.hadoop.IHadoopClusterService;
import org.talend.core.hadoop.repository.HadoopRepositoryUtil;
import org.talend.core.model.components.EComponentType;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.FolderItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryContentHandler;
import org.talend.core.model.repository.IRepositoryTypeProcessor;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryContentManager;
import org.talend.core.model.utils.RepositoryManagerHelper;
import org.talend.core.runtime.services.IGenericWizardService;
import org.talend.repository.ProjectManager;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.processor.ContextTypeProcessor;
import org.talend.repository.ui.processor.HeaderFooterTypeProcessor;
import org.talend.repository.ui.processor.JobTypeProcessor;
import org.talend.repository.ui.processor.MetadataMultiTypeProcessor;
import org.talend.repository.ui.processor.QueryTypeProcessor;
import org.talend.repository.ui.processor.RepositoryTypeProcessor;
import org.talend.repository.ui.processor.SAPFunctionProcessor;
import org.talend.repository.ui.processor.SchemaTypeProcessor;
import org.talend.repository.ui.processor.SingleSelectedInMultiTypesProcessor;
import org.talend.repository.ui.processor.ValidationRuleTypeProcessor;
import org.talend.repository.ui.views.IRepositoryView;
import org.talend.repository.viewer.ui.provider.RepoCommonViewerProvider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * bqian check the content of the repository view. <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 *
 */
public class RepositoryReviewDialog extends Dialog {

    ERepositoryObjectType type;

    private List<ERepositoryObjectType> repObjectTypes;

    String repositoryType;

    private String[] repositoryTypes;

    private RepositoryNode result;

    IRepositoryTypeProcessor typeProcessor;

    /*
     * selectedNodeName,isSelectionId,selectionType for selection
     */
    private String selectedNodeName;

    private boolean isSelectionId;

    private ERepositoryObjectType selectionType;

    private ViewerFilter[] additionalFilters;

    private DatabaseTypeFilter dbSupportFilter;

    ViewerTextFilter textFilter = new ViewerTextFilter();

    DatabaseJDBCFilter jdbcFilter = new DatabaseJDBCFilter();

    private TreeViewer repositoryTreeViewer;

    private IRepositoryView repView;

    private IElement elem;

    private IElementParameter elementParameter;

    private boolean showFilterText;

    private static final String FILTER = "FILTER"; //$NON-NLS-1$

    private static final String ISSPARK = "ISSPARK"; //$NON-NLS-1$

    private static final String USEYARN = "USEYARN"; //$NON-NLS-1$

    private static final String ELEMENT = "ELEMENT"; //$NON-NLS-1$

    private boolean filterReferenceNode = false;

    protected RepositoryReviewDialog(Shell parentShell) {
        super(parentShell);
        setShellStyle(SWT.SHELL_TRIM | SWT.APPLICATION_MODAL | getDefaultOrientation());

        boolean debugMode = CommonsPlugin.isDebugMode();
        // debugMode = true;
        TimeMeasure.display = debugMode;
        TimeMeasure.displaySteps = debugMode;
        TimeMeasure.measureActive = debugMode;

        TimeMeasure.begin(RepositoryReviewDialog.class.getSimpleName());
    }

    public RepositoryReviewDialog(Shell parentShell, List<ERepositoryObjectType> repObjectTypes, String processId) {
        this(parentShell, repObjectTypes, processId, false);
    }

    public RepositoryReviewDialog(Shell parentShell, List<ERepositoryObjectType> repObjectTypes, String processId,
            boolean showFilterText) {
        this(parentShell);
        this.repObjectTypes = repObjectTypes;
        /*
         * avoid select self repository node for Process Type.
         *
         * borrow the repositoryType to set the current process id here.
         */
        this.repositoryType = processId;
        this.showFilterText = showFilterText;
        typeProcessor = createMultiTypesProcessor();
    }

    /**
     * DOC bqian RepositoryReviewDialog constructor comment.
     *
     * @param parentShell
     * @param type support ERepositoryObjectType.PROCESS -> process <br>
     * ERepositoryObjectType.METADATA --> Repository <br>
     * ERepositoryObjectType.METADATA_CON_TABLE --> Schema <br>
     * ERepositoryObjectType.METADATA_CON_QUERY --> Query <br>
     *
     * @param repositoryType String repositoryType = elem.getElementParameter(paramName).getRepositoryValue();<br>
     * see DynamicComposite.updateRepositoryListExtra().<br>
     *
     *
     */
    public RepositoryReviewDialog(Shell parentShell, ERepositoryObjectType type, String repositoryType) {
        this(parentShell);
        this.type = type;
        /*
         * avoid select self repository node for Process Type.
         *
         * borrow the repositoryType to set the current process id here.
         */
        this.repositoryType = repositoryType;
        typeProcessor = createTypeProcessor();
    }

    public RepositoryReviewDialog(Shell parentShell, ERepositoryObjectType type, IElement compElement,
            IElementParameter elemParameter) {
        this(parentShell);
        this.type = type;
        /*
         * avoid select self repository node for Process Type.
         *
         * borrow the repositoryType to set the current process id here.
         */
        this.repositoryType = elemParameter.getRepositoryValue();
        this.elem = compElement;
        this.elementParameter = elemParameter;
        typeProcessor = createTypeProcessor();
    }

    public RepositoryReviewDialog(Shell parentShell, String[] repositoryTypes) {
        this(parentShell);
        this.repositoryTypes = repositoryTypes;
        typeProcessor = createTypeProcessor();
    }

    public RepositoryReviewDialog(Shell parentShell, ERepositoryObjectType type, String repositoryType, String[] itemFilter) {
        this(parentShell, type, repositoryType);
        this.dbSupportFilter = new DatabaseTypeFilter(itemFilter);
    }

    /**
     * DOC ycbai RepositoryReviewDialog constructor comment.
     *
     * @param parentShell
     * @param type
     * @param repositoryType
     * @param additionalFilter
     */
    public RepositoryReviewDialog(Shell parentShell, ERepositoryObjectType type, String repositoryType,
            ViewerFilter[] additionalFilters) {
        this(parentShell, type, repositoryType);
        this.additionalFilters = additionalFilters;
    }

    public RepositoryReviewDialog(Shell parentShell, ERepositoryObjectType type, String repositoryType,
            ViewerFilter[] additionalFilters, IElement elem) {
        this(parentShell, type, repositoryType);
        this.additionalFilters = additionalFilters;
        this.elem = elem;
        this.repositoryType = repositoryType;
        typeProcessor = createTypeProcessor();
    }

    public RepositoryReviewDialog(Shell parentShell, ERepositoryObjectType type, Boolean isHeaderButton, String repositoryType) {
        this(parentShell, type, repositoryType);

        if (typeProcessor instanceof RepositoryTypeProcessor) {
            ((RepositoryTypeProcessor) typeProcessor).setHeaderButton(isHeaderButton);
        }
    }

    public RepositoryReviewDialog(Shell parentShell, ERepositoryObjectType type, String repositoryType,
            boolean hidenTypeSelection, boolean needInitialize) {
        this(parentShell, type, repositoryType);

        if (hidenTypeSelection && (typeProcessor instanceof RepositoryTypeProcessor)) {
            ((RepositoryTypeProcessor) typeProcessor).setHidenTypeSelection(hidenTypeSelection);
        }

    }

    public RepositoryReviewDialog(Shell parentShell, ERepositoryObjectType type) {
        this(parentShell, type, null);
    }

    public RepositoryReviewDialog(Shell parentShell, IRepositoryTypeProcessor typeProcessor, ERepositoryObjectType type) {
        this(parentShell, type);
        this.typeProcessor = typeProcessor;
    }

    protected TreeViewer getRepositoryTreeViewer() {
        return repositoryTreeViewer;
    }

    protected IRepositoryView getRepView() {
        if (repView == null) {
            repView = RepositoryManagerHelper.findRepositoryView();
        }
        return repView;
    }

    /**
     * Creates a processor which provides multiple types of process. Added by Marvin Wang on Apr 19, 2013.
     *
     * @return
     */
    private IRepositoryTypeProcessor createMultiTypesProcessor() {
        if (repObjectTypes != null && !repObjectTypes.isEmpty()) {
            return new SingleSelectedInMultiTypesProcessor(repositoryType, repObjectTypes);
        }
        throw new IllegalArgumentException(Messages.getString("RepositoryReviewDialog.0", type)); //$NON-NLS-1$
    }

    /**
     * bqian create the correct TypeProcessor according to the type.
     *
     * @return
     */
    private IRepositoryTypeProcessor createTypeProcessor() {
        for (IRepositoryContentHandler handler : RepositoryContentManager.getHandlers()) {
            IRepositoryTypeProcessor processor = handler.getRepositoryTypeProcessor(repositoryType);
            if (processor != null) {
                return processor;
            }
        }
        boolean isGeneric = false;
        if(this.elem != null && (this.elem instanceof INode)
                && ((INode)elem).getComponent().getComponentType() == EComponentType.GENERIC){
            isGeneric = true;
        }
        if (repositoryType != null && repositoryType.contains("|")) { //$NON-NLS-1$
            String[] repTypes = repositoryType.split("\\|"); //$NON-NLS-1$
            IRepositoryTypeProcessor hadoopTypeProcessor = getHadoopSubMultiRepTypeProcessor(repTypes);
            if (hadoopTypeProcessor != null) {
                return hadoopTypeProcessor;
            }
        }
        if (type == null && repositoryTypes != null) {
            return new MetadataMultiTypeProcessor(repositoryTypes);
        }

        if (type == ERepositoryObjectType.PROCESS) {
            return new JobTypeProcessor(repositoryType);
        }

        if (type == ERepositoryObjectType.METADATA) {
            return new RepositoryTypeProcessor(repositoryType, isGeneric);
        }

        if (type == ERepositoryObjectType.METADATA_CON_TABLE) {
            return new SchemaTypeProcessor(repositoryType);
        }

        if (type == ERepositoryObjectType.METADATA_CON_QUERY) {
            return new QueryTypeProcessor(repositoryType);
        }

        if (type == ERepositoryObjectType.METADATA_SAP_FUNCTION) {
            return new SAPFunctionProcessor(repositoryType);
        }

        if (type == ERepositoryObjectType.CONTEXT) {
            return new ContextTypeProcessor(repositoryType);
        }
        if (type == ERepositoryObjectType.METADATA_HEADER_FOOTER) {
            return new HeaderFooterTypeProcessor(repositoryType);
        }

        if (type == ERepositoryObjectType.METADATA_VALIDATION_RULES) {
            return new ValidationRuleTypeProcessor(repositoryType, elem);
        }

        throw new IllegalArgumentException(Messages.getString("RepositoryReviewDialog.0", type)); //$NON-NLS-1$
    }

    private IRepositoryTypeProcessor getHadoopSubMultiRepTypeProcessor(String[] repTypes) {
        IHadoopClusterService hadoopClusterService = HadoopRepositoryUtil.getHadoopClusterService();
        if (hadoopClusterService != null) {
            List<String> repTypeList = new ArrayList<String>();
            Map<String, Object> attributes = new HashMap<String, Object>();
            for (String repType : repTypes) {
                Map<String, Object> attr = parseAttributes(repType);
                if (attr == null) {
                    repTypeList.add(repType);
                } else {
                    attributes.putAll(attr);
                }
            }
            attributes.put(ELEMENT, elem);
            IRepositoryTypeProcessor processor = hadoopClusterService.getHadoopSubMultiRepTypeProcessor(repTypeList
                    .toArray(new String[0]));
            processor.setAttributes(attributes);
            return processor;
        }

        return null;
    }

    private Map<String, Object> parseAttributes(String typeString) {
        Map<String, Object> attributeMap = new HashMap<String, Object>();

        if (typeString.contains("{")) { //$NON-NLS-1$
            try {
                JsonNode root = new ObjectMapper().readTree(typeString.replaceAll("\\$", "\"")); //$NON-NLS-1$ //$NON-NLS-2$
                if (root != null) {
                    JsonNode filter = root.get(FILTER);
                    if (filter != null) {
                        JsonNode isSparkJsonNode = filter.get(ISSPARK);
                        if (isSparkJsonNode != null) {
                            String isSparkExpression = isSparkJsonNode.asText();
                            attributeMap.put(ISSPARK,
                                    elementParameter.isShow(isSparkExpression, null, elem.getElementParameters()));
                        }
                        JsonNode useYarnJsonNode = filter.get(USEYARN);
                        if (useYarnJsonNode != null) {
                            String useYarnExpression = useYarnJsonNode.asText();
                            attributeMap.put(USEYARN,
                                    elementParameter.isShow(useYarnExpression, null, elem.getElementParameters()));
                        }
                    }
                } else {
                    attributeMap = null;
                }
            } catch (Exception e) {
                attributeMap = null;
                ExceptionHandler.process(e);
            }
        } else {
            attributeMap = null;
        }

        return attributeMap;
    }

    /**
     * Configures the shell
     *
     * @param shell the shell
     */
    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        // Set the title bar text and the size
        if (typeProcessor.getDialogTitle() == null) {
            shell.setText(Messages.getString("RepositoryReviewDialog.repositoryContent")); //$NON-NLS-1$
        } else {
            shell.setText(typeProcessor.getDialogTitle());
        }
    }

    protected Control createDialogArea(Composite parent, String specifiedPId) {

        Composite container = (Composite) super.createDialogArea(parent);
        TimeMeasure.step(RepositoryReviewDialog.class.getSimpleName(), "before createDialogArea..."); //$NON-NLS-1$
        GridData data = (GridData) container.getLayoutData();
        data.minimumHeight = 400;
        data.heightHint = 400;
        data.minimumWidth = 500;
        data.widthHint = 500;
        container.setLayoutData(data);

        createFilterField(container);

        Composite viewContainer = new Composite(container, SWT.BORDER);
        viewContainer.setLayout(new GridLayout());
        viewContainer.setLayoutData(new GridData(GridData.FILL_BOTH));

        repositoryTreeViewer = RepoCommonViewerProvider.NORMAL.createViewer(viewContainer, specifiedPId);

        TimeMeasure.step(RepositoryReviewDialog.class.getSimpleName(), "finshed createViewer"); //$NON-NLS-1$

        repositoryTreeViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));

        addFilter(textFilter);
        if (dbSupportFilter != null) {
            addFilter(dbSupportFilter);
        }
        if (additionalFilters != null) {
            addFilter(additionalFilters);
        }
        ViewerFilter filter = typeProcessor.makeFilter();
        addFilter(filter);
        // filter in case multiple jdbc
        jdbcFilter.setElem(elem);
        addFilter(jdbcFilter);
        TimeMeasure.step(RepositoryReviewDialog.class.getSimpleName(), "finshed add Filters"); //$NON-NLS-1$

        TimeMeasure.step(RepositoryReviewDialog.class.getSimpleName(), "set input"); //$NON-NLS-1$
        repositoryTreeViewer.expandToLevel(2);
        TimeMeasure.step(RepositoryReviewDialog.class.getSimpleName(), "expandAll"); //$NON-NLS-1$
        final Label label = new Label(container, SWT.NONE);
        label.setText(Messages.getString("RepositoryReviewDialog.label")); //$NON-NLS-1$
        label.setForeground(new Color(null, 250, 0, 0));
        label.setVisible(false);
        // see feature 0003664: tRunJob: When opening the tree dialog to select the job target, it could be useful to
        // open it on previous selected job if exists
        selectNode();
        TimeMeasure.step(RepositoryReviewDialog.class.getSimpleName(), "selectNode"); //$NON-NLS-1$

        repositoryTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                boolean highlightOKButton = isSelectionValid(event);
                getButton(IDialogConstants.OK_ID).setEnabled(highlightOKButton);
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                if (selection != null && !selection.isEmpty()) {
                    RepositoryNode node = (RepositoryNode) selection.getFirstElement();
                    if (!highlightOKButton && node.getType() == ENodeType.REPOSITORY_ELEMENT
                            && node.getObjectType().equals(ERepositoryObjectType.METADATA_VALIDATION_RULES)) {
                        label.setVisible(true);
                    } else {
                        label.setVisible(false);
                    }
                }
            }
        });
        repositoryTreeViewer.addDoubleClickListener(new IDoubleClickListener() {

            @Override
            public void doubleClick(DoubleClickEvent event) {
                if (getButton(IDialogConstants.OK_ID).isEnabled()) {
                    okPressed();
                }
            }
        });

        TimeMeasure.step(RepositoryReviewDialog.class.getSimpleName(), "finished createDialogArea..."); //$NON-NLS-1$
        TimeMeasure.end(RepositoryReviewDialog.class.getSimpleName());
        TimeMeasure.display = false;
        TimeMeasure.displaySteps = false;
        TimeMeasure.measureActive = false;

        return container;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        return createDialogArea(parent, null);
    }

    public void addFilter(ViewerFilter filter) {
        if (filter != null) {
            getRepositoryTreeViewer().addFilter(filter);
        }
    }

    public void addFilter(ViewerFilter[] filters) {
        if (filters != null) {
            for (ViewerFilter filter : filters) {
                addFilter(filter);
            }
        }
    }

    protected boolean isSelectionValid(SelectionChangedEvent event) {
        boolean highlightOKButton = true;
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        if (selection == null || selection.size() != 1) {
            highlightOKButton = false;
        } else {
            RepositoryNode node = (RepositoryNode) selection.getFirstElement();

            if (filterReferenceNode && !ProjectManager.getInstance().isInCurrentMainProject(node)) {
                highlightOKButton = false;
            } else {
                if (node.getType() != ENodeType.REPOSITORY_ELEMENT) {
                    highlightOKButton = false;
                }
                // else if (t == ERepositoryObjectType.SERVICESOPERATION) {
                // return highlightOKButton;
                // }
                else if (!typeProcessor.isSelectionValid(node)) {
                    highlightOKButton = false;
                }
            }
        }
        return highlightOKButton;
    }

    /**
     * DOC bqian Comment method "createFilterField".
     *
     * @param container
     */
    private void createFilterField(Composite container) {

        if (type != ERepositoryObjectType.PROCESS && !showFilterText) {
            return;
        }

        // create text filter
        Label label = new Label(container, SWT.NONE);
        label.setText(Messages.getString("RepositoryReviewDialog.jobNameFormat")); //$NON-NLS-1$
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        final Text text = new Text(container, SWT.BORDER);
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        text.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                String pattern = text.getText();
                pattern = pattern.replace("*", ".*"); //$NON-NLS-1$ //$NON-NLS-2$
                pattern = pattern.replace("?", "."); //$NON-NLS-1$ //$NON-NLS-2$
                pattern = "(?i)" + pattern + ".*"; //$NON-NLS-1$ //$NON-NLS-2$
                textFilter.setText(pattern);
                getRepositoryTreeViewer().refresh();
                getRepositoryTreeViewer().expandAll();
                // repositoryView.selectFirstOne();
            }
        });
    }

    public void setSelectedNodeName(String selectionNode) {
        this.selectedNodeName = selectionNode;
    }

    public void setSelectedNodeName(ERepositoryObjectType selectionType, String selectionNode, boolean isSelectionId) {
        setSelectedNodeName(selectionNode);
        this.selectionType = selectionType;
        this.isSelectionId = isSelectionId;
    }

    private void selectNode() {
        /*
         * Make sure expand all. Just notice it here, because have been expand before.
         */
        // getRepositoryTreeViewer().expandAll();
        RepositoryNode root = (RepositoryNode) getRepositoryTreeViewer().getInput();
        selectNode(root, this.selectionType, this.selectedNodeName, this.isSelectionId);
    }

    private void selectNode(RepositoryNode root, ERepositoryObjectType selectionType, String idOrLabel, boolean isSelectionId) {
        if (idOrLabel == null) {
            return;
        }
        if (selectionType != null) {
            if (root.getContentType() != selectionType || root.getObjectType() != selectionType) {
                return;
            }
        }
        boolean valid = false;
        if (isSelectionId) {
            IRepositoryViewObject object = root.getObject();
            if (object != null && idOrLabel.equals(object.getId())) {
                valid = true;
            }
        } else if (idOrLabel.equals(root.getProperties(EProperties.LABEL))) {
            valid = true;
        }
        if (valid) {
            getRepositoryTreeViewer().setSelection(new StructuredSelection(root), true);
        } else if (root.hasChildren()) {
            for (IRepositoryNode child : root.getChildren()) {
                selectNode((RepositoryNode) child, selectionType, idOrLabel, isSelectionId);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        getButton(IDialogConstants.OK_ID).setEnabled(false);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        IStructuredSelection selection = (IStructuredSelection) getRepositoryTreeViewer().getSelection();
        result = (RepositoryNode) selection.getFirstElement();
        super.okPressed();
    }

    public RepositoryNode getResult() {
        return result;
    }

    public void setJobIDList(List<String> jobIDList) {
        if (this.typeProcessor instanceof JobTypeProcessor) {
            ((JobTypeProcessor) this.typeProcessor).setJobIDList(jobIDList);
        }
    }

    public void setFilterReferenceNode(boolean filterRefNode) {
        this.filterReferenceNode = filterRefNode;
    }
}

/**
 * bqian class global comment. Detailled comment
 */
class ViewerTextFilter extends ViewerFilter {

    private String text = null;

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (text == null || text.equals("")) { //$NON-NLS-1$
            return true;
        }
        RepositoryNode node = (RepositoryNode) element;
        ENodeType nodeType = node.getType();
        if (nodeType == ENodeType.STABLE_SYSTEM_FOLDER || nodeType == ENodeType.SYSTEM_FOLDER) {
            return true;
        }
        if (nodeType != ENodeType.REPOSITORY_ELEMENT) {
            List<IRepositoryNode> children = node.getChildren();
            if (children.isEmpty()) {
                return false;
            }
            for (IRepositoryNode child : children) {
                if (select(viewer, null, child)) {
                    return true;
                }
            }

            return false;
        }

        final IRepositoryViewObject object = node.getObject();
        if (object != null) {
            String name = object.getProperty().getLabel();
            if (name != null) {
                return name.matches(text);
            }

        }
        return true; // always
    }
}

/**
 * wchen class global comment. Detailled comment
 */
class DatabaseTypeFilter extends ViewerFilter {

    private String[] filterItems;

    public DatabaseTypeFilter(String[] filterItems) {
        this.filterItems = filterItems;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object,
     * java.lang.Object)
     */
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        return isSupportNode((RepositoryNode) element, filterItems);
    }

    private boolean isSupportNode(IRepositoryNode node, String[] items) {
        if (filterItems == null) {
            return true;
        }
        List<String> asList = Arrays.asList(items);
        if (node.getObject() == null) {
            if (node.getType() == ENodeType.REFERENCED_PROJECT || node.getType() == ENodeType.SYSTEM_FOLDER) {
                return true;
            }
            if (node.getContentType() != null && node.getContentType().equals(ERepositoryObjectType.METADATA)) {
                return true;
            }
        } else {
            Item item = node.getObject().getProperty().getItem();
            if (item instanceof DatabaseConnectionItem) {
                DatabaseConnectionItem connItem = (DatabaseConnectionItem) item;
                DatabaseConnection connection = (DatabaseConnection) connItem.getConnection();
                if (connection != null) {
                    String databaseType = connection.getDatabaseType();
                    if (databaseType.equals(EDatabaseTypeName.ORACLEFORSID.getDisplayName())) {
                        databaseType = EDatabaseTypeName.ORACLEFORSID.getXmlName();
                    } else if (databaseType.equals(EDatabaseTypeName.ORACLESN.getDisplayName())) {
                        databaseType = EDatabaseTypeName.ORACLESN.getXmlName();
                    } else if (databaseType.equals(EDatabaseTypeName.ORACLE_OCI.getDisplayName())) {
                        databaseType = EDatabaseTypeName.ORACLE_OCI.getXmlName();
                    } else if (databaseType.equals(EDatabaseTypeName.MSSQL.getDisplayName())) {
                        databaseType = EDatabaseTypeName.MSSQL.getXmlName(); // for component
                    } else if (databaseType.equals(EDatabaseTypeName.REDSHIFT_SSO.getDisplayName())) {
                        databaseType = EDatabaseTypeName.REDSHIFT_SSO.getXmlName(); // for component
                    } else if (databaseType.equals(EDatabaseTypeName.GENERAL_JDBC.getProduct())
                            && !databaseType.equals(connection.getProductId())) {
                        if (GlobalServiceRegister.getDefault().isServiceRegistered(IGenericWizardService.class)) {
                            IGenericWizardService service = GlobalServiceRegister.getDefault()
                                    .getService(IGenericWizardService.class);
                            if (service != null && service.getIfAdditionalJDBCDBType(connection.getProductId())) {
                                databaseType = connection.getProductId();
                            } else {
                                databaseType = EDatabaseTypeName.getTypeFromDbType(databaseType).getProduct();
                            }
                        }
                    } else {
                        databaseType = EDatabaseTypeName.getTypeFromDbType(databaseType).getProduct();
                    }

                    if (asList.contains(databaseType)) {
                        return true;
                    }
                }
            } else if (item instanceof FolderItem) {
                return true;
            }
        }
        return false;
    }
}

/**
 * filter for JDBC type, separate JDBC/ additional jdbc (Delta Lake) DOC jding class global comment. Detailled comment
 */
class DatabaseJDBCFilter extends ViewerFilter {

    private IElement elem;

    public void setElem(IElement elem) {
        this.elem = elem;
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        RepositoryNode node = (RepositoryNode) element;
        if (elem == null || node.getObject() == null || node.getObject().getProperty() == null
                || node.getObject().getProperty().getItem() == null) {
            return true;
        }
        Item item = node.getObject().getProperty().getItem();
        if (item instanceof DatabaseConnectionItem) {
            DatabaseConnectionItem connItem = (DatabaseConnectionItem) item;
            DatabaseConnection connection = (DatabaseConnection) connItem.getConnection();
            String databaseType = connection.getDatabaseType();
            if (!databaseType.equals(EDatabaseTypeName.GENERAL_JDBC.getProduct())) {
                return true;
            }

            if (GlobalServiceRegister.getDefault().isServiceRegistered(IGenericWizardService.class)) {
                IGenericWizardService service = GlobalServiceRegister.getDefault().getService(IGenericWizardService.class);
                if (service != null) {
                    String databseNameByNode = service.getDatabseNameByNode(elem);
                    if (StringUtils.isNotBlank(databseNameByNode) && !databseNameByNode.equals(connection.getProductId())) {
                        return false;
                    }
                }
            }

        }

        return true;
    }
}
