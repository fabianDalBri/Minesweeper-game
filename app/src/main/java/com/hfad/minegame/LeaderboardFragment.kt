package com.hfad.minegame

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Chronometer
import android.widget.GridLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.hfad.minegame.databinding.FragmentGameBinding
import com.hfad.minegame.databinding.FragmentLeaderboardBinding

class LeaderboardFragment : Fragment() {
    private lateinit var binding: FragmentLeaderboardBinding
    lateinit var rootView: ConstraintLayout
    lateinit var playerList: TextView
    lateinit var refreshBtn: Button
    lateinit var homeBtn: Button
    lateinit var questionBtn: Button
    lateinit var backBtn: Button
    val db = Firebase.firestore
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        val view = binding.root

        playerList = binding.playerList
        refreshBtn = binding.refreshButton
        backBtn = binding.BackButton
        homeBtn = binding.homeButton
        questionBtn = binding.questionButton

        backBtn.setOnClickListener() {
            val builder = AlertDialog.Builder(context)
            //builder sets alert dialog message
            builder.setMessage("Are you sure you want to go to back?")
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

        homeBtn.setOnClickListener() {
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

        questionBtn.setOnClickListener() {
            val builder = AlertDialog.Builder(context)
            builder.setMessage(
                "How to play: \n" +
                        "Reveal all none mine tiles to win the game.\n" +
                        "Use the flag button to flag potential mines."
            ).setCancelable(true)

            val alert = builder.create()
            alert.show()
        }

        refreshBtn.setOnClickListener {
            var players = getFirebaseInfo()
            playerList.text = players.toString()
        }
        // Inflate the layout for this fragment
        return view
    }

    fun getFirebaseInfo() {
        val collectionRef = db.collection("Leaderboard").document("Players")
        collectionRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
        collectionRef.get()
    }
}