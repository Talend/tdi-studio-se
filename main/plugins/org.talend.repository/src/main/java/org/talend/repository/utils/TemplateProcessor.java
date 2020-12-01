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
package org.talend.repository.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.app.event.implement.EscapeXmlReference;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.eclipse.core.runtime.Platform;
import org.talend.core.runtime.util.SharedStudioUtils;

public class TemplateProcessor {

    public static boolean processTemplate(String logTag, Map<String, Object> contextParams,
            OutputStream outputStream, InputStream templateStream) throws IOException {

        final Reader templateReader = new InputStreamReader(templateStream);
        final Writer outputWriter = new OutputStreamWriter(outputStream);

        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(TemplateProcessor.class.getClassLoader());

            VelocityEngine engine = new VelocityEngine();
//          engine.setProperty("resource.loader", "classpath"); //$NON-NLS-1$ //$NON-NLS-2$
//          engine.setProperty("classpath.resource.loader.class", //$NON-NLS-1$
//                  "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader"); //$NON-NLS-1$
            engine.setProperty(RuntimeConstants.EVENTHANDLER_REFERENCEINSERTION,
                EscapeXmlReference.class.getName());
            if (SharedStudioUtils.isSharedStudioMode()) {
                engine.setProperty(RuntimeConstants.RUNTIME_LOG,
                        Platform.getConfigurationLocation().getURL().getFile() + "/velocity.log");
            }
            engine.init();

            VelocityContext context = new VelocityContext(contextParams);

            return engine.evaluate(context, outputWriter, logTag, templateReader);
        } catch (VelocityException ve) {
            // couldn't find the template
            // org.apache.velocity.exception.ResourceNotFoundException;
            // syntax error: problem parsing the template
            // org.apache.velocity.exception.ParseErrorException;
            // something invoked in the template threw an exception
            // org.apache.velocity.exception.MethodInvocationException;
            throw new IOException(ve);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);

            templateReader.close();

            outputWriter.close();
        }
    }

    public static boolean processTemplate(String logTag, Map<String, Object> contextParams,
        File file, InputStream templateStream) throws IOException {
        return processTemplate(logTag, contextParams, new FileOutputStream(file), templateStream);
    }
}
