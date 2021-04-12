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
package org.talend.repository.json.ui.shadow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.talend.commons.ui.utils.PathUtils;
import org.talend.commons.utils.platform.PluginChecker;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.repository.model.preview.IPreview;
import org.talend.core.utils.CsvArray;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.metadata.managment.ui.i18n.Messages;
import org.talend.metadata.managment.ui.preview.AsynchronousPreviewHandler;
import org.talend.metadata.managment.ui.preview.ProcessDescription;
import org.talend.metadata.managment.ui.utils.ConnectionContextHelper;
import org.talend.repository.model.json.JSONFileConnection;
import org.talend.repository.model.json.SchemaTarget;

/**
 * Create a ProcessDescription to use in the step2 & step3 of CSV File Wizard on Shadow mode.
 *
 * $Id: ShadowProcessHelper.java 51244 2010-11-15 03:28:34Z cli $
 *
 */
public class JSONShadowProcessHelper {

    private static Logger log = Logger.getLogger(JSONShadowProcessHelper.class);

    /*
     * record the current preview.
     */
    private static IPreview currentPreview = null;

    private static final String[] TEXT_ENCLOSURE_DATA = { TalendQuoteUtils.addQuotes("\""), TalendQuoteUtils.addQuotes("\'"), //$NON-NLS-1$ //$NON-NLS-2$
            TalendQuoteUtils.addQuotes("\\\\") }; //$NON-NLS-1$

    private static final String[] ESCAPE_CHAR_DATA = { TalendQuoteUtils.addQuotes("\""), TalendQuoteUtils.addQuotes("\'"), //$NON-NLS-1$ //$NON-NLS-2$
            TalendQuoteUtils.addQuotes("\\\\") }; //$NON-NLS-1$

    public static void forceStopPreview() {
        if (currentPreview != null) {
            currentPreview.stopLoading();
            currentPreview = null;
        }
    }

    private static int getFilePropertyValue(String value) {
        if (value == null) {
            return 0;
        }
        int i = 0;
        try {
            i = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            //
        }
        return i;
    }

    public static String getValueFromArray(String value, String[] array) {
        if (value == null || array.length == 0) {
            return null;
        }
        for (String str : array) {
            if (value.equals(str)) {
                return str;
            }
        }
        return null;
    }

    /**
     * Create a ProcessDescription and set it width the value of JSONFileConnection.
     *
     * This method is usefull to adapt a processDescription before run the shadow process.
     *
     * @param JSONFileConnection
     *
     * @return ProcessDescription
     */
    public static ProcessDescription getProcessDescription(final JSONFileConnection connection, String tempJsonFile) {
        ContextType contextType = null;
        if (connection.isContextMode()) {
            contextType = ConnectionContextHelper.getContextTypeForContextMode(connection);
        }
        ProcessDescription processDescription = new ProcessDescription();
        processDescription.setFilepath(TalendQuoteUtils.addQuotes(PathUtils.getPortablePath(tempJsonFile)));
        
        String xpathQuery = connection.getSchema().get(0).getAbsoluteXPathQuery();
        if (contextType != null && xpathQuery != null) {
            xpathQuery = ContextParameterUtils.getOriginalValue(contextType, xpathQuery);
        }
        processDescription.setLoopQuery(TalendQuoteUtils.addQuotes(xpathQuery));
        if (connection.getSchema().get(0).getLimitBoucle() != null
                && !("").equals(connection.getSchema().get(0).getLimitBoucle()) //$NON-NLS-1$
                && (connection.getSchema().get(0).getLimitBoucle().intValue()) != 0) {
            processDescription.setLoopLimit(connection.getSchema().get(0).getLimitBoucle());
        }

        List<Map<String, String>> mapping = new ArrayList<Map<String, String>>();

        List<SchemaTarget> schemaTargets = connection.getSchema().get(0).getSchemaTargets();

        if (schemaTargets != null && !schemaTargets.isEmpty()) {
            Iterator<SchemaTarget> iterate = schemaTargets.iterator();
            while (iterate.hasNext()) {
                SchemaTarget schemaTarget = iterate.next();
                Map<String, String> lineMapping = new HashMap<String, String>();
                lineMapping.put("QUERY", TalendQuoteUtils.addQuotes(schemaTarget.getRelativeXPathQuery())); //$NON-NLS-1$
                mapping.add(lineMapping);
            }
        }
        processDescription.setReadbyMode(connection.getReadbyMode());
        processDescription.setMapping(mapping);
        String encode = connection.getEncoding();
        if (encode != null && !("").equals(encode)) { //$NON-NLS-1$
            if (contextType != null) {
                encode = ContextParameterUtils.getOriginalValue(contextType, encode);
            }
            processDescription.setEncoding(TalendQuoteUtils.addQuotes(encode));
        } else {
            processDescription.setEncoding(TalendQuoteUtils.addQuotes("UTF-8")); //$NON-NLS-1$
        }

        return processDescription;
    }

