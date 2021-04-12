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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ITDQRepositoryService;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.model.bridge.ReponsitoryContextBridge;

/**
 * DOC klliu class global comment. Detailled comment
 */
public class ImportRulesFromRepository implements SelectionListener {

    /**
     * DOC yyi ImportRulesFromRepository class global comment. Detailled comment
     */
    private class PaserRuleSelectionDialog extends CheckedTreeSelectionDialog {

        private boolean isOverride;

        private TableViewer rulesTable;

        public PaserRuleSelectionDialog(Shell parent, ILabelProvider labelProvider, ITreeContentProvider contentProvider) {
            super(parent, labelProvider, contentProvider);
            addFilter(new PaserRulesFilter());
            setTitle(Messages.getString("ImportRulesFromRepository.dialog")); //$NON-NLS-1$
            setMessage(Messages.getString("ImportRulesFromRepository.title")); //$NON-NLS-1$
            isOverride = false;
        }

        private Composite createCheckerArea(Composite parent) {
            Composite checker = new Composite(parent, SWT.NONE);
            GridLayout innerLayout = new GridLayout();
            innerLayout.numColumns = 1;
            checker.setLayout(innerLayout);
            checker.setFont(parent.getFont());
            final Button check = new Button(checker, SWT.CHECK);
            check.setText(Messages.getString("ImportRulesFromRepository.override")); //$NON-NLS-1$
            check.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    isOverride = check.getSelection();
                }
            });
            return checker;
        }

        @Override
        protected Control createDialogArea(Composite parent) {
            SashForm form = new SashForm(parent, SWT.SMOOTH | SWT.VERTICAL | SWT.FILL);
            GridData data = new GridData(SWT.FILL, SWT.FILL, true, true, 0, 0);
            form.setLayoutData(data);

            Composite composite = (Composite) super.createDialogArea(form);
            createSelectRulesTable(form);
            createCheckerArea(composite);
            form.setWeights(new int[] { 3, 1 });
            return composite;
        }

        private void createSelectRulesTable(Composite parent) {
            Composite composite = new Composite(parent, SWT.None);
            GridLayout layout = new GridLayout();
            layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
            composite.setLayout(layout);

            rulesTable = new TableViewer(composite, SWT.BORDER);
            Table table = rulesTable.getTable();
            TableColumn c1 = new TableColumn(table, SWT.NULL);
            c1.setText(Messages.getString("PaserRuleSelectionDialog.RULE_NAME")); //$NON-NLS-1$
            TableColumn c2 = new TableColumn(table, SWT.NULL);
            c2.setText(Messages.getString("PaserRuleSelectionDialog.RULE_TYPE")); //$NON-NLS-1$
            TableColumn c3 = new TableColumn(table, SWT.NULL);
            c3.setText(Messages.getString("PaserRuleSelectionDialog.RULE_VALUE")); //$NON-NLS-1$
            table.setLinesVisible(true);
            table.setHeaderVisible(true);
            TableLayout tableLayout = new TableLayout();
            for (int i = 0; i < 3; i++) {
                tableLayout.addColumnData(new ColumnWeightData(1, 50, true));
            }
            table.setLayout(tableLayout);

            GridData data = new GridData(SWT.FILL, SWT.FILL, true, true, 0, 0);
            table.setLayoutData(data);

            rulesTable.setContentProvider(ArrayContentProvider.getInstance());
            rulesTable.setLabelProvider(new RulesTableLabelProvider());
        }

        public boolean isOverrideRules() {
            return isOverride;
        }

        @Override
        protected void updateOKStatus() {
            super.updateOKStatus();
            computeResult();
            rulesTable.setInput(getRulesFromFiles(getResult()));
        }

    }

    private class PaserRulesFilter extends ViewerFilter {

        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            if (element instanceof IFolder) {
                IFolder folder = (IFolder) element;
                if (folder.getName().startsWith(".")) { //$NON-NLS-1$
                    return false;
                }
            }
            if (element instanceof IFile) {
                IFile file = (IFile) element;
                if (!"rules".equals(file.getFileExtension())) { //$NON-NLS-1$
                    return false;
                }
            }
            return true;
        }

    }

    private class RuleSelectionValidator implements ISelectionStatusValidator {

        @Override
        public IStatus validate(Object[] selection) {
            Status stat = new Status(IStatus.OK, getClass().getName(), ""); //$NON-NLS-1$
            List<Map<String, String>> parsers = getRulesFromFiles(selection);
            for (Map<String, String> rule : parsers) {
                if (containsRule(rule)) {
                    stat = new Status(IStatus.WARNING, getClass().getName(),
                            Messages.getString("ImportRulesFromRepository.validator")); //$NON-NLS-1$
                    break;
                }
            }
            return stat;
        }
    }

    private class RulesTableLabelProvider extends LabelProvider implements ITableLabelProvider {

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            Image warn = null;
            Map<String, String> rule = (HashMap<String, String>) element;
            if (0 == columnIndex && containsRule(rule)) {
                warn = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK);
            }
            return warn;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            Map<String, String> rule = (HashMap<String, String>) element;
            String result = null;
            switch (columnIndex) {
            case 0: // name
                result = rule.get(RULE_NAME);
                break;
            case 1: // type
                result = rule.get(RULE_TYPE);
                break;
            case 2: // value
                result = rule.get(RULE_VALUE);
                break;
            }
            return result;
        }
    }

    private GenerateGrammarController controller;

    private Node node;

    private List<Map<String, String>> params;

    private PaserRuleSelectionDialog rulesSelectionDialog;

    private ITDQRepositoryService tdqRepositoryService;

    private Object[] selectObjects;

    private static final String PARSER_PATH = "TDQ_Libraries/Rules/Parser"; //$NON-NLS-1$

    private static final String RULE_NAME = "RULE_NAME"; //$NON-NLS-1$

    private static final String RULE_TYPE = "RULE_TYPE"; //$NON-NLS-1$

    private static final String RULE_VALUE = "RULE_VALUE"; //$NON-NLS-1$

    private static final String RULE_TABLE = "RULE_TABLE"; //$NON-NLS-1$

    /**
     * DOC klliu ImportRulesFromRepository constructor comment.
     *
     * @param generateGrammarController
     */
    public ImportRulesFromRepository(GenerateGrammarController generateGrammarController) {
        this.controller = generateGrammarController;
        this.node = (Node) generateGrammarController.elem;
    }

    protected boolean containsRule(Map<String, String> rule) {
        return getRuleNames().contains(rule.get(RULE_NAME));
    }

    private List<String> getRuleNames() {
        List<String> names = new ArrayList<String>();
        for (Map<String, String> rule : params) {
            names.add(rule.get(RULE_NAME));
        }
        return names;
    }

    protected PaserRuleSelectionDialog getRulesDialog() {
        if (null == rulesSelectionDialog) {
            rulesSelectionDialog = new PaserRuleSelectionDialog(null,
                    WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider(), new WorkbenchContentProvider());
            rulesSelectionDialog.addFilter(new PaserRulesFilter());
            rulesSelectionDialog.setValidator(new RuleSelectionValidator());
            rulesSelectionDialog.setContainerMode(true);
        }
        return rulesSelectionDialog;
    }

    private IFolder getRulesFolder() {
        if (ReponsitoryContextBridge.getRootProject() != null) {
            return ReponsitoryContextBridge.getRootProject().getFolder(new Path(PARSER_PATH));
        }
        return null;
    }

    private List<Map<String, String>> getRulesFromFiles(Object[] files) {
        return getTdqRepositoryService().getPaserRulesFromResources(files);
    }

    private ITDQRepositoryService getTdqRepositoryService() {
        if (null == tdqRepositoryService) {
            if (GlobalServiceRegister.getDefault().isServiceRegistered(ITDQRepositoryService.class)) {
                tdqRepositoryService = (ITDQRepositoryService) GlobalServiceRegister.getDefault().getService(
                        ITDQRepositoryService.class);
            }
        }
        return tdqRepositoryService;
    }

    public void mergeRules(List<Map<String, String>> params, List<Map<String, String>> rules) {
        for (Iterator<Map<String, String>> iterator = rules.iterator(); iterator.hasNext();) {
            Map<String, String> rule = iterator.next();
            boolean replacedRule = false;
            for (Map<String, String> param : params) {
                if (rule.get(RULE_NAME).equals(param.get(RULE_NAME))) {
                    param.putAll(rule);
                    replacedRule = true;
                }
            }
            if (replacedRule) {
                iterator.remove();
            }
        }
    }

    private Object[] selectRules(IFolder rulesFolder) {
        CheckedTreeSelectionDialog rsd = getRulesDialog();
        rsd.setInput(rulesFolder);
        if (selectObjects != null) {
            rsd.setInitialSelections(selectObjects);
        }
        rsd.open();
        selectObjects = rsd.getResult();
        return selectObjects;
    }

    public void updateParameters(Object propValue) {
        PropertyChangeCommand cmd = new PropertyChangeCommand(node, RULE_TABLE, propValue);
        cmd.setUpdate(true);
        controller.executeCommand(cmd);
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        this.params = (List<Map<String, String>>) node.getElementParameter(RULE_TABLE).getValue();
        List<Map<String, String>> params = new ArrayList<Map<String, String>>();
        params.addAll(this.params);

        if (getTdqRepositoryService() != null) {
            Object[] selectRules = selectRules(getRulesFolder());
            if (selectRules != null) {
                List<Map<String, String>> rules = getRulesFromFiles(selectRules);
                if (getRulesDialog().isOverrideRules()) {
                    mergeRules(params, rules);
                }
                params.addAll(rules);
                updateParameters(params);
            }
        }
    }
}
