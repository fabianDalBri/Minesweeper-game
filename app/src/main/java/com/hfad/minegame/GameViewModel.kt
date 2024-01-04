package com.hfad.minegame

/**
 * GameViewModel is saving state and saving variables for the gamefragment on configuration changes.
 */

import androidx.lifecycle.ViewModel

class GameViewModel(row : Int, col : Int, mines : Int, level : String) : ViewModel(){
    var rows = row
    var columns = col
    var mines = mines
    var difficulty = level
    var flagCount = mines
    var firstClick = true
    var gameBoardCells = List(rows){ List(columns) { Tile()}}
    var isTimerRunning = false
    var elapsedTime = 0L
    var isGameOver = false
}

