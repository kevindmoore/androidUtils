
package com.mastertechsoftware.sql.cookies;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;

import com.mastertechsoftware.sql.AbstractTable;
import com.mastertechsoftware.sql.Column;
import com.mastertechsoftware.sql.Database;
import com.mastertechsoftware.util.log.Logger;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Table to hold web cookie Information.
 */
public class CookieTable extends AbstractTable {
    public static final String TABLE_NAME = "cookies";

    public static final String ID = "_id";
    public static final String NAME = "name";
    public static final String VALUE = "value";
    public static final String COOKIE_COMMENT = "cookieComment";
    public static final String COOKIE_COMMENT_URL = "cookieCommentUrl";
    public static final String COOKIE_DOMAIN = "cookieDomain";
    public static final String COOKIE_EXPIRY_DATE = "cookieExpiryDate";
    public static final String COOKIE_PATH = "cookiePath";
    public static final String IS_SECURE = "isSecure";
    public static final String COOKIE_VERSION = "cookieVersion";

    private static final int ID_COLUMN = 0;
    private static final int NAME_COLUMN = 1;
    private static final int VALUE_COLUMN = 2;
    private static final int COOKIE_COMMENT_COLUMN = 3;
    private static final int COOKIE_COMMENT_URL_COLUMN = 4;
    private static final int COOKIE_DOMAIN_COLUMN = 5;
    private static final int COOKIE_EXPIRY_DATE_COLUMN = 6;
    private static final int COOKIE_PATH_COLUMN = 7;
    private static final int IS_SECURE_COLUMN = 8;
    private static final int COOKIE_VERSION_COLUMN = 9;
    private static final int version = 1;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yy");

    public CookieTable() {
        super(TABLE_NAME);
        addColumn(new Column(ID, Column.COLUMN_TYPE.INTEGER, true));
        addColumn(new Column(NAME, Column.COLUMN_TYPE.TEXT));
        addColumn(new Column(VALUE, Column.COLUMN_TYPE.TEXT));
        addColumn(new Column(COOKIE_COMMENT, Column.COLUMN_TYPE.TEXT));
        addColumn(new Column(COOKIE_COMMENT_URL, Column.COLUMN_TYPE.TEXT));
        addColumn(new Column(COOKIE_DOMAIN, Column.COLUMN_TYPE.TEXT));
        addColumn(new Column(COOKIE_EXPIRY_DATE, Column.COLUMN_TYPE.TEXT));
        addColumn(new Column(COOKIE_PATH, Column.COLUMN_TYPE.TEXT));
        addColumn(new Column(IS_SECURE, Column.COLUMN_TYPE.INTEGER));
        addColumn(new Column(COOKIE_VERSION, Column.COLUMN_TYPE.INTEGER));
        setVersion(version);
    }

    @Override
    public Object insertEntry(Database database, Object data) {
        DatabaseCookie cookie = (DatabaseCookie) data;
        ContentValues cv = new ContentValues();
        cv.put(NAME, cookie.getName());
        cv.put(VALUE, cookie.getValue());
        cv.put(COOKIE_COMMENT, cookie.getComment());
        cv.put(COOKIE_COMMENT_URL, cookie.getCommentURL());
        cv.put(COOKIE_DOMAIN, cookie.getDomain());
        Date expiryDate = cookie.getExpiryDate();
        if (expiryDate != null) {
            cv.put(COOKIE_EXPIRY_DATE, dateFormat.format(expiryDate));
        }
        cv.put(COOKIE_PATH, cookie.getPath());
        cv.put(IS_SECURE, cookie.isSecure() ? 1 : 0);
        cv.put(COOKIE_VERSION, cookie.getVersion());
        long id = 0;
        try {
            id = database.getDatabase().insert(TABLE_NAME, NAME, cv);
        } catch (SQLiteException e) {
            Logger.error(this, e.getMessage());
            return cookie;
        }
        cookie.setId(id);
        return cookie;
    }

