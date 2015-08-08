package com.mastertechsoftware.mvp.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mastertechsoftware.mvp.model.FieldModel;
import com.mastertechsoftware.mvp.model.ModelInterface;
import com.mastertechsoftware.mvp.presenter.PresenterInterface;
import com.mastertechsoftware.mvp.presenter.PresenterUtils;

import java.util.ArrayList;
import java.util.List;
/**
 * Concrete class that implements ViewInterface
 */
public abstract class MVPView implements ViewInterface {
	protected View currentView;
	protected List<ModelInterface> models = new ArrayList<ModelInterface>();
	protected List<FieldModel> fieldModels = new ArrayList<FieldModel>();
	protected Context context;
	protected LayoutInflater layoutInflator;
	protected PresenterInterface presenter;

	@Override
	public void setup(Context context, LayoutInflater layoutInflator) {
		this.context = context;
		this.layoutInflator = layoutInflator;
	}

	@Override
	public Context getContext() {
		return context;
	}

	@Override
	public void shutdown() {

	}

	@Override
	public void create(int viewResourceId) {
		if (viewResourceId != 0) {
			currentView = layoutInflator.inflate(viewResourceId, null, false);
		}
	}


	@Override
	public void fillView() {
//		if (models == null || currentView == null || mapper == null) {
//			Logger.error("" + (models == null ? "models is null" : " ") + (currentView == null ? " currentView is null" : "")
//							 + (mapper == null ? " mapper is null" : ""));
//			return;
//		}
		mapViews();
		mapListeners();
/*
		if (mapper != null && currentView instanceof ViewGroup) {
			ViewGroup viewGroup = (ViewGroup)currentView;
			final int childCount = viewGroup.getChildCount();
			for (int i = 0; i < childCount; i++) {
				View view = viewGroup.getChildAt(i);
				mapper.map(null, view, view.getId());
			}
		}
*/
	}

	@Override
	public void addView(ViewInterface view) {
		final View mainView = getView();
		if (mainView instanceof ViewGroup) {
			((ViewGroup)mainView).addView(view.getView(), PresenterUtils.getFrameLayout());
		}
	}

	@Override
	public void removeView(ViewInterface view) {
		final View mainView = getView();
		if (mainView instanceof ViewGroup) {
			((ViewGroup)mainView).removeView(view.getView());
		}
	}

	@Override
	public void removeViews() {

	}

	/**
	 * Override this to provide the resource ids to map the view to.
	 * Use the provided mapper
	 */
	public abstract void mapViews();

	/**
	 * Override this to provide listeners to the views (like onClickListener)
	 */
	public abstract void mapListeners();

	@Override
	public View getView() {
		return currentView;
	}

	@Override
	public void saveData() {
		if (models != null) {
			for (ModelInterface model : models) {
				model.saveData();
			}
		}
	}

	@Override
	public void loadData() {
		if (models != null) {
			for (ModelInterface model : models) {
				model.loadData();
			}
		}
		reloadData();
	}

	public void reloadData() {

	}

	@Override
	public PresenterInterface getPresenter() {
		return presenter;
	}

	@Override
	public void setPresenter(PresenterInterface presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setModels(List<ModelInterface> models) {
		this.models = models;
	}

	@Override
	public List<ModelInterface> getModels() {
		return models;
	}

	public void addModel(ModelInterface model) {
		models.add(model);
	}

	@Override
	public List<FieldModel> getFields() {
		return fieldModels;
	}

	@Override
	public void setFields(List<FieldModel> models) {
		this.fieldModels = fieldModels;
	}

	public void addField(FieldModel model) {
		fieldModels.add(model);
	}

	@Override
	public String toString() {
		return "MVPView{" +
			"fieldModels=" + fieldModels +
			", models=" + models +
			'}';
	}
}
