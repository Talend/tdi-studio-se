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

import java.util.Map;

import org.apache.commons.collections.BidiMap;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.utils.workbench.extensions.ExtensionImplementationProvider;
import org.talend.commons.utils.workbench.extensions.ExtensionPointLimiterImpl;
import org.talend.commons.utils.workbench.extensions.IExtensionPointLimiter;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.Element;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.IMultiPageTalendEditor;
import org.talend.designer.runprocess.RunProcessContext;
import org.talend.designer.runprocess.i18n.Messages;
import org.talend.designer.runprocess.ui.views.DefaultProcessViewHelper;
import org.talend.designer.runprocess.ui.views.IProcessViewHelper;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class TargetExecComposite extends ScrolledComposite implements IDynamicProperty {

    private IProcessViewHelper processViewHelper;

    private JobJvmComposite jobComposite;

    private static ProcessManager manager = ProcessManager.getInstance();

    /**
     * DOC Administrator TargetExecComposite constructor comment.
     *
     * @param parent
     * @param style
     */
    public TargetExecComposite(Composite parent, int style) {
        super(parent, style);
        // TODO Auto-generated constructor stub
        setExpandHorizontal(true);
        setExpandVertical(true);
        IExtensionPointLimiter extensionPointLimiter = new ExtensionPointLimiterImpl(
                "org.talend.designer.runprocess.runprocess_view_helper", "runprocess_view_helper");
        IProcessViewHelper processViewHelperPrm = ExtensionImplementationProvider.getSingleInstance(extensionPointLimiter, null);
        if (processViewHelperPrm == null) {
            processViewHelperPrm = new DefaultProcessViewHelper();
        }

        setProcessViewHelper(processViewHelperPrm);

        FormData layoutData = new FormData();
        layoutData.left = new FormAttachment(0, 0);
        layoutData.right = new FormAttachment(100, 0);
        layoutData.top = new FormAttachment(0, 0);
        layoutData.bottom = new FormAttachment(100, 0);
        setLayoutData(layoutData);
        setLayout(new FillLayout());
        final Composite panel = new Composite(this, SWT.NONE);
        setContent(panel);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        panel.setLayout(layout);
        panel.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED));

        jobComposite = this.processViewHelper.getProcessComposite(panel);

        // CSS
        CoreUIPlugin.setCSSClass(this, this.getClass().getSimpleName());
    }

    protected Composite createTargetExecutionComposite(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);

        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        StyledText text = new StyledText(composite, SWT.NONE);
        text.setText(Messages.getString("ProcessComposite.targetExecutionTabTooltipAvailable")); //$NON-NLS-1$
        text.setWordWrap(true);
        text.setEditable(false);
        text.setLayoutData(new GridData(GridData.FILL_BOTH));

        return composite;
    }

    public void setProcessViewHelper(IProcessViewHelper processViewHelper) {
        this.processViewHelper = processViewHelper;
    }

    public void setProcessContext(RunProcessContext processContext) {
        jobComposite.setProcessContext(processContext);
        if (processContext == null) {
            // this.processContext.setMonitorTrace(traceBtn.getSelection());
            manager.setBooleanTrace(false);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#getComposite()
     */
    @Override
    public Composite getComposite() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#getCurRowSize()
     */
    @Override
    public int getCurRowSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#getElement()
     */
    @Override
    public Element getElement() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#getHashCurControls()
     */
    @Override
    public BidiMap getHashCurControls() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#getPart()
     */
    @Override
    public IMultiPageTalendEditor getPart() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @seeorg.talend.core.properties.tab.IDynamicProperty#getRepositoryAliasName(org.talend.core.model.properties.
     * ConnectionItem)
     */
    @Override
    public String getRepositoryAliasName(ConnectionItem connectionItem) {
        // TODO Auto-generated method stub
        return null;
    }

    /* 16969 */
    // /*
    // * (non-Javadoc)
    // *
    // * @see org.talend.core.properties.tab.IDynamicProperty#getRepositoryConnectionItemMap()
    // */
    // public Map<String, ConnectionItem> getRepositoryConnectionItemMap() {
    // // TODO Auto-generated method stub
    // return null;
    // }

    // /*
    // * (non-Javadoc)
    // *
    // * @see org.talend.core.properties.tab.IDynamicProperty#getRepositoryQueryStoreMap()
    // */
    // public Map<String, Query> getRepositoryQueryStoreMap() {
    // // TODO Auto-generated method stub
    // return null;
    // }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#getRepositoryTableMap()
     */
    // public Map<String, IMetadataTable> getRepositoryTableMap() {
    // // TODO Auto-generated method stub
    // return null;
    // }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#getSection()
     */
    @Override
    public EComponentCategory getSection() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#getTableIdAndDbSchemaMap()
     */
    @Override
    public Map<String, String> getTableIdAndDbSchemaMap() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#getTableIdAndDbTypeMap()
     */
    @Override
    public Map<String, String> getTableIdAndDbTypeMap() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#refresh()
     */
    @Override
    public void refresh() {
        // TODO Auto-generated method stub
        if (!isDisposed()) {
            getParent().layout();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#setCurRowSize(int)
     */
    @Override
    public void setCurRowSize(int i) {
        // TODO Auto-generated method stub

    }

}
