package com.mastertechsoftware.views;

import android.graphics.Bitmap;
import android.view.ViewGroup;
/**
 *
 */
public interface DragableListListener {
    void drag(int from, int to);
    void drop(int from, int to);
    void remove(int which);
    Bitmap startDragging(int x, int y, ViewGroup item);
}
