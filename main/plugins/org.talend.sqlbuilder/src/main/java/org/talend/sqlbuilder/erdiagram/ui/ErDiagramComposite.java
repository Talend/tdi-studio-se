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
package org.talend.sqlbuilder.erdiagram.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.CheckBox;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.talend.commons.ui.swt.colorstyledtext.ColorStyledText;
import org.talend.core.CorePlugin;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.model.metadata.MetadataManager;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.database.ExtractMetaDataUtils;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.repository.utils.DatabaseConnectionParameterUtil;
import org.talend.core.sqlbuilder.util.TextUtil;
import org.talend.core.utils.KeywordsValidator;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.cwm.helper.TaggedValueHelper;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;
import org.talend.sqlbuilder.erdiagram.ui.editor.ErdiagramDiagramEditor;
import org.talend.sqlbuilder.erdiagram.ui.nodes.Column;
import org.talend.sqlbuilder.erdiagram.ui.nodes.ErDiagram;
import org.talend.sqlbuilder.erdiagram.ui.nodes.Relation;
import org.talend.sqlbuilder.erdiagram.ui.nodes.Table;
import org.talend.sqlbuilder.erdiagram.ui.parts.ColumnPart;
import org.talend.sqlbuilder.erdiagram.ui.parts.ErDiagramPart;
import org.talend.sqlbuilder.erdiagram.ui.parts.TablePart;
import org.talend.sqlbuilder.repository.utility.EMFRepositoryNodeManager;
import org.talend.sqlbuilder.repository.utility.SQLBuilderRepositoryNodeManager;
import org.talend.sqlbuilder.ui.ISQLBuilderDialog;
import org.talend.sqlbuilder.util.UIUtils;
import org.talend.utils.sql.ConnectionUtils;
import org.talend.utils.sql.metadata.constants.GetTable;

import orgomg.cwm.objectmodel.core.ModelElement;
import orgomg.cwm.resource.relational.Catalog;
import orgomg.cwm.resource.relational.Schema;

/**
 * qzhang class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 *
 */
public class ErDiagramComposite extends SashForm {

    private List<IRepositoryNode> nodes;

    private ErdiagramDiagramEditor editor;

    private StyledText sqlText;

    private final String language = "tsql"; //$NON-NLS-1$

    private String sqlString = ""; //$NON-NLS-1$

    /**
     * admin ErDiagramComposite constructor comment.
     *
     * @param parent
     * @param style
     */
    public ErDiagramComposite(Composite parent, int style) {
        super(parent, style);
        nodes = new ArrayList<IRepositoryNode>();
    }

    /**
     * admin Comment method "addErDiagramEditor".
     */
    @SuppressWarnings("unchecked")
    private void addErDiagramEditor(boolean isShowDesignerPage) {
        GridData gridData = new GridData(GridData.FILL_BOTH);
        this.setLayoutData(gridData);

        GridLayout layout = new GridLayout();
        layout.verticalSpacing = 0;
        layout.marginLeft = 0;
        layout.marginRight = 0;
        layout.marginBottom = 0;
        layout.marginTop = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.numColumns = 1;

        this.setLayout(layout);

        editor = new ErdiagramDiagramEditor();
        editor.createPartControl(this);
        editor.getViewer().setContents(createErDiagram(isShowDesignerPage));
        Control control = editor.getGraphicalControl();
        if (control != null) {
            control.setParent(this);
            control.setLayoutData(gridData);
        }
        layout = new GridLayout();
        layout.marginLeft = 0;
        layout.marginRight = 0;
        layout.marginBottom = 0;
        layout.marginTop = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.heightHint = 30;
        int textstyle = SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL;
        sqlText = new ColorStyledText(this, textstyle, CorePlugin.getDefault().getPreferenceStore(), language);
        sqlText.setLayoutData(gridData);
        sqlText.setText(""); //$NON-NLS-1$
        sqlText.setBackground(getBackground());
        sqlText.addModifyListener(new ModifyListener() {

            /*
             * (non-Javadoc)
             *
             * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
             */
            public void modifyText(ModifyEvent e) {
                sqlString = sqlText.getText();
            }

        });

    }

    /**
     * Getter for sqlText.
     *
     * @return the sqlText
     */
    public String getSqlText() {
        if ("".equals(sqlString)) { //$NON-NLS-1$
            if (!"".equals(getSqlStatement())) { //$NON-NLS-1$
                sqlString = getSqlStatement();
            }
        }
        return sqlString;
    }

    public StyledText getStyledText() {
        return this.sqlText;
    }

    /**
     * Sets the sqlText.
     *
     * @param sqlText the sqlText to set
     */
    public void setSqlText(String sqlText) {
        if (sqlText != null) {
            this.sqlText.setText(sqlText);
            this.sqlText.setVisible(true);

        }
    }

