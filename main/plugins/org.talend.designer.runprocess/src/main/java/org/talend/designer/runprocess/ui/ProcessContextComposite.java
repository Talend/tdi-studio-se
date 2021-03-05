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
package org.talend.designer.runprocess.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.talend.core.model.context.ContextUtils;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextListener;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.context.nattableTree.ContextNatTableUtils;
import org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.designer.runprocess.i18n.Messages;

/**
 * Manages the context of a process. <br/>
 *
 * $Id$
 *
 */
public class ProcessContextComposite extends Composite {

    /**
     *
     */
    private static final int COLUMN_WIDTH = 120;

    /**
     *
     */
    private static final int HINT_HEIGHT = 250;

    /** Context combo viewer. */
    private static ComboViewer contextComboViewer;

    /** Context table viewer. */
    private static TableViewer contextTableViewer;

    private IProcess2 process;

    private IContextListener contextListener;

    private ProcessManager rubjobManager;

    /**
     * Constructs a new ProcessContextComposite.
     *
     * @param parent Parent composite.
     * @param style Style bits.
     */
    public ProcessContextComposite(Composite parent, int style) {
        super(parent, style);

        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        setLayout(layout);

        // Group contextGroup = new Group(this, SWT.NONE);
        // contextGroup.setText(Messages.getString("ProcessComposite.contextGroup")); //$NON-NLS-1$
        Composite contextGroup = this;

        layout = new GridLayout();
        contextGroup.setLayout(layout);
        contextGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

        contextComboViewer = new ComboViewer(contextGroup, SWT.BORDER | SWT.READ_ONLY);
        contextComboViewer.setContentProvider(ArrayContentProvider.getInstance());
        contextComboViewer.setLabelProvider(new ContextNameLabelProvider());
        contextComboViewer.getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        contextComboViewer.getControl().setEnabled(false);

        Table contextTable = new Table(contextGroup, SWT.BORDER);
        contextTable.setLinesVisible(true);
        contextTable.setHeaderVisible(true);

        TableColumn colName = new TableColumn(contextTable, SWT.NONE);
        colName.setText(Messages.getString("ProcessComposite.nameCol")); //$NON-NLS-1$
        colName.setWidth(COLUMN_WIDTH);
        TableColumn colValue = new TableColumn(contextTable, SWT.NONE);
        colValue.setText(Messages.getString("ProcessComposite.valueCol")); //$NON-NLS-1$
        colValue.setWidth(COLUMN_WIDTH);

        contextTableViewer = new TableViewer(contextTable);
        contextTableViewer.setContentProvider(ArrayContentProvider.getInstance());
        contextTableViewer.setLabelProvider(new ContextParameterLabelProvider());
        GridData data = new GridData(GridData.FILL_BOTH);
        data.heightHint = HINT_HEIGHT;
        contextTableViewer.getControl().setLayoutData(data);

        // Add listeners
        rubjobManager = ProcessManager.getInstance();

        // set CSS class
        CoreUIPlugin.setCSSClass(this, ProcessContextComposite.class.getSimpleName());
    }

    ISelectionChangedListener contextComboListener = new ISelectionChangedListener() {

        @Override
        public void selectionChanged(final SelectionChangedEvent event) {
            runSelectionChange(event);
        }
    };

    /**
     * zwxue Comment method "runSelectionChange".
     * for memory run can call this method.
     */
    public void runSelectionChange(final SelectionChangedEvent event) {
		Object input = null;
        if (!event.getSelection().isEmpty()) {
            IContext selectedContext = (IContext) ((IStructuredSelection) event.getSelection()).getFirstElement();
            input = selectedContext.getContextParameterList();
            process.setLastRunContext(selectedContext);
            rubjobManager.setSelectContext(selectedContext);
            // see bug 0003924
            processNeedGenCode(process);
        }
        contextTableViewer.setInput(input);
	}

    /**
     * bqian Comment method "processNeedGenCode".
     */
    private static void processNeedGenCode(IProcess process) {
        if (process instanceof IProcess2) {
            ((IProcess2) process).setNeedRegenerateCode(true);
        }
    }

