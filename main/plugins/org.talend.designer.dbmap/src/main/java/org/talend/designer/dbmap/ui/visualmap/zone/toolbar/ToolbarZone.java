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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.ws.WindowSystem;
import org.talend.designer.dbmap.managers.MapperManager;
import org.talend.designer.dbmap.managers.UIManager;
import org.talend.designer.dbmap.ui.image.ImageInfo;
import org.talend.designer.dbmap.ui.image.ImageProviderMapper;
import org.talend.designer.dbmap.ui.visualmap.zone.Zone;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: ToolbarZone.java 1361 2007-01-09 17:09:46Z amaumont $
 *
 */
public abstract class ToolbarZone {

    private Composite composite;

    private MapperManager mapperManager;

    private ToolBar toolBarActions;

    private ToolItem upTableButton;

    private ToolItem downTableButton;

    private ToolItem propertyButton;

    private ToolItem minimizeButton;

    protected boolean minimized;

    /**
     * DOC amaumont MatadataToolbarEditor constructor comment.
     *
     * @param parent
     * @param style
     * @param manager
     * @param metadatEditorView
     */
    public ToolbarZone(Composite parent, int style, MapperManager manager) {

        this.mapperManager = manager;
        composite = new Composite(parent, style);
        composite.setLayout(new RowLayout(SWT.HORIZONTAL));

        toolBarActions = new ToolBar(composite, SWT.FLAT | SWT.RIGHT);

        if (WindowSystem.isGTK()) {
            composite.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
            composite.setBackgroundMode(SWT.INHERIT_NONE);
            toolBarActions.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        }
    }

    public void addCommonsComponents() {

        upTableButton = new ToolItem(toolBarActions, SWT.PUSH);
        upTableButton.setEnabled(false);
        upTableButton.setToolTipText(getMoveUpTooltipText());
        upTableButton.setImage(org.talend.commons.ui.runtime.image.ImageProvider
                .getImage(org.talend.commons.ui.runtime.image.ImageProvider.getImageDesc(EImage.UP_ICON)));

        downTableButton = new ToolItem(toolBarActions, SWT.PUSH);
        downTableButton.setEnabled(false);
        downTableButton.setImage(org.talend.commons.ui.runtime.image.ImageProvider
                .getImage(org.talend.commons.ui.runtime.image.ImageProvider.getImageDesc(EImage.DOWN_ICON)));
        downTableButton.setToolTipText(getMoveDownTooltipText());

        new ToolItem(getToolBarActions(), SWT.SEPARATOR);

        if (this instanceof ToolbarInputZone) {
            propertyButton = new ToolItem(toolBarActions, SWT.PUSH);
            propertyButton.setImage(ImageProviderMapper.getImage(ImageInfo.PROPERTY_TOOL_ICON));
            propertyButton.setToolTipText("Setup the configurations of elt map"); //$NON-NLS-1$
            propertyButton.setEnabled(!getMapperManager().componentIsReadOnly());
        }

        minimizeButton = new ToolItem(toolBarActions, SWT.PUSH);
        minimizeButton.setEnabled(false);

        addCommonsComponentListeners();
    }

    /**
     * DOC amaumont Comment method "addListeners".
     */
    private void addCommonsComponentListeners() {
        final UIManager uiManager = mapperManager.getUiManager();
        upTableButton.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                uiManager.moveSelectedTable(getZone(), true);
            }

        });

        downTableButton.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                uiManager.moveSelectedTable(getZone(), false);
            }

        });

        minimizeButton.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                setMinimizeButtonState(!minimized);
                uiManager.minimizeAllTables(getZone(), minimized, minimizeButton);
            }

        });

        if (propertyButton != null) {
            propertyButton.addListener(SWT.Selection, new Listener() {

                public void handleEvent(Event event) {
                    uiManager.openPropertySetDialog();
                }
            });
        }
    }

    public Composite getComposite() {
        return this.composite;
    }

    public abstract String getMinimizeTooltipText();

    public abstract String getRestoreTooltipText();

    public abstract String getMoveUpTooltipText();

    public abstract String getMoveDownTooltipText();

    public abstract Zone getZone();

    protected boolean isMinimized() {
        return this.minimized;
    }

    protected ToolBar getToolBarActions() {
        return this.toolBarActions;
    }

    protected MapperManager getMapperManager() {
        return this.mapperManager;
    }

    /**
     *
     * Set the state of the minimize button.
     *
     * @param isMinimized true to set minized state else false
     */
    public void setMinimizeButtonState(boolean isMinimized) {
        if (isMinimized) {
            minimizeButton.setSelection(true);
            minimizeButton.setImage(ImageProviderMapper.getImage(ImageInfo.RESTORE_ICON));
            minimizeButton.setToolTipText(getRestoreTooltipText());
        } else {
            minimizeButton.setSelection(false);
            minimizeButton.setImage(ImageProviderMapper.getImage(ImageInfo.MINIMIZE_ICON));
            minimizeButton.setToolTipText(getMinimizeTooltipText());
        }
        minimized = isMinimized;
    }

    public void setEnabledMoveTableButton(boolean moveUpButton, boolean enabled) {
        if (moveUpButton) {
            upTableButton.setEnabled(enabled);
        } else {
            downTableButton.setEnabled(enabled);
        }
    }

    public void setEnabledMinimizeTablesButton(boolean enabled) {
        minimizeButton.setEnabled(enabled);
    }

}
