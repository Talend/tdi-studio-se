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
package org.talend.designer.scd.util;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Utility class for managing OS resources associated with SWT controls such as colors, fonts, images, etc.
 */
public class SWTResourceManager {

    // ////////////////////////////
    // Color support
    // ////////////////////////////

    /**
     * Maps RGB values to colors
     */
    private static HashMap<RGB, Color> m_ColorMap = new HashMap<RGB, Color>();

    /**
     * Returns the system color matching the specific ID
     *
     * @param systemColorID int The ID value for the color
     * @return Color The system color matching the specific ID
     */
    public static Color getColor(int systemColorID) {
        Display display = Display.getCurrent();
        return display.getSystemColor(systemColorID);
    }

    /**
     * Returns a color given its red, green and blue component values
     *
     * @param r int The red component of the color
     * @param g int The green component of the color
     * @param b int The blue component of the color
     * @return Color The color matching the given red, green and blue componet values
     */
    public static Color getColor(int r, int g, int b) {
        return getColor(new RGB(r, g, b));
    }

    /**
     * Returns a color given its RGB value
     *
     * @param rgb RGB The RGB value of the color
     * @return Color The color matching the RGB value
     */
    public static Color getColor(RGB rgb) {
        Color color = m_ColorMap.get(rgb);
        if (color == null) {
            Display display = Display.getCurrent();
            color = new Color(display, rgb);
            m_ColorMap.put(rgb, color);
        }
        return color;
    }

    /**
     * Dispose of all the cached colors
     */
    public static void disposeColors() {
        for (Iterator<Color> iter = m_ColorMap.values().iterator(); iter.hasNext();) {
            iter.next().dispose();
        }
        m_ColorMap.clear();
    }

    // ////////////////////////////
    // Font support
    // ////////////////////////////

    /**
     * Maps font names to fonts
     */
    private static HashMap<String, Font> m_FontMap = new HashMap<String, Font>();

    private static Map<Integer, Font> m_systemFontMap = new HashMap<Integer, Font>();

    private static FontData[] systemFontData = Display.getCurrent().getSystemFont().getFontData();

    /**
     * Returns a font based on its name, height and style
     *
     * @param name String The name of the font
     * @param height int The height of the font
     * @param style int The style of the font
     * @return Font The font matching the name, height and style
     */
    public static Font getFont(String name, int height, int style) {
        String fontName = name + '|' + height + '|' + style;
        Font font = m_FontMap.get(fontName);
        if (font == null) {
            FontData fontData = new FontData(name, height, style);

            font = new Font(Display.getCurrent(), fontData);
            m_FontMap.put(fontName, font);
        }
        return font;
    }

    public static Font getSystemFont(int style) {
        Font font = m_systemFontMap.get(style);
        if (font == null) {
            systemFontData[0].setStyle(style);
            font = new Font(Display.getCurrent(), systemFontData);
            m_systemFontMap.put(style, font);
        }
        return font;
    }

    /**
     * Dispose all of the cached fonts
     */
    public static void disposeFonts() {
        // clear fonts
        for (Iterator<Font> iter = m_FontMap.values().iterator(); iter.hasNext();) {
            iter.next().dispose();
        }
        m_FontMap.clear();
    }

    // ////////////////////////////
    // Image support
    // ////////////////////////////

    /**
     * Maps image descriptors to images
     */
    private static HashMap<ImageDescriptor, Image> m_DescriptorImageMap = new HashMap<ImageDescriptor, Image>();

    /**
     * Returns an image descriptor stored in the file at the specified path relative to the specified class
     *
     * @param clazz Class The class relative to which to find the image descriptor
     * @param path String The path to the image file
     * @return ImageDescriptor The image descriptor stored in the file at the specified path
     */
    public static ImageDescriptor getImageDescriptor(Class<?> clazz, String path) {
        return ImageDescriptor.createFromFile(clazz, path);
    }

    /**
     * Returns an image descriptor stored in the file at the specified path
     *
     * @param path String The path to the image file
     * @return ImageDescriptor The image descriptor stored in the file at the specified path
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        try {
            return ImageDescriptor.createFromURL((new File(path)).toURI().toURL());
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     * Returns an image based on the specified image descriptor
     *
     * @param descriptor ImageDescriptor The image descriptor for the image
     * @return Image The image based on the specified image descriptor
     */
    public static Image getImage(ImageDescriptor descriptor) {
        if (descriptor == null) {
            return null;
        }
        Image image = m_DescriptorImageMap.get(descriptor);
        if (image == null) {
            image = descriptor.createImage();
            m_DescriptorImageMap.put(descriptor, image);
        }
        return image;
    }

