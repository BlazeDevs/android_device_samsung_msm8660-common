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

public class PanelUndervolt extends HWValueSliderPreference {

    private static String UV_VALUE_PATH = "/sys/module/board_msm8x60_celox/parameters/panel_uv";
    private static int UV_MIN_VALUE = 0;
    private static int UV_MAX_VALUE = 500;
    private static int UV_DEFAULT_VALUE = 500;
    private static int UV_INCREMENT_VALUE = 25;

    private static final HardwareInterface HW_INTERFACE = new HardwareInterface() {
        @Override
        public int getMinValue() {
            return UV_MIN_VALUE;
        }
        @Override
        public int getMaxValue() {
            return UV_MAX_VALUE / UV_INCREMENT_VALUE;
        }
        @Override
        public int getCurrentValue() {
            if(Utils.fileExists(UV_VALUE_PATH)) {
                return Integer.parseInt(Utils.readValue(UV_VALUE_PATH)) / UV_INCREMENT_VALUE;
            } else {
                return 0;
            }
        }
        @Override
        public int getDefaultValue() {
            return UV_DEFAULT_VALUE / UV_INCREMENT_VALUE;
        }
        @Override
        public int getWarningThreshold() {
            return 0;
        }
        @Override
        public boolean setValue(int value) {
            if(Utils.fileExists(UV_VALUE_PATH)) {
                Utils.writeValue(UV_VALUE_PATH, String.valueOf(value * UV_INCREMENT_VALUE));
                return true;
            } else {
                return false;
            }
        }
        @Override
        public String getPreferenceName() {
            return "panel_uv";
        }
    };

    public PanelUndervolt(Context context, AttributeSet attrs) {
        super(context, attrs, isSupported() ? HW_INTERFACE : null);

        setDialogLayoutResource(R.layout.panel_undervolt);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // Do nothing
    }

    public static boolean isSupported() {
        if(Utils.fileExists(UV_VALUE_PATH)) {
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
