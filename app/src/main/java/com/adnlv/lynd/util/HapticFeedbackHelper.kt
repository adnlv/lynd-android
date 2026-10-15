package com.adnlv.lynd.util

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
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    }
}
