package com.mastertechsoftware.sql;

import android.content.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 *  Manager for handling multiple reflection based databases
 */
public class DatabaseManager {
    private static DatabaseManager instance;
    protected Map<String, ReflectionDBHelper> databases = new HashMap<String, ReflectionDBHelper>();
    protected Context context;

    /**
     * Create new database manager with the given context (usually application)
     * @param context
     * @return DatabaseManager
     */
    public static DatabaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseManager(context);
        }
        return instance;
    }

    /**
     * Return the current database manager
     * @return DatabaseManager
     */
    public static DatabaseManager getInstance() {
        return instance;
    }

    protected DatabaseManager(Context context) {
        this.context = context;
    }

    /**
     * Add a new database
     * @param dbName
     * @param mainTableName
     * @param types
     */
    public void addDatabase(String dbName, String mainTableName, Class<? extends ReflectTableInterface>... types) {
        ReflectionDBHelper reflectionDBHelper = new ReflectionDBHelper(context, dbName, mainTableName, types);
        databases.put(dbName, reflectionDBHelper);
    }

    /**
     * Add a new item
     * @param dbName
     * @param type
     * @param data
     * @return id of added item. -1 if there was an error
     */
    public long addItem(String dbName, Class type, ReflectTableInterface data) {
        ReflectionDBHelper reflectionDBHelper = databases.get(dbName);
        return reflectionDBHelper.addItem(type, data);
    }


    /**
     * Update an existing item
     * @param dbName
     * @param type
     * @param data
     */
    public void updateItem(String dbName, Class type, ReflectTableInterface data) {
        ReflectionDBHelper reflectionDBHelper = databases.get(dbName);
        reflectionDBHelper.updateItem(type, data);
    }

    /**
     * Delete all items that match the query
     * @param dbName
     * @param type
     * @param columnName
     * @param columnValue
     */
    public void deleteItemWhere(String dbName, Class type, String columnName, String columnValue) {
        ReflectionDBHelper reflectionDBHelper = databases.get(dbName);
        reflectionDBHelper.deleteItemWhere(type, columnName, columnValue);
    }

    /**
     * Remove all items of the given type
     * @param dbName
     * @param type
     */
    public void removeAllItems(String dbName, Class type) {
        ReflectionDBHelper reflectionDBHelper = databases.get(dbName);
        reflectionDBHelper.removeAllItems(type);
    }

    /**
     * Return a list of all items in the database
     * @param dbName
     * @param type
     * @return List of items
     */
    public List getAllItems(String dbName, Class type) {
        ReflectionDBHelper reflectionDBHelper = databases.get(dbName);
        return reflectionDBHelper.getAllItems(type);
    }

    /**
     * Return a list of items that match the query on the given column
     * @param dbName
     * @param type
     * @param columnName
     * @param columnValue
     * @return List of items
     */
    public List getItemsWhere(String dbName, Class type, String columnName, String columnValue) {
        ReflectionDBHelper reflectionDBHelper = databases.get(dbName);
        return reflectionDBHelper.getItemsWhere(type, columnName, columnValue);
    }

    /**
     * Delete the whole database
     * @param dbName
     */
    public void deleteDatabase(String dbName) {
        ReflectionDBHelper reflectionDBHelper = databases.get(dbName);
        reflectionDBHelper.deleteDatabase();
    }
}
