// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
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
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.ui.images.CoreImageProvider;
import org.talend.designer.components.preference.provider.IPaletteItem;

/**
 * created by nrousseau on Aug 11, 2014 Detailled comment
 *
 */
public class RootPaletteItem extends AbstractPaletteItem implements IPaletteItem {

    public RootPaletteItem(ComponentCategory category) {
        setCategory(category);
        ImageDescriptor imageDesc;
        ERepositoryObjectType type;
        switch (category) {
        case CATEGORY_4_CAMEL:
            type = ERepositoryObjectType.valueOf("ROUTES");
            imageDesc = CoreImageProvider.getImageDesc(type);
            setLabel(type.getLabel());
            break;
        case CATEGORY_4_MAPREDUCE:
            type = ERepositoryObjectType.valueOf("PROCESS_MR");
            imageDesc = CoreImageProvider.getImageDesc(type);
            imageDesc = CoreImageProvider.getImageDesc(type);
            setLabel(type.getLabel());
            break;
        case CATEGORY_4_STORM:
            type = ERepositoryObjectType.valueOf("PROCESS_STORM");
            imageDesc = CoreImageProvider.getImageDesc(type);
            setLabel(type.getLabel());
            break;
        case CATEGORY_4_DI:
        default:
            type = ERepositoryObjectType.PROCESS;
            imageDesc = CoreImageProvider.getImageDesc(type);
            setLabel(type.getLabel());
            break;
        }
        setImageDesc(imageDesc);
    }
}
