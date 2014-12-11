package com.mastertechsoftware.activity;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
/**
 * Since many have to implement this, then create a default class
 */
public class DefaultCurrentActivityListener implements Application.ActivityLifecycleCallbacks, CurrentActivityListener {
    private Activity currentActivity;

    @Override
    public Activity getCurrentActivity() {
        return currentActivity;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

}
