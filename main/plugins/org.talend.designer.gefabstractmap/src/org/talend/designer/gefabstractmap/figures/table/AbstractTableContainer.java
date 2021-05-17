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
package org.talend.designer.gefabstractmap.figures.table;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.ScrollPane;
import org.talend.designer.gefabstractmap.figures.GenericFigure;
import org.talend.designer.gefabstractmap.figures.borders.RowBorder;
import org.talend.designer.gefabstractmap.figures.layout.EqualWidthLayout;
import org.talend.designer.gefabstractmap.figures.layout.TableContainerLayout;
import org.talend.designer.gefabstractmap.figures.manager.TableManager;
import org.talend.designer.gefabstractmap.figures.treesettings.AbstractTreeSettingContainer;
import org.talend.designer.gefabstractmap.figures.treesettings.FilterContainer;
import org.talend.designer.gefabstractmap.figures.treetools.ToolBarContainer;
import org.talend.designer.gefabstractmap.resource.ColorInfo;
import org.talend.designer.gefabstractmap.resource.ColorProviderMapper;
import org.talend.designer.gefabstractmap.resource.FontInfo;
import org.talend.designer.gefabstractmap.resource.FontProviderMapper;

/**
 * created by wchen on 2013-1-14 Detailled comment
 *
 */
public abstract class AbstractTableContainer extends GenericFigure {

    protected AbstractTable table;

    protected Figure header;

    protected ToolBarContainer toolBarContainer;

    protected FilterContainer filterFigure;

    protected AbstractTreeSettingContainer settingContainer;

    protected Figure columnContainer;

    private TableManager tableModelManager;

    private boolean withScroll;

    protected AbstractGlobalMapContainer globalMapContainer;

    public AbstractTableContainer(TableManager tableModelManager) {
        this(tableModelManager, true);
    }

    public AbstractTableContainer(TableManager tableModelManager, boolean withScroll) {
        this.tableModelManager = tableModelManager;
        this.withScroll = withScroll;
    }

    public TableManager getTableManager() {
        return tableModelManager;
    }

    public void highLightHeader(boolean highight) {
        if (header == null || toolBarContainer == null) {
            return;
        }
        if (highight) {
            header.setBackgroundColor(ColorConstants.yellow);
            toolBarContainer.updateButtonsColor(ColorConstants.yellow);
        } else {
            header.setBackgroundColor(ColorProviderMapper.getColor(ColorInfo.ZONE_BACKGROUND_COLOR));
            toolBarContainer.updateButtonsColor(ColorProviderMapper.getColor(ColorInfo.ZONE_BACKGROUND_COLOR));
            if (settingContainer != null) {
                settingContainer.deselectTreeSettingRows();
            }
        }
    }

    public FilterContainer getFilterContainer() {
        return this.filterFigure;
    }

    protected void createContents() {
        setLayoutManager(new TableContainerLayout(tableModelManager));
        this.setBorder(new LineBorder(ColorProviderMapper.getColor(ColorInfo.COLOR_TREE_BORDER)));

        header = new Figure();
        header.setOpaque(true);
        header.setBackgroundColor(ColorConstants.tooltipBackground);
        header.setBorder(new RowBorder());
        header.setLayoutManager(new EqualWidthLayout());
        Label tableName = new Label();
        // tableName.setBorder(new LineBorder(ColorConstants.black));
        tableName.setText(getTreeDisplayName());
        tableName.setFont(FontProviderMapper.getFont(FontInfo.FONT_SYSTEM_BOLD));
        tableName.setLabelAlignment(PositionConstants.LEFT);
        tableName.setBorder(new MarginBorder(5, 10, 5, -1));

        header.add(tableName);
        toolBarContainer = createToolBarContainer();

        header.setOpaque(true);
        header.setBackgroundColor(ColorProviderMapper.getColor(ColorInfo.ZONE_BACKGROUND_COLOR));
        this.add(header);

        if (toolBarContainer != null) {
            header.add(toolBarContainer);
        }

        createTreeSettings(this);

        if (withScroll) {
            ScrollPane scroll = new ScrollPane();
            scroll.setVerticalScrollBarVisibility(ScrollPane.NEVER);
            scroll.getViewport().setContents(createTable());
            scroll.getViewport().setContentsTracksWidth(true);

            // ///
            scroll.setBackgroundColor(ColorConstants.white);
            scroll.setOpaque(true);
            this.add(scroll);
        } else {
            this.add(createTable());
        }
    }

    protected abstract AbstractTable createTable();

    protected abstract String getTreeDisplayName();

    protected abstract ToolBarContainer createToolBarContainer();

    protected abstract void createTreeSettings(Figure parent);

    public IFigure getColumnContainer() {
        return table.getTableItemContainer();
    }

    public AbstractTable getTableTree() {
        return this.table;
    }

    public AbstractTreeSettingContainer getSettingContainer() {
        return this.settingContainer;
    }

    public AbstractGlobalMapContainer getGlobalMapContainer() {
        return this.globalMapContainer;
    }
}
