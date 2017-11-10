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

package com.android.settings.development;

import static android.arch.lifecycle.Lifecycle.Event.ON_CREATE;
import static android.arch.lifecycle.Lifecycle.Event.ON_DESTROY;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.TestConfig;
import com.android.settings.testutils.SettingsRobolectricTestRunner;
import com.android.settingslib.core.lifecycle.Lifecycle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(SettingsRobolectricTestRunner.class)
@Config(manifest = TestConfig.MANIFEST_PATH, sdk = TestConfig.SDK_VERSION)
public class SelectUsbConfigPreferenceControllerTest {

    @Mock
    private ListPreference mPreference;
    @Mock
    private PreferenceScreen mScreen;
    @Mock
    private UsbManager mUsbManager;

    private Context mContext;
    private Lifecycle mLifecycle;
    private SelectUsbConfigPreferenceController mController;

    /**
     * Array Values Key
     *
     * 0: Charging
     * 1: MTP
     * 2: PTP
     * 3: RNDIS
     * 4: Audio Source
     * 5: MIDI
     */
    private String[] mValues;
    private String[] mSummaries;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mLifecycle = new Lifecycle(() -> mLifecycle);
        mContext = spy(RuntimeEnvironment.application);
        doReturn(mUsbManager).when(mContext).getSystemService(Context.USB_SERVICE);
        mValues = mContext.getResources().getStringArray(R.array.usb_configuration_values);
        mSummaries = mContext.getResources().getStringArray(R.array.usb_configuration_titles);
        mController = spy(new SelectUsbConfigPreferenceController(mContext, mLifecycle));
        when(mScreen.findPreference(mController.getPreferenceKey())).thenReturn(mPreference);
        mController.displayPreference(mScreen);

    }

    @Test
    public void onPreferenceChange_setCharging_shouldEnableCharging() {
        when(mUsbManager.isFunctionEnabled(mValues[0])).thenReturn(true);
        doNothing().when(mController).setCurrentFunction(anyString(), anyBoolean());
        mController.onPreferenceChange(mPreference, mValues[0]);

        verify(mController).setCurrentFunction(mValues[0], false /* usb data unlock */);
    }

    @Test
    public void onPreferenceChange_setMtp_shouldEnableMtp() {
        when(mUsbManager.isFunctionEnabled(mValues[1])).thenReturn(true);
        doNothing().when(mController).setCurrentFunction(anyString(), anyBoolean());
        mController.onPreferenceChange(mPreference, mValues[1]);

        verify(mController).setCurrentFunction(mValues[1], true /* usb data unlock */);
    }

    @Test
    public void updateState_chargingEnabled_shouldSetPreferenceToCharging() {
        when(mUsbManager.isFunctionEnabled(mValues[0])).thenReturn(true);

        mController.updateState(mPreference);

        verify(mPreference).setValue(mValues[0]);
        verify(mPreference).setSummary(mSummaries[0]);
    }

    @Test
    public void updateState_RndisEnabled_shouldEnableRndis() {
        when(mUsbManager.isFunctionEnabled(mValues[3])).thenReturn(true);

        mController.updateState(mPreference);

        verify(mPreference).setValue(mValues[3]);
        verify(mPreference).setSummary(mSummaries[3]);
    }

    @Test
    public void updateState_noValueSet_shouldEnableChargingAsDefault() {
        mController.updateState(mPreference);

        verify(mPreference).setValue(mValues[0]);
        verify(mPreference).setSummary(mSummaries[0]);
    }

    @Test
    public void onDeveloperOptionsSwitchDisabled_shouldDisablePreference() {
        mController.onDeveloperOptionsSwitchDisabled();

        verify(mPreference).setEnabled(false);
    }

    @Test
    public void onDeveloperOptionsSwitchEnabled_shouldEnablePreference() {
        mController.onDeveloperOptionsSwitchEnabled();

        verify(mPreference).setEnabled(true);
    }

    @Test
    public void onCreate_shouldRegisterReceiver() {
        mLifecycle.onCreate(null /* bundle */);
        mLifecycle.handleLifecycleEvent(ON_CREATE);

        verify(mContext).registerReceiver(any(), any());
    }

    @Test
    public void onDestroy_shouldUnregisterReceiver() {
        doNothing().when(mContext).unregisterReceiver(any());
        mLifecycle.handleLifecycleEvent(ON_CREATE);
        mLifecycle.handleLifecycleEvent(ON_DESTROY);

        verify(mContext).unregisterReceiver(any());
    }
}
