package com.ipleiria.mothertongue.translations

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions

class TranslatorService(private val finalLang: Int) {

    fun translate(text: String): Task<String> {

        // Create an English-anyLanguage translator:
        val options = FirebaseTranslatorOptions.Builder()
            .setSourceLanguage(FirebaseTranslateLanguage.EN)
            .setTargetLanguage(finalLang)
            .build()
        val englishTranslator = FirebaseNaturalLanguage.getInstance().getTranslator(options)

        //ToDO: not a good idea to download the model here while tha camera is open, move that to main activity
        //downloadModelIfNeeded()
        return englishTranslator.translate(text)

    }
}