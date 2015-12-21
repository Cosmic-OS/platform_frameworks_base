package com.android.systemui.statusbar.phone;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.View;

import com.android.systemui.FontSizeUtils;
import com.android.systemui.R;
import com.android.systemui.statusbar.policy.Clock;

/**
 * To control your...clock
 */
public class ClockController {

    public static final int STYLE_HIDE_CLOCK    = 0;
    public static final int STYLE_CLOCK_RIGHT   = 1;
    public static final int STYLE_CLOCK_CENTER  = 2;
    public static final int STYLE_CLOCK_LEFT    = 3;

    private final NotificationIconAreaController mNotificationIconAreaController;
    private final Context mContext;
    private final SettingsObserver mSettingsObserver;
    private Clock mRightClock, mCenterClock, mLeftClock, mActiveClock;

    private int mAmPmStyle;
    private int mClockLocation;
    private boolean mShowSeconds;
    private int mClockDateStyle;
    private int mClockDateDisplay;

    private int mIconTint = Color.WHITE;

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.STATUS_BAR_AM_PM),
                    false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.STATUS_BAR_CLOCK),
                    false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.STATUS_BAR_CLOCK_SECONDS),
                    false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.STATUS_BAR_CLOCK_DATE_DISPLAY),
                    false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.STATUS_BAR_CLOCK_DATE_STYLE),
                    false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.STATUS_BAR_CLOCK_DATE_FORMAT),
                    false, this, UserHandle.USER_ALL);
            updateSettings();
        }

        void unobserve() {
            mContext.getContentResolver().unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange) {
            updateSettings();
        }
    }

    public ClockController(View statusBar,
            NotificationIconAreaController notificationIconAreaController, Handler handler) {
        mRightClock = (Clock) statusBar.findViewById(R.id.clock);
        mCenterClock = (Clock) statusBar.findViewById(R.id.center_clock);
        mLeftClock = (Clock) statusBar.findViewById(R.id.left_clock);
        mNotificationIconAreaController = notificationIconAreaController;
        mContext = statusBar.getContext();

        mActiveClock = mRightClock;
        mSettingsObserver = new SettingsObserver(handler);
        mSettingsObserver.observe();
    }

    private Clock getClockForCurrentLocation() {
        Clock clockForAlignment;
        switch (mClockLocation) {
            case STYLE_CLOCK_CENTER:
                clockForAlignment = mCenterClock;
                break;
            case STYLE_CLOCK_LEFT:
                clockForAlignment = mLeftClock;
                break;
            case STYLE_CLOCK_RIGHT:
            case STYLE_HIDE_CLOCK:
            default:
                clockForAlignment = mRightClock;
                break;
        }
        return clockForAlignment;
    }

    private void updateActiveClock() {
        mActiveClock.setVisibility(View.GONE);
        if (mClockLocation == STYLE_HIDE_CLOCK) {
            return;
        }

        mActiveClock = getClockForCurrentLocation();
        mActiveClock.setVisibility(View.VISIBLE);
        mActiveClock.setAmPmStyle(mAmPmStyle);
        mActiveClock.setShowSeconds(mShowSeconds);
        mActiveClock.setClockDateDisplay(mClockDateDisplay);
        mActiveClock.setClockDateStyle(mClockDateStyle);

        setClockAndDateStatus();
        setTextColor(mIconTint);
        updateFontSize();
    }

    private void updateSettings() {
        ContentResolver resolver = mContext.getContentResolver();
        mAmPmStyle = Settings.System.getIntForUser(resolver,
                Settings.System.STATUS_BAR_AM_PM, Clock.AM_PM_STYLE_GONE,
                UserHandle.USER_CURRENT);
        mClockLocation = Settings.System.getIntForUser(resolver,
                Settings.System.STATUS_BAR_CLOCK, STYLE_CLOCK_RIGHT,
                UserHandle.USER_CURRENT);
        mShowSeconds = Settings.System.getIntForUser(resolver,
                Settings.System.STATUS_BAR_CLOCK_SECONDS, 0,
                UserHandle.USER_CURRENT) == 1;
        mClockDateDisplay = Settings.System.getIntForUser(resolver,
                Settings.System.STATUS_BAR_CLOCK_DATE_DISPLAY, Clock.CLOCK_DATE_DISPLAY_GONE,
                UserHandle.USER_CURRENT);
        mClockDateStyle = Settings.System.getIntForUser(resolver,
                Settings.System.STATUS_BAR_CLOCK_DATE_STYLE, Clock.CLOCK_DATE_STYLE_REGULAR,
                UserHandle.USER_CURRENT);
        updateActiveClock();
    }

    private void setClockAndDateStatus() {
        if (mNotificationIconAreaController != null) {
            mNotificationIconAreaController.setClockAndDateStatus(mClockLocation);
        }
    }

    public void setVisibility(boolean visible) {
        if (mActiveClock != null) {
            mActiveClock.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    public void setTextColor(int iconTint) {
        mIconTint = iconTint;
        if (mActiveClock != null) {
            mActiveClock.setTextColor(iconTint);
        }
    }

    public void setTextColor(Rect tintArea, int iconTint) {
        if (mActiveClock != null) {
            setTextColor(StatusBarIconController.getTint(tintArea, mActiveClock, iconTint));
        }
    }

    public void updateFontSize() {
        if (mActiveClock != null) {
            FontSizeUtils.updateFontSize(mActiveClock, R.dimen.status_bar_clock_size);
        }
    }

    public void setPaddingRelative(int start, int top, int end, int bottom) {
        if (mActiveClock != null) {
            mActiveClock.setPaddingRelative(start, top, end, bottom);
        }
    }
}
