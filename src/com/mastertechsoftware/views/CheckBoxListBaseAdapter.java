package com.mastertechsoftware.views;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;

import com.mastertechsoftware.list.ViewWrapper;
import com.mastertechsoftware.util.log.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * User: kevin.moore
 */
public abstract class CheckBoxListBaseAdapter extends BaseAdapter implements CompoundButton.OnCheckedChangeListener {
    protected static final int CHECKED_DATA_POSITION = 0;
    protected static final int VIEW_POSITION = 0;
    protected int numViews = 6;
	protected HashMap<Integer, ViewWrapper> adapterData = new HashMap<Integer, ViewWrapper>();
	protected View.OnClickListener clickListener;
	protected boolean debugging = false;
	protected boolean checkBoxOnLeft = true;
    protected boolean loading = false;
    protected Activity activity;


    public CheckBoxListBaseAdapter(Activity activity, View.OnClickListener clickListener) {
		this.activity = activity;
		this.clickListener = clickListener;
	}

    @Override
	public long getItemId(int position) {
		return position;
	}


	/**
	 * Call this when you remove an item so the corresponding data can bew removed
	 * @param position
	 */
	public void removeItem(int position) {
		adapterData.remove(position);
	}

	/**
	 * Items have changed clear our list
	 */
	protected void clearCheckboxes() {
		adapterData.clear();
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
		loading = true;
		if (wrapper == null) {
			if (debugging)
				Logger.debug("Creating wrapper for position " + position);
			wrapper = new ViewWrapper();
			wrapper.setPosition(position);
			adapterData.put(position, wrapper);
		}
		if (convertView == null) {
            checkboxText = createCheckboxView(parent);
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

		loading = false;
		return convertView;
	}

    /**
     * Create the Checkbox View. This can be overridden to provide a view other than the default
     * @param parent
     * @return CheckboxText
     */
    protected CheckboxText createCheckboxView(ViewGroup parent) {
        return new CheckboxText(parent.getContext(), checkBoxOnLeft);
    }

    public ViewWrapper getViewWrapper(int position) {
		return adapterData.get(position);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (loading) {
			return;
		}
		int position = 0;
		Object tag = buttonView.getTag();
		if (tag != null) {
			position = (Integer)tag;
		}
		int size = adapterData.size();
		if (position >= size) {
            ViewWrapper wrapper = new ViewWrapper();
            wrapper.setPosition(position);
            adapterData.put(position, wrapper);
		}
		if (debugging)
			Logger.debug("onCheckedChanged: looking for wrapper " + buttonView);
		ViewWrapper wrapper = adapterData.get(position);
		wrapper.setData(CHECKED_DATA_POSITION, isChecked);
	}

	public List<Integer> getCheckedItems() {
		List<Integer> checkedItems = new ArrayList<Integer>();
		int size = adapterData.size();
		if (debugging)
			Logger.debug("getCheckedItems: " + size);
		for (Integer position : adapterData.keySet()) {
			ViewWrapper wrapper = adapterData.get(position);
			Object data = wrapper.getData(CHECKED_DATA_POSITION);
			if (data != null && data instanceof Boolean && (Boolean) data) {
				checkedItems.add(position);
			}
		}
		return checkedItems;
	}
}
