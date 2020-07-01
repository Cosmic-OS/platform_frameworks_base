/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.android.server.biometrics.sensors.fingerprint;

import android.annotation.NonNull;
import android.content.Context;
import android.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Slog;

import com.android.server.biometrics.sensors.BiometricUtils;
import com.android.server.biometrics.sensors.ClientMonitorCallbackConverter;
import com.android.server.biometrics.sensors.RemovalClient;

/**
 * Fingerprint-specific removal client supporting the
 * {@link android.hardware.biometrics.fingerprint.V2_1} and
 * {@link android.hardware.biometrics.fingerprint.V2_2} HIDL interfaces.
 */
class FingerprintRemovalClient extends RemovalClient {
    private static final String TAG = "FingerprintRemovalClient";

    private final IBiometricsFingerprint mDaemon;

    FingerprintRemovalClient(@NonNull FinishCallback finishCallback, @NonNull Context context,
            @NonNull IBiometricsFingerprint daemon, @NonNull IBinder token,
            @NonNull ClientMonitorCallbackConverter listener, int biometricId, int userId,
            boolean restricted, @NonNull String owner, @NonNull BiometricUtils utils, int sensorId,
            int statsModality) {
        super(finishCallback, context, token, listener, biometricId, userId, restricted, owner,
                utils, sensorId, statsModality);
        mDaemon = daemon;
    }

    @Override
    protected void startHalOperation() {
        try {
            // GroupId was never used. In fact, groupId is always the same as userId.
            mDaemon.remove(getTargetUserId(), mBiometricId);
        } catch (RemoteException e) {
            Slog.e(TAG, "Remote exception when requesting remove", e);
            mFinishCallback.onClientFinished(this);
        }
    }

    @Override
    protected void stopHalOperation() {
        try {
            mDaemon.cancel();
        } catch (RemoteException e) {
            Slog.e(TAG, "Remote exception when requesting cancel", e);
            mFinishCallback.onClientFinished(this);
        }
    }
}