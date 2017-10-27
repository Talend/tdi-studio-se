// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.hl7.ui.view;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.talend.commons.ui.swt.advanced.dataeditor.ExtendedToolbarView;
import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreatorColumn;
import org.talend.commons.utils.data.bean.IBeanPropertyAccessors;
import org.talend.core.CorePlugin;
import org.talend.core.model.metadata.DbDefaultLengthAndPrecision;
import org.talend.core.model.metadata.Dbms;
import org.talend.core.model.metadata.MetadataTalendType;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.impl.MetadataColumnImpl;
import org.talend.core.model.metadata.types.TypesManager;
import org.talend.core.ui.metadata.editor.MetadataEmfTableEditor;
import org.talend.core.ui.proposal.JavaSimpleDateFormatProposalProvider;
import org.talend.designer.hl7.model.IModel;
import org.talend.designer.hl7.ui.HL7MultiSchemaUI;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 * TGU same purpose as MetadataTableEditorView but uses EMF model directly
 * 
 * $Id: MetadataEmfTableEditorView.java 23221 2009-04-01 01:41:36Z zli $
 * 
 */
public class HL7MetadataEmfTableEditorView extends AbstractHL7MetadataTableEditorView<MetadataColumn> {

    public static final String ID_COLUMN_NAME = "ID_COLUMN_NAME"; //$NON-NLS-1$

    private boolean isRepository;

    /**
     * You must initialize graphicals components by calling <code>initGraphicComponents()</code>.
     * 
     * @param parent
     * @param style
     */
    public HL7MetadataEmfTableEditorView(Composite parent, int style) {
        super(parent, style, false);
        initGraphicComponents();
    }

    /**
     * DOC amaumont MetadataEmfTableEditorView constructor comment.
     * 
     * @param parentComposite
     * @param mainCompositeStyle
     * @param initGraphicsComponents
     */
    public HL7MetadataEmfTableEditorView(Composite parentComposite, int mainCompositeStyle, boolean initGraphicsComponents) {
        super(parentComposite, mainCompositeStyle, initGraphicsComponents);
    }

    /**
     * Graphics components are automatically initialized.
     * 
     * @param parentComposite
     * @param mainCompositeStyle
     * @param extendedTableModel
     * @param readOnly
     * @param toolbarVisible
     */
    public HL7MetadataEmfTableEditorView(Composite parentComposite, int mainCompositeStyle,
            ExtendedTableModel<MetadataColumn> extendedTableModel, boolean readOnly, boolean toolbarVisible) {
        super(parentComposite, mainCompositeStyle, extendedTableModel, readOnly, toolbarVisible, true);
    }

    /**
     * Graphics components are automatically initialized.
     * 
     * @param parentComposite
     * @param mainCompositeStyle
     * @param extendedTableModel
     */
    public HL7MetadataEmfTableEditorView(Composite parentComposite, int mainCompositeStyle,
            ExtendedTableModel<MetadataColumn> extendedTableModel) {
        super(parentComposite, mainCompositeStyle, extendedTableModel);
    }

    public MetadataEmfTableEditor getMetadataEditor() {
        return (MetadataEmfTableEditor) getExtendedTableModel();
    }

    public void setMetadataEditor(MetadataEmfTableEditor metadataTableEditor) {
        setExtendedTableModel(metadataTableEditor);
    }

