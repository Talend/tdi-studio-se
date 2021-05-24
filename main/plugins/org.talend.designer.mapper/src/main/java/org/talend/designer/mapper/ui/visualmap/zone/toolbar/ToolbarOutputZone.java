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
package org.talend.designer.mapper.ui.visualmap.zone.toolbar;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolItem;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.designer.mapper.i18n.Messages;
import org.talend.designer.mapper.managers.MapperManager;
import org.talend.designer.mapper.managers.UIManager;
import org.talend.designer.mapper.ui.visualmap.table.DataMapTableView;
import org.talend.designer.mapper.ui.visualmap.zone.Zone;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class ToolbarOutputZone extends ToolbarZone {

    private ToolItem addOutputItem;

    private ToolItem removeOutputItem;

    private ToolItem guessItem;

    // private ToolItem dieOnError;

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
        addOutputItem.setEnabled(!getMapperManager().componentIsReadOnly());
        addOutputItem.setToolTipText(Messages.getString("ToolbarOutputZone.widgetTooltip.addOutputTable")); //$NON-NLS-1$
        addOutputItem.setImage(org.talend.commons.ui.runtime.image.ImageProvider
                .getImage(org.talend.commons.ui.runtime.image.ImageProvider.getImageDesc(EImage.ADD_ICON)));

        removeOutputItem = new ToolItem(getToolBarActions(), SWT.PUSH);
        removeOutputItem.setEnabled(false);
        removeOutputItem.setImage(org.talend.commons.ui.runtime.image.ImageProvider
                .getImage(org.talend.commons.ui.runtime.image.ImageProvider.getImageDesc(EImage.MINUS_ICON)));
        removeOutputItem.setToolTipText(Messages.getString("ToolbarOutputZone.widgetTooltip.removeOutputTable")); //$NON-NLS-1$

        addCommonsComponents();

        new ToolItem(getToolBarActions(), SWT.SEPARATOR);

        guessItem = new ToolItem(getToolBarActions(), SWT.PUSH);
        guessItem.setEnabled(!getMapperManager().componentIsReadOnly());
        guessItem.setToolTipText(Messages.getString("ToolbarOutputZone.widgetTooltip.mapInputAndOutput")); //$NON-NLS-1$
        guessItem.setText(Messages.getString("ToolbarOutputZone.widgetText.autoMap")); //$NON-NLS-1$

        // dieOnError = new ToolItem(getToolBarActions(), SWT.CHECK);
        // dieOnError.setEnabled(!getMapperManager().componentIsReadOnly());
        // dieOnError.setToolTipText("tmap die on error");
        // AbstractMapComponent component = getMapperManager().getAbstractMapComponent();
        // IElementParameter elementParameter = component.getElementParameter("DIE_ON_ERROR");
        // boolean isDieOnError = false;
        // if (elementParameter != null && elementParameter.getValue() != null) {
        // isDieOnError = Boolean.valueOf(elementParameter.getValue().toString());
        // }
        // dieOnError.setSelection(isDieOnError);
        // Image image = ImageProviderMapper.getImage(isDieOnError ? ImageInfo.CHECKED_ICON : ImageInfo.UNCHECKED_ICON);
        // dieOnError.setImage(image);
        // dieOnError.setText("Die on error");

    }

    /**
     * DOC amaumont Comment method "addListeners".
     */
    private void addListeners() {
        final UIManager uiManager = getMapperManager().getUiManager();
        addOutputItem.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                getMapperManager().addOutput();
            }

        });

        removeOutputItem.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                getMapperManager().removeSelectedOutput();
            }

        });

        guessItem.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                List<DataMapTableView> outputsTablesView = getMapperManager().getUiManager().getOutputsTablesView();
                for (DataMapTableView dataMapTableView : outputsTablesView) {
                    dataMapTableView.notifyFocusLost();
                }
                // uiManager.openAutoMappingDialog();

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
     * @see org.talend.designer.mapper.ui.visualmap.zone.ToolbarZone#getZone()
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
        if (!getMapperManager().componentIsReadOnly()) {
            removeOutputItem.setEnabled(enabled);
        }
    }

}
