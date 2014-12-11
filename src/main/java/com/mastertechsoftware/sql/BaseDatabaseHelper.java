package com.mastertechsoftware.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.mastertechsoftware.sql.upgrade.UpgradeHolder;
import com.mastertechsoftware.sql.upgrade.UpgradeStrategy;
import com.mastertechsoftware.sql.upgrade.UpgradeTable;
import com.mastertechsoftware.util.log.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base database helper. Should have most of the functionality of a helper. The Database name & version are required
 */
public class BaseDatabaseHelper extends SQLiteOpenHelper {
    static Pattern tablePattern = Pattern.compile("CREATE TABLE IF NOT EXISTS ([^ ]*) \\(([^\\)]*)\\)", Pattern.DOTALL);
    static Pattern columnPattern = Pattern.compile(" *([^ ]*) ([^ ]*)", Pattern.DOTALL);

    protected enum STATE {
        INITIALIZING,
		CREATED,
		CLOSED,
		OPENING,
		OPEN,
		ERROR
	}

	protected SQLiteDatabase sqLiteDatabase;
	protected Database localDatabase;
	protected MetaDatabase metaDatabase;
	protected Context context;
	protected STATE state = STATE.INITIALIZING;
	protected String mainTableName;
	// NOTE: Override this. This must be set before constructor called
	protected int version = 1;

	// Lock used to serialize access to this API.
	protected final ReentrantLock mLock = new ReentrantLock();
	protected static int openCount = 0;
	// The ExecutorService we use to run requests.
	protected final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    protected UpgradeStrategy upgradeStrategy;
    protected static boolean upgradeCheck = false;

	/**
	 * Create a helper object to create, open, and/or manage a database. This method always returns very quickly. The database is not
	 * actually created or opened until one of {@link #getWritableDatabase} or {@link #getReadableDatabase} is called.
	 *
	 * @param context to use to open or create the database
	 */
	public BaseDatabaseHelper(Context context, String databaseName, String mainTableName, int version) {
		super(context, databaseName, null, version);
		this.context = context;
		this.mainTableName = mainTableName;
		this.version = version;
    }

    /**
     * Check to see if we need to upgrade. Getting the database will start the upgrade process
     */
    private void checkUpgrade() {
        if (!upgradeCheck) {
            upgradeCheck = true;
            state = STATE.INITIALIZING;
            sqLiteDatabase = getWritableDatabase(); // This will trigger an onUpgrade if needed
            state = STATE.OPEN;
            // Reset state
            close();
            sqLiteDatabase = null;
        }
    }

    /**
     * Set The Upgrade strategy
     * @param upgradeStrategy
     */
    public void setUpgradeStrategy(UpgradeStrategy upgradeStrategy) {
        this.upgradeStrategy = upgradeStrategy;
    }

    /**
	 * Check to see if the table exists. If not, create the database.
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {

		sqLiteDatabase = db;

        // Create our Databases
        setupDatabases();

        //        Logger.debug(this, "onCreate");
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + mainTableName + "'", null);

		try {
			if (cursor == null || cursor.getCount() == 0) {
				//                Logger.debug(this, "Creating DB");
				localDatabase.createDatabase();
				state = STATE.CREATED;
			}
		} catch (SQLiteException e) {
			Logger.error(this, e.getMessage());
		} finally {
            if (cursor != null) {
                cursor.close();
            }
		}
	}

    /**
     * Setup our databases
     */
    protected void setupDatabases() {
        // Create our Databases
        createLocalDB();

        setupMetaDatabase();

    }

