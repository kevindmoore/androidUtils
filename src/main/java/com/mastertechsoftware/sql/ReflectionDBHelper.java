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
	protected int version = 1;

	public ReflectionDBHelper(Context context, String dbName, String mainTableName, int version, Class<? extends ReflectTableInterface>... types) {
		Logger.setDebug(ReflectionDBHelper.class.getSimpleName(), debugging);
		this.version = version;
		databaseHelper = new BaseDatabaseHelper(context, dbName, mainTableName, version);
		database = new Database();
		database.setVersion(version);
		databaseHelper.setLocalDatabase(database);
 		for (Class<? extends ReflectTableInterface> reflectClass : types) {
			addTable(reflectClass);
		}

	}

	public ReflectionDBHelper(Context context, String dbName, String mainTableName, Class<? extends ReflectTableInterface>... types) {
		this(context, dbName, mainTableName, 1, types);
    }

	/**
	 * Return the current version of the database
	 * @return version
	 */
	public int getCurrentVersion() {
		return databaseHelper.getDBVersion();
	}

    private void addTable(Class<? extends ReflectTableInterface> reflectClass) {
        try {
            ReflectTable<ReflectTableInterface> table = new ReflectTable<ReflectTableInterface>(reflectClass.newInstance(), database);
			table.setVersion(version);
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

    public void deleteItem(Class type, int id) {
        Integer position = classMapper.get(type);
        if (position == null) {
            Logger.error("Type " + type.getName() + " Not found");
            return;
        }
        CRUDHelper<ReflectTableInterface> crudHelper = getCrudHelper(position);
        crudHelper.deleteItem(id);
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

	public Object getItem(Class type, long id, ReflectTableInterface newItem) {
		Integer position = classMapper.get(type);
		if (position == null) {
			Logger.error("Type " + type.getName() + " Not found");
			return null;
		}
		CRUDHelper<ReflectTableInterface> crudHelper = getCrudHelper(position);
		return crudHelper.getItem(id, newItem);
	}

	public Object getItemWhere(Class type, String columnName, String columnValue) {
		Integer position = classMapper.get(type);
		if (position == null) {
			Logger.error("Type " + type.getName() + " Not found");
			return null;
		}
		CRUDHelper<ReflectTableInterface> crudHelper = getCrudHelper(position);
		try {
			return crudHelper.getItemWhere((ReflectTableInterface) type.newInstance(), columnName, columnValue);
		} catch (InstantiationException e) {
			Logger.error("Problems Creating object of type " + type.getName(), e);
		} catch (IllegalAccessException e) {
			Logger.error("Problems Creating object of type " + type.getName(), e);
		}
		return null;
	}

	/**
	 * Create the database if it was dropped.
	 * @throws DBException
	 */
	public void createDatabase() throws DBException {
		databaseHelper.createDatabase();
	}

}
