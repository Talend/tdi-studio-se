// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.ui.actions.routines;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.commons.ui.runtime.image.OverlayImageProvider;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.properties.ByteArray;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.properties.RoutinesJarItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.routines.CodesJarInfo;
import org.talend.core.model.routines.RoutinesUtil;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.designer.codegen.ICodeGeneratorService;
import org.talend.designer.codegen.ITalendSynchronizer;
import org.talend.designer.core.model.utils.emf.component.IMPORTType;
import org.talend.designer.maven.tools.CodesJarM2CacheManager;
import org.talend.designer.maven.utils.CodesJarMavenUtil;
import org.talend.designer.maven.utils.MavenProjectUtils;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.ui.actions.AContextualAction;
import org.talend.repository.ui.dialog.RepositoryReviewDialog;

public abstract class AbstractAssignCodesToJarAction extends AContextualAction {

    public AbstractAssignCodesToJarAction() {
        super();
        String text = getActionText();
        setText(text);
        setToolTipText(text);
        setImageDescriptor(OverlayImageProvider.getImageWithNew(getImage()));
    }

    @Override
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        setEnabled(false);
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        boolean canWork = true;
        if (selection.isEmpty() || factory.isUserReadOnlyOnCurrentProject()) {
            return;
        }
        Iterator<?> iterator = selection.iterator();
        while (iterator.hasNext()) {
            Object obj = iterator.next();
            if (!(obj instanceof RepositoryNode)) {
                canWork = false;
                break;
            }
            RepositoryNode node = (RepositoryNode) obj;
            if (node.getObjectType() != getCodeType() || !isTargetItem(node.getObject().getProperty().getItem())
                    || RoutinesUtil.isInnerCodes(node.getObject().getProperty())
                    || ((RoutineItem) node.getObject().getProperty().getItem()).isBuiltIn()
                    || factory.getStatus(node.getObject()) == ERepositoryStatus.DELETED
                    || !ProjectManager.getInstance().isInCurrentMainProject(node) || !isLastVersion(node)) {
                canWork = false;
                break;
            }
        }
        setEnabled(canWork);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void doRun() {
        RepositoryReviewDialog dialog = new RepositoryReviewDialog(getWorkbenchPart().getSite().getShell(),
                getTargetCodesJarType());
        if (dialog.open() == Window.OK) {
            RepositoryNode targetcodesJarNode = dialog.getResult();
            ISelection selection = getSelection();
            RoutinesJarItem codesJarItem = (RoutinesJarItem) targetcodesJarNode.getObject().getProperty().getItem();
            IPath targetPath = RepositoryNodeUtilities.getPath(targetcodesJarNode);
            String gavPackage = "package "
                    + StringUtils.replace(CodesJarMavenUtil.getGAVPackageForCodesJar(codesJarItem), "/", ".") + ";";
            int originalSize = codesJarItem.getRoutinesJarType().getImports().size();
            List<IMPORTType> newImports = new ArrayList<>();
            try {
                Iterator<?> iterator = ((IStructuredSelection) selection).iterator();
                while (iterator.hasNext()) {
                    RepositoryNode node = (RepositoryNode) iterator.next();
                    RoutineItem sourceItem = (RoutineItem) node.getObject().getProperty().getItem();
                    List<IMPORTType> backupImports = new ArrayList<>(sourceItem.getImports());
                    sourceItem.getImports().clear();
                    RoutinesUtil.setInnerCodes(sourceItem.getProperty(), getTargetCodesJarType());
                    newImports.addAll(backupImports);
                    RoutineItem innerCodeItem = (RoutineItem) ProxyRepositoryFactory.getInstance().copy(sourceItem, targetPath,
                            sourceItem.getProperty().getLabel());

                    // update package in content
                    ByteArray contents = sourceItem.getContent();
                    String newInnerContent = StringUtils.replaceOnce(new String(contents.getInnerContent()),
                            "package " + getCodeType().name().toLowerCase() + ";", gavPackage);
                    innerCodeItem.getContent().setInnerContent(newInnerContent.getBytes());
                    ProxyRepositoryFactory.getInstance().save(innerCodeItem);

                    // reset source item
                    sourceItem.getImports().addAll(backupImports);
                    RoutinesUtil.setInnerCodes(sourceItem.getProperty(), null);

                    // delete source item
                    ProxyRepositoryFactory.getInstance().deleteObjectPhysical(node.getObject());

                    // sync code
                    ICodeGeneratorService service = (ICodeGeneratorService) GlobalServiceRegister.getDefault()
                            .getService(ICodeGeneratorService.class);
                    ITalendSynchronizer routineSynchronizer = service.createJavaRoutineSynchronizer();
                    routineSynchronizer.syncRoutine(innerCodeItem, true, true);
                }
                codesJarItem.getRoutinesJarType().getImports().addAll(newImports);
                ProxyRepositoryFactory.getInstance().save(codesJarItem);
                // open project
                CodesJarInfo info = CodesJarInfo.create(codesJarItem.getProperty());
                ITalendProcessJavaProject codesJarProject = IRunProcessService.get().getExistingTalendCodesJarProject(info);
                if (codesJarProject == null) {
                    codesJarProject = IRunProcessService.get().getTalendCodesJarJavaProject(info);
                } else {
                    if (codesJarItem.getRoutinesJarType().getImports().size() > originalSize) {
                        CodesJarM2CacheManager.updateCodesJarProjectPom(new NullProgressMonitor(), info);
                    }
                    MavenProjectUtils.updateMavenProject(new NullProgressMonitor(), codesJarProject.getProject());
                }
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }
    }

    abstract protected String getActionText();

    abstract protected Image getImage();

    abstract protected ERepositoryObjectType getTargetCodesJarType();

    abstract protected ERepositoryObjectType getCodeType();

    abstract protected boolean isTargetItem(Item item);

}
