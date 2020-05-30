package com.ipleiria.mothertongue

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.ipleiria.mothertongue.data_manager.Game
import com.ipleiria.mothertongue.databinding.ActivityGameStatusBinding


class GameStatusActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameStatusBinding
    private lateinit var learntWordsAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_status)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_game_status)
    }

    override fun onStart() {
        super.onStart()

        getDashboardData()
    }

    private fun getDashboardData() {
        binding.scoreValueTextView.text = getScore().toString()
        binding.languagesLearntValueTextView.text = getLearningLanguages().toString()
        binding.learntWordsValueTextView.text = getLearntWords().toString()

        getLearnWordsList()
    }


    private fun getScore(): Int {
        return Game.gameStatus.getScore()
    }

    private fun getLearningLanguages(): Int {
        return Game.gameStatus.gameLevels.distinctBy { it.firebaseLanguage }.size
    }

    private fun getLearntWords(): Int {
        var count = 0
        Game.gameStatus.gameLevels.forEach {
            it.gamePhrases.forEach {
                it
                if (it.wasGuessed) count++
            }
        }
        return count
    }

    private fun getLearnWordsList() {
        val learnWordsArray: List<String> =
            Game.gameStatus.gameLevels.flatMap { it.gamePhrases.map { it.takeIf { it.wasGuessed }?.phrase } }
                .filterNotNull().sorted() //Another example why I love kotlin

        learntWordsAdapter =
            ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, learnWordsArray)
        binding.learntWordsListView.adapter = learntWordsAdapter


    }
}
