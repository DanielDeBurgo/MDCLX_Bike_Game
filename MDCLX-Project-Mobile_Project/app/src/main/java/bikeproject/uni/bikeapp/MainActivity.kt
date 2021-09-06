package bikeproject.uni.bikeapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity()
{
    companion object
    {
        private lateinit var activityRef: WeakReference<MainActivity>

        val ref: MainActivity? get() = activityRef.get()
    }

    var connectedToVr = false

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        activityRef            = WeakReference(this)
        Net.currentActivityRef = WeakReference(this)

        usernameTextBox.text = "Welcome, ${Net.CurrentUserName}"

        connectToVrButton.setOnClickListener { startActivityForResult<MainActivity, QrScanActivity>(QrScanActivityReturnCode) }
        connectToWebsiteButton.setOnClickListener { connectToWebsiteButton.isEnabled = false }

        viewLeaderBoardButton.setOnClickListener { startActivity<LeaderBoardActivity>() }
        customizeButton.setOnClickListener       { startActivity<CustomizationActivity>() }

        Net.socket.emit(NetEvent.RequestLeaderboardPosition.identifier)
    }

    fun onGetLeaderboardPosition(leaderboardSpot: Int)
    {
        leaderboardTextView.text = "Leaderboard spot: $leaderboardSpot"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        if (requestCode == QrScanActivityReturnCode)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                val result = data!!.getStringExtra("result")

                Net.socket.emit(NetEvent.ConnectToVr.identifier, result)

                connectToVrButton.isEnabled = false
                connectToVrButton.text = getString(R.string.vr_connected)

                connectedToVr = true
            }

            if (resultCode == Activity.RESULT_CANCELED)
            {
                //Write your code if there's no result
            }
        }
    }

    fun onVrDisconnect()
    {
        connectToVrButton.isEnabled = true
        connectToVrButton.text = getString(R.string.connect_to_vr)
    }
}
