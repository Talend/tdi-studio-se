package org.talend.sdk.component.studio.automerge.analyser;

import org.eclipse.core.runtime.IProgressMonitor;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.general.Project;
import org.talend.repository.gitmerge.automerge.AutoMergeConsts;
import org.talend.repository.gitmerge.automerge.analyser.AbsTalendItemConflictAnalyser;
import org.talend.repository.gitmerge.automerge.analyser.IConflictAnalyser;
import org.talend.repository.gitmerge.automerge.context.IAutoMergeContext;
import org.talend.repository.gitmerge.automerge.info.IConflictInfo;
import org.talend.repository.gitmerge.automerge.info.IPropertiesConflictInfo;
import org.talend.repository.gitmerge.automerge.info.IXmlConflictInfo;
import org.talend.repository.gitmerge.automerge.solution.DummyConflictResolveSolution;
import org.talend.repository.gitmerge.automerge.solution.ESolutionExecutionPoint;
import org.talend.repository.gitmerge.automerge.solution.IConflictResolveSolution;
import org.talend.repository.gitmerge.utils.MergeUtils;
import org.talend.sdk.component.studio.automerge.extractor.TaCoKitConflictExtractor;
import org.talend.utils.security.StudioEncryption;


public class TaCoKitConflictAnalyser extends AbsTalendItemConflictAnalyser implements IConflictAnalyser {

    /**
     * Please use it through getter
     */
    private StudioEncryption studioEncryption;

    public TaCoKitConflictAnalyser() {
        super();
    }

    @Override
    protected IConflictResolveSolution analyseXml(IProgressMonitor monitor, IAutoMergeContext context, Project project,
            IXmlConflictInfo conflictInfo) throws Exception {
        if (!TaCoKitConflictExtractor.ID.equals(conflictInfo.getExtractorId())) {
            if (AutoMergeConsts.NODE_PATH_METADATA_TACOKIT_PROPERTIES.equals(conflictInfo.getKey())) {
                /**
                 * Since we have already extract this xml conflict to properties conflicts, and will handled later; just
                 * ignore the original xml conflict
                 */
                DummyConflictResolveSolution solution = new DummyConflictResolveSolution(conflictInfo);
                solution.setExecutionPoint(ESolutionExecutionPoint.DURING_CHECK_INTEGRITY);
                return solution;
            }
        }
        return null;
    }

    @Override
    protected IConflictResolveSolution analyseProperties(IProgressMonitor monitor, IAutoMergeContext context, Project project,
            IPropertiesConflictInfo conflictInfo) throws Exception {
        IConflictResolveSolution solution = null;
        solution = handlePasswordConflict(monitor, context, project, conflictInfo);
        if (solution != null) {
            return solution;
        }
        return super.analyseProperties(monitor, context, project, conflictInfo);
    }

    private IConflictResolveSolution handlePasswordConflict(IProgressMonitor monitor, IAutoMergeContext context, Project project,
            IPropertiesConflictInfo conflictInfo) throws Exception {
        try {
            if (MergeUtils.isPasswordEquals(conflictInfo, getStudioEncryption(monitor, context, project, conflictInfo))) {
                DummyConflictResolveSolution solution = new DummyConflictResolveSolution(conflictInfo);
                solution.setExecutionPoint(ESolutionExecutionPoint.DURING_CHECK_INTEGRITY);
                return solution;
            }
        } catch (Throwable e) {
            if (MergeUtils.enableAutoMergeLog()) {
                ExceptionHandler.process(e);
            }
        }
        return null;
    }

    private StudioEncryption getStudioEncryption(IProgressMonitor monitor, IAutoMergeContext context, Project project,
            IConflictInfo conflictInfo) throws Exception {
        if (studioEncryption == null) {
            synchronized (this) {
                if (studioEncryption == null) {
                    studioEncryption = StudioEncryption.getStudioEncryption(StudioEncryption.EncryptionKeyName.SYSTEM);
                }
            }
        }
        return studioEncryption;
    }

    @Override
    public void clear() {
        synchronized (this) {
            this.studioEncryption = null;
        }
        super.clear();
    }

}