    private ErDiagram erDiagram;

    /**
     * admin Comment method "createErDiagram".
     *
     * @return
     */
    private ErDiagram createErDiagram(boolean isShowDesignerPage) {
        erDiagram = new ErDiagram();
        erDiagram.setErDiagramComposite(this);
        if (isShowDesignerPage) {
            IRunnableWithProgress progress = new IRunnableWithProgress() {

                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    monitor.beginTask("", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
                    try {
                        List<MetadataColumn> selectedColumns = new ArrayList<MetadataColumn>();
                        List<MetadataTable> tables = EMFRepositoryNodeManager.getInstance().getTables(getNodes(),
                                selectedColumns);

                        erDiagram.setMetadataTables(tables);
                        List<String[]> fks = EMFRepositoryNodeManager.getInstance().getPKFromTables(tables,
                                erDiagram.getErDiagramComposite().getDialog().getSelectedContext());
                        for (MetadataTable metadataTable : tables) {
                            Table table = new Table();
                            table.setMetadataTable(metadataTable, selectedColumns);
                            table.setErDiagram(erDiagram);
                            erDiagram.addTable(table);
                        }
                        erDiagram.setRelations(fks);

                    } finally {
                        monitor.done();
                    }
                }
            };
            UIUtils.runWithProgress(progress, true, dialog.getProgressMonitor(), dialog.getShell());
        } else {
            erDiagram.setMetadataTables(new ArrayList<MetadataTable>());
            erDiagram.setRelations(new ArrayList<String[]>());
        }
        return erDiagram;
    }

    private ISQLBuilderDialog dialog;

    public void setDialog(ISQLBuilderDialog dialog) {
        this.dialog = dialog;
    }

    private String getCurrentDbType() {
        DatabaseConnection connection = (DatabaseConnection) ((ConnectionItem) rootNode.getObject().getProperty().getItem())
                .getConnection();
        String driverClass = connection.getDriverClass();
        if (connection.isContextMode()) {
            driverClass = DatabaseConnectionParameterUtil.getContextTrueValue(connection, driverClass);
        }
        String dbType = ExtractMetaDataUtils.getInstance().getDbTypeByClassName(driverClass);
        if (dbType == null) {
            dbType = connection.getDatabaseType();
        }
        return dbType;
    }

    private String getSchema() {
        DatabaseConnection connection = (DatabaseConnection) ((ConnectionItem) rootNode.getObject().getProperty().getItem())
                .getConnection();
        String schema = "";
        boolean isCalculationView = false;
        if (ConnectionUtils.isTeradata(connection.getURL())) {
            schema = connection.getSID();
        } else if (EDatabaseTypeName.SAPHana.getDisplayName().equals(getCurrentDbType())) {
            if (erDiagram != null) {
                List<MetadataTable> tables = erDiagram.getMetadataTables();
                for (MetadataTable table : tables) {
                	if(MetadataManager.TYPE_CALCULATION_VIEW.equals(table.getTableType())) {
                		isCalculationView  = true;
                	}
                    schema = TaggedValueHelper.getValueString(GetTable.TABLE_SCHEM.name(), table);
                    if (StringUtils.isEmpty(schema)) {
                        schema = connection.getUiSchema();
                    }
                }
            }
        } else {
            schema = connection.getUiSchema();
        }

        if (connection.isContextMode() && !isCalculationView) {
            schema = DatabaseConnectionParameterUtil.getContextTrueValue(connection, schema);
        }
        if (schema == null) {
            schema = "";
        }
        return schema;

    }

