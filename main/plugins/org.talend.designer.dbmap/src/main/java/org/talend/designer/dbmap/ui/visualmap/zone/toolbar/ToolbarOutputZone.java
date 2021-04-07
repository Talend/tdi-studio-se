// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolItem;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.designer.dbmap.i18n.Messages;
import org.talend.designer.dbmap.managers.MapperManager;
import org.talend.designer.dbmap.ui.visualmap.zone.Zone;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: ToolbarOutputZone.java 1782 2007-02-03 07:57:38Z bqian $
 *
 */
public class ToolbarOutputZone extends ToolbarZone {

    private ToolItem addOutputItem;

    private ToolItem removeOutputItem;

    private ToolItem guessItem;

    public static final String MINIMIZE_TOOLTIP = Messages.getString("ToolbarOutputZone.minimizeTooltip"); //$NON-NLS-1$

    public static final String RESTORE_TOOLTIP = Messages.getString("ToolbarOutputZone.restorTooltip"); //$NON-NLS-1$

    private static final String MOVE_UP_TOOLTIP = Messages.getString("ToolbarOutputZone.moveupTooltip"); //$NON-NLS-1$

    private static final String MOVE_DOWN_TOOLTIP = Messages.getString("ToolbarOutputZone.movedownTooltip"); //$NON-NLS-1$

    /**
     * DOC amaumont MatadataToolbarEditor constructor comment.
     *
     * @param parent
     * @param style
     * @param manager
     * @param metadatEditorView
     */
    public ToolbarOutputZone(Composite parent, int style, MapperManager manager) {
        super(parent, style, manager);
        createComponents();
        addListeners();
    }

    /**
     * DOC amaumont Comment method "createComponents".
     */
    private void createComponents() {

        addOutputItem = new ToolItem(getToolBarActions(), SWT.PUSH);
        addOutputItem.setToolTipText(Messages.getString("ToolbarOutputZone.widgetTooltip.addOutputTable")); //$NON-NLS-1$
        addOutputItem.setImage(org.talend.commons.ui.runtime.image.ImageProvider
                .getImage(org.talend.commons.ui.runtime.image.ImageProvider.getImageDesc(EImage.ADD_ICON)));
        addOutputItem.setEnabled(!mapperManager.componentIsReadOnly());

        removeOutputItem = new ToolItem(getToolBarActions(), SWT.PUSH);
        removeOutputItem.setEnabled(false);
        removeOutputItem.setImage(org.talend.commons.ui.runtime.image.ImageProvider
                .getImage(org.talend.commons.ui.runtime.image.ImageProvider.getImageDesc(EImage.MINUS_ICON)));
        removeOutputItem.setToolTipText(Messages.getString("ToolbarOutputZone.widgetTooltip.removeOutputTable")); //$NON-NLS-1$

        addCommonsComponents();

        new ToolItem(getToolBarActions(), SWT.SEPARATOR);

        guessItem = new ToolItem(getToolBarActions(), SWT.PUSH);
        guessItem.setToolTipText(Messages.getString("ToolbarOutputZone.widgetTooltip.mapInputAndOutput")); //$NON-NLS-1$
        guessItem.setText(Messages.getString("ToolbarOutputZone.widgetText.autoMap")); //$NON-NLS-1$
        guessItem.setEnabled(!mapperManager.componentIsReadOnly());

    }

    /**
     * DOC amaumont Comment method "addListeners".
     */
    private void addListeners() {
        // final UIManager uiManager = getMapperManager().getUiManager();
        addOutputItem.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event event) {
                getMapperManager().addOutput();
            }

        });

        removeOutputItem.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event event) {
                getMapperManager().removeSelectedOutputTable();
            }

        });

        guessItem.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event event) {
                getMapperManager().mapAutomaticallly();
            }

        });

    }

    @Override
    public String getMinimizeTooltipText() {
        return MINIMIZE_TOOLTIP;
    }

    @Override
    public String getRestoreTooltipText() {
        return RESTORE_TOOLTIP;
    }

    @Override
    public String getMoveUpTooltipText() {
        return MOVE_UP_TOOLTIP;
    }

    @Override
    public String getMoveDownTooltipText() {
        return MOVE_DOWN_TOOLTIP;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.dbmap.ui.visualmap.zone.ToolbarZone#getZone()
     */
    @Override
    public Zone getZone() {
        return Zone.OUTPUTS;
    }

    /**
     * DOC amaumont Comment method "setEnabledRemoveTableButton".
     *
     * @param b
     */
    public void setEnabledRemoveTableButton(boolean enabled) {
        removeOutputItem.setEnabled(enabled);
    }

}
