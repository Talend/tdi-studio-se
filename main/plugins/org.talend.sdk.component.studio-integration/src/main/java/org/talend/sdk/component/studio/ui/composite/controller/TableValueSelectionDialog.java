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
package org.talend.sdk.component.studio.ui.composite.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.talend.core.ui.composite.ElementsSelectionComposite;

/**
 * Dialog implementation to choose multiple values from suggested values.
 * This dialog may be used together with TABLE ElementParameter
 */
public class TableValueSelectionDialog extends Dialog {

    /**
     * Extension of ElementsSelectionComposite parameterized with Table row type.
     * ElementsSelectionComposite provides checkboxed tree view with filtering capabilities
     */
    private ElementsSelectionComposite<Map<String, Object>> selectionComposite;

    /**
     * Name of column, which stores labels
     */
    private final String tableKey;

    /**
     * A list of all suggested values (in a form of Table parameter value)
     */
    private final List<Map<String, Object>> suggestedValues;

    /**
     * A list of chosen (checked) values
     */
    private List<Map<String, Object>> chosenValues;

    /**
     * Creates TableValueSelectionDialog, sets all suggested values and initially checked values (previously chosen values)
     *
     * @param parentShell parent shell
     * @param labelsColumn name of column, which stores labels
     * @param suggestedValues all suggested values
     * @param chosenValues initially checked values
     */
    public TableValueSelectionDialog(final Shell parentShell, final String labelsColumn,
                                     final  List<Map<String, Object>> suggestedValues,
                                     final List<Map<String, Object>> chosenValues) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MIN | SWT.APPLICATION_MODAL);
        this.tableKey = labelsColumn;
        this.suggestedValues = suggestedValues != null ? new ArrayList<>(suggestedValues) : Collections.emptyList();
        this.chosenValues = chosenValues != null ? new ArrayList<>(chosenValues) : Collections.emptyList();
    }

    @Override
    protected void configureShell(final Shell newShell) {
        super.configureShell(newShell);
        newShell.setSize(450, 550);
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        final Composite dialogArea = (Composite) super.createDialogArea(parent);
        final Composite composite = new Composite(dialogArea, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setLayout(new GridLayout());

        createSelectionComposite(composite);

        return dialogArea;
    }

    private void createSelectionComposite(final Composite parent) {
        selectionComposite = new ElementsSelectionComposite<Map<String, Object>>(parent) {

            @Override
            protected IBaseLabelProvider getLabelProvider() {
                return new LabelProvider() {

                    /**
                     * Converts Table row {@code Map<String, Object>} to String value, which be displayed in dialog
                     * checkboxed tree
                     *
                     * @param row table row
                     * @return String value to display
                     */
                    @Override
                    public String getText(final Object item) {
                        Objects.requireNonNull(item, "row should not be null");
                        final Map<String, Object> row = (Map<String, Object>) item;
                        final Object text = row.get(tableKey);
                        return text.toString();
                    }

                    /**
                     * @return null as no image should be displayed
                     */
                    @Override
                    public Image getImage(final Object obj) {
                        return null;
                    }
                };
            };

            /**
             * Returns selected items.
             * This method is called by {@link ElementsSelectionComposite#setCheckedState()} to set checked items
             *
             * @param selectedElementLabels labels of selected values (is not used in this implementation)
             * @return selected items
             */
            @Override
            protected List<Map<String, Object>> getInitSelectedElements(final List<String> selectedElementLabels) {
                return chosenValues;
            }

        };

        selectionComposite.setMultipleSelection(false);
        selectionComposite.create();
        selectionComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        selectionComposite.setViewerData(suggestedValues);
        selectionComposite.setCheckedState();
    }

    @Override
    protected void initializeBounds() {
        super.initializeBounds();
        final Point size = getShell().getSize();
        final Point location = getInitialLocation(size);
        getShell().setBounds(getConstrainedShellBounds(new Rectangle(location.x, location.y, size.x, size.y)));
    }

    /**
     * Saves selected elements after OK button pressed
     */
    @Override
    protected void okPressed() {
        chosenValues = selectionComposite.getSelectedElements();
        super.okPressed();
    }

    /**
     * Getter for chosen values.
     * To be called after OK button is pushed
     *
     * @return chosen values
     */
    public List<Map<String, Object>> getChosenValues() {
        return chosenValues;
    }
}
