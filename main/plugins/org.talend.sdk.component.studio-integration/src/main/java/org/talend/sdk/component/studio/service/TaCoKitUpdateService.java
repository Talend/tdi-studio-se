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
package org.talend.sdk.component.studio.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.runtime.service.ITaCoKitService;
import org.talend.sdk.component.studio.i18n.Messages;
import org.talend.sdk.component.studio.update.TaCoKitCar;
import org.talend.sdk.component.studio.update.TaCoKitCarFeature;
import org.talend.sdk.component.studio.util.TaCoKitUtil;
import org.talend.updates.runtime.model.ExtraFeature;
import org.talend.updates.runtime.model.InstallationStatus;
import org.talend.updates.runtime.model.InstallationStatus.Status;
import org.talend.updates.runtime.model.interfaces.ITaCoKitCarFeature;
import org.talend.updates.runtime.nexus.component.ComponentIndexBean;
import org.talend.updates.runtime.service.ITaCoKitUpdateService;
import org.talend.updates.runtime.storage.AbstractFeatureStorage;


/**
 * DOC cmeng  class global comment. Detailled comment
 */
public class TaCoKitUpdateService implements ITaCoKitUpdateService {

    @Override
    public ITaCoKitCarFeature generateExtraFeature(File file, IProgressMonitor monitor) throws Exception {
        TaCoKitCar car = new TaCoKitCar(file);
        TaCoKitCarFeature taCoKitCarFeature = new TaCoKitCarFeature(car);
        taCoKitCarFeature.setStorage(new AbstractFeatureStorage() {

            @Override
            protected File downloadImageFile(IProgressMonitor monitor) throws Exception {
                return null;
            }

            @Override
            protected File downloadFeatureFile(IProgressMonitor monitor) throws Exception {
                return file;
            }
        });
        return taCoKitCarFeature;
    }

    @Override
    public ITaCoKitCarFeature generateExtraFeature(ComponentIndexBean indexBean, IProgressMonitor monitor) throws Exception {
        return new TaCoKitCarFeature(indexBean);
    }

    @Override
    public ICarInstallationResult installCars(Collection<File> files, boolean share, IProgressMonitor monitor) throws Exception {
        return installCars(files, share, false, monitor);
    }
    
    @Override
    public ICarInstallationResult deployCars(Collection<File> files, boolean share, IProgressMonitor monitor) throws Exception {
        return installCars(files, share, true, monitor);
    }
    
