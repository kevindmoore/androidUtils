package com.mastertechsoftware.phone;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * User: Kevin
 * Date: Feb 22, 2010
 */
public class PhoneStateHandler extends PhoneStateListener {
    /**
     * The phone state retrieval object.
     */
    private TelephonyManager manager = null;
    private Context context;
    private List<PhoneListener> phoneListeners = new ArrayList<PhoneListener>();
    /**
     * If <code>true</code> then the phone is active (ringing or picked up).
     */
    private volatile boolean phoneActive = false;
    private static PhoneStateHandler phoneStateHandler;

    public static PhoneStateHandler getPhoneStateHandler(Context context) {
        if (phoneStateHandler == null) {
            phoneStateHandler = new PhoneStateHandler(context);
        }

        return phoneStateHandler;
    }

    /**
     * Constructor.
     * @param context
     */
    private PhoneStateHandler(Context context) {
        this.context = context;
        manager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        manager.listen(this, LISTEN_CALL_STATE);
    }

    /**
     * Add Listener
     * @param phoneListener
     */
    public void addPhoneListener(PhoneListener phoneListener) {
        phoneListeners.add(phoneListener);
    }

    /**
     * Remove Phone Listener
     * @param phoneListener
     */
    public void removePhoneListener(PhoneListener phoneListener) {
        phoneListeners.remove(phoneListener);
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        super.onCallStateChanged(state, incomingNumber);
        switch (state) {
            case TelephonyManager.CALL_STATE_OFFHOOK:
            case TelephonyManager.CALL_STATE_RINGING:
                callInitiated();
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                callTerminated();
                break;
        }
    }

    /**
     * The call is initiated, Activities can stop what they are doing
     */
    private void callInitiated() {
        if (phoneActive)
            return;
        phoneActive = true;
        Iterator<PhoneListener> iter = phoneListeners.iterator();
        while (iter.hasNext()) {
            iter.next().callStarted();
        }
    }

    /**
     * The call has ended, Activities can resume what they are doing
     */
    private void callTerminated() {
        if (!phoneActive)
            return;
        phoneActive = false;
        Iterator<PhoneListener> iter = phoneListeners.iterator();
        while (iter.hasNext()) {
            iter.next().callEnded();
        }
    }
}
