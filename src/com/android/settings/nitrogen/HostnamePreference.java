/*
 * Copyright (C) 2013 The CyanogenMod project
 * Copyright (C) 2016 The OmniROM project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.nitrogen;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.android.settings.R;

import com.android.settings.CustomDialogPreference;

public class HostnamePreference extends CustomDialogPreference {

    private static final String TAG = "HostnamePreference";

    private static final String PROP_HOSTNAME = "net.hostname";

    private final String DEFAULT_HOSTNAME;
    private EditText mEditText;

    public HostnamePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // determine the default hostname
        String id = Settings.Secure.getString(getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        if (id != null && id.length() > 0) {
            DEFAULT_HOSTNAME =  "android-r-".concat(id);
        } else {
            DEFAULT_HOSTNAME = "";
        }

        // set layout
        setDialogLayoutResource(R.layout.preference_edit_text);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        setDialogIcon(null);
        setSummary(getText());
    }

    public HostnamePreference(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.editTextPreferenceStyle);
    }

    public HostnamePreference(Context context) {
        this(context, null);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        mEditText = (EditText) view.findViewById(R.id.edit_text);
        mEditText.setHint(DEFAULT_HOSTNAME);
        mEditText.setText(getText());
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            String hostname = mEditText.getText().toString();

            // remove any preceding or succeeding periods or hyphens
            hostname = hostname.replaceAll("(?:\\.|-)+$", "");
            hostname = hostname.replaceAll("^(?:\\.|-)+", "");
            // remove any character that is not alphanumeric, period, or hyphen
            hostname = hostname.replaceAll("[^-.a-zA-Z0-9]", "");

            if (hostname.length() == 0) {
                if (DEFAULT_HOSTNAME.length() != 0) {
                    // if no hostname is given, use the default
                    hostname = DEFAULT_HOSTNAME;
                } else {
                    // if no other name can be determined
                    // fall back on the current hostname
                    hostname = getText();
                }
            }
            setText(hostname);
        }
    }

    public void setText(String text) {
        if (text == null) {
            Log.e(TAG, "tried to set null hostname, request ignored");
            return;
        } else if (text.length() == 0) {
            Log.w(TAG, "setting empty hostname");
        } else {
            Log.i(TAG, "hostname has been set: " + text);
        }
        SystemProperties.set(PROP_HOSTNAME, text);
        persistHostname(text);
        setSummary(text);
    }

    public String getText() {
        return SystemProperties.get(PROP_HOSTNAME);
    }

    @Override
    public void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        String hostname = getText();
        persistHostname(hostname);
    }

    public void persistHostname(String hostname) {
        Settings.Secure.putString(getContext().getContentResolver(),
                Settings.Secure.DEVICE_HOSTNAME, hostname);
    }
}
