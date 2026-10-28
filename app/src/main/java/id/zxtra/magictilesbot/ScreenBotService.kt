package id.zxtra.magictilesbot

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.WindowManager
import kotlin.math.max

class ScreenBotService : Service() {
    private var projection: MediaProjection? = null
    private var reader: ImageReader? = null
    private var display: VirtualDisplay? = null
    private var thread: HandlerThread? = null
    private var handler: Handler? = null
    private var lastFrame = 0L
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP") { stopSelf(); return START_NOT_STICKY }
        BotState.running = true
        createNotification()
        val resultCode = intent?.getIntExtra("resultCode", Activity.RESULT_CANCELED) ?: Activity.RESULT_CANCELED
        val data = if (Build.VERSION.SDK_INT >= 33) intent?.getParcelableExtra("data", Intent::class.java) else @Suppress("DEPRECATION") (intent?.getParcelableExtra("data") as? Intent)
        if (resultCode != Activity.RESULT_OK || data == null) { stopSelf(); return START_NOT_STICKY }
        val mgr = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        projection = mgr.getMediaProjection(resultCode, data)
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION") val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        @Suppress("DEPRECATION") wm.defaultDisplay.getRealMetrics(metrics)
        val w = metrics.widthPixels; val h = metrics.heightPixels; val dpi = metrics.densityDpi
        thread = HandlerThread("tile-detector").also { it.start() }; handler = Handler(thread!!.looper)
        reader = ImageReader.newInstance(w, h, PixelFormat.RGBA_8888, 2)
        reader!!.setOnImageAvailableListener({ r ->
            val now = System.currentTimeMillis()
            if (now - lastFrame < 45) { r.acquireLatestImage()?.close(); return@setOnImageAvailableListener }
            lastFrame = now
            val image = r.acquireLatestImage() ?: return@setOnImageAvailableListener
            try { detect(image, w, h) } catch (_: Exception) { } finally { image.close() }
        }, handler)
        display = projection?.createVirtualDisplay("MagicTilesBotCapture", w, h, dpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, reader!!.surface, null, handler)
        return START_NOT_STICKY
    }
    private fun detect(image: Image, width: Int, height: Int) {
        val service = TileAccessibilityService.instance ?: return
        val plane = image.planes[0]; val buffer = plane.buffer; val pixelStride = plane.pixelStride; val rowStride = plane.rowStride
        val rowPadding = rowStride - pixelStride * width
        val paddedWidth = width + rowPadding / pixelStride
        val bmp = Bitmap.createBitmap(paddedWidth, height, Bitmap.Config.ARGB_8888)
        bmp.copyPixelsFromBuffer(buffer)
        val lanes = 4
        val xStart = (width * 0.04f).toInt(); val laneWidth = (width * 0.92f / lanes).toInt()
        // Look only in the playfield's middle/lower area, avoiding top UI and bottom navigation.
        val yTop = (height * 0.28f).toInt(); val yBottom = (height * 0.79f).toInt()
        val stepY = max(3, height / 180); val stepX = max(3, width / 100)
        for (lane in 0 until lanes) {
            val left = xStart + lane * laneWidth + (laneWidth * 0.18f).toInt()
            val right = xStart + lane * laneWidth + (laneWidth * 0.82f).toInt()
            // Search from bottom upwards; a tile is a broad, dark patch with several dark samples.
            var candidateY = -1
            var y = yBottom
            while (y >= yTop) {
                var dark = 0; var samples = 0
                var x = left
                while (x <= right) { val c = bmp.getPixel(x.coerceIn(0,paddedWidth-1), y); val lum = ((c shr 16 and 255)*299 + (c shr 8 and 255)*587 + (c and 255)*114)/1000; if (lum < 58) dark++; samples++; x += stepX }
                if (samples > 0 && dark.toFloat()/samples > 0.72f) { candidateY = y; break }
                y -= stepY
            }
            if (candidateY > 0) {
                // Tap only when a dark region is detected at a lane's scan row; per-lane cooldown avoids repeats.
                val key = lane
                val now = System.currentTimeMillis()
                if (now - laneLastTap[key] > BotState.tapDelay + 55) {
                    service.tap((xStart + lane*laneWidth + laneWidth/2).toFloat(), candidateY.toFloat())
                    laneLastTap[key] = now
                }
            }
        }
        bmp.recycle()
    }
    private val laneLastTap = LongArray(4)
    private fun createNotification() {
        val channel = "magic_tiles_bot"
        if (Build.VERSION.SDK_INT >= 26) { val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager; nm.createNotificationChannel(NotificationChannel(channel, "Magic Tiles Bot", NotificationManager.IMPORTANCE_LOW)) }
        val n = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(this, channel).setContentTitle("Magic Tiles 3 Bot aktif").setContentText("Deteksi layar berjalan").setSmallIcon(android.R.drawable.ic_media_play).build() else @Suppress("DEPRECATION") (Notification.Builder(this).setContentTitle("Magic Tiles 3 Bot aktif").setContentText("Deteksi layar berjalan").setSmallIcon(android.R.drawable.ic_media_play).build())
        startForeground(93, n)
    }
    override fun onDestroy() { BotState.running = false; display?.release(); reader?.close(); projection?.stop(); thread?.quitSafely(); super.onDestroy() }
}
