package com.mastertechsoftware.sql;

import android.database.Cursor;
/**
 *  Class that provides helper method so each class doesn't have to Reproduce
 */
public abstract class AbstractDataMapper<T> implements DataMapper<T> {

	protected int getColumnIndex(Cursor cursor, String column) {
		return cursor.getColumnIndex(column);
	}
}
