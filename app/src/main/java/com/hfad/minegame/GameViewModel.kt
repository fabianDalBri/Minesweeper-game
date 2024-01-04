package com.hfad.minegame

/**
Klass som innehåller ....
 */

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class GameViewModel(row : Int, col : Int, mines : Int) : ViewModel(){
    var rows = row
    var columns = col
    var mines = mines
    var flagCount = mines
    var firstClick = true
    var gameBoardCells = List(rows){ List(columns) { Tile()}}
    var isTimerRunning = false
    var elapsedTime = 0L
    // någon form av kontroll så att brädet och timer stannar som de är om man roterar skärmen?
    var isGameOver = false

}