    @SuppressWarnings("unchecked")
    private String getSqlStatement() {
        String sql = ""; //$NON-NLS-1$
        List<String> tables = new ArrayList<String>();
        List<String> columns = new ArrayList<String>();
        List<String> wheres = new ArrayList<String>();
        if (editor != null) {
            if (editor.getViewer().getContents() instanceof ErDiagramPart) {
                String schemaPrefix = "".equals(getSchema()) ? "" : getSchema() + "."; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                String schemaPrefixWithDoubleQuotes = "".equals(getSchema()) ? "" : "\"" + getSchema() + "\"."; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                ErDiagramPart er = (ErDiagramPart) editor.getViewer().getContents();
                for (Object object : er.getChildren()) {
                    if (object instanceof TablePart) {
                        TablePart tablePart = (TablePart) object;
                        Table table = (Table) tablePart.getModel();
                        if (TextUtil.isDoubleQuotesNeededDbType(getCurrentDbType())) {
                            tables.add(schemaPrefixWithDoubleQuotes + "\"" + table.getElementName() + "\"");
                        } else {
                            tables.add(schemaPrefix
                                    + TalendTextUtils.addQuotesWithSpaceField(table.getElementName(), getCurrentDbType()));
                        }

                        boolean oracleDbType = TextUtil.isOracleDbType(getCurrentDbType());
                        for (Object obj : tablePart.getChildren()) {
                            if (obj instanceof ColumnPart) {
                                ColumnPart columnPart = (ColumnPart) obj;
                                Column column = (Column) columnPart.getModel();
                                CheckBox isSelected = columnPart.getPrimativeFigure().getFigureCustomColumnIsSelectedFigure();
                                if (isSelected != null && isSelected.isSelected() && !column.getElementName().equals("*")) { //$NON-NLS-1$
                                    if (TextUtil.isDoubleQuotesNeededDbType(getCurrentDbType())) {
                                        columns.add(schemaPrefixWithDoubleQuotes + "\"" + table.getElementName() + "\".\"" //$NON-NLS-1$ //$NON-NLS-2$
                                                + column.getElementName() + "\""); //$NON-NLS-1$
                                    } else {
                                        // added by hyWang
                                        String leftQuote = TalendTextUtils.getQuoteByDBType(getCurrentDbType(), true);
                                        String rightQuote = TalendTextUtils.getQuoteByDBType(getCurrentDbType(), false);
                                        String columnContent = column.getElementName();
                                        Pattern pattern = Pattern.compile("\\w+"); //$NON-NLS-1$
                                        Matcher matcher = pattern.matcher(columnContent);
                                        EDatabaseTypeName dbType = EDatabaseTypeName.getTypeFromDbType(getCurrentDbType());
                                        // modify for bug 12092
                                        boolean sqlKeyword = KeywordsValidator.isSqlKeyword(column.getElementName(),
                                                dbType.getProduct());

                                        if (!matcher.matches() || (sqlKeyword && oracleDbType)) {
                                            columns.add(schemaPrefix
                                                    + TalendTextUtils
                                                            .addQuotesWithSpaceField(table.getElementName(), getCurrentDbType())
                                                    + "." //$NON-NLS-1$
                                                    + TalendTextUtils.addQuotesWithSpaceField(
                                                            leftQuote + column.getElementName() + rightQuote,
                                                            getCurrentDbType()));
                                        } else {
                                            columns.add(schemaPrefix
                                                    + TalendTextUtils.addQuotesWithSpaceField(table.getElementName(),
                                                            getCurrentDbType())
                                                    + "." //$NON-NLS-1$
                                                    + TalendTextUtils.addQuotesWithSpaceField(column.getElementName(),
                                                            getCurrentDbType()));
                                        }
                                    }
                                }
                                for (Relation rel : column.getOutputs()) {
                                    Column source = rel.getSource();
                                    Column target = rel.getTarget();
                                    if (TextUtil.isDoubleQuotesNeededDbType(getCurrentDbType())) {
                                        String where1 = schemaPrefixWithDoubleQuotes + "\"" + source.getTable().getElementName() //$NON-NLS-1$
                                                + "\".\"" //$NON-NLS-1$
                                                + source.getElementName() + "\"= " + schemaPrefixWithDoubleQuotes + "\"" //$NON-NLS-1$
                                                + target.getTable().getElementName() + "\".\"" + target.getElementName() + "\""; //$NON-NLS-1$ //$NON-NLS-2$
                                        if (!wheres.contains(where1)) {
                                            wheres.add(where1);
                                        }
                                    } else {
                                        String where1 = schemaPrefix + TalendTextUtils
                                                .addQuotesWithSpaceField(source.getTable().getElementName(), getCurrentDbType())
                                                + "." //$NON-NLS-1$
                                                + TalendTextUtils.addQuotesWithSpaceField(source.getElementName(),
                                                        getCurrentDbType())
                                                + "=" //$NON-NLS-1$
                                                + schemaPrefix
                                                + TalendTextUtils.addQuotesWithSpaceField(target.getTable().getElementName(),
                                                        getCurrentDbType())
                                                + "." //$NON-NLS-1$
                                                + TalendTextUtils.addQuotesWithSpaceField(target.getElementName(),
                                                        getCurrentDbType());
                                        if (!wheres.contains(where1)) {
                                            wheres.add(where1);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        // Mssql query need add catalog and schema before the table, like this "catolog.schema.table"
        Connection conn = null;
        if (rootNode != null) {
            Item connectionItem = rootNode.getObject().getProperty().getItem();
            if (connectionItem instanceof ConnectionItem) {
                conn = ((ConnectionItem) connectionItem).getConnection();
            }
        }
        if (getCurrentDbType() != null && (getCurrentDbType().equals(EDatabaseTypeName.MSSQL.getDisplayName())
                || getCurrentDbType().equals(EDatabaseTypeName.MSSQL.name())) && conn != null) {
            List<String> newTables = new ArrayList<String>();
            for (String str : tables) {
                newTables.add(getMssqlCatalog(str, conn));
            }
            tables = newTables;
        }
        sql = getSelectStatement(tables, columns, wheres);
        if (sql.endsWith(",")) { //$NON-NLS-1$
            return sql.substring(0, sql.length() - 1);
        } else if (sql.endsWith(" and ")) { //$NON-NLS-1$
            return sql.substring(0, sql.length() - 5);
        }
        return ""; //$NON-NLS-1$

    }

    private String getMssqlCatalog(String realTableName, Connection conn) {
        Set<Catalog> catalog = ConnectionHelper.getAllCatalogs(conn);
        for (Catalog cata : catalog) {
            for (ModelElement ele : cata.getOwnedElement()) {
                if (ele instanceof Schema) {
                    for (ModelElement child : ((Schema) ele).getOwnedElement()) {
                        String childeleName = TalendTextUtils.addQuotesWithSpaceFieldForSQLStringForce(
                                TalendTextUtils.declareString(child.getName()), getCurrentDbType(), true);
                        if (childeleName.startsWith(TalendTextUtils.QUOTATION_MARK)
                                && childeleName.endsWith(TalendTextUtils.QUOTATION_MARK) && childeleName.length() > 2) {
                            childeleName = childeleName.substring(1, childeleName.length() - 1);
                        }
                        if (cata.getName().contains("-")) {
                            return realTableName;
                        } else if (childeleName.equals(realTableName)) {
                            return cata.getName() + "." + ele.getName() + "." + realTableName;
                        } else if (realTableName.endsWith("." + TalendTextUtils.removeQuotesIfExist(childeleName))) {
                            return cata.getName() + "." + realTableName;
                        }
                    }
                }
            }
        }
        return realTableName;
    }

    /**
     * admin Comment method "getSelectStatement".
     *
     * @param tables
     * @param columns
     * @param wheres
     * @return
     */
    private String getSelectStatement(List<String> tables, List<String> columns, List<String> wheres) {

        String sql = ""; //$NON-NLS-1$
        if (tables.isEmpty() || columns.isEmpty()) {
            return sql;
        }
        sql = "select "; //$NON-NLS-1$
        for (String string : columns) {
            sql = sql + string + ","; //$NON-NLS-1$
        }
        sql = sql.substring(0, sql.length() - 1) + " \nfrom "; //$NON-NLS-1$
        for (String string : tables) {
            sql = sql + string + ","; //$NON-NLS-1$
        }
        if (!wheres.isEmpty()) {
            sql = sql.substring(0, sql.length() - 1) + " \nwhere "; //$NON-NLS-1$
            for (String string : wheres) {
                sql = sql + string + " and "; //$NON-NLS-1$
            }
        }
        return sql;
    }

    public List<IRepositoryNode> getNodes() {
        return this.nodes;
    }

    public void setNodes(List<IRepositoryNode> nodes, boolean isShowDesignerPage) {
        this.nodes = nodes;
        addErDiagramEditor(isShowDesignerPage);
    }

    private RepositoryNode rootNode;

    public void setRootNode(RepositoryNode root) {
        if (root == null && !getNodes().isEmpty()) {
            this.rootNode = SQLBuilderRepositoryNodeManager.getRoot((RepositoryNode) getNodes().get(0));

        } else {
            this.rootNode = root;
        }
    }

    public void updateNodes(List<IRepositoryNode> nodes, String sqlText) {
        this.nodes = nodes;
        this.sqlText.setText(sqlText);
        editor.getViewer().setContents(createErDiagram(true));
    }

    private boolean isModified = false;

    /**
     * Getter for isModified.
     *
     * @return the isModified
     */
    public boolean isModified() {
        return this.isModified;
    }

    /**
     * Sets the isModified.
     *
     * @param isModified the isModified to set
     */
    public void setModified(boolean isModified) {
        this.isModified = isModified;
    }

    public ErdiagramDiagramEditor getEditor() {
        return this.editor;
    }

    public ErDiagram getErDiagram() {
        return this.erDiagram;
    }

    public void clearAll() {
        List<Table> tables = new ArrayList<Table>();
        tables.addAll(erDiagram.getTables());
        for (Table table : tables) {
            erDiagram.removeTable(table);
        }
        this.sqlText.setText(""); //$NON-NLS-1$
    }

    /**
     * qzhang Comment method "updateSql".
     */
    public void updateSql() {
        setSqlText(getSqlStatement());
    }

    public ISQLBuilderDialog getDialog() {
        return this.dialog;
    }

}
