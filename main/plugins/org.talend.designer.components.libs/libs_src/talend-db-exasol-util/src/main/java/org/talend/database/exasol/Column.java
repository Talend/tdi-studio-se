// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.database.exasol;

public class Column implements Comparable<Column>{
	
	private String name;
	private Integer index;
	private String format;
	private Integer size;
	private String padding;
	
	public static Column getCSVColumn(String name, Integer sourceIndex, String format) {
		Column c = new Column();
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("name canot be null or empty");
		}
		c.name = name.toUpperCase();
		if (sourceIndex == null) {
			throw new IllegalArgumentException("sourceIndex cannot be null");
		}
		c.index = sourceIndex;
		if (format != null && format.trim().isEmpty() == false) {
			c.format = format;
		}
		return c;
	}
	
	public static Column getFBVColumn(String name, Integer size, Integer start, String padding) {
		Column c = new Column();
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("name canot be null or empty");
		}
		c.name = name;
		c.size = size;
		c.index = start;
		c.padding = padding;
		return c;
	}

	public String getName() {
		return name;
	}

	public Integer getSourceIndex() {
		return index;
	}

	public String getFormat() {
		return format;
	}

	public Integer getSize() {
		return size;
	}

	public Integer getStart() {
		return index;
	}

	public String getPadding() {
		return padding;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Column) {
			Column c = (Column) o;
			if (c.name != null && name != null) {
				return name.equalsIgnoreCase(c.name);
			} else if (index != null) {
				return index.equals(c.index);
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	@Override
	public int compareTo(Column o) {
		if (index != null && o.index != null) {
			return index.compareTo(o.index);
		} else {
			return 0;
		}
	}
	
}
