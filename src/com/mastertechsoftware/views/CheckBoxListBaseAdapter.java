package com.mastertechsoftware.views;

import com.mastertechsoftware.list.ViewWrapper;
import com.mastertechsoftware.util.log.Logger;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * User: kevin.moore
 */
public abstract class CheckBoxListBaseAdapter extends BaseAdapter implements CompoundButton.OnCheckedChangeListener {
	private static final int CHECKED_DATA_POSITION = 0;
	private static final int VIEW_POSITION = 0;
	private int numViews = 6;
	protected HashMap<Integer, ViewWrapper> adapterData = new HashMap<Integer, ViewWrapper>();
	protected View.OnClickListener clickListener;
	protected boolean debugging = false;
	protected boolean checkBoxOnLeft = true;


	protected CheckBoxListBaseAdapter(View.OnClickListener clickListener) {
		this.clickListener = clickListener;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}


	@Override
	public int getItemViewType(int position) {
		return position % numViews;
	}

	@Override
	public int getViewTypeCount() {
		return numViews;
	}

	public void setCheckBoxOnLeft(boolean checkBoxOnLeft) {
		this.checkBoxOnLeft = checkBoxOnLeft;
	}

	protected abstract void setViewData(int position, CheckboxText checkboxText, ViewGroup parent);

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewWrapper wrapper = adapterData.get(position);
		CheckboxText checkboxText;
		boolean checked = false;
		if (wrapper == null) {
			if (debugging)
				Logger.debug("Creating wrapper for position " + position);
			wrapper = new ViewWrapper();
			wrapper.setPosition(position);
			adapterData.put(position, wrapper);
		}
		if (convertView == null) {
			checkboxText = new CheckboxText(parent.getContext(), checkBoxOnLeft);
			if (clickListener != null) {
				checkboxText.setTextOnClickListener(clickListener);
			}
			checkboxText.setOnCheckedListener(this);
			convertView = checkboxText;
			wrapper.setData(CHECKED_DATA_POSITION, false);
			if (debugging)
				Logger.debug("Creating Checkbox text for position " + position);
		} else {
			checkboxText = (CheckboxText) convertView;
			if (debugging)
				Logger.debug("Found existing checkbox text for position " + position);
			Object data = wrapper.getData(CHECKED_DATA_POSITION);
			if (data != null && data instanceof Boolean) {
				if (debugging)
					Logger.debug("Wrapper data exists for position " + position);
				checked = (Boolean)data;
			}
		}
		checkboxText.setTag(position);
		checkboxText.getTextView().setTag(position);
		checkboxText.getCheckBox().setTag(position);
		wrapper.setView(checkboxText.getCheckBox(), VIEW_POSITION);
		checkboxText.setChecked(checked);

		setViewData(position, checkboxText, parent);

		return convertView;
	}

	public ViewWrapper getViewWrapper(int position) {
		return adapterData.get(position);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		int size = adapterData.size();
		if (debugging)
			Logger.debug("onCheckedChanged: looking for wrapper " + buttonView);
		for (int i=0; i < size; i++) {
			ViewWrapper wrapper = adapterData.get(i);
			if (debugging)
				Logger.debug("onCheckedChanged: wrapper view " + wrapper.getView(VIEW_POSITION) + " for position " + i);
			if (wrapper.getView(VIEW_POSITION) == buttonView) {
				if (debugging)
					Logger.debug("onCheckedChanged: Found wrapper for position " + i);
				wrapper.setData(CHECKED_DATA_POSITION, isChecked);
				break;
			}
		}
	}

	public List<Integer> getCheckedItems() {
		List<Integer> checkedItems = new ArrayList<Integer>();
		int size = adapterData.size();
		if (debugging)
			Logger.debug("getCheckedItems: " + size);
		for (int i=0; i < size; i++) {
			ViewWrapper wrapper = adapterData.get(i);
			Object data = wrapper.getData(CHECKED_DATA_POSITION);
			if (data != null && data instanceof Boolean && (Boolean) data) {
				checkedItems.add(i);
			}
		}
		return checkedItems;
	}
}
