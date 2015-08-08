package com.mastertechsoftware.mvp.presenter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.mastertechsoftware.mvp.model.ViewModel;
import com.mastertechsoftware.mvp.view.ViewInterface;
/**
 * Interface for all Presenter types
 */
public interface PresenterInterface {
	ViewInterface loadView(ViewModel viewModel);
	void dropView(ViewInterface view);
	void goToView(ViewInterface view);
	void setup(Context context, LayoutInflater layoutInflator);
	Context getContext();
	LayoutInflater getLayoutInflater();
	void setMainLayout(ViewGroup mainLayout);
	void setMainViewModel(ViewModel viewModel);
	ViewModel getMainViewModel();
	ViewInterface findViewByModel(ViewModel viewModel);
	ViewInterface findView(Class viewClass);
	void setViewModel(ViewModel viewModel);
	ViewModel getViewModel();
	ViewInterface getMainView();
	void start(ViewGroup layoutView, ViewModel viewModel);
	void addViewToLayout(ViewInterface view);
	ViewModel findViewModelByName(String viewName);
	ViewModel findViewModelByLayout(String layoutName);
	void saveLayouts();
	void loadLayouts();
	boolean handleBack();
	void shutdown();
	void setMainView(ViewInterface mainView);
}
