/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.settings.language;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.speech.tts.TtsEngines;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.applications.defaultapps.DefaultAutofillPreferenceController;
import com.android.settings.core.PreferenceController;
import com.android.settings.core.lifecycle.Lifecycle;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.inputmethod.GameControllerPreferenceController;
import com.android.settings.inputmethod.PhysicalKeyboardPreferenceController;
import com.android.settings.inputmethod.SpellCheckerPreferenceController;
import com.android.settings.inputmethod.VirtualKeyboardPreferenceController;
import com.android.settings.search.BaseSearchIndexProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LanguageAndInputSettings extends DashboardFragment {

    private static final String TAG = "LangAndInputSettings";

    private static final String KEY_TEXT_TO_SPEECH = "tts_settings_summary";

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.SETTINGS_LANGUAGE_CATEGORY;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mProgressiveDisclosureMixin.setTileLimit(2);
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.language_and_input;
    }

    @Override
    protected List<PreferenceController> getPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getLifecycle());
    }

    private static List<PreferenceController> buildPreferenceControllers(Context context,
            Lifecycle lifecycle) {
        final List<PreferenceController> controllers = new ArrayList<>();
        // Language
        controllers.add(new PhoneLanguagePreferenceController(context));
        controllers.add(new SpellCheckerPreferenceController(context));
        controllers.add(new UserDictionaryPreferenceController(context));
        controllers.add(new TtsPreferenceController(context, new TtsEngines(context)));
        // Input
        controllers.add(new VirtualKeyboardPreferenceController(context));
        controllers.add(new PhysicalKeyboardPreferenceController(context, lifecycle));
        final GameControllerPreferenceController gameControllerPreferenceController
                = new GameControllerPreferenceController(context);
        if (lifecycle != null) {
            lifecycle.addObserver(gameControllerPreferenceController);
        }

        controllers.add(gameControllerPreferenceController);
        controllers.add(new DefaultAutofillPreferenceController(context));
        return controllers;
    }

    private static class SummaryProvider implements SummaryLoader.SummaryProvider {

        private final Context mContext;
        private final SummaryLoader mSummaryLoader;

        public SummaryProvider(Context context, SummaryLoader summaryLoader) {
            mContext = context;
            mSummaryLoader = summaryLoader;
        }

        @Override
        public void setListening(boolean listening) {
            if (listening) {
                final String flattenComponent = Settings.Secure.getString(
                        mContext.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
                if (!TextUtils.isEmpty(flattenComponent)) {
                    final PackageManager packageManage = mContext.getPackageManager();
                    final String pkg = ComponentName.unflattenFromString(flattenComponent)
                            .getPackageName();
                    final InputMethodManager imm = (InputMethodManager) mContext.getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    final List<InputMethodInfo> imis = imm.getInputMethodList();
                    for (InputMethodInfo imi : imis) {
                        if (TextUtils.equals(imi.getPackageName(), pkg)) {
                            mSummaryLoader.setSummary(this, imi.loadLabel(packageManage));
                            return;
                        }
                    }
                }
                mSummaryLoader.setSummary(this, "");
            }
        }
    }

    public static final SummaryLoader.SummaryProviderFactory SUMMARY_PROVIDER_FACTORY
            = (activity, summaryLoader) -> new SummaryProvider(activity, summaryLoader);

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.language_and_input;
                    return Arrays.asList(sir);
                }

                @Override
                public List<PreferenceController> getPreferenceControllers(Context context) {
                    return buildPreferenceControllers(context, null /* lifecycle */);
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    // Duplicates in summary and details pages.
                    keys.add(KEY_TEXT_TO_SPEECH);

                    return keys;
                }
            };
}
