package com.mirror.app

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.io.File
import java.util.concurrent.Executors

/** Native Android front end for the upstream HTTrack C engine via JNI. */
class MainActivity : Activity() {
    private val worker = Executors.newSingleThreadExecutor()
    private lateinit var status: TextView
    private lateinit var startButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (16 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }
        root.addView(TextView(this).apply { text = "Mirror"; textSize = 28f })
        root.addView(TextView(this).apply {
            text = "Website copier powered by the original HTTrack engine"
            textSize = 15f
        })
        val address = EditText(this).apply {
            hint = "https://example.com/"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                android.text.InputType.TYPE_TEXT_VARIATION_URI
            singleLine = true
        }
        root.addView(address, ViewGroup.LayoutParams(-1, -2))
        status = TextView(this).apply {
            text = "Mirrors are saved in the app's private storage. Keep the app open while a mirror runs."
            textSize = 14f
        }
        root.addView(status)
        startButton = Button(this).apply {
            text = "Start mirroring"
            setOnClickListener {
                val url = address.text.toString().trim()
                if (!(url.startsWith("https://") || url.startsWith("http://"))) {
                    status.text = "Enter a valid http:// or https:// website address."
                    return@setOnClickListener
                }
                isEnabled = false
                status.text = "Starting the original HTTrack engine…"
                val output = File(filesDir, "mirrors/${System.currentTimeMillis()}")
                worker.execute {
                    val result = try {
                        output.mkdirs()
                        NativeMirror.run(url, output.absolutePath)
                    } catch (error: UnsatisfiedLinkError) {
                        -2000
                    } catch (error: Exception) {
                        -2001
                    }
                    runOnUiThread {
                        isEnabled = true
                        status.text = when (result) {
                            0 -> "Mirror finished. Files saved in: ${output.absolutePath}"
                            -2000 -> "Native engine is not available in this build. Check the NDK/OpenSSL configuration."
                            -2001 -> "The mirror could not start because of an application error."
                            else -> "HTTrack finished with code $result. Check the URL and network connection. Output: ${output.absolutePath}"
                        }
                    }
                }
            }
        }
        root.addView(startButton)
        setContentView(ScrollView(this).apply { addView(root) })
    }

    override fun onDestroy() {
        worker.shutdown()
        super.onDestroy()
    }
}
