package com.hfad.minegame
/**
    The gamefragment were the game will be displayed on.
 */

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.SystemClock
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Chronometer
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.hfad.minegame.databinding.FragmentGameBinding
import kotlin.math.min
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class GameFragment : Fragment(){

    private lateinit var binding: FragmentGameBinding
    lateinit var rootView : LinearLayout
    lateinit var gameboard : GridLayout
    lateinit var gameBoardCells : List<List<Tile>>
    lateinit var resetBtn : Button
    lateinit var timer : Chronometer
    lateinit var viewModel: GameViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentGameBinding.inflate(inflater, container, false)
        val view = binding.root
        viewModel = ViewModelProvider(this)[GameViewModel::class.java]

        rootView = binding.rootLayout
        gameboard = binding.gameBoard
        resetBtn = binding.resetButton
        timer = binding.timer

        resetBtn.setOnClickListener(){
            setUpGame()
        }
        //Skapar bräde med celler, lista med listor rows*columns, där varje cell består
        //av objekt av typen Tile
        gameBoardCells = List(viewModel.rows){ List(viewModel.columns) { Tile()}}

        setUpGame()
        // Inflate the layout for this fragment
        return view
    }
    fun setText(text: String){
        binding.testText.text = text
    }

    /** Skapar en ny ImageView för varje Tile-objekt i spelbrädet
     *
     */
    @SuppressLint("SuspiciousIndentation")
    fun setUpGame() {
        viewModel.firstClick = true
        resetBoard()
        plantMines()
        calculateNumbers()
        for (array in gameBoardCells)
            for (elements in array) {
                elements.row = gameBoardCells.indexOf(array)
                elements.col = array.indexOf(elements)

                //elements.reveal()
                val newView: ImageView = ImageView(context)

                elements.tileView = newView
                gameboard.addView(newView)
                newView.layoutParams.height = 80
                newView.layoutParams.width = 80
                //kalla på metod som sätter drawable beroende på isRevealed och state?
                setDrawables(newView, elements)

                newView.setOnClickListener(View.OnClickListener {
                    if(!elements.isRevealed && !elements.isFlagged){
                        revealCell(elements.row, elements.col)
                        gameWon()
                    }
                    if (viewModel.firstClick && !elements.isMine){
                        setBaseTime()
                        timer.start()
                        viewModel.firstClick = false
                    }
                })
                newView.setOnLongClickListener(View.OnLongClickListener {
                    toggleFlag(elements)
                    true
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
        when(currentTile.state) {
            Tile.State.MINE -> image = resources.getDrawable(R.drawable.mine_tile)
            Tile.State.FLAGGED -> image = resources.getDrawable(R.drawable.flag_tile)
            Tile.State.HIDDEN -> image = resources.getDrawable(R.drawable.tile_hidden)
            // tilestate för detonerad bomb för alla eller de som ej flaggats?
            Tile.State.NUMBERED -> image = numberedTile(currentTile.numberOfMinedNeighbours)
        }
        currentView.setImageDrawable(image)
    }
    fun updateBoard(currentTile : Tile) {
        val imView = currentTile.tileView
        setDrawables(imView, currentTile)
    }

    fun revealBoard(){
        val revealAll = gameBoardCells.flatten()
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
        if (!viewModel.firstClick){
            setText("You lost! ${elapsedTime()}")
        }else
            setText("You lost! Your time was 0:00")
    }

    fun gameWon(){
        // Kollar hur många tiles som är revealed och adderar reavealedTiles
        var revealedTiles : Int = 0
        val totalAmountOfTiles : Int = viewModel.rows*viewModel.columns
        for(array in gameBoardCells){
            for (elements in array){
                if (elements.isRevealed)
                    revealedTiles++
            }
        }
        if (revealedTiles == totalAmountOfTiles - viewModel.mines){
            revealBoard()
            timer.stop()
            elapsedTime()
            setText("You won! ${elapsedTime()}")
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
        gameBoardCells.flatten().filter { it.isMine }.forEach { tile -> tile.removeMine() }
        // om tile är avslöjad, göm den igen.
        gameBoardCells.flatten().filter { it.isRevealed }.forEach { tile -> tile.hide() }
        // ta bort view.
        gameboard.removeAllViews()
        // ta bort text
        setText("")
        // nollställa klocka
        setBaseTime()
    }

    private fun revealCell(row : Int, col : Int) {
        val currentTile = gameBoardCells[row][col]
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
                if (newRow in 0 until viewModel.rows && newCol in 0 until viewModel.columns && !gameBoardCells[newRow][newCol].isRevealed) {
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
        val allTiles = gameBoardCells.flatten()
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
        var testNum : String = ""
        for (row in 0 until viewModel.rows) {
            for (col in 0 until viewModel.columns) {
                if (!gameBoardCells[row][col].isMine) {
                    val count = countAdjacentMines(row, col)
                    testNum += count
                    gameBoardCells[row][col].numberOfMinedNeighbours = count
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
                if (newRow in 0 until viewModel.rows && newCol in 0 until viewModel.columns && gameBoardCells[newRow][newCol].isMine) {
                    count++
                }
            }
        }
        return count
    }
    fun setBaseTime() {
        binding.timer.base = SystemClock.elapsedRealtime() - 0
    }
}
