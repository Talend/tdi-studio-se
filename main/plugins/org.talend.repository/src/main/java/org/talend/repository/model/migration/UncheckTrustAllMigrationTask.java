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
  package org.talend.repository.model.migration;

  import java.util.Arrays;
  import java.util.Date;
  import java.util.GregorianCalendar;
  import org.talend.commons.exception.PersistenceException;
  import org.talend.commons.ui.runtime.exception.ExceptionHandler;
  import org.talend.core.language.ECodeLanguage;
  import org.talend.core.model.components.ComponentUtilities;
  import org.talend.core.model.components.ModifyComponentsAction;
  import org.talend.core.model.components.conversions.IComponentConversion;
  import org.talend.core.model.components.filters.IComponentFilter;
  import org.talend.core.model.components.filters.NameComponentFilter;
  import org.talend.core.model.migration.AbstractJobMigrationTask;
  import org.talend.core.model.properties.Item;
  import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
  import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
  import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

  /**
   * DOC Administrator class global comment. Detailled comment
   */
  @SuppressWarnings("deprecation")
  public class UncheckTrustAllMigrationTask extends AbstractJobMigrationTask {

  	@Override
  	public ExecutionResult execute(Item item) {
  		ProcessType processType = getProcessType(item);
  		if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
  			return ExecutionResult.NOTHING_TO_DO;
  		}

  		IComponentFilter filter = new NameComponentFilter("tFileFetch"); //$NON-NLS-1$
  		try {
  			ModifyComponentsAction.searchAndModify(item, processType, filter,
  					Arrays.<IComponentConversion>asList(new IComponentConversion() {

  						public void transform(NodeType node) {
  							ElementParameterType trustAll = ComponentUtilities.getNodeProperty(node,
  									"TRUST_ALL_SERVER"); //$NON-NLS-1$
  							if(trustAll==null){
  							    ComponentUtilities.addNodeProperty(node, "TRUST_ALL_SERVER", "CHECK");
  							    trustAll = ComponentUtilities.getNodeProperty(node, "TRUST_ALL_SERVER");
  							    trustAll.setValue("true");
  							}
  						}
  					}));
  		} catch (PersistenceException e) {
  			ExceptionHandler.process(e);
  			return ExecutionResult.FAILURE;
  		}

  		return ExecutionResult.SUCCESS_WITH_ALERT;

  	}

  	public Date getOrder() {
  		GregorianCalendar gc = new GregorianCalendar(2020, 9, 12, 17, 0, 0);
  		return gc.getTime();
  	}

  }