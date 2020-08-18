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

package com.android.systemui.wmshell;

import android.content.Context;
import android.os.Handler;
import android.view.IWindowManager;

import com.android.systemui.dagger.SysUISingleton;
import com.android.systemui.dagger.qualifiers.Main;
import com.android.systemui.pip.phone.PipMenuActivity;
import com.android.systemui.pip.phone.dagger.PipMenuActivityClass;
import com.android.systemui.wm.DisplaySystemBarsController;
import com.android.wm.shell.common.DisplayController;
import com.android.wm.shell.common.DisplayImeController;
import com.android.wm.shell.common.TransactionPool;

import dagger.Module;
import dagger.Provides;

/** Provides dependencies from {@link com.android.wm.shell} for CarSystemUI. */
@Module(includes = WMShellBaseModule.class)
public class CarWMShellModule {
    @SysUISingleton
    @Provides
    DisplayImeController provideDisplayImeController(Context context,
            IWindowManager wmService, DisplayController displayController,
            @Main Handler mainHandler, TransactionPool transactionPool) {
        return new DisplaySystemBarsController.Builder(context, wmService, displayController,
                mainHandler, transactionPool).build();
    }

    /** TODO(b/150319024): PipMenuActivity will move to a Window */
    @SysUISingleton
    @PipMenuActivityClass
    @Provides
    Class<?> providePipMenuActivityClass() {
        return PipMenuActivity.class;
    }
}