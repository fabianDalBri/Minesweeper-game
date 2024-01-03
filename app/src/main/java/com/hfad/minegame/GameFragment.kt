package com.hfad.minegame
/**
The gamefragment were the game will be displayed on.
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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.gms.common.internal.ServiceSpecificExtraArgs.GamesExtraArgs
import com.google.firebase.Firebase
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.hfad.minegame.databinding.FragmentGameBinding

class GameFragment() : Fragment(){

    private lateinit var binding: FragmentGameBinding
    lateinit var rootView : ConstraintLayout
    lateinit var gameboard : GridLayout
    lateinit var resetBtn : Button
    lateinit var homeBtn : Button
    lateinit var questionBtn : Button
    lateinit var timer : Chronometer
    lateinit var viewModel: GameViewModel
    lateinit var viewModelFactory : GameViewModelFactory
    var usrName : String = ""
    val db = Firebase.firestore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentGameBinding.inflate(inflater, container, false)
        val view = binding.root


        var row = GameFragmentArgs.fromBundle(requireArguments()).rows
        var col = GameFragmentArgs.fromBundle(requireArguments()).columns
        var mine = GameFragmentArgs.fromBundle(requireArguments()).mines

        viewModelFactory = GameViewModelFactory(row, col, mine)
        viewModel = ViewModelProvider(this, viewModelFactory).get(GameViewModel::class.java)

        rootView = binding.rootLayout
        gameboard = binding.gameBoard
        resetBtn = binding.resetButton
        timer = binding.timer
        homeBtn = binding.homeButton
        questionBtn = binding.questionButton

        homeBtn.setOnClickListener(){
            val builder = AlertDialog.Builder(context)
            //builder sets alert dialog message
            builder.setMessage("Are you sure you want to go to Home?")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, id ->
                    view.findNavController().navigate(R.id.welcomeFragment)
                }
                .setNegativeButton("No") { dialog, id ->
                    // Dismiss the dialog
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }

        questionBtn.setOnClickListener(){
            val builder = AlertDialog.Builder(context)
            builder.setMessage("How to play: \n" +
                            "Reveal all none mine tiles to win the game.\n" +
                            "Use the flag button to flag potential mines.").setCancelable(true)

            val alert = builder.create()
            alert.show()
        }



        resetBtn.setOnClickListener(){
            if(viewModel.isTimerRunning) {
                timer.stop()
                viewModel.isTimerRunning = false
            }
            viewModel.elapsedTime = 0L
            setBaseTime()
            initiateGame()
        }

        //Skapar bräde med celler, lista med listor rows*columns, där varje cell består
        //av objekt av typen Tile
       // gameBoardCells = viewModel.gameBoardCells

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

    fun initiateGame() {
        viewModel.firstClick = true
        resetBoard()
        plantMines()
        calculateNumbers()
        setUpGame()
    }

    /** Skapar en ny ImageView för varje Tile-objekt i spelbrädet
     *
     */
    fun setUpGame() {
        for (array in viewModel.gameBoardCells)
            for (elements in array) {
                elements.row = viewModel.gameBoardCells.indexOf(array)
                elements.col = array.indexOf(elements)

                //elements.reveal()
                val newView: ImageView = ImageView(context)

                elements.tileView = newView
                gameboard.addView(newView)
                gameboard.rowCount = viewModel.rows
                gameboard.columnCount = viewModel.columns
                newView.layoutParams.height = 80
                newView.layoutParams.width = 80
                //kalla på metod som sätter drawable beroende på isRevealed och state?
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
                        }
                    })
            }
    }

    // Kallar på funktion i Tile.kt som ändrar isFlagged till true/false
    // Skapa knapp som ändrar till flaggningstryck.
    fun toggleFlag(currentTile: Tile) {
        currentTile.toggleFlag()
        updateBoard(currentTile)
    }

    /** Kontrollerar hur många childviews gameBoard har och ska ändra vilken bild
     * som visas beroende på state. (fungerar ej helt?)
     * Redundant? varför har jag lagt till childviews? För att hitta och ändra view och
     * dess drawable?
     */
    fun setDrawables(currentView : ImageView, currentTile : Tile){
        lateinit var image : Drawable
        image = when(currentTile.state) {
            Tile.State.MINE -> resources.getDrawable(R.drawable.mine_tile)
            Tile.State.FLAGGED -> resources.getDrawable(R.drawable.flag_tile)
            Tile.State.HIDDEN -> resources.getDrawable(R.drawable.tile_hidden)
            // tilestate för detonerad bomb för alla eller de som ej flaggats?
            Tile.State.NUMBERED -> numberedTile(currentTile.numberOfMinedNeighbours)
        }
        currentView.setImageDrawable(image)
    }
    fun updateBoard(currentTile : Tile) {
        val imView = currentTile.tileView
        setDrawables(imView, currentTile)
    }

    fun revealBoard(){
        val revealAll = viewModel.gameBoardCells.flatten()
        for (tile in revealAll) {
            if (!tile.isRevealed) {
                tile.reveal()
            }
            updateBoard(tile)
        }
    }

    fun gameOver(currentTile : Tile) {
        revealBoard()
        currentTile.tileView.setImageDrawable(resources.getDrawable(R.drawable.mine_detonated))
        timer.stop()
        viewModel.isTimerRunning = false
        viewModel.isGameOver = true
        //viewModel.isRunning = false
        viewModel.elapsedTime = 0L


    }

    fun gameWon(){
        // Kollar hur många tiles som är revealed och adderar reavealedTiles
        var revealedTiles : Int = 0
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
            elapsedTime()
            val builder = AlertDialog.Builder(context)
            val input = EditText(context)
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)
            builder.setMessage("You won! ${elapsedTime()} \n"+"Please enter your username: " )
                .setPositiveButton("Confirm") { dialog, which ->
                    firebase(input.text.toString())
                }
            viewModel.isGameOver = true
            //viewModel.isRunning = false
            viewModel.elapsedTime = 0L

            val alert = builder.create()
            alert.show()
        }

    }

    fun firebase(playerName : String) {
        // create a player and it's time
        val elapsedTime = SystemClock.elapsedRealtime() - timer.base
        var totalSeconds = elapsedTime / 1000

        val user = hashMapOf(
            "Player name" to playerName,
            "Time in S" to totalSeconds
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

    fun elapsedTime(): String {
        val elapsedTime = SystemClock.elapsedRealtime() - timer.base
        // Omvandlar tid från millisekunder till sekunder m 2 decimaler
        var totalSeconds = elapsedTime/1000
        // om minuter blir mindre är 10 lägg på en nolla framför.
        var minutes = totalSeconds/60
        // om sekunder blir mindre är 10 lägg på en nolla framför.
        var seconds = totalSeconds%60
        return("Your time was $minutes minutes and $seconds seconds")
    }

    private fun resetBoard() {
        // om tile är mine, ta bort från gameboard.
        viewModel.gameBoardCells.flatten().filter { it.isMine }.forEach { tile -> tile.removeMine() }
        // om tile är avslöjad, göm den igen.
        viewModel.gameBoardCells.flatten().filter { it.isRevealed }.forEach { tile -> tile.hide() }
        // om tile är flaggad, ta bort flagga
        viewModel.gameBoardCells.flatten().filter { it.isFlagged }.forEach { tile -> tile.toggleFlag() }
        // ta bort view.
        gameboard.removeAllViews()
        // nollställa klocka
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

    /** Tanken är att funktionen ska sätta rätt numbered tile baserat på hur många
     * minor som finns i närheten (med hjälp av annan funktion?)
     */
    fun numberedTile(number: Int): Drawable = when (number) {
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

    // Ändrar state på Tile till mina? Returnerar lista så att jag ska kunna
    // kontrollera om det ändras, kan tar bort returtyp sen?
    fun plantMines() : List<Tile>{
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
        //var endast för testutskrift
        var testNum : String = ""
        for (row in 0 until viewModel.rows) {
            for (col in 0 until viewModel.columns) {
                if (!viewModel.gameBoardCells[row][col].isMine) {
                    val count = countAdjacentMines(row, col)
                    //endast för testutskrift
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
    fun setBaseTime() {
        binding.timer.base = SystemClock.elapsedRealtime() - viewModel.elapsedTime
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if(viewModel.isTimerRunning)
            viewModel.elapsedTime = SystemClock.elapsedRealtime() - timer.base
    }
}
