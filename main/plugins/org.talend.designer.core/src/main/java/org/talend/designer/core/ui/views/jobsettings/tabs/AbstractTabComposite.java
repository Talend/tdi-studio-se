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
package org.talend.designer.core.ui.views.jobsettings.tabs;

import java.text.SimpleDateFormat;
import java.util.Map;

import org.apache.commons.collections.BidiMap;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.Element;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;

/**
 * yzhang class global comment. Detailled comment
 */
public class AbstractTabComposite extends Composite implements IDynamicProperty {

    protected static final int NB_LINES = 4;

    protected TabbedPropertySheetWidgetFactory widgetFactory;

    protected IRepositoryViewObject repositoryObject;

    protected static final SimpleDateFormat FORMATTER = new SimpleDateFormat();

    /**
     * yzhang AbstractTabComposite constructor comment.
     *
     * @param parent
     * @param style
     */
    public AbstractTabComposite(Composite parent, int style, TabbedPropertySheetWidgetFactory widgetFactory,
            IRepositoryViewObject iRepositoryViewObject) {
        super(parent, style);
        this.widgetFactory = widgetFactory;
        this.repositoryObject = iRepositoryViewObject;
        //
        // final Item item = repositoryObject.getProperty().getItem();
        // IDesignerCoreService designerCoreService = CorePlugin.getDefault().getDesignerCoreService();
        // if (item instanceof ProcessItem) {
        // IProcess process = designerCoreService.getProcessFromProcessItem((ProcessItem) item);
        // process.getElementParameter(EParameterName.HEADER_ENABLED.getName());
        // }

    }

    public IEditorPart getEditor() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties.controllers.generator.IDynamicProperty#getComposite()
     */
    public Composite getComposite() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties.controllers.generator.IDynamicProperty#getCurRowSize()
     */
    public int getCurRowSize() {
        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties.controllers.generator.IDynamicProperty#getElement()
     */
    public Element getElement() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties.controllers.generator.IDynamicProperty#getHashCurControls()
     */
    public BidiMap getHashCurControls() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties.controllers.generator.IDynamicProperty#getPart()
     */
    public AbstractMultiPageTalendEditor getPart() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.generator.IDynamicProperty#getRepositoryAliasName(org
     * .talend.core.model.properties.ConnectionItem)
     */
    public String getRepositoryAliasName(ConnectionItem connectionItem) {
        return null;
    }

    /* 16969 */
    // /*
    // * (non-Javadoc)
    // *
    // * @see
    // *
    // org.talend.designer.core.ui.editor.properties.controllers.generator.IDynamicProperty#getRepositoryConnectionItemMap
    // * ()
    // */
    // public Map<String, ConnectionItem> getRepositoryConnectionItemMap() {
    // return null;
    // }

    // /*
    // * (non-Javadoc)
    // *
    // * @see
    // *
    // org.talend.designer.core.ui.editor.properties.controllers.generator.IDynamicProperty#getRepositoryQueryStoreMap()
    // */
    // public Map<String, Query> getRepositoryQueryStoreMap() {
    // return null;
    // }

    // /*
    // * (non-Javadoc)
    // *
    // * @see
    // org.talend.designer.core.ui.editor.properties.controllers.generator.IDynamicProperty#getRepositoryTableMap()
    // */
    // public Map<String, IMetadataTable> getRepositoryTableMap() {
    // return null;
    // }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties.controllers.generator.IDynamicProperty#getSection()
     */
    public EComponentCategory getSection() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.generator.IDynamicProperty#getTableIdAndDbSchemaMap()
     */
    public Map<String, String> getTableIdAndDbSchemaMap() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.generator.IDynamicProperty#getTableIdAndDbTypeMap()
     */
    public Map<String, String> getTableIdAndDbTypeMap() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties.controllers.generator.IDynamicProperty#refresh()
     */
    public void refresh() {

        Display.getDefault().syncExec(new Runnable() {

            /*
             * (non-Javadoc)
             *
             * @see java.lang.Runnable#run()
             */
            public void run() {
                getParent().layout();
            }
        });

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties.controllers.generator.IDynamicProperty#setCurRowSize(int)
     */
    public void setCurRowSize(int i) {

    }

}
