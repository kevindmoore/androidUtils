package com.mastertechsoftware.mvp.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.mastertechsoftware.mvp.model.FieldModel;
import com.mastertechsoftware.mvp.model.ModelInterface;
import com.mastertechsoftware.mvp.presenter.PresenterInterface;

import java.util.List;
/**
 * Interface for all View types
 */
public interface ViewInterface {
	void setup(Context context, LayoutInflater layoutInflator);
	Context getContext();
	void shutdown();
	void create(int viewResourceId);
	void addView(ViewInterface view);
	void setModels(List<ModelInterface> models);
	List<ModelInterface> getModels();
	void setFields(List<FieldModel> models);
	List<FieldModel> getFields();
	void fillView();
	void removeView(ViewInterface viewInterface);
	void removeViews();
	void reloadData();
	View getView();
	void saveData();
	void loadData();
	PresenterInterface getPresenter();
	void setPresenter(PresenterInterface presenter);
}
