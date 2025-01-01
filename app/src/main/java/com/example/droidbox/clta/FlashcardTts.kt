package com.example.droidbox.clta

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class FlashcardTts(context: Context, private val initialLanguage: String, private val initialSpeechRate: Float) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech = TextToSpeech(context, this)
    private var isInitialized = false

    // Supported languages
    val supportedLanguages: Map<String, Locale> = mapOf(
        "English" to Locale.ENGLISH,
        "Spanish" to Locale("es", "ES"),
        "Filipino" to Locale("fil", "PH"),
        "Korean" to Locale.KOREAN,
        "French" to Locale.FRENCH,
        "German" to Locale.GERMAN,
        "Italian" to Locale.ITALIAN,
        "Japanese" to Locale.JAPANESE,
        "Chinese (Simplified)" to Locale.SIMPLIFIED_CHINESE,
        "Chinese (Traditional)" to Locale.TRADITIONAL_CHINESE,
        "Russian" to Locale("ru", "RU"),
        "Portuguese" to Locale("pt", "PT"),
        "Hindi" to Locale("hi", "IN"),
        "Arabic" to Locale("ar", "AE"),
        "Thai" to Locale("th", "TH"),
        "Dutch" to Locale("nl", "NL"),
        "Greek" to Locale("el", "GR"),
        "Swedish" to Locale("sv", "SE"),
        "Turkish" to Locale("tr", "TR"),
        "Vietnamese" to Locale("vi", "VN")
    )

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            changeLanguage(initialLanguage)
            adjustSpeechRate(initialSpeechRate)
            isInitialized = true
        } else {
            Log.e("FlashcardTts", "TTS initialization failed.")
        }
    }

    fun speak(text: String) {
        if (isInitialized) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            Log.e("FlashcardTts", "TTS is not initialized.")
        }
    }

    fun changeLanguage(language: String) {
        val locale = supportedLanguages[language]
        if (locale != null) {
            tts.language = locale
            Log.d("TTS", "Language changed to $language with locale $locale")
        } else {
            Log.e("TTS", "Language $language is not supported.")
        }
    }

    fun adjustSpeechRate(rate: Float) {
        tts.setSpeechRate(rate)
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }

    fun getTts(): TextToSpeech {
        return tts
    }
}