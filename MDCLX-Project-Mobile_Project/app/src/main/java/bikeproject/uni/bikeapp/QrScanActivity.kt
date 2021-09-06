package bikeproject.uni.bikeapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.SurfaceView
import android.widget.TextView
import github.nisrulz.qreader.QREader
import java.lang.ref.WeakReference



const val QrScanActivityReturnCode = 'q'.toInt() + 'r'.toInt()

class QrScanActivity : AppCompatActivity()
{
    lateinit var qrReader:    QREader
    lateinit var surfaceView: SurfaceView
    lateinit var text:        TextView

    companion object
    {
        private lateinit var activityRef: WeakReference<MainActivity>

        val ref: MainActivity? get() = activityRef.get()
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_scan)

        surfaceView = findViewById(R.id.qr_view)
        text = findViewById(R.id.qr_text)

        qrReader = QREader.Builder(this, surfaceView,
        {
            val returnIntent = Intent()
            returnIntent.putExtra("result", it)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        })
        .facing(QREader.BACK_CAM)
        .enableAutofocus(true)
        .height(surfaceView.width)
        .width(surfaceView.height)
        .build()

        qrReader.initAndStart(surfaceView)

        Net.currentActivityRef = WeakReference(this)
    }

    override fun onResume()
    {
        super.onResume()

        qrReader.initAndStart(surfaceView)
    }

    override fun onPause()
    {
        super.onPause()

        qrReader.initAndStart(surfaceView)
    }
}
