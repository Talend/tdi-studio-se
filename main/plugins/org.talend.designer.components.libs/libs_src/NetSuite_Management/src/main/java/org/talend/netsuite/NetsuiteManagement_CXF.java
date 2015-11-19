package org.talend.netsuite;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.ws.BindingProvider;

import org.apache.cxf.headers.Header;
import org.apache.cxf.jaxb.JAXBDataBinding;

import com.netsuite.webservices.platform.ExceededRequestLimitFault;
import com.netsuite.webservices.platform.ExceededRequestSizeFault;
import com.netsuite.webservices.platform.InsufficientPermissionFault;
import com.netsuite.webservices.platform.InvalidAccountFault;
import com.netsuite.webservices.platform.InvalidCredentialsFault;
import com.netsuite.webservices.platform.InvalidVersionFault;
import com.netsuite.webservices.platform.NetSuitePortType;
import com.netsuite.webservices.platform.NetSuiteService;
import com.netsuite.webservices.platform.UnexpectedErrorFault;
import com.netsuite.webservices.platform.core.DataCenterUrls;
import com.netsuite.webservices.platform.core.ListOrRecordRef;
import com.netsuite.webservices.platform.core.NullField;
import com.netsuite.webservices.platform.core.Passport;
import com.netsuite.webservices.platform.core.Record;
import com.netsuite.webservices.platform.core.RecordRef;
import com.netsuite.webservices.platform.core.SearchBooleanCustomField;
import com.netsuite.webservices.platform.core.SearchBooleanField;
import com.netsuite.webservices.platform.core.SearchCustomField;
import com.netsuite.webservices.platform.core.SearchCustomFieldList;
import com.netsuite.webservices.platform.core.SearchDateCustomField;
import com.netsuite.webservices.platform.core.SearchDateField;
import com.netsuite.webservices.platform.core.SearchDoubleCustomField;
import com.netsuite.webservices.platform.core.SearchDoubleField;
import com.netsuite.webservices.platform.core.SearchEnumMultiSelectField;
import com.netsuite.webservices.platform.core.SearchLongCustomField;
import com.netsuite.webservices.platform.core.SearchLongField;
import com.netsuite.webservices.platform.core.SearchMultiSelectCustomField;
import com.netsuite.webservices.platform.core.SearchMultiSelectField;
import com.netsuite.webservices.platform.core.SearchRecord;
import com.netsuite.webservices.platform.core.SearchResult;
import com.netsuite.webservices.platform.core.SearchStringCustomField;
import com.netsuite.webservices.platform.core.SearchStringField;
import com.netsuite.webservices.platform.core.Status;
import com.netsuite.webservices.platform.core.StatusDetail;
import com.netsuite.webservices.platform.core.types.RecordType;
import com.netsuite.webservices.platform.core.types.SearchDateFieldOperator;
import com.netsuite.webservices.platform.core.types.SearchDoubleFieldOperator;
import com.netsuite.webservices.platform.core.types.SearchEnumMultiSelectFieldOperator;
import com.netsuite.webservices.platform.core.types.SearchLongFieldOperator;
import com.netsuite.webservices.platform.core.types.SearchMultiSelectFieldOperator;
import com.netsuite.webservices.platform.core.types.SearchStringFieldOperator;
import com.netsuite.webservices.platform.messages.AddRequest;
import com.netsuite.webservices.platform.messages.AddResponse;
import com.netsuite.webservices.platform.messages.DeleteRequest;
import com.netsuite.webservices.platform.messages.DeleteResponse;
import com.netsuite.webservices.platform.messages.GetDataCenterUrlsRequest;
import com.netsuite.webservices.platform.messages.GetRequest;
import com.netsuite.webservices.platform.messages.GetResponse;
import com.netsuite.webservices.platform.messages.LoginRequest;
import com.netsuite.webservices.platform.messages.Preferences;
import com.netsuite.webservices.platform.messages.SearchMoreWithIdRequest;
import com.netsuite.webservices.platform.messages.SearchPreferences;
import com.netsuite.webservices.platform.messages.SearchRequest;
import com.netsuite.webservices.platform.messages.UpdateRequest;
import com.netsuite.webservices.platform.messages.UpdateResponse;
import com.netsuite.webservices.platform.messages.WriteResponse;
import com.netsuite.webservices.setup.customization.CustomRecord;
import com.netsuite.webservices.setup.customization.CustomRecordSearch;
import com.netsuite.webservices.setup.customization.CustomRecordSearchAdvanced;

