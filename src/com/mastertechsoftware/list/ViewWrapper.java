package com.mastertechsoftware.list;

import android.view.View;

import java.util.HashMap;
import java.util.Map;

public class ViewWrapper {
	private int position;
    private Map<Integer, View> views = new HashMap<Integer, View>();
    private Map<Integer, Object> viewData = new HashMap<Integer, Object>();
	private Object owner;

	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}

    public void setView(View view, int position) {
        views.put(position, view);
    }
    
    public View getView(int position) {
        return views.get(position);
    }
    public void setData(int position, Object data) {
        viewData.put(position, data);
    }

    public Object getData(int position) {
        return viewData.get(position);
    }

	public Object getOwner() {
		return owner;
	}

	public void setOwner(Object owner) {
		this.owner = owner;
	}
}
