package com.hfad.minegame
/**
    Startfragment with game-menue for the games different opptions.
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
        startButton.setOnClickListener {
        view.findNavController().navigate(R.id.action_welcomeFragment_to_gameFragment)

         }
        // Inflate the layout for this fragment
        return view

        //easyButton onclicklistener { createBoardActivity(8,8,10) }
        //MediumButton onclicklistener { createBoardActivity(?,?,?) }
        //HardButton onclicklistener { createBoardActivity(?,?,?) }
    }
    /*
    fun createBoardActivity(height: Int, width: Int, numberOfMines: Int){
        skicka med input på något vis till gameViewModel.
    }
     */
}