    private ICarInstallationResult installCars(Collection<File> files, boolean share, boolean isDeployCommand, IProgressMonitor monitor) throws Exception {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        TaCoKitUtil.checkMonitor(monitor);
        CarInstallationResult result = new CarInstallationResult();

        if (files != null && files.size() > 0) {
            monitor.beginTask(Messages.getString("TaCoKitUpdateService.progress.generatingFeatures"), files.size()); //$NON-NLS-1$

            List<ITaCoKitCarFeature> carFeatures = new LinkedList<>();
            for (File carFile : files) {
                TaCoKitUtil.checkMonitor(monitor);
                monitor.subTask(
                        Messages.getString("TaCoKitUpdateService.progress.generatingFeatures.current", carFile.getName())); //$NON-NLS-1$
                ITaCoKitCarFeature carFeature = null;
                try {
                    carFeature = generateExtraFeature(carFile, monitor);
                    carFeature.setShareEnable(share);
                    carFeature.setDeployCommand(isDeployCommand);
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
                if (carFeature != null) {
                    carFeatures.add(carFeature);
                } else {
                    result.getFailedFile().add(carFile);
                }
                monitor.worked(1);
            }

            ICarInstallationResult instResult = installCarFeatures(carFeatures, share, monitor);
            if (instResult.needRestart()) {
                result.setNeedRestart(instResult.needRestart());
            }
            result.getFailedFile().addAll(instResult.getFailedFile());
            result.getInstalledStatus().putAll(instResult.getInstalledStatus());

        }

        return result;
    }

    @Override
    public ICarInstallationResult installCarFeatures(Collection<ITaCoKitCarFeature> features, boolean share,
            IProgressMonitor monitor)
            throws Exception {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        TaCoKitUtil.checkMonitor(monitor);
        CarInstallationResult result = new CarInstallationResult();

        if (features != null && !features.isEmpty()) {
            monitor.beginTask(Messages.getString("TaCoKitUpdateService.progress.installingFeatures"), features.size()); //$NON-NLS-1$
            List<ITaCoKitCarFeature> featureList = new LinkedList<>(features);
            Collections.sort(featureList);

            int succeedNum = 0;
            for (ITaCoKitCarFeature carFeature : featureList) {
                TaCoKitUtil.checkMonitor(monitor);
                try {
                    monitor.subTask(Messages.getString("TaCoKitUpdateService.progress.installingFeatures.current.isInstalled", //$NON-NLS-1$
                            carFeature.getName()));
                    if (carFeature.canBeInstalled(monitor)) {
                        monitor.subTask(Messages.getString("TaCoKitUpdateService.progress.installingFeatures.current.installing", //$NON-NLS-1$
                                carFeature.getName()));
                        carFeature.setAutoReloadAfterInstalled(false);
                        IStatus installStatus = carFeature.install(monitor, Collections.EMPTY_LIST);
                        if (share) {
                            // won't share here, just copy file to installed folder
                            carFeature.setShareEnable(false);
                            carFeature.syncComponentsToInstalledFolder(monitor, carFeature.getStorage().getFeatureFile(monitor));
                        }
                        result.getInstalledStatus().put(carFeature.getCarFile(monitor), installStatus);
                        if (carFeature.needRestart()) {
                            result.setNeedRestart(true);
                        }
                        switch (installStatus.getSeverity()) {
                        case IStatus.OK:
                        case IStatus.INFO:
                        case IStatus.WARNING:
                            ++succeedNum;
                            break;
                        default:
                            // nothing to do
                            break;
                        }
                    } else {
                        throw new Exception(Messages.getString(
                                "TaCoKitUpdateService.progress.installingFeatures.current.cantInstall", carFeature.getName())); //$NON-NLS-1$
                    }
                } catch (InterruptedException e) {
                    throw e;
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                    result.getFailedFile().add(carFeature.getCarFile(monitor));
                }
                monitor.worked(1);
            }

            if (0 < succeedNum) {
                // if studio need to restart, then no need to reload tacokit
                if (!result.needRestart()) {
                    try {
                        String log = ITaCoKitService.getInstance().reload(monitor);
                        ExceptionHandler.log(log);
                    } catch (InterruptedException e) {
                        throw e;
                    } catch (Exception e) {
                        ExceptionHandler.process(e);
                    }
                }
            }
        }

        return result;
    }

    @Override
    public Collection<ExtraFeature> filterUpdatableFeatures(Collection<ExtraFeature> features, IProgressMonitor monitor)
            throws Exception {
        Collection<ExtraFeature> updates = new LinkedList<>();
        if (features == null || features.isEmpty()) {
            return updates;
        }
        for (ExtraFeature feature : features) {
            if (feature instanceof ITaCoKitCarFeature) {
                try {
                    InstallationStatus installationStatus = feature.getInstallationStatus(monitor);
                    Status status = installationStatus.getStatus();
                    /*
                     * Here I didn't use InstallationStatus#canBeInstalled, since I think it's good to show there is an
                     * update but need to update studio first
                     */
                    if (status.isInstalled() && status.canBeInstalled()) {
                        updates.add(feature);
                    }
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }
        }
        return updates;
    }

    @Override
    public boolean isCar(File file, IProgressMonitor monitor) throws Exception {
        TaCoKitUtil.checkMonitor(monitor);
        return TaCoKitCar.isCar(file);
    }

    static class CarInstallationResult implements ICarInstallationResult {

        private Map<File, IStatus> installedStatusMap;

        private List<File> failedFiles;

        private boolean needRestart;

        public CarInstallationResult() {
            installedStatusMap = new LinkedHashMap<>();
            failedFiles = new ArrayList<>();
            needRestart = false;
        }

        @Override
        public boolean needRestart() {
            return needRestart;
        }

        public void setNeedRestart(boolean needRestart) {
            this.needRestart = needRestart;
        }

        @Override
        public Map<File, IStatus> getInstalledStatus() {
            return installedStatusMap;
        }

        @Override
        public List<File> getFailedFile() {
            return failedFiles;
        }

    }
}
