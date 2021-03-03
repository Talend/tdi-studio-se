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
package org.talend.designer.core.ui.editor.properties.notes;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.commons.ui.gmf.util.ColorPalettePopup;
import org.talend.commons.ui.gmf.util.FontHelper;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.utils.image.ColorUtils;
import org.talend.core.model.process.IProcess2;
import org.talend.core.ui.properties.tab.HorizontalTabFactory;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.cmd.ChangeNoteOpacityCommand;
import org.talend.designer.core.ui.editor.cmd.ChangeNoteTextCommand;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.notes.Note;

/**
 * yzhang class global comment. Detailled comment
 */
public class BasicNotePropertyComposite extends AbstractNotePropertyComposite {

    private Button check;

    private Text text;

    private Composite composite;

    private Group adjustTextGroup, adjustLabelGroup;

    private Button leftBtn, rightBtn, centreBtn, centreLabelBtn, topBtn, bottomBtn;

    protected Group colorsAndFontsGroup;

    protected CCombo fontFamilyCombo;

    private CCombo fontSizeCombo;

    private Button fontBoldButton;

    private Button fontItalicButton;

    protected Button fillColorButton;

    protected Button fontColorButton;

    protected Button lineColorButton;

    /**
     * DOC yzhang BasicNotePropertyComposite constructor comment.
     *
     * @param parent
     * @param note
     * @param tabFactory
     */
    public BasicNotePropertyComposite(Composite parent, Note note, HorizontalTabFactory tabFactory) {
        super(parent, note, tabFactory);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.notes.AbstractNotePropertyComposite#createControl(org.eclipse.swt
     * .widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        composite = getWidgetFactory().createFlatFormComposite(parent);
        if (composite.getLayout() instanceof FormLayout) {
            FormLayout formLayout = (FormLayout) composite.getLayout();
            formLayout.spacing = 0;
        }
        FormData data;

        check = getWidgetFactory().createButton(composite, "", SWT.CHECK); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(0, 0);
        data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
        check.setLayoutData(data);
        CLabel labelLabel = getWidgetFactory().createCLabel(composite, Messages.getString("OpaqueNoteSection.Label")); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(check);
        data.top = new FormAttachment(check, 0, SWT.TOP);
        labelLabel.setLayoutData(data);

        check.setSelection(note.isOpaque());

        check.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (check.getSelection() != (note.isOpaque())) {
                    ChangeNoteOpacityCommand command = new ChangeNoteOpacityCommand(note, check.getSelection());
                    getCommandStack().execute(command);
                }
            }
        });
        createFontsAndColorsGroups(composite);

        createAlignGroups(composite);

        createTextControl(composite);

        refresh();
    }

    /**
     * DOC qwei Comment method "createTextcontrol".
     */
    private void createTextControl(Composite parent) {
        CLabel textLabel = getWidgetFactory().createCLabel(composite, Messages.getString("TextNoteSection.Label")); //$NON-NLS-1$
        //$NON-NLS-1$
        FormData data = new FormData();
        data.left = new FormAttachment(0, 0);
        data.top = new FormAttachment(colorsAndFontsGroup, 30);
        textLabel.setLayoutData(data);
        text = getWidgetFactory().createText(composite, "", SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL); //$NON-NLS-1$
        //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
        data.right = new FormAttachment(textLabel, 560);
        data.top = new FormAttachment(textLabel, 0, SWT.TOP);
        data.height = 5 * text.getLineHeight(); // 5 lines
        text.setLayoutData(data);
        text.setForeground(new Color(null, ColorUtils.stringToRGB((String) note.getPropertyValue(EParameterName.NOTETXT_COLOR
                .getName()))));
        text.setText(note.getText());

        text.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                if (!text.getText().equals(note.getText())) {
                    ChangeNoteTextCommand command = new ChangeNoteTextCommand(note, text.getText());
                    getCommandStack().execute(command);
                }
            }
        });

        text.addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(org.eclipse.swt.events.KeyEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void keyReleased(org.eclipse.swt.events.KeyEvent e) {
                // TODO Auto-generated method stub
                if (!text.getText().equals(note.getText())) {
                    ChangeNoteTextCommand command = new ChangeNoteTextCommand(note, text.getText());
                    getCommandStack().execute(command);
                }
            }

        });
        textChanged();
    }

    /**
     * DOC qwei Comment method "createAlignGroups".
     */
    private void createAlignGroups(Composite parent) {
        adjustLabelGroup = getWidgetFactory().createGroup(composite, "Adjust horizontal"); //$NON-NLS-1$
        FormData data = new FormData();
        adjustLabelGroup.setLayout(new GridLayout(3, false));
        data.left = new FormAttachment(colorsAndFontsGroup, 10);
        data.top = new FormAttachment(check, 30);
        adjustLabelGroup.setLayoutData(data);
        leftBtn = getWidgetFactory().createButton(adjustLabelGroup, "left", SWT.RADIO); //$NON-NLS-1$
        leftBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        leftBtn.setSelection((Boolean) note.getPropertyValue(EParameterName.NOTETXT_LEFT.getName()));
        rightBtn = getWidgetFactory().createButton(adjustLabelGroup, "right", SWT.RADIO); //$NON-NLS-1$
        rightBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        rightBtn.setSelection((Boolean) note.getPropertyValue(EParameterName.NOTETXT_RIGHT.getName()));
        centreBtn = getWidgetFactory().createButton(adjustLabelGroup, "centre", SWT.RADIO); //$NON-NLS-1$
        centreBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        centreBtn.setSelection((Boolean) note.getPropertyValue(EParameterName.NOTETXT_CENTER.getName()));

        adjustTextGroup = getWidgetFactory().createGroup(composite, "Adjust vertical"); //$NON-NLS-1$
        data = new FormData();
        adjustTextGroup.setLayout(new GridLayout(3, false));
        data.left = new FormAttachment(adjustLabelGroup, 10);
        data.top = new FormAttachment(check, 30);
        adjustTextGroup.setLayoutData(data);
        topBtn = getWidgetFactory().createButton(adjustTextGroup, "top", SWT.RADIO); //$NON-NLS-1$
        topBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        topBtn.setSelection((Boolean) note.getPropertyValue(EParameterName.NOTETXT_TOP.getName()));
        bottomBtn = getWidgetFactory().createButton(adjustTextGroup, "bottom", SWT.RADIO); //$NON-NLS-1$
        bottomBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        bottomBtn.setSelection((Boolean) note.getPropertyValue(EParameterName.NOTETXT_BOTTOM.getName()));
        centreLabelBtn = getWidgetFactory().createButton(adjustTextGroup, "centre", SWT.RADIO); //$NON-NLS-1$
        centreLabelBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        centreLabelBtn.setSelection((Boolean) note.getPropertyValue(EParameterName.NOTELABEL_CENTER.getName()));
        addListener();
    }

    /**
     * DOC qwei Comment method "createFontsAndColorsGroups".
     */
    protected Group createFontsAndColorsGroups(Composite parent) {
        colorsAndFontsGroup = getWidgetFactory().createGroup(parent, "Fonts and Colors"); //$NON-NLS-1$
        FormData data = new FormData();
        data.left = new FormAttachment(0, 0);
        data.top = new FormAttachment(check, 10);
        GridLayout layout = new GridLayout(1, false);
        colorsAndFontsGroup.setLayout(layout);
        colorsAndFontsGroup.setLayoutData(data);
        createFontsGroup(colorsAndFontsGroup);
        return colorsAndFontsGroup;

    }

    /**
     * Create font tool bar group
     *
     * @param parent - parent composite
     * @return - font tool bar
     */
    protected Composite createFontsGroup(Composite parent) {
        Composite familySize = getWidgetFactory().createComposite(parent);
        GridLayout layout = new GridLayout(2, false);
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        familySize.setLayout(layout);

        fontFamilyCombo = getWidgetFactory().createCCombo(familySize, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
        fontFamilyCombo.setItems(FontHelper.getFontNames());
        fontFamilyCombo.setText((String) note.getPropertyValue(EParameterName.NOTE_FONT.getName()));
        fontFamilyCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                updateFontFamily();
            }
        });

        fontSizeCombo = getWidgetFactory().createCCombo(familySize, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
        fontSizeCombo.setItems(FontHelper.getFontSizes());
        fontSizeCombo.setText((String) note.getPropertyValue(EParameterName.FONT_SIZE.getName()));
        fontSizeCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                updateFontSize();
            }
        });

        Composite toolBar = new Composite(parent, SWT.SHADOW_NONE);
        toolBar.setLayout(new GridLayout(7, false));
        toolBar.setBackground(parent.getBackground());

        fontBoldButton = new Button(toolBar, SWT.TOGGLE);
        fontBoldButton.setImage(ImageProvider.getImage(EImage.BOLD_ICON));
        fontBoldButton.getAccessible().addAccessibleListener(new AccessibleAdapter() {

            @Override
            public void getName(AccessibleEvent e) {
                e.result = "Bold";
            }
        });

        fontItalicButton = new Button(toolBar, SWT.TOGGLE);
        fontItalicButton.setImage(ImageProvider.getImage(EImage.ITALIC_ICON));
        fontItalicButton.getAccessible().addAccessibleListener(new AccessibleAdapter() {

            @Override
            public void getName(AccessibleEvent e) {
                e.result = "Italic";
            }
        });

        fontBoldButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                updateFontBold();
            }
        });

        fontItalicButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                updateFontItalic();
            }
        });

        new Label(toolBar, SWT.LEFT);

        fontColorButton = new Button(toolBar, SWT.PUSH);
        Image overlyedImage = new ColorOverlayImageDescriptor(ImageProvider.getImageDesc(EImage.FONT_COLOR_ICON).getImageData(),
                ColorUtils.stringToRGB((String) note.getPropertyValue(EParameterName.NOTETXT_COLOR.getName()))).createImage();
        fontColorButton.setImage(overlyedImage);
        fontColorButton.getAccessible().addAccessibleListener(new AccessibleAdapter() {

            @Override
            public void getName(AccessibleEvent e) {
                e.result = "Font Color";
            }
        });
        fontColorButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                ColorPalettePopup popup = new ColorPalettePopup(fontColorButton.getParent().getShell(),
                        IDialogConstants.BUTTON_BAR_HEIGHT);
                Rectangle r = fontColorButton.getBounds();
                Point location = fontColorButton.getParent().toDisplay(r.x, r.y);
                popup.open(new Point(location.x, location.y + r.height));

                if (popup.getSelectedColor() == null && !popup.useDefaultColor()) {
                    return;
                }
                // selectedColor should be null if we are to use the default color
                RGB selectedColor = popup.getSelectedColor();
                if (popup.useDefaultColor()) {
                    selectedColor = new RGB(0, 0, 0);
                }
                if (selectedColor != null) {
                    String value = selectedColor.red + ";" + selectedColor.green + ";" + selectedColor.blue; //$NON-NLS-1$ //$NON-NLS-2$
                    Command cmd = new PropertyChangeCommand(note, EParameterName.NOTETXT_COLOR.getName(), value);
                    getCommandStack().execute(cmd);
                    note.refresh();
                    Image overlyedImage = new ColorOverlayImageDescriptor(ImageProvider.getImageDesc(EImage.FONT_COLOR_ICON)
                            .getImageData(), selectedColor).createImage();
                    fontColorButton.setImage(overlyedImage);
                    text.setForeground(new Color(null, ColorUtils.stringToRGB((String) note
                            .getPropertyValue(EParameterName.NOTETXT_COLOR.getName()))));
                }
            }
        });
        new Label(toolBar, SWT.LEFT);

        lineColorButton = new Button(toolBar, SWT.PUSH);
        overlyedImage = new ColorOverlayImageDescriptor(ImageProvider.getImageDesc(EImage.LINE_COLOR_ICON).getImageData(),
                ColorUtils.stringToRGB((String) note.getPropertyValue(EParameterName.NOTE_LINECOLOR.getName()))).createImage();
        lineColorButton.setImage(overlyedImage);
        lineColorButton.getAccessible().addAccessibleListener(new AccessibleAdapter() {

            @Override
            public void getName(AccessibleEvent e) {
                e.result = "Line Color";
            }
        });
        lineColorButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                ColorPalettePopup popup = new ColorPalettePopup(lineColorButton.getParent().getShell(),
                        IDialogConstants.BUTTON_BAR_HEIGHT);
                Rectangle r = lineColorButton.getBounds();
                Point location = lineColorButton.getParent().toDisplay(r.x, r.y);
                popup.open(new Point(location.x, location.y + r.height));

                if (popup.getSelectedColor() == null && !popup.useDefaultColor()) {
                    return;
                }
                // selectedColor should be null if we are to use the default color
                RGB selectedColor = popup.getSelectedColor();
                if (popup.useDefaultColor()) {
                    selectedColor = new RGB(237, 201, 122);
                }
                if (selectedColor != null) {
                    String value = selectedColor.red + ";" + selectedColor.green + ";" + selectedColor.blue; //$NON-NLS-1$ //$NON-NLS-2$
                    Command cmd = new PropertyChangeCommand(note, EParameterName.NOTE_LINECOLOR.getName(), value);
                    getCommandStack().execute(cmd);
                    note.refresh();
                    Image overlyedImage = new ColorOverlayImageDescriptor(ImageProvider.getImageDesc(EImage.LINE_COLOR_ICON)
                            .getImageData(), selectedColor).createImage();
                    lineColorButton.setImage(overlyedImage);
                }
            }
        });

        fillColorButton = new Button(toolBar, SWT.PUSH);
        overlyedImage = new ColorOverlayImageDescriptor(ImageProvider.getImageDesc(EImage.FILL_COLOR_ICON).getImageData(),
                ColorUtils.stringToRGB((String) note.getPropertyValue(EParameterName.NOTE_COLOR.getName()))).createImage();
        fillColorButton.setImage(overlyedImage);
        fillColorButton.getAccessible().addAccessibleListener(new AccessibleAdapter() {

            @Override
            public void getName(AccessibleEvent e) {
                e.result = "Fill Color";
            }
        });
        fillColorButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                ColorPalettePopup popup = new ColorPalettePopup(fillColorButton.getParent().getShell(),
                        IDialogConstants.BUTTON_BAR_HEIGHT);
                Rectangle r = fillColorButton.getBounds();
                Point location = fillColorButton.getParent().toDisplay(r.x, r.y);
                popup.open(new Point(location.x, location.y + r.height));

                if (popup.getSelectedColor() == null && !popup.useDefaultColor()) {
                    return;
                }
                // selectedColor should be null if we are to use the default color
                RGB selectedColor = popup.getSelectedColor();
                if (popup.useDefaultColor()) {
                    selectedColor = new RGB(255, 255, 203);
                }
                if (selectedColor != null) {
                    String value = selectedColor.red + ";" + selectedColor.green + ";" + selectedColor.blue; //$NON-NLS-1$ //$NON-NLS-2$
                    Command cmd = new PropertyChangeCommand(note, EParameterName.NOTE_COLOR.getName(), value);
                    getCommandStack().execute(cmd);
                    note.refresh();
                    Image overlyedImage = new ColorOverlayImageDescriptor(ImageProvider.getImageDesc(EImage.FILL_COLOR_ICON)
                            .getImageData(), selectedColor).createImage();
                    fillColorButton.setImage(overlyedImage);
                }
            }
        });

        return toolBar;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties.notes.AbstractNotePropertyComposite#getComposite()
     */
    @Override
    public Composite getComposite() {
        return composite;
    }

    private void addListener() {
        leftBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Boolean value = leftBtn.getSelection();
                if (value) {
                    Command cmd = new PropertyChangeCommand(note, EParameterName.NOTETXT_LEFT.getName(), value);
                    getCommandStack().execute(cmd);
                    if ((Boolean) note.getPropertyValue(EParameterName.NOTETXT_RIGHT.getName())) {
                        cmd = new PropertyChangeCommand(note, EParameterName.NOTETXT_RIGHT.getName(), !value);
                        getCommandStack().execute(cmd);
                    }
                    if ((Boolean) note.getPropertyValue(EParameterName.NOTETXT_CENTER.getName())) {
                        cmd = new PropertyChangeCommand(note, EParameterName.NOTETXT_CENTER.getName(), !value);
                        getCommandStack().execute(cmd);
                    }
                    note.refresh();
                }
            }
        });
        rightBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Boolean value = rightBtn.getSelection();
                if (value) {
                    Command cmd = new PropertyChangeCommand(note, EParameterName.NOTETXT_RIGHT.getName(), value);
                    getCommandStack().execute(cmd);
                    if ((Boolean) note.getPropertyValue(EParameterName.NOTETXT_LEFT.getName())) {
                        cmd = new PropertyChangeCommand(note, EParameterName.NOTETXT_LEFT.getName(), !value);
                        getCommandStack().execute(cmd);
                    }
                    if ((Boolean) note.getPropertyValue(EParameterName.NOTETXT_CENTER.getName())) {
                        cmd = new PropertyChangeCommand(note, EParameterName.NOTETXT_CENTER.getName(), !value);
                        getCommandStack().execute(cmd);
                    }
                    note.refresh();
                }
            }
        });
        centreBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Boolean value = centreBtn.getSelection();
                if (value) {
                    Command cmd = new PropertyChangeCommand(note, EParameterName.NOTETXT_CENTER.getName(), value);
                    getCommandStack().execute(cmd);
                    if ((Boolean) note.getPropertyValue(EParameterName.NOTETXT_LEFT.getName())) {
                        cmd = new PropertyChangeCommand(note, EParameterName.NOTETXT_LEFT.getName(), !value);
                        getCommandStack().execute(cmd);
                    }
                    if ((Boolean) note.getPropertyValue(EParameterName.NOTETXT_RIGHT.getName())) {
                        cmd = new PropertyChangeCommand(note, EParameterName.NOTETXT_RIGHT.getName(), !value);
                        getCommandStack().execute(cmd);
                    }
                    note.refresh();
                }
            }
        });

        topBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Boolean value = topBtn.getSelection();
                if (value) {
                    Command cmd = new PropertyChangeCommand(note, EParameterName.NOTETXT_TOP.getName(), value);
                    getCommandStack().execute(cmd);
                    if ((Boolean) note.getPropertyValue(EParameterName.NOTELABEL_CENTER.getName())) {
                        cmd = new PropertyChangeCommand(note, EParameterName.NOTELABEL_CENTER.getName(), !value);
                        getCommandStack().execute(cmd);
                    }
                    if ((Boolean) note.getPropertyValue(EParameterName.NOTETXT_BOTTOM.getName())) {
                        cmd = new PropertyChangeCommand(note, EParameterName.NOTETXT_BOTTOM.getName(), !value);
                        getCommandStack().execute(cmd);
                    }
                    note.refresh();
                }
            }
        });
        bottomBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Boolean value = bottomBtn.getSelection();
                if (value) {
                    Command cmd = new PropertyChangeCommand(note, EParameterName.NOTETXT_BOTTOM.getName(), value);
                    getCommandStack().execute(cmd);
                    if ((Boolean) note.getPropertyValue(EParameterName.NOTELABEL_CENTER.getName())) {
                        cmd = new PropertyChangeCommand(note, EParameterName.NOTELABEL_CENTER.getName(), !value);
                        getCommandStack().execute(cmd);
                    }
                    if ((Boolean) note.getPropertyValue(EParameterName.NOTETXT_TOP.getName())) {
                        cmd = new PropertyChangeCommand(note, EParameterName.NOTETXT_TOP.getName(), !value);
                        getCommandStack().execute(cmd);
                    }
                    note.refresh();
                }
            }
        });
        centreLabelBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Boolean value = centreLabelBtn.getSelection();
                if (value) {
                    Command cmd = new PropertyChangeCommand(note, EParameterName.NOTELABEL_CENTER.getName(), value);
                    getCommandStack().execute(cmd);
                    if ((Boolean) note.getPropertyValue(EParameterName.NOTETXT_BOTTOM.getName())) {
                        cmd = new PropertyChangeCommand(note, EParameterName.NOTETXT_BOTTOM.getName(), !value);
                        getCommandStack().execute(cmd);
                    }
                    if ((Boolean) note.getPropertyValue(EParameterName.NOTETXT_TOP.getName())) {
                        cmd = new PropertyChangeCommand(note, EParameterName.NOTETXT_TOP.getName(), !value);
                        getCommandStack().execute(cmd);
                    }
                    note.refresh();
                }
            }
        });
    }

    /**
     * Update font family property
     */
    protected void updateFontFamily() {

        // Update model in response to user
        if (fontFamilyCombo.getText() != null || !fontFamilyCombo.getText().equals("")) { //$NON-NLS-1$
            String value = fontFamilyCombo.getText();
            Command cmd = new PropertyChangeCommand(note, EParameterName.NOTE_FONT.getName(), value);
            getCommandStack().execute(cmd);
            note.refresh();
            textChanged();
        }
    }

    private void updateFontSize() {
        if (fontSizeCombo.getText() != null || !fontSizeCombo.getText().equals("")) { //$NON-NLS-1$
            String value = fontSizeCombo.getText();
            Command cmd = new PropertyChangeCommand(note, EParameterName.FONT_SIZE.getName(), value);
            getCommandStack().execute(cmd);
            note.refresh();
            textChanged();
        }
    }

    private void updateFontBold() {
        Boolean value = fontBoldButton.getSelection();
        Command cmd = new PropertyChangeCommand(note, EParameterName.FONT_BOLD.getName(), value);
        getCommandStack().execute(cmd);
        note.refresh();
        textChanged();

    }

    private void updateFontItalic() {
        Boolean value = fontItalicButton.getSelection();
        Command cmd = new PropertyChangeCommand(note, EParameterName.FONT_ITALIC.getName(), value);
        getCommandStack().execute(cmd);
        note.refresh();
        textChanged();
    }

    /**
     * An image descriptor that overlays two images: a basic icon and a thin color bar underneath it
     */
    protected static class ColorOverlayImageDescriptor extends CompositeImageDescriptor {

        /** default color icon width */
        private static final Point ICON_SIZE = new Point(16, 16);

        /** the basic icon */
        private ImageData basicImgData;

        /** the color of the thin color bar */
        private RGB rgb;

        /**
         * Creates a new color menu image descriptor
         *
         * @param basicIcon The basic Image data
         * @param rgb The color bar RGB value
         */
        public ColorOverlayImageDescriptor(ImageData basicImgData, RGB rgb) {
            this.basicImgData = basicImgData;
            this.rgb = rgb;
        }

        /**
         * @see org.eclipse.jface.resource.CompositeImageDescriptor#drawCompositeImage(int, int)
         */
        @Override
        protected void drawCompositeImage(int width, int height) {

            // draw the thin color bar underneath
            if (rgb != null) {
                ImageData colorBar = new ImageData(width, height / 5, 1,

                new PaletteData(new RGB[] { rgb }));
                drawImage(colorBar, 0, height - height / 5);

            }
            // draw the base image
            drawImage(basicImgData, 0, 0);
        }

        /**
         * @see org.eclipse.jface.resource.CompositeImageDescriptor#getSize()
         */
        @Override
        protected Point getSize() {
            return ICON_SIZE;
        }
    }

    private void textChanged() {
        if ((Boolean) note.getPropertyValue(EParameterName.FONT_BOLD.getName())
                && (Boolean) note.getPropertyValue(EParameterName.FONT_ITALIC.getName())) {
            Font font = new Font(null, (String) note.getPropertyValue(EParameterName.NOTE_FONT.getName()), Integer.parseInt(note
                    .getPropertyValue(EParameterName.FONT_SIZE.getName()).toString()), SWT.BOLD | SWT.ITALIC);
            text.setFont(font);
        } else if ((Boolean) note.getPropertyValue(EParameterName.FONT_BOLD.getName())) {
            Font font = new Font(null, (String) note.getPropertyValue(EParameterName.NOTE_FONT.getName()), Integer.parseInt(note
                    .getPropertyValue(EParameterName.FONT_SIZE.getName()).toString()), SWT.BOLD);
            text.setFont(font);
        } else if ((Boolean) note.getPropertyValue(EParameterName.FONT_ITALIC.getName())) {
            Font font = new Font(null, (String) note.getPropertyValue(EParameterName.NOTE_FONT.getName()), Integer.parseInt(note
                    .getPropertyValue(EParameterName.FONT_SIZE.getName()).toString()), SWT.ITALIC);
            text.setFont(font);
        } else {
            Font font = new Font(null, (String) note.getPropertyValue(EParameterName.NOTE_FONT.getName()), Integer.parseInt(note
                    .getPropertyValue(EParameterName.FONT_SIZE.getName()).toString()), SWT.NULL);
            text.setFont(font);
        }
    }

    public void refresh() {
        IProcess2 process = note.getProcess();
        if (process != null) {
            boolean readOnly = process.isReadOnly();
            if (check != null && !check.isDisposed()) {
                check.setEnabled(!readOnly);
            }
            if (text != null && !text.isDisposed()) {
                text.setEnabled(!readOnly);
            }
            if (fontFamilyCombo != null && !fontFamilyCombo.isDisposed()) {
                fontFamilyCombo.setEnabled(!readOnly);
            }
            if (fontSizeCombo != null && !fontSizeCombo.isDisposed()) {
                fontSizeCombo.setEnabled(!readOnly);
            }

            checkButton(leftBtn, readOnly);
            checkButton(rightBtn, readOnly);
            checkButton(centreBtn, readOnly);
            checkButton(centreLabelBtn, readOnly);
            checkButton(topBtn, readOnly);
            checkButton(bottomBtn, readOnly);
            checkButton(fontBoldButton, readOnly);
            checkButton(fontItalicButton, readOnly);
            checkButton(fillColorButton, readOnly);
            checkButton(fontColorButton, readOnly);
            checkButton(lineColorButton, readOnly);

        }
    }

    private void checkButton(Button btn, boolean readOnly) {
        if (btn != null && !btn.isDisposed()) {
            btn.setEnabled(!readOnly);
        }
    }
}
