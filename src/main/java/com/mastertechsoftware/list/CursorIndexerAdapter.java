package com.mastertechsoftware.list;

import android.content.Context;
import android.database.Cursor;
import android.widget.AlphabetIndexer;
import android.widget.SectionIndexer;
import android.widget.SimpleCursorAdapter;

/**
 * Date: Jul 18, 2010
 */
public class CursorIndexerAdapter extends SimpleCursorAdapter implements SectionIndexer {
    private AlphabetIndexer indexer;
    private String alphabet = " 0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-.!@#$%^&*()_+=/?><,~`";
    /**
     * Constructor.
     *
     * @param context The context where the ListView associated with this
     *                SimpleListItemFactory is running
     * @param layout  resource identifier of a layout file that defines the views
     *                for this list item. The layout file should include at least
     *                those named views defined in "to"
     * @param c       The database cursor.  Can be null if the cursor is not available yet.
     * @param from    A list of column names representing the data to bind to the UI.  Can be null
     *                if the cursor is not available yet.
     * @param to      The views that should display column in the "from" parameter.
     *                These should all be TextViews. The first N views in this list
     *                are given the values of the first N columns in the from
     *                parameter.  Can be null if the cursor is not available yet.
	 * @param index   sortedColumnIndex
     */
    public CursorIndexerAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int index) {
        super(context, layout, c, from, to);
        indexer = new AlphabetIndexer(c, index, alphabet);
    }

    @Override
    public Object[] getSections() {
        return indexer.getSections();
    }

    @Override
    public int getPositionForSection(int section) {
        return indexer.getPositionForSection(section);
    }

    @Override
    public int getSectionForPosition(int position) {
        return indexer.getSectionForPosition(position);
    }
}
