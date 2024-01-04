package com.hfad.minegame

/**
 * Difficultyfragment is a middleground fragment that decides what difficulty the game will be set to,
 * easy, medium or hard that increase the amount of tiles and mines.
 */

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.hfad.minegame.databinding.FragmentDifficultyBinding

class DifficultyFragment : Fragment() {
    private lateinit var binding: FragmentDifficultyBinding
    private var rows : Int = 0
    private var columns : Int = 0
    private var mines : Int = 0
    private var level : String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDifficultyBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.easy.setOnClickListener {createGame(8,8,10, "Easy")}
        binding.medium.setOnClickListener {createGame(12, 12, 26, "Medium")}
        binding.hard.setOnClickListener {createGame(18, 12, 38, "Hard")}

        binding.questionButton.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("Choose your desired difficulty. \n Easy will display a 8x8 gameboard with 10 mines. " +
                    "\n Medium will display a 12x12 gameboard with 26 mines. " +
                    "\n Hard will display a 18x12 gameboard with 38 mines.").setCancelable(true)

            val alert = builder.create()
            alert.show()}

        binding.homeButton.setOnClickListener {
                    view.findNavController().navigate(R.id.welcomeFragment)
                }
        return view
    }

    /**
     * createGame sends the gameboard parameters (amount of rows, columns, mines and the
     * chosen difficulty to the GameFragment using SafeArgs.
     */
    private fun createGame(row : Int, column : Int, mine : Int, levels : String) {
        this.rows = row
        this.columns = column
        this.mines = mine
        this.level = levels

        val action = DifficultyFragmentDirections.actionDifficultyFragmentToGameFragment(rows, columns, mines, level)
        view?.findNavController()?.navigate(action)
    }

}