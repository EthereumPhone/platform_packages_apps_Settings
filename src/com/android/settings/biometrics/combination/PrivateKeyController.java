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

package com.android.settings.biometrics.combination;

import static android.content.Context.CLIPBOARD_SERVICE;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.VisibleForTesting;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.DeviceInfoUtils;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Scanner;
import java.io.IOException;
import java.io.FileNotFoundException;
import org.web3j.crypto.*;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.nio.charset.StandardCharsets;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;
import java.security.Provider;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.Security;
import java.io.FileWriter;
import java.nio.ByteBuffer;
import androidx.biometric.BiometricManager;
import android.hardware.biometrics.BiometricPrompt;
import android.os.CancellationSignal;
import java.lang.reflect.*;

public class PrivateKeyController extends BasePreferenceController {

    private static final String KEY_PRIVATE_KEY = "private_key_settings";
    private static final String KEY_PREFERENCE_CATEGORY = "security_category";

    private Context mContext;
    private BiometricPrompt mBiometricPrompt;
    private CancellationSignal mCancellationSignal;


    private final List<Preference> mPreferenceList = new ArrayList<>();

    public PrivateKeyController(Context context, String key) {
        super(context, key);
        mContext = context;
        
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public CharSequence getSummary() {
        return mContext.getString(R.string.device_info_protected_single_press);
    }

    private void authenticate(Preference preference) {
        BiometricManager biometricManager = BiometricManager.from(mContext);
        boolean hasAFingerprintRegistered  = biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS;
        if (hasAFingerprintRegistered) {
            mCancellationSignal = new CancellationSignal();
            mBiometricPrompt = new BiometricPrompt.Builder(mContext)
                    .setTitle("Authenticate")
                    .setSubtitle("Authenticate to view private key")
                    .setNegativeButton("Cancel", mContext.getMainExecutor(), (dialogInterface, i) -> {
                        Toast.makeText(mContext, "Authentication cancelled", Toast.LENGTH_LONG).show();
                    })
                    .build();
            mBiometricPrompt.authenticate(mCancellationSignal, mContext.getMainExecutor(), new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    Toast.makeText(mContext, "Authentication error: " + errString, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    Toast.makeText(mContext, "Authentication succeeded!", Toast.LENGTH_LONG).show();
                    updatePreferenceSummary(preference, getPrivateKey());
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    Toast.makeText(mContext, "Authentication failed", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(mContext, "No fingerprint registered", Toast.LENGTH_LONG).show();
            updatePreferenceSummary(preference, getPrivateKey());
        }
        // updatePreferenceSummary(credentials.getEcKeyPair().getPrivateKey().toString(16));
    }

    private String getPrivateKey() {
        try {
            Class cls = Class.forName("android.os.PrivateWalletProxy");
            Object obj = mContext.getSystemService("privatewallet");
            Method method = cls.getDeclaredMethod("getPrivateKey");
            String privateKey = (String) method.invoke(obj);
            return "0x" + privateKey;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "0x0";
        
    }

    private void updatePreferenceSummary(Preference preference, String summary) {
        if (preference != null) {
            preference.setSummary(summary);
        }
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        String prefKey = preference.getKey();
        if (prefKey.startsWith(KEY_PRIVATE_KEY)) {
            authenticate(preference);
        }
        return super.handlePreferenceTreeClick(preference);
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        final Preference preference = screen.findPreference(getPreferenceKey());
        final PreferenceCategory category = screen.findPreference(KEY_PREFERENCE_CATEGORY);
        mPreferenceList.add(preference);
    }

    @Override
    public void updateState(Preference preference) {
        for (int simSlotNumber = 0; simSlotNumber < mPreferenceList.size(); simSlotNumber++) {
            final Preference simStatusPreference = mPreferenceList.get(simSlotNumber);
            simStatusPreference.setTitle(getPreferenceTitle(simSlotNumber));
            simStatusPreference.setSummary(getSummary());
        }
    }

    @Override
    public boolean useDynamicSliceSummary() {
        return true;
    }

    @Override
    public void copy() {
        final ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(
                CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("text", getPrivateKey()));

        final String toast = "Copied to clipboard";
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }

    private CharSequence getPreferenceTitle(int simSlot) {
        return "Private Key";
    }
}
