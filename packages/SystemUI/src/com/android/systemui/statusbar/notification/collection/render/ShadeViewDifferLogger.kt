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

package com.android.systemui.statusbar.notification.collection.render

import com.android.systemui.log.LogBuffer
import com.android.systemui.log.LogLevel
import com.android.systemui.log.dagger.NotificationLog
import javax.inject.Inject

class ShadeViewDifferLogger @Inject constructor(
    @NotificationLog private val buffer: LogBuffer
) {
    fun logDetachingChild(
        key: String,
        isTransfer: Boolean,
        oldParent: String?,
        newParent: String?
    ) {
        buffer.log(TAG, LogLevel.DEBUG, {
            str1 = key
            bool1 = isTransfer
            str2 = oldParent
            str3 = newParent
        }, {
            "Detach $str1 isTransfer=$bool1 oldParent=$str2 newParent=$str3"
        })
    }

    fun logSkippingDetach(key: String, parent: String?) {
        buffer.log(TAG, LogLevel.DEBUG, {
            str1 = key
            str2 = parent
        }, {
            "Skipping detach of $str1 because its parent $str2 is also being detached"
        })
    }

    fun logAttachingChild(key: String, parent: String) {
        buffer.log(TAG, LogLevel.DEBUG, {
            str1 = key
            str2 = parent
        }, {
            "Attaching view $str1 to $str2"
        })
    }

    fun logMovingChild(key: String, parent: String, toIndex: Int) {
        buffer.log(TAG, LogLevel.DEBUG, {
            str1 = key
            str2 = parent
            int1 = toIndex
        }, {
            "Moving child view $str1 in $str2 to index $int1"
        })
    }
}

private const val TAG = "NotifViewManager"