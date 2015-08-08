package com.mastertechsoftware.mvp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.mastertechsoftware.AndroidUtil.R;
import com.mastertechsoftware.mvp.presenter.PresenterInterface;
import com.mastertechsoftware.mvp.view.MainView;
/**
 * Main MVP Activity. Handles Activity/View Lifecycle
 */
public abstract class MVPActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener {
	protected PresenterInterface presenter;
	protected ViewGroup mvp_layout;
	protected FrameLayout frameLayout;
	protected boolean firstTime = true;
	protected Toolbar toolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mvp_layout);
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		if (toolbar != null) {
			toolbar.setOnMenuItemClickListener(this);
		}
		presenter = getPresenter();
		presenter.setup(this, getLayoutInflater());
		mvp_layout = (ViewGroup) findViewById(R.id.mvp_layout);
		frameLayout = (FrameLayout) findViewById(R.id.frameLayout);
		MainView mainView = new MainView();
		mainView.setFrameLayout(frameLayout);
		presenter.setMainView(mainView);
		presenter.start(frameLayout, presenter.getViewModel());
		fillModels(presenter);
	}

	@Override
	public void onBackPressed() {
		if (!presenter.handleBack()) {
			super.onBackPressed();
		}
	}

	public Toolbar getToolbar() {
		return toolbar;
	}

	public void setMenu(int menuId) {
		if (toolbar != null) {
			toolbar.inflateMenu(menuId);
		}
	}

	public void setTitle(int stringId) {
		if (toolbar != null) {
			toolbar.setTitle(stringId);
		}
	}

	/**
	 * Subclasses should create & fill their own presenter.
	 * You can use the Presenter class or subclass it
	 * @return
	 */
	public abstract PresenterInterface getPresenter();

	/**
	 * Once a Presenter is created & Set up. The subclass need to fill in the models with data
	 * @param presenter
	 */
	public abstract void fillModels(PresenterInterface presenter);

	@Override
	protected void onDestroy() {
		super.onDestroy();
		presenter.shutdown();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onPause() {
		super.onPause();
		presenter.saveLayouts();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!firstTime) {
			presenter.loadLayouts();
		}
		firstTime = false;
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		return false;
	}
}
