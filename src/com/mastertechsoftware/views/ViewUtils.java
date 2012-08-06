package com.mastertechsoftware.views;

import android.view.View;
import android.view.ViewGroup;
/**
 * Misc View Utilities
 */
public class ViewUtils {

    /**
     * View a view that is a child of the viewgroup.
     * @param parent
     * @param id
     * @return found view
     */
    public static View findViewById(ViewGroup parent, int id) {
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = parent.getChildAt(i);
            if (childAt != null && childAt.getId() == id) {
                return childAt;
            }
            if (childAt instanceof ViewGroup) {
                View foundView = findViewById((ViewGroup) childAt, id);
                if (foundView != null) {
                    return foundView;
                }
            }
        }
        return null;
    }
}
