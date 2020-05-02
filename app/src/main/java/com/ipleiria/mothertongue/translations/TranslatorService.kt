package com.ipleiria.mothertongue.translations

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions

class TranslatorService(val sourceLang: String, val finalLang: String) {

    fun translate(text: String): Task<String> {

        // Create an English-German translator:
        val options = FirebaseTranslatorOptions.Builder()
            .setSourceLanguage(FirebaseTranslateLanguage.EN)
            .setTargetLanguage(FirebaseTranslateLanguage.DE)
            .build()
        val englishGermanTranslator = FirebaseNaturalLanguage.getInstance().getTranslator(options)

        //ToDO: not a good idea to download the model here while tha camera is open, move that to main activity
        //downloadModelIfNeeded()
        return englishGermanTranslator.translate(text)

    }
}