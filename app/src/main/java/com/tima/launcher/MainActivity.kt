package com.tima.launcher

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * TiMa home screen — the box boots directly here (see AndroidManifest: HOME category).
 * Three tiles: Game, Software, AI. This screen owns ONLY navigation/branding —
 * all the real work (GPU session, video decode, chat) happens in the activities it opens.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<android.widget.FrameLayout>(R.id.tileGame).apply {
            setOnClickListener { openStream(StreamTarget.GAME) }
            requestFocus() // first thing a D-Pad user lands on
        }

        findViewById<android.widget.FrameLayout>(R.id.tileSoftware).setOnClickListener {
            openStream(StreamTarget.SOFTWARE)
        }

        findViewById<android.widget.FrameLayout>(R.id.tileAi).setOnClickListener {
            startActivity(Intent(this, AiChatActivity::class.java))
        }
    }

    private fun openStream(target: StreamTarget) {
        val intent = Intent(this, StreamActivity::class.java)
        intent.putExtra(StreamActivity.EXTRA_TARGET, target.name)
        startActivity(intent)
    }
}

enum class StreamTarget { GAME, SOFTWARE }
