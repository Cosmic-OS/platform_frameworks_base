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

package com.android.systemui.statusbar.notification.stack;

import static com.android.systemui.statusbar.notification.ViewGroupFadeHelper.reset;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.testing.AndroidTestingRunner;

import androidx.test.filters.SmallTest;

import com.android.systemui.SysuiTestCase;
import com.android.systemui.statusbar.notification.DynamicPrivacyController;
import com.android.systemui.statusbar.notification.row.NotificationGutsManager;
import com.android.systemui.statusbar.phone.HeadsUpManagerPhone;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.tuner.TunerService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for {@link NotificationStackScrollLayoutController}.
 */
@SmallTest
@RunWith(AndroidTestingRunner.class)
public class NotificationStackScrollerControllerTest extends SysuiTestCase {

    @Mock
    private NotificationGutsManager mNotificationGutsManager;
    @Mock
    private HeadsUpManagerPhone mHeadsUpManager;
    @Mock
    private NotificationRoundnessManager mNotificationRoundnessManager;
    @Mock
    private TunerService mTunerService;
    @Mock
    private DynamicPrivacyController mDynamicPrivacyController;
    @Mock
    private ConfigurationController mConfigurationController;
    @Mock
    private NotificationStackScrollLayout mNotificationStackScrollLayout;
    @Mock
    private ZenModeController mZenModeController;

    NotificationStackScrollLayoutController mController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        mController = new NotificationStackScrollLayoutController(
                true,
                mNotificationGutsManager,
                mHeadsUpManager,
                mNotificationRoundnessManager,
                mTunerService,
                mDynamicPrivacyController,
                mConfigurationController,
                mZenModeController
        );

        when(mNotificationStackScrollLayout.isAttachedToWindow()).thenReturn(true);
    }


    @Test
    public void testAttach_viewAlreadyAttached() {
        mController.attach(mNotificationStackScrollLayout);

        verify(mConfigurationController).addCallback(
                any(ConfigurationController.ConfigurationListener.class));
    }
    @Test
    public void testAttach_viewAttachedAfterInit() {
        when(mNotificationStackScrollLayout.isAttachedToWindow()).thenReturn(false);

        mController.attach(mNotificationStackScrollLayout);

        verify(mConfigurationController, never()).addCallback(
                any(ConfigurationController.ConfigurationListener.class));

        mController.mOnAttachStateChangeListener.onViewAttachedToWindow(
                mNotificationStackScrollLayout);

        verify(mConfigurationController).addCallback(
                any(ConfigurationController.ConfigurationListener.class));
    }

    @Test
    public void testOnDensityOrFontScaleChanged_reInflatesFooterViews() {
        mController.attach(mNotificationStackScrollLayout);
        mController.mConfigurationListener.onDensityOrFontScaleChanged();
        verify(mNotificationStackScrollLayout).reinflateViews();
    }

    @Test
    public void testUpdateEmptyShadeView_notificationsVisible() {
        when(mZenModeController.areNotificationsHiddenInShade()).thenReturn(true);
        mController.attach(mNotificationStackScrollLayout);

        mController.updateEmptyShadeView(true /* visible */);
        verify(mNotificationStackScrollLayout).updateEmptyShadeView(
                true /* visible */,
                true /* notifVisibleInShade */);
        reset(mNotificationStackScrollLayout);
        mController.updateEmptyShadeView(false /* visible */);
        verify(mNotificationStackScrollLayout).updateEmptyShadeView(
                false /* visible */,
                true /* notifVisibleInShade */);
    }

    @Test
    public void testUpdateEmptyShadeView_notificationsHidden() {
        when(mZenModeController.areNotificationsHiddenInShade()).thenReturn(false);
        mController.attach(mNotificationStackScrollLayout);

        mController.updateEmptyShadeView(true /* visible */);
        verify(mNotificationStackScrollLayout).updateEmptyShadeView(
                true /* visible */,
                false /* notifVisibleInShade */);
        reset(mNotificationStackScrollLayout);
        mController.updateEmptyShadeView(false /* visible */);
        verify(mNotificationStackScrollLayout).updateEmptyShadeView(
                false /* visible */,
                false /* notifVisibleInShade */);
    }
}