    /**
     * Set the process on wich we are selecting context.
     *
     * @param process The process.
     */
    public void setProcess(final IProcess2 process) {
        // Select the first context
        if (process != null) {
            if (process.equals(this.process)) {
                // check the ContextParameter is same or not , avoid to set two times the same object .
                if (checkIsSameContextParameter()) {
                    return;
                }
            }
            if (this.process != null) {
                // remove old listeners
                this.process.getContextManager().removeContextListener(contextListener);
                contextComboViewer.removeSelectionChangedListener(contextComboListener);
            }

            this.process = process;
            contextComboViewer.getControl().setEnabled(true);

            getInformationsFromContextManager(process.getContextManager());

            contextListener = new IContextListener() {

                @Override
                public void contextsChanged() {
                    if (!ProcessContextComposite.this.isDisposed()) {
                        getInformationsFromContextManager(process.getContextManager());
                    }
                    // contextComboViewer.refresh();

                }
            };

            this.process.getContextManager().addContextListener(contextListener);
            contextComboViewer.addSelectionChangedListener(contextComboListener);
        } else {
            if (this.process == null) {
                // there is no process already
                return;
            }
            if (this.process != null) {
                this.process.getContextManager().removeContextListener(contextListener);
                this.process = null;
                contextListener = null;
            }
            contextComboViewer.getControl().setEnabled(false);
            contextComboViewer.removeSelectionChangedListener(contextComboListener);
            contextComboViewer.setInput(null);
            contextTableViewer.setInput(null);
        }
    }

    protected void getInformationsFromContextManager(IContextManager contextManager) {
        List<IContext> internalContextList = new ArrayList<IContext>();
        IContext newSelectedCopiedContext = null;

        // if (!contextComboViewer.getSelection().isEmpty()) {
        // oldSelectedCopiedContext = (IContext) ((StructuredSelection)
        // contextComboViewer.getSelection()).getFirstElement();
        // }

        if (process != null && process.getLastRunContext() != null) {
            for (IContext context : contextManager.getListContext()) {
                IContext copiedContext = context.clone();
                internalContextList.add(copiedContext);
                if (process.getLastRunContext().getName().equals(context.getName())) {
                    newSelectedCopiedContext = copiedContext;
                }

            }
        } else {
            for (IContext context : contextManager.getListContext()) {
                IContext copiedContext = context.clone();
                internalContextList.add(copiedContext);
                if (contextManager.getDefaultContext().equals(context)) {
                    newSelectedCopiedContext = copiedContext;
                }

            }
        }
        Collections.sort(internalContextList, new ContextCompare());
        contextComboViewer.setInput(internalContextList);

        ProcessManager processManager = ProcessManager.getInstance();

        if (newSelectedCopiedContext != null) {
            setContextComboSelection(new StructuredSelection(newSelectedCopiedContext));
            processManager.setSelectContext(newSelectedCopiedContext);
            contextTableViewer.setInput(newSelectedCopiedContext.getContextParameterList());
        } else {
            IContext element = internalContextList.get(0);
            processManager.setSelectContext(element);
            setContextComboSelection(new StructuredSelection(element));
            contextTableViewer.setInput(element.getContextParameterList());
        }

    }

