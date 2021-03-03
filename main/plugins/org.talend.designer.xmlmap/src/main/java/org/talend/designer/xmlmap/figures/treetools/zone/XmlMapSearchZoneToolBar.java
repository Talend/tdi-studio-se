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
package org.talend.designer.xmlmap.figures.treetools.zone;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.PositionConstants;
import org.talend.designer.gefabstractmap.figures.VarNodeTextLabel;
import org.talend.designer.gefabstractmap.figures.treetools.zone.SearchZoneToolBar;
import org.talend.designer.gefabstractmap.part.directedit.DirectEditType;
import org.talend.designer.xmlmap.parts.XmlMapDataManager;
import org.talend.designer.xmlmap.ui.tabs.MapperManager;

/**
 * DOC hcyi class global comment. Detailled comment
 */
public class XmlMapSearchZoneToolBar extends SearchZoneToolBar {

    private MapperManager mapperManger;

    private final SearchZoneMapper searchZoneMapper;

    private Map<Integer, Map<Integer, Figure>> searchMaps = new LinkedHashMap<Integer, Map<Integer, Figure>>();

    private Integer selectKey = -1;

    private VarNodeTextLabel searchText;

    public XmlMapSearchZoneToolBar(XmlMapDataManager rootModelManager) {
        super(rootModelManager);
        mapperManger = (MapperManager) rootModelManager.getMapperManger();
        searchZoneMapper = new SearchZoneMapper(mapperManger);
        createZoneContent();
    }

    @Override
    public void createZoneContent() {
        super.createZoneContent();

        Label findLabel = new Label("Find :");
        add(findLabel);

        searchText = new VarNodeTextLabel();
        searchText.setDirectEditType(DirectEditType.SERACH);
        // searchText.setText("Enter search text prefix or pattern(*,?)");
        searchText.setLabelAlignment(PositionConstants.LEFT);
        searchText.setBorder(new LineBorder(ColorConstants.gray));
        searchText.setOpaque(true);
        searchText.setBackgroundColor(ColorConstants.white);
        add(searchText);

        this.add(move_down);
        this.add(move_up);
        this.add(hightLightAll);
        move_down.setEnabled(true);
        move_up.setEnabled(true);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.gefabstractmap.figures.treetools.zone.ZoneToolBar#moveDown()
     */
    @Override
    protected void moveDown() {
        if (searchMaps.isEmpty()) {
            searchZoneMapper.search(searchMaps, searchText.getText());
        }
        if (selectKey + 1 < searchMaps.size()) {
            if (!searchZoneMapper.isHightlightAll()) {
                searchZoneMapper.hightlightAll(searchMaps, false);
            } else {
                searchZoneMapper.hightlightAll(searchMaps, true);
            }
        }
        Integer selectKeyAtTableItem = searchZoneMapper.getSelectedKeyAtSelectedTableItem(searchMaps);
        if (selectKeyAtTableItem > 0) {
            selectKey = searchZoneMapper.selectHightlight(searchMaps, selectKeyAtTableItem, "next");
        } else if (selectKeyAtTableItem == 0 && selectKey == -1) {
            selectKey = searchZoneMapper.selectHightlight(searchMaps, 0, "first");
        } else {
            selectKey = searchZoneMapper.selectHightlight(searchMaps, selectKey, "next");
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.gefabstractmap.figures.treetools.zone.ZoneToolBar#moveUp()
     */
    @Override
    protected void moveUp() {
        if (searchMaps.isEmpty()) {
            searchZoneMapper.search(searchMaps, searchText.getText());
        }
        if (selectKey > 0) {
            if (!searchZoneMapper.isHightlightAll()) {
                searchZoneMapper.hightlightAll(searchMaps, false);
            } else {
                searchZoneMapper.hightlightAll(searchMaps, true);
            }
        }
        Integer selectKeyAtTableItem = searchZoneMapper.getSelectedKeyAtSelectedTableItem(searchMaps);
        if (selectKeyAtTableItem > 0) {
            selectKey = searchZoneMapper.selectHightlight(searchMaps, selectKeyAtTableItem, "previous");
        } else if (selectKeyAtTableItem == 0 && selectKey == -1) {
            selectKey = searchZoneMapper.selectHightlight(searchMaps, 0, "first");
        } else {
            selectKey = searchZoneMapper.selectHightlight(searchMaps, selectKey, "previous");
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.gefabstractmap.figures.treetools.zone.ZoneToolBar#moveDown()
     */
    @Override
    protected void hightlightAll() {
        if (searchMaps.isEmpty()) {
            searchZoneMapper.search(searchMaps, searchText.getText());
        }
        searchZoneMapper.setHightlightAll(searchZoneMapper.isHightlightAll() ? false : true);
        searchZoneMapper.hightlightAll(searchMaps, searchZoneMapper.isHightlightAll());
    }

    public void search(String searchText) {
        // if change the search text ,need clear the data .
        if (searchMaps.size() > 0) {
            searchZoneMapper.hightlightAll(searchMaps, false);
            searchZoneMapper.setHightlightAll(false);
            searchMaps.clear();
        }
        searchZoneMapper.search(searchMaps, searchText);
        // selectKey = searchZoneMapper.selectHightlight(searchMaps, 0, "first");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.gefabstractmap.figures.treetools.zone.ZoneToolBar#getMinSizeStatus()
     */
    @Override
    protected boolean getMinSizeStatus() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.gefabstractmap.figures.treetools.zone.ZoneToolBar#isMinSizeEnable()
     */
    @Override
    protected boolean isMinSizeEnable() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.gefabstractmap.figures.treetools.zone.ZoneToolBar#minSize()
     */
    @Override
    protected void minSize() {

    }

    public VarNodeTextLabel getSearchText() {
        return this.searchText;
    }

    public SearchZoneMapper getSearchZoneMapper() {
        return this.searchZoneMapper;
    }

    public Map<Integer, Map<Integer, Figure>> getSearchMaps() {
        return this.searchMaps;
    }
}
