package com.mastertechsoftware.list;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Date: Nov 1, 2010
 */
public class TextAdapter<T> extends ArrayAdapter<T> {
    private int section;

    public TextAdapter(int section, Context context, int resource, int textViewResourceId, List<T> objects) {
        super(context, resource, textViewResourceId, objects);
        this.section = section;
    }

    public TextAdapter(int section, Context context, int resource, int textViewResourceId, T[] objects) {
        super(context, resource, textViewResourceId, objects);
        this.section = section;
    }

    public TextAdapter(int section, Context context, int resource, T[] objects) {
        super(context, resource, objects);
        this.section = section;
    }

    public TextAdapter(int section, Context context, int resource, List<T> objects) {
        super(context, resource, objects);
        this.section = section;
    }

    public TextAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

    public TextAdapter(Context context, int resource, int textViewResourceId, List<T> objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public TextAdapter(Context context, int resource, int textViewResourceId, T[] objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public TextAdapter(Context context, int resource) {
        super(context, resource);
    }

    public TextAdapter(Context context, int resource, List<T> objects) {
        super(context, resource, objects);
    }

    public TextAdapter(Context context, int resource, T[] objects) {
        super(context, resource, objects);
    }

    public int getSection() {
        return section;
    }
}
