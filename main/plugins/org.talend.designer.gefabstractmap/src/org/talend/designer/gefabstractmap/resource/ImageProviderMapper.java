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
package org.talend.designer.gefabstractmap.resource;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.talend.designer.gefabstractmap.NewAbstractmap;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 *
 * $Id: ImageProviderMapper.java 39246 2010-03-26 17:56:43Z sgandon $
 *
 */
public class ImageProviderMapper {

    private static Map<ImageInfo, Image> imageCache = new HashMap<ImageInfo, Image>();

    private static Map<Image, Image> disabledImageCache = new HashMap<Image, Image>();

    public static Image getImage(ImageDescriptor desc) {
        return desc.createImage();
    }

    public static Image getImage(ImageInfo imageInfo) {
        Image imageFromCache = imageCache.get(imageInfo);
        if (imageFromCache != null) {
            return imageFromCache;
        }
        Image image = getImage(getImageDescriptor(imageInfo));
        imageCache.put(imageInfo, image);
        return image;
    }

    public static ImageDescriptor getImageDescriptor(ImageInfo image) {
        return ImageDescriptor.createFromFile(NewAbstractmap.class, image.getPath());
    }

    public static Image getDisabledImage(Image image) {
        Image disabledImage = disabledImageCache.get(image);
        if (disabledImage != null) {
            return disabledImage;
        }
        disabledImage = new Image(image.getDevice(), image, SWT.IMAGE_DISABLE);
        disabledImageCache.put(image, disabledImage);
        return disabledImage;
    }

    /**
     * You can continue to use the provider after call this method.
     */
    public static void releaseImages() {
        for (Image image : disabledImageCache.values()) {
            if (!image.isDisposed()) {
                image.dispose();
            }
        }
        disabledImageCache.clear();

        Collection<Image> images = imageCache.values();
        for (Image image : images) {
            if (!image.isDisposed()) {
                image.dispose();
            }
        }
        imageCache.clear();

    }

    public static void dispose(ImageInfo imageInfo) {

        Image image = imageCache.get(imageInfo);

        Image disabledImage = disabledImageCache.get(image);
        if (disabledImage != null && !disabledImage.isDisposed()) {
            disabledImage.dispose();
        }
        disabledImageCache.remove(image);

        if (!image.isDisposed()) {
            image.dispose();
        }
        imageCache.remove(imageInfo);
    }

}
