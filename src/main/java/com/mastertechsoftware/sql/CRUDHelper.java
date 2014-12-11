package com.mastertechsoftware.sql;

import android.database.Cursor;

import com.mastertechsoftware.util.log.Logger;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
/**
 * This is a helper class that will do CRUD (Create, read, update, delete) operations
 */
public class CRUDHelper<T> {
	// Lock used to serialize access to this API.
	protected final ReentrantLock mLock = new ReentrantLock();
	protected ReflectTable<T> table;
	protected Database database;
	protected BaseDatabaseHelper databaseHelper;
	protected boolean debugging = false;

	public CRUDHelper(ReflectTable<T> table, BaseDatabaseHelper databaseHelper) {
		Logger.setDebug(ReflectionDBHelper.class.getSimpleName(), debugging);
		this.table = table;
		this.database = databaseHelper.localDatabase;
		this.databaseHelper = databaseHelper;
	}

    /**
     * Get the table that this helper represents
     * @return ReflectTable<T>
     */
    public ReflectTable<T> getTable() {
        return table;
    }

    /**
     * Get the Database that this helper represents
     * @return Database
     */
    public Database getDatabase() {
        return database;
    }

    /**
     * Get the BaseDatabaseHelper that this helper uses
     * @return BaseDatabaseHelper
     */
    public BaseDatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }

    /**
	 * Add a new Item
	 *
	 * @param item
	 * @return id
	 */
	public long addItem(T item) {
		// Lock it!
		mLock.lock();
		try {
			databaseHelper.startTransaction();
            return table.insertEntry(database, item, table.getMapper());
		} catch (DBException e) {
			Logger.error(this, "Problems starting transaction");
		} finally {
			databaseHelper.endTransaction();
			mLock.unlock();
		}
		return -1;
	}

	/**
	 * Get the list of all items
	 * @return List<T>
	 */
	public List<? extends T> getItems(Class<? extends T> classItem) {
		// Lock it!
		mLock.lock();
		try {
			databaseHelper.startTransaction();
			return table.getAllEntries(database, classItem, table.getMapper());
		} catch (DBException e) {
			Logger.error(this, "Problems starting transaction");
		} finally {
			databaseHelper.endTransaction();
			mLock.unlock();
		}
		return null;
	}

    /**
     * Get items with where clause
     * @param classItem
     * @param columnName
     * @param columnValue
     * @return
     */
	public List<? extends T> getItemsWhere(Class<? extends T> classItem, String columnName, String columnValue) {
		// Lock it!
		mLock.lock();
		try {
			databaseHelper.startTransaction();
			return table.getAllEntriesWhere(database, (Class<T>) classItem, columnName, columnValue, table.getMapper());
		} catch (DBException e) {
			Logger.error(this, "Problems starting transaction");
		} finally {
			databaseHelper.endTransaction();
			mLock.unlock();
		}
		return null;
	}

	/**
	 * Get the list of all items
	 * @return List<T>
	 */
	public List<T> getItems(Class<T> classItem, long id) {
		// Lock it!
		mLock.lock();
		try {
			databaseHelper.startTransaction();
			return table.getAllEntriesWhere(database, classItem, Table.ID, String.valueOf(id), table.getMapper());
		} catch (DBException e) {
			Logger.error(this, "Problems starting transaction");
		} finally {
			databaseHelper.endTransaction();
			mLock.unlock();
		}
		return null;
	}

    /**
     * Get a Cursor for the table
     * @return Cursor
     */
    public Cursor getItemCursor() {
        // Lock it!
        mLock.lock();
        try {
            databaseHelper.startTransaction();
            return table.getAllEntries(database);
        } catch (DBException e) {
            Logger.error(this, "Problems starting transaction");
        } finally {
            databaseHelper.endTransaction();
            mLock.unlock();
        }
        return null;

    }
	/**
	 * Delete Item
	 * @param id
	 */
	public void deleteItem(long id) {
		// Lock it!
		mLock.lock();
		try {
			databaseHelper.startTransaction();
			int deleted = table.deleteEntryWhere(database, Table.ID, String.valueOf(id));
			Logger.debug("Deleted " + deleted + " items ");
		} catch (DBException e) {
			Logger.error(this, "Problems starting transaction");
		} finally {
			databaseHelper.endTransaction();
			mLock.unlock();
		}
	}

    /**
     * Delete an item where the column name = the column value
     * @param columnName
     * @param columnValue
     */
    public void deleteItemWhere(String columnName, String columnValue) {
        // Lock it!
        mLock.lock();
        try {
            databaseHelper.startTransaction();
            int deleted = table.deleteEntryWhere(database, columnName, columnValue);
            Logger.debug("Deleted " + deleted + " items ");
        } catch (DBException e) {
            Logger.error(this, "Problems starting transaction");
        } finally {
            databaseHelper.endTransaction();
            mLock.unlock();
        }
    }

    public void deleteAllItems() {
        // Lock it!
        mLock.lock();
        try {
            databaseHelper.startTransaction();
            table.deleteAllEntries(database);
        } catch (DBException e) {
            Logger.error(this, "Problems starting transaction");
        } finally {
            databaseHelper.endTransaction();
            mLock.unlock();
        }

    }

	public void updateItem(T item, long id) {
		// Lock it!
		mLock.lock();
		try {
			databaseHelper.startTransaction();
			int result = table.updateEntry(database, item, id, table.getMapper());
            if (result <= 0) {
                Logger.error("Unable to update table " + table.getTableName());
            }
		} catch (DBException e) {
			Logger.error(this, "Problems starting transaction");
		} finally {
			databaseHelper.endTransaction();
			mLock.unlock();
		}
	}

    /**
     * Return the columns name for our table
     * @return List<String> column names
     */
    public List<String> getColumnNames() {
        return table.getColumnNames();
    }
}