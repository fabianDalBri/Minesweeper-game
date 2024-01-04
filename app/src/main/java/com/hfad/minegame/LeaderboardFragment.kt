package com.hfad.minegame

/**
 * LeaderboardFragment gets data from the firestore data base and displays it in a textview so
 * players can see a sort of leaderbord with statistics from each game won!
 */
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.findNavController
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.hfad.minegame.databinding.FragmentLeaderboardBinding

class LeaderboardFragment : Fragment() {
    private lateinit var binding: FragmentLeaderboardBinding
    private lateinit var playerList: TextView
    private lateinit var homeBtn: Button
    private lateinit var questionBtn: Button
    private lateinit var backBtn: Button
    private val db = Firebase.firestore
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
                            document.get("Time in S").toString()+" sec | "+ document.get("Difficulty") + "\n"
                }
                playerList.text = leaderboardList
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }

        backBtn.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            //builder sets alert dialog message
            builder.setMessage("Are you sure you want to go to back?")
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

        homeBtn.setOnClickListener {
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

        questionBtn.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setMessage(
                "In the leaderboard below player name and game time is shown."
            ).setCancelable(true)

            val alert = builder.create()
            alert.show()
        }


        // Inflate the layout for this fragment
        return view
    }
}


