package com.hfad.minegame

/**
Klass som innehåller ....
 */

import androidx.lifecycle.ViewModel

class GameViewModel : ViewModel(){
    var rows = 8
    var columns = 8
    var mines = 10
    var firstClick = true
    var gameBoardCells = List(rows){ List(columns) { Tile()}}
    var isRunning = false
    var elapsedTime = 0L
    // någon form av kontroll så att brädet och timer stannar som de är om man roterar skärmen?
    var gameLost = false




}