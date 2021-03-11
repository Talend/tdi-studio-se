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
package org.talend.designer.core.ui.editor;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.search.AnalyzerDescriptor;
import org.eclipse.help.internal.search.LocalSearchManager;
import org.eclipse.help.internal.search.PluginVersionInfo;
import org.eclipse.help.internal.search.SearchIndex;
import org.eclipse.help.internal.search.SearchIndexWithIndexingProgress;
import org.eclipse.help.internal.toc.TocFileProvider;
import org.eclipse.help.internal.toc.TocManager;
import org.talend.commons.exception.CommonExceptionHandler;

/**
 * DOC cmeng class global comment. Detailled comment<br/>
 *
 * @see org.eclipse.help.internal.search.SearchIndexWithIndexingProgress
 */
public class TalendPaletteSearchIndex extends SearchIndexWithIndexingProgress {

    protected static File indexDir;

    protected static Field f_indexDir;

    protected static String locale = Platform.getNL();

    protected static TalendPaletteSearchIndex instance = null;

    protected static Field f_locale;

    public static TalendPaletteSearchIndex getInstance() {
        if (instance != null) {
            return instance;
        }
        LocalSearchManager localSearchManager = BaseHelpSystem.getInstance().getLocalSearchManager();
        Method getAnalyzer;
        try {
            f_locale = SearchIndex.class.getDeclaredField("locale");
            f_locale.setAccessible(true);
            f_indexDir = SearchIndex.class.getDeclaredField("indexDir");
            f_indexDir.setAccessible(true);
            getAnalyzer = localSearchManager.getClass().getDeclaredMethod("getAnalyzer", String.class);
            getAnalyzer.setAccessible(true);
            Object analyzerObj = getAnalyzer.invoke(localSearchManager, locale);
            AnalyzerDescriptor analyzer = null;
            if (analyzerObj != null) {
                analyzer = (AnalyzerDescriptor) analyzerObj;
                instance = new TalendPaletteSearchIndex(locale, analyzer, HelpPlugin.getTocManager());
            }
        } catch (Throwable e) {
            CommonExceptionHandler.process(e);
        }
        return instance;
    }

    /**
     * @param locale
     * @param analyzerDesc
     * @param tocManager
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    protected TalendPaletteSearchIndex(String locale, AnalyzerDescriptor analyzerDesc, TocManager tocManager)
            throws IllegalArgumentException, IllegalAccessException {
        super(locale + "_talend", analyzerDesc, tocManager);
        f_locale.set(this, locale);
        indexDir = (File) f_indexDir.get(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.help.internal.search.SearchIndex#getDocPlugins()
     */
    @Override
    public PluginVersionInfo getDocPlugins() {
        return getTalendDocPlugins();
    }

    protected PluginVersionInfo getTalendDocPlugins() {
        Set<String> totalIds = new HashSet<String>();
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(TocFileProvider.EXTENSION_POINT_ID_TOC);
        IExtension[] extensions = extensionPoint.getExtensions();
        for (IExtension extension : extensions) {
            try {
                totalIds.add(extension.getNamespaceIdentifier());
            } catch (InvalidRegistryObjectException e) {
                // ignore this extension and move on
            }
        }
        Collection<String> additionalPluginIds = BaseHelpSystem.getLocalSearchManager().getPluginsWithSearchParticipants();
        totalIds.addAll(additionalPluginIds);
        Iterator<String> idIter = totalIds.iterator();
        while (idIter.hasNext()) {
            String id = idIter.next();
            if (!id.startsWith("org.talend.")) {
                idIter.remove();
            }
        }
        return new PluginVersionInfo(INDEXED_CONTRIBUTION_INFO_FILE, totalIds, indexDir, !exists());
    }
}
