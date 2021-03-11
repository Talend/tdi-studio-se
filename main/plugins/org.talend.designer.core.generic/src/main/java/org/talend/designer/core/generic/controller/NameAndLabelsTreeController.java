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
package org.talend.designer.core.generic.controller;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.ui.composite.ElementsSelectionComposite;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.daikon.NamedThing;
import org.talend.designer.core.generic.model.GenericElementParameter;
import org.talend.designer.core.generic.utils.ComponentsUtils;
import org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController;

/**
 *
 * created by ycbai on 2015年10月9日 Detailled comment
 *
 */
public class NameAndLabelsTreeController extends AbstractElementPropertySectionController {

    public NameAndLabelsTreeController(IDynamicProperty dp) {
        super(dp);
    }

    @Override
    public Control createControl(Composite subComposite, final IElementParameter param, int numInRow, int nbInRow, int top,
            Control lastControl) {
        FormData data = new FormData();
        data.left = new FormAttachment(0, 0);
        data.top = new FormAttachment(0, 0);
        data.right = new FormAttachment(100, 0);
        data.bottom = new FormAttachment(100, 0);
        Composite parentComp = new Composite(subComposite, SWT.NONE);
        parentComp.setLayoutData(data);
        parentComp.setLayout(new GridLayout());
        ElementsSelectionComposite<NamedThing> selectionComposite = new ElementsSelectionComposite<NamedThing>(parentComp) {

            @Override
            protected IBaseLabelProvider getLabelProvider() {
                return new LabelProvider() {

                    @Override
                    public String getText(Object obj) {
                        NamedThing nal = (NamedThing) obj;
                        return nal.getDisplayName();
                    }

                    @Override
                    public Image getImage(Object obj) {
                        return null;
                    }
                };
            };

            @Override
            protected void doSelectionChanged() {
                param.setValue(getSelectedElements());
            }

            @Override
            protected List<String> getSelectedElementLabels() {
                List<String> labels = new ArrayList<>();
                Object value = param.getValue();
                if (value instanceof List) {
                    List<?> values = (List<?>) value;
                    for (Object valueObj : values) {
                        if (valueObj instanceof NamedThing) {
                            labels.add(((NamedThing) valueObj).getName());
                        }
                    }
                    return labels;
                }
                return null;
            }

            @Override
            protected List<NamedThing> getInitSelectedElements(List<String> selectedElementLabels) {
                List<NamedThing> selectedElements = new ArrayList<>();
                List<NamedThing> viewerDatas = getViewerData();
                for (NamedThing viewerData : viewerDatas) {
                    if (selectedElementLabels.contains(viewerData.getName())) {
                        selectedElements.add(viewerData);
                    }
                }
                return selectedElements;
            }

        }.setShowToolbar(true).create();
        selectionComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        if (param instanceof GenericElementParameter) {
            selectionComposite.setViewerData(ComponentsUtils.getFormalPossibleValues((GenericElementParameter) param));
        }
        selectionComposite.setCheckedState();
        return parentComp;
    }

    @Override
    public void refresh(IElementParameter param, boolean check) {
        // TODO Auto-generated method stub
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // TODO Auto-generated method stub
    }

    @Override
    public int estimateRowSize(Composite subComposite, IElementParameter param) {
        return 0;
    }

}
