package com.android.settings.touch;

import android.content.Context;
import android.content.IntentFilter;
import android.os.SystemProperties;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.core.TogglePreferenceController;

public class TouchSensitivityPreferenceController extends TogglePreferenceController {

    private static final String TOUCH_SENSITIVITY_ENABLED = "touch_sensitivity_enabled";
    private static final String TOUCH_SENSITIVITY_PROP = "debug.touch_sensitivity_mode";

    private static final int ON = 1;
    private static final int OFF = 0;

    public TouchSensitivityPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return mContext.getResources().getBoolean(R.bool.config_show_touch_sensitivity)
                ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public boolean isChecked() {
        return Settings.Secure.getInt(mContext.getContentResolver(),
                TOUCH_SENSITIVITY_ENABLED, OFF) == ON;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        Settings.Secure.putInt(mContext.getContentResolver(),
                TOUCH_SENSITIVITY_ENABLED, isChecked ? ON : OFF);
        SystemProperties.set(TOUCH_SENSITIVITY_PROP, isChecked ? "1" : "0");
        return true;
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return R.string.menu_key_display;
    }
}
