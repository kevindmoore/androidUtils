package com.mastertechsoftware.sql;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

/**
 * Class used to describe a SQL Table. Holds the columns, table name & projection.
 */
public abstract class Table {
    public static final String ID = "_id";
	protected String tableName;
    protected String idField = ID;
	protected List<Column> columns = new ArrayList<Column>();
	protected String[] projection;
    protected int version = 1;

  /**
   * Constructor
   */
	public Table() {
	}

  /**
   * Constructor
   * @param tableName
   */
	public Table(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * Create a table with the given columns and table name
	 * @param columns
	 * @param tableName
	 */
	public Table(List<Column> columns, String tableName) {
		this.columns = columns;
		this.tableName = tableName;
	}

	/**
	 * Get the list of columns for this table
	 * @return list of columns
	 */
	public List<Column> getColumns() {
		return columns;
	}

	/**
	 * Replace the current columns with this list
	 * @param columns
	 */
	public void setColumns(List<Column> columns) {
		this.columns = columns;
	}

	/**
	 * Get table name
	 * @return name
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * set the table name
	 * @param tableName
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * Add a new column
	 * @param column
	 */
	public void addColumn(Column column) {
		columns.add(column);
	}

	/**
	 * Remove a column
	 * @param column
	 */
	public void removeColumn(Column column) {
		columns.remove(column);
	}

	/**
	 * Create a string to create a new table.
	 * @return sql string
	 */
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

	/**
	 * Get the projection needed for sql queries
	 * @return Projection
	 */
	public String[] getProjection() {
		if (projection == null) {
			projection = new String[columns.size()];
			int count = 0;
			for (Column column : columns) {
				projection[count++] = column.getName();
			}
		}
		return projection;
	}

	/**
	 * Generic method to insert a table entry
	 *
	 * @param database
	 * @param data
	 * @return the object created
	 */
	public abstract Object insertEntry(Database database, Object data);

	/**
     * Generic method to insert a table entry
     * @param database
     * @param data
     * @return the id of the object created
     */
    public abstract long insertEntry(Database database, List<String> data);

    /**
     * Generic method to insert a table entry
     * @param database
     * @param data
     * @return the id of the object created
     */
    public abstract long insertEntry(Database database, ContentValues data);

    /**
	 * Generic method to delete a table entry
	 *
	 * @param database
	 * @param data
	 */
	public abstract void deleteEntry(Database database, Object data);

	/**
	 * Delete the entry with the given where clause and values
	 * @param database
	 * @param whereClause
	 * @param whereArgs
	 */
	public abstract void deleteEntryWhere(Database database, String whereClause, String[] whereArgs);

	/**
	 * Delete all table entries.
	 * @param database
	 */
	public abstract void deleteAllEntries(Database database);

	/**
	 * Generic method to get a table entry
	 *
	 * @param database
	 * @param data
     * @return the object found
	 */
	public abstract Object getEntry(Database database, Object data);

	/**
     * Generic method to get a table entry
     *
     * @param database
     * @param id
     * @return the cursor for the object
     */
    public abstract Cursor getEntry(Database database, long id);

    /**
     * Find an entry where the given column matches the given value.
     * @param database
     * @param columnName
     * @param columnValue
     * @return the cursor for the object
     */
    public abstract Cursor getEntry(Database database, String columnName, String columnValue);

    /**
	 * Generic method to update a table entry
	 *
	 * @param database
	 * @param data
     * @return the object updated
	 */
	public abstract Object updateEntry(Database database, Object data);

	/**
     * Generic method to update a table entry
     * @param database
     * @param data
     * @return the number of objects updated
     */
    public abstract int updateEntry(Database database, List<String> data);

    /**
     * Generic method to update a table entry
     * @param database
     * @param data
     * @return the number of objects updated
     */
    public abstract int updateEntry(Database database, ContentValues data);

    /**
	 * Update the entry with the given where clause and values
	 * @param database
	 * @param cv
	 * @param whereClause
	 * @param whereArgs
	 * @return number of items updated
	 */
	public abstract int updateEntryWhere(Database database, ContentValues cv, String whereClause, String[] whereArgs);

	/**
	 * Return the string identifying the id field. Usually _id
     * 
	 * @return field string
	 */
    public String getIdField() {
        return idField;
    }

    /**
     * Set the id field for this table. Usually "_id"
     * @param idField
     */
    public void setIdField(String idField) {
        this.idField = idField;
    }

	/**
	 * Generic method to get all table entries
	 *
	 * @param database
	 * @param data
     * @return all entries
	 */
	public abstract Object getAllEntries(Database database, Object data);

    /**
     * Return a cursor with all entries
     * @param database
     * @return Cursor
     */
    public abstract Cursor getAllEntries(Database database);

	/**
	 * Helper method to create a set of content values. can string together calls.
	 * @param cv
	 * @param field
	 * @param value
	 * @return ContentValues
	 */
	public ContentValues addContentValue(ContentValues cv, String field, String value) {
		if (cv == null) {
			cv = new ContentValues();
		}
		cv.put(field, value);
		return cv;
	}

    /**
     * Return the version # for this table
     * 
     * @return version #
     */
    public int getVersion() {
        return version;
    }

	/**
     * Set the version # for this table
     */
    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * Helper method to create a set of content values. can string together
     * calls.
	 */
	public static class ContentValueBuilder {
		ContentValues contentValues;

		/**
		 * Helper method to create a set of content values. can string together calls.
		 * @param field
		 * @param value
		 * @return ContentValueBuilder
		 */
		public ContentValueBuilder addContentValue(String field, String value) {
			if (contentValues == null) {
				contentValues = new ContentValues();
			}
			contentValues.put(field, value);
			return this;
		}

		/**
		 * Call this last to get the content value list
		 * @return
		 */
		public ContentValues build() {
			return contentValues;
		}
	}
}
