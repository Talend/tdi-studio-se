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
package org.talend.sdk.component.studio.update;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.equinox.p2.metadata.Version;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.runtime.service.ITaCoKitService;
import org.talend.commons.utils.resource.FileExtensions;
import org.talend.core.runtime.maven.MavenUrlHelper;
import org.talend.sdk.component.studio.i18n.Messages;
import org.talend.sdk.component.studio.util.TaCoKitConst;
import org.talend.sdk.component.studio.util.TaCoKitUtil;
import org.talend.sdk.component.studio.util.TaCoKitUtil.GAV;
import org.talend.updates.runtime.feature.model.Category;
import org.talend.updates.runtime.feature.model.Type;
import org.talend.updates.runtime.model.AbstractExtraFeature;
import org.talend.updates.runtime.model.ExtraFeatureException;
import org.talend.updates.runtime.model.InstallationStatus;
import org.talend.updates.runtime.model.UpdateSiteLocationType;
import org.talend.updates.runtime.model.interfaces.ITaCoKitCarFeature;
import org.talend.updates.runtime.nexus.component.ComponentIndexBean;
import org.talend.updates.runtime.storage.AbstractFeatureStorage;
import org.talend.updates.runtime.storage.IFeatureStorage;
import org.talend.updates.runtime.utils.PathUtils;
import org.talend.utils.io.FilesUtils;

/**
 * DOC cmeng  class global comment. Detailled comment
 */
public class TaCoKitCarFeature extends AbstractExtraFeature implements ITaCoKitCarFeature {

    private boolean autoReloadAfterInstalled = true;

    private TaCoKitCar car;

    private Object carLock = new Object();

    public TaCoKitCarFeature(ComponentIndexBean indexBean) {
        super(indexBean.getBundleId(), indexBean.getName(), indexBean.getVersion(), indexBean.getDescription(),
                indexBean.getMvnURI(), indexBean.getImageMvnURI(), indexBean.getProduct(), indexBean.getCompatibleStudioVersion(),
                null, PathUtils.convert2Types(indexBean.getTypes()), PathUtils.convert2Categories(indexBean.getCategories()),
                Boolean.valueOf(indexBean.getDegradable()), false, false);
    }

    public TaCoKitCarFeature(TaCoKitCar car) throws Exception {
        super(car.getId(), car.getName(), car.getCarVersion(), car.getDescription(), null, null, null, null, null,
                new LinkedList<>(Arrays.asList(Type.TCOMP_V1)),
                new LinkedList<>(Arrays.asList(Category.ALL)), false, false, false);
        this.car = car;
    }

    @Override
    public boolean isInstalled(IProgressMonitor progress) throws Exception {
        InstallationStatus installationStatus = getInstallationStatus(progress);
        return installationStatus.getStatus().isInstalled();
    }

    /**
     * @return Map<GAV, GAV> newComponent to installedComponent
     */
    private Map<GAV, GAV> filterAlreadyInstalledComponent(List<GAV> installedComponents, List<GAV> newComponents,
            IProgressMonitor progress) throws Exception {
        Map<GAV, GAV> alreadyInstalledComponentMap = new HashMap<>();
        for (GAV newComponent : newComponents) {
            TaCoKitUtil.checkMonitor(progress);
            boolean alreadyInstalled = false;
            GAV curInstalledComponent = null;
            for (GAV installedComponent : installedComponents) {
                TaCoKitUtil.checkMonitor(progress);
                if (alreadyInstalled) {
                    break;
                }
                try {
                    if (StringUtils.equals(
                            installedComponent.getGroupId() + ":" + installedComponent.getArtifactId() + ":" //$NON-NLS-1$ //$NON-NLS-2$
                                    + installedComponent.getClassifier() + ":" + installedComponent.getType(), //$NON-NLS-1$
                            newComponent.getGroupId() + ":" + newComponent.getArtifactId() + ":" //$NON-NLS-1$ //$NON-NLS-2$
                                    + newComponent.getClassifier() + ":" + newComponent.getType())) { //$NON-NLS-1$
                        alreadyInstalled = true;
                    }
                } catch (Exception e) {
                    alreadyInstalled = true;
                    ExceptionHandler.process(e);
                } finally {
                    if (alreadyInstalled) {
                        curInstalledComponent = installedComponent;
                    }
                }
            }
            if (alreadyInstalled) {
                alreadyInstalledComponentMap.put(newComponent, curInstalledComponent);
            }
        }
        return alreadyInstalledComponentMap;
    }

