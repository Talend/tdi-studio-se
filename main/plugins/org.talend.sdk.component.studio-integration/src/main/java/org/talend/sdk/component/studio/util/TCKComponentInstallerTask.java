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
package org.talend.sdk.component.studio.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.codehaus.plexus.util.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ILibraryManagerService;
import org.talend.core.model.utils.BaseComponentInstallerTask;
import org.talend.sdk.component.studio.util.TaCoKitUtil.GAV;
import org.talend.updates.runtime.service.ITaCoKitUpdateService;
import org.talend.updates.runtime.service.ITaCoKitUpdateService.ICarInstallationResult;
import org.talend.updates.runtime.utils.PathUtils;

/**
 * @author bhe created on Jul 1, 2021
 *
 */
public class TCKComponentInstallerTask extends BaseComponentInstallerTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(TCKComponentInstallerTask.class);

    /**
     * Get car file
     * 
     * @return car file
     */
    protected List<File> getCarFiles() {
        return null;
    }

    protected GAV getComponentGAV() {
        GAV tempGAV = new GAV();
        tempGAV.setGroupId(getComponentGroupId());
        tempGAV.setArtifactId(getComponenArtifactId());
        tempGAV.setVersion(getComponenVersion());
        tempGAV.setClassifier(getComponentClassifier());
        tempGAV.setType(getComponentPackageType());
        return tempGAV;
    }

    @Override
    public boolean needInstall() {
        GAV compGAV = getComponentGAV();
        ILibraryManagerService librairesManagerService = (ILibraryManagerService) GlobalServiceRegister.getDefault().getService(ILibraryManagerService.class);
        if (librairesManagerService != null) {
            File jarFile = librairesManagerService.resolveStatusLocally(compGAV.toMavenUri());
            if (jarFile != null) {
                LOGGER.info("Component: {} was already installed", compGAV.toString());
                return false;
            }
        }

        LOGGER.info("Component: {} is going to be installed", compGAV.toString());
        return true;
    }

    @Override
    public boolean install(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        Set<File> files = new HashSet<File>();
        List<File> carFiles = getCarFiles();
        if (carFiles != null) {
            LOGGER.info("Car file: {} was added", Arrays.toString(carFiles.toArray()));
            files.addAll(carFiles);
        }
        if (files.isEmpty()) {
            LOGGER.info("No car files");
            return false;
        }

        try {
            ICarInstallationResult result = ITaCoKitUpdateService.getInstance().installCars(files, true, monitor, false);
            boolean requireUpdateConfig = false;
            for (File carFile : carFiles) {
                IStatus stat = result.getInstalledStatus().get(carFile);
                if (stat == null) {
                    LOGGER.info("Skipped to install car: {}", carFile);
                } else if (stat.getCode() == IStatus.OK) {
                    requireUpdateConfig = true;
                    LOGGER.info("TCK Component installed: {}", carFile);
                } else {
                    LOGGER.error("Failed to install car: {}", carFile);
                }
            }

            if (requireUpdateConfig) {
                // update config.ini
                updateConfig();

                LOGGER.info("TCK Component installed: {}", Arrays.toString(carFiles.toArray()));
                return true;
            }

        } catch (Exception e) {
            LOGGER.error("Failed to install car: {}", Arrays.toString(carFiles.toArray()), e);
        }
        return false;
    }

    protected void updateConfig() throws Exception {
        List<GAV> installedComponents = TaCoKitUtil.getInstalledComponents(null);

        GAV thisGAV = this.getComponentGAV();

        boolean found = false;

        for (GAV ic : installedComponents) {
            if (compareGAV(thisGAV, ic)) {
                found = true;
                break;
            }
        }

        if (!found) {
            // update config.ini
            LOGGER.info("updating config.ini");

            File studioConfigFile = PathUtils.getStudioConfigFile();
            Properties configProps = PathUtils.readProperties(studioConfigFile);
            StringBuffer sb = new StringBuffer();
            String coordinates = configProps.getProperty(TaCoKitConst.PROP_COMPONENT);
            if (!StringUtils.isEmpty(coordinates)) {
                sb.append(coordinates);
            }
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(thisGAV.toCoordinateStr());

            configProps.put(TaCoKitConst.PROP_COMPONENT, sb.toString());

            try (BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(studioConfigFile))) {
                configProps.store(fos, "Updated by TCKComponentInstaller");

                LOGGER.info("updated config.ini");
            } catch (Exception e) {
                LOGGER.error("Can not update config.ini", e);
                ExceptionHandler.process(e);
            }
        }

    }

    protected boolean compareGAV(GAV gav1, GAV gav2) {
        if (!StringUtils.equals(gav1.getGroupId(), gav2.getGroupId())) {
            return false;
        }

        if (!StringUtils.equals(gav1.getArtifactId(), gav2.getArtifactId())) {
            return false;
        }

        if (!StringUtils.equals(gav1.getVersion(), gav2.getVersion())) {
            return false;
        }

        if (!StringUtils.equals(gav1.getClassifier(), gav2.getClassifier())) {
            return false;
        }

        if (!StringUtils.equals(gav1.getType(), gav2.getType())) {
            return false;
        }

        return true;

    }

}
