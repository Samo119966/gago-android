
package com.gago.assistant

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.view.Gravity
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var status: TextView
    private lateinit var speakButton: Button
    private var tts: TextToSpeech? = null

    private val requestCode = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this) { result ->
            if (result == TextToSpeech.SUCCESS) {
                tts?.language = Locale("ru", "RU")
            }
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 40, 32, 40)
        }

        val title = TextView(this).apply {
            text = "ГА́ГО"
            textSize = 36f
            gravity = Gravity.CENTER
        }

        status = TextView(this).apply {
            text = "Привет! Я Гаго. Я готов."
            textSize = 20f
            gravity = Gravity.CENTER
            setPadding(10, 40, 10, 40)
        }

        speakButton = Button(this).apply {
            text = "🎤 Говорить с Гаго"
            textSize = 20f
            setOnClickListener { listen() }
        }

        layout.addView(title)
        layout.addView(status)
        layout.addView(speakButton)

        setContentView(layout)
    }

    private fun listen() {
        if (checkSelfPermission(
                android.Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(android.Manifest.permission.RECORD_AUDIO),
                requestCode
            )
            return
        }

        startListening()
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Гаго слушает...")
        }

        try {
            status.text = "Слушаю тебя..."
            startActivityForResult(intent, requestCode + 1)
        } catch (e: Exception) {
            status.text = "Не удалось запустить распознавание речи"
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == this.requestCode + 1 &&
            resultCode == RESULT_OK
        ) {
            val words = data?.getStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS
            )

            val question = words?.firstOrNull() ?: return

            status.text = "Ты сказал: $question"

            val answer = "Я тебя услышал. Ты сказал: $question"

            tts?.speak(
                answer,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "gago_answer"
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode, permissions, grantResults
        )

        if (requestCode == this.requestCode &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startListening()
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
