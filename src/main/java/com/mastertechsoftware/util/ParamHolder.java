package com.mastertechsoftware.util;

import java.util.ArrayList;
import java.util.List;

public class ParamHolder {
	protected Object result;
	protected List<Object> params = new ArrayList<Object>();
	protected List<Object> results = new ArrayList<Object>();

	public Object getResult() {
		return result;
	}
	public void setResult(Object result) {
		this.result = result;
	}
	public List<Object> getParams() {
		return params;
	}
	public void setParams(List<Object> params) {
		this.params = params;
	}

    public Object getParam(int index) {
        if (params.size() < index) {
            return null;
        }
        return params.get(index);
    }
    public void clearParams() {
        params.clear();
    }
	public void addParam(Object param) {
		params.add(param);
	}
	
	public List<Object> getResults() {
		return results;
	}
	
	public void addResult(Object result) {
		results.add(result);
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ParamHolder)) {
            return false;
        }

        ParamHolder other = (ParamHolder)o;
        if (!other.params.equals(params)) {
            return false;
        }
        if (!other.results.equals(results)) {
            return false;
        }
        return (result == null ? other.result == null : result.equals(other.result));
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + params.hashCode();
        hash = 31 * hash + results.hashCode();
        if (result != null) {
            hash = 31 * hash + result.hashCode();
        }
        return hash;
    }

}
