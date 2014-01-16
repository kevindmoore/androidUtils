package com.mastertechsoftware.sql;

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

	public CRUDHelper(ReflectTable<T> table, BaseDatabaseHelper databaseHelper) {
		this.table = table;
		this.database = databaseHelper.localDatabase;
		this.databaseHelper = databaseHelper;
	}

	/**
	 * Add a new Item
	 *
	 * @param item
	 * @return id
	 */
	public long addItem(T item ) {
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
	public List<T> getItems(Class<T> classItem) {
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
	 * Delete Item
	 * @param id
	 */
	public void deleteItem(long id) {
		// Lock it!
		mLock.lock();
		try {
			databaseHelper.startTransaction();
			int deleted = table.deleteEntryWhere(database, Table.ID, String.valueOf(id));
			Logger.debug("Deleted " + deleted + " kids ");
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
			table.updateEntry(database, item, id, table.getMapper());
		} catch (DBException e) {
			Logger.error(this, "Problems starting transaction");
		} finally {
			databaseHelper.endTransaction();
			mLock.unlock();
		}
	}
}
