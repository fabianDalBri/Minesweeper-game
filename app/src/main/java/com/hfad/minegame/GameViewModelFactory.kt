package com.hfad.minegame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class GameViewModelFactory(private val rows : Int, private val columns : Int, private val mines : Int) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass : Class<T>) : T {
        if(modelClass.isAssignableFrom(GameViewModel::class.java))
            return GameViewModel(rows, columns, mines) as T
        throw IllegalArgumentException("Unknown ViewModel")
    }
}