    public TableViewerCreator<MetadataColumn> getTableViewerCreator() {
        return getExtendedTableViewer().getTableViewerCreator();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.ui.metadata.editor.AbstractMetadataTableEditorView#getNullableAccessor()
     */
    @Override
    protected IBeanPropertyAccessors getNullableAccessor() {
        return new IBeanPropertyAccessors<MetadataColumn, Boolean>() {

            public Boolean get(MetadataColumn bean) {
                return bean.isNullable() ? Boolean.TRUE : Boolean.FALSE;
            }

            public void set(MetadataColumn bean, Boolean value) {
                bean.setNullable(value);
            }

        };
    }

    @Override
    protected IBeanPropertyAccessors<MetadataColumn, String> getCommentAccessor() {
        return new IBeanPropertyAccessors<MetadataColumn, String>() {

            public String get(MetadataColumn bean) {
                return bean.getComment();
            }

            public void set(MetadataColumn bean, String value) {
                bean.setComment(value);
            }

        };
    }

    @Override
    protected IBeanPropertyAccessors<MetadataColumn, String> getDefaultValueAccessor() {
        return new IBeanPropertyAccessors<MetadataColumn, String>() {

            public String get(MetadataColumn bean) {
                String value = bean.getDefaultValue();
                return value;
            }

            public void set(MetadataColumn bean, String value) {
                bean.setDefaultValue(value);
            }
        };
    }

    @Override
    protected IBeanPropertyAccessors<MetadataColumn, Long> getPrecisionAccessor() {
        return new IBeanPropertyAccessors<MetadataColumn, Long>() {

            public Long get(MetadataColumn bean) {
                // String dbmsId = getCurrentDbms();
                // if (dbmsId != null && bean.getPrecision() < 0) {
                // Dbms dbms = MetadataTalendType.getDbms(dbmsId);
                // List<DbDefaultLengthAndPrecision> dlpList = dbms.getDefaultLengthPrecision();
                // for (DbDefaultLengthAndPrecision dlp : dlpList) {
                // if (dlp.getDbTypeName().equals(bean.getSourceType()))
                // if (dlp.getDefaultPrecision() == null) {
                // bean.setPrecision(0);
                // } else {
                // bean.setPrecision(dlp.getDefaultPrecision());
                // }
                // }
                // }
                return bean.getPrecision();
            }

            public void set(MetadataColumn bean, Long value) {
                bean.setPrecision(value);
            }

        };
    }

    @Override
    protected IBeanPropertyAccessors<MetadataColumn, Long> getLengthAccessor() {
        return new IBeanPropertyAccessors<MetadataColumn, Long>() {

            public Long get(MetadataColumn bean) {
                // String dbmsId = getCurrentDbms();
                // if (dbmsId != null && bean.getLength() <= 0) {
                // Dbms dbms = MetadataTalendType.getDbms(dbmsId);
                // List<DbDefaultLengthAndPrecision> dlpList = dbms.getDefaultLengthPrecision();
                // for (DbDefaultLengthAndPrecision dlp : dlpList) {
                // if (dlp.getDbTypeName().equals(bean.getSourceType())) {
                // if (dlp.getDefaultLength() == null) {
                // bean.setLength(0);
                // } else {
                // bean.setLength(dlp.getDefaultLength());
                // }
                // }
                // }
                // }
                return bean.getLength();
            }

            public void set(MetadataColumn bean, Long value) {
                bean.setLength(value);
            }

        };
    }

    @Override
    protected IBeanPropertyAccessors<MetadataColumn, String> getPatternAccessor() {
        return new IBeanPropertyAccessors<MetadataColumn, String>() {

            public String get(MetadataColumn bean) {
                return bean.getPattern();
            }

            public void set(MetadataColumn bean, String value) {
                bean.setPattern(value); // MetadataEmfTableEditorView.this.getJavaDateTypeForDefaultPattern(bean));
            }

        };
    }

    @Override
    protected IBeanPropertyAccessors<MetadataColumn, Boolean> getKeyAccesor() {
        return new IBeanPropertyAccessors<MetadataColumn, Boolean>() {

            public Boolean get(MetadataColumn bean) {
                return new Boolean(bean.isKey());
            }

            public void set(MetadataColumn bean, Boolean value) {
                bean.setKey(value);
            }

        };
    }

    @Override
    protected IBeanPropertyAccessors<MetadataColumn, String> getLabelAccessor() {
        return new IBeanPropertyAccessors<MetadataColumn, String>() {

            public String get(MetadataColumn bean) {
                return bean.getLabel();
            }

            public void set(MetadataColumn bean, String value) {

                if (bean.getLabel().equals(bean.getOriginalField())) {
                    // bean.setOriginalField(value);
                }
                bean.setLabel(value);
            }

        };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.ui.metadata.editor.AbstractMetadataTableEditorView#validateColumnName(java.lang.String, int)
     */
    @Override
    protected String validateColumnName(String newValue, int beanPosition) {
        return getMetadataEditor().validateColumnName(newValue, beanPosition);
    }

    @Override
    protected IBeanPropertyAccessors<MetadataColumn, String> getTalendTypeAccessor() {
        return new IBeanPropertyAccessors<MetadataColumn, String>() {

            public String get(MetadataColumn bean) {
                return bean.getTalendType();
            }

            public void set(MetadataColumn bean, String value) {
                String oldTalendType = bean.getTalendType();
                bean.setTalendType(value);
                if (!oldTalendType.equals(value)) {
                    String typeLength = getCurrentTypeLength(value);
                    if (typeLength != null && !typeLength.equals("")) { //$NON-NLS-1$
                        bean.setLength(Integer.parseInt(typeLength));
                    }
                }
                String dbms = getCurrentDbms();
                if (showDbTypeColumn && (dbms != null)) {
                    String oldDbType = bean.getSourceType();
                    String oldDefaultDbType = null;
                    if (!"".equals(oldDbType)) { //$NON-NLS-1$
                        oldDefaultDbType = TypesManager.getDBTypeFromTalendType(dbms, oldTalendType);
                    }
                    if (oldDbType == null || oldDbType.equals(oldDefaultDbType) || "".equals(oldDbType)) { //$NON-NLS-1$
                        bean.setSourceType(TypesManager.getDBTypeFromTalendType(dbms, value));
                    }
                }
                if (currentBeanHasJavaDateType(bean)) {
                    bean.setPattern(new JavaSimpleDateFormatProposalProvider().getProposals(null, 0)[0].getContent());
                    // adaptLengthAndPrecision(bean, dbms);
                }
            }
        };
    }

    private void adaptLengthAndPrecision(MetadataColumn bean, String dbms) {
        Dbms dbmsD = MetadataTalendType.getDbms(dbms);
        List<DbDefaultLengthAndPrecision> dlpList = dbmsD.getDefaultLengthPrecision();
        for (DbDefaultLengthAndPrecision dlp : dlpList) {
            if (dlp.getDbTypeName().equals(bean.getSourceType())) {
                bean.setLength(dlp.getDefaultLength());
                bean.setPrecision(dlp.getDefaultPrecision());
            }
        }

    }

    @Override
    protected IBeanPropertyAccessors getDbTypeAccessor() {
        return new IBeanPropertyAccessors<MetadataColumn, String>() {

            public String get(MetadataColumn bean) {
                return bean.getSourceType();
            }

            public void set(MetadataColumn bean, String value) {
                bean.setSourceType(value);
                // String dbms = getCurrentDbms();
                // if (dbms != null) {
                // adaptLengthAndPrecision(bean, dbms);
                // }
            }

        };
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.core.ui.metadata.editor.AbstractMetadataTableEditorView#configureDefaultColumn(org.talend.commons.
     * ui.swt.tableviewer.TableViewerCreator)
     */
    @Override
    protected void configureDefaultColumn(TableViewerCreator<MetadataColumn> tableViewerCreator) {
        super.configureDefaultColumn(tableViewerCreator);
    }

    @Override
    protected IBeanPropertyAccessors<MetadataColumn, String> getDbColumnNameAccessor() {
        return new IBeanPropertyAccessors<MetadataColumn, String>() {

            public String get(MetadataColumn bean) {
                return bean.getOriginalField();
            }

            public void set(MetadataColumn bean, String value) {
                bean.setOriginalField(value);
                IStructuredSelection selection = (IStructuredSelection) ((HL7MultiSchemaUI) linker.getMainui())
                        .getMetaTableViewer().getSelection();
                Object selectedObj = selection.getFirstElement();
                if (selectedObj != null) {
                    String key = ((IModel) selectedObj).getDisplayName();
                    linker.getManager().updateRelationMapping(key, bean, false);
                    // if (columns.indexOf(bean) > 0) {
                    // columns.get(columns.indexOf(bean)).setOriginalField(value);
                    // }
                }
            }

        };
    }

    public String getCurrentTypeLength(String value) {
        return CorePlugin.getDefault().getPreferenceStore().getString(value.toUpperCase());
    }

    @Override
    protected boolean canModifyDBColumn(Object bean) {
        // TODO
        if (bean instanceof MetadataColumnImpl) {
            // if (((MetadataColumnImpl) bean).getLabel().equals(((MetadataColumnImpl) bean).getOriginalField()))
            // return true;
            return !isRepository;
        }
        return false;
    }

    @Override
    public ExtendedTableModel<MetadataColumn> getExtendedTableModel() {
        return super.getExtendedTableModel();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.ui.metadata.editor.AbstractMetadataTableEditorView#initToolBar()
     */
    @Override
    protected ExtendedToolbarView initToolBar() {
        return new HL7MetadataEmfToolbarEditor(getMainComposite(), SWT.NONE, this.getExtendedTableViewer(), linker,
                isRepository());
    }

    public TableViewerCreatorColumn getNameColumn() {
        return this.nameColumn;
    }

    public boolean isRepository() {
        return this.isRepository;
    }

    public void setRepository(boolean isRepository) {
        this.isRepository = isRepository;
    }

}
