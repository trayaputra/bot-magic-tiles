package id.zxtra.magictilesbot

object BotState {
    @Volatile var running = true
    @Volatile var tapDelay = 55L
    @Volatile var taps = 0L
    @Volatile var lastTapAt = 0L
}
