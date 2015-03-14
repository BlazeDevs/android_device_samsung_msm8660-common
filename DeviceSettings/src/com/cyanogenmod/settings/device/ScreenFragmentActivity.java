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

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import com.cyanogenmod.settings.device.Utils;
import com.cyanogenmod.settings.device.R;
import com.cyanogenmod.settings.device.DisplayColor;
import com.cyanogenmod.settings.device.DisplayGamma;

public class ScreenFragmentActivity extends PreferenceFragment implements OnPreferenceChangeListener {

    private TouchscreenSensitivity mTouchscreenSensitivity;
    private CheckBoxPreference mMirroring;
    private CheckBoxPreference mRemoteDisplay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.screen_preferences);

        final PreferenceGroup calibrationCategory =
                (PreferenceGroup) findPreference(DisplaySettings.KEY_DISPLAY_CALIBRATION_CATEGORY);

        if (!DisplayColor.isSupported() && !DisplayGamma.isSupported()) {
            getPreferenceScreen().removePreference(calibrationCategory);
        } else {
            if (!DisplayColor.isSupported()) {
                calibrationCategory.removePreference(findPreference(DisplaySettings.KEY_DISPLAY_COLOR));
            }
            if (!DisplayGamma.isSupported()) {
                calibrationCategory.removePreference(findPreference(DisplaySettings.KEY_DISPLAY_GAMMA));
            }
        }
        
        mTouchscreenSensitivity = (TouchscreenSensitivity) findPreference(DisplaySettings.KEY_TOUCHSCREEN_SENSITIVITY);
        mTouchscreenSensitivity.setEnabled(mTouchscreenSensitivity.isSupported());
        
        new cmdTask("init").execute();
        
        mMirroring = (CheckBoxPreference) findPreference(DisplaySettings.KEY_MIRRORING);
        mMirroring.setEnabled(Utils.mirroringIsSupported());
        new cmdTask(DisplaySettings.KEY_MIRRORING_UI_TASK).execute();
        mMirroring.setOnPreferenceChangeListener(this);
        
        mRemoteDisplay = (CheckBoxPreference) findPreference(DisplaySettings.KEY_REMOTE_DISPLAY);
        mRemoteDisplay.setEnabled(Utils.mirroringIsSupported());
        new cmdTask(DisplaySettings.KEY_REMOTE_UI_TASK).execute();
        mRemoteDisplay.setOnPreferenceChangeListener(this);
    }
    
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final String key = preference.getKey();
        final boolean checkboxEnabled = newValue.toString().equals("true");
        
        if (key.equals(DisplaySettings.KEY_MIRRORING)) {
            new cmdTask(DisplaySettings.KEY_MIRRORING_DB_TASK, checkboxEnabled).execute();
            return true;
        }
        else if (key.equals(DisplaySettings.KEY_REMOTE_DISPLAY)) {
            new cmdTask(DisplaySettings.KEY_REMOTE_DB_TASK, checkboxEnabled).execute();
            return true;
        }
        
        return false;
    }
    
    private class cmdTask extends AsyncTask<String, Void, String> {
        String task;
        boolean enabled;
        
        public cmdTask(String task) {
            this(task, false);
        }
    
        public cmdTask(String task, boolean enabled) {
            this.task = task;
            this.enabled = enabled;
        }

        @Override
        protected String doInBackground(String... params) {
            if (task.equals("init")) {
                Utils.initializeGSFDB();
            }
            else if (task.equals(DisplaySettings.KEY_MIRRORING_UI_TASK) || task.equals(DisplaySettings.KEY_REMOTE_UI_TASK)) {
                return task;
            }
            else if (task.equals(DisplaySettings.KEY_MIRRORING_DB_TASK)) {
                Utils.setOverride(DisplaySettings.GSF_MIRRORING_ENABLED, enabled);
            }
            else if (task.equals(DisplaySettings.KEY_REMOTE_DB_TASK)) {
                Utils.setOverride(DisplaySettings.GSF_REMOTE_DISPLAY_ENABLED, enabled);
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            if (task.equals(DisplaySettings.KEY_MIRRORING_UI_TASK)) {
                mMirroring.setChecked(Utils.overrideEnabled(DisplaySettings.GSF_MIRRORING_ENABLED));
            }
            else if (task.equals(DisplaySettings.KEY_REMOTE_UI_TASK)) {
                mRemoteDisplay.setChecked(Utils.overrideEnabled(DisplaySettings.GSF_REMOTE_DISPLAY_ENABLED));
            }
        }

        @Override
        protected void onPreExecute() {
            
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            
        }
    }

}
