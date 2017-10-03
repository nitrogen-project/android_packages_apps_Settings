/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.settings.system;

import android.content.ComponentName;
import android.content.Context;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.android.internal.hardware.AmbientDisplayConfiguration;
import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.core.PreferenceController;
import com.android.settings.core.lifecycle.Lifecycle;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.gestures.AssistGesturePreferenceController;
import com.android.settings.gestures.DoubleTapPowerPreferenceController;
import com.android.settings.gestures.DoubleTapScreenPreferenceController;
import com.android.settings.gestures.DoubleTwistPreferenceController;
import com.android.settings.gestures.PickupGesturePreferenceController;
import com.android.settings.gestures.SwipeToNotificationPreferenceController;
import com.android.settings.search.BaseSearchIndexProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GesturesFragment extends DashboardFragment {

    private static final String TAG = "GesturesFragment";

    private static final String KEY_ASSIST = "gesture_assist_input_summary";
    private static final String KEY_SWIPE_DOWN = "gesture_swipe_down_fingerprint_input_summary";
    private static final String KEY_DOUBLE_TAP_POWER = "gesture_double_tap_power_input_summary";
    private static final String KEY_DOUBLE_TWIST = "gesture_double_twist_input_summary";
    private static final String KEY_DOUBLE_TAP_SCREEN = "gesture_double_tap_screen_input_summary";
    private static final String KEY_PICK_UP = "gesture_pick_up_input_summary";

    private AmbientDisplayConfiguration mAmbientDisplayConfig;

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.NITROGEN_SETTINGS;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.gestures_prefs;
    }

    @Override
    protected List<PreferenceController> getPreferenceControllers(Context context) {
        if (mAmbientDisplayConfig == null) {
            mAmbientDisplayConfig = new AmbientDisplayConfiguration(context);
        }

        return buildPreferenceControllers(context, getLifecycle(), mAmbientDisplayConfig);
    }

    private static List<PreferenceController> buildPreferenceControllers(@NonNull Context context,
            @Nullable Lifecycle lifecycle,
            @NonNull AmbientDisplayConfiguration ambientDisplayConfiguration) {
        final List<PreferenceController> controllers = new ArrayList<>();
        // Gestures
        controllers.add(new AssistGesturePreferenceController(context, lifecycle, KEY_ASSIST));
        controllers.add(new SwipeToNotificationPreferenceController(context, lifecycle,
                KEY_SWIPE_DOWN));
        controllers.add(new DoubleTwistPreferenceController(context, lifecycle, KEY_DOUBLE_TWIST));
        controllers.add(new DoubleTapPowerPreferenceController(context, lifecycle,
                KEY_DOUBLE_TAP_POWER));
        controllers.add(new PickupGesturePreferenceController(context, lifecycle,
                ambientDisplayConfiguration, UserHandle.myUserId(), KEY_PICK_UP));
        controllers.add(new DoubleTapScreenPreferenceController(context, lifecycle,
                ambientDisplayConfiguration, UserHandle.myUserId(), KEY_DOUBLE_TAP_SCREEN));
        return controllers;
    }

    @VisibleForTesting
    void setAmbientDisplayConfig(AmbientDisplayConfiguration ambientConfig) {
        mAmbientDisplayConfig = ambientConfig;
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    final ArrayList<SearchIndexableResource> result = new ArrayList<>();

                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.gestures_prefs;
                    result.add(sir);
                    return result;
                }

                @Override
                public List<PreferenceController> getPreferenceControllers(Context context) {
                    return buildPreferenceControllers(context, null,
                            new AmbientDisplayConfiguration(context));
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    // Duplicates in summary and details pages.
                    keys.add(KEY_ASSIST);
                    keys.add(KEY_SWIPE_DOWN);
                    keys.add(KEY_DOUBLE_TAP_POWER);
                    keys.add(KEY_DOUBLE_TWIST);
                    keys.add(KEY_DOUBLE_TAP_SCREEN);
                    keys.add(KEY_PICK_UP);

                    return keys;
                }
            };
}
