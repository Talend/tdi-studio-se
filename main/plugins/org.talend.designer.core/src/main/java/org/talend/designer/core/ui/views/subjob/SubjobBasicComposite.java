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
package org.talend.designer.core.ui.views.subjob;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.talend.commons.ui.utils.image.ColorUtils;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.Element;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainer;
import org.talend.designer.core.ui.views.jobsettings.AbstractPreferenceComposite;
import org.talend.designer.core.utils.DesignerColorUtils;

/**
 * ggu class global comment. Detailled comment
 */
public class SubjobBasicComposite extends AbstractPreferenceComposite {

    private static final IPreferenceStore PREFERENCE_STORE = DesignerPlugin.getDefault().getPreferenceStore();

    public SubjobBasicComposite(Composite parentComposite, int styles, Element element) {
        super(parentComposite, styles, EComponentCategory.BASIC, element, true);
        setDialogTitle(Messages.getString("SubjobBasicComposite.DialogTitle")); //$NON-NLS-1$
    }

    @Override
    public void addComponents(boolean forceRedraw, boolean reInitialize, int height) {
        super.addComponents(forceRedraw, reInitialize, height);
        if (reloadBtn != null) {
            reloadBtn.setText(Messages.getString("SubjobBasicComposite.RestoreLabel")); //$NON-NLS-1$
            reloadBtn.setToolTipText(Messages.getString("SubjobBasicComposite.RestoreTip")); //$NON-NLS-1$
        }
        if (saveBtn != null) {
            saveBtn.setText(Messages.getString("SubjobBasicComposite.SaveLabel")); //$NON-NLS-1$
            saveBtn.setToolTipText(Messages.getString("SubjobBasicComposite.SaveTip")); //$NON-NLS-1$
        }
    }

    @Override
    protected boolean needApplyToChildren() {
        return false;
    }

    @Override
    protected void onReloadButtonClick() {
        if (!sameASPreference()) {
            super.onReloadButtonClick();
        }
    }

    @Override
    protected void onSaveButtonClick() {
        if (!sameASPreference()) {
            super.onSaveButtonClick();
        }
    }

    private boolean sameASPreference() {
        String subjobColor = getPropertyColor();
        String pSubjobColor = ColorUtils.transform(getPreferenceColor());

        String subjobTitleColor = getPropertyTitleColor();
        String pSubjobTitleColor = ColorUtils.transform(getPreferenceTitleColor());

        if (pSubjobColor != null && pSubjobColor.equals(subjobColor) && pSubjobTitleColor != null
                && pSubjobTitleColor.equals(subjobTitleColor)) {
            return true; // same as preference
        }
        return false;
    }

    private String getPreferenceColor() {
        return PREFERENCE_STORE.getString(DesignerColorUtils.SUBJOB_COLOR_NAME);
    }

    private String getPreferenceTitleColor() {
        return PREFERENCE_STORE.getString(DesignerColorUtils.SUBJOB_TITLE_COLOR_NAME);
    }

    private String getPropertyColor() {
        if (getElement() == null) {
            return null;
        }
        return (String) getElement().getPropertyValue(EParameterName.SUBJOB_COLOR.getName());
    }

    private String getPropertyTitleColor() {
        if (getElement() == null) {
            return null;
        }
        return (String) getElement().getPropertyValue(EParameterName.SUBJOB_TITLE_COLOR.getName());
    }

    @Override
    protected void onReloadPreference() {
        if (getElement() != null) {
            String colorStr = getPreferenceColor();
            RGB subjobColor = ColorUtils.parseStringToRGB(colorStr, DesignerColorUtils.SUBJOB_COLOR);
            getElement().setPropertyValue(EParameterName.SUBJOB_COLOR.getName(), ColorUtils.getRGBValue(subjobColor));

            colorStr = getPreferenceTitleColor();
            RGB subjobTitleColor = ColorUtils.parseStringToRGB(colorStr, DesignerColorUtils.SUBJOB_TITLE_COLOR);
            getElement().setPropertyValue(EParameterName.SUBJOB_TITLE_COLOR.getName(), ColorUtils.getRGBValue(subjobTitleColor));

        }
    }

    @Override
    protected void onSavePreference() {
        if (getElement() != null) {
            RGB subjobColor = ColorUtils.parseStringToRGB(getPropertyColor(), DesignerColorUtils.SUBJOB_COLOR);
            PreferenceConverter.setValue(PREFERENCE_STORE, DesignerColorUtils.SUBJOB_COLOR_NAME, subjobColor);

            subjobColor = ColorUtils.parseStringToRGB(getPropertyTitleColor(), DesignerColorUtils.SUBJOB_TITLE_COLOR);
            PreferenceConverter.setValue(PREFERENCE_STORE, DesignerColorUtils.SUBJOB_TITLE_COLOR_NAME, subjobColor);
        }
    }

    @Override
    public void refresh() {
        super.refresh();

        Element element = getElement();
        if (element != null && element instanceof SubjobContainer) {
            SubjobContainer process = (SubjobContainer) element;
            if (reloadBtn != null && !reloadBtn.isDisposed()) {
                reloadBtn.setEnabled(!process.isReadOnly());
            }
            if (saveBtn != null && !saveBtn.isDisposed()) {
                saveBtn.setEnabled(!process.isReadOnly());
            }
        }
    }

}