public class NetsuiteManagement_CXF {
	
	static {
		// adjusting the log levels
		System.setProperty("java.util.logging.config.file", NetsuiteManagement_CXF.class.getResource("/META-INF/logging.properties").toString());
	}
	
	private NetSuitePortType _port;
	
	private String _url;
	private String _email;
	private String _pwd;
	private String _role;
	private String _account;
	private int _pageSize = 100;
	
	public NetsuiteManagement_CXF(String email, String pwd, String role, String account, String url) {
		this._email = email;
		this._pwd = pwd;
		this._role = role;
		this._url = url;
		this._account = account;
	}
	
	private Class<?> entityClass;
	private Class<?> searchClass;
	private Class<?> searchBasicClass;
	private Class<?> searchAdvancedClass;
	
	private SearchRecord search;             // search class' instance
	private SearchRecord searchBasic;        // search basic class' instance
	private SearchRecord searchAdvanced;     // search advanced class' instance
	
	private boolean isItemSearch;
	private String searchId = null;

	// no use for now
	private SearchCustomField[] customCriteria;
	
	public void initialize(String searchentity, String savedSearchId) throws SecurityException, NoSuchMethodException, SOAPException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		this.entityClass = TalendComponentGenerator.getEntityClass(searchentity);
		this.searchClass = TalendComponentGenerator.getSearchClass(searchentity);
		this.searchBasicClass = TalendComponentGenerator.getSearchBasicClass(searchentity);
		this.searchAdvancedClass = TalendComponentGenerator.getSearchAdvancedClass(searchentity);
		
		this.customCriteria = new SearchCustomField[1];

		// if type is customRecord
		if (this.searchClass == null) {
			this.searchClass = CustomRecordSearch.class;
			searchentity = CustomRecord.class.getName();
			this.searchAdvancedClass = CustomRecordSearchAdvanced.class;
			this.searchBasicClass = TalendComponentGenerator.getSearchBasicClass(searchentity);
		}

		// search class not found or supported
		if (this.searchClass == null) {
			throw new IllegalArgumentException("SearchClass not found - " + searchentity);
		}

		// get a search class instance
		Constructor<?> constructor = this.searchClass.getConstructor(new Class[0]);
		this.search = ((SearchRecord) constructor.newInstance(new Object[0]));
		
		// get a advanced search class instance and set SaveSearchId into it
		this.searchAdvanced = null;
		if ((savedSearchId != null) && (savedSearchId.length() > 0)) {
			constructor = this.searchAdvancedClass.getConstructor(new Class[0]);
			this.searchAdvanced = ((SearchRecord) constructor.newInstance(new Object[0]));
			Method setSavedSearchIdMethod = findMethod(this.searchAdvancedClass, "setSavedSearchId");
			Object[] args = { savedSearchId };
			setSavedSearchIdMethod.invoke(this.searchAdvanced, args);
		}

		// search class is itemSearch, it's special
		if (this.searchClass.getSimpleName().equals("ItemSearch")) {
			this.isItemSearch = true;
		}

		// basic search class not found or supported
		if (this.searchBasicClass == null) {
			throw new IllegalArgumentException("SearchBasicClass not found-" + searchentity);
		}

