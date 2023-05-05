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

package com.android.settings.network;

import android.content.Context;
import android.os.Build;
import android.os.SystemProperties;
import android.text.TextUtils;

import androidx.annotation.VisibleForTesting;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import com.android.settingslib.net.AbstractEthChainPreferenceController;

import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import java.lang.reflect.*;


public class EthChainPreferenceController extends AbstractEthChainPreferenceController
        implements Preference.OnPreferenceChangeListener, PreferenceControllerMixin {

    private static final String ETH_CHAIN_KEY = "eth_chain_id";

    @VisibleForTesting
    static final String USER_BUILD_TYPE = "user";

    private final String[] mListValues;
    private final String[] mListSummaries;
    private final Object walletManager;
    private final Object publicWalletManager;
    private Class walletProxy;
    private Class publicWalletProxy;
    private Method changeChainId;
    private Method createSession;
    private Method hasBeenFulfilled;
    private Method getChainId;

    public EthChainPreferenceController(Context context) {
        super(context);

        mListValues = mContext.getResources().getStringArray(R.array.eth_chain_array_values);
        mListSummaries = mContext.getResources().getStringArray(R.array.eth_chain_array_summaries);

        walletManager = context.getSystemService("privatewallet");
        publicWalletManager = context.getSystemService("wallet");
        try {
            walletProxy = Class.forName("android.os.PrivateWalletProxy");
            publicWalletProxy = Class.forName("android.os.WalletProxy");
        } catch (ClassNotFoundException classNotFoundException) {
            classNotFoundException.printStackTrace();
            walletProxy = null;
        }
        try {
            changeChainId = walletProxy.getMethod("changeChainId", int.class);
            createSession = publicWalletProxy.getMethod("createSession");
            hasBeenFulfilled = publicWalletProxy.getMethod("hasBeenFulfilled", String.class);
            getChainId = publicWalletProxy.getMethod("getChainId", String.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        
    }
    
    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return ETH_CHAIN_KEY;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        System.out.println("Updating chain_id state through onPreferenceChange");
        final String value = (String) newValue;
        int newChainId = 1;
        if (value.equals("ethereum")) {
            newChainId = 1;
        } else if(value.equals("optimism")) {
            newChainId = 10;
        } else if(value.equals("arbitrum")) {
            newChainId = 42161;
        } else if(value.equals("goerli")) {
            newChainId = 5;
        }

        try {
            changeChainId.invoke(walletManager, newChainId);
        } catch (IllegalAccessException illegalAccessException) {
            illegalAccessException.printStackTrace();
        } catch (InvocationTargetException invocationTargetException) {
            invocationTargetException.printStackTrace();
        }
        updateStateList((ListPreference) preference);

        return true;
    }

    @Override
    public void updateState(Preference preference) {
        updateStateList((ListPreference) preference);
    }

    private void updateStateList(ListPreference preference) {
        try {
            String sessionId = (String) createSession.invoke(publicWalletManager);
            String requestId = (String) getChainId.invoke(publicWalletManager, sessionId);
    
            Thread.sleep(100);
    
            while (hasBeenFulfilled.invoke(publicWalletManager, requestId).equals("notfulfilled")) {
            }
    
            String stringChainId = (String) hasBeenFulfilled.invoke(publicWalletManager, requestId);
            int chainId = Integer.parseInt(stringChainId);
    
            if (chainId == 1){
                preference.setValue("ethereum");
                preference.setSummary("Ethereum Mainnet");
            } else if (chainId == 10){
                preference.setValue("optimism");
                preference.setSummary("Optimism");
            } else if (chainId == 42161){
                preference.setValue("arbitrum");
                preference.setSummary("Arbitrum One");
            } else if (chainId == 84531){
                preference.setValue("base_testnet");
                preference.setSummary("Base Testnet");
            } else if (chainId == 5){
                preference.setValue("goerli");
                preference.setSummary("Goerli Testnet");
            }
        }catch(Exception e){
            e.printStackTrace();
            preference.setValue("ethereum");
            preference.setSummary("Ethereum Mainnet");
        }
        
    }

    @VisibleForTesting
    public String getBuildType() {
        return Build.TYPE;
    }
}
