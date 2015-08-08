package com.mastertechsoftware.mvp.model;

/**
 * Hold information about a model used by a View.
 */
public class ViewDataModel {
	protected Class modelClass;
	protected Object model;

	public Class getModelClass() {
		return modelClass;
	}

	public void setModelClass(Class modelClass) {
		this.modelClass = modelClass;
	}

	public Object getModel() {
		return model;
	}

	public void setModel(Object model) {
		this.model = model;
	}
}
