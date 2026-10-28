package id.zxtra.magictilesbot

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout

class FloatingControlService : Service() {
    private var wm: WindowManager? = null; private var view: LinearLayout? = null
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onCreate() { super.onCreate(); wm = getSystemService(WINDOW_SERVICE) as WindowManager
        view = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(8,8,8,8); setBackgroundColor(Color.rgb(225,242,255)) }
        val toggle = Button(this).apply { text = "BOT ON / OFF"; isAllCaps = false; setOnClickListener { BotState.running = !BotState.running; text = if (BotState.running) "BOT ON" else "BOT OFF" } }
        val stop = Button(this).apply { text = "STOP"; isAllCaps = false; setOnClickListener { stopService(Intent(this@FloatingControlService, ScreenBotService::class.java)); stopSelf() } }
        view!!.addView(toggle); view!!.addView(stop)
        val type = if (Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else @Suppress("DEPRECATION") (WindowManager.LayoutParams.TYPE_PHONE)
        val params = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, type, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP or Gravity.END; x = 12; y = 160 }
        try { wm?.addView(view, params) } catch (_: Exception) {}
    }
    override fun onDestroy() { try { wm?.removeView(view) } catch (_: Exception) {}; super.onDestroy() }
}
