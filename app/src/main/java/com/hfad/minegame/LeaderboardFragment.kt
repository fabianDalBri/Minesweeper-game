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
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.firebase.Firebase
import com.google.firebase.firestore.auth.FirebaseAuthCredentialsProvider
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.getField
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
        backBtn = binding.BackButton
        homeBtn = binding.homeButton
        questionBtn = binding.questionButton

        var leaderboardList = ""
        db.collection("Players").orderBy("Time in S")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                    leaderboardList += document.getString("Player name").toString() + " : " +
                            document.get("Time in S").toString()+ " sec"+"\n"
                }
                playerList.text = leaderboardList
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }

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


        // Inflate the layout for this fragment
        return view
    }

    }


