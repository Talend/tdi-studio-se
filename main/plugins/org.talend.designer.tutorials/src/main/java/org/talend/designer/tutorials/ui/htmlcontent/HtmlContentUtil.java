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
package org.talend.designer.tutorials.ui.htmlcontent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.ui.internal.intro.impl.model.loader.IntroContentParser;
import org.talend.designer.tutorials.TutorialsPlugin;
import org.w3c.dom.Document;

/**
 *
 * created by hcyi on Mar 20, 2017 Detailled comment
 *
 */
public class HtmlContentUtil {

    public static Document readHtmlToDocument(String path) throws IOException {
        URL entry = TutorialsPlugin.getDefault().getBundle().getEntry(path);
        Document dom = null;
        if (entry != null) {
            entry = FileLocator.toFileURL(entry);
            String result = entry.toExternalForm();
            if (result.startsWith("file:/")) { //$NON-NLS-1$
                if (result.startsWith("file:///") == false) { //$NON-NLS-1$
                    result = "file:///" + result.substring(6); //$NON-NLS-1$
                }
            }
            IntroContentParser parser = new IntroContentParser(result);
            dom = parser.getDocument();
        }
        return dom;

    }

    public static String readHtmlContent(String path) throws IOException {
        URL entry = TutorialsPlugin.getDefault().getBundle().getEntry(path);
        Document dom = null;
        if (entry != null) {
            entry = FileLocator.toFileURL(entry);
            String result = entry.toExternalForm();
            if (result.startsWith("file:/")) { //$NON-NLS-1$
                result = result.substring("file:/".length(), result.length());
            }
            StringBuffer buffer = new StringBuffer();
            BufferedReader reader = new BufferedReader(new FileReader(new File(result)));
            String line = null;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            return buffer.toString();
        }
        return "";
    }
}
