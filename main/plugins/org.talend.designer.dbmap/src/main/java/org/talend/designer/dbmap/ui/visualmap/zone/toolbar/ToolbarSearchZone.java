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
package org.talend.designer.dbmap.ui.visualmap.zone.toolbar;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.designer.abstractmap.model.tableentry.ITableEntry;
import org.talend.designer.dbmap.managers.MapperManager;
import org.talend.designer.dbmap.ui.visualmap.table.EntryState;

/**
 * DOC hcyi class global comment. Detailled comment
 */
public class ToolbarSearchZone {

    private Composite composite;

    private MapperManager mapperManager;

    private Text searchText;

    private ToolItem upTableButton;

    private ToolItem downTableButton;

    private ToolItem hightLightAllButton;

    private Integer selectKey = -1;

    private Map<Integer, Map<Integer, ITableEntry>> searchMaps = new LinkedHashMap<Integer, Map<Integer, ITableEntry>>();

    public ToolbarSearchZone(Composite parent, int style, MapperManager manager) {
        this.mapperManager = manager;
        composite = new Composite(parent, style);
        composite.setLayout(new GridLayout(3, false));
        addCommonsComponents();
    }

    public void addCommonsComponents() {
        Label findLabel = new Label(composite, SWT.NONE);
        findLabel.setText("Find :");

        searchText = new Text(composite, SWT.BORDER);
        GridData gridData = new GridData();
        gridData.widthHint = 150;
        searchText.setBackground(EntryState.NONE.getColor());
        searchText.setLayoutData(gridData);
        searchText.setToolTipText("Enter search text prefix or pattern(*,?,or camel case).");

        ToolBar toolBarActions = new ToolBar(composite, SWT.FLAT | SWT.RIGHT);
        toolBarActions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        downTableButton = new ToolItem(toolBarActions, SWT.PUSH);
        downTableButton.setImage(org.talend.commons.ui.runtime.image.ImageProvider
                .getImage(org.talend.commons.ui.runtime.image.ImageProvider.getImageDesc(EImage.DOWN_ICON)));
        // downTableButton.setText("Next");
        downTableButton.setToolTipText("next");

        upTableButton = new ToolItem(toolBarActions, SWT.PUSH);
        upTableButton.setImage(org.talend.commons.ui.runtime.image.ImageProvider
                .getImage(org.talend.commons.ui.runtime.image.ImageProvider.getImageDesc(EImage.UP_ICON)));
        // upTableButton.setText("Previous");
        upTableButton.setToolTipText("previous");

        hightLightAllButton = new ToolItem(toolBarActions, SWT.CHECK);
        hightLightAllButton.setImage(org.talend.commons.ui.runtime.image.ImageProvider
                .getImage(org.talend.commons.ui.runtime.image.ImageProvider.getImageDesc(EImage.HIGHTLIGHT_ICON)));
        // hightLightAllButton.setText("Hightlight All");
        hightLightAllButton.setToolTipText("hightlight all");
        hightLightAllButton.setSelection(false);
        // Add Listeners
        addCommonsComponentListeners();
    }

    private void addCommonsComponentListeners() {
        final SearchZoneMapper searchZoneMapper = new SearchZoneMapper(mapperManager);

        searchText.addKeyListener(new KeyListener() {

            public void keyPressed(KeyEvent e) {
                // if change the search text ,need clear the data .
                hightLightAllButton.setSelection(false);
                searchZoneMapper.setHightlightAll(hightLightAllButton.getSelection());
                if (searchMaps.size() > 0) {
                    searchZoneMapper.hightlightAll(searchMaps, false);
                    searchMaps.clear();
                }
                if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
                    e.doit = false;
                    searchZoneMapper.search(searchMaps, searchText.getText());
                    selectKey = searchZoneMapper.selectHightlight(searchMaps, 0, "first");
                }
            }

            public void keyReleased(KeyEvent e) {
            }
        });

        downTableButton.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
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
        });

        upTableButton.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
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
        });

        hightLightAllButton.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                if (hightLightAllButton.getSelection()) {
                    searchMaps.clear();
                    searchZoneMapper.search(searchMaps, searchText.getText());
                }
                searchZoneMapper.setHightlightAll(hightLightAllButton.getSelection());
                searchZoneMapper.hightlightAll(searchMaps, hightLightAllButton.getSelection());
            }
        });
    }

    public Composite getComposite() {
        return this.composite;
    }

    protected MapperManager getMapperManager() {
        return this.mapperManager;
    }
}
