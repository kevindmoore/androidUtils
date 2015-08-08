package com.mastertechsoftware.mvp.presenter;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.mastertechsoftware.json.JSONData;
import com.mastertechsoftware.json.JSONDataException;
import com.mastertechsoftware.mvp.model.FieldModel;
import com.mastertechsoftware.mvp.model.ModelInterface;
import com.mastertechsoftware.mvp.model.ViewDataModel;
import com.mastertechsoftware.mvp.model.ViewModel;
import com.mastertechsoftware.mvp.view.ViewInterface;
import com.mastertechsoftware.util.log.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
/**
 * Concrete class for handling presentations/Views
 */
public class Presenter implements PresenterInterface {

	public static final String VIEWS = "views";
	public static final String LAYOUT_NAME = "layoutName";
	public static final String VIEW_TYPE = "viewType";
	public static final String FIELD_MODELS = "fieldModels";
	public static final String FIELD_NAME = "fieldName";
	public static final String FIELD_TYPE = "fieldType";
	public static final String MODELS = "models";
	public static final String CLASS_NAME = "className";
	public static final String VIEW_NAME = "viewName";
	public static final String MAIN_VIEW = "mainView";
	public static final String FIRST_VIEW = "firstView";
	protected Stack<ViewInterface> views = new Stack<ViewInterface>();
	protected List<ViewModel> viewModels = new ArrayList<ViewModel>();
	protected LayoutInflater layoutInflater;
	protected ViewGroup mainLayout;
	protected ViewModel viewModel;
	protected ViewModel mainViewModel;
	protected ViewInterface mainView;
	protected Context context;
	protected Map<ViewModel, ViewInterface> viewMap = new HashMap<ViewModel, ViewInterface>();

	/**
	 * Load the view from the given view model.
	 * Also load the view's models and fields.
	 * @param viewModel
	 * @return ViewInterface
	 */
	@Override
	public ViewInterface loadView(ViewModel viewModel) {
		if (viewModel == null) {
			Logger.error("loadView model is null");
			return null;
		}
		try {
			Class viewClass = viewModel.getViewType();
			int id = viewModel.getViewResourceId();
			final ViewInterface viewInterface = (ViewInterface) viewClass.newInstance();
			viewModel.setViewInterface(viewInterface);
			viewInterface.setPresenter(this);
			viewInterface.setup(context, layoutInflater);
			viewInterface.create(id);

			final List<ViewDataModel> viewModels = viewModel.getViewModels();
			if (viewModels != null) {
				List<ModelInterface> models = new ArrayList<ModelInterface>();
				for (ViewDataModel dataModel : viewModels) {
					if (dataModel.getModelClass() != null) {
						final ModelInterface model = (ModelInterface) dataModel.getModelClass().newInstance();
						dataModel.setModel(model);
						models.add(model);
					}
				}
				viewInterface.setModels(models);
			}

			final List<FieldModel> fieldModels = viewModel.getFieldModels();
			viewInterface.setFields(fieldModels);
			viewInterface.fillView();
			viewMap.put(viewModel, viewInterface);
			return viewInterface;
		} catch (InstantiationException e) {
			Logger.error("Problems creating view of type " + viewModel.getClass().getName(), e);
		} catch (IllegalAccessException e) {
			Logger.error("Problems creating view of type " + viewModel.getClass().getName(), e);
		}
		return null;
	}

	@Override
	public void dropView(ViewInterface view) {
		view.shutdown();
		views.remove(view);
	}

	@Override
	public void shutdown() {
		for (ViewInterface view : views) {
			view.shutdown();
		}
		views.clear();
	}

	public ViewInterface findViewByModel(ViewModel viewModel) {
		return viewMap.get(viewModel);
	}

	@Override
	public void goToView(ViewInterface view) {
		addViewToLayout(view);
	}

	/**
	 * Set the context & layout inflator
	 * @param context
	 * @param layoutInflator
	 */
	@Override
	public void setup(Context context, LayoutInflater layoutInflator) {
		this.context = context;
		this.layoutInflater = layoutInflator;
	}

	@Override
	public Context getContext() {
		return context;
	}

	@Override
	public LayoutInflater getLayoutInflater() {
		return layoutInflater;
	}

	@Override
	public void setMainViewModel(ViewModel viewModel) {
		this.mainViewModel = viewModel;
	}

	@Override
	public ViewModel getMainViewModel() {
		return mainViewModel;
	}

	@Override
	public ViewInterface findView(Class viewClass) {
		for (ViewInterface view : views) {
			if (viewClass.equals(view.getClass())) {
				return view;
			}
		}
		for (ViewModel model : viewModels) {
			if (model.getViewType().equals(viewClass)) {
				ViewInterface viewInterface = model.getViewInterface();
				if (viewInterface == null) {
					viewInterface = loadView(model);
				}
				return viewInterface;
			}
		}
		Logger.error("Could not find view " + viewClass.getSimpleName());
		return null;
	}

	@Override
	public void setViewModel(ViewModel viewModel) {
		if (viewModel == null) {
			Logger.error("ViewModel is null");
		}
		this.viewModel = viewModel;
	}

	@Override
	public ViewModel getViewModel() {
		return viewModel;
	}

	/**
	 * Find the view model with the given layout name
	 * @param layoutName
	 * @return ViewModel
	 */
	public ViewModel findViewModelByLayout(String layoutName) {
		for (ViewModel model : viewModels) {
			if (layoutName.equalsIgnoreCase(model.getLayoutName())) {
				return model;
			}
		}
		Logger.error("ViewModel not found");
		return null;
	}

