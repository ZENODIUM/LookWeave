package com.vizeno.loomie.ui

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
internal fun VoiceWave(active: Boolean, level: Float, onActivate: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(160.dp).semantics { contentDescription = if (active) "End voice conversation" else "Start voice conversation" }.clickable(role = Role.Button, onClick = onActivate)) {
        AndroidView(factory = { context ->
            WebView(context).apply {
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                settings.javaScriptEnabled = true
                settings.blockNetworkLoads = true
                settings.allowContentAccess = false
                importantForAccessibility = android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO
                setOnTouchListener { _, _ -> true }
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String?) {
                        view.evaluateJavascript(view.tag as? String ?: "window.updateVoice(false,0)", null)
                    }
                }
                loadUrl("file:///android_asset/voice/index.html")
            }
        }, update = { view ->
            val update = "window.updateVoice && window.updateVoice($active,${level.coerceIn(0f, 1f)})"
            view.tag = update
            view.evaluateJavascript(update, null)
        }, onRelease = { it.destroy() }, modifier = Modifier.fillMaxWidth().height(160.dp))
        // The overlay owns taps; the bundled renderer only visualizes SDK audio.
        Box(Modifier.fillMaxWidth().height(160.dp).clickable(role = Role.Button, onClick = onActivate))
    }
}
