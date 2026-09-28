package com.tima.launcher

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.concurrent.thread

/**
 * Full-screen viewer for a running Game or Software session on Abr Ferdowsi's
 * cloud. Shows a "Connecting..." state (this is your real Time-to-First-Session
 * KPI, unlike the earlier local demo) then loads their session URL fullscreen.
 */
class StreamActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TARGET = "target"
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val targetName = intent.getStringExtra(EXTRA_TARGET) ?: StreamTarget.GAME.name
        val target = StreamTarget.valueOf(targetName)

        val root = FrameLayout(this)
        val statusText = TextView(this).apply {
            text = getString(R.string.connecting)
            setTextColor(getColor(R.color.tima_text))
            textSize = 20f
        }
        val progress = ProgressBar(this)
        val statusLayout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            addView(progress)
            addView(statusText)
        }
        root.addView(statusLayout)
        setContentView(root)

        val webView = WebView(this).apply {
            visibility = View.GONE
            settings.javaScriptEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    statusLayout.visibility = View.GONE
                    visibility = View.VISIBLE
                }
            }
        }
        root.addView(webView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)

        // Network call must not run on the main thread.
        thread {
            val result = StreamConfig.requestSessionUrl(target)
            runOnUiThread {
                result.onSuccess { url ->
                    webView.loadUrl(url)
                }.onFailure { err ->
                    statusText.text = "اتصال ناموفق: ${err.message}\nبرگشت با دکمهٔ Back"
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // TODO: call Abr Ferdowsi's "end session" endpoint here if they require
        // an explicit teardown call (matches the Session Manager teardown step
        // in the platform architecture — see SRS section 7).
    }
}
