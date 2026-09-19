package com.adnlv.lynd.util

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View

object HapticFeedbackHelper {

    fun vibratePageSwitch(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    fun vibrateRevealThreshold(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }


    fun vibratePrimaryAction(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }
}
