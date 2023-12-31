package com.hfad.minegame
/**
    Startfragment with game-menu for the games different options, start game or see the leaderboard.
 */

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.findNavController

class WelcomeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_welcome, container, false)
        val startButton = view.findViewById<Button>(R.id.start_game)
        val leaderboardButton = view.findViewById<Button>(R.id.leaderboard)

        startButton.setOnClickListener {
        view.findNavController().navigate(R.id.action_welcomeFragment_to_difficultyFragment)
         }

        leaderboardButton.setOnClickListener{
            view.findNavController().navigate(R.id.action_welcomeFragment_to_leaderboardFragment)
        }

        return view
    }
}





