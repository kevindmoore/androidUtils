
package com.mastertechsoftware.sql;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.util.List;

/**
 * Class that implements all methods so subclasses can implement only those methods needed.
 */
public class AbstractTable extends Table {

    public static final String TAG = "AbstractTable";

    public AbstractTable() {
    }

    public AbstractTable(String tableName) {
        super(tableName);
    }

    public AbstractTable(List<Column> columns, String tableName) {
        super(columns, tableName);
    }

    @Override
    public Object insertEntry(Database database, Object data) {
        return null;
    }

    /**
     * Insert a new row with the given column data.
     * @param database
     * @param data
     * @return id of new item
     */
    @Override
    public long insertEntry(Database database, List<String> data) {
        ContentValues cv = new ContentValues();
        int columnSize = columns.size();
        int dataSize = data.size();
        if (dataSize > (columnSize-1)) {
            Log.e(TAG, "You cannot insert more data than there are columns");
            return 0;
        }
        // Assume the first column is the id column
        for (int i=0; i < dataSize && i < columnSize; i++) {
            cv.put(columns.get(i+1).getName(), data.get(i));
        }
        return insertEntry(database, cv);
    }

    /**
     * Insert a new row with the given column data.
     * @param database
     * @param data
     * @return id of new item
     */
    @Override
    public long insertEntry(Database database, ContentValues data) {
        long id = 0;
        try {
            id = database.getDatabase().insert(getTableName(), getIdField(), data);
        } catch (SQLiteException e) {
            Log.e(TAG, e.getMessage());
            return id;
        }
        return id;
    }

    /**
     * Delete a single entry with the given id
     * @param database
     * @param data - id to delete
     */
    @Override
    public void deleteEntry(Database database, Object data) {
        String[] whereArgs = new String[1];
        whereArgs[0] = String.valueOf(data);
        try {
            database.getDatabase().delete(getTableName(), getIdField() + "=?", whereArgs);
        } catch (SQLiteException e) {
            Log.e(TAG, e.getMessage());
        }

    }

    /**
     * Delete an entry with the given where clause. Needs to use "?" formats
     * @param database
     * @param whereClause
     * @param whereArgs
     */
    @Override
    public void deleteEntryWhere(Database database, String whereClause, String[] whereArgs) {
        try {
            database.getDatabase().delete(getTableName(), whereClause, whereArgs);
        } catch (SQLiteException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * Delete all entries in this table.
     * @param database
     */
    @Override
    public void deleteAllEntries(Database database) {
        try {
            database.getDatabase().delete(getTableName(), null, null);
        } catch (SQLiteException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * Get a single entry and return the cursor.
     * @param database
     * @param data
     * @return Cursor
     */
    @Override
    public Object getEntry(Database database, Object data) {
        return null;
    }

    /**
     * Generic method to get a table entry
     *
     * @param database
     * @param id
     * @return the cursor for the object
     */
    @Override
    public Cursor getEntry(Database database, long id) {
        Cursor result;
        String[] params = { String.valueOf(id) };
        try {
            result = database.getDatabase().query(getTableName(), getProjection(), getIdField() + "=?",
                    params, null, null, null);
        } catch (SQLiteException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
        return result;
    }

    /**
     * Find an entry where the given column matches the given value.
     * @param database
     * @param columnName
     * @param columnValue
     * @return the cursor for the object
     */
    @Override
    public Cursor getEntry(Database database, String columnName, String columnValue) {
        Cursor result;
        String[] params = { columnValue };
        try {
            result = database.getDatabase().query(getTableName(), getProjection(), columnName + "=?",
                    params, null, null, null);
        } catch (SQLiteException e) {
            Log.e(TAG, e.getMessage());
        return null;
    }
        return result;
    }

    @Override
    public Object updateEntry(Database database, Object data) {
        return null;
    }

    /**
     * Update the table with the given column data. 1st column is the id.
     * @param database
     * @param data
     * @return # of items updated
     */
    @Override
    public int updateEntry(Database database, List<String> data) {
        ContentValues cv = new ContentValues();
        int columnSize = columns.size();
        int dataSize = data.size();
        // Assume the first column is the id column
        for (int i=0; i < dataSize && i < columnSize; i++) {
            cv.put(columns.get(i).getName(), data.get(i));
        }
        return updateEntry(database, cv);
    }

    /**
     * Update the table with the given column data. 1st column is the id.
     * @param database
     * @param data
     * @return # of items updated
     */
    @Override
    public int updateEntry(Database database, ContentValues data) {
        try {
            String[] whereArgs = new String[1];
            whereArgs[0] = data.getAsString(getIdField());
            return database.getDatabase().update(getTableName(), data, getIdField() + "=?", whereArgs);
        } catch (SQLiteException e) {
            Log.e(TAG, e.getMessage());
            return 0;
        }
    }

    /**
     * Update the table with the given content values & where information.
     * @param database
     * @param data
     * @return # of items updated
     */
    @Override
    public int updateEntryWhere(Database database, ContentValues cv, String whereClause,
            String[] whereArgs) {
        try {
            return database.getDatabase().update(getTableName(), cv, whereClause, whereArgs);
        } catch (SQLiteException e) {
            Log.e(TAG, e.getMessage());
        return 0;
    }
    }

    /**
     * Get all entries and return a cursor.
     * @param database
     * @param data
     * @return null
     */
    @Override
    public Object getAllEntries(Database database, Object data) {
        return null;
    }

    /**
     * Return a cursor with all entries
     * @param database
     * @return Cursor
     */
    @Override
    public Cursor getAllEntries(Database database) {
        Cursor result;
        try {
            result = database.getDatabase().query(getTableName(), getProjection(), null, null, null, null,
                    null);
        } catch (SQLiteException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
        if (!result.moveToFirst()) {
            result.close();
        return null;
    }
        return result;
    }
}
