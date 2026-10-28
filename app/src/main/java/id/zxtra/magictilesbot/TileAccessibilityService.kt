package id.zxtra.magictilesbot

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import android.os.Build

class TileAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
    override fun onServiceConnected() { super.onServiceConnected(); instance = this }
    override fun onDestroy() { if (instance === this) instance = null; super.onDestroy() }
    fun tap(x: Float, y: Float) {
        if (!BotState.running || Build.VERSION.SDK_INT < 24) return
        val now = System.currentTimeMillis()
        if (now - BotState.lastTapAt < BotState.tapDelay) return
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder().addStroke(GestureDescription.StrokeDescription(path, 0, 35)).build()
        if (dispatchGesture(gesture, null, null)) { BotState.lastTapAt = now; BotState.taps++ }
    }
    companion object { @Volatile var instance: TileAccessibilityService? = null }
}
