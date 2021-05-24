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

import org.apache.log4j.Priority;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.talend.commons.exception.CommonExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.designer.runprocess.IRunProcessService;

/**
 * DOC cmeng class global comment. Detailled comment
 */
public class TalendDefaultJavaEditor extends CompilationUnitEditor {

    protected String fileType = FILE_TYPE_OTHERS;

    protected static final String FILE_TYPE_TALEND_GENERATED_READONLY = "READONLY_GENERATED_BY_TALEND"; //$NON-NLS-1$

    protected static final String FILE_TYPE_TALEND_GENERATED_WRITEABLE = "WRITEABLE_GENERATED_BY_TALEND"; //$NON-NLS-1$

    protected static final String FILE_TYPE_OTHERS = "OTHERS_NOT_BY_TALEND"; //$NON-NLS-1$

    @Override
    protected void doSetInput(IEditorInput input) throws CoreException {

        if (input instanceof FileEditorInput) {
            try {
                // IRepositoryService repositoryService = DesignerPlugin.getDefault().getRepositoryService();
                // IProxyRepositoryFactory repFactory = repositoryService.getProxyRepositoryFactory();
                IRunProcessService runProcessService = (IRunProcessService) GlobalServiceRegister.getDefault().getService(
                        IRunProcessService.class);
                // I think runProcessService can't be null, and should throw exception if it is null
                ITalendProcessJavaProject talendJavaProject = runProcessService.getTalendCodeJavaProject(ERepositoryObjectType.ROUTINES);
                if (talendJavaProject == null) {
                    return;
                }
                final String pathSeperator = "/"; //$NON-NLS-1$
                String routinesFolderName = ERepositoryObjectType.ROUTINES.getFolder().split(pathSeperator)[1];
                String talendSrcFolderPath = talendJavaProject.getSrcFolder().getLocation().toString() + pathSeperator;
                String routineFolderPath = talendSrcFolderPath + routinesFolderName + pathSeperator;
                IFile openedFile = ((FileEditorInput) input).getFile();
                String openedFilePath = openedFile.getLocation().toString();
                boolean isRoutine = openedFilePath.startsWith(routineFolderPath);
                boolean isTalendGeneratedFile = openedFilePath.startsWith(talendSrcFolderPath);
                String systemRoutineFolderName = "system"; //$NON-NLS-1$
                String systemRoutineFolderPath = routineFolderPath + systemRoutineFolderName + pathSeperator;
                boolean isSystemRoutine = openedFilePath.startsWith(systemRoutineFolderPath);

                if (isSystemRoutine) {
                    fileType = FILE_TYPE_TALEND_GENERATED_READONLY;
                } else if (isRoutine) {
                    fileType = FILE_TYPE_TALEND_GENERATED_READONLY;
                } else if (isTalendGeneratedFile) {
                    fileType = FILE_TYPE_TALEND_GENERATED_READONLY;
                } else {
                    fileType = FILE_TYPE_OTHERS;
                }
            } catch (Throwable e) {
                CommonExceptionHandler.process(e, Priority.WARN);
                fileType = FILE_TYPE_OTHERS;
            }

        }
        super.doSetInput(input);
    }

    @Override
    public boolean isEditable() {
        boolean isEditable = false;

        if (FILE_TYPE_TALEND_GENERATED_READONLY.equals(fileType)) {
            isEditable = false;
        } else if (FILE_TYPE_TALEND_GENERATED_WRITEABLE.equals(fileType)) {
            isEditable = true;
        } else if (FILE_TYPE_OTHERS.equals(fileType)) {
            isEditable = true;
        }

        return isEditable;
    }
}
