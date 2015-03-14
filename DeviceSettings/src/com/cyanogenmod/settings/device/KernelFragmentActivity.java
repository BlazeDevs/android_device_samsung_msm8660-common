/*
 * Copyright (C) 2012 The CyanogenMod Project
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

package com.cyanogenmod.settings.device;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.cyanogenmod.settings.device.R;

public class KernelFragmentActivity extends PreferenceFragment {

    private static final String TAG = "GalaxyS2Parts_Kernel";

    private static final String FILE_INPUT_BOOST_ENABLED = "/sys/module/cpu_input_boost/parameters/input_boost_enabled";
    private static final String FILE_INPUT_BOOST_FREQ = "/sys/module/cpu_input_boost/parameters/input_boost_freq";
    private static final String FILE_INPUT_BOOST_MS = "/sys/module/cpu_input_boost/parameters/input_boost_ms";
    private static final String FILE_PANEL_UV = "/sys/module/board_msm8x60_celox/parameters/panel_uv";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.kernel_preferences);

        final PreferenceGroup performanceCategory =
                (PreferenceGroup) findPreference(DisplaySettings.KEY_KERNEL_PERFORMANCE_CATEGORY);

        final PreferenceGroup batteryCategory =
                (PreferenceGroup) findPreference(DisplaySettings.KEY_KERNEL_BATTERY_CATEGORY);

        if (!Utils.fileExists(FILE_INPUT_BOOST_ENABLED) ||
            !Utils.fileExists(FILE_INPUT_BOOST_FREQ) ||
            !Utils.fileExists(FILE_INPUT_BOOST_MS)) {
            getPreferenceScreen().removePreference(performanceCategory);
        }

        if (!PanelUndervolt.isSupported()) {
            getPreferenceScreen().removePreference(batteryCategory);
        }

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        String boxValue;
        String key = preference.getKey();

        if (key.compareTo(DisplaySettings.KEY_INPUT_BOOST_ENABLED) == 0) {
            boxValue = (((CheckBoxPreference)preference).isChecked() ? "1" : "0");
            Utils.writeValue(FILE_INPUT_BOOST_ENABLED, boxValue);
        }

        return true;
    }

    public static boolean isSupported(String FILE) {
        return Utils.fileExists(FILE);
    }

    public static void restore(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        Utils.writeValue(FILE_INPUT_BOOST_ENABLED, sharedPrefs.getBoolean(DisplaySettings.KEY_INPUT_BOOST_ENABLED, true) ? "0" : "1");
        Utils.writeValue(FILE_INPUT_BOOST_FREQ, sharedPrefs.getString(DisplaySettings.KEY_INPUT_BOOST_FREQ, "1134000"));
        Utils.writeValue(FILE_INPUT_BOOST_MS, sharedPrefs.getString(DisplaySettings.KEY_INPUT_BOOST_TIMEOUT, "1500"));
        Utils.writeValue(FILE_PANEL_UV, sharedPrefs.getString(DisplaySettings.KEY_PANEL_UV, "500"));
    }
}
