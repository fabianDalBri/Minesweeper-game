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
    private lateinit var easyList: TextView
    private lateinit var mediumList: TextView
    private lateinit var hardList: TextView
    private lateinit var homeBtn: Button
    private lateinit var questionBtn: Button
    private val db = Firebase.firestore
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        val view = binding.root

        easyList = binding.easyList
        mediumList = binding.mediumList
        hardList = binding.hardList
        homeBtn = binding.homeButton
        questionBtn = binding.questionButton

        getData()

        homeBtn.setOnClickListener {
                    view.findNavController().navigate(R.id.welcomeFragment)
        }

        questionBtn.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setMessage(
                "In the leaderboard below player name and game time is shown."
            ).setCancelable(true)

            val alert = builder.create()
            alert.show()
        }

        return view
    }

    /**
     * Gets data from database and displays it in the "playerList" textview.
     */
    fun getData() {
        var leaderboardEasy = "Easy: \n"
        var leaderboardMedium = "Medium: \n"
        var leaderboardHard = "Hard: \n"
        db.collection("Players").orderBy("Time in S")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                    if (document.get("Difficulty") == "Easy"){
                        leaderboardEasy += document.getString("Player name").toString() + " : " +
                                document.get("Time in S").toString()+" sec "+  "\n"
                    } else if (document.get("Difficulty") == "Medium"){
                        leaderboardMedium += document.getString("Player name").toString() + " : " +
                                document.get("Time in S").toString()+" sec "+  "\n"
                    } else {
                        leaderboardHard += document.getString("Player name").toString() + " : " +
                                document.get("Time in S").toString()+" sec " + "\n"
                    }

                }
                easyList.text = leaderboardEasy
                mediumList.text = leaderboardMedium
                hardList.text = leaderboardHard
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }
}


