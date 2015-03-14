/*
 * Copyright (C) 2013 The CyanogenMod Project
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
import android.os.Vibrator;
import android.util.AttributeSet;
import android.widget.SeekBar;

public class VibratorIntensity extends HWValueSliderPreference {

    private static String LEVEL_PATH = "/sys/class/timed_output/vibrator/pwm_value";
    private static String LEVEL_MAX_PATH = "/sys/class/timed_output/vibrator/pwm_max";
    private static String LEVEL_MIN_PATH = "/sys/class/timed_output/vibrator/pwm_min";
    private static String LEVEL_DEFAULT_PATH = "/sys/class/timed_output/vibrator/pwm_default";
    private static String LEVEL_THRESHOLD_PATH = "/sys/class/timed_output/vibrator/pwm_threshold";

    private static final HardwareInterface HW_INTERFACE = new HardwareInterface() {
        @Override
        public int getMinValue() {
            if(Utils.fileExists(LEVEL_MIN_PATH)) {
                return Integer.parseInt(Utils.readValue(LEVEL_MIN_PATH));
            } else {
                return 0;
            }
        }
        @Override
        public int getMaxValue() {
            if(Utils.fileExists(LEVEL_MAX_PATH)) {
                return Integer.parseInt(Utils.readValue(LEVEL_MAX_PATH));
            } else {
                return 100;
            }
        }
        @Override
        public int getCurrentValue() {
            if(Utils.fileExists(LEVEL_PATH)) {
                return Integer.parseInt(Utils.readValue(LEVEL_PATH));
            } else {
                return 0;
            }
        }
        @Override
        public int getDefaultValue() {
            if(Utils.fileExists(LEVEL_DEFAULT_PATH)) {
                return Integer.parseInt(Utils.readValue(LEVEL_DEFAULT_PATH));
            } else {
                return 50;
            }
        }
        @Override
        public int getWarningThreshold() {
            if(Utils.fileExists(LEVEL_THRESHOLD_PATH)) {
                return Integer.parseInt(Utils.readValue(LEVEL_THRESHOLD_PATH));
            } else {
                return 75;
            }
        }
        @Override
        public boolean setValue(int value) {
            if(Utils.fileExists(LEVEL_PATH)) {
                Utils.writeValue(LEVEL_PATH, String.valueOf(value));
                return true;
            } else {
                return false;
            }
        }
        @Override
        public String getPreferenceName() {
            return "vibration_intensity";
        }
    };

    public VibratorIntensity(Context context, AttributeSet attrs) {
        super(context, attrs, isSupported() ? HW_INTERFACE : null);

        setDialogLayoutResource(R.layout.vibrator_intensity);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Vibrator vib = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(200);
    }

    public static boolean isSupported() {
        if(Utils.fileExists(LEVEL_PATH)) {
            return true;
        } else {
            return false;
        }
    }

    public static void restore(Context context) {
        if (!isSupported()) {
            return;
        }
        HWValueSliderPreference.restore(context, HW_INTERFACE);
    }
}
