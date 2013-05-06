
package com.mastertechsoftware.sql;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;

import com.mastertechsoftware.util.log.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that implements all methods so subclasses can implement only those methods needed.
 */
public class AbstractTable<T> extends Table<T> {

    public AbstractTable() {
    }

    public AbstractTable(String tableName) {
        super(tableName);
    }

    public AbstractTable(List<Column> columns, String tableName) {
        super(columns, tableName);
    }

    @Override
    public T insertEntry(Database database, T data) {
        return null;
    }

    /**
     * Insert a new entry into the db using a mapper
     * @param database
     * @param data
     * @param mapper
     * @return new id
     */
    public long insertEntry(Database database, T data, DataMapper<T> mapper) {
        int columnPosition = 0;
        ContentValues cv = new ContentValues();
        for (Column column : columns) {
            if (column.column_position == 0) {
                column.column_position = columnPosition;
            }
            mapper.write(cv, column, data);
            columnPosition++;
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
    public long insertEntry(Database database, List<String> data) {
        ContentValues cv = new ContentValues();
        int columnSize = columns.size();
        int dataSize = data.size();
        if (dataSize > (columnSize-1)) {
            Logger.error(this, "You cannot insert more data than there are columns");
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
            Logger.error(this, e.getMessage());
            return id;
        }
        return id;
    }

    /**
     * Delete a single entry with the given id
     * @param database
     * @param key - id to delete
     */
    @Override
    public int deleteEntry(Database database, Object key) {
        String[] whereArgs = new String[1];
        whereArgs[0] = String.valueOf(key);
        try {
            return database.getDatabase().delete(getTableName(), getIdField() + "=?", whereArgs);
        } catch (SQLiteException e) {
            Logger.error(this, e.getMessage());
        }
		return 0;
    }

    /**
     * Delete an entry with the given where clause. Needs to use "?" formats
     * @param database
     * @param whereClause
     * @param whereArgs
     */
    @Override
    public int deleteEntryWhere(Database database, String whereClause, String[] whereArgs) {
        try {
            return database.getDatabase().delete(getTableName(), whereClause, whereArgs);
        } catch (SQLiteException e) {
            Logger.error(this, e.getMessage());
        }
		return 0;
    }

	/**
	 * Delete the entry with the given where column and value
	 * @param database
	 * @param columnName
	 * @param columnValue
	 */
    public int deleteEntryWhere(Database database, String columnName, String columnValue) {
		String[] whereArgs = new String[1];
		whereArgs[0] = columnValue;
        try {
            return database.getDatabase().delete(getTableName(), columnName + "=?" , whereArgs);
        } catch (SQLiteException e) {
            Logger.error(this, e.getMessage());
        }
		return 0;
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
            Logger.error(this, e.getMessage());
        }
    }

    /**
     * Get a single entry and return the cursor.
     * @param database
     * @param key
     * @return Cursor
     */
    @Override
    public T getEntry(Database database, Object key) {
        return null;
    }

    /**
     * Get a single entry and return the object using a mapper.
     * @param database
     * @param key
     * @param mapper
     * @return T
     */
    public T getEntry(Database database, Object key, T data, DataMapper<T> mapper) {
        Cursor cursor = null;
        String[] params = { String.valueOf(key) };
        try {
            cursor = database.getDatabase().query(getTableName(), getProjection(), getIdField() + "=?",
                    params, null, null, null);
            if (cursor == null) {
                return null;
            }
            if (!cursor.moveToNext()) {
                cursor.close();
                return null;
            }
            int columnPosition = 0;
            for (Column column : columns) {
                if (column.column_position == 0) {
                    column.column_position = columnPosition;
                }
                mapper.read(cursor, column, data);
                columnPosition++;
            }
            return data;
        } catch (SQLiteException e) {
            Logger.error(this, e.getMessage());
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Get a single entry and return the object using a mapper based on the column search.
     * @param database
     * @param data
     * @param columnName
     * @param columnValue
     * @param mapper
     * @return T
     */
    public T getEntry(Database database, T data, String columnName, String columnValue, DataMapper<T> mapper) {
        Cursor cursor = null;
        String[] params = { String.valueOf(columnValue) };
        try {
            cursor = database.getDatabase().query(getTableName(), getProjection(), columnName + "=?",
                    params, null, null, null);
            if (cursor == null) {
                return null;
            }
            if (!cursor.moveToNext()) {
                cursor.close();
                return null;
            }
            int columnPosition = 0;
            for (Column column : columns) {
                if (column.column_position == 0) {
                    column.column_position = columnPosition;
                }
                mapper.read(cursor, column, data);
                columnPosition++;
            }
            return data;
        } catch (SQLiteException e) {
            Logger.error(this, e.getMessage());
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
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
            Logger.error(this, e.getMessage());
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
            Logger.error(this, e.getMessage());
        return null;
    }
        return result;
    }

    @Override
    public T updateEntry(Database database, T data, Object key) {
        return null;
    }

    /**
     * Update the table with the given column data.
     * @param database
     * @param data
     * @param key
     * @return # of items updated
     */
    @Override
    public int updateEntry(Database database, List<String> data, Object key) {
        ContentValues cv = new ContentValues();
        int columnSize = columns.size();
        int dataSize = data.size();
        // Assume the first column is the id column
        for (int i=0; i < dataSize && i < columnSize; i++) {
            cv.put(columns.get(i).getName(), data.get(i));
        }
        return updateEntry(database, cv, key);
    }

    /**
     * Update the table with the given column data. 1st column is the id.
     * @param database
     * @param data
     * @param key
     * @return # of items updated
     */
    @Override
    public int updateEntry(Database database, ContentValues data, Object key) {
        try {
            String[] whereArgs = {String.valueOf(key)};
            return database.getDatabase().update(getTableName(), data, getIdField() + "=?", whereArgs);
        } catch (SQLiteException e) {
            Logger.error(this, e.getMessage());
            return 0;
        }
    }

    /**
     * Update the table with the given key using a mapper.
     * @param database
     * @param data
     * @param key
     * @param mapper
     * @return # of items updated
     */
    public int updateEntry(Database database, T data, Object key, DataMapper<T> mapper) {
        try {
            String[] whereArgs = {String.valueOf(key)};
            int columnPosition = 0;
            ContentValues cv = new ContentValues();
            for (Column column : columns) {
                if (column.column_position == 0) {
                    column.column_position = columnPosition;
                }
                mapper.write(cv, column, data);
                columnPosition++;
            }
			Logger.debug(this, "updateEntry for table " + getTableName() + " id: " + getIdField() + " with where args: " + whereArgs[0]);
            return database.getDatabase().update(getTableName(), cv, getIdField() + "=?", whereArgs);
        } catch (SQLiteException e) {
            Logger.error(this, e.getMessage());
            return 0;
        }
    }

    /**
     * Update the table with the given key using a mapper.
     * @param database
     * @param data
     * @param columnName
     * @param columnValue
     * @param mapper
     * @return # of items updated
     */
    public int updateEntry(Database database, T data, String columnName, String columnValue, DataMapper<T> mapper) {
        try {
            String[] whereArgs = {columnValue};
            int columnPosition = 0;
            ContentValues cv = new ContentValues();
            for (Column column : columns) {
                if (column.column_position == 0) {
                    column.column_position = columnPosition;
                }
                mapper.write(cv, column, data);
                columnPosition++;
            }
            return database.getDatabase().update(getTableName(), cv, columnName + "=?", whereArgs);
        } catch (SQLiteException e) {
            Logger.error(this, e.getMessage());
            return 0;
        }
    }

    /**
    * Update the table with the given content values & where information.
    * @param database
    * @param cv
    * @param whereClause
    * @param whereArgs
    * @return # of items updated
    */
    @Override
    public int updateEntryWhere(Database database, ContentValues cv, String whereClause,
            String[] whereArgs) {
        try {
            return database.getDatabase().update(getTableName(), cv, whereClause, whereArgs);
        } catch (SQLiteException e) {
            Logger.error(this, e.getMessage());
        return 0;
    }
    }

    /**
     * Get all entries and return a list of items.
     * @param database
     * @param cls
     * @param mapper
     * @return List<T>
     */
    public List<T> getAllEntries(Database database, Class<T> cls, DataMapper<T> mapper) {
        Cursor cursor = null;
        List<T> dataList = new ArrayList<T>();
        try {
            cursor = database.getDatabase().query(getTableName(), getProjection(), null, null, null,
                    null, null);
            if (cursor == null) {
                return dataList;
            }
            if (!cursor.moveToFirst()) {
                cursor.close();
                return dataList;
            }
            do  {
                int columnPosition = 0;
                T data = cls.newInstance();
                for (Column column : columns) {
                    if (column.column_position == 0) {
                        column.column_position = columnPosition;
                    }
                    mapper.read(cursor, column, data);
                    columnPosition++;
                }
                dataList.add(data);
            } while (cursor.moveToNext());
        } catch (SQLiteException e) {
            Logger.error(this, e.getMessage());
        } catch (InstantiationException e) {
            Logger.error(this, e.getMessage());
        } catch (IllegalAccessException e) {
            Logger.error(this, e.getMessage());
		} finally {
			if (cursor != null) {
				cursor.close();
			}
        }
        return dataList;
    }

    /**
     * Get all the entries that match the given column value.
     * @param database
     * @param cls
     * @param columnName
     * @param columnValue
     * @param mapper
     * @return List<T>
     */
    public List<T> getAllEntriesWhere(Database database, Class<T> cls, String columnName, String columnValue, DataMapper<T> mapper) {
        Cursor cursor = null;
        List<T> dataList = new ArrayList<T>();
        String[] params = { String.valueOf(columnValue) };
        try {
            cursor = database.getDatabase().query(getTableName(), getProjection(), columnName + "=?",
                    params, null,
                    null, null);
            if (cursor == null) {
                return dataList;
            }
            if (!cursor.moveToFirst()) {
                cursor.close();
                return dataList;
            }
            do  {
                int columnPosition = 0;
                T data = cls.newInstance();
                for (Column column : columns) {
                    if (column.column_position == 0) {
                        column.column_position = columnPosition;
                    }
                    mapper.read(cursor, column, data);
                    columnPosition++;
                }
                dataList.add(data);
            } while (cursor.moveToNext());
        } catch (SQLiteException e) {
            Logger.error(this, e.getMessage());
        } catch (InstantiationException e) {
            Logger.error(this, e.getMessage());
        } catch (IllegalAccessException e) {
            Logger.error(this, e.getMessage());
		} finally {
			if (cursor != null) {
				cursor.close();
			}
        }
        return dataList;
    }

    /**
     * Return a cursor with all entries
     * @param database
     * @return Cursor
     */
    @Override
    public Cursor getAllEntries(Database database) {
        Cursor cursor;
        try {
            cursor = database.getDatabase().query(getTableName(), getProjection(), null, null, null,
                    null, null);
        } catch (SQLiteException e) {
            Logger.error(this, e.getMessage());
            return null;
        }
        if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }

    /**
     * Get all entries and return a list of items.
     * @param database
     * @param cls
     * @return List<T>
     */
    public List<T> getAllEntries(Database database, Class<T> cls) {
        return null;
    }
}
