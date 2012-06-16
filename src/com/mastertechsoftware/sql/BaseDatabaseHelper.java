package com.mastertechsoftware.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.mastertechsoftware.util.log.Logger;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Base database helper. Should have most of the functionality of a helper.
 * The Database name & version are required
 */
public class BaseDatabaseHelper extends SQLiteOpenHelper  {

    protected enum STATE {
        NEW,
        CREATED,
        CLOSED,
        OPENING,
        OPEN
    }
    protected SQLiteDatabase sqLiteDatabase;
    protected Database localDatabase;
    protected Context context;
    protected STATE state = STATE.NEW;
    protected String mainTableName;
    // NOTE: Override this. This must be set before constructor called
    protected int version = 1;

    // Lock used to serialize access to this API.
    protected final ReentrantLock mLock = new ReentrantLock();

    /**
     * Create a helper object to create, open, and/or manage a database. This method always returns
     * very quickly. The database is not actually created or opened until one of {@link
     * #getWritableDatabase} or {@link #getReadableDatabase} is called.
     *
     * @param context to use to open or create the database
     * @param databaseName
     * @param mainTableName
     * @param version
     */
    protected BaseDatabaseHelper(Context context, String databaseName, String mainTableName, int version) {
        super(context, databaseName, null, version);
        this.context = context;
        this.mainTableName = mainTableName;
        this.version = version;
    }

    /**
     * Check to see if the table exists. If not, create the database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

//        Logger.debug(this, "onCreate");
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='"
                + mainTableName + "'", null);

        sqLiteDatabase = db;
        // Create our Databases
        createLocalDB();

        try {
            if (cursor.getCount() == 0) {
//                Logger.debug(this, "Creating DB");
                localDatabase.createDatabase();
                state = STATE.CREATED;
            }
        } catch (SQLiteException e) {
            Logger.error(this, e.getMessage());
        } finally {
            cursor.close();
        }
    }

    /**
     * Is the database open
     * @return true if open
     */
    protected boolean isOpen() {
        return (state == STATE.OPEN);
    }


    /**
     * Call to open database.
     */
    public synchronized void open() {
        // Already open
        if (state == STATE.OPEN || state == STATE.OPENING) {
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
            if (state == STATE.NEW) {
//                Logger.debug(this, "State is new, creating");
                onCreate(sqLiteDatabase);
            } else {
//                Logger.debug(this, "State is " + state);
                createLocalDB();
            }
            state = STATE.OPEN;
        } catch (SQLiteException e) {
            Logger.error(this, "Problems opening database " + mainTableName, e);
        } catch (IllegalStateException e) {
            Logger.error(this, "Problems opening database " + mainTableName, e);
        } finally {
            mLock.unlock();
        }
    }

    /**
     * Create our database objects with the current SQLite db.
     * You will override this to create the database object with your own.
     */
    protected void createLocalDB() {
        localDatabase = new Database(sqLiteDatabase);
    }

    /**
     * Close the database. This call is very important. Need to call after finished using class.
     * Don't keep open.
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
            if ((state != STATE.CLOSED && state != STATE.OPENING) && sqLiteDatabase != null) {
                super.close();
            }
            state = STATE.CLOSED;
            sqLiteDatabase = null;
            localDatabase = null;
        } finally {
            mLock.unlock();
        }
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
    public void dropDatabase() {
        open();
        localDatabase.dropDatabase();
        onCreate(sqLiteDatabase);
    }

    /**
     * If the database version changes, migrate the data to the new scheme
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        sqLiteDatabase = db;
        createLocalDB();
        // For now, just delete
        if (oldVersion != newVersion) {
            dropDatabase();
        }
    }

    /**
     * Execute sql statement. Be careful.
     * @param sql
     */
    public void execSQL(String sql) {
        // Lock it!
        mLock.lock();
        try {
            open();
            sqLiteDatabase.execSQL(sql);
        } catch (SQLiteException e) {
            Logger.error(e.getMessage());
        } finally {
            mLock.unlock();
        }
    }

    /**
     * Execute sql statement. Be careful.
     * @param sql
     * @param selectionArgs
     * @return Cursor
     */
    public Cursor rawQuery(String sql, String[] selectionArgs) {
        // Lock it!
        mLock.lock();
        try {
            open();
            return sqLiteDatabase.rawQuery(sql, selectionArgs);
        } catch (SQLiteException e) {
            Logger.error(e.getMessage());
        } finally {
            mLock.unlock();
        }
        return null;
    }

