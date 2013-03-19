package com.mastertechsoftware.list;

import android.app.Activity;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Date: Nov 1, 2010
 */
public abstract class SimpleSectionedAdapter extends SectionedAdapter implements View.OnClickListener {
    protected DataSetObservable mDataSetObservable = new DataSetObservable();
    protected Activity activity;

    public SimpleSectionedAdapter(Activity activity) {
        this.activity = activity;
    }

    public void registerDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.registerObserver(observer);
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.unregisterObserver(observer);
    }

    /**
     * Notifies the attached View that the underlying data has been changed
     * and it should refresh itself.
     */
    public void notifyDataSetChanged() {
        mDataSetObservable.notifyChanged();
    }

    public void notifyDataSetInvalidated() {
        mDataSetObservable.notifyInvalidated();
    }

    public DataSetObservable getDataSetObservable() {
        return mDataSetObservable;
    }

    public void setDataSetObservable(DataSetObservable dataSetObservable) {
        mDataSetObservable = dataSetObservable;
    }

    protected abstract View getHeaderView(String caption, int position,
                                                                View convertView,
                                                                ViewGroup parent);

    @Override
    public void onClick(View v) {
        if (v != null && v instanceof TextView) {
            int position = (Integer)v.getTag();
            Section section = getSectionAtIndex(position);
            if (section != null) {
                if (section.isCollapsed() || section.subSectionsCollapsed()) {
                    section.setCollapsed(false);
                } else {
                    section.setCollapsed(true);
                }
                notifyDataSetChanged();
            }
        }

    }
}
