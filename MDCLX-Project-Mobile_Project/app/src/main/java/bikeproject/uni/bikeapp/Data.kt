package bikeproject.uni.bikeapp

import java.util.*

data class Player(val name: String, val gamesPlayed: Int, val highscore: Int, val bestTime: Int, val highScoreTimeStamp: Date)
{
    val credit get() = highscore * gamesPlayed
}