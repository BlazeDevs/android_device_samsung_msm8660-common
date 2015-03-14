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
import android.util.AttributeSet;
import android.widget.SeekBar;

public class InputBoostFreq extends HWValueSliderPreference {

    private static String FILE_INPUT_BOOST_FREQ = "/sys/module/cpu_input_boost/parameters/input_boost_freq";
    private static int MIN_VALUE = 192000;
    private static int MAX_VALUE = 1890000;
    private static int DEFAULT_VALUE = 1134000;

    private static final HardwareInterface HW_INTERFACE = new HardwareInterface() {
        @Override
        public int getMinValue() {
            return Utils.CPUFreqToIndex(MIN_VALUE);
        }
        @Override
        public int getMaxValue() {
            return Utils.CPUFreqToIndex(MAX_VALUE);
        }
        @Override
        public int getCurrentValue() {
            if(Utils.fileExists(FILE_INPUT_BOOST_FREQ)) {
                return Utils.CPUFreqToIndex(Integer.parseInt(Utils.readValue(FILE_INPUT_BOOST_FREQ).replace("\n", "")));
            } else {
                return 0;
            }
        }
        @Override
        public int getDefaultValue() {
            return Utils.CPUFreqToIndex(DEFAULT_VALUE);
        }
        @Override
        public int getWarningThreshold() {
            return 0;
        }
        @Override
        public boolean setValue(int value) {
            if(Utils.fileExists(FILE_INPUT_BOOST_FREQ)) {
                Utils.writeValue(FILE_INPUT_BOOST_FREQ, String.valueOf(Utils.IndexToCPUFreq(value)));
                return true;
            } else {
                return false;
            }
        }
        @Override
        public String getPreferenceName() {
            return "input_boost_freq";
        }
    };

    public InputBoostFreq(Context context, AttributeSet attrs) {
        super(context, attrs, isSupported() ? HW_INTERFACE : null);

        setDialogLayoutResource(R.layout.input_boost_freq);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // Do nothing
    }

    public static boolean isSupported() {
        if(Utils.fileExists(FILE_INPUT_BOOST_FREQ)) {
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
