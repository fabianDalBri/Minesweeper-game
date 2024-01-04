package com.hfad.minegame
/**
 * Gamefragment contains all the game logic like game state and variables and
 * will display the game board that the user interacts with. This fragment also adds data to
 * the firestore data base in firebase.
 */

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.SystemClock
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Chronometer
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.hfad.minegame.databinding.FragmentGameBinding

class GameFragment : Fragment(){

    private lateinit var binding: FragmentGameBinding
    private lateinit var rootView : ConstraintLayout
    private lateinit var gameBoard : GridLayout
    private lateinit var resetBtn : Button
    private lateinit var homeBtn : Button
    private lateinit var questionBtn : Button
    private lateinit var timer : Chronometer
    private lateinit var flagCount : TextView
    private lateinit var viewModel: GameViewModel
    private lateinit var viewModelFactory : GameViewModelFactory
    private var totalSeconds = 0L
    private val db = Firebase.firestore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = FragmentGameBinding.inflate(inflater, container, false)
        val view = binding.root


        val row = GameFragmentArgs.fromBundle(requireArguments()).rows
        val col = GameFragmentArgs.fromBundle(requireArguments()).columns
        val mine = GameFragmentArgs.fromBundle(requireArguments()).mines
        val level = GameFragmentArgs.fromBundle(requireArguments()).level

        viewModelFactory = GameViewModelFactory(row, col, mine, level)
        viewModel = ViewModelProvider(this, viewModelFactory).get(GameViewModel::class.java)

        rootView = binding.rootLayout
        gameBoard = binding.gameBoard
        resetBtn = binding.resetButton
        timer = binding.timer
        flagCount = binding.flagCounter!!
        homeBtn = binding.homeButton
        questionBtn = binding.questionButton

        homeBtn.setOnClickListener{
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

        questionBtn.setOnClickListener{
            val builder = AlertDialog.Builder(context)
            builder.setMessage("How to play: \n" +
                            "Reveal all none mine tiles to win the game.\n" +
                            "Use the flag button to flag potential mines.").setCancelable(true)

            val alert = builder.create()
            alert.show()
        }
        
        resetBtn.setOnClickListener{
            if(viewModel.isTimerRunning) {
                timer.stop()
                viewModel.isTimerRunning = false
            }
            viewModel.elapsedTime = 0L
            setBaseTime()
            initiateGame()
        }

        if(!viewModel.isTimerRunning) {
            //viewModel.elapsedTime = 0L
            initiateGame()
        }else{
            timer.start()
            setBaseTime()
            setUpGame()
        }
        // Inflate the layout for this fragment
        return view
    }

    private fun initiateGame() {
        viewModel.firstClick = true
        resetBoard()
        plantMines()
        calculateNumbers()
        setUpGame()
    }

    /**
     * Creates the game board and adds on click listeners to each tile click.
     */
    private fun setUpGame() {
        countFlags()
        for (array in viewModel.gameBoardCells)
            for (elements in array) {
                elements.row = viewModel.gameBoardCells.indexOf(array)
                elements.col = array.indexOf(elements)

                val newView = ImageView(context)

                elements.tileView = newView
                gameBoard.addView(newView)
                gameBoard.rowCount = viewModel.rows
                gameBoard.columnCount = viewModel.columns
                newView.layoutParams.height = 80
                newView.layoutParams.width = 80
                setDrawables(newView, elements)

                    newView.setOnClickListener(View.OnClickListener {
                        if(!binding.switchButton.isChecked) {
                            if (!elements.isRevealed && !elements.isFlagged) {
                                revealCell(elements.row, elements.col)
                                gameWon()
                            }
                            if (viewModel.firstClick && !elements.isMine) {
                                setBaseTime()
                                timer.start()
                                viewModel.firstClick = false
                                viewModel.isTimerRunning = true
                            }
                        } else {
                            toggleFlag(elements)
                            countFlags()
                        }
                    })
            }
    }

    private fun toggleFlag(currentTile: Tile) {
        currentTile.toggleFlag()
        updateBoard(currentTile)
    }

    private fun countFlags() {
        var counter = 0
        val tiles = viewModel.gameBoardCells.flatten()
        tiles.filter { it.isFlagged }.forEach { _ -> counter++}
        viewModel.flagCount = counter
        val amountOfFlags = viewModel.mines - viewModel.flagCount
        flagCount.text = amountOfFlags.toString()


    }

    private fun setDrawables(currentView : ImageView, currentTile : Tile){
        lateinit var image : Drawable
        image = when(currentTile.state) {
            Tile.State.MINE -> resources.getDrawable(R.drawable.mine_tile)
            Tile.State.FLAGGED -> resources.getDrawable(R.drawable.flag_tile)
            Tile.State.HIDDEN -> resources.getDrawable(R.drawable.tile_hidden)
            Tile.State.NUMBERED -> numberedTile(currentTile.numberOfMinedNeighbours)
        }
        currentView.setImageDrawable(image)
    }
    private fun updateBoard(currentTile : Tile) {
        val imView = currentTile.tileView
        setDrawables(imView, currentTile)
    }

    private fun revealBoard(){
        val revealAll = viewModel.gameBoardCells.flatten()
        for (tile in revealAll) {
            if (!tile.isRevealed) {
                tile.reveal()
            }
            updateBoard(tile)
        }
    }

