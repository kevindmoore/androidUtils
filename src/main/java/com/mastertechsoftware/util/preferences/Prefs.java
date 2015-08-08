package com.mastertechsoftware.util.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.mastertechsoftware.json.JSONData;
import com.mastertechsoftware.util.log.Logger;
/**
 * Handle System Preferences
 */
public class Prefs {
    protected static Context mContext;
    private static Prefs singleton;
    private static SharedPreferences mSharedPreferences;

    public static Prefs getPrefs() {
        if (singleton == null) {
            singleton = new Prefs();
        }
        return singleton;
    }

    /**
     * This should be called in your application 1x. It could be called in an activity but if that activity goes away
     * then this context won't be valid in other places.
     * @param context
     */
    public static void setContext(Context context) {
        mContext = context;
        mSharedPreferences = null; // Cause a new one to be created
        setupSharedPrefs();
    }

    private Prefs() {
        setupSharedPrefs();
    }

    private static void setupSharedPrefs() {
        if (mSharedPreferences == null && mContext != null) {
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        if (!checkSharedPreferences()) {
            return false;
        }
        return mSharedPreferences.getBoolean(key, defaultValue);
    }

    public boolean getBoolean(String key) {
        if (!checkSharedPreferences()) {
            return false;
        }
        return mSharedPreferences.getBoolean(key, false);
    }


    public String getString(String key, String defaultValue) {
        if (!checkSharedPreferences()) {
            return null;
        }
        return mSharedPreferences.getString(key, defaultValue);
    }

    public String getString(String key) {
        if (!checkSharedPreferences()) {
            return null;
        }
        return mSharedPreferences.getString(key, null);
    }


    public int getInt(String key, int defaultValue) {
        if (!checkSharedPreferences()) {
            return 0;
        }
        return mSharedPreferences.getInt(key, defaultValue);
    }

    public int getInt(String key) {
        if (!checkSharedPreferences()) {
            return 0;
        }
        return mSharedPreferences.getInt(key, 0);
    }


    public long getLong(String key, long defaultValue) {
        if (!checkSharedPreferences()) {
            return 0;
        }
        return mSharedPreferences.getLong(key, defaultValue);
    }

    public long getLong(String key) {
        if (!checkSharedPreferences()) {
            return 0;
        }
        return mSharedPreferences.getLong(key, 0);
    }


    public Float getFloat(String key) {
        if (!checkSharedPreferences()) {
            return null;
        }
        return mSharedPreferences.getFloat(key, 0);
    }

    public Double getDouble(String key) {
        if (!checkSharedPreferences()) {
            return null;
        }
        return Double.valueOf(mSharedPreferences.getFloat(key, 0));
    }

	public Object getObject(String key, Class type) {
		if (!checkSharedPreferences()) {
			return null;
		}
		String stringObject = mSharedPreferences.getString(key, null);
		if (!TextUtils.isEmpty(stringObject)) {
			return JSONData.convertFromJSON(stringObject, type);
		}
		return null;
	}

    public void putBoolean(String key, boolean value) {
        if (!checkSharedPreferences()) {
            return;
        }
        mSharedPreferences.edit().putBoolean(key, value).commit();
    }

    public void putString(String key, String value) {
        if (!checkSharedPreferences()) {
            return;
        }
        mSharedPreferences.edit().putString(key, value).commit();
    }

    public void putInt(String key, int value) {
        if (!checkSharedPreferences()) {
            return;
        }
        mSharedPreferences.edit().putInt(key, value).commit();
    }

    public void putLong(String key, long value) {
        if (!checkSharedPreferences()) {
            return;
        }
        mSharedPreferences.edit().putLong(key, value).commit();
    }

	public void putObject(String key, Object object) {
		if (!checkSharedPreferences()) {
			return;
		}
		mSharedPreferences.edit().putString(key, JSONData.convertToJSON(object)).commit();

	}

	public void removePref(String key) {
		if (!checkSharedPreferences()) {
			return;
		}
		mSharedPreferences.edit().remove(key).commit();
	}

    private boolean checkSharedPreferences() {
        if (mSharedPreferences == null) {
            Logger.error(this, "Shared Preferences not set");
            return false;
        }
        return true;
    }

    public void putFloat(String key, Float value) {
        if (!checkSharedPreferences()) {
            return;
        }
        mSharedPreferences.edit().putFloat(key, value).commit();
    }

    public void putDouble(String key, Double value) {
        if (!checkSharedPreferences()) {
            return;
        }
        mSharedPreferences.edit().putFloat(key, value.floatValue()).commit();

    }
}
