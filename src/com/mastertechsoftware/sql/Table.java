package com.mastertechsoftware.sql;

import java.util.ArrayList;
import java.util.List;

/**
 * User: kevin.moore
 */
public class Table {
	protected String tableName;
	protected List<Column> columns = new ArrayList<Column>();

	public Table() {
	}

	public Table(String tableName) {
		this.tableName = tableName;
	}

	public Table(List<Column> columns, String tableName) {
		this.columns = columns;
		this.tableName = tableName;
	}

	public List<Column> getColumns() {
		return columns;
	}

	public void setColumns(List<Column> columns) {
		this.columns = columns;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public void addColumn(Column column) {
		columns.add(column);
	}

	public void removeColumn(Column column) {
		columns.remove(column);
	}

	public String getCreateTableString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CREATE TABLE ").append(tableName).append(" (");
		boolean firstColumn = true;
		for (Column column : columns) {
			if (!firstColumn) {
				builder.append(", ");
			}
			firstColumn = false;
			builder.append(column.getCreateString());
		}
		builder.append(")");
		return builder.toString();

	}
}
