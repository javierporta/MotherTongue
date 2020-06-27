package com.ipleiria.mothertongue.ui.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.ipleiria.mothertongue.R
import com.ipleiria.mothertongue.data_manager.Game
import com.ipleiria.mothertongue.databinding.FragmentGameProfileBinding
import com.ipleiria.mothertongue.databinding.FragmentHomeBinding


class GameProfile : Fragment() {

    private lateinit var binding: FragmentGameProfileBinding
    private lateinit var learntWordsAdapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate<FragmentGameProfileBinding>(inflater, R.layout.fragment_game_profile, container, false)


        return binding.root
    }

    override fun onStart() {
        super.onStart()

        binding.usernameTextView.setText(Game.gameStatus.username)

        getDashboardData()
    }

    override fun onPause() {
        super.onPause()
        this.saveUsername()
    }

    private fun saveUsername() {
        Game.gameStatus.username = binding.usernameTextView.text.toString()
        Game.saveGame(this.activity?.applicationContext!!)
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
            ArrayAdapter<String>(
                this.activity!!.applicationContext,
                android.R.layout.simple_list_item_1,
                learnWordsArray
            )
        binding.learntWordsListView.adapter = learntWordsAdapter

    }

}
