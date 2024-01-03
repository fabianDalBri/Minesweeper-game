package com.hfad.minegame

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import com.hfad.minegame.databinding.FragmentDifficultyBinding

class DifficultyFragment : Fragment() {
    private lateinit var binding: FragmentDifficultyBinding
    var rows : Int = 0
    var columns : Int = 0
    var mines : Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDifficultyBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.easy.setOnClickListener() {createGame(8,8,10)}
        binding.medium.setOnClickListener(){createGame(12, 12, 26)}
        binding.hard.setOnClickListener(){createGame(12, 20, 38)}
        // Inflate the layout for this fragment
        return view
    }

    fun createGame(row : Int, column : Int, mine : Int) {
        this.rows = row
        this.columns = column
        this.mines = mine

        var action = DifficultyFragmentDirections.actionDifficultyFragmentToGameFragment(rows, columns, mines)
        view?.findNavController()?.navigate(action)
    }

}