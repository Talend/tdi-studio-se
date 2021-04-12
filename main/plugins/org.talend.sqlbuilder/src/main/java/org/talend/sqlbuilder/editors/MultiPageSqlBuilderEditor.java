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
package org.talend.sqlbuilder.editors;

import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.internal.services.INestable;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.services.IServiceLocator;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.core.model.metadata.builder.connection.Query;
import org.talend.core.sqlbuilder.util.ConnectionParameters;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.actions.SaveAsSQLAction;
import org.talend.sqlbuilder.actions.SaveSQLAction;
import org.talend.sqlbuilder.erdiagram.ui.ErDiagramComposite;
import org.talend.sqlbuilder.repository.utility.EMFRepositoryNodeManager;
import org.talend.sqlbuilder.ui.AbstractSQLEditorComposite;
import org.talend.sqlbuilder.ui.ISQLBuilderDialog;
import org.talend.sqlbuilder.ui.SQLBuilderDesignerComposite;
import org.talend.sqlbuilder.ui.SQLBuilderEditorComposite;

/**
 * qzhang class global comment. Detailled comment <br/>
 *
 * $Id: talend-code-templates.xml 1 2007-2-9 上午10:33:48 (星期五, 29 九月 2006) qzhang $
 *
 */
public class MultiPageSqlBuilderEditor extends MultiPageEditorPart {

	private SQLBuilderEditorComposite sqlEdit;

    private SQLBuilderDesignerComposite sqlDesigner;

    private boolean readOnly = false;

    public SQLBuilderEditorComposite getSqlEdit() {
        return this.sqlEdit;
    }

    public SQLBuilderDesignerComposite getSqlDesigner() {
        return this.sqlDesigner;
    }

    private List<IRepositoryNode> nodes;

    /**
     * qzhang MultiPageSqlBuilderEditor constructor comment.
     */
    public MultiPageSqlBuilderEditor(List<IRepositoryNode> nodes, CTabItem tabItem, boolean isDefaultEditor,
            ConnectionParameters connParam, RepositoryNode rootNode, ISQLBuilderDialog dialog) {
        super();
        this.nodes = nodes;
        this.tabItem = tabItem;
        this.isDefaultEditor = isDefaultEditor;
        this.connParam = connParam;
        this.rootNode = rootNode;
        this.dialog = dialog;
    }

    public static final String ID = "org.talend.sqlbuilder.editors.MultiPageSqlBuilderEditor"; //$NON-NLS-1$

    private ConnectionParameters connParam;

    private CTabItem tabItem;

    private boolean isDefaultEditor;

    private RepositoryNode rootNode;

    private ISQLBuilderDialog dialog;

    public ISQLBuilderDialog getDialog() {
        return this.dialog;
    }

    private ErDiagramComposite erDiagramComposite;
	private CTabFolder container;
	private INestable activeServiceLocator;

    /*
     * (non-Java)
     *
     * @see org.eclipse.ui.part.MultiPageEditorPart#createPages()
     */
    @Override
    protected void createPages() {
        try {
            sqlEdit = new SQLBuilderEditorComposite(this.getContainer(), tabItem, isDefaultEditor, connParam, rootNode, dialog,
                    readOnly);

            sqlEdit.setEditorContent(connParam);
            sqlEdit.setRepositoryNode(rootNode);
            sqlEdit.setQueryObject(dialog.getConnParameters().getQueryObject());
            sqlEdit.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
            sqlEdit.setMultiPageEditor(this);

            int index = addPage(sqlEdit);
            setPageText(index, Messages.getString("MultiPageSqlBuilderEditor.EditTab.Text")); //$NON-NLS-1$
            EMFRepositoryNodeManager.getInstance().setPrompt(false);
            sqlDesigner = new SQLBuilderDesignerComposite(this.getContainer(), tabItem, isDefaultEditor, connParam, rootNode,
                    dialog, nodes, readOnly);

            sqlDesigner.setSqlText(sqlEdit.getSQLToBeExecuted());
            sqlDesigner.setEditorContent(connParam);
            // sqlDesigner.setQueryObject(dialog.getConnParameters().getQueryObject());
            sqlDesigner.setMaxResult(sqlEdit.getMaxResult());
            sqlDesigner.setIfLimit(sqlEdit.getIfLimit());
            sqlDesigner.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
            sqlDesigner.setMultiPageEditor(this);
            EMFRepositoryNodeManager.getInstance().setPrompt(true);
            erDiagramComposite = sqlDesigner.getErDiagramComposite();
            index = addPage(sqlDesigner);
            setPageText(index, Messages.getString("MultiPageSqlBuilderEditor.DesignerTab.Text")); //$NON-NLS-1$
            attachListeners();
            sqlEdit.setEditorContent(connParam); // reset the sql to avoid 007813
        } catch (Exception e) {
            MessageDialog.openError(getContainer().getShell(), Messages.getString("MultiPageSqlBuilderEditor.ErrorTitle"), //$NON-NLS-1$
                    Messages.getString("MultiPageSqlBuilderEditor.ErrorInfo") + e.getMessage()); //$NON-NLS-1$
        }

    }

