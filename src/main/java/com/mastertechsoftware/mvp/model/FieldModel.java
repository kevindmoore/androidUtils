package com.mastertechsoftware.mvp.model;

/**
 * Hold information about a View Field (such as a TextView)
 */
public class FieldModel {
	protected String fieldName;
	protected int fieldResourceId;
	protected Class fieldType;
	protected Object fieldData;

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public Object getFieldData() {
		return fieldData;
	}

	public void setFieldData(Object fieldData) {
		this.fieldData = fieldData;
	}

	public int getFieldResourceId() {
		return fieldResourceId;
	}

	public void setFieldResourceId(int fieldResourceId) {
		this.fieldResourceId = fieldResourceId;
	}

	public Class getFieldType() {
		return fieldType;
	}

	public void setFieldType(Class fieldType) {
		this.fieldType = fieldType;
	}
}
