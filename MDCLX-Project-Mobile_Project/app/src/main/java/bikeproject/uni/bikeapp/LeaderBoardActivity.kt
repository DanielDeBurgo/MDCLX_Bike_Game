package bikeproject.uni.bikeapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_leader_board.*
import java.lang.ref.WeakReference

class LeaderBoardActivity : AppCompatActivity()
{
    companion object
    {
        private lateinit var activityRef: WeakReference<LeaderBoardActivity>

        val ref: LeaderBoardActivity? get() = activityRef.get()
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leader_board)

        Net.socket.emit(NetEvent.GetLeaderBoardResults.identifier)

        activityRef = WeakReference(this)
        Net.currentActivityRef = WeakReference(this)
    }

    fun onLeaderBoardResults(list: List<String>)
    {
        val resultBuilder = StringBuilder()

        var ct = 1
        for (it in list)
        {
            resultBuilder.append("#$ct $it\n\n")
            ct++
        }

        leaderboardTextView.text = resultBuilder.toString()
    }
}
