package com.mastertechsoftware.list;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.mastertechsoftware.util.log.Logger;
/**
 * Class used to handle clicks for RecyclerView
 */
public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener
{
	public static interface OnItemClickListener
	{
		public void onItemClick(View view, int position);
		public void onItemLongClick(View view, int position);
	}

	private OnItemClickListener mListener;
	private GestureDetector mGestureDetector;

	public RecyclerItemClickListener(Context context, final RecyclerView recyclerView, OnItemClickListener listener)
	{
		mListener = listener;

		mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener()
		{
			@Override
			public boolean onSingleTapUp(MotionEvent e)
			{
				return true;
			}

			@Override
			public void onLongPress(MotionEvent e)
			{
				View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());

				if(childView != null && mListener != null)
				{
					mListener.onItemLongClick(childView, recyclerView.getChildPosition(childView));
				}
			}
		});
	}

	@Override
	public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e)
	{
		View mainLayout = view.findChildViewUnder(e.getX(), e.getY());

		switch (e.getAction()) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_UP: {
				if (mainLayout instanceof ViewGroup) {
					Rect hitRect = new Rect();
					ViewGroup viewGroup = ((ViewGroup) mainLayout);
					final int childCount = viewGroup.getChildCount();
					for (int i = 0; i < childCount; i++) {
						View childView = viewGroup.getChildAt(i);
						childView.getGlobalVisibleRect(hitRect);
						if (hitRect.contains((int)e.getRawX(), (int)e.getRawY()) && childView.hasOnClickListeners()) {
							Logger.debug("onInterceptTouchEvent. ChildView has click listeners");
							return false;
						}

					}
				} else if (mainLayout != null && mainLayout.hasOnClickListeners()) {
					Logger.debug("onInterceptTouchEvent. mainLayout has click listeners");
					return false;
				}
			}
		}
		if (mainLayout != null && mListener != null && mGestureDetector.onTouchEvent(e))
		{
			mListener.onItemClick(mainLayout, view.getChildPosition(mainLayout));
		}

		return false;
	}

	@Override
	public void onTouchEvent(RecyclerView view, MotionEvent motionEvent){}
}
