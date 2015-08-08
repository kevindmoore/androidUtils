package com.mastertechsoftware.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import com.mastertechsoftware.util.log.Logger;

import java.util.ArrayList;
import java.util.List;
/**
 * Copy of SlidingTabLayout that just has a tab strip
 */
public class TabStrip extends HorizontalScrollView {
	protected static final int TITLE_OFFSET_DIPS = 24;
	protected static final int TAB_VIEW_PADDING_DIPS = 16;
	protected static final int TAB_VIEW_TEXT_SIZE_SP = 12;

	protected int mTitleOffset;
	protected final SlidingTabStrip mTabStrip;
	protected TabStripListener tabStripListener;
	protected TabClickListener tabClickListener;
	protected List<TextView> tabViews = new ArrayList<TextView>();

	public interface TabStripListener {
		void tabClicked(int position, String title);
	}
	public TabStrip(Context context) {
		this(context, null);
	}

	public TabStrip(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TabStrip(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// Disable the Scroll Bar
		setHorizontalScrollBarEnabled(false);
		// Make sure that the Tab Strips fills this View
		setFillViewport(true);

		mTitleOffset = (int) (TITLE_OFFSET_DIPS * getResources().getDisplayMetrics().density);

		tabClickListener = new TabClickListener();
		mTabStrip = new SlidingTabStrip(context);
		addView(mTabStrip, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	}

	public void setTabStripListener(TabStripListener tabStripListener) {
		this.tabStripListener = tabStripListener;
	}

	public void selectTab(int position) {
		mTabStrip.selectTab(position);
	}

	/**
	 * Add a new Tab
	 * @param title
	 */
	public void addTab(String title) {
		TextView tabTitleView = createDefaultTabView(getContext());
		tabTitleView.setText(title);
		tabTitleView.setTag(title);
		tabTitleView.setOnClickListener(tabClickListener);
		tabViews.add(tabTitleView);

		mTabStrip.addView(tabTitleView);
		mTabStrip.selectTab(mTabStrip.getChildCount() - 1);
	}

	/**
	 * Rename a tab at the given position
	 * @param title
	 * @param position
	 */
	public void renameTab(String title, int position) {
		final TextView tabTitleView = (TextView) mTabStrip.getChildAt(position);
		if (tabTitleView == null) {
			Logger.error("renameTab: No tab at position " + position);
			return;
		}
		tabTitleView.setText(title);
		tabTitleView.setTag(title);
	}

	/**
	 * Return how many tabs there are
	 * @return
	 */
	public int getTabCount() {
		return mTabStrip.getChildCount();
	}

	public void removeAllTabs() {
		mTabStrip.removeAllViews();
	}

	/**
	 * Create a default view to be used for tabs. This is called if a custom tab view is not set via
	 */
	protected TextView createDefaultTabView(Context context) {
		TextView textView = new TextView(context);
		textView.setGravity(Gravity.CENTER);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, TAB_VIEW_TEXT_SIZE_SP);
		textView.setTypeface(Typeface.DEFAULT_BOLD);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// If we're running on Honeycomb or newer, then we can use the Theme's
			// selectableItemBackground to ensure that the View has a pressed state
			TypedValue outValue = new TypedValue();
			getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground,
													 outValue, true);
			textView.setBackgroundResource(outValue.resourceId);
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			// If we're running on ICS or newer, enable all-caps to match the Action Bar tab style
			textView.setAllCaps(true);
		}

		int padding = (int) (TAB_VIEW_PADDING_DIPS * getResources().getDisplayMetrics().density);
		textView.setPadding(padding, padding, padding, padding);

		return textView;
	}

	private class TabClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			for (int i = 0; i < mTabStrip.getChildCount(); i++) {
				if (v == mTabStrip.getChildAt(i)) {
					if (tabStripListener != null) {
						tabStripListener.tabClicked(i, tabViews.get(i).getText().toString());
					}
					mTabStrip.onViewPagerPageChanged(i, 0);
					return;
				}
			}
		}
	}
}
