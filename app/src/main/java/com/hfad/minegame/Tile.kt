package com.hfad.minegame

import android.widget.ImageView

/**
 * The Tile class represents every tile on the gameboard, and is responsible for
 * changing the states of each tile.
 *
 * Row, column and tileView are assigned values when the game is set up and then
 * used to access specific tiles and their image views.
 */
class Tile {
    var row : Int = 0
    var col : Int = 0
    lateinit var tileView : ImageView
    enum class State {
        HIDDEN,
        FLAGGED,
        MINE,
        NUMBERED
    }

    var isMine = false
    fun plantMine() {
        isMine = true
    }

    fun hide() {
        if(!isRevealed) return
        isRevealed = false
    }

    fun reveal() {
        if(isRevealed) return
        isRevealed = true
    }

    fun toggleFlag() {
        isFlagged = !isFlagged
    }
    fun removeMine() {
        isMine = false
    }

    var numberOfMinedNeighbours = 0
    var isFlagged = false
    var isRevealed = false
    val state: State
        get() = when (isRevealed) {
            true -> when(isMine) {
                true -> State.MINE
                false -> State.NUMBERED
            }
            false -> when (isFlagged) {
                true -> State.FLAGGED
                false -> State.HIDDEN
            }
        }

    override fun toString(): String {
        return when (state) {
            State.MINE -> "Mine"
            State.HIDDEN -> "Hidden"
            State.FLAGGED -> "Flagged"
            State.NUMBERED -> numberOfMinedNeighbours.toString()
        }
    }
}
