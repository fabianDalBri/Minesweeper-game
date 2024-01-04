package com.hfad.minegame

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
            builder.setMessage("Choose your desired difficulty. Easy will display a 8*8 gameboard with 10 mines.").setCancelable(true)

            val alert = builder.create()
            alert.show()}

        binding.homeButton.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            //builder sets alert dialog message
            builder.setMessage("Are you sure you want to go to Home?")
                .setCancelable(false)
                .setPositiveButton("Yes") { _, _ ->
                    view.findNavController().navigate(R.id.welcomeFragment)
                }
                .setNegativeButton("No") { dialog, _ ->
                    // Dismiss the dialog
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }
        // Inflate the layout for this fragment
        return view
    }

    private fun createGame(row : Int, column : Int, mine : Int, levels : String) {
        this.rows = row
        this.columns = column
        this.mines = mine
        this.level = levels

        val action = DifficultyFragmentDirections.actionDifficultyFragmentToGameFragment(rows, columns, mines, level)
        view?.findNavController()?.navigate(action)
    }

}