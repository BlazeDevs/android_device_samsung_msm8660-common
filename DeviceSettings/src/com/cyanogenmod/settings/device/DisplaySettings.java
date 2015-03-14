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

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.cyanogenmod.settings.device.R;

import java.util.ArrayList;

public class DisplaySettings extends FragmentActivity {

    public static final String SHARED_PREFERENCES_BASENAME = "com.cyanogenmod.settings.device";
    public static final String ACTION_UPDATE_PREFERENCES = "com.cyanogenmod.settings.device.UPDATE";
    public static final String KEY_USE_GYRO_CALIBRATION = "use_gyro_calibration";
    public static final String KEY_CALIBRATE_GYRO = "calibrate_gyro";
    public static final String KEY_PANEL_UV = "panel_uv";
    public static final int    UV_DEFAULT_VALUE = 500;
    public static final int    UV_INCREMENT_VALUE = 25;
    public static final String KEY_TOUCHSCREEN_SENSITIVITY = "touchscreen_sensitivity";
    public static final String KEY_MIRRORING = "mirroring_enabled";
    public static final String KEY_REMOTE_DISPLAY = "remote_display_enabled";
    public static final String KEY_MIRRORING_UI_TASK = "mirroringUITask";
    public static final String KEY_MIRRORING_DB_TASK = "mirroringDBTask";
    public static final String KEY_REMOTE_UI_TASK = "remoteUITask";
    public static final String KEY_REMOTE_DB_TASK = "remoteDBTask";
    public static final String KEY_TOUCHKEY_LIGHT = "touchkey_light";
    public static final String KEY_TOUCHKEY_BLN = "touchkey_bln";
    public static final String KEY_TOUCHKEY_BLN_OFF = "touchkey_bln_off";
    public static final String KEY_TOUCHKEY_BLN_INTERVAL = "touchkey_bln_interval";
    public static final String KEY_TOUCHKEY_BLN_TIMEOUT = "touchkey_bln_timeout";
    public static final String GSF_DB_FILE = "/data/data/com.google.android.gsf/databases/gservices.db";
    public static final String SUBMIX_FILE = "/system/lib/hw/audio.r_submix.default.so";
    public static final String GSF_OVERRIDES_TABLE = "overrides";
    public static final String GSF_MIRRORING_ENABLED = "gms:cast:mirroring_enabled";
    public static final String GSF_REMOTE_DISPLAY_ENABLED = "gms:cast:remote_display_enabled";
    public static final String GSF_PACKAGE = "com.google.android.gsf";
    public static final String GMS_PACKAGE = "com.google.android.gms";
    public static final String CHROMECAST_PACKAGE = "com.google.android.apps.chromecast.app";
    public static final String KEY_DISPLAY_CALIBRATION_CATEGORY = "display_calibration_category";
    public static final String KEY_DISPLAY_COLOR = "color_calibration";
    public static final String KEY_DISPLAY_GAMMA = "gamma_tuning";
    public static final String KEY_SENSORS_MOTORS_CATEGORY = "sensors_motors_category";
    public static final String KEY_TOUCHKEY_S2W = "touchkey_s2w";
    public static final String KEY_TOUCHKEY_S2S = "touchkey_s2s";
    public static final String KEY_TOUCHKEY_S2W_SENSITIVE = "touchkey_s2w_sensitive";
    public static final String FILE_CPU_FREQS = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";
    public static final String FILE_INPUT_BOOST_ENABLED = "/sys/module/cpu_input_boost/input_boost_enabled";
    public static final String FILE_INPUT_BOOST_FREQ = "/sys/module/cpu_input_boost/input_boost_freq";
    public static final String FILE_INPUT_BOOST_MS = "/sys/module/cpu_input_boost/input_boost_ms";
    public static final String KEY_KERNEL_PERFORMANCE_CATEGORY = "kernel_performance_category";
    public static final String KEY_KERNEL_BATTERY_CATEGORY = "kernel_battery_category";
    public static final String KEY_INPUT_BOOST_ENABLED = "input_boost_enabled";
    public static final String KEY_INPUT_BOOST_FREQ = "input_boost_freq";
    public static final String KEY_INPUT_BOOST_TIMEOUT = "input_boost_timeout";

    ViewPager mViewPager;
    TabsAdapter mTabsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewPager = new ViewPager(this);
        mViewPager.setId(R.id.viewPager);
        setContentView(mViewPager);

        final ActionBar bar = getActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE, ActionBar.DISPLAY_SHOW_TITLE);
        bar.setTitle(R.string.app_name);

        mTabsAdapter = new TabsAdapter(this, mViewPager);
        mTabsAdapter.addTab(bar.newTab().setText(R.string.category_screen_title),
                ScreenFragmentActivity.class, null);
        mTabsAdapter.addTab(bar.newTab().setText(R.string.category_sensors_title),
                SensorsFragmentActivity.class, null);
        mTabsAdapter.addTab(bar.newTab().setText(R.string.category_kernel_title),
                KernelFragmentActivity.class, null);

        if (savedInstanceState != null) {
            bar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
    }

    public static class TabsAdapter extends FragmentPagerAdapter
            implements ActionBar.TabListener, ViewPager.OnPageChangeListener {
        private final Context mContext;
        private final ActionBar mActionBar;
        private final ViewPager mViewPager;
        private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

        static final class TabInfo {
            private final Class<?> clss;
            private final Bundle args;

            TabInfo(Class<?> _class, Bundle _args) {
                clss = _class;
                args = _args;
            }
        }

        public TabsAdapter(Activity activity, ViewPager pager) {
            super(activity.getFragmentManager());
            mContext = activity;
            mActionBar = activity.getActionBar();
            mViewPager = pager;
            mViewPager.setAdapter(this);
            mViewPager.setOnPageChangeListener(this);
        }

        public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args) {
            TabInfo info = new TabInfo(clss, args);
            tab.setTag(info);
            tab.setTabListener(this);
            mTabs.add(info);
            mActionBar.addTab(tab);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }

        @Override
        public Fragment getItem(int position) {
            TabInfo info = mTabs.get(position);
            return Fragment.instantiate(mContext, info.clss.getName(), info.args);
        }

        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        public void onPageSelected(int position) {
            mActionBar.setSelectedNavigationItem(position);
        }

        public void onPageScrollStateChanged(int state) {
        }

        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            Object tag = tab.getTag();
            for (int i=0; i<mTabs.size(); i++) {
                if (mTabs.get(i) == tag) {
                    mViewPager.setCurrentItem(i);
                }
            }
        }

        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        }

        public void onTabReselected(Tab tab, FragmentTransaction ft) {
        }
    }
}