    @Override
    public IStatus install(IProgressMonitor progress, List<URI> allRepoUris) throws Exception {
        IStatus status = null;
        try {
            TaCoKitUtil.checkMonitor(progress);
            boolean succeed = install(progress);
            if (succeed) {
                status = new Status(IStatus.OK, TaCoKitConst.BUNDLE_ID,
                        Messages.getString("TaCoKitCarFeature.status.succeed", getName())); //$NON-NLS-1$
                if (!needRestart() && isAutoReloadAfterInstalled()) {
                    // if studio need to restart, then no need to reload here
                    String log = ITaCoKitService.getInstance().reload(progress);
                    ExceptionHandler.log(log);
                }
            } else {
                status = new Status(IStatus.ERROR, TaCoKitConst.BUNDLE_ID,
                        Messages.getString("TaCoKitCarFeature.status.failed", getName())); //$NON-NLS-1$
            }
        } catch (InterruptedException e) {
            status = new Status(IStatus.CANCEL, TaCoKitConst.BUNDLE_ID, Messages.getString("progress.cancel"), e); //$NON-NLS-1$
        } catch (Exception e) {
            status = new Status(IStatus.ERROR, TaCoKitConst.BUNDLE_ID,
                    Messages.getString("TaCoKitCarFeature.status.failed", getName()), e); //$NON-NLS-1$
        }
        return status;
    }

    @SuppressWarnings("nls")
    public boolean install(IProgressMonitor progress) throws Exception {
        boolean success = false;
        String tckCarPath = getCar(progress).getCarFile().getAbsolutePath();
        String installationPath = URIUtil.toFile(URIUtil.toURI(Platform.getInstallLocation().getURL())).getAbsolutePath();

        String command = deployToStudio(tckCarPath, installationPath);
        success = execCarMain(progress, command);
        if (!success) {
            return false;
        }

        File config = new File(installationPath, "configuration/config.ini"); //$NON-NLS-1$
        Properties configuration = new Properties();
        try (InputStream stream = new FileInputStream(config)) {
            configuration.load(stream);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        String repositoryType = configuration.getProperty("maven.repository"); //$NON-NLS-1$
        String m2RootFromContext = System.getProperty("maven.local.repository"); //$NON-NLS-1$
        // zero-install CI mode
        if (StringUtils.isNoneBlank(m2RootFromContext) && "global".equals(repositoryType)) {
            File defaultM2Root = new File(System.getProperty("user.home"), ".m2/repository/");
            // FIXME to avoid the bug from CarMain that can't install car to custom m2 when using global m2 repository.
            if (!new File(m2RootFromContext).equals(defaultM2Root)) {
                command = deployToM2(tckCarPath, m2RootFromContext);
                success = execCarMain(progress, command);
            }
        }

        return success;
    }

    private String deployToStudio(String tckCarPath, String installationPath) {
        StringBuilder commandBuilder = new StringBuilder();
        commandBuilder.append("java -jar "); //$NON-NLS-1$
        commandBuilder.append(StringUtils.wrap(tckCarPath, "\"")); //$NON-NLS-1$
        commandBuilder.append(" studio-deploy "); //$NON-NLS-1$
        commandBuilder.append(StringUtils.wrap(installationPath, "\"")); //$NON-NLS-1$
        return commandBuilder.toString();
    }

    private String deployToM2(String tckCarPath, String m2Root) {
        StringBuilder commandBuilder = new StringBuilder();
        commandBuilder.append("java -jar "); //$NON-NLS-1$
        commandBuilder.append(StringUtils.wrap(tckCarPath, "\"")); //$NON-NLS-1$
        commandBuilder.append(" maven-deploy "); //$NON-NLS-1$
        commandBuilder.append(StringUtils.wrap(m2Root, "\"")); //$NON-NLS-1$
        return commandBuilder.toString();
    }

    private boolean execCarMain(IProgressMonitor progress, String command) throws Exception {
        String osName = System.getProperty("os.name"); //$NON-NLS-1$
        String[] cmd = new String[3];
        if (osName.startsWith("Windows")) { //$NON-NLS-1$
            cmd[0] = "cmd.exe"; //$NON-NLS-1$
            cmd[1] = "/C"; //$NON-NLS-1$
            cmd[2] = command;
        } else if (osName.startsWith("Mac")) { //$NON-NLS-1$
            cmd[0] = "/bin/bash"; //$NON-NLS-1$
            cmd[1] = "-c"; //$NON-NLS-1$
            cmd[2] = command;
        } else {
            // linux
            cmd[0] = "/bin/bash"; //$NON-NLS-1$
            cmd[1] = "-c"; //$NON-NLS-1$
            cmd[2] = command;
        }
        Process exec = Runtime.getRuntime().exec(cmd);
        while (exec.isAlive()) {
            try {
                TaCoKitUtil.checkMonitor(progress);
            } catch (InterruptedException e) {
                exec.destroy();
                boolean alreadyInstalled = false;
                try {
                    InstallationStatus installationStatus = getInstallationStatus(new NullProgressMonitor());
                    if (installationStatus != null) {
                        org.talend.updates.runtime.model.InstallationStatus.Status status = installationStatus.getStatus();
                        if (status != null) {
                            if (status.isInstalled()) {
                                String installedVersion = installationStatus.getInstalledVersion();
                                alreadyInstalled = StringUtils.equals(getVersion(), installedVersion);
                            }
                        }
                    }
                } catch (Exception e1) {
                    ExceptionHandler.process(e);
                }
                if (alreadyInstalled) {
                    return true;
                } else {
                    throw e;
                }
            }
            Thread.sleep(100);
        }
        int exitValue = exec.exitValue();
        if (exitValue != 0) {
            throw new Exception(getErrorMessage(exec));
        }
        return true;
    }

    private String getErrorMessage(Process exec) {
        StringBuffer strBuffer = new StringBuffer();
        InputStreamReader inReader = null;
        BufferedReader bufReader = null;
        try {
            inReader = new InputStreamReader(exec.getErrorStream());
            bufReader = new BufferedReader(inReader);
            do {
                String line = bufReader.readLine();
                if (line == null || line.isEmpty()) {
                    break;
                }
                strBuffer.append(line).append("\n"); //$NON-NLS-1$
            } while (true);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        } finally {
            if (inReader != null) {
                try {
                    inReader.close();
                } catch (Exception e) {
                    // nothing to do
                }
            }
            if (bufReader != null) {
                try {
                    bufReader.close();
                } catch (Exception e) {
                    // nothing to do
                }
            }
        }
        return strBuffer.toString();
    }

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        return "TaCoKitCarFeature [car=" + this.car.toString() + "]";
    }

