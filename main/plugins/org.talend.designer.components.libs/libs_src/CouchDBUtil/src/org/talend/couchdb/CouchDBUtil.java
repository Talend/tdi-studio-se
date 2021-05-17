package org.talend.couchdb;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.Set;

import org.lightcouch.View;

public class CouchDBUtil {

	public static void initView(View view, Map<String, Object> options)
			throws Exception {

		if (options != null && options.size() > 0) {
			Set<String> params = options.keySet();
			for (String param : params) {
				try {
					if ("key".equals(param)) {
						view.key(options.get(param));
					}
					if ("keys".equals(param)) {
						view.keys((List<String>) options.get(param));
					}
					if ("startkey".equals(param)) {
						view.startKey(options.get(param));
					}
					if ("startkey_docid".equals(param)) {
						view.startKeyDocId((String) options.get(param));
					}
					if ("endkey".equals(param)) {
						view.endKey(options.get(param));
					}
					if ("endkey_docid".equals(param)) {
						view.endKeyDocId((String) options.get(param));
					}
					if ("limit".equals(param)) {
						view.limit((Integer) options.get(param));
					}
					if ("stale".equals(param)) {
						view.stale((String) options.get(param));
					}
					if ("descending".equals(param)) {
						view.descending((Boolean) options.get(param));
					}
					if ("skip".equals(param)) {
						view.skip((Integer) options.get(param));
					}
					if ("group".equals(param)) {
						view.group((Boolean) options.get(param));
					}
					if ("group_level".equals(param)) {
						view.groupLevel((Integer) options.get(param));
					}
					if ("reduce".equals(param)) {
						view.reduce((Boolean) options.get(param));
					}
					if ("include_docs".equals(param)) {
						view.includeDocs((Boolean) options.get(param));
					}
					if ("inclusive_end".equals(param)) {
						view.inclusiveEnd((Boolean) options.get(param));
					}
					if ("update_seq".equals(param)) {
						view.updateSeq((Boolean) options.get(param));
					}
				} catch (ClassCastException e) {
					throw new ClassCastException("Please check parameter: "
							+ param + " value type.");
				}
			}

		}

	}
}