	/**
	 * Find a view model by it's view name
	 * @param viewName
	 * @return
	 */
	public ViewModel findViewModelByName(String viewName) {
		for (ViewModel model : viewModels) {
			if (viewName.equalsIgnoreCase(model.getViewName())) {
				return model;
			}
		}
		Logger.error("ViewModel not found");
		return null;
	}

	@Override
	public void setMainLayout(ViewGroup mainLayout) {
		this.mainLayout = mainLayout;
	}

	public 	void setMainView(ViewInterface mainView) {
		this.mainView = mainView;
	}

	/**
	 * Call this method to start loading layouts.
	 * @param layoutView
	 * @param startViewModel
	 */
	@Override
	public void start(ViewGroup layoutView, ViewModel startViewModel) {
		mainLayout = layoutView;
		if (mainViewModel != null) {
			mainView = loadView(mainViewModel);
			mainLayout.addView(mainView.getView(), PresenterUtils.getFrameLayout());
		}
		if (startViewModel != null) {
			final ViewInterface view = loadView(startViewModel);
			addViewToLayout(view);
			setViewModel(startViewModel);
		}
	}

	/**
	 * Add the view to the main layout
	 * @param view
	 */
	public void addViewToLayout(ViewInterface view) {
		if (mainView != null) {
			mainView.addView(view);
		} else if (mainLayout != null) {
			mainLayout.removeAllViews();
			mainLayout.addView(view.getView(), PresenterUtils.getFrameLayout());
		} else {
			Logger.error("No Main View exists");
		}
		views.add(view);
	}

	@Override
	public void saveLayouts() {
		for (ViewInterface view : viewMap.values()) {
			view.saveData();
		}
	}

	@Override
	public void loadLayouts() {
		for (ViewInterface view : views) {
			view.loadData();
		}
	}

	@Override
	public boolean handleBack() {
		if (views.size() > 1) {
			views.pop(); // pop current
			addViewToLayout(views.pop()); // Will be added back on
			return true;
		}
		return false;
	}

	@Override
	public ViewInterface getMainView() {
		return mainView;
	}

	/**
	 * Read in a json file that has all the views associated with this app
	 * @param context
	 * @param json
	 */
	public void readConfiguration(Context context, String json) {
		try {
			final Resources resources = context.getResources();
			JSONData jsonData = new JSONData(json);
			if (jsonData.has(VIEWS)) {
				JSONData views = jsonData.findChild(VIEWS);
				List<JSONData> children = views.getChildren();
				for (JSONData childData : children) {
					ViewModel viewModel = new ViewModel();
					viewModels.add(viewModel);
					if (childData.has(VIEW_NAME)) {
						final String viewName = childData.getChildString(VIEW_NAME);
						viewModel.setViewName(viewName);
					}
					if (childData.has(LAYOUT_NAME)) {
						final String layout_name = childData.getChildString(LAYOUT_NAME);
						final int layoutId = resources.getIdentifier(layout_name, "layout", context.getPackageName());
						viewModel.setLayoutName(layout_name);
						viewModel.setViewResourceId(layoutId);
					}
					if (childData.has(VIEW_TYPE)) {
						final String view_type = childData.getChildString(VIEW_TYPE);
						Class viewClass = Class.forName(view_type);
						viewModel.setViewType(viewClass);
					}
					if (childData.has(MAIN_VIEW)) {
						final boolean mainView = childData.getChildBoolean(MAIN_VIEW);
						viewModel.setMainView(mainView);
					}
					if (childData.has(FIRST_VIEW)) {
						final boolean firstView = childData.getChildBoolean(FIRST_VIEW);
						viewModel.setFirstView(firstView);
					}
					if (viewModel.isFirstView()) {
						setViewModel(viewModel);
					}
					if (viewModel.isMainView()) {
						setMainViewModel(viewModel);
					}
					if (childData.has(FIELD_MODELS)) {
						JSONData fields = jsonData.findChild(FIELD_MODELS);
						children = fields.getChildren();
						for (JSONData fieldData : children) {
							FieldModel fieldModel = new FieldModel();
							viewModel.addField(fieldModel);
							if (fieldData.has(FIELD_NAME)) {
								fieldModel.setFieldName(fieldData.getChildString(FIELD_NAME));
								final int fieldId = resources.getIdentifier(fieldModel.getFieldName(), "id", context.getPackageName());
								fieldModel.setFieldResourceId(fieldId);
							}
							if (fieldData.has(FIELD_TYPE)) {
								final String fieldType = fieldData.getChildString(FIELD_TYPE);
								Class fieldClass = Class.forName(fieldType);
								fieldModel.setFieldType(fieldClass);
							}
						}
					}
					if (childData.has(MODELS)) {
						JSONData fields = jsonData.findChild(MODELS);
						children = fields.getChildren();
						for (JSONData fieldData : children) {
							ViewDataModel viewDataModel = new ViewDataModel();
							viewModel.addViewModel(viewDataModel);
							if (fieldData.has(CLASS_NAME)) {
								final String fieldType = fieldData.getChildString(CLASS_NAME);
								Class fieldClass = Class.forName(fieldType);
								viewDataModel.setModelClass(fieldClass);
							}
						}
					}
				}
			}
		} catch (JSONDataException e) {
			Logger.error("Problems parsing json data", e);
		} catch (ClassNotFoundException e) {
			Logger.error("Problems creating class file", e);
		}
	}

}