    /**
     * parse a file describe by a processDescription in XmlArray.
     *
     * @param processDescription
     * @return xmlArray
     */
    public static CsvArray getCsvArray(final ProcessDescription processDescription, String type) throws CoreException {

        CsvArray csvArray = null;

        IPreview preview = createPreview(type);

        if (preview != null) {
            csvArray = preview.preview(processDescription, type);
        }
        return csvArray;
    }

    /**
     * parse a file describe by a processDescription in XmlArray.
     *
     * @param processDescription
     * @return xmlArray
     */
    public static CsvArray getCsvArray(final ProcessDescription processDescription, String type, boolean errorOutoutAsException)
            throws CoreException {

        CsvArray csvArray = null;

        IPreview preview = createPreview();

        if (preview != null) {
            csvArray = preview.preview(processDescription, type, errorOutoutAsException);
        }
        return csvArray;
    }

    /**
     * DOC amaumont Comment method "createPreview".
     *
     * @param configurationElements
     * @return
     * @throws CoreException
     */
    private static IPreview createPreview() throws CoreException {
        return createPreview(null);
    }

    /**
     * DOC amaumont Comment method "createPreview".
     *
     * @param configurationElements
     * @return
     * @throws CoreException
     */
    private static IPreview createPreview(String type) throws CoreException {
        IExtensionRegistry registry = Platform.getExtensionRegistry();

        // use the org.talend.repository.filepreview_provider
        IConfigurationElement[] configurationElements = registry
                .getConfigurationElementsFor("org.talend.core.runtime.filepreview_provider"); //$NON-NLS-1$
        // When start a new preview. need stop before preview.
        forceStopPreview();

        IPreview preview = null;
        if (configurationElements.length > 0) {
            preview = (IPreview) configurationElements[0].createExecutableExtension("class"); //$NON-NLS-1$
        }

        for (IConfigurationElement configurationElement : configurationElements) {
            String fileType = configurationElement.getAttribute("type"); //$NON-NLS-1$

            // to do this just make sure the code behaviour is same like before
            if (type == null && fileType != null) {
                // fileType is not null which means this previewer is for some specified file type, not good for the
                // current type(null value, means should use the default)
                continue;
            } else if (type != null) { // want to create a specified previewer for a specified type
                if (fileType == null) { // means this is a gereral previewer, not good for a specified type
                    continue;
                } else if (!fileType.equals(type)) {
                    continue;
                }
            }

            IPreview pre = (IPreview) configurationElement.createExecutableExtension("class"); //$NON-NLS-1$
            if (!PluginChecker.isOnlyTopLoaded() && !pre.isTopPreview()) {
                preview = pre;
            }
        }

        if (preview == null) {
            log.error(Messages.getString("ShadowProcessHelper.logError.previewIsNull01") //$NON-NLS-1$
                    + Messages.getString("ShadowProcessHelper.logError.previewIsNull02")); //$NON-NLS-1$
        }
        currentPreview = preview;
        return preview;
    }

    public static AsynchronousPreviewHandler<CsvArray> createPreviewHandler(String type) throws CoreException {
        IPreview preview = createPreview(type);
        return new AsynchronousPreviewHandler<CsvArray>(preview);
    }

    public static AsynchronousPreviewHandler<CsvArray> createPreviewHandler() throws CoreException {
        return createPreviewHandler(null);
    }

}
