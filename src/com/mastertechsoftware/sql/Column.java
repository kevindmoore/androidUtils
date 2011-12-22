package com.mastertechsoftware.sql;

/**
 * User: kevin.moore
 */
public class Column {
	public enum COLUMN_TYPE {
		INTEGER,
		TEXT

	}
	protected String name;
	protected COLUMN_TYPE type;
	protected boolean key = false;
	protected int column_position;

	public Column() {
	}

	public Column(String name, COLUMN_TYPE type) {
		this.name = name;
		this.type = type;
	}

	public Column(String name, COLUMN_TYPE type, boolean key) {
		this.key = key;
		this.name = name;
		this.type = type;
	}

	public boolean isKey() {
		return key;
	}

	public void setKey(boolean key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public COLUMN_TYPE getType() {
		return type;
	}

	public void setType(COLUMN_TYPE type) {
		this.type = type;
	}

	public String getCreateString() {
		StringBuilder builder = new StringBuilder();
		builder.append(name).append(" ").append(type.toString()).append(" ");
		if (key) {
			builder.append(" PRIMARY KEY AUTOINCREMENT ");
		}
		return builder.toString();

	}

	public int getColumn_position() {
		return column_position;
	}

	public void setColumn_position(int column_position) {
		this.column_position = column_position;
	}
}
