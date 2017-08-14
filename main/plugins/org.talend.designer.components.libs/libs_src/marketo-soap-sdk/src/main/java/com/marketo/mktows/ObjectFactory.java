
package com.marketo.mktows;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.marketo.mktows package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _StaticListSelectorStaticListId_QNAME = new QName("", "staticListId");
    private final static QName _StaticListSelectorStaticListName_QNAME = new QName("", "staticListName");
    private final static QName _ParamsScheduleCampaignCloneToProgramName_QNAME = new QName("", "cloneToProgramName");
    private final static QName _ParamsScheduleCampaignCampaignRunAt_QNAME = new QName("", "campaignRunAt");
    private final static QName _ParamsScheduleCampaignProgramTokenList_QNAME = new QName("", "programTokenList");
    private final static QName _ResultGetLeadChangesLeadChangeRecordList_QNAME = new QName("", "leadChangeRecordList");
    private final static QName _AttributeAttrType_QNAME = new QName("", "attrType");
    private final static QName _LeadRecordEmail_QNAME = new QName("", "Email");
    private final static QName _LeadRecordForeignSysType_QNAME = new QName("", "ForeignSysType");
    private final static QName _LeadRecordLeadAttributeList_QNAME = new QName("", "leadAttributeList");
    private final static QName _LeadRecordForeignSysPersonId_QNAME = new QName("", "ForeignSysPersonId");
    private final static QName _LeadRecordId_QNAME = new QName("", "Id");
    private final static QName _ResultGetCustomObjectsCustomObjList_QNAME = new QName("", "customObjList");
    private final static QName _ResultGetImportToListStatusEstimatedTime_QNAME = new QName("", "estimatedTime");
    private final static QName _ResultGetImportToListStatusStartedTime_QNAME = new QName("", "startedTime");
    private final static QName _ResultGetImportToListStatusRowsFailed_QNAME = new QName("", "rowsFailed");
    private final static QName _ResultGetImportToListStatusEndedTime_QNAME = new QName("", "endedTime");
    private final static QName _ResultGetImportToListStatusRowsImported_QNAME = new QName("", "rowsImported");
    private final static QName _ResultGetImportToListStatusImportSummary_QNAME = new QName("", "importSummary");
    private final static QName _ResultGetImportToListStatusRowsIgnored_QNAME = new QName("", "rowsIgnored");
    private final static QName _ResultGetImportToListStatusEstimatedRows_QNAME = new QName("", "estimatedRows");
    private final static QName _ParamsRequestCampaignLeadList_QNAME = new QName("", "leadList");
    private final static QName _ParamsRequestCampaignCampaignId_QNAME = new QName("", "campaignId");
    private final static QName _ParamsRequestCampaignProgramName_QNAME = new QName("", "programName");
    private final static QName _ParamsRequestCampaignCampaignName_QNAME = new QName("", "campaignName");
    private final static QName _LeadChangeRecordMktgAssetName_QNAME = new QName("", "mktgAssetName");
    private final static QName _LeadChangeRecordActivityAttributes_QNAME = new QName("", "activityAttributes");
    private final static QName _LeadChangeRecordId_QNAME = new QName("", "id");
    private final static QName _ResultListOperationStatusList_QNAME = new QName("", "statusList");
    private final static QName _ParamsSyncCustomObjectsOperation_QNAME = new QName("", "operation");
    private final static QName _LastUpdateAtSelectorLatestUpdatedAt_QNAME = new QName("", "latestUpdatedAt");
    private final static QName _ParamsSyncLeadMarketoCookie_QNAME = new QName("", "marketoCookie");
    private final static QName _ParamsSyncLeadMoveCookie_QNAME = new QName("", "moveCookie");
    private final static QName _MergeStatusError_QNAME = new QName("", "error");
    private final static QName _MergeStatusWinningLeadId_QNAME = new QName("", "winningLeadId");
    private final static QName _MergeStatusLosingLeadIdList_QNAME = new QName("", "losingLeadIdList");
    private final static QName _ParamsGetCustomObjectsStreamPosition_QNAME = new QName("", "streamPosition");
    private final static QName _ParamsGetCustomObjectsBatchSize_QNAME = new QName("", "batchSize");
    private final static QName _ParamsGetCustomObjectsCustomObjKeyList_QNAME = new QName("", "customObjKeyList");
    private final static QName _ResultSyncLeadLeadRecord_QNAME = new QName("", "leadRecord");
    private final static QName _ResultGetMultipleLeadsLeadRecordList_QNAME = new QName("", "leadRecordList");
    private final static QName _StreamPositionActivityCreatedAt_QNAME = new QName("", "activityCreatedAt");
    private final static QName _StreamPositionOffset_QNAME = new QName("", "offset");
    private final static QName _StreamPositionOldestCreatedAt_QNAME = new QName("", "oldestCreatedAt");
    private final static QName _StreamPositionLatestCreatedAt_QNAME = new QName("", "latestCreatedAt");
    private final static QName _VersionedItemDescription_QNAME = new QName("", "description");
    private final static QName _VersionedItemType_QNAME = new QName("", "type");
    private final static QName _ParamsGetCampaignsForSourceName_QNAME = new QName("", "name");
    private final static QName _ParamsGetCampaignsForSourceExactName_QNAME = new QName("", "exactName");
    private final static QName _LeadActivityListActivityRecordList_QNAME = new QName("", "activityRecordList");
    private final static QName _ParamsGetLeadChangesActivityNameFilter_QNAME = new QName("", "activityNameFilter");
    private final static QName _ParamsGetLeadChangesActivityFilter_QNAME = new QName("", "activityFilter");
    private final static QName _CustomObjCustomObjAttributeList_QNAME = new QName("", "customObjAttributeList");
    private final static QName _ActivityRecordPersonName_QNAME = new QName("", "personName");
    private final static QName _ActivityRecordForeignSysOrgId_QNAME = new QName("", "foreignSysOrgId");
    private final static QName _ActivityRecordOrgName_QNAME = new QName("", "orgName");
    private final static QName _ActivityRecordForeignSysId_QNAME = new QName("", "foreignSysId");
    private final static QName _ActivityRecordCampaign_QNAME = new QName("", "campaign");
    private final static QName _ParamsSyncMultipleLeadsDedupEnabled_QNAME = new QName("", "dedupEnabled");
    private final static QName _ParamsListOperationStrict_QNAME = new QName("", "strict");
    private final static QName _ResultGetCampaignsForSourceCampaignRecordList_QNAME = new QName("", "campaignRecordList");
    private final static QName _ParamsGetLeadActivityStartPosition_QNAME = new QName("", "startPosition");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.marketo.mktows
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link SuccessGetLeadActivity }
     * 
     */
    public SuccessGetLeadActivity createSuccessGetLeadActivity() {
        return new SuccessGetLeadActivity();
    }

    /**
     * Create an instance of {@link LeadActivityList }
     * 
     */
    public LeadActivityList createLeadActivityList() {
        return new LeadActivityList();
    }

    /**
     * Create an instance of {@link AuthenticationHeader }
     * 
     */
    public AuthenticationHeader createAuthenticationHeader() {
        return new AuthenticationHeader();
    }

    /**
     * Create an instance of {@link ParamsGetMultipleLeads }
     * 
     */
    public ParamsGetMultipleLeads createParamsGetMultipleLeads() {
        return new ParamsGetMultipleLeads();
    }

    /**
     * Create an instance of {@link ArrayOfString }
     * 
     */
    public ArrayOfString createArrayOfString() {
        return new ArrayOfString();
    }

    /**
     * Create an instance of {@link SuccessImportToList }
     * 
     */
    public SuccessImportToList createSuccessImportToList() {
        return new SuccessImportToList();
    }

    /**
     * Create an instance of {@link ResultImportToList }
     * 
     */
    public ResultImportToList createResultImportToList() {
        return new ResultImportToList();
    }

    /**
     * Create an instance of {@link SuccessDeleteCustomObjects }
     * 
     */
    public SuccessDeleteCustomObjects createSuccessDeleteCustomObjects() {
        return new SuccessDeleteCustomObjects();
    }

    /**
     * Create an instance of {@link ResultDeleteCustomObjects }
     * 
     */
    public ResultDeleteCustomObjects createResultDeleteCustomObjects() {
        return new ResultDeleteCustomObjects();
    }

    /**
     * Create an instance of {@link SuccessGetTags }
     * 
     */
    public SuccessGetTags createSuccessGetTags() {
        return new SuccessGetTags();
    }

    /**
     * Create an instance of {@link ResultGetTags }
     * 
     */
    public ResultGetTags createResultGetTags() {
        return new ResultGetTags();
    }

    /**
     * Create an instance of {@link ParamsDeleteCustomObjects }
     * 
     */
    public ParamsDeleteCustomObjects createParamsDeleteCustomObjects() {
        return new ParamsDeleteCustomObjects();
    }

    /**
     * Create an instance of {@link ArrayOfKeyList }
     * 
     */
    public ArrayOfKeyList createArrayOfKeyList() {
        return new ArrayOfKeyList();
    }

    /**
     * Create an instance of {@link ParamsGetCustomObjects }
     * 
     */
    public ParamsGetCustomObjects createParamsGetCustomObjects() {
        return new ParamsGetCustomObjects();
    }

    /**
     * Create an instance of {@link ArrayOfAttribute }
     * 
     */
    public ArrayOfAttribute createArrayOfAttribute() {
        return new ArrayOfAttribute();
    }

    /**
     * Create an instance of {@link SuccessSyncMultipleLeads }
     * 
     */
    public SuccessSyncMultipleLeads createSuccessSyncMultipleLeads() {
        return new SuccessSyncMultipleLeads();
    }

    /**
     * Create an instance of {@link ResultSyncMultipleLeads }
     * 
     */
    public ResultSyncMultipleLeads createResultSyncMultipleLeads() {
        return new ResultSyncMultipleLeads();
    }

    /**
     * Create an instance of {@link SuccessGetCampaignsForSource }
     * 
     */
    public SuccessGetCampaignsForSource createSuccessGetCampaignsForSource() {
        return new SuccessGetCampaignsForSource();
    }

    /**
     * Create an instance of {@link ResultGetCampaignsForSource }
     * 
     */
    public ResultGetCampaignsForSource createResultGetCampaignsForSource() {
        return new ResultGetCampaignsForSource();
    }

    /**
     * Create an instance of {@link SuccessGetLead }
     * 
     */
    public SuccessGetLead createSuccessGetLead() {
        return new SuccessGetLead();
    }

    /**
     * Create an instance of {@link ResultGetLead }
     * 
     */
    public ResultGetLead createResultGetLead() {
        return new ResultGetLead();
    }

    /**
     * Create an instance of {@link ParamsRequestCampaign }
     * 
     */
    public ParamsRequestCampaign createParamsRequestCampaign() {
        return new ParamsRequestCampaign();
    }

    /**
     * Create an instance of {@link ArrayOfLeadKey }
     * 
     */
    public ArrayOfLeadKey createArrayOfLeadKey() {
        return new ArrayOfLeadKey();
    }

    /**
     * Create an instance of {@link ArrayOfAttrib }
     * 
     */
    public ArrayOfAttrib createArrayOfAttrib() {
        return new ArrayOfAttrib();
    }

    /**
     * Create an instance of {@link SuccessGetMObjects }
     * 
     */
    public SuccessGetMObjects createSuccessGetMObjects() {
        return new SuccessGetMObjects();
    }

    /**
     * Create an instance of {@link ResultGetMObjects }
     * 
     */
    public ResultGetMObjects createResultGetMObjects() {
        return new ResultGetMObjects();
    }

    /**
     * Create an instance of {@link SuccessSyncCustomObjects }
     * 
     */
    public SuccessSyncCustomObjects createSuccessSyncCustomObjects() {
        return new SuccessSyncCustomObjects();
    }

    /**
     * Create an instance of {@link ResultSyncCustomObjects }
     * 
     */
    public ResultSyncCustomObjects createResultSyncCustomObjects() {
        return new ResultSyncCustomObjects();
    }

    /**
     * Create an instance of {@link SuccessGetMultipleLeads }
     * 
     */
    public SuccessGetMultipleLeads createSuccessGetMultipleLeads() {
        return new SuccessGetMultipleLeads();
    }

    /**
     * Create an instance of {@link ResultGetMultipleLeads }
     * 
     */
    public ResultGetMultipleLeads createResultGetMultipleLeads() {
        return new ResultGetMultipleLeads();
    }

    /**
     * Create an instance of {@link ParamsMergeLeads }
     * 
     */
    public ParamsMergeLeads createParamsMergeLeads() {
        return new ParamsMergeLeads();
    }

    /**
     * Create an instance of {@link ParamsSyncMObjects }
     * 
     */
    public ParamsSyncMObjects createParamsSyncMObjects() {
        return new ParamsSyncMObjects();
    }

    /**
     * Create an instance of {@link ArrayOfMObject }
     * 
     */
    public ArrayOfMObject createArrayOfMObject() {
        return new ArrayOfMObject();
    }

    /**
     * Create an instance of {@link ParamsSyncMultipleLeads }
     * 
     */
    public ParamsSyncMultipleLeads createParamsSyncMultipleLeads() {
        return new ParamsSyncMultipleLeads();
    }

    /**
     * Create an instance of {@link ArrayOfLeadRecord }
     * 
     */
    public ArrayOfLeadRecord createArrayOfLeadRecord() {
        return new ArrayOfLeadRecord();
    }

    /**
     * Create an instance of {@link SuccessSyncLead }
     * 
     */
    public SuccessSyncLead createSuccessSyncLead() {
        return new SuccessSyncLead();
    }

    /**
     * Create an instance of {@link ResultSyncLead }
     * 
     */
    public ResultSyncLead createResultSyncLead() {
        return new ResultSyncLead();
    }

    /**
     * Create an instance of {@link SuccessListOperation }
     * 
     */
    public SuccessListOperation createSuccessListOperation() {
        return new SuccessListOperation();
    }

    /**
     * Create an instance of {@link ResultListOperation }
     * 
     */
    public ResultListOperation createResultListOperation() {
        return new ResultListOperation();
    }

    /**
     * Create an instance of {@link SuccessRequestCampaign }
     * 
     */
    public SuccessRequestCampaign createSuccessRequestCampaign() {
        return new SuccessRequestCampaign();
    }

    /**
     * Create an instance of {@link ResultRequestCampaign }
     * 
     */
    public ResultRequestCampaign createResultRequestCampaign() {
        return new ResultRequestCampaign();
    }

    /**
     * Create an instance of {@link SuccessSyncMObjects }
     * 
     */
    public SuccessSyncMObjects createSuccessSyncMObjects() {
        return new SuccessSyncMObjects();
    }

    /**
     * Create an instance of {@link ResultSyncMObjects }
     * 
     */
    public ResultSyncMObjects createResultSyncMObjects() {
        return new ResultSyncMObjects();
    }

    /**
     * Create an instance of {@link ParamsListOperation }
     * 
     */
    public ParamsListOperation createParamsListOperation() {
        return new ParamsListOperation();
    }

    /**
     * Create an instance of {@link ListKey }
     * 
     */
    public ListKey createListKey() {
        return new ListKey();
    }

    /**
     * Create an instance of {@link SuccessGetCustomObjects }
     * 
     */
    public SuccessGetCustomObjects createSuccessGetCustomObjects() {
        return new SuccessGetCustomObjects();
    }

    /**
     * Create an instance of {@link ResultGetCustomObjects }
     * 
     */
    public ResultGetCustomObjects createResultGetCustomObjects() {
        return new ResultGetCustomObjects();
    }

    /**
     * Create an instance of {@link MktowsContextHeader }
     * 
     */
    public MktowsContextHeader createMktowsContextHeader() {
        return new MktowsContextHeader();
    }

    /**
     * Create an instance of {@link ParamsDescribeMObject }
     * 
     */
    public ParamsDescribeMObject createParamsDescribeMObject() {
        return new ParamsDescribeMObject();
    }

    /**
     * Create an instance of {@link ParamsGetCampaignsForSource }
     * 
     */
    public ParamsGetCampaignsForSource createParamsGetCampaignsForSource() {
        return new ParamsGetCampaignsForSource();
    }

    /**
     * Create an instance of {@link ParamsGetLeadChanges }
     * 
     */
    public ParamsGetLeadChanges createParamsGetLeadChanges() {
        return new ParamsGetLeadChanges();
    }

    /**
     * Create an instance of {@link StreamPosition }
     * 
     */
    public StreamPosition createStreamPosition() {
        return new StreamPosition();
    }

    /**
     * Create an instance of {@link ActivityTypeFilter }
     * 
     */
    public ActivityTypeFilter createActivityTypeFilter() {
        return new ActivityTypeFilter();
    }

    /**
     * Create an instance of {@link SuccessGetImportToListStatus }
     * 
     */
    public SuccessGetImportToListStatus createSuccessGetImportToListStatus() {
        return new SuccessGetImportToListStatus();
    }

    /**
     * Create an instance of {@link ResultGetImportToListStatus }
     * 
     */
    public ResultGetImportToListStatus createResultGetImportToListStatus() {
        return new ResultGetImportToListStatus();
    }

    /**
     * Create an instance of {@link SuccessGetLeadChanges }
     * 
     */
    public SuccessGetLeadChanges createSuccessGetLeadChanges() {
        return new SuccessGetLeadChanges();
    }

    /**
     * Create an instance of {@link ResultGetLeadChanges }
     * 
     */
    public ResultGetLeadChanges createResultGetLeadChanges() {
        return new ResultGetLeadChanges();
    }

    /**
     * Create an instance of {@link SuccessScheduleCampaign }
     * 
     */
    public SuccessScheduleCampaign createSuccessScheduleCampaign() {
        return new SuccessScheduleCampaign();
    }

    /**
     * Create an instance of {@link ResultScheduleCampaign }
     * 
     */
    public ResultScheduleCampaign createResultScheduleCampaign() {
        return new ResultScheduleCampaign();
    }

    /**
     * Create an instance of {@link SuccessListMObjects }
     * 
     */
    public SuccessListMObjects createSuccessListMObjects() {
        return new SuccessListMObjects();
    }

    /**
     * Create an instance of {@link ResultListMObjects }
     * 
     */
    public ResultListMObjects createResultListMObjects() {
        return new ResultListMObjects();
    }

    /**
     * Create an instance of {@link ParamsSyncCustomObjects }
     * 
     */
    public ParamsSyncCustomObjects createParamsSyncCustomObjects() {
        return new ParamsSyncCustomObjects();
    }

    /**
     * Create an instance of {@link ArrayOfCustomObj }
     * 
     */
    public ArrayOfCustomObj createArrayOfCustomObj() {
        return new ArrayOfCustomObj();
    }

    /**
     * Create an instance of {@link ParamsDeleteMObjects }
     * 
     */
    public ParamsDeleteMObjects createParamsDeleteMObjects() {
        return new ParamsDeleteMObjects();
    }

    /**
     * Create an instance of {@link ParamsGetMObjects }
     * 
     */
    public ParamsGetMObjects createParamsGetMObjects() {
        return new ParamsGetMObjects();
    }

    /**
     * Create an instance of {@link Attrib }
     * 
     */
    public Attrib createAttrib() {
        return new Attrib();
    }

    /**
     * Create an instance of {@link ArrayOfMObjCriteria }
     * 
     */
    public ArrayOfMObjCriteria createArrayOfMObjCriteria() {
        return new ArrayOfMObjCriteria();
    }

    /**
     * Create an instance of {@link ArrayOfMObjAssociation }
     * 
     */
    public ArrayOfMObjAssociation createArrayOfMObjAssociation() {
        return new ArrayOfMObjAssociation();
    }

    /**
     * Create an instance of {@link SuccessDescribeMObject }
     * 
     */
    public SuccessDescribeMObject createSuccessDescribeMObject() {
        return new SuccessDescribeMObject();
    }

    /**
     * Create an instance of {@link ResultDescribeMObject }
     * 
     */
    public ResultDescribeMObject createResultDescribeMObject() {
        return new ResultDescribeMObject();
    }

    /**
     * Create an instance of {@link SuccessMergeLeads }
     * 
     */
    public SuccessMergeLeads createSuccessMergeLeads() {
        return new SuccessMergeLeads();
    }

    /**
     * Create an instance of {@link ResultMergeLeads }
     * 
     */
    public ResultMergeLeads createResultMergeLeads() {
        return new ResultMergeLeads();
    }

    /**
     * Create an instance of {@link ParamsSyncLead }
     * 
     */
    public ParamsSyncLead createParamsSyncLead() {
        return new ParamsSyncLead();
    }

    /**
     * Create an instance of {@link LeadRecord }
     * 
     */
    public LeadRecord createLeadRecord() {
        return new LeadRecord();
    }

    /**
     * Create an instance of {@link ParamsListMObjects }
     * 
     */
    public ParamsListMObjects createParamsListMObjects() {
        return new ParamsListMObjects();
    }

    /**
     * Create an instance of {@link ParamsScheduleCampaign }
     * 
     */
    public ParamsScheduleCampaign createParamsScheduleCampaign() {
        return new ParamsScheduleCampaign();
    }

    /**
     * Create an instance of {@link ParamsGetChannels }
     * 
     */
    public ParamsGetChannels createParamsGetChannels() {
        return new ParamsGetChannels();
    }

    /**
     * Create an instance of {@link Tag }
     * 
     */
    public Tag createTag() {
        return new Tag();
    }

    /**
     * Create an instance of {@link ParamsGetImportToListStatus }
     * 
     */
    public ParamsGetImportToListStatus createParamsGetImportToListStatus() {
        return new ParamsGetImportToListStatus();
    }

    /**
     * Create an instance of {@link SuccessDeleteMObjects }
     * 
     */
    public SuccessDeleteMObjects createSuccessDeleteMObjects() {
        return new SuccessDeleteMObjects();
    }

    /**
     * Create an instance of {@link ResultDeleteMObjects }
     * 
     */
    public ResultDeleteMObjects createResultDeleteMObjects() {
        return new ResultDeleteMObjects();
    }

    /**
     * Create an instance of {@link SuccessGetChannels }
     * 
     */
    public SuccessGetChannels createSuccessGetChannels() {
        return new SuccessGetChannels();
    }

    /**
     * Create an instance of {@link ResultGetChannels }
     * 
     */
    public ResultGetChannels createResultGetChannels() {
        return new ResultGetChannels();
    }

    /**
     * Create an instance of {@link ParamsImportToList }
     * 
     */
    public ParamsImportToList createParamsImportToList() {
        return new ParamsImportToList();
    }

    /**
     * Create an instance of {@link ParamsGetLeadActivity }
     * 
     */
    public ParamsGetLeadActivity createParamsGetLeadActivity() {
        return new ParamsGetLeadActivity();
    }

    /**
     * Create an instance of {@link LeadKey }
     * 
     */
    public LeadKey createLeadKey() {
        return new LeadKey();
    }

    /**
     * Create an instance of {@link ParamsGetLead }
     * 
     */
    public ParamsGetLead createParamsGetLead() {
        return new ParamsGetLead();
    }

    /**
     * Create an instance of {@link ParamsGetTags }
     * 
     */
    public ParamsGetTags createParamsGetTags() {
        return new ParamsGetTags();
    }

    /**
     * Create an instance of {@link ArrayOfTag }
     * 
     */
    public ArrayOfTag createArrayOfTag() {
        return new ArrayOfTag();
    }

    /**
     * Create an instance of {@link ProgressionStatus }
     * 
     */
    public ProgressionStatus createProgressionStatus() {
        return new ProgressionStatus();
    }

    /**
     * Create an instance of {@link ArrayOfLeadChangeRecord }
     * 
     */
    public ArrayOfLeadChangeRecord createArrayOfLeadChangeRecord() {
        return new ArrayOfLeadChangeRecord();
    }

    /**
     * Create an instance of {@link MObjStatus }
     * 
     */
    public MObjStatus createMObjStatus() {
        return new MObjStatus();
    }

    /**
     * Create an instance of {@link SyncCustomObjStatus }
     * 
     */
    public SyncCustomObjStatus createSyncCustomObjStatus() {
        return new SyncCustomObjStatus();
    }

    /**
     * Create an instance of {@link CustomObj }
     * 
     */
    public CustomObj createCustomObj() {
        return new CustomObj();
    }

    /**
     * Create an instance of {@link ArrayOfSyncCustomObjStatus }
     * 
     */
    public ArrayOfSyncCustomObjStatus createArrayOfSyncCustomObjStatus() {
        return new ArrayOfSyncCustomObjStatus();
    }

    /**
     * Create an instance of {@link MObjCriteria }
     * 
     */
    public MObjCriteria createMObjCriteria() {
        return new MObjCriteria();
    }

    /**
     * Create an instance of {@link Attribute }
     * 
     */
    public Attribute createAttribute() {
        return new Attribute();
    }

    /**
     * Create an instance of {@link MObject }
     * 
     */
    public MObject createMObject() {
        return new MObject();
    }

    /**
     * Create an instance of {@link ArrayOfMObjStatus }
     * 
     */
    public ArrayOfMObjStatus createArrayOfMObjStatus() {
        return new ArrayOfMObjStatus();
    }

    /**
     * Create an instance of {@link StaticListSelector }
     * 
     */
    public StaticListSelector createStaticListSelector() {
        return new StaticListSelector();
    }

    /**
     * Create an instance of {@link MObjFieldMetadata }
     * 
     */
    public MObjFieldMetadata createMObjFieldMetadata() {
        return new MObjFieldMetadata();
    }

    /**
     * Create an instance of {@link LeadChangeRecord }
     * 
     */
    public LeadChangeRecord createLeadChangeRecord() {
        return new LeadChangeRecord();
    }

    /**
     * Create an instance of {@link VersionedItem }
     * 
     */
    public VersionedItem createVersionedItem() {
        return new VersionedItem();
    }

    /**
     * Create an instance of {@link TypeAttrib }
     * 
     */
    public TypeAttrib createTypeAttrib() {
        return new TypeAttrib();
    }

    /**
     * Create an instance of {@link ArrayOfLeadStatus }
     * 
     */
    public ArrayOfLeadStatus createArrayOfLeadStatus() {
        return new ArrayOfLeadStatus();
    }

    /**
     * Create an instance of {@link ArrayOfTagStatus }
     * 
     */
    public ArrayOfTagStatus createArrayOfTagStatus() {
        return new ArrayOfTagStatus();
    }

    /**
     * Create an instance of {@link ArrayOfBase64Binary }
     * 
     */
    public ArrayOfBase64Binary createArrayOfBase64Binary() {
        return new ArrayOfBase64Binary();
    }

    /**
     * Create an instance of {@link TagStatus }
     * 
     */
    public TagStatus createTagStatus() {
        return new TagStatus();
    }

    /**
     * Create an instance of {@link LastUpdateAtSelector }
     * 
     */
    public LastUpdateAtSelector createLastUpdateAtSelector() {
        return new LastUpdateAtSelector();
    }

    /**
     * Create an instance of {@link LeadKeySelector }
     * 
     */
    public LeadKeySelector createLeadKeySelector() {
        return new LeadKeySelector();
    }

    /**
     * Create an instance of {@link ArrayOfSyncStatus }
     * 
     */
    public ArrayOfSyncStatus createArrayOfSyncStatus() {
        return new ArrayOfSyncStatus();
    }

    /**
     * Create an instance of {@link ArrayOfActivityType }
     * 
     */
    public ArrayOfActivityType createArrayOfActivityType() {
        return new ArrayOfActivityType();
    }

    /**
     * Create an instance of {@link SyncStatus }
     * 
     */
    public SyncStatus createSyncStatus() {
        return new SyncStatus();
    }

    /**
     * Create an instance of {@link ArrayOfTypeAttrib }
     * 
     */
    public ArrayOfTypeAttrib createArrayOfTypeAttrib() {
        return new ArrayOfTypeAttrib();
    }

    /**
     * Create an instance of {@link ArrayOfVersionedItem }
     * 
     */
    public ArrayOfVersionedItem createArrayOfVersionedItem() {
        return new ArrayOfVersionedItem();
    }

    /**
     * Create an instance of {@link ArrayOfMObjFieldMetadata }
     * 
     */
    public ArrayOfMObjFieldMetadata createArrayOfMObjFieldMetadata() {
        return new ArrayOfMObjFieldMetadata();
    }

    /**
     * Create an instance of {@link ArrayOfInteger }
     * 
     */
    public ArrayOfInteger createArrayOfInteger() {
        return new ArrayOfInteger();
    }

    /**
     * Create an instance of {@link LeadStatus }
     * 
     */
    public LeadStatus createLeadStatus() {
        return new LeadStatus();
    }

    /**
     * Create an instance of {@link ArrayOfCampaignRecord }
     * 
     */
    public ArrayOfCampaignRecord createArrayOfCampaignRecord() {
        return new ArrayOfCampaignRecord();
    }

    /**
     * Create an instance of {@link ArrayOfActivityRecord }
     * 
     */
    public ArrayOfActivityRecord createArrayOfActivityRecord() {
        return new ArrayOfActivityRecord();
    }

    /**
     * Create an instance of {@link ActivityRecord }
     * 
     */
    public ActivityRecord createActivityRecord() {
        return new ActivityRecord();
    }

    /**
     * Create an instance of {@link CampaignRecord }
     * 
     */
    public CampaignRecord createCampaignRecord() {
        return new CampaignRecord();
    }

    /**
     * Create an instance of {@link MObjectMetadata }
     * 
     */
    public MObjectMetadata createMObjectMetadata() {
        return new MObjectMetadata();
    }

    /**
     * Create an instance of {@link ArrayOfProgressionStatus }
     * 
     */
    public ArrayOfProgressionStatus createArrayOfProgressionStatus() {
        return new ArrayOfProgressionStatus();
    }

    /**
     * Create an instance of {@link MObjAssociation }
     * 
     */
    public MObjAssociation createMObjAssociation() {
        return new MObjAssociation();
    }

    /**
     * Create an instance of {@link MergeStatus }
     * 
     */
    public MergeStatus createMergeStatus() {
        return new MergeStatus();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "staticListId", scope = StaticListSelector.class)
    public JAXBElement<Integer> createStaticListSelectorStaticListId(Integer value) {
        return new JAXBElement<Integer>(_StaticListSelectorStaticListId_QNAME, Integer.class, StaticListSelector.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "staticListName", scope = StaticListSelector.class)
    public JAXBElement<String> createStaticListSelectorStaticListName(String value) {
        return new JAXBElement<String>(_StaticListSelectorStaticListName_QNAME, String.class, StaticListSelector.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "cloneToProgramName", scope = ParamsScheduleCampaign.class)
    public JAXBElement<String> createParamsScheduleCampaignCloneToProgramName(String value) {
        return new JAXBElement<String>(_ParamsScheduleCampaignCloneToProgramName_QNAME, String.class, ParamsScheduleCampaign.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "campaignRunAt", scope = ParamsScheduleCampaign.class)
    public JAXBElement<XMLGregorianCalendar> createParamsScheduleCampaignCampaignRunAt(XMLGregorianCalendar value) {
        return new JAXBElement<XMLGregorianCalendar>(_ParamsScheduleCampaignCampaignRunAt_QNAME, XMLGregorianCalendar.class, ParamsScheduleCampaign.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ArrayOfAttrib }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "programTokenList", scope = ParamsScheduleCampaign.class)
    public JAXBElement<ArrayOfAttrib> createParamsScheduleCampaignProgramTokenList(ArrayOfAttrib value) {
        return new JAXBElement<ArrayOfAttrib>(_ParamsScheduleCampaignProgramTokenList_QNAME, ArrayOfAttrib.class, ParamsScheduleCampaign.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ArrayOfLeadChangeRecord }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "leadChangeRecordList", scope = ResultGetLeadChanges.class)
    public JAXBElement<ArrayOfLeadChangeRecord> createResultGetLeadChangesLeadChangeRecordList(ArrayOfLeadChangeRecord value) {
        return new JAXBElement<ArrayOfLeadChangeRecord>(_ResultGetLeadChangesLeadChangeRecordList_QNAME, ArrayOfLeadChangeRecord.class, ResultGetLeadChanges.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "attrType", scope = Attribute.class)
    public JAXBElement<String> createAttributeAttrType(String value) {
        return new JAXBElement<String>(_AttributeAttrType_QNAME, String.class, Attribute.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "Email", scope = LeadRecord.class)
    public JAXBElement<String> createLeadRecordEmail(String value) {
        return new JAXBElement<String>(_LeadRecordEmail_QNAME, String.class, LeadRecord.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ForeignSysType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "ForeignSysType", scope = LeadRecord.class)
    public JAXBElement<ForeignSysType> createLeadRecordForeignSysType(ForeignSysType value) {
        return new JAXBElement<ForeignSysType>(_LeadRecordForeignSysType_QNAME, ForeignSysType.class, LeadRecord.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ArrayOfAttribute }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "leadAttributeList", scope = LeadRecord.class)
    public JAXBElement<ArrayOfAttribute> createLeadRecordLeadAttributeList(ArrayOfAttribute value) {
        return new JAXBElement<ArrayOfAttribute>(_LeadRecordLeadAttributeList_QNAME, ArrayOfAttribute.class, LeadRecord.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "ForeignSysPersonId", scope = LeadRecord.class)
    public JAXBElement<String> createLeadRecordForeignSysPersonId(String value) {
        return new JAXBElement<String>(_LeadRecordForeignSysPersonId_QNAME, String.class, LeadRecord.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "Id", scope = LeadRecord.class)
    public JAXBElement<Integer> createLeadRecordId(Integer value) {
        return new JAXBElement<Integer>(_LeadRecordId_QNAME, Integer.class, LeadRecord.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ArrayOfCustomObj }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "customObjList", scope = ResultGetCustomObjects.class)
    public JAXBElement<ArrayOfCustomObj> createResultGetCustomObjectsCustomObjList(ArrayOfCustomObj value) {
        return new JAXBElement<ArrayOfCustomObj>(_ResultGetCustomObjectsCustomObjList_QNAME, ArrayOfCustomObj.class, ResultGetCustomObjects.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "estimatedTime", scope = ResultGetImportToListStatus.class)
    public JAXBElement<Integer> createResultGetImportToListStatusEstimatedTime(Integer value) {
        return new JAXBElement<Integer>(_ResultGetImportToListStatusEstimatedTime_QNAME, Integer.class, ResultGetImportToListStatus.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "startedTime", scope = ResultGetImportToListStatus.class)
    public JAXBElement<XMLGregorianCalendar> createResultGetImportToListStatusStartedTime(XMLGregorianCalendar value) {
        return new JAXBElement<XMLGregorianCalendar>(_ResultGetImportToListStatusStartedTime_QNAME, XMLGregorianCalendar.class, ResultGetImportToListStatus.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "rowsFailed", scope = ResultGetImportToListStatus.class)
    public JAXBElement<Integer> createResultGetImportToListStatusRowsFailed(Integer value) {
        return new JAXBElement<Integer>(_ResultGetImportToListStatusRowsFailed_QNAME, Integer.class, ResultGetImportToListStatus.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "endedTime", scope = ResultGetImportToListStatus.class)
    public JAXBElement<XMLGregorianCalendar> createResultGetImportToListStatusEndedTime(XMLGregorianCalendar value) {
        return new JAXBElement<XMLGregorianCalendar>(_ResultGetImportToListStatusEndedTime_QNAME, XMLGregorianCalendar.class, ResultGetImportToListStatus.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "rowsImported", scope = ResultGetImportToListStatus.class)
    public JAXBElement<Integer> createResultGetImportToListStatusRowsImported(Integer value) {
        return new JAXBElement<Integer>(_ResultGetImportToListStatusRowsImported_QNAME, Integer.class, ResultGetImportToListStatus.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "importSummary", scope = ResultGetImportToListStatus.class)
    public JAXBElement<String> createResultGetImportToListStatusImportSummary(String value) {
        return new JAXBElement<String>(_ResultGetImportToListStatusImportSummary_QNAME, String.class, ResultGetImportToListStatus.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "rowsIgnored", scope = ResultGetImportToListStatus.class)
    public JAXBElement<Integer> createResultGetImportToListStatusRowsIgnored(Integer value) {
        return new JAXBElement<Integer>(_ResultGetImportToListStatusRowsIgnored_QNAME, Integer.class, ResultGetImportToListStatus.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "estimatedRows", scope = ResultGetImportToListStatus.class)
    public JAXBElement<Integer> createResultGetImportToListStatusEstimatedRows(Integer value) {
        return new JAXBElement<Integer>(_ResultGetImportToListStatusEstimatedRows_QNAME, Integer.class, ResultGetImportToListStatus.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ArrayOfLeadKey }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "leadList", scope = ParamsRequestCampaign.class)
    public JAXBElement<ArrayOfLeadKey> createParamsRequestCampaignLeadList(ArrayOfLeadKey value) {
        return new JAXBElement<ArrayOfLeadKey>(_ParamsRequestCampaignLeadList_QNAME, ArrayOfLeadKey.class, ParamsRequestCampaign.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "campaignId", scope = ParamsRequestCampaign.class)
    public JAXBElement<Integer> createParamsRequestCampaignCampaignId(Integer value) {
        return new JAXBElement<Integer>(_ParamsRequestCampaignCampaignId_QNAME, Integer.class, ParamsRequestCampaign.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "programName", scope = ParamsRequestCampaign.class)
    public JAXBElement<String> createParamsRequestCampaignProgramName(String value) {
        return new JAXBElement<String>(_ParamsRequestCampaignProgramName_QNAME, String.class, ParamsRequestCampaign.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "campaignName", scope = ParamsRequestCampaign.class)
    public JAXBElement<String> createParamsRequestCampaignCampaignName(String value) {
        return new JAXBElement<String>(_ParamsRequestCampaignCampaignName_QNAME, String.class, ParamsRequestCampaign.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ArrayOfAttrib }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "programTokenList", scope = ParamsRequestCampaign.class)
    public JAXBElement<ArrayOfAttrib> createParamsRequestCampaignProgramTokenList(ArrayOfAttrib value) {
        return new JAXBElement<ArrayOfAttrib>(_ParamsScheduleCampaignProgramTokenList_QNAME, ArrayOfAttrib.class, ParamsRequestCampaign.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "mktgAssetName", scope = LeadChangeRecord.class)
    public JAXBElement<String> createLeadChangeRecordMktgAssetName(String value) {
        return new JAXBElement<String>(_LeadChangeRecordMktgAssetName_QNAME, String.class, LeadChangeRecord.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ArrayOfAttribute }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "activityAttributes", scope = LeadChangeRecord.class)
    public JAXBElement<ArrayOfAttribute> createLeadChangeRecordActivityAttributes(ArrayOfAttribute value) {
        return new JAXBElement<ArrayOfAttribute>(_LeadChangeRecordActivityAttributes_QNAME, ArrayOfAttribute.class, LeadChangeRecord.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Long }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "id", scope = LeadChangeRecord.class)
    public JAXBElement<Long> createLeadChangeRecordId(Long value) {
        return new JAXBElement<Long>(_LeadChangeRecordId_QNAME, Long.class, LeadChangeRecord.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ArrayOfLeadStatus }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "statusList", scope = ResultListOperation.class)
    public JAXBElement<ArrayOfLeadStatus> createResultListOperationStatusList(ArrayOfLeadStatus value) {
        return new JAXBElement<ArrayOfLeadStatus>(_ResultListOperationStatusList_QNAME, ArrayOfLeadStatus.class, ResultListOperation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SyncOperationEnum }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "operation", scope = ParamsSyncCustomObjects.class)
    public JAXBElement<SyncOperationEnum> createParamsSyncCustomObjectsOperation(SyncOperationEnum value) {
        return new JAXBElement<SyncOperationEnum>(_ParamsSyncCustomObjectsOperation_QNAME, SyncOperationEnum.class, ParamsSyncCustomObjects.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "latestUpdatedAt", scope = LastUpdateAtSelector.class)
    public JAXBElement<XMLGregorianCalendar> createLastUpdateAtSelectorLatestUpdatedAt(XMLGregorianCalendar value) {
        return new JAXBElement<XMLGregorianCalendar>(_LastUpdateAtSelectorLatestUpdatedAt_QNAME, XMLGregorianCalendar.class, LastUpdateAtSelector.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "marketoCookie", scope = ParamsSyncLead.class)
    public JAXBElement<String> createParamsSyncLeadMarketoCookie(String value) {
        return new JAXBElement<String>(_ParamsSyncLeadMarketoCookie_QNAME, String.class, ParamsSyncLead.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "moveCookie", scope = ParamsSyncLead.class)
    public JAXBElement<Boolean> createParamsSyncLeadMoveCookie(Boolean value) {
        return new JAXBElement<Boolean>(_ParamsSyncLeadMoveCookie_QNAME, Boolean.class, ParamsSyncLead.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "error", scope = MergeStatus.class)
    public JAXBElement<String> createMergeStatusError(String value) {
        return new JAXBElement<String>(_MergeStatusError_QNAME, String.class, MergeStatus.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "winningLeadId", scope = MergeStatus.class)
    public JAXBElement<Integer> createMergeStatusWinningLeadId(Integer value) {
        return new JAXBElement<Integer>(_MergeStatusWinningLeadId_QNAME, Integer.class, MergeStatus.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ArrayOfInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "losingLeadIdList", scope = MergeStatus.class)
    public JAXBElement<ArrayOfInteger> createMergeStatusLosingLeadIdList(ArrayOfInteger value) {
        return new JAXBElement<ArrayOfInteger>(_MergeStatusLosingLeadIdList_QNAME, ArrayOfInteger.class, MergeStatus.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "streamPosition", scope = ParamsGetCustomObjects.class)
    public JAXBElement<String> createParamsGetCustomObjectsStreamPosition(String value) {
        return new JAXBElement<String>(_ParamsGetCustomObjectsStreamPosition_QNAME, String.class, ParamsGetCustomObjects.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "batchSize", scope = ParamsGetCustomObjects.class)
    public JAXBElement<Integer> createParamsGetCustomObjectsBatchSize(Integer value) {
        return new JAXBElement<Integer>(_ParamsGetCustomObjectsBatchSize_QNAME, Integer.class, ParamsGetCustomObjects.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ArrayOfAttribute }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "customObjKeyList", scope = ParamsGetCustomObjects.class)
    public JAXBElement<ArrayOfAttribute> createParamsGetCustomObjectsCustomObjKeyList(ArrayOfAttribute value) {
        return new JAXBElement<ArrayOfAttribute>(_ParamsGetCustomObjectsCustomObjKeyList_QNAME, ArrayOfAttribute.class, ParamsGetCustomObjects.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "error", scope = SyncStatus.class)
    public JAXBElement<String> createSyncStatusError(String value) {
        return new JAXBElement<String>(_MergeStatusError_QNAME, String.class, SyncStatus.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LeadRecord }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "leadRecord", scope = ResultSyncLead.class)
    public JAXBElement<LeadRecord> createResultSyncLeadLeadRecord(LeadRecord value) {
        return new JAXBElement<LeadRecord>(_ResultSyncLeadLeadRecord_QNAME, LeadRecord.class, ResultSyncLead.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ArrayOfLeadRecord }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "leadRecordList", scope = ResultGetMultipleLeads.class)
    public JAXBElement<ArrayOfLeadRecord> createResultGetMultipleLeadsLeadRecordList(ArrayOfLeadRecord value) {
        return new JAXBElement<ArrayOfLeadRecord>(_ResultGetMultipleLeadsLeadRecordList_QNAME, ArrayOfLeadRecord.class, ResultGetMultipleLeads.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ArrayOfLeadRecord }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "leadRecordList", scope = ResultGetLead.class)
    public JAXBElement<ArrayOfLeadRecord> createResultGetLeadLeadRecordList(ArrayOfLeadRecord value) {
        return new JAXBElement<ArrayOfLeadRecord>(_ResultGetMultipleLeadsLeadRecordList_QNAME, ArrayOfLeadRecord.class, ResultGetLead.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "activityCreatedAt", scope = StreamPosition.class)
    public JAXBElement<XMLGregorianCalendar> createStreamPositionActivityCreatedAt(XMLGregorianCalendar value) {
        return new JAXBElement<XMLGregorianCalendar>(_StreamPositionActivityCreatedAt_QNAME, XMLGregorianCalendar.class, StreamPosition.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "offset", scope = StreamPosition.class)
    public JAXBElement<String> createStreamPositionOffset(String value) {
        return new JAXBElement<String>(_StreamPositionOffset_QNAME, String.class, StreamPosition.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "oldestCreatedAt", scope = StreamPosition.class)
    public JAXBElement<XMLGregorianCalendar> createStreamPositionOldestCreatedAt(XMLGregorianCalendar value) {
        return new JAXBElement<XMLGregorianCalendar>(_StreamPositionOldestCreatedAt_QNAME, XMLGregorianCalendar.class, StreamPosition.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "latestCreatedAt", scope = StreamPosition.class)
    public JAXBElement<XMLGregorianCalendar> createStreamPositionLatestCreatedAt(XMLGregorianCalendar value) {
        return new JAXBElement<XMLGregorianCalendar>(_StreamPositionLatestCreatedAt_QNAME, XMLGregorianCalendar.class, StreamPosition.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "description", scope = VersionedItem.class)
    public JAXBElement<String> createVersionedItemDescription(String value) {
        return new JAXBElement<String>(_VersionedItemDescription_QNAME, String.class, VersionedItem.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "type", scope = VersionedItem.class)
    public JAXBElement<String> createVersionedItemType(String value) {
        return new JAXBElement<String>(_VersionedItemType_QNAME, String.class, VersionedItem.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "name", scope = ParamsGetCampaignsForSource.class)
    public JAXBElement<String> createParamsGetCampaignsForSourceName(String value) {
        return new JAXBElement<String>(_ParamsGetCampaignsForSourceName_QNAME, String.class, ParamsGetCampaignsForSource.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "exactName", scope = ParamsGetCampaignsForSource.class)
    public JAXBElement<Boolean> createParamsGetCampaignsForSourceExactName(Boolean value) {
        return new JAXBElement<Boolean>(_ParamsGetCampaignsForSourceExactName_QNAME, Boolean.class, ParamsGetCampaignsForSource.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ArrayOfActivityRecord }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "activityRecordList", scope = LeadActivityList.class)
    public JAXBElement<ArrayOfActivityRecord> createLeadActivityListActivityRecordList(ArrayOfActivityRecord value) {
        return new JAXBElement<ArrayOfActivityRecord>(_LeadActivityListActivityRecordList_QNAME, ArrayOfActivityRecord.class, LeadActivityList.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ArrayOfString }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "activityNameFilter", scope = ParamsGetLeadChanges.class)
    public JAXBElement<ArrayOfString> createParamsGetLeadChangesActivityNameFilter(ArrayOfString value) {
        return new JAXBElement<ArrayOfString>(_ParamsGetLeadChangesActivityNameFilter_QNAME, ArrayOfString.class, ParamsGetLeadChanges.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ActivityTypeFilter }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "activityFilter", scope = ParamsGetLeadChanges.class)
    public JAXBElement<ActivityTypeFilter> createParamsGetLeadChangesActivityFilter(ActivityTypeFilter value) {
        return new JAXBElement<ActivityTypeFilter>(_ParamsGetLeadChangesActivityFilter_QNAME, ActivityTypeFilter.class, ParamsGetLeadChanges.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "batchSize", scope = ParamsGetLeadChanges.class)
    public JAXBElement<Integer> createParamsGetLeadChangesBatchSize(Integer value) {
        return new JAXBElement<Integer>(_ParamsGetCustomObjectsBatchSize_QNAME, Integer.class, ParamsGetLeadChanges.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ArrayOfAttribute }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "customObjAttributeList", scope = CustomObj.class)
    public JAXBElement<ArrayOfAttribute> createCustomObjCustomObjAttributeList(ArrayOfAttribute value) {
        return new JAXBElement<ArrayOfAttribute>(_CustomObjCustomObjAttributeList_QNAME, ArrayOfAttribute.class, CustomObj.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ArrayOfAttribute }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "customObjKeyList", scope = CustomObj.class)
    public JAXBElement<ArrayOfAttribute> createCustomObjCustomObjKeyList(ArrayOfAttribute value) {
        return new JAXBElement<ArrayOfAttribute>(_ParamsGetCustomObjectsCustomObjKeyList_QNAME, ArrayOfAttribute.class, CustomObj.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "personName", scope = ActivityRecord.class)
    public JAXBElement<String> createActivityRecordPersonName(String value) {
        return new JAXBElement<String>(_ActivityRecordPersonName_QNAME, String.class, ActivityRecord.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "foreignSysOrgId", scope = ActivityRecord.class)
    public JAXBElement<String> createActivityRecordForeignSysOrgId(String value) {
        return new JAXBElement<String>(_ActivityRecordForeignSysOrgId_QNAME, String.class, ActivityRecord.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "orgName", scope = ActivityRecord.class)
    public JAXBElement<String> createActivityRecordOrgName(String value) {
        return new JAXBElement<String>(_ActivityRecordOrgName_QNAME, String.class, ActivityRecord.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "foreignSysId", scope = ActivityRecord.class)
    public JAXBElement<String> createActivityRecordForeignSysId(String value) {
        return new JAXBElement<String>(_ActivityRecordForeignSysId_QNAME, String.class, ActivityRecord.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ArrayOfAttribute }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "activityAttributes", scope = ActivityRecord.class)
    public JAXBElement<ArrayOfAttribute> createActivityRecordActivityAttributes(ArrayOfAttribute value) {
        return new JAXBElement<ArrayOfAttribute>(_LeadChangeRecordActivityAttributes_QNAME, ArrayOfAttribute.class, ActivityRecord.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "campaign", scope = ActivityRecord.class)
    public JAXBElement<String> createActivityRecordCampaign(String value) {
        return new JAXBElement<String>(_ActivityRecordCampaign_QNAME, String.class, ActivityRecord.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Long }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "id", scope = ActivityRecord.class)
    public JAXBElement<Long> createActivityRecordId(Long value) {
        return new JAXBElement<Long>(_LeadChangeRecordId_QNAME, Long.class, ActivityRecord.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "streamPosition", scope = ParamsGetMultipleLeads.class)
    public JAXBElement<String> createParamsGetMultipleLeadsStreamPosition(String value) {
        return new JAXBElement<String>(_ParamsGetCustomObjectsStreamPosition_QNAME, String.class, ParamsGetMultipleLeads.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "batchSize", scope = ParamsGetMultipleLeads.class)
    public JAXBElement<Integer> createParamsGetMultipleLeadsBatchSize(Integer value) {
        return new JAXBElement<Integer>(_ParamsGetCustomObjectsBatchSize_QNAME, Integer.class, ParamsGetMultipleLeads.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "dedupEnabled", scope = ParamsSyncMultipleLeads.class)
    public JAXBElement<Boolean> createParamsSyncMultipleLeadsDedupEnabled(Boolean value) {
        return new JAXBElement<Boolean>(_ParamsSyncMultipleLeadsDedupEnabled_QNAME, Boolean.class, ParamsSyncMultipleLeads.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "strict", scope = ParamsListOperation.class)
    public JAXBElement<Boolean> createParamsListOperationStrict(Boolean value) {
        return new JAXBElement<Boolean>(_ParamsListOperationStrict_QNAME, Boolean.class, ParamsListOperation.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ArrayOfCampaignRecord }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "campaignRecordList", scope = ResultGetCampaignsForSource.class)
    public JAXBElement<ArrayOfCampaignRecord> createResultGetCampaignsForSourceCampaignRecordList(ArrayOfCampaignRecord value) {
        return new JAXBElement<ArrayOfCampaignRecord>(_ResultGetCampaignsForSourceCampaignRecordList_QNAME, ArrayOfCampaignRecord.class, ResultGetCampaignsForSource.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "error", scope = SyncCustomObjStatus.class)
    public JAXBElement<String> createSyncCustomObjStatusError(String value) {
        return new JAXBElement<String>(_MergeStatusError_QNAME, String.class, SyncCustomObjStatus.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ArrayOfAttribute }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "customObjKeyList", scope = SyncCustomObjStatus.class)
    public JAXBElement<ArrayOfAttribute> createSyncCustomObjStatusCustomObjKeyList(ArrayOfAttribute value) {
        return new JAXBElement<ArrayOfAttribute>(_ParamsGetCustomObjectsCustomObjKeyList_QNAME, ArrayOfAttribute.class, SyncCustomObjStatus.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ActivityTypeFilter }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "activityFilter", scope = ParamsGetLeadActivity.class)
    public JAXBElement<ActivityTypeFilter> createParamsGetLeadActivityActivityFilter(ActivityTypeFilter value) {
        return new JAXBElement<ActivityTypeFilter>(_ParamsGetLeadChangesActivityFilter_QNAME, ActivityTypeFilter.class, ParamsGetLeadActivity.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "batchSize", scope = ParamsGetLeadActivity.class)
    public JAXBElement<Integer> createParamsGetLeadActivityBatchSize(Integer value) {
        return new JAXBElement<Integer>(_ParamsGetCustomObjectsBatchSize_QNAME, Integer.class, ParamsGetLeadActivity.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StreamPosition }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "startPosition", scope = ParamsGetLeadActivity.class)
    public JAXBElement<StreamPosition> createParamsGetLeadActivityStartPosition(StreamPosition value) {
        return new JAXBElement<StreamPosition>(_ParamsGetLeadActivityStartPosition_QNAME, StreamPosition.class, ParamsGetLeadActivity.class, value);
    }

}
