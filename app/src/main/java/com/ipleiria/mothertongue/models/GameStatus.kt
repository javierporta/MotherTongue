package com.ipleiria.mothertongue.models

data class GameStatus(
    var username: String,
    var totalScore: Int,
    var gameLevels: ArrayList<GameLevel>
) {
    fun calculateScore(): Int {
        var score = 0
        if (gameLevels == null || gameLevels?.size == 0) {
            return score
        } else {
            //We suppose that each word guessed has the same weight for the score (it sums 10 points)
            for (gameLevel in gameLevels!!) {
                val guessedWordsCount = gameLevel.gamePhrases.count { it.wasGuessed }
                val levelScore = guessedWordsCount * 10;
                score += levelScore
            }
            return score
        }
    }


}