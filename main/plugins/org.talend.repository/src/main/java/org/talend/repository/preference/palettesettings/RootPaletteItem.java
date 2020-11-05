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
package org.talend.repository.preference.palettesettings;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.PluginChecker;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.utils.ConvertJobsUtil;
import org.talend.core.ui.component.preference.provider.IPaletteItem;

/**
 * created by nrousseau on Aug 11, 2014 Detailled comment
 *
 */
public class RootPaletteItem extends AbstractPaletteItem implements IPaletteItem {

    public RootPaletteItem(ComponentCategory category) {
        setCategory(category);
        ERepositoryObjectType type;
        ImageDescriptor imageDesc;
        switch (category) {
        case CATEGORY_4_CAMEL:
            type = ERepositoryObjectType.PROCESS_ROUTE;
            imageDesc = ImageProvider.getImageDesc(ECoreImage.ROUTES_ICON);
            break;
        case CATEGORY_4_MAPREDUCE:
            type = ERepositoryObjectType.valueOf("PROCESS_MR"); //$NON-NLS-1$
            imageDesc = ImageProvider.getImageDesc(ECoreImage.PROCESS_BATCH_MR_ICON);
            break;
        case CATEGORY_4_SPARK:
            type = ERepositoryObjectType.valueOf("PROCESS_MR"); //$NON-NLS-1$
            imageDesc = ImageProvider.getImageDesc(ECoreImage.PROCESS_BATCH_SPARK_ICON);
            break;
        case CATEGORY_4_DI:
        default:
            type = ERepositoryObjectType.PROCESS;
            imageDesc = ImageProvider.getImageDesc(ECoreImage.PROCESS_ICON);
            break;
        }
        Assert.isNotNull(type);
        String desc = category.getDesc();
        String label = type.getLabel();
        if (ERepositoryObjectType.PROCESS.equals(type)
                && (PluginChecker.isMapReducePluginLoader())) {
            label = ConvertJobsUtil.JobType.STANDARD.getDisplayName();
        }
        setLabel(label + ("".equals(desc) ? "" : " (" + desc + ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        setImageDesc(imageDesc);
    }
}