    @Override
    public EnumSet<UpdateSiteLocationType> getUpdateSiteCompatibleTypes() {
        return EnumSet.allOf(UpdateSiteLocationType.class);
    }

    @Override
    public boolean mustBeInstalled() {
        return false;
    }

    @Override
    public boolean needRestart() {
        // seems ReloadAction#reload can't reload config.ini?
        return true;
    }

    @Override
    public String getName() {
        String name = super.getName();
        if (StringUtils.isBlank(name)) {
            try {
                return getCar(new NullProgressMonitor()).getName();
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
            return null;
        } else {
            return name;
        }
    }

    @Override
    public String getDescription() {
        String description = super.getDescription();
        if (StringUtils.isBlank(description)) {
            try {
                return getCar(new NullProgressMonitor()).getDescription();
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
            return null;
        } else {
            return description;
        }
    }

    @Override
    public String getVersion() {
        String version = super.getVersion();
        if (StringUtils.isBlank(version)) {
            try {
                return getCar(new NullProgressMonitor()).getCarVersion();
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
            return null;
        } else {
            return version;
        }
    }

    @Override
    public String getMvnUri() {
        String mvnUri = super.getMvnUri();
        if (mvnUri == null) {
            GAV gav = car.getComponents().get(0);
            mvnUri = MavenUrlHelper.generateMvnUrl(gav.getGroupId(), gav.getArtifactId(), gav.getVersion(),
                    FileExtensions.ZIP_EXTENSION, null);
        }
        return mvnUri;
    }

    @Override
    public boolean isAutoReloadAfterInstalled() {
        return this.autoReloadAfterInstalled;
    }

    @Override
    public void setAutoReloadAfterInstalled(boolean autoReload) {
        this.autoReloadAfterInstalled = autoReload;
    }

    @Override
    public boolean isShareEnable() {
        return share;
    }

    @Override
    public void setShareEnable(boolean share) {
        this.share = share;
    }

    @Override
    public File getCarFile(IProgressMonitor progress) throws Exception {
        return getCar(new NullProgressMonitor()).getCarFile();
    }

    private TaCoKitCar getCar(IProgressMonitor progress) throws Exception {
        if (this.car != null) {
            return this.car;
        }
        synchronized (carLock) {
            this.car = new TaCoKitCar(getStorage().getFeatureFile(progress));
        }
        return this.car;
    }

    @Override
    public boolean canBeInstalled(IProgressMonitor progress) throws ExtraFeatureException {
        try {
            InstallationStatus installationStatus = getInstallationStatus(progress);
            return installationStatus.canBeInstalled();
        } catch (Exception e) {
            throw new ExtraFeatureException(e);
        }
    }

    @Override
    public InstallationStatus getInstallationStatus(IProgressMonitor monitor) throws Exception {
        List<GAV> installedComponents = TaCoKitUtil.getInstalledComponents(monitor);
        if (installedComponents != null && !installedComponents.isEmpty()) {
            TaCoKitUtil.checkMonitor(monitor);
            List<GAV> newComponents = null;
            if (this.car == null) {
                // means the car is still on network
                newComponents = new ArrayList<>();
                GAV featGAV = new GAV();
                String id = getP2IuId();
                String groupId = id.substring(0, id.lastIndexOf(".")); //$NON-NLS-1$
                String artifactId = id.substring(id.lastIndexOf(".") + 1); //$NON-NLS-1$
                featGAV.setGroupId(groupId);
                featGAV.setArtifactId(artifactId);
                featGAV.setVersion(getVersion());
                newComponents.add(featGAV);
            } else {
                // means it's a local patch, or car is downloaded
                newComponents = getCar(monitor).getComponents();
            }
            if (newComponents != null) {
                Map<GAV, GAV> alreadyInstalledComponentMap = filterAlreadyInstalledComponent(installedComponents, newComponents,
                        monitor);
                if (alreadyInstalledComponentMap != null && !alreadyInstalledComponentMap.isEmpty()) {
                    Version latestInstalledVersion = null;
                    for (Map.Entry<GAV, GAV> entry : alreadyInstalledComponentMap.entrySet()) {
                        GAV newGAV = entry.getKey();
                        GAV installedGAV = entry.getValue();
                        String newVersion = newGAV.getVersion();
                        if (!TaCoKitUtil.isBlank(newGAV.getClassifier())) {
                            newVersion = newVersion + "-" + newGAV.getClassifier();
                        }
                        String installedVersion = installedGAV.getVersion();
                        if (!TaCoKitUtil.isBlank(installedGAV.getClassifier())) {
                            installedVersion = installedVersion + "-" + installedGAV.getClassifier();
                        }
                        Version nVer = PathUtils.convert2Version(newVersion);
                        Version iVer = PathUtils.convert2Version(installedVersion);
                        if (latestInstalledVersion == null) {
                            latestInstalledVersion = iVer;
                        } else {
                            if (latestInstalledVersion.compareTo(iVer) < 0) {
                                latestInstalledVersion = iVer;
                            }
                        }
                        if (0 < nVer.compareTo(iVer)) {
                            InstallationStatus status = new InstallationStatus(InstallationStatus.Status.UPDATABLE);
                            status.setRequiredStudioVersion(getCompatibleStudioVersion());
                            status.setInstalledVersion(installedVersion.toString());
                            return status;
                        }
                    }
                    InstallationStatus status = new InstallationStatus(InstallationStatus.Status.INSTALLED);
                    status.setInstalledVersion(latestInstalledVersion.toString());
                    status.setRequiredStudioVersion(getCompatibleStudioVersion());
                    return status;
                } else {
                    InstallationStatus status = new InstallationStatus(InstallationStatus.Status.INSTALLABLE);
                    status.setRequiredStudioVersion(getCompatibleStudioVersion());
                    return status;
                }
            } else {
                // maybe new case? just install
            }
        }
        InstallationStatus status = new InstallationStatus(InstallationStatus.Status.INSTALLABLE);
        status.setRequiredStudioVersion(getCompatibleStudioVersion());
        return status;
    }

    @Override
    public void setStorage(IFeatureStorage storage) {
        super.setStorage(storage);
        final File workFolder = PathUtils.getComponentsDownloadedFolder();
        FilesUtils.deleteFolder(workFolder, false); // empty the folder
        if (!workFolder.exists()) {
            workFolder.mkdirs();
        }
        ((AbstractFeatureStorage) getStorage()).setFeatDownloadFolder(workFolder);
    }
}
