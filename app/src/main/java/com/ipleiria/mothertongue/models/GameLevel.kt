package com.ipleiria.mothertongue.models

import java.io.Serializable

data class GameLevel(
    var id: String,
    var firebaseLanguage: Int, //FirebaeLanguage Enum
    var gamePhrases: ArrayList<GamePhrase>,
    var isComplete: Boolean
) : Serializable {

}