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
package org.talend.repository.preference.palettesettings;

import org.eclipse.jface.resource.ImageDescriptor;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.ui.component.preference.provider.IPaletteItem;
import org.talend.repository.ui.ERepositoryImages;

/**
 * created by nrousseau on Aug 11, 2014 Detailled comment
 *
 */
public class FolderPaletteItem extends AbstractPaletteItem implements IPaletteItem {

    /**
     * Create fodler palette item.
     *
     * @param label : from translated family name
     * @param orignalLabel : from original family in component
     * @param category
     */
    public FolderPaletteItem(String label, ComponentCategory category) {
        ImageDescriptor foldIcon = ImageProvider.getImageDesc(ERepositoryImages.FOLDER_ICON);
        setImageDesc(foldIcon);
        setLabel(label);
        setCategory(category);
    }
}
