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
package org.talend.database.exasol.imp;
/**
 * This class describes a column within the IMPORT statement.
 * @author Jan Lolling, jan.lolling@cimt-ag.de
 */
public class Column implements Comparable<Column>{

	private String name;
	private Integer index;
	private String format;
	private Integer size;
	private String padding;

	/**
	 * Builds a CSV column definition
	 * @param name column name in the target table
	 * @param sourceIndex source index, can be null to use the next useful index
	 * @param format the format according to the documentation.
	 * @return a Column object.
	 */
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

	/**
	 * Builds a DBMS column definition
	 * @param name column name in the target table
	 * @param sourceIndex source index, can be null to use the next useful index
	 * @return a Column object.
	 */
	public static Column getDbmsColumn(String name, Integer sourceIndex) {
		Column c = new Column();
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("name canot be null or empty");
		}
		c.name = name.trim();
		if (sourceIndex == null) {
			throw new IllegalArgumentException("sourceIndex cannot be null");
		}
		c.index = sourceIndex;
		return c;
	}

	/**
	 * Creates a column for the Fixed Block File format
	 * @param name name of the column in the target table
	 * @param size size of the column in the source
	 * @param start start index within the source row
	 * @param padding string around the actual value
	 * @return a Column
	 */
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
