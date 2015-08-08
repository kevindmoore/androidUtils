package com.mastertechsoftware.mvp.model;

import android.view.View;

import com.mastertechsoftware.mvp.view.ViewInterface;

import java.util.ArrayList;
import java.util.List;
/**
 * Hold information about the view
 */
public class ViewModel {
	protected String viewName;
	protected String layoutName;
	protected int viewResourceId;
	protected ViewInterface viewInterface;
	protected View view;
	protected Class viewType;
	protected boolean mainView;
	protected boolean firstView;
	protected List<FieldModel> fieldModels = new ArrayList<FieldModel>();
	protected List<ViewDataModel> viewModels = new ArrayList<ViewDataModel>();

	public String getViewName() {
		return viewName;
	}

	public void setViewName(String viewName) {
		this.viewName = viewName;
	}

	public View getView() {
		return view;
	}

	public void setView(View view) {
		this.view = view;
	}

	public ViewInterface getViewInterface() {
		return viewInterface;
	}

	public void setViewInterface(ViewInterface viewInterface) {
		this.viewInterface = viewInterface;
	}

	public String getLayoutName() {
		return layoutName;
	}

	public void setLayoutName(String layoutName) {
		this.layoutName = layoutName;
	}

	public void addField(FieldModel fieldModel) {
		fieldModels.add(fieldModel);
	}

	public void addViewModel(ViewDataModel model) {
		viewModels.add(model);
	}

	public List<ViewDataModel> getViewModels() {
		return viewModels;
	}

	public void setViewModels(List<ViewDataModel> viewModels) {
		this.viewModels = viewModels;
	}

	public List<FieldModel> getFieldModels() {
		return fieldModels;
	}

	public void setFieldModels(List<FieldModel> fieldModels) {
		this.fieldModels = fieldModels;
	}

	public int getViewResourceId() {
		return viewResourceId;
	}

	public void setViewResourceId(int viewResourceId) {
		this.viewResourceId = viewResourceId;
	}

	public Class getViewType() {
		return viewType;
	}

	public void setViewType(Class viewType) {
		this.viewType = viewType;
	}

	public boolean isFirstView() {
		return firstView;
	}

	public void setFirstView(boolean firstView) {
		this.firstView = firstView;
	}

	public boolean isMainView() {
		return mainView;
	}

	public void setMainView(boolean mainView) {
		this.mainView = mainView;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ViewModel viewModel = (ViewModel) o;

		if (viewResourceId != viewModel.viewResourceId) {
			return false;
		}
		if (mainView != viewModel.mainView) {
			return false;
		}
		if (firstView != viewModel.firstView) {
			return false;
		}
		if (viewName != null ? !viewName.equals(viewModel.viewName) : viewModel.viewName != null) {
			return false;
		}
		if (layoutName != null ? !layoutName.equals(viewModel.layoutName) : viewModel.layoutName != null) {
			return false;
		}
		if (view != null ? !view.equals(viewModel.view) : viewModel.view != null) {
			return false;
		}
		if (viewType != null ? !viewType.equals(viewModel.viewType) : viewModel.viewType != null) {
			return false;
		}
		if (fieldModels != null ? !fieldModels.equals(viewModel.fieldModels) : viewModel.fieldModels != null) {
			return false;
		}
		return !(viewModels != null ? !viewModels.equals(viewModel.viewModels) : viewModel.viewModels != null);

	}

	@Override
	public int hashCode() {
		int result = viewName != null ? viewName.hashCode() : 0;
		result = 31 * result + (layoutName != null ? layoutName.hashCode() : 0);
		result = 31 * result + viewResourceId;
		result = 31 * result + (view != null ? view.hashCode() : 0);
		result = 31 * result + (viewType != null ? viewType.hashCode() : 0);
		result = 31 * result + (mainView ? 1 : 0);
		result = 31 * result + (firstView ? 1 : 0);
		result = 31 * result + (fieldModels != null ? fieldModels.hashCode() : 0);
		result = 31 * result + (viewModels != null ? viewModels.hashCode() : 0);
		return result;
	}
}
