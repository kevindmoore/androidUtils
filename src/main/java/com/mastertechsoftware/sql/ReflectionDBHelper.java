package com.mastertechsoftware.sql;

import android.content.Context;

import com.mastertechsoftware.util.log.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Hold all the pieces needed for Handling a reflection db.
 */
public class ReflectionDBHelper {

    protected List<CRUDHelper<ReflectTableInterface>> crudHelpers = new ArrayList<CRUDHelper<ReflectTableInterface>>();
    protected Database database;
    protected BaseDatabaseHelper databaseHelper;
    protected Map<Class, Integer> classMapper = new HashMap<Class, Integer>();
	protected boolean debugging = false;

    public ReflectionDBHelper(Context context, String dbName, String mainTableName, Class<? extends ReflectTableInterface>... types) {
		Logger.setDebug(ReflectionDBHelper.class.getSimpleName(), debugging);
        databaseHelper = new BaseDatabaseHelper(context, dbName, mainTableName, 1);
        database = new Database();
        databaseHelper.setLocalDatabase(database);
        for (Class<? extends ReflectTableInterface> reflectClass : types) {
            addTable(reflectClass);
        }
    }

    private void addTable(Class<? extends ReflectTableInterface> reflectClass) {
        try {
            ReflectTable<ReflectTableInterface> table = new ReflectTable<ReflectTableInterface>(reflectClass.newInstance(), database);
            if (!database.tableExists(table.getTableName())) {
                Logger.debug("Adding " + table.toString());
                database.addTable(table);
                classMapper.put(reflectClass, crudHelpers.size()); // Do this before adding so it's zero based
                CRUDHelper<ReflectTableInterface> crudHelper = new CRUDHelper<ReflectTableInterface>(table, databaseHelper);
                crudHelpers.add(crudHelper);
                List<Field> reflectFields = table.getReflectFields();
                for (Field reflectField : reflectFields) {
                    addTable(((Class<? extends ReflectTableInterface>) reflectField.getType()));
                }
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

    public CRUDHelper<ReflectTableInterface> getCrudHelper(String tableName) {
        for (CRUDHelper<ReflectTableInterface> crudHelper : crudHelpers) {
            if (crudHelper.getTable().getTableName().equalsIgnoreCase(tableName)) {
                return crudHelper;
            }
        }
        return null;
    }

    public void deleteDatabase() {
        try {
            databaseHelper.dropDatabase();
        } catch (DBException e) {
            Logger.error(this, "Problems deleting database", e);
        }
    }

    public long addItem(Class type, ReflectTableInterface data) {
        Integer position = classMapper.get(type);
        if (position == null) {
            Logger.error("Type " + type.getName() + " Not found");
            return -1;
        }
        CRUDHelper<ReflectTableInterface> crudHelper = getCrudHelper(position);
        return crudHelper.addItem(data);
    }

    public void updateItem(Class type, ReflectTableInterface data) {
        Integer position = classMapper.get(type);
        if (position == null) {
            Logger.error("Type " + type.getName() + " Not found");
            return;
        }
        CRUDHelper<ReflectTableInterface> crudHelper = getCrudHelper(position);
        crudHelper.updateItem(data, data.getId());
    }

    public void deleteItemWhere(Class type, String columnName, String columnValue) {
        Integer position = classMapper.get(type);
        if (position == null) {
            Logger.error("Type " + type.getName() + " Not found");
            return;
        }
        CRUDHelper<ReflectTableInterface> crudHelper = getCrudHelper(position);
        crudHelper.deleteItemWhere(columnName, columnValue);
    }

    public void removeAllItems(Class type) {
        Integer position = classMapper.get(type);
        if (position == null) {
            Logger.error("Type " + type.getName() + " Not found");
            return;
        }
        CRUDHelper<ReflectTableInterface> crudHelper = getCrudHelper(position);
        crudHelper.deleteAllItems();
    }

    public List getAllItems(Class type) {
        Integer position = classMapper.get(type);
        if (position == null) {
            Logger.error("Type " + type.getName() + " Not found");
            return null;
        }
        CRUDHelper<ReflectTableInterface> crudHelper = getCrudHelper(position);
        return (List) crudHelper.getItems(type);
    }

    public List getItemsWhere(Class type, String columnName, String columnValue) {
        Integer position = classMapper.get(type);
        if (position == null) {
            Logger.error("Type " + type.getName() + " Not found");
            return null;
        }
        CRUDHelper<ReflectTableInterface> crudHelper = getCrudHelper(position);
        return (List) crudHelper.getItemsWhere(type, columnName, columnValue);
    }
}
