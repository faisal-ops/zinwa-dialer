package com.zinwa.dialer.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.zinwa.dialer.MainActivity

/**
 * AccessibilityService that intercepts the BB toolbar Call and End hardware
 * buttons. These keys (KEYCODE_CALL / KEYCODE_ENDCALL) are normally consumed
 * by the system before reaching the activity — this service catches them first.
 *
 * The user must enable this service in Settings → Accessibility.
 */
class ButtonInterceptService : AccessibilityService() {

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_CALL -> {
                    val callback = ToolbarButtonHandler.onCallPressed
                    if (callback != null) {
                        callback.invoke()
                    } else {
                        openDefaultDialer()
                    }
                    return true
                }
                KeyEvent.KEYCODE_ENDCALL -> {
                    ToolbarButtonHandler.onEndPressed?.invoke()
                    return true
                }
            }
        }
        return super.onKeyEvent(event)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not used — we only need key event interception
    }

    override fun onInterrupt() {}

    private fun openDefaultDialer() {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                action = Intent.ACTION_MAIN
                putExtra(EXTRA_CALL_BUTTON_PRESSED, true)
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                )
            }
        )
    }

    companion object {
        const val EXTRA_CALL_BUTTON_PRESSED = "call_button_pressed"
    }
}
