package com.mastertechsoftware.sql;

import android.content.Context;

import com.mastertechsoftware.util.log.Logger;

import java.util.ArrayList;
import java.util.List;
/**
 * Hold all the pieces needed for Handling a reflection db.
 */
public class ReflectionDBHelper {

	protected List<CRUDHelper<ReflectTableInterface>> crudHelpers = new ArrayList<CRUDHelper<ReflectTableInterface>>();
	protected Database database;
	protected BaseDatabaseHelper databaseHelper;

	public ReflectionDBHelper(Context context, String dbName, String mainTableName, Class<? extends ReflectTableInterface> ... types) {
		databaseHelper = new BaseDatabaseHelper(context, dbName, mainTableName, 1);
		database = new Database();
		databaseHelper.setLocalDatabase(database);
		try {
			for (Class<? extends ReflectTableInterface> reflectClass : types) {
				ReflectTable<ReflectTableInterface> table = new ReflectTable<ReflectTableInterface>(reflectClass.newInstance());
				database.addTable(table);
				CRUDHelper<ReflectTableInterface> crudHelper = new CRUDHelper<ReflectTableInterface>(table, databaseHelper);
				crudHelpers.add(crudHelper);
			}
		} catch (InstantiationException e) {
			Logger.error(this, "Problems creating table", e);
		} catch (IllegalAccessException e) {
			Logger.error(this, "Problems creating table", e);
		}
	}

    public Database getDatabase() {
        return database;
    }

    public BaseDatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }

    public CRUDHelper<ReflectTableInterface> getCrudHelper(int position) {
		return crudHelpers.get(position);
	}

	public void deleteDatabase() {
		try {
			databaseHelper.dropDatabase();
		} catch (DBException e) {
			Logger.error(this, "Problems deleting database", e);
		}
	}
}
