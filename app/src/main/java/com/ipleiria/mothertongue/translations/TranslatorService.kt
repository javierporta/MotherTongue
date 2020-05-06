package com.ipleiria.mothertongue.translations

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions

class TranslatorService(private val finalLang: Int) {
    private var englishTranslator: FirebaseTranslator

    init {
        // Create an English-anyLanguage translator:
        val options = FirebaseTranslatorOptions.Builder()
            .setSourceLanguage(FirebaseTranslateLanguage.EN)
            .setTargetLanguage(finalLang)
            .build()
        englishTranslator = FirebaseNaturalLanguage.getInstance().getTranslator(options)
    }


    fun translate(text: String): Task<String> {
        return englishTranslator.translate(text)

    }

    fun downloadModelIfNeeded()
            : Task<Void> {
        return englishTranslator.downloadModelIfNeeded()
    }
}