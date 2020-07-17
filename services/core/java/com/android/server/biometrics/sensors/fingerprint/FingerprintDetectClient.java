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
import android.annotation.Nullable;
import android.content.Context;
import android.hardware.biometrics.BiometricAuthenticator;
import android.hardware.biometrics.BiometricFingerprintConstants;
import android.hardware.biometrics.BiometricsProtoEnums;
import android.hardware.biometrics.fingerprint.V2_1.IBiometricsFingerprint;
import android.hardware.fingerprint.IUdfpsOverlayController;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Slog;

import com.android.server.biometrics.sensors.AcquisitionClient;
import com.android.server.biometrics.sensors.AuthenticationConsumer;
import com.android.server.biometrics.sensors.ClientMonitorCallbackConverter;
import com.android.server.biometrics.sensors.PerformanceTracker;

import java.util.ArrayList;

/**
 * Performs fingerprint detection without exposing any matching information (e.g. accept/reject
 * have the same haptic, lockout counter is not increased).
 */
class FingerprintDetectClient extends AcquisitionClient<IBiometricsFingerprint>
        implements AuthenticationConsumer, Udfps {

    private static final String TAG = "FingerprintDetectClient";

    private final boolean mIsStrongBiometric;
    @Nullable private final IUdfpsOverlayController mUdfpsOverlayController;

    public FingerprintDetectClient(@NonNull Context context,
            @NonNull LazyDaemon<IBiometricsFingerprint> lazyDaemon, @NonNull IBinder token,
            @NonNull ClientMonitorCallbackConverter listener, int userId, @NonNull String owner,
            int sensorId, @Nullable IUdfpsOverlayController udfpsOverlayController,
            boolean isStrongBiometric, int statsClient) {
        super(context, lazyDaemon, token, listener, userId, owner, 0 /* cookie */, sensorId,
                BiometricsProtoEnums.MODALITY_FINGERPRINT, BiometricsProtoEnums.ACTION_AUTHENTICATE,
                statsClient);
        mUdfpsOverlayController = udfpsOverlayController;
        mIsStrongBiometric = isStrongBiometric;
    }

    @Override
    protected void stopHalOperation() {
        UdfpsHelper.hideUdfpsOverlay(mUdfpsOverlayController);
        try {
            getFreshDaemon().cancel();
        } catch (RemoteException e) {
            Slog.e(TAG, "Remote exception when requesting cancel", e);
            onError(BiometricFingerprintConstants.FINGERPRINT_ERROR_HW_UNAVAILABLE,
                    0 /* vendorCode */);
            mFinishCallback.onClientFinished(this, false /* success */);
        }
    }

    @Override
    public void start(@NonNull FinishCallback finishCallback) {
        super.start(finishCallback);
        startHalOperation();
    }

    @Override
    protected void startHalOperation() {
        UdfpsHelper.showUdfpsOverlay(mUdfpsOverlayController);
        try {
            getFreshDaemon().authenticate(0 /* operationId */, getTargetUserId());
        } catch (RemoteException e) {
            Slog.e(TAG, "Remote exception when requesting auth", e);
            onError(BiometricFingerprintConstants.FINGERPRINT_ERROR_HW_UNAVAILABLE,
                    0 /* vendorCode */);
            UdfpsHelper.hideUdfpsOverlay(mUdfpsOverlayController);
            mFinishCallback.onClientFinished(this, false /* success */);
        }
    }

    @Override
    public void onFingerDown(int x, int y, float minor, float major) {
        UdfpsHelper.onFingerDown(getFreshDaemon(), x, y, minor, major);
    }

    @Override
    public void onFingerUp() {
        UdfpsHelper.onFingerUp(getFreshDaemon());
    }

    @Override
    public void onAuthenticated(BiometricAuthenticator.Identifier identifier, boolean authenticated,
            ArrayList<Byte> hardwareAuthToken) {
        logOnAuthenticated(getContext(), authenticated, false /* requireConfirmation */,
                getTargetUserId(), false /* isBiometricPrompt */);

        // Do not distinguish between success/failures.
        vibrateSuccess();

        final PerformanceTracker pm = PerformanceTracker.getInstanceForSensorId(getSensorId());
        pm.incrementAuthForUser(getTargetUserId(), authenticated);

        try {
            getListener().onDetected(getSensorId(), getTargetUserId(), mIsStrongBiometric);
        } catch (RemoteException e) {
            Slog.e(TAG, "Remote exception when sending onDetected", e);
        }
    }
}