    @Override
    public void deleteEntry(Database database, Object data) {
        DatabaseCookie cookie = (DatabaseCookie) data;
        String[] whereArgs = new String[1];
        whereArgs[0] = String.valueOf(cookie.getId());
        try {
            database.getDatabase().delete(TABLE_NAME, ID + "=?", whereArgs);
        } catch (SQLiteException e) {
            Logger.error(this, e.getMessage());
        }

    }

    @Override
    public void deleteAllEntries(Database database) {
        try {
            database.getDatabase().delete(TABLE_NAME, null, null);
        } catch (SQLiteException e) {
            Logger.error(this, e.getMessage());
        }

    }

    @Override
    public Object getEntry(Database database, Object data) {
        String id = (String) data;
        Cursor result;
        String[] params = {
            id
        };
        try {
            result = database.getDatabase().query(TABLE_NAME, projection, NAME + "=?", params,
                    null, null, null);
        } catch (SQLiteException e) {
            Logger.error(this, e.getMessage());
            return null;
        }
        if (result.moveToFirst()) {
            DatabaseCookie cookie = fillCookie(result);
            result.close();
            return cookie;
        }
        result.close();
        return null;
    }

    public List<DatabaseCookie> getAllCookies(Database database) {
        List<DatabaseCookie> soundboardCookies = new ArrayList<DatabaseCookie>();
        Cursor result;
        try {
            result = database.getDatabase().query(TABLE_NAME, projection, null, null, null, null,
                    null);
        } catch (SQLiteException e) {
            Logger.error(this, e.getMessage());
            return soundboardCookies;
        }
        if (!result.moveToFirst()) {
            result.close();
            return soundboardCookies;
        }
        while (!result.isAfterLast()) {
            DatabaseCookie cookie = fillCookie(result);
            soundboardCookies.add(cookie);
            result.moveToNext();
        }
        result.close();
        return soundboardCookies;
    }

    /**
     * Fill in a SoundboardCookie from the given cursor
     * 
     * @param result
     * @return SoundboardCookie
     */
    public DatabaseCookie fillCookie(Cursor result) {
        String name = result.getString(NAME_COLUMN);
        String value = result.getString(VALUE_COLUMN);
        DatabaseCookie cookie = new DatabaseCookie(name, value);
        cookie.setId(result.getLong(ID_COLUMN));
        cookie.setComment(result.getString(COOKIE_COMMENT_COLUMN));
        cookie.setCommentURL(result.getString(COOKIE_COMMENT_URL_COLUMN));
        cookie.setDomain(result.getString(COOKIE_DOMAIN_COLUMN));
        try {
            String dateString = result.getString(COOKIE_EXPIRY_DATE_COLUMN);
            if (dateString != null && dateString.length() > 0) {
                cookie.setExpiryDate(dateFormat.parse(dateString));
            }
        } catch (ParseException e) {
            Logger.error(this, "Problems parsing Date");
        }
        cookie.setPath(result.getString(COOKIE_PATH_COLUMN));
        cookie.setSecure(result.getInt(IS_SECURE_COLUMN) == 1);
        cookie.setVersion(result.getInt(COOKIE_VERSION_COLUMN));
        return cookie;
    }

    /**
     * Add all the values from the cookie store to our database. Make sure you
     * delete all values if you don't want duplicates
     * 
     * @param cookieDatabase
     * @param cookieStore
     */
    public void addCookieStore(Database cookieDatabase, CookieStore cookieStore) {
        List<Cookie> cookies = cookieStore.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getValue() != null) {
                DatabaseCookie soundboardCookie = new DatabaseCookie(cookie.getName(),
                        cookie.getValue());
                soundboardCookie.fillCookie(cookie);
                insertEntry(cookieDatabase, soundboardCookie);
            } else {
                Logger.error(this, "addCookieStore. Not adding cookie " + cookie.getName()
                        + " because of null value");
            }
        }
    }

    /**
     * Get a cookie store from our database.
     * 
     * @param cookieDatabase
     * @return
     */
    public CookieStore getCookieStore(Database cookieDatabase) {
        CookieStore store = new BasicCookieStore();
        List<DatabaseCookie> soundboardCookies = getAllCookies(cookieDatabase);
        for (DatabaseCookie soundboardCookie : soundboardCookies) {
            store.addCookie(soundboardCookie);
        }
        return store;
    }

}