		// get a basic search class instance
		constructor = this.searchBasicClass.getConstructor(null);
		this.searchBasic = ((SearchRecord) constructor.newInstance(new Object[0]));
	}
	
	public void initializeStub() throws JAXBException, UnexpectedErrorFault, ExceededRequestSizeFault, InvalidCredentialsFault, InvalidVersionFault, InsufficientPermissionFault, ExceededRequestLimitFault, InvalidAccountFault, MalformedURLException {
		URL wsdl_locationUrl = this.getClass().getResource("/wsdl/netsuite.wsdl");
		QName serviceQname = new QName("urn:platform_2014_2.webservices.netsuite.com", "NetSuiteService");
		NetSuiteService service = new NetSuiteService(wsdl_locationUrl, serviceQname);
		NetSuitePortType port = service.getNetSuitePort();
		
		Preferences preferences = new Preferences();
		preferences.setDisableMandatoryCustomFieldValidation(Boolean.FALSE);
		preferences.setWarningAsError(Boolean.FALSE);
		preferences.setIgnoreReadOnlyFields(Boolean.TRUE);
		preferences.setDisableMandatoryCustomFieldValidation(Boolean.TRUE);
		
		SearchPreferences searchPreferences = new SearchPreferences();
		searchPreferences.setPageSize(this._pageSize);
		searchPreferences.setBodyFieldsOnly(Boolean.valueOf(false));
		
		RecordRef role = new RecordRef();
		role.setInternalId(this._role);
		Passport passport = new Passport();
		passport.setEmail(this._email);
		passport.setPassword(this._pwd);
		passport.setRole(role);
		passport.setAccount(this._account);
		
		// Get the webservices domain for your account
		GetDataCenterUrlsRequest dataCenterRequest = new GetDataCenterUrlsRequest();
		dataCenterRequest.setAccount(this._account);
		DataCenterUrls urls = port.getDataCenterUrls(dataCenterRequest).getGetDataCenterUrlsResult().getDataCenterUrls();
		String wsDomain = urls.getWebservicesDomain();
		String endpoint = wsDomain.concat(new URL(this._url).getPath());
		
		BindingProvider provider = (BindingProvider) port;
		Map<String, Object> requestContext = provider.getRequestContext();
		requestContext.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
		requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);

		List<Header> list = (List<Header>)requestContext.get(Header.HEADER_LIST);
		if(list == null){
			list = new ArrayList<Header>();
			requestContext.put(Header.HEADER_LIST, list);
		}
		
		Header searchPreferences_header = new Header(new QName("urn:messages_2014_2.platform.webservices.netsuite.com", "searchPreferences"), searchPreferences, new JAXBDataBinding(searchPreferences.getClass()));
		Header preferences_header = new Header(new QName("urn:messages_2014_2.platform.webservices.netsuite.com", "preferences"), preferences, new JAXBDataBinding(preferences.getClass()));
		list.add(searchPreferences_header);
		list.add(preferences_header);
		
		LoginRequest request = new LoginRequest();
		request.setPassport(passport);
		
		port.login(request);
		
		this._port = port;
		
		Arrays.asList(new String[]{"", "", ""});
	}
	
	private Method criteriaSetter;
	private int size = 0;
	private boolean hasCustomCriteria = false;
	
	private SearchResult searchResult;

	public void search(String searchentity, String searchFieldName, String searchOperator, List<String> searchValue, String forcedType) throws DatatypeConfigurationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if ((searchValue.get(0) != null) && (searchFieldName != null)) {
			this.criteriaSetter = findMethod(this.searchBasicClass, "set" + searchFieldName);
			
			if (this.criteriaSetter == null) {
				SearchCustomField customCriteria = null;

				if (forcedType.equals("String")) {
					SearchStringCustomField searchArgumentType = new SearchStringCustomField();
					searchArgumentType.setInternalId(searchFieldName);
					
					if(searchValue != null && searchValue.size() != 0){
						searchArgumentType.setSearchValue(searchValue.get(0));
					}

					searchArgumentType
							.setOperator(SearchStringFieldOperator
									.fromValue(searchOperator));
					customCriteria = searchArgumentType;
				} else if (forcedType.equals("Long")) {
					SearchLongCustomField searchArgumentType = new SearchLongCustomField();
					searchArgumentType.setInternalId(searchFieldName);
					
					if(searchValue != null && searchValue.size() != 0){
						searchArgumentType.setSearchValue(Long.valueOf(Long
								.parseLong(searchValue.get(0))));

						if (searchValue.size() > 1) {
							searchArgumentType.setSearchValue2(Long
									.valueOf(Long.parseLong(searchValue.get(1))));
						}
					}

					searchArgumentType.setOperator(SearchLongFieldOperator
							.fromValue(searchOperator));
					customCriteria = searchArgumentType;
				} else if (forcedType.equals("Date")) {
					SearchDateCustomField searchArgumentType = new SearchDateCustomField();
					
					if(searchValue != null && searchValue.size() != 0){
						Calendar calValue = Calendar.getInstance();
						Calendar calValue2 = Calendar.getInstance();

						String dateFormat = "yyyy-MM-dd";
						String timeFormat = "HH:mm:ss";

						String format = dateFormat + " " + timeFormat;
						if (searchValue.get(0).length() == dateFormat.length()) {
							format = dateFormat;
						}
						
						if (searchValue.get(0).length() == timeFormat.length()) {
							searchValue.set(0, new SimpleDateFormat(dateFormat).format(calValue.getTime()) + " " + searchValue.get(0));
							if(searchValue.size() > 1){
								searchValue.set(1, new SimpleDateFormat(dateFormat).format(calValue.getTime()) + " " + searchValue.get(1));
							}
						}
						
						DateFormat df = new SimpleDateFormat(format);
						
						try {
							calValue.setTime(df.parse(searchValue.get(0)));
							if(searchValue.size() > 1){
								calValue2.setTime(df.parse(searchValue.get(1)));
							}
						} catch (ParseException e) {
							e.printStackTrace();
						}
						
						XMLGregorianCalendar xts = DatatypeFactory.newInstance().newXMLGregorianCalendar();
						xts.setYear(calValue.get(Calendar.YEAR));
						xts.setMonth(calValue.get(Calendar.MONTH) +1);
						xts.setDay(calValue.get(Calendar.DAY_OF_MONTH));
						xts.setHour(calValue.get(Calendar.HOUR_OF_DAY));
						xts.setMinute(calValue.get(Calendar.MINUTE));
						xts.setSecond(calValue.get(Calendar.SECOND));
						xts.setMillisecond(calValue.get(Calendar.MILLISECOND));
						xts.setTimezone(calValue.get(Calendar.ZONE_OFFSET) / 60000 );
						
						searchArgumentType.setSearchValue(xts);
						
						if(searchValue.size() > 1){
							XMLGregorianCalendar xts2 = DatatypeFactory.newInstance().newXMLGregorianCalendar();
							xts2.setYear(calValue.get(Calendar.YEAR));
							xts2.setMonth(calValue.get(Calendar.MONTH) +1);
							xts2.setDay(calValue.get(Calendar.DAY_OF_MONTH));
							xts2.setHour(calValue.get(Calendar.HOUR_OF_DAY));
							xts2.setMinute(calValue.get(Calendar.MINUTE));
							xts2.setSecond(calValue.get(Calendar.SECOND));
							xts2.setMillisecond(calValue.get(Calendar.MILLISECOND));
							xts2.setTimezone(calValue.get(Calendar.ZONE_OFFSET) / 60000 );
							searchArgumentType.setSearchValue2(xts2);
						}
					}
					
					searchArgumentType.setOperator(SearchDateFieldOperator.fromValue(searchOperator));
					customCriteria = searchArgumentType;
				} else if (forcedType.equals("Boolean")) {
					SearchBooleanCustomField searchArgumentType = new SearchBooleanCustomField();
					searchArgumentType.setInternalId(searchFieldName);
					searchArgumentType.setSearchValue(Boolean
							.valueOf(Boolean.parseBoolean(searchValue.get(0))));
					customCriteria = searchArgumentType;
				} else if (forcedType.equals("Double")) {
					SearchDoubleCustomField searchArgumentType = new SearchDoubleCustomField();
					searchArgumentType.setInternalId(searchFieldName);
					
					if(searchValue != null && searchValue.size() != 0){
						searchArgumentType.setSearchValue(Double.valueOf(Double
								.parseDouble(searchValue.get(0))));
						

						if (searchValue.size() > 1) {
							searchArgumentType
									.setSearchValue2(Double.valueOf(Double
											.parseDouble(searchValue.get(1))));
						}
					}
					
					searchArgumentType
					.setOperator(SearchDoubleFieldOperator
							.fromValue(searchOperator));

					customCriteria = searchArgumentType;
				} else if (forcedType.equals("List")) {
					SearchMultiSelectCustomField searchArgumentType = new SearchMultiSelectCustomField();

					int len = searchValue.size();

					List<ListOrRecordRef> lr = searchArgumentType.getSearchValue();
					for (int i = 0; i < len; i++) {
						ListOrRecordRef lRecordRef = new ListOrRecordRef();
						lRecordRef.setName(searchValue.get(i));
						lr.add(lRecordRef);
					}
					
					searchArgumentType
							.setOperator(SearchMultiSelectFieldOperator
									.fromValue(searchOperator));
					customCriteria = searchArgumentType;
				} else {
					throw new IllegalArgumentException("Unsupported search field type: " + forcedType);
				}

				push(customCriteria);

				this.hasCustomCriteria = true;
			} else {
				this.criteriaSetter.invoke(this.searchBasic, new Object[] { getSearchField(this.searchBasicClass, searchValue, searchFieldName, searchOperator) });
			}
		}
	}
	
	private Object getSearchField(Class<?> searchClass, List<String> searchValue, String searchFieldName, String searchOperator) throws DatatypeConfigurationException {
		Object criteria = null;
		Method getFieldMethod = findMethod(searchClass, "get" + searchFieldName);
		String searchType = getFieldMethod.getReturnType().getSimpleName();

		if (searchType.equals("SearchStringField")) {
			
			SearchStringField searchArgumentType = new SearchStringField();
			
			if(searchValue != null && searchValue.size() != 0){
				searchArgumentType.setSearchValue(searchValue.get(0));
			}
			
			searchArgumentType.setOperator(SearchStringFieldOperator.fromValue(searchOperator));
			criteria = searchArgumentType;
			
		} else if (searchType.equals("SearchLongField")) {
			
			SearchLongField searchArgumentType = new SearchLongField();
			
			if(searchValue != null && searchValue.size() != 0){
				searchArgumentType.setSearchValue(Long.valueOf(Long.parseLong(searchValue.get(0))));
				
				if (searchValue.size() > 1) {
					searchArgumentType.setSearchValue2(Long.valueOf(Long.parseLong(searchValue.get(1))));
				}
			}

			searchArgumentType.setOperator(SearchLongFieldOperator.fromValue(searchOperator));
			criteria = searchArgumentType;
			
		} else if (searchType.equals("SearchDateField")) {
			
			SearchDateField searchArgumentType = new SearchDateField();
			
			if(searchValue != null && searchValue.size() != 0){
				Calendar calValue = Calendar.getInstance();

				String dateFormat = "yyyy-MM-dd";
				String timeFormat = "HH:mm:ss";

				String format = dateFormat + " " + timeFormat;
				
				if (searchValue.get(0).length() == dateFormat.length()) {
					format = dateFormat;
				}
				
				if (searchValue.get(0).length() == timeFormat.length()) {
					searchValue.set(0, new SimpleDateFormat(dateFormat).format(calValue.getTime()) + " " + searchValue.get(0));
				}

				DateFormat df = new SimpleDateFormat(format);
				
				try {
					calValue.setTime(df.parse(searchValue.get(0)));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
				XMLGregorianCalendar xts = DatatypeFactory.newInstance().newXMLGregorianCalendar();
				xts.setYear(calValue.get(Calendar.YEAR));
				xts.setMonth(calValue.get(Calendar.MONTH) +1);
				xts.setDay(calValue.get(Calendar.DAY_OF_MONTH));
				xts.setHour(calValue.get(Calendar.HOUR_OF_DAY));
				xts.setMinute(calValue.get(Calendar.MINUTE));
				xts.setSecond(calValue.get(Calendar.SECOND));
				xts.setMillisecond(calValue.get(Calendar.MILLISECOND));
				xts.setTimezone(calValue.get(Calendar.ZONE_OFFSET) / 60000 );
				
				searchArgumentType.setSearchValue(xts);
				
				if (searchValue.size() > 1) {
					try {
						calValue.setTime(df.parse(searchValue.get(1)));
					} catch (ParseException e) {
						e.printStackTrace();
					}
					
					XMLGregorianCalendar xts2 = DatatypeFactory.newInstance().newXMLGregorianCalendar();
					xts2.setYear(calValue.get(Calendar.YEAR));
					xts2.setMonth(calValue.get(Calendar.MONTH) +1);
					xts2.setDay(calValue.get(Calendar.DAY_OF_MONTH));
					xts2.setHour(calValue.get(Calendar.HOUR_OF_DAY));
					xts2.setMinute(calValue.get(Calendar.MINUTE));
					xts2.setSecond(calValue.get(Calendar.SECOND));
					xts2.setMillisecond(calValue.get(Calendar.MILLISECOND));
					xts2.setTimezone(calValue.get(Calendar.ZONE_OFFSET) / 60000 );
				
					searchArgumentType.setSearchValue2(xts2);
				}
			}

			searchArgumentType.setOperator(SearchDateFieldOperator.fromValue(searchOperator));
			
			criteria = searchArgumentType;
			
		} else if (searchType.equals("SearchBooleanField")) {
			SearchBooleanField searchArgumentType = new SearchBooleanField();
			
			searchArgumentType.setSearchValue(Boolean.valueOf(searchValue.get(0)));
			criteria = searchArgumentType;
			
		} else if (searchType.equals("SearchDoubleField")) {
			
			SearchDoubleField searchArgumentType = new SearchDoubleField();

			if(searchValue != null && searchValue.size() != 0){
				searchArgumentType.setSearchValue(Double.valueOf(Double.parseDouble(searchValue.get(0))));
				
				if (searchValue.size() > 1) {
					searchArgumentType.setSearchValue2(Double.valueOf(Double.parseDouble(searchValue.get(1))));
				}
			}

			searchArgumentType.setOperator(SearchDoubleFieldOperator.fromValue(searchOperator));
			criteria = searchArgumentType;
			
		} else if (searchType.equals("SearchMultiSelectField")) {
			
			SearchMultiSelectField searchArgumentType = new SearchMultiSelectField();

			List<RecordRef> values = searchArgumentType.getSearchValue();
			for (int i = 0; i < searchValue.size(); i++) {
				RecordRef ref = new RecordRef();
				ref.setName(searchValue.get(i));
				ref.setInternalId(searchValue.get(i));
				ref.setExternalId(null);
				ref.setType(null);
				values.add(ref);
			}

			searchArgumentType.setOperator(SearchMultiSelectFieldOperator.fromValue(searchOperator));
			criteria = searchArgumentType;
			
		} else if (searchType.equals("SearchEnumMultiSelectField")) {
			
			SearchEnumMultiSelectField searchArgumentType = new SearchEnumMultiSelectField();
			searchArgumentType.getSearchValue().addAll(searchValue);
			searchArgumentType.setOperator(SearchEnumMultiSelectFieldOperator.fromValue(searchOperator));
			criteria = searchArgumentType;
			
		} else if (searchType.equals("String[]")) {
			
			SearchEnumMultiSelectField searchArgumentType = new SearchEnumMultiSelectField();
			searchArgumentType.getSearchValue().addAll(searchValue);
			searchArgumentType.setOperator(SearchEnumMultiSelectFieldOperator.fromValue(searchOperator));
			criteria = searchArgumentType;
			
		} else {
			throw new IllegalArgumentException("Unsupported search field type: " + searchType);
		}

		return criteria;
	}
	
	public List<Record> submitRequest(boolean useRequestLevelAuth) throws Exception {
		if (useRequestLevelAuth) {
			initializeStub();
		}

		List<String> list = Arrays.asList(TalendComponentGenerator.transactionTypeList);

		if (list.contains(this.entityClass.getSimpleName())) {
			Method typeSetter = findMethod(this.searchBasicClass, "setType");
			SearchEnumMultiSelectField semsf = new SearchEnumMultiSelectField();
			semsf.getSearchValue().add(TalendComponentGenerator.toInitialLower(this.entityClass.getSimpleName()));
			semsf.setOperator(SearchEnumMultiSelectFieldOperator.ANY_OF);
			typeSetter.invoke(this.searchBasic, new Object[] { semsf });
		}

		if (this.hasCustomCriteria) {
			this.criteriaSetter = findMethod(this.searchBasicClass, "setCustomFieldList");
			SearchCustomFieldList scl = new SearchCustomFieldList();
			scl.getCustomField().addAll(Arrays.asList(this.customCriteria));
			this.criteriaSetter.invoke(this.searchBasic, new Object[] { scl });
		}

		Method basicSetter = findMethod(this.searchClass, "setBasic");
		basicSetter.invoke(this.search, new Object[] { this.searchBasic });

		SearchRecord s = this.search;

		if (this.searchAdvanced != null) {
			s = this.searchAdvanced;
			Method setCriteriaMethod = findMethod(this.searchAdvancedClass, "setCriteria");
			setCriteriaMethod.invoke(this.searchAdvanced, new Object[] { this.search });
		}
		
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.setSearchRecord(s);

		this.searchResult = this._port.search(searchRequest).getSearchResult();

		if (this.searchResult.getStatus().isIsSuccess()) {
			this.searchId = this.searchResult.getSearchId();
			if (this.searchResult.getRecordList() != null) {
				return filterItemSearch();
			}
		}

		return null;
	}
	
	public List<Record> filterItemSearch() {
		if (this.isItemSearch) {
			int numtodelete = 0;
			List<Record> r = this.searchResult.getRecordList().getRecord();
			
			for (int i = 0; (r != null) && (i < this.searchResult.getRecordList().getRecord().size()); i++) {
				if (!this.searchResult.getRecordList().getRecord().get(i).getClass().equals(this.entityClass)) {
					this.searchResult.getRecordList().getRecord().set(i, null);
					numtodelete++;
				}
			}
			
			if (numtodelete > 0) {
				ArrayList<Record> newRecords = new ArrayList<Record>(this.searchResult.getRecordList().getRecord().size() - numtodelete);
				int newindex = 0;
				for (int i = 0; i < this.searchResult.getRecordList().getRecord().size(); i++) {
					if (this.searchResult.getRecordList().getRecord().get(i) != null) {
						newRecords.set(newindex, this.searchResult.getRecordList().getRecord().get(i));
						newindex++;
					}
				}
				return newRecords;
			}
			
		}
		
		if (this.searchResult.getRecordList().getRecord() == null) {
			return new ArrayList<Record>();
		}
		
		return this.searchResult.getRecordList().getRecord();
	}
	
	public boolean hasMore() {
		if (this.searchResult == null) {
			return false;
		}

		if (this.searchResult.getPageIndex() == null) {
			return false;
		}

		if (this.searchResult.getTotalPages() == null) {
			return false;
		}

		return this.searchResult.getPageIndex().intValue() < this.searchResult.getTotalPages().intValue();
	}
	
	private void push(SearchCustomField newCriteria) {
		ensureCapacity();
		this.customCriteria[(this.size++)] = newCriteria;
	}
	
	private void ensureCapacity() {
		if (this.customCriteria.length == this.size) {
			this.customCriteria = ((SearchCustomField[]) Arrays.copyOf(this.customCriteria, this.size + 1));
		}
	}
	
	public List<Record> getMoreRecords() throws InvalidCredentialsFault, com.netsuite.webservices.platform.ExceededRecordCountFault, com.netsuite.webservices.platform.InvalidSessionFault, ExceededRequestLimitFault, com.netsuite.webservices.platform.ExceededUsageLimitFault, UnexpectedErrorFault, ExceededRequestSizeFault {

		if (this.searchId != null) {
			SearchMoreWithIdRequest searchMoreRequest = new SearchMoreWithIdRequest();
			searchMoreRequest.setSearchId(this.searchId);
			searchMoreRequest.setPageIndex(this.searchResult.getPageIndex().intValue() + 1);
			
			this.searchResult = this._port.searchMoreWithId(searchMoreRequest).getSearchResult();

			if (this.searchResult.getStatus().isIsSuccess()) {
				return filterItemSearch();
			}
			return null;
		}

		return null;
	}
	
	public String insert(Record record) throws Exception {
		
		initializeStub();
		
		AddRequest addRequest = new AddRequest();
		addRequest.setRecord(record);
		AddResponse response = getPort().add(addRequest);
		
		WriteResponse wr = response.getWriteResponse();

		if (!wr.getStatus().isIsSuccess()) {
			throw new NetsuiteException(getErrorCodes(wr.getStatus()), getErrorMessages(wr.getStatus()));
		}

		java.lang.reflect.Field field = wr.getBaseRef().getClass().getDeclaredField("internalId");
		field.setAccessible(true);
		String internalId = (String) field.get(wr.getBaseRef());

		return internalId;
	}
	
	public void update(Record record) throws Exception {
		initializeStub();
		
		UpdateRequest updateRequest = new UpdateRequest();
		updateRequest.setRecord(record);

		UpdateResponse response = getPort().update(updateRequest);
		
		java.lang.reflect.Field field = record.getClass().getDeclaredField("internalId");
		field.setAccessible(true);
		String internalId = (String) field.get(record);

		if (!response.getWriteResponse().getStatus().isIsSuccess()) {
			throw new NetsuiteException(getErrorCodes(response.getWriteResponse().getStatus()), getErrorMessages(response.getWriteResponse().getStatus()));
		}
	}
	
	public String upsert(Record record) throws Exception {
		
		initializeStub();
		
		Record existingRecord = null;
		String entityType = record.getClass().getName();

		String internalID = (String) invokeGetter(record, "InternalId");
		if (internalID != null) {
			existingRecord = get(entityType, internalID);
		}

		if (existingRecord != null) {
			this.update(record);
		} else {
			invokeSetter(record, "InternalId", "");
			internalID = this.insert(record);
		}

		return internalID;
	}
	
	public void delete(String entitytype, String id) throws Exception {
		String typeName = TalendComponentGenerator.toInitialLower(Class.forName(entitytype).getSimpleName());

		RecordRef recordRef = new RecordRef();
		recordRef.setType(RecordType.fromValue(typeName));
		recordRef.setInternalId(id);
		
		DeleteRequest deleteRequest = new DeleteRequest();
		deleteRequest.setBaseRef(recordRef);

		DeleteResponse response = getPort().delete(deleteRequest);

		if (!response.getWriteResponse().getStatus().isIsSuccess()) {
			throw new NetsuiteException(getErrorCodes(response.getWriteResponse().getStatus()), getErrorMessages(response.getWriteResponse().getStatus()));
		}
	}
	
	public Record get(String entitytype, String id) throws MalformedURLException, InvalidCredentialsFault, com.netsuite.webservices.platform.ExceededRecordCountFault, com.netsuite.webservices.platform.InvalidSessionFault, ExceededRequestLimitFault, com.netsuite.webservices.platform.ExceededUsageLimitFault, UnexpectedErrorFault, ExceededRequestSizeFault, JAXBException, InvalidVersionFault, InsufficientPermissionFault, InvalidAccountFault {
		String typeName = TalendComponentGenerator.toInitialLower(TalendComponentGenerator.getEntityClass(entitytype).getSimpleName());

		RecordRef ref = new RecordRef();
		ref.setInternalId(id);
		ref.setType(RecordType.fromValue(typeName));
		
		GetRequest getRequest = new GetRequest();
		getRequest.setBaseRef(ref);
		
		GetResponse getResponse = getPort().get(getRequest);

		if (getResponse.getReadResponse().getStatus().isIsSuccess()) {
			return getResponse.getReadResponse().getRecord();
		}

		return null;
	}
	
	public NetSuitePortType getPort() throws JAXBException, UnexpectedErrorFault, ExceededRequestSizeFault, MalformedURLException, InvalidCredentialsFault, InvalidVersionFault, InsufficientPermissionFault, ExceededRequestLimitFault, InvalidAccountFault {

		if (this._port == null) {
			initializeStub();
		}
		return this._port;

	}
	
	public String getStatusDetails(Status status) {
		
		StringBuffer stringBuffer = new StringBuffer();
		List<StatusDetail> statusDetails = status.getStatusDetail();
		
		if (statusDetails != null) {
			for (StatusDetail detail : statusDetails) {
				stringBuffer.append("[Code=" + detail.getCode() + "] " + detail.getMessage() + "\n");
			}
		}
		
		return stringBuffer.toString();
	}
	
	public String getErrorCodes(Status status) {
		
		StringBuffer stringBuffer = new StringBuffer();
		List<StatusDetail> statusDetails = status.getStatusDetail();
		
		if (statusDetails != null) {
			int i = 0;
			for(; i < statusDetails.size() - 1; i++){
				stringBuffer.append(statusDetails.get(i).getCode() + ", ");
			}
			stringBuffer.append(statusDetails.get(i).getCode());
		}
		
		return stringBuffer.toString();
	}
	
	public String getErrorMessages(Status status) {
		
		StringBuffer stringBuffer = new StringBuffer();
		List<StatusDetail> statusDetails = status.getStatusDetail();
		
		if (statusDetails != null) {
			int i = 0;
			for(; i < statusDetails.size() - 1; i++){
				stringBuffer.append(statusDetails.get(i).getMessage() + ", ");
			}
			stringBuffer.append(statusDetails.get(i).getMessage());
		}
		
		return stringBuffer.toString();
	}
	
	public void setPageSize(int pageSize) {
		this._pageSize = pageSize;
	}
	
	public Object invokeGetter(Object target, String field) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Method getter = target.getClass().getMethod("get" + field, new Class[0]);
		return getter.invoke(target, null);
	}
	
	public void invokeSetter(Object target, String field, Object value) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Method setter = target.getClass().getMethod("set" + field, new Class[] { value.getClass() });
		Object[] args = new Object[1];
		args[0] = value;
		setter.invoke(target, args);
	}
	
	private static Method findMethod(Class<?> clazz, String methodName) {
		Method[] methods = clazz.getMethods();
		
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().equals(methodName))
				return methods[i];
		}
		
		return null;
	}
	
	public void setNullFieldValue(NullField fl, String id) {
		List<String> nullFields = fl.getName();
		if(!nullFields.contains(id)){
			nullFields.add(id);
		}
	}

}