    /*
     * check same ContextParameter or not.
     */
    private boolean checkIsSameContextParameter() {
        List<ContextItem> allContextItem = ContextUtils.getAllContextItem();
        for (IContext context : process.getContextManager().getListContext()) {
            for (IContextParameter param : context.getContextParameterList()) {
                if (allContextItem != null) {
                    ContextItem contextItem = ContextUtils.getContextItemById(allContextItem, param.getSource());
                    ContextType contextType = ContextUtils.getContextTypeByName(contextItem, context.getName(), true);
                    ContextParameterType contextParameterType = ContextUtils.getContextParameterTypeByName(contextType,
                            param.getName());
                    if (!ContextUtils.samePropertiesForContextParameter(param, contextParameterType)) {
                        return false;
                    }
                    // if don't open the job to run it(not use the "Detect and update all jobs"), will show
                    // UpdateDetectionDialog to update the context ,after updated the item, the contextComboViewer still
                    // set the old one , so need refresh.
                    IContext runJobViewContext = getSelectedContext();
                    if (runJobViewContext != null) {
                        for (IContextParameter tempContext : runJobViewContext.getContextParameterList()) {
                            if (tempContext.getName().equals(contextParameterType.getName())
                                    && !ContextUtils.samePropertiesForContextParameter(tempContext, contextParameterType)) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * DOC hshen ContextCompare class global comment. Detailled comment
     */
    private class ContextCompare implements java.util.Comparator<IContext> {

        /*
         * (non-Javadoc)
         *
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(IContext o1, IContext o2) {
            String name1 = o1.getName().toUpperCase();
            String name2 = o2.getName().toUpperCase();
            return name1.compareTo(name2);
        }

    }

    public void setContextComboSelection(StructuredSelection selection) {
        contextComboViewer.removeSelectionChangedListener(contextComboListener);
        contextComboViewer.setSelection(selection);
        contextComboViewer.addSelectionChangedListener(contextComboListener);
    }

    public IContext getSelectedContext() {
        return (IContext) ((IStructuredSelection) contextComboViewer.getSelection()).getFirstElement();
    }

    public boolean promptConfirmLauch() {
        return promptConfirmLauch(getShell(), getSelectedContext(), process);
    }

    public static boolean promptConfirmLauch(Shell shell, IContext context, IProcess process) {
        boolean continueLaunch = true;

        int nbValues = 0;
        if (context == null) {
            throw new IllegalArgumentException("Context is null"); //$NON-NLS-1$
        }
        // Prompt for context values ?
        for (IContextParameter parameter : context.getContextParameterList()) {
            if (parameter.isPromptNeeded()) {
                nbValues++;
            }
        }
        if (nbValues > 0) {
            IContext contextCopy = context.clone();
            PromptDialog promptDialog = new PromptDialog(shell, contextCopy);
            if (promptDialog.open() == PromptDialog.OK) {
                for (IContextParameter param : context.getContextParameterList()) {
                    boolean found = false;
                    IContextParameter paramCopy = null;
                    for (int i = 0; i < contextCopy.getContextParameterList().size() & !found; i++) {
                        paramCopy = contextCopy.getContextParameterList().get(i);
                        if (param.getName().equals(paramCopy.getName())) {
                            // param.setValueList(paramCopy.getValueList());
                            param.setInternalValue(paramCopy.getValue());
                            found = true;
                        }
                    }
                }
                contextComboViewer.refresh();
                contextTableViewer.refresh();
                processNeedGenCode(process);
            } else {
                continueLaunch = false;
            }
        } else {
            if (context.isConfirmationNeeded()) {
                continueLaunch = MessageDialog.openQuestion(shell, Messages.getString("ProcessComposite.confirmTitle"), //$NON-NLS-1$
                        Messages.getString("ProcessComposite.confirmText", context.getName())); //$NON-NLS-1$
            }

            updateDefaultValueForListTypeParameter(context.getContextParameterList());
        }
        return continueLaunch;
    }

    public Object getContextComboInput(){
    	return contextComboViewer.getInput();
    }
    public ComboViewer getContextComboViewer(){
    	return contextComboViewer;
    }

    /**
     * Set the first item of value list for the default value if this parameter does not need to prompt.
     *
     * @param contextParameterList
     */
    private static void updateDefaultValueForListTypeParameter(List<IContextParameter> contextParameterList) {
        for (IContextParameter contextParameter : contextParameterList) {
            String[] list = contextParameter.getValueList();
            if (list == null) {
                continue;
            }
            if (list.length == 0) {
                contextParameter.setInternalValue(""); //$NON-NLS-1$
            } else {
                contextParameter.setInternalValue(list[0]);
            }

        }
    }

    /**
     * LabelProvider for a context combo. <br/>
     *
     * $Id$
     *
     */
    private static class ContextNameLabelProvider extends LabelProvider {

        /**
         * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
         */
        @Override
        public String getText(final Object element) {
            IContext context = (IContext) element;
            return context.getName();
        }
    }

    /**
     * LabelProvider for a context table. <br/>
     *
     * $Id$
     *
     */
    private static class ContextParameterLabelProvider extends LabelProvider implements ITableLabelProvider {

        /**
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
         */
        @Override
        public String getColumnText(final Object element, final int columnIndex) {
            String text;
            IContextParameter parameter = (IContextParameter) element;
            switch (columnIndex) {
            case 0:
                text = parameter.getName();
                break;
            case 1:
                if (ContextParameterUtils.isPasswordType(parameter)) {
                    // see bug 0005661: In Run, Context password fields appear
                    // in plaintext
                    text = "****"; //$NON-NLS-1$
                } else {
                    String displayValue = ContextNatTableUtils.getSpecialTypeDisplayValue(parameter.getType(),
                            parameter.getValue());
                    if (displayValue != null) {
                        text = displayValue;
                    } else {
                        text = parameter.getValue();
                    }
                }
                break;
            default:
                text = super.getText(element);
            }
            return text;
        }

        /**
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
         */
        @Override
        public Image getColumnImage(final Object element, final int columnIndex) {
            return null;
        }
    }

}