    /**
     * Insert a new table item.
     * @param table
     * @param data
     * @return result
     */
    public Object insertEntry(TableEntry table, Object data) {
        // Lock it!
        mLock.lock();
        try {
            open();
            return table.getTable().insertEntry(localDatabase, data);
        } finally {
            mLock.unlock();
        }
    }

    /**
     * Insert a new table item.
     * @param table
     * @param data
     * @return id
     */
    public long insertEntry(TableEntry table, List<String> data) {
        // Lock it!
        mLock.lock();
        try {
            open();
            return table.getTable().insertEntry(localDatabase, data);
        } finally {
            mLock.unlock();
        }
    }

    /**
    * Delete a table item.
    * @param table
    * @param data
    */
    public void deleteEntry(TableEntry table, Object data) {
        // Lock it!
        mLock.lock();
        try {
            open();
            table.getTable().deleteEntry(localDatabase, data);
        } finally {
            mLock.unlock();
        }
    }

    /**
     * Delete a table item with where items
     * @param table
     * @param whereClause
     * @param whereArgs
     */
    public void deleteEntryWhere(TableEntry table, String whereClause, String[] whereArgs) {
        // Lock it!
        mLock.lock();
        try {
            open();
            table.getTable().deleteEntryWhere(localDatabase, whereClause, whereArgs);
        } finally {
            mLock.unlock();
        }
    }

    /**
     * Delete all items in this table
     * @param table
     */
    public void deleteAllEntries(TableEntry table) {
        // Lock it!
        mLock.lock();
        try {
            open();
            table.getTable().deleteAllEntries(localDatabase);
        } finally {
            mLock.unlock();
        }
    }

    /**
     * Get an row for this table given the key passed in data
     * @param table
     * @param data
     * @return result
     */
    public Object getEntry(TableEntry table, Object data) {
        // Lock it!
        mLock.lock();
        try {
            open();
            return table.getTable().getEntry(localDatabase, data);
        } finally {
            mLock.unlock();
        }
    }

    /**
     * Get an row for this table given the key passed in data
     * @param table
     * @param id
     * @return cursor
     */
    public Cursor getEntry(TableEntry table, long id) {
        // Lock it!
        mLock.lock();
        try {
            open();
            return table.getTable().getEntry(localDatabase, id);
        } finally {
            mLock.unlock();
        }
    }

    /**
     * Find an entry where the given column matches the given value.
     * @param table
     * @param columnName
     * @param columnValue
     * @return the cursor for the object
     */
    public Cursor getEntry(TableEntry table, String columnName, String columnValue) {
        // Lock it!
        mLock.lock();
        try {
            open();
            return table.getTable().getEntry(localDatabase, columnName, columnValue);
        } finally {
            mLock.unlock();
        }
    }

    /**
     * Update an row for this table given the key passed in data
     * @param table
     * @param data
     * @return result
     */
    public Object updateEntry(TableEntry table, Object data, Object key) {
        // Lock it!
        mLock.lock();
        try {
            open();
            return table.getTable().updateEntry(localDatabase, data, key);
        } finally {
            mLock.unlock();
        }
    }

    /**
     * Update an row for this table given the key passed in data
     * @param table
     * @param data
     * @param key
     * @return # of items updated
     */
    public int updateEntry(TableEntry table, List<String> data, Object key) {
        // Lock it!
        mLock.lock();
        try {
            open();
            return table.getTable().updateEntry(localDatabase, data, key);
        } finally {
            mLock.unlock();
        }
    }

    /**
     * Update an row for this table given the values & where info
     * @param table
     * @param cv
     * @param whereClause
     * @param whereArgs
     * @return # of rows updated
     */
    public int updateEntryWhere(TableEntry table, ContentValues cv, String whereClause,
                                String[] whereArgs) {
        // Lock it!
        mLock.lock();
        try {
            open();
            return table.getTable().updateEntryWhere(localDatabase, cv, whereClause, whereArgs);
        } finally {
            mLock.unlock();
        }
    }


    /**
     * Return a cursor with all entries
     * @param table
     * @return Cursor
     */
    public Cursor getAllEntries(TableEntry table) {
        // Lock it!
        mLock.lock();
        try {
            open();
            return table.getTable().getAllEntries(localDatabase);
        } finally {
            mLock.unlock();
        }
    }
}
