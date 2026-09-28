package com.tima.launcher

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/**
 * ================================================================
 *  ONLY FILE YOU NEED TO EDIT TO CONNECT TO ABR FERDOWSI'S SERVICE
 * ================================================================
 *
 * Abr Ferdowsi already runs the "software/game on cloud" service — this app's
 * job is only to display it. The one thing we don't know yet is the exact shape
 * of their API (auth method, request/response fields), so everything below is a
 * clearly-marked placeholder built around the MOST COMMON pattern for this kind
 * of service:
 *
 *   1. Your app calls Abr Ferdowsi's API to "start a session" for a given
 *      target (game / software).
 *   2. Their API returns a URL (usually a web-based viewer — WebRTC/HLS page)
 *      that renders the running session.
 *   3. Your app just displays that URL fullscreen (see StreamActivity).
 *
 * If Abr Ferdowsi instead gives you a native Android SDK/AAR to embed
 * (rather than a URL), replace requestSessionUrl() below with a call into
 * their SDK, and swap the WebView in StreamActivity for their SDK's view.
 */
object StreamConfig {

    // TODO: replace with the real base URL Abr Ferdowsi gives you
    private const val ABR_FERDOWSI_API_BASE = "https://api.abrferdowsi.example/v1"

    // TODO: replace with your real API key / auth token from Abr Ferdowsi
    private const val API_KEY = "REPLACE_ME"

    // TODO: replace with the real identifiers Abr Ferdowsi uses for your
    // provisioned game/software images on their platform
    private const val GAME_APP_ID = "REPLACE_ME_GAME_ID"       // e.g. their GTA V image id
    private const val SOFTWARE_APP_ID = "REPLACE_ME_SOFTWARE_ID" // e.g. their Photoshop/SolidWorks image id

    private val client = OkHttpClient()

    /**
     * Asks Abr Ferdowsi's platform to start (or resume) a session for [target]
     * and returns the URL to display. Runs on a background thread — call it
     * from a coroutine/thread, never the main thread.
     */
    fun requestSessionUrl(target: StreamTarget): Result<String> {
        val appId = when (target) {
            StreamTarget.GAME -> GAME_APP_ID
            StreamTarget.SOFTWARE -> SOFTWARE_APP_ID
        }

        return try {
            // TODO: adjust the endpoint path, method, and body to match
            // Abr Ferdowsi's real API contract once you have their docs.
            val request = Request.Builder()
                .url("$ABR_FERDOWSI_API_BASE/sessions?appId=$appId")
                .addHeader("Authorization", "Bearer $API_KEY")
                .post(okhttp3.RequestBody.create(null, ByteArray(0)))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return Result.failure(Exception("Session request failed: ${response.code}"))
                }
                val body = response.body?.string() ?: "{}"
                val json = JSONObject(body)
                // TODO: adjust this field name to whatever key Abr Ferdowsi
                // actually returns the viewer URL under (e.g. "streamUrl", "viewerUrl")
                val url = json.optString("streamUrl", "")
                if (url.isBlank()) Result.failure(Exception("No streamUrl in response"))
                else Result.success(url)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
