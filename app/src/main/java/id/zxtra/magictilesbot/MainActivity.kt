package id.zxtra.magictilesbot

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.*
import android.graphics.Color
import android.graphics.Typeface

class MainActivity : Activity() {
    private val projectionRequest = 731
    private lateinit var status: TextView
    private lateinit var speed: SeekBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(234,246,255)
        window.navigationBarColor = Color.WHITE
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(24, 26, 24, 24); setBackgroundColor(Color.rgb(245,250,255)) }
        val title = TextView(this).apply { text = "Magic Tiles 3 Bot"; textSize = 27f; setTextColor(Color.rgb(20,55,90)); typeface = Typeface.DEFAULT_BOLD }
        val subtitle = TextView(this).apply { text = "AUTO PLAY • SCREEN DETECTION"; textSize = 12f; setTextColor(Color.rgb(25,130,220)); setPadding(0,6,0,18) }
        root.addView(title); root.addView(subtitle)
        val card = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(18,18,18,18); setBackgroundColor(Color.WHITE) }
        status = TextView(this).apply { text = "Status: siap diatur"; textSize = 16f; setTextColor(Color.rgb(30,60,90)); setPadding(0,0,0,12) }
        card.addView(status)
        val enableA11y = button("1. Aktifkan Accessibility") { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        val overlay = button("2. Izinkan Floating Overlay") { if (!Settings.canDrawOverlays(this)) startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))) else toast("Izin overlay sudah aktif") }
        val start = button("3. MULAI AUTO PLAY") { startCapture() }
        val stop = button("STOP BOT") { stopService(Intent(this, ScreenBotService::class.java)); stopService(Intent(this, FloatingControlService::class.java)); status.text = "Status: dihentikan" }
        card.addView(enableA11y); card.addView(overlay)
        val speedLabel = TextView(this).apply { text = "Kecepatan ketukan: 55 ms"; setTextColor(Color.DKGRAY); setPadding(0,12,0,0) }
        speed = SeekBar(this).apply { max = 180; progress = 55; setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener { override fun onProgressChanged(s: SeekBar?, p: Int, u: Boolean) { val v = p.coerceAtLeast(20); speedLabel.text = "Kecepatan ketukan: $v ms"; BotState.tapDelay = v.toLong() }; override fun onStartTrackingTouch(s: SeekBar?) {}; override fun onStopTrackingTouch(s: SeekBar?) {} }) }
        card.addView(speedLabel); card.addView(speed); card.addView(start); card.addView(stop)
        root.addView(card)
        val info = TextView(this).apply { text = "Cara pakai\n• Buka Accessibility dan aktifkan layanan bot.\n• Izinkan overlay jika ingin panel mengambang.\n• Tekan Mulai, izinkan perekaman layar, lalu buka Magic Tiles 3.\n\nTips: mulai dengan kecepatan sedang. Deteksi gambar bisa keliru pada efek, menu, atau mode game tertentu. Jangan gunakan untuk mengakali kompetisi online."; textSize = 13f; setTextColor(Color.rgb(70,90,110)); setPadding(2,18,2,0) }
        root.addView(info)
        setContentView(ScrollView(this).apply { addView(root) })
    }
    private fun button(text: String, action: () -> Unit) = Button(this).apply { this.text = text; isAllCaps = false; setTextColor(Color.rgb(20,70,120)); setOnClickListener { action() } }
    private fun startCapture() {
        if (!Settings.canDrawOverlays(this)) { toast("Aktifkan izin overlay dulu"); startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))); return }
        val mgr = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(mgr.createScreenCaptureIntent(), projectionRequest)
    }
    @Deprecated("Deprecated Android callback kept for broad compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { super.onActivityResult(requestCode, resultCode, data); if (requestCode == projectionRequest && resultCode == RESULT_OK && data != null) { val i = Intent(this, ScreenBotService::class.java).putExtra("resultCode", resultCode).putExtra("data", data).putExtra("tapDelay", BotState.tapDelay); if (Build.VERSION.SDK_INT >= 26) startForegroundService(i) else startService(i); startService(Intent(this, FloatingControlService::class.java)); status.text = "Status: Auto Play aktif — buka game" } }
    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
}
