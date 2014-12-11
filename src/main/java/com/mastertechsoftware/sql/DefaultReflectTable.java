package com.mastertechsoftware.sql;

/**
 * Default Class that implements this simple interface
 */
public class DefaultReflectTable implements ReflectTableInterface {
    protected int _id;

    @Override
    public int getId() {
        return _id;
    }

    public void setId(int id) {
        this._id = id;
    }
}