    /**
     * Check if the Meta database exists. Create if necessary
     */
    protected void setupMetaDatabase() {
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='meta'", null);
        try {
            if (metaDatabase == null) {
                metaDatabase = new MetaDatabase(sqLiteDatabase);
            } else {
                metaDatabase.setDatabase(sqLiteDatabase);
            }
            Logger.debug(this, "metaDatabase creation screen " + metaDatabase.getCreateDatabaseString());
            if (cursor == null || cursor.getCount() == 0) {
                //                Logger.debug(this, "Creating DB");
                metaDatabase.createDatabase();
                addDatabaseToMeta();
            }
        } catch (SQLiteException e) {
            Logger.error(this, e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Add Database data to meta table if necessary
     */
    protected void addDatabaseToMeta() {
        // Lock it!
        mLock.lock();
        try {
            if (!metaDatabase.databaseExists(version, getDatabaseName())) {
                metaDatabase.addDatabaseEntry(version, getDatabaseName(), localDatabase.getCreateDatabaseString());
            }
        } catch (Exception e) {
            Logger.error(this, "Problems Adding Meta data", e);
            return;
        } finally {
            mLock.unlock();
        }
    }

    /**
	 * Is the database open
	 *
	 * @return true if open
	 */
	protected boolean isOpen() {
		return (state == STATE.OPEN);
	}

	/**
	 * Call to open database.
	 */
	public synchronized void open() throws DBException {
        // Already open
		if (state == STATE.OPEN || state == STATE.OPENING) {
			return;
		}
        if (sqLiteDatabase != null) {
            return;
        }
		// Lock it!
		mLock.lock();

		// Wrap the whole thing so we can make sure to unlock in
		// case something throws.
		try {
			//            Logger.debug(this, "Opening DB");
			STATE oldState = state;
			state = STATE.OPENING;

			sqLiteDatabase = getWritableDatabase();

			state = oldState;
			// Make sure the tables exist
			if (state == STATE.INITIALIZING) {
				//                Logger.debug(this, "State is new, creating");
                setupDatabases();
			} else {
				//                Logger.debug(this, "State is " + state);
				createLocalDB();
			}
			state = STATE.OPEN;
		} catch (SQLiteException e) {
			Logger.error(this, "Problems opening database " + mainTableName, e);
			state = STATE.ERROR;
			throw new DBException("Problems opening database " + mainTableName, e);
		} catch (IllegalStateException e) {
			Logger.error(this, "Problems opening database " + mainTableName, e);
			state = STATE.ERROR;
			throw new DBException("Problems opening database " + mainTableName, e);
		} finally {
			mLock.unlock();
		}
	}

	/**
	 * Close the database. This call is very important. Need to call after finished using class. Don't keep open.
	 */
	@Override
	public synchronized void close() {
		// Already closed
		if (state == STATE.CLOSED) {
			return;
		}
		// Lock it!
		mLock.lock();

		// Wrap the whole thing so we can make sure to unlock in
		// case something throws.
		try {
			//            Logger.debug(this, "Closing DB");
			if (sqLiteDatabase != null && state != STATE.INITIALIZING) {
				super.close();
			}
		} finally {
            if (sqLiteDatabase != null && state != STATE.INITIALIZING) {
                sqLiteDatabase = null;
                state = STATE.CLOSED;
            }
			mLock.unlock();
		}
	}

	/**
	 * Start a transaction by incrementing the open count and opening the db if necessary Make multiple db class by calling this method
	 * yourself before other methods
	 */
	public void startTransaction() throws DBException {
        // Check to see if we need to upgrade first
        checkUpgrade();

		openCount++;
		open();
	}

	/**
	 * End a set of transactions be decreasing the open count and closing the db if necessary
	 */
	public void endTransaction() {
		openCount--;
		openCount = Math.max(0, openCount); // Make sure we don't go below 0
		if (openCount == 0) {
			close();
		}
	}

	/**
	 * Create our database objects with the current SQLite db. You will override this to create the database object with your own.
	 */
	protected void createLocalDB() {
		if (localDatabase == null) {
			localDatabase = new Database(sqLiteDatabase);
		} else {
			localDatabase.setDatabase(sqLiteDatabase);
		}
	}

    /**
     * Get the meta database. Describes the current database versions
     * @return metaDatabase
     */
    protected MetaDatabase getMetaDatabase() {
        if (metaDatabase == null || localDatabase == null) {
            // Lock it!
            mLock.lock();
            try {
                startTransaction();
                open();
            } catch (DBException e) {
                Logger.error(this, "Problems opening database", e);
            } finally {
                endTransaction();
                mLock.unlock();
            }
        }
        return metaDatabase;
    }

    /**
	 * If we already have a database, set it here
	 * @param localDatabase
	 */
	public void setLocalDatabase(Database localDatabase) {
		this.localDatabase = localDatabase;
	}

    /**
     * Get the local Database
     * @return Database
     */
    public Database getLocalDatabase() {
        return localDatabase;
    }

    /**
	 * Current Version Number
	 *
	 * @return version
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * Delete the database.
	 */
	public void dropDatabase() throws DBException {
		// Lock it!
		mLock.lock();
		try {
			startTransaction();
			open();
			localDatabase.dropDatabase();
            openCount = 1; // Make sure we close
		} finally {
			endTransaction();
			mLock.unlock();
		}
		state = STATE.INITIALIZING;
	}

	/**
	 * If the database version changes, migrate the data to the new scheme
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		sqLiteDatabase = db;
        setupMetaDatabase();
		createLocalDB();
		if (oldVersion != newVersion) {
			try {
                addDatabaseToMeta();
                if (upgradeStrategy != null) {
                    upgradeStrategy.setVersions(oldVersion, newVersion);
                    upgradeStrategy.loadData(this);
                    dropDatabase();
                    upgradeStrategy.onDelete(this);
                    upgradeStrategy.addData(this);
                } else {
                    dropDatabase();
                }
			} catch (DBException e) {
				Logger.error(this, "Problems dropping database during upgrade");
			}
		}
	}

    /**
	 * Execute sql statement. Be careful.
	 */
	public void execSQL(String sql) throws DBException {
		// Lock it!
		mLock.lock();
		try {
			open();
			sqLiteDatabase.execSQL(sql);
        } catch (IllegalArgumentException e) {
            Logger.error(this, e.getMessage());
            throw new DBException("Problems executing " + sql + " for database " + mainTableName, e);
		} catch (SQLiteException e) {
			Logger.error(this, e.getMessage());
			throw new DBException("Problems executing " + sql + " for database " + mainTableName, e);
		} finally {
			mLock.unlock();
		}
	}

	/**
	 * Execute sql statement. Be careful.
	 *
	 * @return Cursor
	 */
	public Cursor rawQuery(String sql, String[] selectionArgs) throws DBException {
		// Lock it!
		mLock.lock();
		try {
			open();
			return sqLiteDatabase.rawQuery(sql, selectionArgs);
		} catch (IllegalArgumentException e) {
            Logger.error(this, e.getMessage());
            throw new DBException("Problems executing " + sql + " for database " + mainTableName, e);
		} catch (SQLiteException e) {
			Logger.error(this, e.getMessage());
			throw new DBException("Problems executing " + sql + " for database " + mainTableName, e);
		} finally {
			mLock.unlock();
		}
	}

    /**
     * Return the Meta object for the given version & database
     * @param version
     * @param databaseName
     * @return Meta
     */
    public Meta getMeta(int version, String databaseName) {
        getMetaDatabase();
        mLock.lock();
        try {
            startTransaction();
            return metaDatabase.getMeta(version, databaseName);
        } catch (DBException e) {
            Logger.error(this, "Problems getting meta table entry ", e);
            return null;
        } finally {
            endTransaction();
            mLock.unlock();
        }

    }
	/**
	 * Insert a new table item.
	 *
	 * @return result
	 */
	public Object insertEntry(TableEntry table, Object data) {
		// Lock it!
		mLock.lock();
		try {
			startTransaction();
			return table.getTable().insertEntry(localDatabase, data);
		} catch (DBException e) {
			Logger.error(this, "Problems inserting entry for table " + table, e);
			return null;
		} finally {
			endTransaction();
			mLock.unlock();
		}
	}

	/**
	 * Insert a new table item.
	 *
	 * @return id
	 */
	public long insertEntry(TableEntry table, List<String> data) {
		// Lock it!
		mLock.lock();
		try {
			startTransaction();
			return table.getTable().insertEntry(localDatabase, data);
		} catch (DBException e) {
			Logger.error(this, "Problems inserting entry for table " + table, e);
			return -1;
		} finally {
			endTransaction();
			mLock.unlock();
		}
	}

    /**
     * Insert data into table
     * @param table
     * @param data
     * @param mapper
     * @return id
     */
    public long insertEntry(AbstractTable table, Object data, DataMapper mapper) {
        // Lock it!
        mLock.lock();
        try {
            startTransaction();
            return table.insertEntry(localDatabase, data, mapper);
        } catch (DBException e) {
            Logger.error(this, "Problems inserting entry for table " + table, e);
            return -1;
        } finally {
            endTransaction();
            mLock.unlock();
        }
    }
	/**
	 * Delete a table item.
	 */
	public void deleteEntry(TableEntry table, Object data) {
		// Lock it!
		mLock.lock();
		try {
			startTransaction();
			table.getTable().deleteEntry(localDatabase, data);
		} catch (DBException e) {
			Logger.error(this, "Problems deleting entry for table " + table, e);
		} finally {
			endTransaction();
			mLock.unlock();
		}
	}

	/**
	 * Delete a table item with where items
	 */
	public void deleteEntryWhere(TableEntry table, String whereClause, String[] whereArgs) {
		// Lock it!
		mLock.lock();
		try {
			startTransaction();
			table.getTable().deleteEntryWhere(localDatabase, whereClause, whereArgs);
		} catch (DBException e) {
			Logger.error(this, "Problems deleting entry for table " + table, e);
		} finally {
			endTransaction();
			mLock.unlock();
		}
	}

	/**
	 * Delete all items in this table
	 */
	public void deleteAllEntries(TableEntry table) {
		// Lock it!
		mLock.lock();
		try {
			startTransaction();
			table.getTable().deleteAllEntries(localDatabase);
		} catch (DBException e) {
			Logger.error(this, "Problems deleting entry for table " + table, e);
		} finally {
			endTransaction();
			mLock.unlock();
		}
	}

	/**
	 * Get an row for this table given the key passed in data
	 *
	 * @return result
	 */
	public Object getEntry(TableEntry table, Object data) {
		// Lock it!
		mLock.lock();
		try {
			startTransaction();
			return table.getTable().getEntry(localDatabase, data);
		} catch (DBException e) {
			Logger.error(this, "Problems getting entry for table " + table, e);
			return null;
		} finally {
			endTransaction();
			mLock.unlock();
		}
	}

	/**
	 * Get an row for this table given the key passed in data
	 *
	 * @return cursor
	 */
	public Cursor getEntry(TableEntry table, long id) {
		// Lock it!
		mLock.lock();
		try {
			startTransaction();
			return table.getTable().getEntry(localDatabase, id);
		} catch (DBException e) {
			Logger.error(this, "Problems getting entry for table " + table, e);
			return null;
		} finally {
			endTransaction();
			mLock.unlock();
		}
	}

	/**
	 * Find an entry where the given column matches the given value.
	 *
	 * @return the cursor for the object
	 */
	public Cursor getEntry(TableEntry table, String columnName, String columnValue) {
		// Lock it!
		mLock.lock();
		try {
			startTransaction();
			return table.getTable().getEntry(localDatabase, columnName, columnValue);
		} catch (DBException e) {
			Logger.error(this, "Problems getting entry for table " + table, e);
			return null;
		} finally {
			endTransaction();
			mLock.unlock();
		}
	}

	/**
	 * Update an row for this table given the key passed in data
	 *
	 * @return result
	 */
	public Object updateEntry(TableEntry table, Object data, Object key) {
		// Lock it!
		mLock.lock();
		try {
			startTransaction();
			return table.getTable().updateEntry(localDatabase, data, key);
		} catch (DBException e) {
			Logger.error(this, "Problems updating entry for table " + table, e);
			return null;
		} finally {
			endTransaction();
			mLock.unlock();
		}
	}

	/**
	 * Update an row for this table given the key passed in data
	 *
	 * @return # of items updated
	 */
	public int updateEntry(TableEntry table, List<String> data, Object key) {
		// Lock it!
		mLock.lock();
		try {
			startTransaction();
			return table.getTable().updateEntry(localDatabase, data, key);
		} catch (DBException e) {
			Logger.error(this, "Problems updating entry for table " + table, e);
			return -1;
		} finally {
			endTransaction();
			mLock.unlock();
		}
	}

	/**
	 * Update an row for this table given the values & where info
	 *
	 * @return # of rows updated
	 */
	public int updateEntryWhere(TableEntry table, ContentValues cv, String whereClause, String[] whereArgs) {
		// Lock it!
		mLock.lock();
		try {
			startTransaction();
			return table.getTable().updateEntryWhere(localDatabase, cv, whereClause, whereArgs);
		} catch (DBException e) {
			Logger.error(this, "Problems updating entry for table " + table, e);
			return -1;
		} finally {
			endTransaction();
			mLock.unlock();
		}
	}

	/**
	 * Return a cursor with all entries
	 *
	 * @return Cursor
	 */
	public Cursor getAllEntries(TableEntry table) {
		// Lock it!
		mLock.lock();
		try {
			startTransaction();
			return table.getTable().getAllEntries(localDatabase);
		} catch (DBException e) {
			Logger.error(this, "Problems getting all entries for table " + table, e);
			return null;
		} finally {
			endTransaction();
			mLock.unlock();
		}
	}

    /**
     * Get All entries for the give table
     * @param table
     * @param cls
     * @param mapper
     * @return
     */
    public List getAllEntries(AbstractTable table, Class cls, DataMapper mapper) {
        // Lock it!
        mLock.lock();
        try {
            startTransaction();
            return table.getAllEntries(localDatabase, cls, mapper);
        } catch (DBException e) {
            Logger.error(this, "Problems getting all entries for table " + table, e);
            return null;
        } finally {
            endTransaction();
            mLock.unlock();
        }
    }

        /**
         * Execute the runnable in the executor. Will run on another thread
         *
         * @return true if executed.
         */
	protected boolean executeTask(Runnable aRunnable) {
		// Lock it!
		mLock.lock();

		// Wrap the whole thing so we can make sure to unlock in
		// case something throws.
		try {

			// If we're shutdown or terminated we can't accept any new requests.
			if (mExecutor.isShutdown() || mExecutor.isTerminated()) {
				return false;
			}

			// Push the request onto the queue.
			// Check to see if our app details is valid
			if (aRunnable != null) {
				mExecutor.execute(aRunnable);
			}
		} catch (Exception RejectedExecutionException) {
			return false;
		} finally {
			mLock.unlock();
		}

		// Return the request token so the request can be canceled.
		return true;
	}


    /**
     * Get upgrade tables.
     * @param meta
     */
    public List<UpgradeTable> getUpgradeTables(Meta meta) {
        String creationString = meta.creationString;
        StringTokenizer tableTokenizer = new StringTokenizer(creationString, ";");
        List<UpgradeTable> oldDataTables = new ArrayList<UpgradeTable>();
        while (tableTokenizer.hasMoreTokens()) {
            String tableString = tableTokenizer.nextToken();
            Matcher matcher = tablePattern.matcher(tableString);
            if (matcher.find()) {
                String tableName = matcher.group(1);
                String fields = matcher.group(2);
                UpgradeTable upgradeTable = new UpgradeTable();
                upgradeTable.setTableName(tableName);
                oldDataTables.add(upgradeTable);
                Logger.debug(this, "tableName " + tableName + " fields " + fields);
                StringTokenizer fieldTokenizer = new StringTokenizer(fields, ",");
                int column = 0;
                while (fieldTokenizer.hasMoreTokens()) {
                    String columnString = fieldTokenizer.nextToken();
                    matcher = columnPattern.matcher(columnString);
                    if (matcher.find()) {
                        String columnName = matcher.group(1);
                        String type = matcher.group(2);
                        Logger.debug(this, "Column " + columnName + " type " + type);
                        upgradeTable.addField(columnName, Column.COLUMN_TYPE.getType(type), column);
                        upgradeTable.addColumn(new Column(columnName, Column.COLUMN_TYPE.getType(type)));
                        column++;
                    }
                }
                List<? extends UpgradeHolder> allEntries = getAllEntries(upgradeTable, UpgradeHolder.class,
                                                                                      upgradeTable.getMapper());
                if (allEntries != null) {
                    upgradeTable.setAllEntries(allEntries);
                }
            }
        }
        return oldDataTables;
    }
}
