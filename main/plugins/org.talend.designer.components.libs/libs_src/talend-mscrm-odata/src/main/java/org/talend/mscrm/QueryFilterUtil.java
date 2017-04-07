package org.talend.mscrm;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.client.api.uri.FilterFactory;
import org.apache.olingo.client.api.uri.URIFilter;
import org.apache.olingo.client.core.uri.FilterFactoryImpl;

public class QueryFilterUtil {

    public static String getURIFilterString(LogicalOperator logicalOperator, List<String> fields,
            List<ConditionOperator> conditionOperators, List<Object> values) {
        if (fields == null || conditionOperators == null || values == null || fields.size() != conditionOperators.size()
                || fields.size() != values.size()) {
            throw new IllegalArgumentException(
                    "Please make sure list fields/conditionOperators/values not null and their size are same.");
        }
        FilterFactory filterFactory = new FilterFactoryImpl();
        List<URIFilter> uriFilters = new ArrayList<URIFilter>();
        for (int i = 0; i < fields.size(); i++) {
            switch (conditionOperators.get(i)) {
            case Equal:
                uriFilters.add(filterFactory.eq(fields.get(i), values.get(i)));
                break;
            case NotEqual:
                uriFilters.add(filterFactory.ne(fields.get(i), values.get(i)));
                break;
            case GreaterThan:
                uriFilters.add(filterFactory.gt(fields.get(i), values.get(i)));
                break;
            case GreaterEqual:
                uriFilters.add(filterFactory.ge(fields.get(i), values.get(i)));
                break;
            case LessThan:
                uriFilters.add(filterFactory.lt(fields.get(i), values.get(i)));
                break;
            case LessEqual:
                uriFilters.add(filterFactory.le(fields.get(i), values.get(i)));
                break;
            default:
                throw new IllegalArgumentException("Usupported condition operator:" + conditionOperators.get(i));
            }

        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < uriFilters.size(); i++) {
            if (i != 0) {
                sb.append(" " + logicalOperator.toString().toLowerCase() + " ");
            }
            sb.append(uriFilters.get(i).build());
        }
        return sb.toString();
    }

}
