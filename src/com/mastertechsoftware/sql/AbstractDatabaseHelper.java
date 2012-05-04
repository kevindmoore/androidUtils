package com.mastertechsoftware.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.mastertechsoftware.util.log.Logger;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Abstract database helper. Should have most of the functionality of a helper.
 * The Database name & version are required
 */
public abstract class AbstractDatabaseHelper extends SQLiteOpenHelper  {

    protected enum STATE {
        NEW,
        CREATED,
        CLOSED,
        OPEN
    }
    protected SQLiteDatabase sqLiteDatabase;
    protected Database localDatabase;
    protected Context context;
    protected STATE state = STATE.NEW;
    // NOTE: Override this. This must be set before constructor called
    protected static int version = 1;
    protected String databaseName;

    // Lock used to serialize access to this API.
    protected final ReentrantLock mLock = new ReentrantLock();

    /**
     * Create a helper object to create, open, and/or manage a database. This method always returns
     * very quickly. The database is not actually created or opened until one of {@link
     * #getWritableDatabase} or {@link #getReadableDatabase} is called.
     *
     * @param context to use to open or create the database
     */
    protected AbstractDatabaseHelper(Context context, String databaseName) {
        super(context, databaseName, null, version);
        this.databaseName = databaseName;
        this.context = context;
    }

    /**
     * Check to see if the table exists. If not, create the database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

//        Logger.debug(this, "onCreate");
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='"
                + getMainTableName() + "'", null);

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
     * Return the main table name. This is used to check for the existence of the database.
     * @return table name
     */
    protected abstract String getMainTableName();

    /**
     * Call to open database.
     */
    public synchronized void open() {
        // Already open
        if (state == STATE.OPEN) {
            return;
        }
        // Lock it!
        mLock.lock();

        // Wrap the whole thing so we can make sure to unlock in
        // case something throws.
        try {
//            Logger.debug(this, "Opening DB");
            sqLiteDatabase = getWritableDatabase();

            // Make sure the tables exist
            if (state == STATE.NEW) {
//                Logger.debug(this, "State is new, creating");
                onCreate(sqLiteDatabase);
            } else {
//                Logger.debug(this, "State is " + state);
                createLocalDB();
            }
            state = STATE.OPEN;
        } finally {
            mLock.unlock();
        }
    }

    /**
     * Create our database objects with the current SQLite db.
     * You will override this to create the database object with your own.
     */
    protected abstract void createLocalDB();

    /**
     * Close the database. This call is very important. Need to call after finished using class.
     * Don't keep open.
     */
    @Override
    public synchronized void close() {
        // Lock it!
        mLock.lock();

        // Wrap the whole thing so we can make sure to unlock in
        // case something throws.
        try {
//            Logger.debug(this, "Closing DB");
            if (state == STATE.OPEN && sqLiteDatabase != null) {
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
    public static int getVersion() {
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
     * Drop a table (i.e. delete)
     * @param table
     */
    public void dropTable(Table table) {
        dropTable(table.getTableName());
    }

    /**
     * Drop a table (i.e. delete)
     * @param table
     */
    public void dropTable(String table) {
        open();
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + table);
    }

    /**
     * Create a new table
     * @param table
     */
    public void createTable(String table) {
        open();
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + table);
    }

    /**
     * Execute sql statement. Be careful.
     * @param sql
     */
    public void execSQL(String sql) {
        localDatabase.execSQL(sql);
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
     * Insert a new table item.
     * @param table
     * @param data
     * @return result
     */
    public Object insertEntry(TableEntry table, Object data) {
        // Lock it!
        mLock.lock();
        try {
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
            return table.getTable().getEntry(localDatabase, data);
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
    public Object updateEntry(TableEntry table, Object data) {
        // Lock it!
        mLock.lock();
        try {
            return table.getTable().updateEntry(localDatabase, data);
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
            return table.getTable().updateEntryWhere(localDatabase, cv, whereClause, whereArgs);
        } finally {
            mLock.unlock();
        }
    }

    /**
     * Return all rows for this table
     * @param table
     * @param data
     * @return Generic result. Should be list of items
     */
    public Object getAllEntries(TableEntry table, Object data) {
        // Lock it!
        mLock.lock();
        try {
            return table.getTable().getAllEntries(localDatabase, data);
        } finally {
            mLock.unlock();
        }
    }
}