    private fun gameOver(currentTile : Tile) {
        revealBoard()
        currentTile.tileView.setImageDrawable(resources.getDrawable(R.drawable.mine_detonated))
        timer.stop()
        viewModel.isTimerRunning = false
        viewModel.isGameOver = true
        //viewModel.isRunning = false
        viewModel.elapsedTime = 0L


    }

    /**
     * Each time a tile is clicked gameWon method checks if all tiles are revealed
     * except the mines.
     */
    private fun gameWon(){
        var revealedTiles = 0
        val totalAmountOfTiles : Int = viewModel.rows*viewModel.columns
        for(array in viewModel.gameBoardCells){
            for (elements in array){
                if (elements.isRevealed)
                    revealedTiles++
            }
        }
        if (revealedTiles == totalAmountOfTiles - viewModel.mines){
            revealBoard()
            timer.stop()
            viewModel.isTimerRunning = false
            var time = elapsedTime()
            val builder = AlertDialog.Builder(context)
            val input = EditText(context)
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)
            builder.setMessage("You won! $time \n"+"Please enter your username: " )
                .setPositiveButton("Confirm") { _, _ ->
                    firebase(input.text.toString())
                }
            viewModel.isGameOver = true
            viewModel.elapsedTime = 0L

            val alert = builder.create()
            alert.show()
        }
    }

    private fun firebase(playerName : String) {
        // create a player and it's time
        //val elapsedTime = SystemClock.elapsedRealtime() - timer.base
        //val totalSeconds = elapsedTime / 1000

        val user = hashMapOf(
            "Player name" to playerName,
            "Time in S" to totalSeconds,
            "Difficulty" to viewModel.difficulty
        )

        // Add a new document with a generated ID
        db.collection("Players")
            .add(user)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }

    private fun elapsedTime(): String {
        val elapsedTime = SystemClock.elapsedRealtime() - timer.base
        totalSeconds = elapsedTime/1000
        val minutes = totalSeconds/60
        val seconds = totalSeconds%60
        return("Your time was $minutes minutes and $seconds seconds")
    }

    private fun resetBoard() {
        viewModel.gameBoardCells.flatten().filter { it.isMine }.forEach { tile -> tile.removeMine() }
        viewModel.gameBoardCells.flatten().filter { it.isRevealed }.forEach { tile -> tile.hide() }
        viewModel.gameBoardCells.flatten().filter { it.isFlagged }.forEach { tile -> tile.toggleFlag() }
        gameBoard.removeAllViews()
        setBaseTime()
    }

    private fun revealCell(row : Int, col : Int) {
        val currentTile = viewModel.gameBoardCells[row][col]
        if (!currentTile.isRevealed && !currentTile.isFlagged) {
            currentTile.reveal()
            if (currentTile.numberOfMinedNeighbours == 0) {
                revealAdjacentCells(row, col)
            }
            updateBoard(currentTile)
        }
        if (currentTile.isMine) {
            gameOver(currentTile)
        }
    }

    private fun revealAdjacentCells(row: Int, col: Int) {
        for (i in -1..1) {
            for (j in -1..1) {
                val newRow = row + i
                val newCol = col + j
                if (newRow in 0 until viewModel.rows && newCol in 0 until viewModel.columns && !viewModel.gameBoardCells[newRow][newCol].isRevealed) {
                    revealCell(newRow, newCol)
                }
            }
        }
    }

    /**
     * Give a tile the correct number based on how many mines are nearby
     */
    private fun numberedTile(number: Int): Drawable = when (number) {
        0 -> resources.getDrawable(R.drawable.numbered_tile_0)
        1 -> resources.getDrawable(R.drawable.numbered_tile_1)
        2 -> resources.getDrawable(R.drawable.numbered_tile_2)
        3 -> resources.getDrawable(R.drawable.numbered_tile_3)
        4 -> resources.getDrawable(R.drawable.numbered_tile_4)
        5 -> resources.getDrawable(R.drawable.numbered_tile_5)
        6 -> resources.getDrawable(R.drawable.numbered_tile_6)
        7 -> resources.getDrawable(R.drawable.numbered_tile_7)
        8 -> resources.getDrawable(R.drawable.numbered_tile_8)
        else -> {
            resources.getDrawable(R.mipmap.ic_launcher)
        }
    }

    private fun plantMines() : List<Tile>{
        val allTiles = viewModel.gameBoardCells.flatten()
        var counter = 0
        while(counter < viewModel.mines) {
            val randomTile = allTiles.random()
            if(!randomTile.isMine) {
                randomTile.plantMine()
                counter++
            }
        }
        return allTiles
    }

    private fun calculateNumbers() {
        var testNum = ""
        for (row in 0 until viewModel.rows) {
            for (col in 0 until viewModel.columns) {
                if (!viewModel.gameBoardCells[row][col].isMine) {
                    val count = countAdjacentMines(row, col)
                    testNum += count
                    viewModel.gameBoardCells[row][col].numberOfMinedNeighbours = count
                }
            }
        }
    }

    private fun countAdjacentMines(row: Int, col: Int): Int {
        var count = 0
        for (i in -1..1) {
            for (j in -1..1) {
                val newRow = row + i
                val newCol = col + j
                if (newRow in 0 until viewModel.rows && newCol in 0 until viewModel.columns && viewModel.gameBoardCells[newRow][newCol].isMine) {
                    count++
                }
            }
        }
        return count
    }
    private fun setBaseTime() {
        binding.timer.base = SystemClock.elapsedRealtime() - viewModel.elapsedTime
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if(viewModel.isTimerRunning)
            viewModel.elapsedTime = SystemClock.elapsedRealtime() - timer.base
    }
}
