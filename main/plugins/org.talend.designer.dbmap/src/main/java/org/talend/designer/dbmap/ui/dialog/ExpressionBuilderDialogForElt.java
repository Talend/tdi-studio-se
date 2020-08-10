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
package org.talend.designer.dbmap.ui.dialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.runtime.model.expressionbuilder.Variable;
import org.talend.commons.runtime.xml.XmlUtil;
import org.talend.commons.ui.runtime.exception.RuntimeExceptionHandler;
import org.talend.commons.ui.runtime.expressionbuilder.IExpressionBuilderDialogController;
import org.talend.commons.ui.runtime.expressionbuilder.IExpressionDataBean;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.swt.proposal.ExtendedTextCellEditorWithProposal;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreatorColumn;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.general.Project;
import org.talend.core.model.process.INode;
import org.talend.designer.dbmap.i18n.Messages;
import org.talend.designer.dbmap.managers.MapperManager;
import org.talend.expressionbuilder.ExpressionFileOperation;
import org.talend.expressionbuilder.ExpressionPersistance;
import org.talend.expressionbuilder.model.CategoryManager;
import org.talend.repository.model.RepositoryConstants;
import org.xml.sax.SAXException;

public class ExpressionBuilderDialogForElt extends TrayDialog implements IExpressionBuilderDialogController {

    private static final int EXPORT_ID = IDialogConstants.CLIENT_ID + 22;

    private static final int IMPORT_ID = IDialogConstants.CLIENT_ID + 21;

    protected static ExpressionComposite expressionComposite;

    protected CategoryManager manager = new CategoryManager();

    protected final ExtendedTextCellEditorWithProposal dataBean;

    protected String defaultExpression = ""; //$NON-NLS-1$

    protected List<Variable> defaultVariables;

    private Composite container;

    private final INode component;

    private final String nodeStyle;

    private String expressionForTable = null;

    protected static boolean isESCClose = true;

    private MapperManager mapperManager;

    private TableViewerCreator tableViewerCreator;

    private TableViewerCreatorColumn column;

    /**
     * Create the dialog
     *
     * @param parentShell
     */
    public ExpressionBuilderDialogForElt(Shell parentShell, ExtendedTextCellEditorWithProposal dataBean,
            MapperManager mapperManager,
            TableViewerCreator tableViewerCreator, TableViewerCreatorColumn column) {
        super(parentShell);
        this.nodeStyle = parentShell.toString();

        setShellStyle(this.getShellStyle() | SWT.RESIZE | SWT.MAX);
        this.dataBean = dataBean;
        this.component = mapperManager.getComponent();
        this.mapperManager = mapperManager;
        this.tableViewerCreator = tableViewerCreator;
        this.column = column;
    }

    /**
     * Create contents of the dialog
     *
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        container = (Composite) super.createDialogArea(parent);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.makeColumnsEqualWidth = true;
        container.setLayout(gridLayout);

        final SashForm sashForm = new SashForm(container, SWT.NONE);
        sashForm.setOrientation(SWT.VERTICAL);

        final Composite upperComposite = new Composite(sashForm, SWT.NONE);
        upperComposite.setLayout(new FillLayout());

        final SashForm upperSashform = new SashForm(upperComposite, SWT.NONE);

        expressionComposite = new EltExpressionComposite(this, upperSashform, SWT.NONE, dataBean, mapperManager);
        expressionComposite.setExpression(defaultExpression, true);
        final GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        sashForm.setLayoutData(gridData);

        return container;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.Dialog#create()
     */
    @Override
    public void create() {
        super.create();
        addUndoOperationListener(container);
        isESCClose = true;
    }