    public final void createPartControl2(Composite parent) {
    	//super.createPartControl(parent);
		Composite pageContainer = createPageContainer(parent);
		this.container = createContainer(pageContainer);
		createPages();
		// set the active page (page 0 by default), unless it has already been
		// done
		if (getActivePage() == -1) {
			setActivePage(0);
			IEditorPart part = getEditor(0);
			if (part!=null) {
				final IServiceLocator serviceLocator = part.getEditorSite();
				if (serviceLocator instanceof INestable) {
					activeServiceLocator = (INestable) serviceLocator;
					activeServiceLocator.activate();
				}
			}
		}
	}

    private CTabFolder createContainer(Composite parent) {
		// use SWT.FLAT style so that an extra 1 pixel border is not reserved
		// inside the folder
		parent.setLayout(new FillLayout());
		final CTabFolder newContainer = new CTabFolder(parent, SWT.BOTTOM
				| SWT.FLAT);
		newContainer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int newPageIndex = newContainer.indexOf((CTabItem) e.item);
				pageChange(newPageIndex);
			}
		});
		newContainer.addTraverseListener(new TraverseListener() {
			// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=199499 : Switching tabs by Ctrl+PageUp/PageDown must not be caught on the inner tab set
			@Override
			public void keyTraversed(TraverseEvent e) {
				switch (e.detail) {
					case SWT.TRAVERSE_PAGE_NEXT:
					case SWT.TRAVERSE_PAGE_PREVIOUS:
						int detail = e.detail;
						e.doit = true;
						e.detail = SWT.TRAVERSE_NONE;
						Control control = newContainer.getParent();
						do {
							if (control.traverse(detail))
								return;
							if (control.getListeners(SWT.Traverse).length != 0)
								return;
							if (control instanceof Shell)
								return;
							control = control.getParent();
						} while (control != null);
				}
			}
		});
		return newContainer;
	}

    private boolean isFirst = true;

    private boolean isFirst2 = true;

    /**
     * qzhang Comment method "attachListeners".
     */
    private void attachListeners() {
        sqlDesigner.getColorText().addModifyListener(new ModifyListener() {

            /*
             * (non-Javadoc)
             *
             * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
             */
            public void modifyText(ModifyEvent e) {
                if (!isFirst) {
                    updateEditorTitle(null);
                } else {
                    isFirst = false;
                }
            }

        });
        sqlEdit.getColorText().addModifyListener(new ModifyListener() {

            /*
             * (non-Javadoc)
             *
             * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
             */
            public void modifyText(ModifyEvent e) {
                if (!isFirst2) {
                    updateEditorTitle(null);
                } else {
                    isFirst2 = false;
                }
            }

        });
    }

    public void showDesignerPage() {
        setActivePage(1);
        erDiagramComposite.setSqlText(erDiagramComposite.getSqlText());
    }

    public void showEditPage() {
        setActivePage(0);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.part.MultiPageEditorPart#getActivePage()
     */
    @Override
    public int getActivePage() {
		CTabFolder tabFolder = getTabFolder();
		if (tabFolder != null && !tabFolder.isDisposed()) {
			return tabFolder.getSelectionIndex();
		}
		return -1;
	}

    public void setSqlText(String sql) {
        erDiagramComposite.setSqlText(sql);
    }

    public String getActivePageSqlString() {
        final int activePage = getActivePage();
        if (activePage == 0) {
            return sqlEdit.getSQLToBeExecuted();
        } else {
            return sqlDesigner.getSQLToBeExecuted();
        }
    }

    public RepositoryNode getActivePageRepositoryNode() {
        final int activePage = getActivePage();
        if (activePage == 0) {
            return sqlEdit.getRepositoryNode();
        } else {
            return sqlDesigner.getRepositoryNode();
        }

    }

    public boolean isModified() {
        if ("".equals(erDiagramComposite.getSqlText())) { //$NON-NLS-1$
            return (!"".equals(sqlEdit.getSQLToBeExecuted())); //$NON-NLS-1$
        } else {
            if ("".equals(sqlEdit.getSQLToBeExecuted())) { //$NON-NLS-1$
                return true;
            } else {
                EMFRepositoryNodeManager.getInstance().setPrompt(false);
                String orginSql = EMFRepositoryNodeManager.getInstance().initSqlStatement(sqlEdit.getSQLToBeExecuted());
                String sqlText = EMFRepositoryNodeManager.getInstance().initSqlStatement(erDiagramComposite.getSqlText());
                EMFRepositoryNodeManager.getInstance().setPrompt(true);
                if (sqlText == null) {
                    return orginSql != null;
                } else {
                    if (orginSql == null) {
                        return true;
                    } else {
                        return !sqlText.trim().equals(orginSql.trim());
                    }
                }
            }
        }

    }

    /*
     * (non-Java)
     *
     * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void doSave(IProgressMonitor monitor) {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.part.MultiPageEditorPart#setFocus()
     */
    @Override
    public void setFocus() {
        int focusIndex = getActivePage();
        final Control control = getControl(focusIndex);
        if (control != null) {
            control.setFocus();
        }
    }

    /*
     * (non-Java)
     *
     * @see org.eclipse.ui.part.EditorPart#doSaveAs()
     */
    @Override
    public void doSaveAs() {

    }

    /*
     * (non-Java)
     *
     * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
     */
    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    public void setRepositoryNode(RepositoryNode node) {
        sqlEdit.setRepositoryNode(node);
        sqlEdit.refresh(true);
        sqlDesigner.setRepositoryNode(node);
        sqlDesigner.refresh(true);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.part.MultiPageEditorPart#pageChange(int)
     */
    @Override
    public void pageChange(int newPageIndex) {
        setFocus();
        if (newPageIndex == 1) {
            try {
                String toSql = sqlEdit.getSQLToBeExecuted();
                EMFRepositoryNodeManager.getInstance().updateErDiagram(isModified(), erDiagramComposite, toSql, rootNode);
            } catch (Exception e) {
                MessageDialog.openError(getContainer().getShell(), Messages.getString("MultiPageSqlBuilderEditor.ErrorTitle"), //$NON-NLS-1$
                        Messages.getString("MultiPageSqlBuilderEditor.ErrorInfo") + e.getMessage()); //$NON-NLS-1$
            }
        } else if (newPageIndex == 0) {
            if (isModified()) {
                Boolean iskeep = null;
                if (connParam.isNeedTakePrompt()) {
                    iskeep = MessageDialog.openQuestion(DisplayUtils.getDefaultShell(false),
                            Messages.getString("MultiPageSqlBuilderEditor.AddComment.Title"), //$NON-NLS-1$
                            Messages.getString("MultiPageSqlBuilderEditor.AddComment.Info")); //$NON-NLS-1$
                }
                connParam.setNeedTakePrompt(true);
                if (iskeep != null && iskeep) {
                    // String newSql = EMFRepositoryNodeManager.getInstance()
                    // .addComment(builderEditorComposite.getSQLToBeExecuted())
                    // + "\n";
                    // newSql += editor.getSqlText();
                    sqlEdit.setEditorContent(erDiagramComposite.getSqlText());
                }
            }
            sqlEdit.setModified(false);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.part.MultiPageEditorPart#getContainer()
     */
    @Override
    public Composite getContainer() {
        return this.container;
    }

    public void updateEditorTitle(String text) {
        if (text == null) {
            text = this.tabItem.getText();
            if (!text.substring(0, 1).equals("*")) { //$NON-NLS-1$
                text = "*" + text; //$NON-NLS-1$
            }
            tabItem.setText(text);
        } else {
            tabItem.setText(text);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.part.WorkbenchPart#getTitle()
     */
    @Override
    public String getTitle() {
        if (tabItem != null) {
            return tabItem.getText();
        }
        return super.getTitle();
    }

    public SaveSQLAction getDeactivePageSaveSQLAction() {
        switch (getActivePage()) {
        case 1:
            return sqlEdit.getSaveSQLAction();
        case 0:
            return sqlDesigner.getSaveSQLAction();
        default:
            return null;
        }
    }

    public SaveAsSQLAction getActivePageSaveAsSQLAction() {
        switch (getActivePage()) {
        case 0:
            return sqlEdit.getSaveAsSQLAction();
        case 1:
            return sqlDesigner.getSaveAsSQLAction();
        default:
            return null;
        }
    }

    public void setItemData(Query query) {
        this.tabItem.setData(query);
    }

    public AbstractSQLEditorComposite getActiveEditors() {
        switch (getActivePage()) {
        case 0:
            return sqlEdit;
        case 1:
            return sqlDesigner;
        default:
            return null;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.part.MultiPageEditorPart#initializePageSwitching()
     */

    @Override
    protected void initializePageSwitching() {

    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
    public int addPage(Control control) {
		int index = getPageCount();
		addPage(index, control);
		return index;
	}
    @Override
    protected int getPageCount() {
		CTabFolder folder = getTabFolder();
		// May not have been created yet, or may have been disposed.
		if (folder != null && !folder.isDisposed()) {
			return folder.getItemCount();
		}
		return 0;
	}

    //These methods are from MultiPageEditor mostly
    public void addPage(int index, Control control) {
		createItem(index, control);
	}

    private CTabItem createItem(int index, Control control) {
		CTabItem item = new CTabItem(getTabFolder(), SWT.NONE, index);
		item.setControl(control);
		return item;
	}
    private CTabFolder getTabFolder() {
		return this.container;
	}

    private CTabItem getItem(int pageIndex) {
		return getTabFolder().getItem(pageIndex);
	}

    protected void setActivePage(int pageIndex) {
		Assert.isTrue(pageIndex >= 0 && pageIndex < getPageCount());
		getTabFolder().setSelection(pageIndex);
		pageChange(pageIndex);
	}

    @Override
    protected Control getControl(int pageIndex) {
		return getItem(pageIndex).getControl();
	}
    @Override
    protected IEditorPart getEditor(int pageIndex) {
		Item item = getItem(pageIndex);
		if (item != null) {
			Object data = item.getData();
			if (data instanceof IEditorPart) {
				return (IEditorPart) data;
			}
		}
		return null;
	}
    @Override
    protected Image getPageImage(int pageIndex) {
		return getItem(pageIndex).getImage();
	}
    @Override
    protected String getPageText(int pageIndex) {
		return getItem(pageIndex).getText();
	}
    @Override
    protected void setControl(int pageIndex, Control control) {
		getItem(pageIndex).setControl(control);
	}
    @Override
    protected void setPageImage(int pageIndex, Image image) {
		getItem(pageIndex).setImage(image);
	}
    @Override
    protected void setPageText(int pageIndex, String text) {
		getItem(pageIndex).setText(text);
	}

}
