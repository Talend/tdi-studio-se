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
package org.talend.spark.utils;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

/**
 * Company : Altic - LIPN User: Tugdual Sarazin Date: 27/05/14 Time: 16:34
 */
public class SortComparator implements Comparator<List<Object>>, Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private List<CompareCol> compCols;

	public SortComparator(List<CompareCol> compCols) {
		this.compCols = compCols;
	}

	public int compare(List<Object> o1, List<Object> o2) {
		if (o1.size() != o2.size()) {
			return o1.size() - o2.size();
		}
		int cmp;
		for (int i = 0; i < o1.size(); i++) {
			if (this.compCols.get(i).getCompareType() == CompareType.DIGIT) {
				double i1 = Double.parseDouble(o1.get(i).toString());
				double i2 = Double.parseDouble(o2.get(i).toString());
				cmp = i1 > i2 ? 1 : (i1 == i2 ? 0 : -1);// comparing double is
														// not a good idea.
			} else {
				cmp = o1.get(i).toString().compareTo(o2.get(i).toString());
			}

			// o1 != o2
			if (cmp != 0) {
				boolean asc = this.compCols.get(i).isAscending();
				// o1 > o2 && asc || o1 < o2 && desc
				if ((cmp > 0 && asc) || (cmp < 0 && !asc)) {
					return 1;
					// o1 < o2 && asc || o1 > o2 && desc
				} else {
					return -1;
				}
			}
		}
		return 0;
	}
}
