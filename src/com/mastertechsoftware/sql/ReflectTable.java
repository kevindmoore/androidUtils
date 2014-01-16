package com.mastertechsoftware.sql;

import android.content.ContentValues;
import android.database.Cursor;

import com.mastertechsoftware.util.log.Logger;
import com.mastertechsoftware.util.reflect.UtilReflector;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
/**
 * Table that is built by using reflection on the object
 */
public class ReflectTable<T> extends AbstractTable<T> {
	private Mapper mapper;

	public ReflectTable(T type) {
		mapper = new Mapper();
		String tableName = type.getClass().getSimpleName().toLowerCase();
		setTableName(tableName);
		Field[] fields = UtilReflector.getFields(type.getClass());
		boolean idFieldFound = false;
		for (Field field : fields) {
			Column.COLUMN_TYPE column_type = Column.COLUMN_TYPE.TEXT;
			Class<?> fieldType = field.getType();
			if (fieldType == int.class || fieldType == Integer.class) {
				column_type = Column.COLUMN_TYPE.INTEGER;
			} else if (fieldType == float.class || fieldType == Float.class) {
				column_type = Column.COLUMN_TYPE.FLOAT;
			} else if (fieldType == boolean.class || fieldType == Boolean.class) {
				column_type = Column.COLUMN_TYPE.BOOLEAN;
			} else if (fieldType == long.class || fieldType == Long.class) {
				column_type = Column.COLUMN_TYPE.LONG;
			} else if (fieldType == Number.class) {
				column_type = Column.COLUMN_TYPE.INTEGER;
			}
			Column column = null;
			if (ID.equalsIgnoreCase(field.getName())) {
				idFieldFound = true;
				column = new Column(field.getName(), column_type, true);
			} else {
				column = new Column(field.getName(), column_type);
			}
			addColumn(column);
		}
		if (!idFieldFound) {
			throw new RuntimeException("No ID field found");
		}
	}

	public Mapper getMapper() {
		return mapper;
	}

	public class Mapper extends AbstractDataMapper<T> {

		@Override
		public void write(ContentValues cv, Column column, T type) {
			Field[] fields = UtilReflector.getFields(type.getClass());
			Field field = fields[column.getColumnPosition()];
			field.setAccessible(true);

			// Need to skip ID
			if (column.getName().equalsIgnoreCase(ID)) {
				return;
			}
			switch (column.getType()) {
				case TEXT:
					try {
						cv.put(column.getName(), (String)field.get(type));
					} catch (IllegalAccessException e) {
						Logger.error(this, "Problems mapping column " + column.getName(), e);
					}
					break;
				case INTEGER:
					try {
						cv.put(column.getName(), (Integer)field.get(type));
					} catch (IllegalAccessException e) {
						Logger.error(this, "Problems mapping column " + column.getName(), e);
					}
					break;
				case FLOAT:
					try {
						cv.put(column.getName(), (Float)field.get(type));
					} catch (IllegalAccessException e) {
						Logger.error(this, "Problems mapping column " + column.getName(), e);
					}
					break;
				case BOOLEAN:
					try {
						cv.put(column.getName(), (Boolean)field.get(type));
					} catch (IllegalAccessException e) {
						Logger.error(this, "Problems mapping column " + column.getName(), e);
					}
					break;
				case LONG:
					try {
						cv.put(column.getName(), (Long)field.get(type));
					} catch (IllegalAccessException e) {
						Logger.error(this, "Problems mapping column " + column.getName(), e);
					}
					break;
			}
		}

		@Override
		public void read(Cursor cursor, Column column, T type) {
			int columnIndex = getColumnIndex(cursor, column.getName());
			if (columnIndex == -1) {
				Logger.error(this, "Mapper.read: Column " + column.getName() + " does not exist in cursor");
				return;
			}
			Field[] fields = UtilReflector.getFields(type.getClass());
			Field field = fields[column.getColumnPosition()];
			field.setAccessible(true);
			switch (column.getType()) {
				case TEXT:
					try {
						field.set(type, cursor.getString(columnIndex));
					} catch (IllegalAccessException e) {
						Logger.error(this, "Problems mapping column " + column.getName(), e);
					}
					break;
				case INTEGER:
					try {
						field.set(type, cursor.getInt(columnIndex));
					} catch (IllegalAccessException e) {
						Logger.error(this, "Problems mapping column " + column.getName(), e);
					}
					break;
				case FLOAT:
					try {
						field.set(type, cursor.getFloat(columnIndex));
					} catch (IllegalAccessException e) {
						Logger.error(this, "Problems mapping column " + column.getName(), e);
					}
					break;
				case BOOLEAN:
					try {
						field.set(type, cursor.getInt(columnIndex) == 1 ? Boolean.TRUE : Boolean.FALSE);
					} catch (IllegalAccessException e) {
						Logger.error(this, "Problems mapping column " + column.getName(), e);
					}
					break;
				case LONG:
					try {
						field.set(type, cursor.getLong(columnIndex));
					} catch (IllegalAccessException e) {
						Logger.error(this, "Problems mapping column " + column.getName(), e);
					}
					break;
			}
		}
	}

}
