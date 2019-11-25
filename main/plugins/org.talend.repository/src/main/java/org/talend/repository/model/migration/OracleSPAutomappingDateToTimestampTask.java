package org.talend.repository.model.migration;

import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.*;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryService;
import org.talend.repository.model.migration.utils.EmfUtils;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OracleSPAutomappingDateToTimestampTask extends AbstractJobMigrationTask {

    private static final IComponentFilter filter = new NameComponentFilter("tOracleSP");

    private static final List<IComponentConversion> actions = Collections.<IComponentConversion>singletonList(new OracleSPAutomappingDateToTimestampTask.AutomappingConverter());

    @Override
    public ExecutionResult execute(Item item) {

        try {
            ProcessType processType = getProcessType(item);
            if (processType == null) {
                return ExecutionResult.NOTHING_TO_DO;
            }
            ModifyComponentsAction.searchAndModify(item, processType, OracleSPAutomappingDateToTimestampTask.filter, OracleSPAutomappingDateToTimestampTask.actions);
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }


    public Date getOrder() {
        return new GregorianCalendar(2019, 10, 15).getTime();
    }

    private void save(Item item) throws PersistenceException {
        IRepositoryService service = GlobalServiceRegister.getDefault().getService(IRepositoryService.class);
        IProxyRepositoryFactory factory = service.getProxyRepositoryFactory();
        factory.save(item, true);
    }

    private static class AutomappingConverter implements IComponentConversion {

        @Override
        public void transform(NodeType node) {

            // Date column in input schema.
            Stream<MetadataType> metas = EmfUtils.findAll(node.getMetadata(), MetadataType.class, (MetadataType m) -> "FLOW".equals(m.getConnector()));
            final Map<String, ColumnType> dateColumns = metas.findFirst()
                    .map(this::findColumnDate)
                    .orElseGet(Stream::empty)
                    .collect(Collectors.toMap(ColumnType::getName, Function.identity()));

            // Stock proc arguments.
            final List<TableColumn> cols = EmfUtils.findAll(node.getElementParameter(), ElementParameterType.class, (ElementParameterType e) -> "SP_ARGS".equals(e.getName()))
                    .findFirst() // SP_ARGS parameter.
                    .map(ElementParameterType::getElementValue)
                    .map(this::toTableCol)
                    .orElseGet(Collections::emptyList);

            Boolean trasformDone = cols.stream()
                    .map((TableColumn t) -> t.automappingModify(dateColumns.keySet()::contains))
                    .reduce(Boolean::logicalOr)
                    .orElse(Boolean.FALSE);
        }

        private Stream<ColumnType> findColumnDate(MetadataType meta) {
            return EmfUtils.findAll(meta.getColumn(),
                    ColumnType.class,
                    (ColumnType c) -> "id_Date".equals(c.getType()));
        }

        /**
         * Liste of value for table
         *       <elementValue elementRef="COLUMN" value="newColumn"/>
         *       <elementValue elementRef="TYPE" value="INOUT"/>
         *       <elementValue elementRef="DBTYPE" value="DATE"/>
         *       <elementValue elementRef="ISCUSTOME" value="false"/>
         *       <elementValue elementRef="CUSTOME_TYPE" value="STRUCT"/>
         *       <elementValue elementRef="CUSTOMENAME" value=""/>
         *       <elementValue elementRef="COLUMN" value="newColumn1"/>
         *       <elementValue elementRef="TYPE" value="INOUT"/>
         * to list of TableColumn type.
         * @param valueListe : liste of ElementValue.
         * @return liste.
         */
        private List<TableColumn> toTableCol(EList valueListe) {
            List<TableColumn> cols = new ArrayList<>();
            String name = null;
            String type = null;
            ElementValueType dbType = null;
            for (Object obj : valueListe) {
                ElementValueType el = (ElementValueType) obj;
                if ("COLUMN".equals(el.getElementRef())) {
                    if (name != null) {
                        OracleSPAutomappingDateToTimestampTask.TableColumn c = new OracleSPAutomappingDateToTimestampTask.TableColumn(name, type, dbType);
                        cols.add(c);
                    }
                    name = el.getValue();
                }
                else if ("TYPE".equals(el.getElementRef())) {
                    type = el.getValue();
                }
                else if ("DBTYPE".equals(el.getElementRef())) {
                    dbType = el;
                }
            }
            cols.add(new TableColumn(name, type, dbType));
            return cols;
        }
    }


    /**
     * To represent column from table for tOracleSP components of .item jobs file.
     *  <elementParameter field="TABLE" name="SP_ARGS">
     *       <elementValue elementRef="COLUMN" value="newColumn"/>
     *       <elementValue elementRef="TYPE" value="INOUT"/>
     *       <elementValue elementRef="DBTYPE" value="DATE"/>
     *       <elementValue elementRef="ISCUSTOME" value="false"/>
     *       <elementValue elementRef="CUSTOME_TYPE" value="STRUCT"/>
     *       ...
     */
    private static class TableColumn {
        private final String name; // elementRef="COLUMN"

        private final String type; // elementRef="TYPE"

        private final ElementValueType dbtype; // elementRef="DBTYPE"

        public TableColumn(String name, String type, ElementValueType dbtype) {
            this.name = name;
            this.type = type;
            this.dbtype = dbtype;
        }

        public boolean automappingModify(Predicate<String> nameOK) {
            boolean ret = nameOK.test(this.name) && this.isIN() && this.isAutomapping();
            if (ret) {
                this.dbtype.setValue("TIMESTAMP");
            }
            return ret;
        }

        private boolean isIN() {
            return this.type != null && this.type.contains("IN"); // in or inout.
        }

        private boolean isAutomapping() {
            return "AUTOMAPPING".equals(dbtype.getValue());
        }

    }
}
