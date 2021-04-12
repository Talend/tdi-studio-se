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
package org.talend.designer.unifiedcomponent.resources;

import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.talend.commons.ui.runtime.image.IImage;
import org.talend.commons.ui.runtime.image.ImageProvider;

/**
 *
 * created by wchen on Dec 5, 2017 Detailled comment
 *
 */
public class ComponentIconLoading {

    private IImage iImage_32;

    private Map<String, ImageDescriptor> registry;

    /**
     *
     * DOC wchen ComponentIconLoading constructor comment.
     *
     * @param componentsImageRegistry
     * @param iImage_32
     */
    public ComponentIconLoading(Map<String, ImageDescriptor> componentsImageRegistry, IImage iImage_32) {
        super();
        this.registry = componentsImageRegistry;
        this.iImage_32 = iImage_32;

    }

    public ImageDescriptor getImage32() {
        ImageDescriptor image32 = registry.get(iImage_32.getLocation() + iImage_32.getPath() + "_icon32");
        if (image32 == null || image32.getImageData() == null) {
            image32 = ImageProvider.getImageDesc(iImage_32);
        }
        registry.put(iImage_32.getLocation() + iImage_32.getPath() + "_icon32", image32);
        return image32;
    }

    public ImageDescriptor getImage24() {
        ImageDescriptor image24 = registry.get(iImage_32.getLocation() + iImage_32.getPath() + "_icon24");
        if (image24 == null || image24.getImageData() == null) {
            try {
                image24 = ImageDescriptor.createFromImageData(getImage32().getImageData().scaledTo(24, 24));
            } catch (NullPointerException e) {
                image24 = getImage32();
            }
        }
        registry.put(iImage_32.getLocation() + iImage_32.getPath() + "_icon24", image24);

        return image24;
    }

    public ImageDescriptor getImage16() {
        ImageDescriptor image16 = registry.get(iImage_32.getLocation() + iImage_32.getPath() + "_icon16");
        if (image16 == null || image16.getImageData() == null) {
            try {
                image16 = ImageDescriptor.createFromImageData(getImage32().getImageData().scaledTo(16, 16));
            } catch (NullPointerException e) {
                image16 = getImage32();
            }
        }
        registry.put(iImage_32.getLocation() + iImage_32.getPath() + "_icon16", image16);

        return image16;
    }

}
