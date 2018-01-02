/*
 * Copyright (C) 2017 CypherOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.aoscp.display;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.util.ArrayMap;
import android.util.Log;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.display.ThemePreferenceController;
import com.android.settings.widget.RadioButtonPreference;

import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ColorManagerFragment extends DashboardFragment
        implements RadioButtonPreference.OnClickListener {

    private static final String TAG = "ColorManagerSettings";

    private static final String KEY_THEME_AUTO = "theme_auto";
    private static final String KEY_THEME_LIGHT = "theme_light";
    private static final String KEY_THEME_DARK = "theme_dark";
    private static final String KEY_THEME_BLACK = "theme_black";

    List<RadioButtonPreference> mThemes = new ArrayList<>();

    private Context mContext;

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.DISPLAY;
    }
    
    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.color_manager_settings;
    }

    @Override
    protected List<AbstractPreferenceController> getPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getLifecycle());
    }

    @Override
    public void displayResourceTiles() {
        final int resId = getPreferenceScreenResId();
        if (resId <= 0) {
            return;
        }
        addPreferencesFromResource(resId);
        final PreferenceScreen screen = getPreferenceScreen();
        Collection<AbstractPreferenceController> controllers = mPreferenceControllers.values();
        for (AbstractPreferenceController controller : controllers) {
            controller.displayPreference(screen);
        }

        for (int i = 0; i < screen.getPreferenceCount(); i++) {
            Preference pref = screen.getPreference(i);
            if (pref instanceof RadioButtonPreference) {
                RadioButtonPreference themePref = (RadioButtonPreference) pref;
                themePref.setOnClickListener(this);
                mThemes.add(themePref);
            }
        }

        switch (Settings.Secure.getInt(getContentResolver(), Settings.Secure.DEVICE_THEME, 0)) {
            case 0:
                updateThemeItems(KEY_THEME_AUTO);
                break;
            case 1:
                updateThemeItems(KEY_THEME_LIGHT);
                break;
            case 2:
                updateThemeItems(KEY_THEME_DARK);
                break;
            case 3:
                updateThemeItems(KEY_THEME_BLACK);
                break;
        }
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, Lifecycle lifecycle) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new ThemePreferenceController(context));
        return controllers;
    }

    private void updateThemeItems(String selectionKey) {
        for (RadioButtonPreference pref : mThemes) {
            if (selectionKey.equals(pref.getKey())) {
                pref.setChecked(true);
            } else {
                pref.setChecked(false);
            }
        }
    }

    @Override
    public void onRadioButtonClicked(RadioButtonPreference pref) {
        switch (pref.getKey()) {
            case KEY_THEME_AUTO:
                Settings.Secure.putInt(getContentResolver(), 
                         Settings.Secure.DEVICE_THEME, 0);
                break;
            case KEY_THEME_LIGHT:
                Settings.Secure.putInt(getContentResolver(), 
                         Settings.Secure.DEVICE_THEME, 1);
                break;
            case KEY_THEME_DARK:
                Settings.Secure.putInt(getContentResolver(), 
                         Settings.Secure.DEVICE_THEME, 2);
                break;
            case KEY_THEME_BLACK:
                Settings.Secure.putInt(getContentResolver(), 
                         Settings.Secure.DEVICE_THEME, 3);
                break;
        }
        updateThemeItems(pref.getKey());
    }
}
