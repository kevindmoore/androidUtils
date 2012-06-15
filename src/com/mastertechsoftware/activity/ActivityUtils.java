package com.mastertechsoftware.activity;

import android.app.Activity;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;
/**
 * Helper Utilities for Activities
 */
public class ActivityUtils {
    /**
     * Hide any keyboard that might be open
     */
    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodService = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodService != null && inputMethodService.isActive()) {
            inputMethodService.hideSoftInputFromWindow(
                    activity.getWindow().getDecorView().getWindowToken(), 0);
        }
    }

}
