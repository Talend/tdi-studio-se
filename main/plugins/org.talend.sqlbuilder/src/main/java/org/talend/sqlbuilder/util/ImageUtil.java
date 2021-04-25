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
package org.talend.sqlbuilder.util;

import java.net.URL;
import java.util.HashMap;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.SqlBuilderPlugin;
/**
 *
 * DOC dev  class global comment. Detailled comment
 * <br/>
 *
 * $Id: ImageUtil.java,v 1.3 2006/10/26 21:44:30 qiang.zhang Exp $
 *
 */
public class ImageUtil {

    private static HashMap pimageCount = new HashMap();

    private static HashMap pimages = new HashMap();


    /**
     * Dispose of an image in cache. Once there are no more open handles to the
     * image it will be disposed of.
     *
     */
    @SuppressWarnings("unchecked") //$NON-NLS-1$
	public static void disposeImage(String propertyName) {

        try {

            Image image = (Image) pimages.get(propertyName);

            if (image == null) {
                return;
            }

            image.dispose();
            pimages.remove(propertyName);

            // decrease image handle count by one

            Integer handleCount = (Integer) pimageCount.get(propertyName);

            if (handleCount == null) {
                handleCount = new Integer(0);
            } else {
                handleCount = new Integer(handleCount.intValue() - 1);
            }
            pimageCount.put(propertyName, handleCount);

        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("ImageUtil.logMessage1"), e); //$NON-NLS-1$
        }
    }


    /**
     * Create an image descriptor for the given image property in the
     * text.properties file.
     *
     * @param propertyName
     * @return
     */
    public static ImageDescriptor getDescriptor(String propertyName) {

        try {

            if (propertyName == null) {
                return null;
            }

            // get image path
            String path = Messages.getString(propertyName);

            if (path == null || path.trim().length() == 0) {
                SqlBuilderPlugin.log(Messages.getString("ImageUtil.logMessage2", propertyName), null); //$NON-NLS-1$
                return null;
            }

            // create image
            URL url = URLUtil.getResourceURL(path);
            return ImageDescriptor.createFromURL(url);

        } catch (Exception e) {
            SqlBuilderPlugin.log(Messages.getString("ImageUtil.logMessage3", propertyName), e); //$NON-NLS-1$
            return null;
        }

    }

    /**
     * Get an image object from cache or create one if it doesn't exist yet.
     * Everytime an object is retrieved, it should be disposed of using the
     * ImageUtil.disposeImage method.
     *
     * @param propertyName
     */
    @SuppressWarnings("unchecked") //$NON-NLS-1$
	public static Image getImage(String propertyName) {

        Image image = (Image) pimages.get(propertyName);

        if (image == null) {
            image = getDescriptor(propertyName).createImage();

            if (image == null) {
                return null;
            }

            pimages.put(propertyName, image);
        }

        // increase image handle count by one

        Integer handleCount = (Integer) pimageCount.get(propertyName);

        if (handleCount == null) {
            handleCount = new Integer(1);
        } else {
            handleCount = new Integer(handleCount.intValue() + 1);
        }
        pimageCount.put(propertyName, handleCount);

        return image;
    }

    public static ImageDescriptor getFragmentDescriptor(String fragmentId, String path) {

        try {

            if (path == null || path.trim().length() == 0) {
                return null;
            }

            // create image
            URL url = URLUtil.getFragmentResourceURL(fragmentId, path);
            return ImageDescriptor.createFromURL(url);

        } catch (Exception e) {
            SqlBuilderPlugin.log(Messages.getString("ImageUtil.logMessage3") + fragmentId + ": " + path, e); //$NON-NLS-1$ //$NON-NLS-2$
            return null;
        }

    }

    public static Image getFragmentImage(String fragmentId, String path) {

        try {

            if (path == null || path.trim().length() == 0) {
                return null;
            }

            // create image
            URL url = URLUtil.getFragmentResourceURL(fragmentId, path);
            ImageDescriptor descriptor = ImageDescriptor.createFromURL(url);
            if (descriptor == null) {
                return null;
            }
            return descriptor.createImage();

        } catch (Exception e) {
            SqlBuilderPlugin.log(Messages.getString("ImageUtil.logMessage3") + fragmentId + ": " + path, e); //$NON-NLS-1$ //$NON-NLS-2$
            return null;
        }
    }
}
