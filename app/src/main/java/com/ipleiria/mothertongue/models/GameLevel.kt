package com.ipleiria.mothertongue.models

data class GameLevel(
    var firebaseLanguage: Int, //FirebaeLanguage Enum
    var gamePhrases: ArrayList<GamePhrase>
) {
}