    /*
     * Dispose all of the cached images
     */
    public static void disposeImages() {
        SWTResourceManager.disposeImages();
        // dispose ImageDescriptor images
        {
            for (Iterator<Image> I = m_DescriptorImageMap.values().iterator(); I.hasNext();) {
                I.next().dispose();
            }
            m_DescriptorImageMap.clear();
        }
        // dispose plugin images
        {
            for (Iterator<Image> I = m_URLImageMap.values().iterator(); I.hasNext();) {
                I.next().dispose();
            }
            m_URLImageMap.clear();
        }
    }

    // ////////////////////////////
    // Plugin images support
    // ////////////////////////////

    /**
     * Maps URL to images
     */
    private static HashMap<URL, Image> m_URLImageMap = new HashMap<URL, Image>();

    /**
     * Retuns an image based on a plugin and file path
     *
     * @param plugin Object The plugin containing the image
     * @param name String The path to th eimage within the plugin
     * @return Image The image stored in the file at the specified path
     */
    public static Image getPluginImage(Object plugin, String name) {
        try {
            try {
                URL url = getPluginImageURL(plugin, name);
                if (m_URLImageMap.containsKey(url)) {
                    return m_URLImageMap.get(url);
                }
                InputStream is = url.openStream();
                Image image;
                try {
                    image = getImage(is);
                    m_URLImageMap.put(url, image);
                } finally {
                    is.close();
                }
                return image;
            } catch (Throwable e) {
                // Ignore any exceptions
            }
        } catch (Throwable e) {
            // Ignore any exceptions
        }
        return null;
    }

    /**
     * Returns an image encoded by the specified input stream
     *
     * @param is InputStream The input stream encoding the image data
     * @return Image The image encoded by the specified input stream
     */
    protected static Image getImage(InputStream is) {
        Display display = Display.getCurrent();
        ImageData data = new ImageData(is);
        if (data.transparentPixel > 0) {
            return new Image(display, data, data.getTransparencyMask());
        }
        return new Image(display, data);
    }

    /**
     * Retuns an image descriptor based on a plugin and file path
     *
     * @param plugin Object The plugin containing the image
     * @param name String The path to th eimage within the plugin
     * @return ImageDescriptor The image descriptor stored in the file at the specified path
     */
    public static ImageDescriptor getPluginImageDescriptor(Object plugin, String name) {
        try {
            try {
                URL url = getPluginImageURL(plugin, name);
                return ImageDescriptor.createFromURL(url);
            } catch (Throwable e) {
                // Ignore any exceptions
            }
        } catch (Throwable e) {
            // Ignore any exceptions
        }
        return null;
    }

    /**
     * Retuns an URL based on a plugin and file path
     *
     * @param plugin Object The plugin containing the file path
     * @param name String The file path
     * @return URL The URL representing the file at the specified path
     * @throws Exception
     */
    private static URL getPluginImageURL(Object plugin, String name) throws Exception {
        // try to work with 'plugin' as with OSGI BundleContext
        try {
            Class<?> bundleClass = Class.forName("org.osgi.framework.Bundle"); //$NON-NLS-1$
            Class<?> bundleContextClass = Class.forName("org.osgi.framework.BundleContext"); //$NON-NLS-1$
            if (bundleContextClass.isAssignableFrom(plugin.getClass())) {
                Method getBundleMethod = bundleContextClass.getMethod("getBundle", new Class[0]); //$NON-NLS-1$
                Object bundle = getBundleMethod.invoke(plugin, new Object[0]);
                //
                Class<?> ipathClass = Class.forName("org.eclipse.core.runtime.IPath"); //$NON-NLS-1$
                Class<?> pathClass = Class.forName("org.eclipse.core.runtime.Path"); //$NON-NLS-1$
                Constructor<?> pathConstructor = pathClass.getConstructor(new Class[] { String.class });
                Object path = pathConstructor.newInstance(new Object[] { name });
                //
                Class<?> platformClass = Class.forName("org.eclipse.core.runtime.Platform"); //$NON-NLS-1$
                Method findMethod = platformClass.getMethod("find", new Class[] { bundleClass, ipathClass }); //$NON-NLS-1$
                return (URL) findMethod.invoke(null, new Object[] { bundle, path });
            }
        } catch (Throwable e) {
            // Ignore any exceptions
        }
        // else work with 'plugin' as with usual Eclipse plugin
        {
            Class<?> pluginClass = Class.forName("org.eclipse.core.runtime.Plugin"); //$NON-NLS-1$
            if (pluginClass.isAssignableFrom(plugin.getClass())) {
                //
                Class<?> ipathClass = Class.forName("org.eclipse.core.runtime.IPath"); //$NON-NLS-1$
                Class<?> pathClass = Class.forName("org.eclipse.core.runtime.Path"); //$NON-NLS-1$
                Constructor<?> pathConstructor = pathClass.getConstructor(new Class[] { String.class });
                Object path = pathConstructor.newInstance(new Object[] { name });
                //
                Method findMethod = pluginClass.getMethod("find", new Class[] { ipathClass }); //$NON-NLS-1$
                return (URL) findMethod.invoke(plugin, new Object[] { path });
            }
        }
        return null;
    }

}
