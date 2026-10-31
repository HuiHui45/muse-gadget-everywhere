package ai.muse.gadgeteverywhere

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import ai.muse.gadgeteverywhere.Ui.dp

/**
 * Bottom "Muse 正在回复…" pill shown while TTS speaks.
 * A system overlay (TYPE_APPLICATION_OVERLAY): small, bottom-anchored,
 * non-focusable and non-touchable, so it never blocks video playback.
 * Auto-dismissed when the utterance completes, errors, or is stopped.
 */
object SpeechOverlay {
    private var pill: View? = null
    private var manager: WindowManager? = null

    @Synchronized
    fun show(context: Context) {
        hide()
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(context)) return
        val app = context.applicationContext
        val wm = app.getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return
        val label = TextView(app).apply {
            text = app.getString(R.string.speech_replying)
            textSize = 15f
            setTextColor(app.resources.getColor(R.color.accent_ink))
            gravity = Gravity.CENTER
        }
        val row = LinearLayout(app).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = app.dp(24).toFloat()
                setColor(app.resources.getColor(R.color.accent))
            }
            val padH = app.dp(22); val padV = app.dp(12)
            setPadding(padH, padV, padH, padV)
            addView(label)
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            y = app.dp(64)
            x = app.dp(48)
        }
        try {
            wm.addView(row, params)
            pill = row
            manager = wm
        } catch (_: Exception) {
            pill = null; manager = null
        }
    }

    @Synchronized
    fun hide() {
        val wm = manager; val view = pill
        pill = null; manager = null
        if (wm != null && view != null) {
            try { wm.removeView(view) } catch (_: Exception) { }
        }
    }
}
