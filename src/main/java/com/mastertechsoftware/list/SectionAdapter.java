package com.mastertechsoftware.list;

import com.mastertechsoftware.layout.LayoutIDGenerator;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * User: kevin.moore
 */
public abstract class SectionAdapter<T> extends TextAdapter<T> {
	public static final String TEXT_VIEW = "textView";
	protected int listViewSize;

	public SectionAdapter(int section, Context context, List<T> objects) {
		super(section, context, 0, objects);
		listViewSize = (int) (64 * context.getResources().getDisplayMetrics().density);
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return getView(position, convertView, parent);
	}

	abstract public View createView(int position, View convertView, ViewGroup parent);


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = createView(position, convertView, parent);
		TextView textView = (TextView) convertView.findViewById(LayoutIDGenerator.getID(TEXT_VIEW));
		T item = getItem(position);
		if (item instanceof CharSequence) {
			textView.setText((CharSequence)item);
		} else {
			textView.setText(item.toString());
		}
		return convertView;
	}
}
