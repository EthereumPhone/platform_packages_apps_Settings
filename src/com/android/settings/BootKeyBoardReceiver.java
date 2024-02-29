/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.android.settings.fuelgauge.batteryusage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.android.settings.core.instrumentation.ElapsedTimeUtils;
import com.android.settings.fuelgauge.BatteryUsageHistoricalLogEntry.Action;
import com.android.settings.fuelgauge.batteryusage.bugreport.BatteryUsageLogUtils;
import com.android.settings.overlay.FeatureFactory;

import java.time.Duration;

/** Receives broadcasts to start or stop the periodic fetching job. */
public final class BootKeyBoardReceiver extends BroadcastReceiver {
    private static final String TAG = "BootKeyBoardReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent == null ? "" : intent.getAction();

        // Waits a while to recheck the scheduler to avoid AlarmManager is not ready.
        System.out.println("ETHOSDEBUG IS BOOTING IN SETTINGS");
    }

}
