package bikeproject.uni.bikeapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.camnter.easyrecyclerview.adapter.EasyRecyclerViewAdapter
import com.camnter.easyrecyclerview.holder.EasyRecyclerViewHolder
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.android.synthetic.main.activity_manager.*
import org.json.JSONArray
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*


class PlayerAdapter : EasyRecyclerViewAdapter()
{
    override fun getItemLayouts() = intArrayOf(R.layout.user_card)

    override fun onBindRecycleViewHolder(viewHolder: EasyRecyclerViewHolder, position: Int)
    {
        val data     = this.getItem<Player>(position)

        viewHolder.findViewById<TextView>(R.id.userCardName).text = data.name
        viewHolder.findViewById<TextView>(R.id.userCardGamesPlayed).text = "Games played: ${data.gamesPlayed.toString()}"
        viewHolder.findViewById<TextView>(R.id.userCardHighscore).text = "Highscore: ${data.highscore.toString()}"
        viewHolder.findViewById<TextView>(R.id.userCardBestTime).text = "Best time: ${data.bestTime.toString()}"
    }

    override fun getRecycleViewItemType(position: Int) = 0
}

class ManagerActivity : AppCompatActivity()
{
    companion object
    {
        private lateinit var activityRef: WeakReference<ManagerActivity>

        val ref: ManagerActivity? get() = activityRef.get()
    }

    //@Todo: Overall credit gained by all users
    val playersTestData = arrayListOf(
            Player("test0", gamesPlayed= 0, highscore= 0,   bestTime= 0,   highScoreTimeStamp= Date(20130526160000)),
            Player("test1", gamesPlayed= 0, highscore= 100, bestTime= 100, highScoreTimeStamp= Date(20130526160000)),
            Player("test2", gamesPlayed= 0, highscore= 240, bestTime= 120, highScoreTimeStamp= Date(20130526160000)),
            Player("test3", gamesPlayed= 0, highscore= 350, bestTime= 130, highScoreTimeStamp= Date(20130526160000)),
            Player("test4", gamesPlayed= 0, highscore= 500, bestTime= 140, highScoreTimeStamp= Date(20130526160000)),
            Player("test5", gamesPlayed= 0, highscore= 430, bestTime= 160, highScoreTimeStamp= Date(20130526160000)),
            Player("test6", gamesPlayed= 0, highscore= 200, bestTime= 170, highScoreTimeStamp= Date(20130526160000)),
            Player("test7", gamesPlayed= 0, highscore= 320, bestTime= 200, highScoreTimeStamp= Date(20130526160000))
    )

    val players = ArrayList<Player>()

    val recyclerViewAdapter = PlayerAdapter()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager)

        activityRef = WeakReference(this)

        setChart()

        searchButton.setOnClickListener {
            val name = searchTextBox.text.toString()

            if (name.isEmpty())
            {
                recyclerViewAdapter.list = players
            }
            else
            {
                recyclerViewAdapter.list = players.filter { it.name == name }
            }

            recyclerViewAdapter.notifyDataSetChanged()
        }

        playerList.adapter = recyclerViewAdapter

        Net.socket.emit(NetEvent.GetManagerInfo.identifier)
    }

    @SuppressLint("SimpleDateFormat")
    fun parseDate(date: String): Date
    {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")

        val splitDateString = date.split(" ")

        val month = when (splitDateString[1])
        {
            "Jan" -> 1
            "Feb" -> 2
            "Mar" -> 3
            "Apr" -> 4
            "May" -> 5
            "Jun" -> 6
            "Jul" -> 7
            "Aug" -> 8
            "Sep" -> 9
            "Oct" -> 10
            "Nov" -> 11
            "Dec" -> 12

            else -> 0
        }

        val day  = splitDateString[2].toInt()
        val year = splitDateString[3].toInt()
        val time = splitDateString[4].removeRange(5, splitDateString[4].length)

        val dateString = "$year-$month-$day $time"

        val dateTime = dateFormat.parse(dateString)

        return dateTime
    }

    fun onManagerInfoReceived(data: Array<Any>)
    {
        val names       = data[0] as JSONArray
        val scores      = data[1] as JSONArray
        val bestTimes   = data[2] as JSONArray
        val timestamps  = data[3] as JSONArray
        val gamesPlayed = data[4] as JSONArray

        for (i in 0 until names.length())
        {
            players.add(Player(names.getString(i), gamesPlayed.getInt(i), scores.getInt(i), bestTimes.getInt(i), parseDate(timestamps.getString(i))))
        }

        recyclerViewAdapter.list = players
        recyclerViewAdapter.notifyDataSetChanged()

        setChart()
    }

    fun setChart()
    {
        val entries = ArrayList<Entry>()

        for (player in players)
        {
            entries.add(Entry(player.bestTime.toFloat(), player.highscore.toFloat()))
        }

        val lineDataSet = LineDataSet(entries, "Player scores")

        lineDataSet.circleRadius = 5f
        lineDataSet.lineWidth    = 5f

        val description = Description()
        description.text = "X axis: player score\nY axis: player time"

        chart.data = LineData(lineDataSet)
        chart.description = description
        chart.zoom(0.3f, 0.3f, 0f, 0f)
        chart.setPinchZoom(true)
        chart.invalidate()
    }
}
