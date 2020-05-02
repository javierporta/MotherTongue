package com.ipleiria.mothertongue.translations

interface OnGetTranslationListener {
    //this is for callbacks
    fun onSuccess(text: String?)
    fun onStart()
    fun onFailure()
}
