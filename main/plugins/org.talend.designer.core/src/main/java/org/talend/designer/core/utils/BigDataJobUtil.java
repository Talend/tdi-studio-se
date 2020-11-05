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
package org.talend.designer.core.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.hadoop.HadoopConstants;
import org.talend.core.hadoop.version.EHadoopDistributions;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.repository.ERepositoryObjectType;

/**
 * created by nrousseau on Mar 24, 2018 Detailled comment
 *
 */
public class BigDataJobUtil {

    private IProcess process;

    public BigDataJobUtil(IProcess process) {
        this.process = process;
    }

    /**
     * DOC nrousseau Comment method "needsShade".
     *
     * @param item
     */
    public boolean needsShade() {
        if (isMRWithHDInsight()) {
            return true;
        }
        return isSparkWithHDInsight();
    }

    /**
     * DOC nrousseau Comment method "isSparkWithHDInsight".
     *
     * @param isSparkWithHDInsight
     * @return
     */
    public boolean isSparkWithHDInsight() {
        boolean isSparkWithHDInsight = false;
        if (isBDJobWithFramework(ERepositoryObjectType.PROCESS_MR, HadoopConstants.FRAMEWORK_SPARK)) {
            List<? extends IElementParameter> parameters = process.getElementParametersWithChildrens();
            boolean modeParameterVisited = false;
            for (IElementParameter pt : parameters) {
                if (pt.getName().equals("SPARK_LOCAL_MODE")) { //$NON-NLS-1$
                    modeParameterVisited = true;
                    if ("true".equals(pt.getValue())) { //$NON-NLS-1$
                        isSparkWithHDInsight = false;
                        break;
                    }
                }
                if (pt.getName().equals("DISTRIBUTION") //$NON-NLS-1$
                        && EHadoopDistributions.MICROSOFT_HD_INSIGHT.getName().equals(pt.getValue())) {
                    isSparkWithHDInsight = true;
                    // If the SPARK_LOCAL_MODE parameter already have been processed and if we continue to loop,
                    // that means we are not in a LOCAL mode context. We can break the loop.
                    if (modeParameterVisited) {
                        break;
                    }
                }
            }
        }
        return isSparkWithHDInsight;
    }

    public boolean isMRWithHDInsight() {
        Boolean isMRWithHDInsight = false;
        if (process != null) {
            isMRWithHDInsight = false;
            if (isBDJobWithFramework(ERepositoryObjectType.PROCESS_MR, HadoopConstants.FRAMEWORK_MAPREDUCE)) {
                List<? extends IElementParameter> parameters = process.getElementParametersWithChildrens();
                for (IElementParameter pt : parameters) {
                    if (pt.getName().equals("DISTRIBUTION") //$NON-NLS-1$
                            && EHadoopDistributions.MICROSOFT_HD_INSIGHT.getName().equals(pt.getValue())) {
                        isMRWithHDInsight = true;
                        break;
                    }
                }
            }
        }
        return isMRWithHDInsight;
    }

    /**
     * Test if it is a spark job with yarn cluster mode
     */
    private boolean isSparkWithYarnClusterMode() {
        Boolean isSparkInYarnClusterMode = false;
        // Test if we are in Spark or Spark streaming
        if (isBDJobWithFramework(ERepositoryObjectType.PROCESS_MR, HadoopConstants.FRAMEWORK_SPARK)) {

            List<? extends IElementParameter> parameters = process.getElementParametersWithChildrens();
            for (IElementParameter pt : parameters) {
                if (HadoopConstants.SPARK_MODE.equals(pt.getName())
                        && HadoopConstants.SPARK_MODE_YARN_CLUSTER.equals(pt.getValue())) {
                    isSparkInYarnClusterMode = true;
                    break;
                }
            }
        }
        return isSparkInYarnClusterMode;
    }

    private boolean isBDJobWithFramework(ERepositoryObjectType objectType, String frameworkName) {
        if (process != null && process instanceof IProcess2 && ((IProcess2) process).getAdditionalProperties() != null
                && frameworkName.equals(((IProcess2) process).getAdditionalProperties().get(HadoopConstants.FRAMEWORK))) {
            return true;
        }
        return false;
    }

    /** Find the distribution where the generated jar rquired to have the context files inside **/
    public boolean needsToHaveContextInsideJar() {
        List<? extends IElementParameter> parameters = process.getElementParametersWithChildrens();

        if (process != null && parameters != null) {

            for (IElementParameter pt : parameters) {
                if (pt.getName().equals("DISTRIBUTION")) { //$NON-NLS-1$
                    String value = String.valueOf(pt.getValue());
                    if ("MICROSOFT_HD_INSIGHT".equals(value) //$NON-NLS-1$
                            || "GOOGLE_CLOUD_DATAPROC".equals(value) //$NON-NLS-1$
                            || "CLOUDERA_ALTUS".equals(value) || "DATABRICKS".equals(value) || "QUBOLE".equals(value)) { //$NON-NLS-1$
                        return true;
                    }
                }
            }
            if (isSparkWithYarnClusterMode()) {
                return true;
            }
        }
        return false;
    }

    public void setExcludedModules(Collection<ModuleNeeded> modulesNeeded) {
        if (isMRWithHDInsight() || isSparkWithHDInsight()) {
            // we need to exclude every non-MR Required jars.
            for (ModuleNeeded currentModule : modulesNeeded) {
                if (currentModule.isMrRequired()) {
                    currentModule.setExcluded(true);
                }
            }
        }
    }

    public void removeExcludedModules(Collection<ModuleNeeded> modulesNeeded) {
        Iterator<ModuleNeeded> itModules = modulesNeeded.iterator();
        while (itModules.hasNext()) {
            ModuleNeeded module = itModules.next();
            if (module.isExcluded()) {
                itModules.remove();
            }
        }
    }

    /**
     * DOC nrousseau Comment method "getShadedModulesExclude".
     *
     * @param modulesNeeded
     * @return
     */
    public Set<ModuleNeeded> getShadedModulesExclude(Set<ModuleNeeded> modulesNeeded) {
        Set<ModuleNeeded> excludedModules = new HashSet<>();
        if (isMRWithHDInsight() || isSparkWithHDInsight()) {
            // we need to exclude every non-MR Required jars.
            for (ModuleNeeded currentModule : modulesNeeded) {
                if (!currentModule.isMrRequired()) {
                    excludedModules.add(currentModule);
                } else {
                    currentModule.setExcluded(true);
                }
            }
        }
        // which must be true

        Set<String> stormJarNames = new HashSet<>();
        try {
            // from org.talend.libraries.apache.storm/lib
            Bundle bundle = Platform.getBundle("org.talend.libraries.apache.storm"); //$NON-NLS-1$
            if (bundle != null) {
                URL stormLibUrl = FileLocator.toFileURL(FileLocator.find(bundle, new Path("lib"), null)); //$NON-NLS-1$
                if (stormLibUrl != null) {
                    File file = new File(stormLibUrl.getFile());
                    File[] jars = file.listFiles();
                    for (File f : jars) {
                        stormJarNames.add(f.getName());
                    }
                }
            }
        } catch (IOException e) {
            ExceptionHandler.process(e);
        }

        for (ModuleNeeded currentModule : modulesNeeded) {
            if (stormJarNames.contains(currentModule.getModuleName())) {
                excludedModules.add(currentModule);
            }
        }

        return excludedModules;
    }

}
