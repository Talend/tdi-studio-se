package org.talend.sdk.component.studio.automerge.extractor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.talend.commons.utils.resource.FileExtensions;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.repository.gitmerge.automerge.AutoMergeConsts;
import org.talend.repository.gitmerge.automerge.context.IAutoMergeContext;
import org.talend.repository.gitmerge.automerge.extractor.AbsPropertiesConflictExtractor;
import org.talend.repository.gitmerge.automerge.extractor.AbsTalendItemConflictExtractor;
import org.talend.repository.gitmerge.automerge.extractor.IConflictExtractor;
import org.talend.repository.gitmerge.automerge.extractor.ModelSupplier;
import org.talend.repository.gitmerge.automerge.info.IConflictInfo;
import org.talend.repository.gitmerge.enums.ConflictType;
import org.talend.repository.gitmerge.enums.EItemType;
import org.talend.repository.gitmerge.exceptions.MergeException;
import org.talend.repository.gitmerge.items.AbstractConflictItem;
import org.talend.repository.gitmerge.items.ConflictFile;
import org.talend.repository.gitmerge.utils.MergeUtils;
import org.talend.sdk.component.studio.util.TaCoKitUtil;


public class TaCoKitConflictExtractor extends AbsTalendItemConflictExtractor implements IConflictExtractor {

    /**
     * Should keep the ID same with the one configured in extension
     */
    public static final String ID = "extractor.tacokit"; //$NON-NLS-1$

    public TaCoKitConflictExtractor() {
        super();
    }

    @Override
    public boolean checkSupportManually(IProgressMonitor monitor, IAutoMergeContext context, AbstractConflictItem conflictItem) {
        ConflictType conflictType = conflictItem.getConflictType();
        return conflictType.hasSelfVersion() && conflictType.hasTheirVersion()
                && TaCoKitUtil.isTaCoKitType(conflictItem.getRepositoryObjectType());
    }

    @Override
    protected List<IConflictInfo> doExtractUnknownFile(IProgressMonitor monitor, IAutoMergeContext context,
            AbstractConflictItem conflictItem, ConflictFile conflictFile, ModelSupplier<?> model) throws MergeException {
        /**
         * here we only care about the properties conflict, no need to check other conflicts, because they'll be handled
         * by the KnownTalendItemConflictExtrator/KnownTalendItemConflictAnalyser
         */
        return Arrays.asList(createNoneConflictInfo(monitor, context, conflictItem, conflictFile));
    }

    @Override
    protected List<IConflictInfo> doExtractXml(IProgressMonitor monitor, IAutoMergeContext context,
            AbstractConflictItem conflictItem, ConflictFile conflictFile, ModelSupplier<?> model) throws MergeException {
        List<IConflictInfo> newConflictInfos = MergeUtils.createResizeList(IConflictInfo.class);
        List<IConflictInfo> conflictInfos = super.doExtractXml(monitor, context, conflictItem, conflictFile, model);

        for (IConflictInfo conflictInfo : conflictInfos) {
            if (AutoMergeConsts.NODE_PATH_METADATA_TACOKIT_PROPERTIES.equals(conflictInfo.getKey())) {
                List<IConflictInfo> propertiesConflictInfos = doExtractTaCoKitProperties(monitor, context, conflictItem,
                        conflictFile, model);
                if (propertiesConflictInfos == null || propertiesConflictInfos.isEmpty()) {
                    newConflictInfos.add(conflictInfo);
                } else {
                    newConflictInfos.addAll(propertiesConflictInfos);
                }
            } else {
                /**
                 * Here we only care about the conflicts in properties, other conflicts will be handled in
                 * KnownTalendItemConflictExtrator/KnownTalendItemConflictAnalyser
                 */
            }
        }

        return newConflictInfos;
    }

    private List<IConflictInfo> doExtractTaCoKitProperties(IProgressMonitor monitor, IAutoMergeContext context,
            AbstractConflictItem conflictItem, ConflictFile conflictFile, ModelSupplier<?> model) throws MergeException {
        AbsPropertiesConflictExtractor propertiesConflictExtractor = new AbsPropertiesConflictExtractor() {

            @Override
            protected EItemType getItemType(IAutoMergeContext context, AbstractConflictItem conflictItem,
                    ConflictFile conflictFile) {
                return TaCoKitConflictExtractor.this.getItemType(context, conflictItem, conflictFile);
            }

            @Override
            protected Object loadLeftCompareModel(IProgressMonitor monitor, IAutoMergeContext context,
                    AbstractConflictItem conflictItem, ConflictFile conflictFile) throws MergeException {
                ConnectionItem connectionItem = (ConnectionItem) conflictItem.getSelfVersionItem();
                if (connectionItem == null) {
                    return new Properties();
                } else {
                    return getTaCoKitProperties(connectionItem);
                }
            }

            @Override
            protected Object loadRightCompareModel(IProgressMonitor monitor, IAutoMergeContext context,
                    AbstractConflictItem conflictItem, ConflictFile conflictFile) throws MergeException {
                ConnectionItem connectionItem = (ConnectionItem) conflictItem.getOthersVersionItem();
                if (connectionItem == null) {
                    return new Properties();
                } else {
                    return getTaCoKitProperties(connectionItem);
                }
            }

        };
        return propertiesConflictExtractor.extract(monitor, context, conflictItem, conflictFile, model);
    }

    private Properties getTaCoKitProperties(ConnectionItem connectionItem) throws MergeException {
        try {
            Properties properties = new Properties();
            Map<Object, Object> propertiesMap = connectionItem.getConnection().getProperties();
            for (Map.Entry<Object, Object> property : propertiesMap.entrySet()) {
                properties.put(property.getKey(), property.getValue());
            }
            return properties;
        } catch (Throwable e) {
            if (e instanceof MergeException) {
                throw (MergeException) e;
            }
            throw new MergeException(e);
        }
    }

    @Override
    protected boolean isXmlConflict(IProgressMonitor monitor, IAutoMergeContext context, AbstractConflictItem conflictItem,
            ConflictFile conflictFile, Object model) throws MergeException {
        String conflictPath = conflictFile.getConflictPath();

        if (conflictPath.endsWith(FileExtensions.ITEM_FILE_SUFFIX)) {
            return true;
        }
        return false;
    }

    @Override
    protected boolean isJsonConflict(IProgressMonitor monitor, IAutoMergeContext context, AbstractConflictItem conflictItem,
            ConflictFile conflictFile, Object model) throws MergeException {
        return false;
    }

    @Override
    protected boolean isPropertiesConflict(IProgressMonitor monitor, IAutoMergeContext context, AbstractConflictItem conflictItem,
            ConflictFile conflictFile, Object model) throws MergeException {
        return false;
    }

}