    /**
     * yzhang ExpressionBuilderDialog class global comment. Detailled comment <br/>
     *
     * $Id: ExpressionBuilderDialog.java ä¸‹å�ˆ08:32:27 2007-9-13 +0000 (2007-9-13) yzhang $
     *
     */
    class UndoKeyListener extends KeyAdapter {

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.swt.events.KeyAdapter#keyPressed(org.eclipse.swt.events.KeyEvent)
         */
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.stateMask == SWT.CTRL && (e.keyCode == 'z' || e.keyCode == 'Z')) {
                getExpressionComposite().undoOperation();
            }
        }

    }

    /**
     * yzhang Comment method "addUndoOperationListener".
     *
     * @param composite
     */
    protected void addUndoOperationListener(Composite composite) {
        if (composite != null) {
            Control controls[] = composite.getChildren();
            for (Control control : controls) {
                if (control instanceof Composite) {
                    addUndoOperationListener((Composite) control);
                }
                control.addKeyListener(new UndoKeyListener());
            }
        }
    }

    /**
     * Create contents of the button bar
     *
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {

        ((GridLayout) parent.getLayout()).numColumns++;

        Composite buttons = new Composite(parent, SWT.NONE);
        buttons.setLayout(new GridLayout(2, false));

        final Button importButton = new Button(buttons, SWT.PUSH);
        importButton.setToolTipText(Messages.getString("ExpressionBuilderDialog.import"));//$NON-NLS-1$
        importButton.setImage(ImageProvider.getImage(EImage.IMPORT_ICON));

        final Button exportButton = new Button(buttons, SWT.PUSH);
        exportButton.setToolTipText(Messages.getString("ExpressionBuilderDialog.export"));//$NON-NLS-1$
        exportButton.setImage(ImageProvider.getImage(EImage.EXPORT_ICON));

        createButton(parent, IDialogConstants.OK_ID, Messages.getString("ExpressionBuilderDialog.ok.button"), true); //$NON-NLS-1$
        createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("ExpressionBuilderDialog.cancel.button"), false); //$NON-NLS-1$

        exportButton.addMouseListener(new MouseAdapter() {

            /*
             * (non-Javadoc)
             *
             * @see org.eclipse.swt.events.MouseAdapter#mouseUp(org.eclipse.swt.events.MouseEvent)
             */
            @Override
            public void mouseUp(MouseEvent e) {

                FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
                dialog.setFilterExtensions(new String[] { "*.xml" }); //$NON-NLS-1$

                String filePath = dialog.open();
                if (filePath != null) {
                    String expresionContent = expressionComposite.getExpression();
                    List<Variable> variables = new ArrayList<Variable>();
                    // variables = testComposite.getVariableList();
                    File file = new File(filePath);
                    ExpressionFileOperation operation = new ExpressionFileOperation();
                    try {
                        if (file != null) {
                            file.createNewFile();
                        }
                        operation.saveExpressionToFile(file, variables, expresionContent);
                    } catch (IOException e1) {
                        RuntimeExceptionHandler.process(e1);
                    } catch (ParserConfigurationException e1) {
                        RuntimeExceptionHandler.process(e1);
                    }
                }
            }
        });

        importButton.addMouseListener(new MouseAdapter() {

            /*
             * (non-Javadoc)
             *
             * @see org.eclipse.swt.events.MouseAdapter#mouseUp(org.eclipse.swt.events.MouseEvent)
             */
            @Override
            public void mouseUp(MouseEvent e) {
                FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
                dialog.setFilterExtensions(new String[] { "*.xml" }); //$NON-NLS-1$

                String filePath = dialog.open();
                if (filePath != null) {
                    File file = new File(filePath);
                    ExpressionFileOperation operation = new ExpressionFileOperation();
                    try {
                        List list = operation.importExpressionFromFile(file, getShell());
                        if (list != null && list.size() != 0) {
                            expressionComposite.setExpression((String) list.get(0), false);
                            list.remove(0);
                            if (list.size() > 0) {
                                // testComposite.addVariables(list);
                            }
                        }
                    } catch (IOException e1) {
                        RuntimeExceptionHandler.process(e1);
                    } catch (ParserConfigurationException e1) {
                        RuntimeExceptionHandler.process(e1);
                    } catch (SAXException e1) {
                        RuntimeExceptionHandler.process(e1);
                    }
                }
            }
        });

    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.getString("ExpressionBuilderDialog.expression.builder")); //$NON-NLS-1$
    }

    @Override
    protected Point getInitialSize() {
        return new Point(870, 500);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.Dialog#close()
     */
    @Override
    public boolean close() {
        if (isESCClose) {
            if (defaultExpression != null && !defaultExpression.equals(newExpression())) {
                boolean flag = MessageDialog.openConfirm(getParentShell(), Messages.getString("ExpressionBuilderDialog.message"),
                        Messages.getString("ExpressionBuilderDialog.Confirm"));
                if (!flag) {
                    return false;
                }
            }
        }
        return super.close();
    }

    protected String newExpression() {
        String expression = null;
        int startInx = nodeStyle.indexOf("-") + 2;//$NON-NLS-1$
        int endInx = nodeStyle.lastIndexOf("-") - 1;//$NON-NLS-1$
        String sub;
        if (endInx - startInx > 0) {
            sub = nodeStyle.substring(startInx, endInx);
        } else {
            sub = nodeStyle;
        }
        if (sub != null && sub.equals("tRowGenerator")) { //$NON-NLS-1$
            expression = expressionComposite.getReplaceExpression();
            expressionForTable = expression; // hywang add for 9225
        } else {
            expression = expressionComposite.getExpression();
            expressionForTable = expression;
        }
        return expression;
    }


    /**
     * Getter for expressionComposite.
     *
     * @return the expressionComposite
     */
    public static ExpressionComposite getExpressionComposite() {
        return expressionComposite;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        String expression = null;
        int startInx = nodeStyle.indexOf("-") + 2;//$NON-NLS-1$
        int endInx = nodeStyle.lastIndexOf("-") - 1;//$NON-NLS-1$
        String sub;
        if (endInx - startInx > 0) {
            sub = nodeStyle.substring(startInx, endInx);
        } else {
            sub = nodeStyle;
        }
        if (sub.equals("tRowGenerator")) { //$NON-NLS-1$
            expression = expressionComposite.getReplaceExpression().trim();
            expressionForTable = expression; // hywang add for 9225
        } else {
            expression = expressionComposite.getExpression().trim();
            expressionForTable = expression;
        }
        if (dataBean != null) {
            dataBean.setConsumerExpression(expression + " "); //$NON-NLS-1$
        }
        isESCClose = false;
        // to trigger refresh sql tab view
        int previousActivatedIndex = dataBean.getCommonTextEditor().getPreviousActivatedIndex();
        tableViewerCreator.getTableViewer()
                .editElement(this.tableViewerCreator.getTableViewer().getElementAt(previousActivatedIndex),
                column.getIndex());
        super.okPressed();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.expressionbuilder.ui.IExpressionBuilderDialogController#openDialog()
     */
    @Override
    public void openDialog(Object obj) {
        if (obj instanceof IExpressionDataBean) {

            List<Variable> vars = new ArrayList<Variable>();
            IExpressionDataBean bean = (IExpressionDataBean) obj;
            setDefaultExpression(bean.getExpression());

            ExpressionPersistance persistance = ExpressionPersistance.getInstance();
            persistance.setOwnerId(bean.getOwnerId());
            persistance.setPath(getExpressionStorePath());
            List<Variable> varList = new ArrayList<Variable>(persistance.loadExpression().getVariables());
            vars.addAll(varList);

            if (bean.getVariables() != null) {
                for (Variable var1 : bean.getVariables()) {
                    boolean needAdd = true;
                    for (Variable var2 : varList) {
                        if (var1.getName() != null && var1.getName().equals(var2.getName())) {
                            needAdd = false;
                            break;
                        }
                    }
                    if (var1.getName() != null && needAdd) {
                        vars.add(var1);
                    }
                }
            }
            addVariables(vars);
        }
        open();
        setBlockOnOpen(true);

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.expressionbuilder.ui.IExpressionBuilderDialogController#setDefaultExpression(java.lang.String)
     */
    @Override
    public void setDefaultExpression(String expression) {
        defaultExpression = expression;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.expressionbuilder.ui.IExpressionBuilderDialogController#setVariables(java.util.List)
     */
    @Override
    public void addVariables(List<Variable> variables) {
        defaultVariables = variables;
    }

    /**
     * yzhang Comment method "getExpressionStorePath".
     *
     * @return
     */
    protected String getExpressionStorePath() {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        RepositoryContext repositoryContext = (RepositoryContext) CorePlugin.getContext().getProperty(
                Context.REPOSITORY_CONTEXT_KEY);
        Project project = repositoryContext.getProject();
        IProject p = root.getProject(project.getTechnicalLabel());

        // put to the temp folder
        String projectPath = p.getLocation().toPortableString() + File.separator + RepositoryConstants.TEMP_DIRECTORY;

        String configurationPath = projectPath + File.separator + ExpressionPersistance.CONFIGURATION_FOLDER_NAME;
        configurationPath = getValidFolderPath(configurationPath, 1);
        File configurationFolder = new File(configurationPath);
        if (!configurationFolder.exists()) {
            configurationFolder.mkdir();
        }
        String expressionPath = configurationPath + File.separator + ExpressionPersistance.EXPRESSION_FOLDER_NAME;
        expressionPath = getValidFolderPath(expressionPath, 1);
        File expressionFolder = new File(expressionPath);
        if (!expressionFolder.exists()) {
            expressionFolder.mkdir();
        }

        String jobName = "";
        if (component != null) {
            jobName = component.getProcess().getName();
        } else {
            jobName = "tXMLMap";
        }
        expressionPath = expressionPath + File.separator + jobName + XmlUtil.FILE_XML_SUFFIX;
        return expressionPath;
    }

    public String getValidFolderPath(String path, int suffix) {
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            path = path + suffix;
            return getValidFolderPath(path, suffix);
        }
        return path;

    }

    @Override
    public String getExpressionForTable() {
        return this.expressionForTable;
    }

    public CategoryManager getManager() {
        return this.manager;
    }
}
