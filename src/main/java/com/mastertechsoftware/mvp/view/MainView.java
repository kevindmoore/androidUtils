package com.mastertechsoftware.mvp.view;

import android.widget.FrameLayout;

import com.mastertechsoftware.AndroidUtil.R;
import com.mastertechsoftware.mvp.presenter.PresenterUtils;
/**
 * Class that holds the Framelayout used to remove/add all user views
 */
public class MainView extends MVPView {
	protected FrameLayout frameLayout;

	@Override
	public void addView(ViewInterface view) {
		frameLayout.removeAllViews();
		frameLayout.addView(view.getView(), PresenterUtils.getFrameLayout());
	}

	@Override
	public void removeView(ViewInterface view) {
		frameLayout.removeView(view.getView());
	}

	@Override
	public void removeViews() {
		frameLayout.removeAllViews();
	}

	@Override
	public void mapViews() {
		if (currentView != null) {
			frameLayout = (FrameLayout) currentView.findViewById(R.id.frameLayout);
		}
	}

	public void setFrameLayout(FrameLayout frameLayout) {
		this.frameLayout = frameLayout;
		currentView = frameLayout;
	}

	@Override
	public void mapListeners() {

	}

}
