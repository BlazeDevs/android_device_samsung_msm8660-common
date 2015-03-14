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
import android.content.res.TypedArray;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

public class BlinkInterval extends DialogPreference implements android.widget.Button.OnClickListener {

    private static final String FILE_BLN_INTERVAL = "/sys/class/misc/enhanced_bln/blink_interval_ms";
    private static final int DEFAULT_MIN_VALUE = 500;
    private static final int DEFAULT_MAX_VALUE = 5000;
    private static final int DEFAULT_VALUE = 1000;
    
    private NumberPicker mPickerOn;
    private NumberPicker mPickerOff;
    private int mValueOn;
    private int mValueOff;
    private String mValueOnOff;
    private Context mContext;
    
    public BlinkInterval(Context context) {
        this(context, null);
    }

    public BlinkInterval(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        setPersistent(false);
        mContext = context;
        
        // Set Layout
        setDialogLayoutResource(R.layout.bln_blink_interval_dialog);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        setDialogIcon(null);
    }
    
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        String intervals = Utils.readValue(FILE_BLN_INTERVAL).replace("\n", "");
        setValue(intervals);
    }
    
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        
        TextView dialogMessageText = (TextView) view.findViewById(R.id.blink_interval_dialog_message);
        dialogMessageText.setText(getDialogMessage());
        
        Button resetButton = (Button) view.findViewById(R.id.blink_interval_dialog_reset_button);
        resetButton.setOnClickListener(this);

        mPickerOn = (NumberPicker) view.findViewById(R.id.NumberPickerOn);
        mPickerOn.setMinValue(DEFAULT_MIN_VALUE);
        mPickerOn.setMaxValue(DEFAULT_MAX_VALUE);
        mPickerOn.setValue(mValueOn);

        mPickerOff = (NumberPicker) view.findViewById(R.id.NumberPickerOff);
        mPickerOff.setMinValue(DEFAULT_MIN_VALUE);
        mPickerOff.setMaxValue(DEFAULT_MAX_VALUE);
        mPickerOff.setValue(mValueOff);
        
    }
    
    public String getValue() {
        return mValueOnOff;
    }
    
    public void setValue(String value) {
        String intervals[] = value.split(" ");
        int miliSecOn = Integer.parseInt(intervals[0]);
        int miliSecOff = Integer.parseInt(intervals[1]);
        
        if (!value.equals(mValueOnOff)){
            mValueOn = miliSecOn;
            mValueOff = miliSecOff;
            mValueOnOff = value;
            notifyChanged();
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            mPickerOn.clearFocus();
            mPickerOff.clearFocus();
            
            int valueOn = mPickerOn.getValue();
            int valueOff = mPickerOff.getValue();
            String valueOnOff = valueOn + " " + valueOff;
            
            if (callChangeListener(valueOnOff)) {
                SharedPreferences sharedPrefs = mContext.getSharedPreferences(DisplaySettings.SHARED_PREFERENCES_BASENAME + "_preferences", 0);
                SharedPreferences.Editor editor = sharedPrefs.edit();
                
                setValue(valueOnOff);
                editor.putString(DisplaySettings.KEY_TOUCHKEY_BLN_INTERVAL, mValueOnOff);
                editor.commit();
                Utils.writeValue(FILE_BLN_INTERVAL, mValueOnOff + "\n");
                
                Utils.showToast(mContext, mContext.getString(R.string.touchkey_bln_interval_dialog_success_toast));
            }
        }
    }
    
    @Override
    protected Parcelable onSaveInstanceState() {
        // save the instance state so that it will survive screen orientation changes and other events that may temporarily destroy it
        final Parcelable superState = super.onSaveInstanceState();
 
        // set the state's value with the class member that holds current setting value
        final SavedState myState = new SavedState(superState);
        myState.value = getValue();
 
        return myState;
    }
    
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        // check whether we saved the state in onSaveInstanceState()
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // didn't save the state, so call superclass
            super.onRestoreInstanceState(state);
            return;
        }
 
        // restore the state
        SavedState myState = (SavedState) state;
        setValue(myState.value);
 
        super.onRestoreInstanceState(myState.getSuperState());
    }

    @Override
    public void onClick(View v) {
        setValue(DEFAULT_VALUE + " " + DEFAULT_VALUE);
        mPickerOn.setValue(DEFAULT_VALUE);
        mPickerOff.setValue(DEFAULT_VALUE);
        Utils.showToast(mContext, mContext.getString(R.string.touchkey_bln_interval_dialog_reset_toast));
    }
    
    private static class SavedState extends BaseSavedState {
        String value;
 
        public SavedState(Parcelable superState)
        {
            super(superState);
        }
 
        public SavedState(Parcel source)
        {
            super(source);
 
            value = source.readString();
        }
 
        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);
 
            dest.writeString(value);
        }
 
        @SuppressWarnings("unused")
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>()
        {
            @Override
            public SavedState createFromParcel(Parcel in)
            {
                return new SavedState(in);
            }
 
            @Override
            public SavedState[] newArray(int size)
            {
                return new SavedState[size];
            }
        };
    }
}
