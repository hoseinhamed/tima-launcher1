package com.tima.launcher

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import kotlin.concurrent.thread

/**
 * The "هوش" (AI) tile — per SRS section 10, this is an in-platform conversational
 * service only, never exposed standalone. Plain text chat, no streaming/video needed,
 * so this is the simplest tile and a good first thing to get fully working end to end.
 *
 * TODO: replace API_URL / API_KEY / request-shape below with whichever LLM provider
 * TiMa ends up using (Anthropic, OpenAI, or a self-hosted model on Abr Ferdowsi's cloud).
 * The shape below follows the Anthropic Messages API as one concrete example.
 */
class AiChatActivity : AppCompatActivity() {

    // TODO: replace with the real endpoint TiMa's backend will call.
    // In production this call should go through YOUR OWN backend (not directly
    // from the device) so the API key never ships inside the app.
    private val API_URL = "https://your-tima-backend.example/api/ai-chat"

    private val client = OkHttpClient()
    private val history = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_chat)

        val chatLog = findViewById<TextView>(R.id.chatLog)
        val input = findViewById<EditText>(R.id.messageInput)
        val sendButton = findViewById<Button>(R.id.sendButton)

        sendButton.setOnClickListener {
            val text = input.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

            history.append("شما: $text\n")
            chatLog.text = history.toString()
            input.text.clear()

            thread {
                val reply = try {
                    askAi(text)
                } catch (e: Exception) {
                    "خطا در اتصال: ${e.message}"
                }
                runOnUiThread {
                    history.append("تیما: $reply\n\n")
                    chatLog.text = history.toString()
                }
            }
        }
    }

    private fun askAi(message: String): String {
        // TODO: swap this body for your backend's real expected request shape.
        val json = JSONObject().apply { put("message", message) }
        val body = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url(API_URL).post(body).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return "پاسخ ناموفق (${response.code})"
            val responseBody = response.body?.string() ?: "{}"
            // TODO: adjust this field name to whatever key your backend returns
            // the reply text under.
            return JSONObject(responseBody).optString("reply", "(پاسخ خالی)")
        }
    }
}
