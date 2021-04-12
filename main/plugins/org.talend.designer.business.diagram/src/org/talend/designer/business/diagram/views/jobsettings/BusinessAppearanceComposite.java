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
package org.talend.designer.business.diagram.views.jobsettings;

import java.util.Iterator;

import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.properties.sections.appearance.ConnectionAppearancePropertySection;
import org.eclipse.gmf.runtime.diagram.ui.properties.sections.appearance.DiagramColorsAndFontsPropertySection;
import org.eclipse.gmf.runtime.diagram.ui.properties.sections.appearance.ShapeColorsAndFontsPropertySection;
import org.eclipse.gmf.runtime.diagram.ui.properties.sections.appearance.TextAlignmentPropertySection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;
import org.talend.core.CorePlugin;
import org.talend.core.i18n.Messages;
import org.talend.core.model.business.BusinessAlignment;
import org.talend.core.model.business.BusinessType;
import org.talend.designer.business.diagram.custom.IDiagramModelService;
import org.talend.designer.core.ui.views.jobsettings.tabs.AbstractTabComposite;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class BusinessAppearanceComposite extends AbstractTabComposite {

    private Button leftBtn, rightBtn, hCentre, vCentre, topBtn, bottomBtn;

    private Group verticalGroup, horizontalGroup;

    private ISelection selected;

    public BusinessAppearanceComposite(Composite parent, int style, TabbedPropertySheetWidgetFactory widgetFactory,
            ISelection selection) {
        super(parent, style, widgetFactory, null);
        this.selected = selection;

        Composite composite = widgetFactory.createFlatFormComposite(this);

        FormLayout layout = new FormLayout();
        setLayout(layout);

        FormData thisFormData = new FormData();
        thisFormData.left = new FormAttachment(0, 0);
        thisFormData.right = new FormAttachment(100, 0);
        thisFormData.top = new FormAttachment(0, 0);
        setLayoutData(thisFormData);

        thisFormData = new FormData();
        thisFormData.left = new FormAttachment(0, 0);
        thisFormData.right = new FormAttachment(100, 0);
        thisFormData.top = new FormAttachment(0, 0);
        composite.setLayoutData(thisFormData);
        this.setBackground(composite.getBackground());

        BusinessType type = CorePlugin.getDefault().getDiagramModelService().getBusinessModelType(
                ((IStructuredSelection) selection).getFirstElement());
        if (type != null) {
            switch (type) {
            case CONNECTION:
                AppearanceSection appearance = new AppearanceSection();
                appearance.setInput(getEditor(), selection);
                appearance.createControls(composite, null);
                appearance.refresh();
                break;
            case SHAP: {
                ShapAppearance colorsAndFontsAndLine = new ShapAppearance();
                colorsAndFontsAndLine.setInput(getEditor(), selection);
                colorsAndFontsAndLine.createControls(composite, null);
                colorsAndFontsAndLine.refresh();
                createTextAlignmentGroup(composite);
            }
                break;
            case NOTE:
                // selection = new
                // StructuredSelection(CorePlugin.getDefault().getDiagramModelService().getBusinessEditorProcess());
                NoteAppearanceSection notediagramAppearance = new NoteAppearanceSection();
                notediagramAppearance.setInput(getEditor(), selection);
                notediagramAppearance.createControls(composite, null);
                notediagramAppearance.refresh();
                break;
            case PROCESS:
                selection = new StructuredSelection(CorePlugin.getDefault().getDiagramModelService().getBusinessEditorProcess());
                DiagramAppearanceSection diagramAppearance = new DiagramAppearanceSection();
                diagramAppearance.setInput(getEditor(), selection);
                diagramAppearance.createControls(composite, null);

                diagramAppearance.refresh();
                createTextAlignmentGroup(composite);

            }
            // for NamedEditPart
        } else {
            selection = new StructuredSelection(CorePlugin.getDefault().getDiagramModelService().getBusinessEditorProcess());
            DiagramAppearanceSection diagramAppearance = new DiagramAppearanceSection();
            diagramAppearance.setInput(getEditor(), selection);
            diagramAppearance.createControls(composite, null);
            diagramAppearance.refresh();

        }

    }

    private class AppearanceSection extends ConnectionAppearancePropertySection {

        @Override
        public TabbedPropertySheetWidgetFactory getWidgetFactory() {
            return widgetFactory;
        }

    }

    private class NoteAppearanceSection extends DiagramColorsAndFontsPropertySection {

        @Override
        public TabbedPropertySheetWidgetFactory getWidgetFactory() {
            return widgetFactory;
        }

        @Override
        public IGraphicalEditPart getSingleInput() {
            return (IGraphicalEditPart) getPrimarySelection();
        }

        @Override
        protected Iterator getInputIterator() {
            return getInput().iterator();
        }
    }

    private class DiagramAppearanceSection extends DiagramColorsAndFontsPropertySection {

        @Override
        public TabbedPropertySheetWidgetFactory getWidgetFactory() {
            return widgetFactory;
        }

    }

    private class ShapAppearance extends ShapeColorsAndFontsPropertySection {

        @Override
        public TabbedPropertySheetWidgetFactory getWidgetFactory() {
            return widgetFactory;
        }
    }

    private class TextShapAppearance extends TextAlignmentPropertySection {

        @Override
        public TabbedPropertySheetWidgetFactory getWidgetFactory() {
            return widgetFactory;
        }

    }

    private void createTextAlignmentGroup(Composite nearby) {
        Composite alignComposite = widgetFactory.createFlatFormComposite(this);
        alignComposite.setLayout(new GridLayout());
        FormData thisFormData = new FormData();
        thisFormData = new FormData();
        thisFormData.left = new FormAttachment(2, 0);
        thisFormData.right = new FormAttachment(100, 0);
        thisFormData.top = new FormAttachment(nearby, 2);
        thisFormData.bottom = new FormAttachment(100, 0);
        alignComposite.setLayoutData(thisFormData);
        Group textAlignment = widgetFactory.createGroup(alignComposite, Messages
                .getString("BusinessAppearanceComposite.textAlignmentGroup"));//$NON-NLS-1$
        textAlignment.setBackground(getBackground());
        textAlignment.setLayout(new GridLayout(2, false));

        horizontalGroup = widgetFactory.createGroup(textAlignment, Messages
                .getString("BusinessAppearanceComposite.textAlignment.horizontal")); //$NON-NLS-1$
        horizontalGroup.setLayout(new GridLayout(3, false));
        leftBtn = widgetFactory.createButton(horizontalGroup, Messages
                .getString("BusinessAppearanceComposite.textAlignment.horizontal.left"), SWT.RADIO); //$NON-NLS-1$
        hCentre = widgetFactory.createButton(horizontalGroup, Messages
                .getString("BusinessAppearanceComposite.textAlignment.horizontal.centre"), SWT.RADIO); //$NON-NLS-1$
        rightBtn = widgetFactory.createButton(horizontalGroup, Messages
                .getString("BusinessAppearanceComposite.textAlignment.horizontal.right"), SWT.RADIO); //$NON-NLS-1$

        verticalGroup = widgetFactory.createGroup(textAlignment, Messages
                .getString("BusinessAppearanceComposite.textAlignment.vertical")); //$NON-NLS-1$
        verticalGroup.setLayout(new GridLayout(3, false));
        topBtn = widgetFactory.createButton(verticalGroup, Messages
                .getString("BusinessAppearanceComposite.textAlignment.vertical.top"), SWT.RADIO); //$NON-NLS-1$
        vCentre = widgetFactory.createButton(verticalGroup, Messages
                .getString("BusinessAppearanceComposite.textAlignment.vertical.centre"), SWT.RADIO); //$NON-NLS-1$
        bottomBtn = widgetFactory.createButton(verticalGroup, Messages
                .getString("BusinessAppearanceComposite.textAlignment.vertical.bottom"), SWT.RADIO); //$NON-NLS-1$

        Object obj = ((IStructuredSelection) selected).getFirstElement();
        IDiagramModelService service = CorePlugin.getDefault().getDiagramModelService();

        if (BusinessType.SHAP.equals(service.getBusinessModelType(obj))) {
            String horizontalAlignment = service.getBusinessItemAlignment(obj, BusinessAlignment.HORIZONTAL);
            if (horizontalAlignment == null) {
                horizontalAlignment = BusinessAlignment.LEFT.toString();
            }

            leftBtn.setSelection(BusinessAlignment.LEFT.toString().equals(horizontalAlignment));
            hCentre.setSelection(BusinessAlignment.HCENTRE.toString().equals(horizontalAlignment));
            rightBtn.setSelection(BusinessAlignment.RIGHT.toString().equals(horizontalAlignment));

            String verticalAlignment = service.getBusinessItemAlignment(obj, BusinessAlignment.VERTICAL);
            if (verticalAlignment == null) {
                verticalAlignment = BusinessAlignment.TOP.toString();
            }

            topBtn.setSelection(BusinessAlignment.TOP.toString().equals(verticalAlignment));
            vCentre.setSelection(BusinessAlignment.VCENTRE.toString().equals(verticalAlignment));
            bottomBtn.setSelection(BusinessAlignment.BOTTOM.toString().equals(verticalAlignment));

        }

        addListener();

    }

    private void addListener() {
        leftBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Boolean value = leftBtn.getSelection();
                if (value) {
                    ChangModels(BusinessAlignment.LEFT, BusinessAlignment.HORIZONTAL);
                }
            }

        });
        hCentre.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Boolean value = hCentre.getSelection();
                if (value) {
                    ChangModels(BusinessAlignment.HCENTRE, BusinessAlignment.HORIZONTAL);
                }
            }

        });
        rightBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Boolean value = rightBtn.getSelection();
                if (value) {
                    ChangModels(BusinessAlignment.RIGHT, BusinessAlignment.HORIZONTAL);
                }
            }

        });

        topBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Boolean value = topBtn.getSelection();
                if (value) {
                    ChangModels(BusinessAlignment.TOP, BusinessAlignment.VERTICAL);
                }
            }

        });

        vCentre.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Boolean value = vCentre.getSelection();
                if (value) {
                    ChangModels(BusinessAlignment.VCENTRE, BusinessAlignment.VERTICAL);
                }
            }

        });
        bottomBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Boolean value = bottomBtn.getSelection();
                if (value) {
                    ChangModels(BusinessAlignment.BOTTOM, BusinessAlignment.VERTICAL);
                }
            }

        });
    }

    private void ChangModels(BusinessAlignment alignment, BusinessAlignment type) {
        Object obj = ((IStructuredSelection) selected).getFirstElement();
        IDiagramModelService service = CorePlugin.getDefault().getDiagramModelService();
        if (BusinessType.SHAP.equals(service.getBusinessModelType(obj))) {
            service.setBusinessItemAlignment(alignment, type, obj);

        } else if (BusinessType.PROCESS.equals(service.getBusinessModelType(obj))) {
            service.setBusinessItemsAlignment(alignment, type, obj);
        }
    }

}
